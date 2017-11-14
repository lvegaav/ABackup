package com.americavoice.backup.service;

//BEGIN_INCLUDE(job)

import android.Manifest;
import android.accounts.Account;
import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.americavoice.backup.Const;
import com.americavoice.backup.authentication.AccountUtils;
import com.americavoice.backup.datamodel.ArbitraryDataProvider;
import com.americavoice.backup.db.PreferenceManager;
import com.americavoice.backup.explorer.ui.FileListFragment;
import com.americavoice.backup.files.service.FileUploader;
import com.americavoice.backup.files.utils.FileUtils;
import com.americavoice.backup.operations.UploadFileOperation;
import com.americavoice.backup.utils.BaseConstants;
import com.americavoice.backup.utils.FileStorageUtils;
import com.americavoice.backup.utils.JobSchedulerUtils;
import com.crashlytics.android.Crashlytics;
import com.owncloud.android.lib.common.utils.Log_OC;

import java.util.ArrayList;
import java.util.List;

/**
 * Example stub job to monitor when there is a change to photos in the media provider.
 */
@RequiresApi(api = Build.VERSION_CODES.N)
public class MediaContentJob extends JobService {
    // The root URI of the media provider, to monitor for generic changes to its content.
    static final Uri MEDIA_URI = Uri.parse("content://" + MediaStore.AUTHORITY + "/");

    // Path segments for image-specific URIs in the provider.
    static final List<String> EXTERNAL_PATH_SEGMENTS
            = MediaStore.Images.Media.EXTERNAL_CONTENT_URI.getPathSegments();

    // The columns we want to retrieve about a particular image.
    static final String[] PROJECTION = new String[] {
            MediaStore.Images.ImageColumns._ID, MediaStore.Images.ImageColumns.DATA
    };
    static final int PROJECTION_ID = 0;
    static final int PROJECTION_DATA = 1;

