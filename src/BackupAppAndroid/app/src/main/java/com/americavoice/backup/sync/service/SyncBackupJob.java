/*
 * Nextcloud Android client application
 *
 * @author Tobias Kaminsky
 * Copyright (C) 2017 Tobias Kaminsky
 * Copyright (C) 2017 Nextcloud GmbH.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.americavoice.backup.sync.service;

import android.Manifest;
import android.accounts.Account;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.IBinder;
import android.provider.CallLog;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.text.format.DateFormat;

import com.americavoice.backup.authentication.AccountUtils;
import com.americavoice.backup.calls.ui.CallsBackupFragment;
import com.americavoice.backup.calls.ui.model.Call;
import com.americavoice.backup.datamodel.ArbitraryDataProvider;
import com.americavoice.backup.datamodel.FileDataStorageManager;
import com.americavoice.backup.datamodel.OCFile;
import com.americavoice.backup.files.service.FileUploader;
import com.americavoice.backup.operations.UploadFileOperation;
import com.americavoice.backup.service.OperationsService;
import com.americavoice.backup.utils.BaseConstants;
import com.evernote.android.job.Job;
import com.evernote.android.job.util.support.PersistableBundleCompat;
import com.owncloud.android.lib.common.utils.Log_OC;

import org.json.JSONArray;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Vector;

/**
 * Job that backup contacts to /Contacts-Backup and deletes files older than x days
 */

public class SyncBackupJob extends Job {
    public static final String TAG = "SyncBackupJob";
    public static final String ACCOUNT = "account";
    public static final String PENDING_VIDEOS = "PENDING_VIDEOS";
    public static final String PENDING_PHOTOS = "PENDING_PHOTOS";
    public static final String FORCE = "force";


    @NonNull
    @Override
    protected Result onRunJob(Params params) {

        PersistableBundleCompat bundle = params.getExtras();

        boolean force = bundle.getBoolean(FORCE, false);

        final Context context = getContext();

        final Account account = AccountUtils.getOwnCloudAccountByName(context, bundle.getString(ACCOUNT, ""));

        final String[] pendingPhotos = bundle.getStringArray(PENDING_PHOTOS);

        final String[] pendingVideos = bundle.getStringArray(PENDING_VIDEOS);

        backup(account, pendingPhotos, pendingVideos);

        return Result.SUCCESS;
    }

    private void backup(Account account, String[] pendingPhotos, String[] pendingVideos) {

        if (pendingPhotos != null)
        {
            for (String item : pendingPhotos)
            {
                try
                {
                    File file = new File(item);
                    FileUploader.UploadRequester requester = new FileUploader.UploadRequester();
                    requester.uploadNewFile(
                            getContext(),
                            account,
                            file.getAbsolutePath(),
                            "/Photos/" + getFileName(item),
                            FileUploader.LOCAL_BEHAVIOUR_MOVE,
                            null,
                            true,
                            UploadFileOperation.CREATED_BY_USER
                    );
                } catch (Exception e){}

            }
        }

        if (pendingVideos != null)
        {
            for (String item : pendingVideos)
            {
                try
                {
                    File file = new File(item);
                    FileUploader.UploadRequester requester = new FileUploader.UploadRequester();
                    requester.uploadNewFile(
                            getContext(),
                            account,
                            file.getAbsolutePath(),
                            "/Videos/" + getFileName(item),
                            FileUploader.LOCAL_BEHAVIOUR_MOVE,
                            null,
                            true,
                            UploadFileOperation.CREATED_BY_USER
                    );
                } catch (Exception e){}
            }
        }


    }

    private String getFileName(String path)
    {
        return path.substring(path.lastIndexOf('/') + 1);
    }
}
