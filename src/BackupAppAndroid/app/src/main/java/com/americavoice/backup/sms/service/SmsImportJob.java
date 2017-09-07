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
import android.net.Uri;
import android.provider.CallLog;
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
import java.util.Date;
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
            FileInputStream in = new FileInputStream(callFilePath);
            Scanner br = new Scanner(new InputStreamReader(in));
            while (br.hasNext()) {
                String strLine = br.nextLine();
                JSONArray jsonArr = new JSONArray(strLine);
                for (int i = 0; i < jsonArr.length(); i++) {
                    Sms sms = Sms.fromJson(jsonArr.getString(i));
                    ContentValues initialValues = new ContentValues();
                    initialValues.put("address", sms.getAddress());

                    long millis = Long.parseLong(sms.getTime()) * 1000;
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd kk:mm");
                    sdf.setTimeZone(TimeZone.getDefault());
                    String date = sdf.format(new Date(millis));
                    initialValues.put("date", date);
                    initialValues.put("body", sms.getMsg());
                    initialValues.put("read", sms.getReadState());
                    getContext().getContentResolver().insert(Uri.parse("content://sms/" + sms.getFolderName()), initialValues);
                }
            }
        } catch (Exception e) {
            Log_OC.e(TAG, e.getMessage());
        }

        return Result.SUCCESS;
    }
}
