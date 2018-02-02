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

package com.americavoice.backup.calls.service;

import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;

import com.americavoice.backup.calls.ui.model.Call;
import com.americavoice.backup.contacts.ui.ContactListFragment;
import com.evernote.android.job.Job;
import com.evernote.android.job.util.support.PersistableBundleCompat;
import com.owncloud.android.lib.common.utils.Log_OC;

import org.json.JSONArray;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import java.util.TreeMap;

import ezvcard.Ezvcard;
import ezvcard.VCard;
import third_parties.ezvcard_android.ContactOperations;

/**
 * Job to import contacts
 */

public class CallsImportJob extends Job {
    public static final String TAG = "CallsImportJob";

    public static final String CALL_FILE_PATH = "call_file_path";

    @NonNull
    @Override
    protected Result onRunJob(Params params) {
        PersistableBundleCompat bundle = params.getExtras();
        final Context context = getContext();
        String callFilePath = bundle.getString(CALL_FILE_PATH, "");
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.WRITE_CALL_LOG) != PackageManager.PERMISSION_GRANTED) {
            return Result.FAILURE;
        }

        try {
            //Current history
            List<String> currentHistory = new ArrayList<>();
            String strOrder = android.provider.CallLog.Calls.DATE + " DESC";
            Cursor managedCursor = getContext().getContentResolver().query(CallLog.Calls.CONTENT_URI, null, null, null, strOrder);

            int number = managedCursor.getColumnIndex(CallLog.Calls.NUMBER);
            int type = managedCursor.getColumnIndex(CallLog.Calls.TYPE);
            int date = managedCursor.getColumnIndex(CallLog.Calls.DATE);
            int duration = managedCursor.getColumnIndex(CallLog.Calls.DURATION);
            while (managedCursor.moveToNext()) {
                String phoneNumber = managedCursor.getString(number);
                String callType = managedCursor.getString(type);
                String callDate = managedCursor.getString(date);
                String callDuration = managedCursor.getString(duration);
                currentHistory.add(new Call(phoneNumber, callType, callDate, callDuration).ToJson());
            }

            FileInputStream in = new FileInputStream(callFilePath);
            Scanner br = new Scanner(new InputStreamReader(in));
            while (br.hasNext()) {
                String strLine = br.nextLine();
                JSONArray jsonArr = new JSONArray(strLine);
                int counter = currentHistory.size();
                for (int i = 0; i < jsonArr.length(); i++) {
                    String callString = jsonArr.getString(i);
                    //Check if call exists in current history
                    if (!currentHistory.contains(callString)) {
                        Call call = Call.FromJson(callString);
                        ContentValues values = new ContentValues();
                        values.put(CallLog.Calls.NUMBER, call.getPhoneNumber());
                        values.put(CallLog.Calls.DATE, Long.valueOf(call.getCallDate()));
                        values.put(CallLog.Calls.DURATION, Long.valueOf(call.getCallDuration()));
                        values.put(CallLog.Calls.TYPE, Integer.valueOf(call.getCallType()));
                        values.put(CallLog.Calls.NEW, 1);
                        values.put(CallLog.Calls.CACHED_NAME, "");
                        values.put(CallLog.Calls.CACHED_NUMBER_TYPE, 0);
                        values.put(CallLog.Calls.CACHED_NUMBER_LABEL, "");
                        context.getContentResolver().insert(CallLog.Calls.CONTENT_URI, values);
                        counter++;
                        if (counter >= 500) {
                            break;
                        }
                    }
                }
            }
        } catch (Exception e) {
            Log_OC.e(TAG, e.getMessage());
        }

        return Result.SUCCESS;
    }
}
