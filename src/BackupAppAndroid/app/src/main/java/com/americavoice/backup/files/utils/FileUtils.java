package com.americavoice.backup.files.utils;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;

import com.americavoice.backup.datamodel.OCFile;
import com.americavoice.backup.utils.BaseConstants;

import java.util.ArrayList;

/**
 * Created by angelchanquin on 9/27/17.
 */

public class FileUtils {

    public static final String EXTERNAL_STORAGE_PATH = Environment.getExternalStorageDirectory().toString();

    public static final String EXTERNAL_FILES_PATH = EXTERNAL_STORAGE_PATH + OCFile.PATH_SEPARATOR + BaseConstants.DATA_FOLDER;

    public static ArrayList<String> getListOfCameraImages(Context context) {
        Uri uri;
        Cursor cursor;
        ArrayList<String> listOfAllImages = new ArrayList<>();
        uri = android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

        final String[] projection = { MediaStore.Images.ImageColumns.DATA };
        final String selection = null;
        final String[] selectionArgs = null;
        final String orderBy = MediaStore.Images.Media.DATE_TAKEN;

        cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, orderBy + " DESC");
        if (cursor != null) {
            int column_index_data = cursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.DATA);
            while (cursor.moveToNext()) {
                // Getting the absolute path of the image.
                listOfAllImages.add(cursor.getString(column_index_data));
            }
            cursor.close();
        }

        return listOfAllImages;
    }

    public static ArrayList<String> getListOfCameraVideos(Context context) {
        Uri uri;
        Cursor cursor;
        ArrayList<String> listOfAllVideos = new ArrayList<>();
        uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;

        final String[] projection = { MediaStore.Video.VideoColumns.DATA };
        final String selection = null;
        final String[] selectionArgs = null;

        final String orderBy = MediaStore.Video.VideoColumns.DATE_TAKEN;
        cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, orderBy + " DESC");

        if (cursor != null) {
            int column_index_data = cursor.getColumnIndexOrThrow(MediaStore.Video.VideoColumns.DATA);
            while (cursor.moveToNext()) {
                // Getting the absolute path of the video.
                listOfAllVideos.add(cursor.getString(column_index_data));
            }

            cursor.close();
        }

        return listOfAllVideos;
    }

    public static String getFileName(String path) {
        return path.substring(path.lastIndexOf('/') + 1);
    }
}