    // This is the external storage directory where cameras place pictures.
    static final String DCIM_DIR = Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_DCIM).getPath();

    // A pre-built JobInfo we use for scheduling our job.
    static final JobInfo JOB_INFO;
    private static final String TAG = MediaContentJob.class.getName();

    static {
        JobInfo.Builder builder = new JobInfo.Builder(JobIds.MEDIA_CONTENT_JOB,
                new ComponentName(Const.AUTHORITY, MediaContentJob.class.getName()));
        // Look for specific changes to images in the provider.
        builder.addTriggerContentUri(new JobInfo.TriggerContentUri(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        JobInfo.TriggerContentUri.FLAG_NOTIFY_FOR_DESCENDANTS)
                )
                .addTriggerContentUri(new JobInfo.TriggerContentUri(
                        MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                        JobInfo.TriggerContentUri.FLAG_NOTIFY_FOR_DESCENDANTS)
                );
        // Also look for general reports of changes in the overall provider.
        builder.addTriggerContentUri(new JobInfo.TriggerContentUri(MEDIA_URI, 0));
        JOB_INFO = builder.build();
    }

    // Fake job work.  A real implementation would do some work on a separate thread.
    final Handler mHandler = new Handler();
    final Runnable mWorker = new Runnable() {
        @Override public void run() {
            scheduleJob(MediaContentJob.this);
            jobFinished(mRunningParams, false);
        }
    };

    JobParameters mRunningParams;

    // Schedule this job, replace any existing one.
    public static void scheduleJob(Context context) {
        JobSchedulerUtils.scheduleJob(context, JOB_INFO);
    }

    // Check whether this job is currently scheduled.
    public static boolean isScheduled(Context context) {
        return JobSchedulerUtils.isScheduled(context, JobIds.MEDIA_CONTENT_JOB);
    }

    // Cancel this job, if currently scheduled.
    public static void cancelJob(Context context) {
        JobSchedulerUtils.cancelJob(context, JobIds.MEDIA_CONTENT_JOB);
    }

    @Override
    public boolean onStartJob(JobParameters params) {
        Log.i(TAG, "JOB STARTED!");
        mRunningParams = params;

        // Instead of real work, we are going to build a string to show to the user.
        StringBuilder sb = new StringBuilder();

        // Did we trigger due to a content change?
        if (params.getTriggeredContentAuthorities() != null) {
            boolean rescanNeeded = false;

            if (params.getTriggeredContentUris() != null) {
                // If we have details about which URIs changed, then iterate through them
                // and collect either the ids that were impacted or note that a generic
                // change has happened.
                ArrayList<String> ids = new ArrayList<>();
                for (Uri uri : params.getTriggeredContentUris()) {
                    handleNewFileAction(getApplicationContext(), uri);
                    List<String> path = uri.getPathSegments();
                    if (path != null && path.size() == EXTERNAL_PATH_SEGMENTS.size()+1) {
                        // This is a specific file.
                        ids.add(path.get(path.size()-1));
                    } else {
                        // Oops, there is some general change!
                        rescanNeeded = true;
                    }
                }

                if (ids.size() > 0) {
                    // If we found some ids that changed, we want to determine what they are.
                    // First, we do a query with content provider to ask about all of them.
                    StringBuilder selection = new StringBuilder();
                    for (int i=0; i<ids.size(); i++) {
                        if (selection.length() > 0) {
                            selection.append(" OR ");
                        }
                        selection.append(MediaStore.Images.ImageColumns._ID);
                        selection.append("='");
                        selection.append(ids.get(i));
                        selection.append("'");
                    }

                    // Now we iterate through the query, looking at the filenames of
                    // the items to determine if they are ones we are interested in.
                    Cursor cursor = null;
                    boolean haveFiles = false;
                    try {
                        cursor = getContentResolver().query(
                                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                                PROJECTION, selection.toString(), null, null);
                        while (cursor.moveToNext()) {
                            // We only care about files in the DCIM directory.
                            String dir = cursor.getString(PROJECTION_DATA);
                            if (dir.startsWith(DCIM_DIR)) {
                                if (!haveFiles) {
                                    haveFiles = true;
                                    sb.append("New photos:\n");
                                }
                                sb.append(cursor.getInt(PROJECTION_ID));
                                sb.append(": ");
                                sb.append(dir);
                                sb.append("\n");
                            }
                        }
                    } catch (SecurityException e) {
                        sb.append("Error: no access to media!");
                    } finally {
                        if (cursor != null) {
                            cursor.close();
                        }
                    }
                }

            } else {
                // We don't have any details about URIs (because too many changed at once),
                // so just note that we need to do a full rescan.
                rescanNeeded = true;
            }

            if (rescanNeeded) {
                sb.append("Photos rescan needed!");
            }
        } else {
            sb.append("(No photos content)");
        }
        Log.i(TAG, sb.toString());

        // We will emulate taking some time to do this work, so we can see batching happen.
        mHandler.postDelayed(mWorker, 10*1000);
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        mHandler.removeCallbacks(mWorker);
        return false;
    }

    private void handleNewFileAction(Context context, Uri fileUri) {
        try {
            Cursor c;
            String file_path;
            String file_name;
            String mime_type;
            long date_taken;

            Log_OC.i(TAG, "New photo received");

            Account account = AccountUtils.getCurrentOwnCloudAccount(context);
            if (account == null) {
                Log_OC.w(TAG, "No account found for instant upload, aborting");
                return;
            }
            ArbitraryDataProvider arbitraryDataProvider = new ArbitraryDataProvider(getContentResolver());
            final boolean photosBackupEnabled = arbitraryDataProvider.getBooleanValue(account, FileListFragment.PREFERENCE_PHOTOS_AUTOMATIC_BACKUP);
            final boolean videosBackupEnabled = arbitraryDataProvider.getBooleanValue(account, FileListFragment.PREFERENCE_VIDEOS_AUTOMATIC_BACKUP);
            String data, displayName, mimeType, size;
            if (!isVideoContentUri(fileUri.toString())) {
                data = MediaStore.Images.Media.DATA;
                displayName = MediaStore.Images.Media.DISPLAY_NAME;
                mimeType = MediaStore.Images.Media.MIME_TYPE;
                size = MediaStore.Images.Media.SIZE;
                if (!photosBackupEnabled){
                    Log_OC.w(TAG, "No automatic backup for photos, aborting");
                    return;
                }
            } else {
                data = MediaStore.Video.Media.DATA;
                displayName = MediaStore.Video.Media.DISPLAY_NAME;
                mimeType = MediaStore.Video.Media.MIME_TYPE;
                size = MediaStore.Video.Media.SIZE;
                if (!videosBackupEnabled){
                    Log_OC.w(TAG, "No automatic backup for videos, aborting");
                    return;
                }
            }


            String[] CONTENT_PROJECTION = {
                    data, displayName, mimeType, size};

            int permissionCheck = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE);

            if (android.content.pm.PackageManager.PERMISSION_GRANTED != permissionCheck) {
                Log_OC.w(TAG, "Read external storage permission isn't granted, aborting");
                return;
            }

            c = context.getContentResolver().query(fileUri, CONTENT_PROJECTION, null, null, null);
            if (c != null) {
                if (!c.moveToFirst()) {
                    Log_OC.e(TAG, "Couldn't resolve given uri: " + fileUri);
                    return;
                }
                file_path = c.getString(c.getColumnIndex(data));
                file_name = c.getString(c.getColumnIndex(displayName));
                mime_type = c.getString(c.getColumnIndex(mimeType));
                date_taken = System.currentTimeMillis();
                c.close();
            } else {
                return;
            }

            Log_OC.d(TAG, "Path: " + file_path + "");

            if (file_path.startsWith(FileUtils.EXTERNAL_FILES_PATH)){
                return;
            }

            new FileUploader.UploadRequester();

            int behaviour = FileUploader.LOCAL_BEHAVIOUR_FORGET;
            Boolean subfolderByDate = PreferenceManager.instantPictureUploadPathUseSubfolders(context);
            String uploadPath;
            int createdBy;
            if (isVideoContentUri(fileUri.toString())) {
                uploadPath = BaseConstants.VIDEOS_REMOTE_FOLDER;
                createdBy = UploadFileOperation.CREATED_AS_INSTANT_VIDEO;
            } else {
                uploadPath = BaseConstants.PHOTOS_REMOTE_FOLDER;
                createdBy = UploadFileOperation.CREATED_AS_INSTANT_PICTURE;
            }

            FileUploader.UploadRequester requester = new FileUploader.UploadRequester();
            requester.uploadNewFile(
                    context,
                    account,
                    file_path,
                    FileStorageUtils.getInstantUploadFilePath(uploadPath, file_name, date_taken, subfolderByDate),
                    behaviour,
                    mime_type,
                    true,           // create parent folder if not existent
                    createdBy
            );
        } catch (Exception e) {
            Log_OC.e(TAG, e.getMessage());
            Crashlytics.logException(e);
        }
    }

    private boolean isVideoContentUri(String mimeType) {
        return mimeType.startsWith(MediaStore.Video.Media.EXTERNAL_CONTENT_URI.toString());
    }

}