/**
 * Nextcloud Android client application
 *
 * @author Tobias Kaminsky
 * Copyright (C) 2017 Tobias Kaminsky
 * Copyright (C) 2017 Nextcloud GmbH.
 * <p>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.americavoice.backup.sms.service;

import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.CallLog;
import android.provider.Telephony;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;

import com.americavoice.backup.sms.ui.model.Sms;
import com.evernote.android.job.Job;
import com.evernote.android.job.util.support.PersistableBundleCompat;
import com.owncloud.android.lib.common.utils.Log_OC;

import org.json.JSONArray;

import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;
import java.util.TimeZone;

/**
 * Job to import sms
 */

public class SmsImportJob extends Job {
    public static final String TAG = "SmsImportJob";

    public static final String SMS_FILE_PATH = "sms_file_path";

    @NonNull
    @Override
    protected Result onRunJob(Params params) {
        PersistableBundleCompat bundle = params.getExtras();
        final Context context = getContext();
        String callFilePath = bundle.getString(SMS_FILE_PATH, "");
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED) {
            return Result.FAILURE;
        }
        try {
            //Current history
            List<String> currentHistory = new ArrayList<>();
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

                        currentHistory.add(objSms.toJson());
                        c.moveToNext();
                    }
                } else {
                    c.close();
                    Log_OC.d(TAG, "You have no SMS");
                }
                c.close();
            }

            FileInputStream in = new FileInputStream(callFilePath);
            Scanner br = new Scanner(new InputStreamReader(in));
            while (br.hasNext()) {
                String strLine = br.nextLine();
                JSONArray jsonArr = new JSONArray(strLine);
                for (int i = 0; i < jsonArr.length(); i++) {
                    String smsString = jsonArr.getString(i);
                    //Check if call exists in current history
                    if (!currentHistory.contains(smsString)) {
                        Sms sms = Sms.fromJson(smsString);
                        saveSms(sms);
                    }
                }
            }
        } catch (Exception e) {
            Log_OC.e(TAG, e.getMessage());
        }

        return Result.SUCCESS;
    }

    public boolean saveSms(Sms sms) {
        boolean ret = false;
        try {
            long millis = Long.parseLong(sms.getTime()) * 1000;
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd kk:mm");
            sdf.setTimeZone(TimeZone.getDefault());
            String date = sdf.format(new Date(millis));
            String folderName = sms.getFolderName();

            ContentValues initialValues = new ContentValues();
            initialValues.put("address", sms.getAddress());
            initialValues.put("body", sms.getMsg());
            initialValues.put("read", sms.getReadState());
            initialValues.put("date", date);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                Uri uri = Telephony.Sms.Sent.CONTENT_URI;
                if(folderName.equals("inbox")){
                    uri = Telephony.Sms.Inbox.CONTENT_URI;
                }
                getContext().getContentResolver().insert(uri, initialValues);
            }
            else {
                /* folderName  could be inbox or sent */
                getContext().getContentResolver().insert(Uri.parse("content://sms/" + folderName), initialValues);
            }

            ret = true;
        } catch (Exception ex) {
            ex.printStackTrace();
            ret = false;
        }
        return ret;
    }
}
