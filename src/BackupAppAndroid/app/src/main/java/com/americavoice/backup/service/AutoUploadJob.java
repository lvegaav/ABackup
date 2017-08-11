/**
 * Nextcloud Android client application
 *
 * @author Tobias Kaminsky
 * Copyright (C) 2017 Mario Danic
 * Copyright (C) 2016 Tobias Kaminsky
 * Copyright (C) 2016 Nextcloud
 * <p>
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU AFFERO GENERAL PUBLIC LICENSE
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU AFFERO GENERAL PUBLIC LICENSE for more details.
 * <p>
 * You should have received a copy of the GNU Affero General Public
 * License along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.americavoice.backup.service;

import android.accounts.Account;
import android.content.Context;
import android.support.annotation.NonNull;

import com.americavoice.backup.authentication.AccountUtils;
import com.americavoice.backup.files.service.FileUploader;
import com.americavoice.backup.operations.UploadFileOperation;
import com.americavoice.backup.utils.MimeTypeUtil;
import com.evernote.android.job.Job;
import com.evernote.android.job.util.support.PersistableBundleCompat;


import java.io.File;

public class AutoUploadJob extends Job {
    public static final String TAG = "AutoUploadJob";

    public static final String LOCAL_PATH = "filePath";
    public static final String REMOTE_PATH = "remotePath";
    public static final String ACCOUNT = "account";
    public static final String UPLOAD_BEHAVIOUR = "uploadBehaviour";

    @NonNull
    @Override
    protected Result onRunJob(Params params) {
        final Context context = getContext();
        PersistableBundleCompat bundle = params.getExtras();
        final String filePath = bundle.getString(LOCAL_PATH, "");
        final String remotePath = bundle.getString(REMOTE_PATH, "");
        final Account account = AccountUtils.getOwnCloudAccountByName(context, bundle.getString(ACCOUNT, ""));
        final Integer uploadBehaviour = bundle.getInt(UPLOAD_BEHAVIOUR, FileUploader.LOCAL_BEHAVIOUR_FORGET);


        File file = new File(filePath);


        // File can be deleted between job generation and job execution. If file does not exist, just ignore it
        if (file.exists()) {
            final String mimeType = MimeTypeUtil.getBestMimeTypeByFilename(file.getAbsolutePath());

            final FileUploader.UploadRequester requester = new FileUploader.UploadRequester();
            requester.uploadNewFile(
                    context,
                    account,
                    filePath,
                    remotePath,
                    uploadBehaviour,
                    mimeType,
                    true,           // create parent folder if not existent
                    UploadFileOperation.CREATED_AS_INSTANT_PICTURE
            );
        }


        return Result.SUCCESS;
    }
}
