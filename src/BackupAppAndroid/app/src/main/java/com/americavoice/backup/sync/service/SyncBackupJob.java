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

import android.accounts.Account;
import android.content.Context;
import android.support.annotation.NonNull;

import com.americavoice.backup.authentication.AccountUtils;
import com.americavoice.backup.files.utils.FileUtils;
import com.evernote.android.job.Job;
import com.evernote.android.job.util.support.PersistableBundleCompat;

/**
 * Job that backup contacts to /Contacts-Backup and deletes files older than x days
 */

public class SyncBackupJob extends Job {

    public static final String TAG = "SyncBackupJob";
    public static final String ACCOUNT = "account";
    public static final String PENDING_VIDEOS = "PENDING_VIDEOS";
    public static final String PENDING_PHOTOS = "PENDING_PHOTOS";
    public static final String PENDING_MUSIC = "PENDING_MUSIC";
    public static final String FORCE = "force";

    @NonNull
    @Override
    protected Result onRunJob(Params params) {

        PersistableBundleCompat bundle = params.getExtras();

        final Context context = getContext();

        final Account account = AccountUtils.getOwnCloudAccountByName(context, bundle.getString(ACCOUNT, ""));

        final String[] pendingPhotos = bundle.getStringArray(PENDING_PHOTOS);

        final String[] pendingVideos = bundle.getStringArray(PENDING_VIDEOS);

        final String[] pendingSongs = bundle.getStringArray(PENDING_MUSIC);

        FileUtils.backupPendingFiles(getContext(), account, pendingPhotos, pendingVideos, pendingSongs);

        return Result.SUCCESS;
    }

}
