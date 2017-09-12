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

package com.americavoice.backup.sms.service;

import android.Manifest;
import android.accounts.Account;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.IBinder;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.text.format.DateFormat;

import com.americavoice.backup.authentication.AccountUtils;
import com.americavoice.backup.calls.ui.CallsBackupFragment;
import com.americavoice.backup.datamodel.ArbitraryDataProvider;
import com.americavoice.backup.datamodel.FileDataStorageManager;
import com.americavoice.backup.datamodel.OCFile;
import com.americavoice.backup.files.service.FileUploader;
import com.americavoice.backup.operations.UploadFileOperation;
import com.americavoice.backup.service.OperationsService;
import com.americavoice.backup.sms.ui.SmsBackupFragment;
import com.americavoice.backup.sms.ui.model.Sms;
import com.americavoice.backup.utils.BaseConstants;
import com.evernote.android.job.Job;
import com.evernote.android.job.util.support.PersistableBundleCompat;
import com.owncloud.android.lib.common.utils.Log_OC;

import org.json.JSONArray;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Vector;

/**
 * Job that backup sms
 */

public class SmsBackupJob extends Job {
    public static final String TAG = "SmsBackupJob";
    public static final String ACCOUNT = "account";
    public static final String FORCE = "force";
    private OperationsServiceConnection operationsServiceConnection;
    private OperationsService.OperationsServiceBinder operationsServiceBinder;


    @NonNull
    @Override
    protected Result onRunJob(Params params) {

        PersistableBundleCompat bundle = params.getExtras();

        boolean force = bundle.getBoolean(FORCE, false);

        final Context context = getContext();

        final Account account = AccountUtils.getOwnCloudAccountByName(context, bundle.getString(ACCOUNT, ""));

        ArbitraryDataProvider arbitraryDataProvider = new ArbitraryDataProvider(getContext().getContentResolver());
        Long lastExecution = arbitraryDataProvider.getLongValue(account, SmsBackupFragment.PREFERENCE_SMS_LAST_BACKUP);

        if (force || (lastExecution + 24 * 60 * 60 * 1000) < Calendar.getInstance().getTimeInMillis()) {
            Log_OC.d(TAG, "start sms backup job");

            String backupFolder = BaseConstants.SMS_BACKUP_FOLDER +
                    OCFile.PATH_SEPARATOR;
            Integer daysToExpire = BaseConstants.SMS_BACKUP_EXPIRE;

            backupSms(account, backupFolder);

            // bind to Operations Service
            operationsServiceConnection = new OperationsServiceConnection(daysToExpire, backupFolder, account);

            getContext().bindService(new Intent(getContext(), OperationsService.class), operationsServiceConnection,
                    OperationsService.BIND_AUTO_CREATE);

            // store execution date
            arbitraryDataProvider.storeOrUpdateKeyValue(account,
                    SmsBackupFragment.PREFERENCE_SMS_LAST_BACKUP,
                    String.valueOf(Calendar.getInstance().getTimeInMillis()));
        } else {
            Log_OC.d(TAG, "last execution less than 24h ago");
        }

        return Result.SUCCESS;
    }

