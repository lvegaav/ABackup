/**
 *   ownCloud Android client application
 *
 *   Copyright (C) 2012 Bartek Przybylski
 *   Copyright (C) 2016 ownCloud Inc.
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License version 2,
 *   as published by the Free Software Foundation.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.americavoice.backup.utils;


import com.americavoice.backup.datamodel.FileDataStorageManager;
import com.americavoice.backup.files.service.FileDownloader;
import com.americavoice.backup.files.service.FileUploader;
import com.americavoice.backup.service.OperationsService;

public interface ComponentsGetter {

    /**
     * To be invoked when the parent activity is fully created to get a reference
     * to the FileDownloader service API.
     */
    public FileDownloader.FileDownloaderBinder getFileDownloaderBinder();

    
    /**
     * To be invoked when the parent activity is fully created to get a reference
     * to the FileUploader service API.
     */
    public FileUploader.FileUploaderBinder getFileUploaderBinder();

    
    /**
     * To be invoked when the parent activity is fully created to get a reference
     * to the OperationsSerivce service API.
     */
    public OperationsService.OperationsServiceBinder getOperationsServiceBinder();

    
    public FileDataStorageManager getStorageManager();


}
