package com.americavoice.backup.utils;

import com.americavoice.backup.Const;
import com.americavoice.backup.datamodel.OCFile;

/**
 * Created by angelchanquin on 8/11/17.
 */

public class BaseConstants {
    public static final String ACCOUNT_TYPE = Const.ACCOUNT_TYPE;
    public static final String AUTHORITY = Const.AUTHORITY;
    public static final String FILE_PROVIDER_AUTHORITY = Const.FILE_PROVIDER_AUTHORITY;
    public static final String DATA_FOLDER = Const.DATA_FOLDER;

    public static final String PHOTOS_FOLDER = "Photos";
    public static final String VIDEOS_FOLDER = "Videos";
    public static final String DOCUMENTS_FOLDER = "Documents";
    public static final String CONTACTS_FOLDER = "Contacts";
    public static final String SMS_FOLDER = "Sms";
    public static final String CALLS_FOLDER = "Calls";

    public static final String PHOTOS_REMOTE_FOLDER = OCFile.PATH_SEPARATOR + PHOTOS_FOLDER + OCFile.PATH_SEPARATOR;
    public static final String VIDEOS_REMOTE_FOLDER = OCFile.PATH_SEPARATOR + VIDEOS_FOLDER + OCFile.PATH_SEPARATOR;
    public static final String DOCUMENTS_REMOTE_FOLDER = OCFile.PATH_SEPARATOR + DOCUMENTS_FOLDER + OCFile.PATH_SEPARATOR;
    public static final String CONTACTS_REMOTE_FOLDER = OCFile.PATH_SEPARATOR + CONTACTS_FOLDER + OCFile.PATH_SEPARATOR;
    public static final String SMS_REMOTE_FOLDER = OCFile.PATH_SEPARATOR + SMS_FOLDER + OCFile.PATH_SEPARATOR;
    public static final String CALLS_REMOTE_FOLDER = OCFile.PATH_SEPARATOR + CALLS_FOLDER + OCFile.PATH_SEPARATOR;

    public static final Integer CONTACTS_BACKUP_EXPLIRE = -1;
    public static final Integer CALLS_BACKUP_EXPLIRE = -1;
    public static final Integer SMS_BACKUP_EXPIRE = -1;

    public static final String EXTRA_ACCOUNT = "EXTRA_ACCOUNT";
    public static final String EXTRA_ACTION = "EXTRA_ACTION";

    public static class PreferenceKeys {
        public static final String STORAGE_PATH = "storage_path";
        public static final String STORAGE_FULL = "STORAGE_FULL";
        public static final String STORAGE_CACHE_INVALIDATED = "STORAGE_CACHE_INVALIDATED";
    }

}