    private void backupSms(Account account, String backupFolder) {

        try {
            List<String> smsList = new ArrayList<>();

            if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED) {
                return;
            }

            Uri message = Uri.parse("content://sms/");

            Cursor c = getContext().getContentResolver().query(message, null, null, null, null);
            if (c != null) {
                int totalSMS = c.getCount();

                if (c.moveToFirst()) {
                    for (int i = 0; i < totalSMS; i++) {

                        Sms objSms = new Sms();
                        objSms.setAddress(c.getString(c
                                .getColumnIndexOrThrow("address")));
                        objSms.setMsg(c.getString(c.getColumnIndexOrThrow("body")));
                        objSms.setReadState(c.getString(c.getColumnIndex("read")));
                        objSms.setTime(c.getString(c.getColumnIndexOrThrow("date")));
                        if (c.getString(c.getColumnIndexOrThrow("type")).contains("1")) {
                            objSms.setFolderName("inbox");
                        } else {
                            objSms.setFolderName("sent");
                        }

                        smsList.add(objSms.toJson());
                        c.moveToNext();
                    }
                } else {
                    c.close();
                    Log_OC.d(TAG, "You have no SMS");
                    return;
                }
                c.close();

                // store total
                ArbitraryDataProvider arbitraryDataProvider = new ArbitraryDataProvider(getContext().getContentResolver());
                arbitraryDataProvider.storeOrUpdateKeyValue(account,
                        SmsBackupFragment.PREFERENCE_SMS_LAST_TOTAL,
                        String.valueOf(smsList.size()));

                String filename = DateFormat.format("yyyy-MM-dd_HH-mm-ss", Calendar.getInstance()).toString() + ".data";
                Log_OC.d(TAG, "Storing: " + filename);
                File file = new File(getContext().getCacheDir(), filename);

                FileWriter fw = null;
                try {
                    fw = new FileWriter(file);
                    JSONArray jsArray = new JSONArray(smsList);
                    fw.write(jsArray.toString());
                } catch (IOException e) {
                    Log_OC.d(TAG, "Error ", e);
                } finally {
                    if (fw != null) {
                        try {
                            fw.close();
                        } catch (IOException e) {
                            Log_OC.d(TAG, "Error closing file writer ", e);
                        }
                    }
                }

                FileUploader.UploadRequester requester = new FileUploader.UploadRequester();
                requester.uploadNewFile(
                        getContext(),
                        account,
                        file.getAbsolutePath(),
                        backupFolder + filename,
                        FileUploader.LOCAL_BEHAVIOUR_MOVE,
                        null,
                        true,
                        UploadFileOperation.CREATED_BY_USER
                );
            }
        } catch (Exception e) {
            Log_OC.d(TAG, e.getMessage());
        }
    }

    private void expireFiles(Integer daysToExpire, String backupFolderString, Account account) {
        // -1 disables expiration
        if (daysToExpire > -1) {
            FileDataStorageManager storageManager = new FileDataStorageManager(account, getContext());
            OCFile backupFolder = storageManager.getFileByPath(backupFolderString);
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DAY_OF_YEAR, -daysToExpire);
            Long timestampToExpire = cal.getTimeInMillis();

            if (backupFolder != null) {
                Log_OC.d(TAG, "expire: " + daysToExpire + " " + backupFolder.getFileName());
            }

            Vector<OCFile> backups = storageManager.getFolderContent(backupFolder, false);

            for (OCFile backup : backups) {
                if (timestampToExpire > backup.getModificationTimestamp()) {
                    Log_OC.d(TAG, "delete " + backup.getRemotePath());

                    // delete backups
                    Intent service = new Intent(getContext(), OperationsService.class);
                    service.setAction(OperationsService.ACTION_REMOVE);
                    service.putExtra(OperationsService.EXTRA_ACCOUNT, account);
                    service.putExtra(OperationsService.EXTRA_REMOTE_PATH, backup.getRemotePath());
                    service.putExtra(OperationsService.EXTRA_REMOVE_ONLY_LOCAL, false);
                    operationsServiceBinder.queueNewOperation(service);
                }
            }
        }

        getContext().unbindService(operationsServiceConnection);
    }

    /**
     * Implements callback methods for service binding.
     */
    private class OperationsServiceConnection implements ServiceConnection {
        private Integer daysToExpire;
        private String backupFolder;
        private Account account;

        OperationsServiceConnection(Integer daysToExpire, String backupFolder, Account account) {
            this.daysToExpire = daysToExpire;
            this.backupFolder = backupFolder;
            this.account = account;
        }

        @Override
        public void onServiceConnected(ComponentName component, IBinder service) {
            Log_OC.d(TAG, "service connected");


            if (component.equals(new ComponentName(getContext(), OperationsService.class))) {
                operationsServiceBinder = (OperationsService.OperationsServiceBinder) service;
                expireFiles(daysToExpire, backupFolder, account);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName component) {
            Log_OC.d(TAG, "service disconnected");

            if (component.equals(new ComponentName(getContext(), OperationsService.class))) {
             operationsServiceBinder = null;
            }
        }
    }
}
