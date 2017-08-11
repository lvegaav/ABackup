/**
 * ownCloud Android client application
 * <p>
 * Copyright (C) 2016 ownCloud Inc.
 * <p>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 2,
 * as published by the Free Software Foundation.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.americavoice.backup.utils;

import android.net.Uri;
import android.webkit.MimeTypeMap;


import com.americavoice.backup.datamodel.OCFile;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>Helper class for detecting the right icon for a file or folder,
 * based on its mime type and file extension.</p>
 *
 * This class maintains all the necessary mappings fot these detections.<br/>
 * In order to add further mappings, there are up to three look up maps that need further values:
 * <ol>
 *     <li>
 *         {@link MimeTypeUtil#FILE_EXTENSION_TO_MIMETYPE_MAPPING}<br/>
 *         to add a new file extension to mime type mapping
 *     </li>
 * </ol>
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public class MimeTypeUtil {

    /** Mapping: mime type for file extension. */
    private static final Map<String, List<String>> FILE_EXTENSION_TO_MIMETYPE_MAPPING = new HashMap<>();

    static {
        populateFileExtensionMimeTypeMapping();
    }

    /**
     * Returns a single MIME type of all the possible, by inspection of the file extension, and taking
     * into account the MIME types known by ownCloud first.
     *
     * @param filename      Name of file
     * @return A single MIME type, "application/octet-stream" for unknown file extensions.
     */
    public static String getBestMimeTypeByFilename(String filename) {
        List<String> candidates = determineMimeTypesByFilename(filename);
        if (candidates == null || candidates.size() < 1) {
            return "application/octet-stream";
        }
        return candidates.get(0);
    }

    /**
     * @return 'True' if the mime type defines image
     */
    public static boolean isImage(String mimeType) {
        return (mimeType != null && mimeType.toLowerCase().startsWith("image/") && !mimeType.toLowerCase().contains
                ("djvu"));
    }

    /**
     * @return 'True' the mime type defines video
     */
    public static boolean isVideo(String mimeType) {
        return (mimeType != null && mimeType.toLowerCase().startsWith("video/"));
    }

    /**
     * @return 'True' the mime type defines audio
     */
    public static boolean isAudio(String mimeType) {
        return (mimeType != null && mimeType.toLowerCase().startsWith("audio/"));
    }

    /**
     * @return 'True' if mime type defines text
     */
    public static boolean isText(String mimeType) {
        return (mimeType != null && mimeType.toLowerCase().startsWith("text/"));
    }

    /**
     * @return 'True' if mime type defines vcard
     */
    public static boolean isVCard(String mimeType) {
        return "text/vcard".equalsIgnoreCase(mimeType);
    }

    /**
     * Checks if file passed is a video.
     *
     * @param file the file to be checked
     * @return 'True' the mime type defines video
     */
    public static boolean isVideo(File file) {
        return isVideo(extractMimeType(file));
    }

    /**
     * Checks if file passed is an image.
     *
     * @param file the file to be checked
     * @return 'True' the mime type defines video
     */
    public static boolean isImage(File file) {
        return isImage(extractMimeType(file));
    }

    public static boolean isSVG(OCFile file) {
        return "image/svg+xml".equalsIgnoreCase(file.getMimetype());
    }

    /**
     * @param file the file to be analyzed
     * @return 'True' if the file contains audio
     */
    public static boolean isAudio(OCFile file) {
        return MimeTypeUtil.isAudio(file.getMimetype());
    }

    /**
     * @param file the file to be analyzed
     * @return 'True' if the file contains video
     */
    public static boolean isVideo(OCFile file) {
        return MimeTypeUtil.isVideo(file.getMimetype());
    }

    /**
     * @param file the file to be analyzed
     * @return 'True' if the file contains an image
     */
    public static boolean isImage(OCFile file) {
        return (MimeTypeUtil.isImage(file.getMimetype())
                || MimeTypeUtil.isImage(getMimeTypeFromPath(file.getRemotePath())));
    }

    /**
     * @param file the file to be analyzed
     * @return 'True' if the file is simple text (e.g. not application-dependent, like .doc or .docx)
     */
    public static boolean isText(OCFile file) {
        return (MimeTypeUtil.isText(file.getMimetype())
                || MimeTypeUtil.isText(getMimeTypeFromPath(file.getRemotePath())));
    }


    /**
     * @param file the file to be analyzed
     * @return 'True' if the file is a vcard
     */
    public static boolean isVCard(OCFile file) {
        return isVCard(file.getMimetype()) || isVCard(getMimeTypeFromPath(file.getRemotePath()));
    }

    /**
     * Extracts the mime type for the given file.
     *
     * @param file the file to be analyzed
     * @return the file's mime type
     */
    private static String extractMimeType(File file) {
        Uri selectedUri = Uri.fromFile(file);
        String fileExtension = MimeTypeMap.getFileExtensionFromUrl(selectedUri.toString().toLowerCase());
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExtension);
    }

    /**
     * determines the list of possible mime types for the given file, based on its extension.
     *
     * @param filename the file name
     * @return list of possible mime types (ordered), empty list in case no mime types found
     */
    private static List<String> determineMimeTypesByFilename(String filename) {
        String fileExtension = getExtension(filename);

        // try detecting the mimetype based on the web app logic equivalent
        List<String> mimeTypeList = FILE_EXTENSION_TO_MIMETYPE_MAPPING.get(fileExtension);
        if (mimeTypeList != null && mimeTypeList.size() > 0) {
            return mimeTypeList;
        } else {
            // try detecting the mime type via android itself
            String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExtension);
            if (mimeType != null) {
                return Collections.singletonList(mimeType);
            } else {
                return new ArrayList<>();
            }
        }
    }

    public static String getMimeTypeFromPath(String path) {
        String extension = "";
        int pos = path.lastIndexOf('.');
        if (pos >= 0) {
            extension = path.substring(pos + 1);
        }
        String result = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension.toLowerCase());
        return (result != null) ? result : "";
    }

    /**
     * provides the file extension of a given filename.
     *
     * @param filename the filename
     * @return the file extension
     */
    private static String getExtension(String filename) {
        return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
    }



    /**
     * populates the mapping list: file extension --> mime type.
     */
    private static void populateFileExtensionMimeTypeMapping() {
        FILE_EXTENSION_TO_MIMETYPE_MAPPING.put("3gp", Collections.singletonList("video/3gpp"));
        FILE_EXTENSION_TO_MIMETYPE_MAPPING.put("7z", Collections.singletonList("application/x-7z-compressed"));
        FILE_EXTENSION_TO_MIMETYPE_MAPPING.put("accdb", Collections.singletonList("application/msaccess"));
        FILE_EXTENSION_TO_MIMETYPE_MAPPING.put("ai", Collections.singletonList("application/illustrator"));
        FILE_EXTENSION_TO_MIMETYPE_MAPPING.put("apk", Collections.singletonList("application/vnd.android.package-archive"));
        FILE_EXTENSION_TO_MIMETYPE_MAPPING.put("arw", Collections.singletonList("image/x-dcraw"));
        FILE_EXTENSION_TO_MIMETYPE_MAPPING.put("avi", Collections.singletonList("video/x-msvideo"));
        FILE_EXTENSION_TO_MIMETYPE_MAPPING.put("bash", Collections.singletonList("text/x-shellscript"));
        FILE_EXTENSION_TO_MIMETYPE_MAPPING.put("blend", Collections.singletonList("application/x-blender"));
        FILE_EXTENSION_TO_MIMETYPE_MAPPING.put("bin", Collections.singletonList("application/x-bin"));
        FILE_EXTENSION_TO_MIMETYPE_MAPPING.put("bmp", Collections.singletonList("image/bmp"));
        FILE_EXTENSION_TO_MIMETYPE_MAPPING.put("bpg", Collections.singletonList("image/bpg"));
        FILE_EXTENSION_TO_MIMETYPE_MAPPING.put("cb7", Collections.singletonList("application/x-cbr"));
        FILE_EXTENSION_TO_MIMETYPE_MAPPING.put("cba", Collections.singletonList("application/x-cbr"));
        FILE_EXTENSION_TO_MIMETYPE_MAPPING.put("cbr", Collections.singletonList("application/x-cbr"));
        FILE_EXTENSION_TO_MIMETYPE_MAPPING.put("cbt", Collections.singletonList("application/x-cbr"));
        FILE_EXTENSION_TO_MIMETYPE_MAPPING.put("cbtc", Collections.singletonList("application/x-cbr"));
        FILE_EXTENSION_TO_MIMETYPE_MAPPING.put("cbz", Collections.singletonList("application/x-cbr"));
        FILE_EXTENSION_TO_MIMETYPE_MAPPING.put("cc", Collections.singletonList("text/x-c"));
        FILE_EXTENSION_TO_MIMETYPE_MAPPING.put("cdr", Collections.singletonList("application/coreldraw"));
        FILE_EXTENSION_TO_MIMETYPE_MAPPING.put("cnf", Collections.singletonList("text/plain"));
        FILE_EXTENSION_TO_MIMETYPE_MAPPING.put("conf", Collections.singletonList("text/plain"));
        FILE_EXTENSION_TO_MIMETYPE_MAPPING.put("cpp", Collections.singletonList("text/x-c++src"));
        FILE_EXTENSION_TO_MIMETYPE_MAPPING.put("cr2", Collections.singletonList("image/x-dcraw"));
        FILE_EXTENSION_TO_MIMETYPE_MAPPING.put("css", Collections.singletonList("text/css"));
        FILE_EXTENSION_TO_MIMETYPE_MAPPING.put("csv", Collections.singletonList("text/csv"));
        FILE_EXTENSION_TO_MIMETYPE_MAPPING.put("cvbdl", Collections.singletonList("application/x-cbr"));
        FILE_EXTENSION_TO_MIMETYPE_MAPPING.put("c", Collections.singletonList("text/x-c"));
        FILE_EXTENSION_TO_MIMETYPE_MAPPING.put("c++", Collections.singletonList("text/x-c++src"));
        FILE_EXTENSION_TO_MIMETYPE_MAPPING.put("dcr", Collections.singletonList("image/x-dcraw"));
        FILE_EXTENSION_TO_MIMETYPE_MAPPING.put("deb", Collections.singletonList("application/x-deb"));
        FILE_EXTENSION_TO_MIMETYPE_MAPPING.put("dng", Collections.singletonList("image/x-dcraw"));
        FILE_EXTENSION_TO_MIMETYPE_MAPPING.put("doc", Collections.singletonList("application/msword"));
        FILE_EXTENSION_TO_MIMETYPE_MAPPING.put("docm", Collections.singletonList("application/vnd.ms-word.document.macroEnabled.12"));
        FILE_EXTENSION_TO_MIMETYPE_MAPPING.put("docx", Collections.singletonList("application/vnd.openxmlformats-officedocument.wordprocessingml.document"));
        FILE_EXTENSION_TO_MIMETYPE_MAPPING.put("dot", Collections.singletonList("application/msword"));
        FILE_EXTENSION_TO_MIMETYPE_MAPPING.put("dotx", Collections.singletonList("application/vnd.openxmlformats-officedocument.wordprocessingml.template"));
        FILE_EXTENSION_TO_MIMETYPE_MAPPING.put("dv", Collections.singletonList("video/dv"));
        FILE_EXTENSION_TO_MIMETYPE_MAPPING.put("eot", Collections.singletonList("application/vnd.ms-fontobject"));
        FILE_EXTENSION_TO_MIMETYPE_MAPPING.put("epub", Collections.singletonList("application/epub+zip"));
        FILE_EXTENSION_TO_MIMETYPE_MAPPING.put("eps", Collections.singletonList("application/postscript"));
        FILE_EXTENSION_TO_MIMETYPE_MAPPING.put("erf", Collections.singletonList("image/x-dcraw"));
        FILE_EXTENSION_TO_MIMETYPE_MAPPING.put("exe", Collections.singletonList("application/x-ms-dos-executable"));
        FILE_EXTENSION_TO_MIMETYPE_MAPPING.put("flac", Collections.singletonList("audio/flac"));
        FILE_EXTENSION_TO_MIMETYPE_MAPPING.put("flv", Collections.singletonList("video/x-flv"));
        FILE_EXTENSION_TO_MIMETYPE_MAPPING.put("gif", Collections.singletonList("image/gif"));
        FILE_EXTENSION_TO_MIMETYPE_MAPPING.put("gz", Collections.singletonList("application/x-gzip"));
        FILE_EXTENSION_TO_MIMETYPE_MAPPING.put("gzip", Collections.singletonList("application/x-gzip"));
        FILE_EXTENSION_TO_MIMETYPE_MAPPING.put("h", Collections.singletonList("text/x-h"));
        FILE_EXTENSION_TO_MIMETYPE_MAPPING.put("hh", Collections.singletonList("text/x-h"));
        FILE_EXTENSION_TO_MIMETYPE_MAPPING.put("html", Arrays.asList("text/html", "text/plain"));
        FILE_EXTENSION_TO_MIMETYPE_MAPPING.put("htm", Arrays.asList("text/html", "text/plain"));
        FILE_EXTENSION_TO_MIMETYPE_MAPPING.put("ical", Collections.singletonList("text/calendar"));
        FILE_EXTENSION_TO_MIMETYPE_MAPPING.put("ics", Collections.singletonList("text/calendar"));
        FILE_EXTENSION_TO_MIMETYPE_MAPPING.put("iiq", Collections.singletonList("image/x-dcraw"));
        FILE_EXTENSION_TO_MIMETYPE_MAPPING.put("impress", Collections.singletonList("text/impress"));
        FILE_EXTENSION_TO_MIMETYPE_MAPPING.put("jpeg", Collections.singletonList("image/jpeg"));
        FILE_EXTENSION_TO_MIMETYPE_MAPPING.put("jpg", Collections.singletonList("image/jpeg"));
        FILE_EXTENSION_TO_MIMETYPE_MAPPING.put("jps", Collections.singletonList("image/jpeg"));
        FILE_EXTENSION_TO_MIMETYPE_MAPPING.put("js", Arrays.asList("application/javascript", "text/plain"));
        FILE_EXTENSION_TO_MIMETYPE_MAPPING.put("json", Arrays.asList("application/json", "text/plain"));
        FILE_EXTENSION_TO_MIMETYPE_MAPPING.put("k25", Collections.singletonList("image/x-dcraw"));
        FILE_EXTENSION_TO_MIMETYPE_MAPPING.put("kdc", Collections.singletonList("image/x-dcraw"));
        FILE_EXTENSION_TO_MIMETYPE_MAPPING.put("key", Collections.singletonList("application/x-iwork-keynote-sffkey"));
        FILE_EXTENSION_TO_MIMETYPE_MAPPING.put("keynote", Collections.singletonList("application/x-iwork-keynote-sffkey"));
        FILE_EXTENSION_TO_MIMETYPE_MAPPING.put("kra", Collections.singletonList("application/x-krita"));
        FILE_EXTENSION_TO_MIMETYPE_MAPPING.put("m2t", Collections.singletonList("video/mp2t"));
        FILE_EXTENSION_TO_MIMETYPE_MAPPING.put("m4v", Collections.singletonList("video/mp4"));
        FILE_EXTENSION_TO_MIMETYPE_MAPPING.put("markdown", Collections.singletonList("text/markdown"));
        FILE_EXTENSION_TO_MIMETYPE_MAPPING.put("mdown", Collections.singletonList("text/markdown"));
        FILE_EXTENSION_TO_MIMETYPE_MAPPING.put("md", Collections.singletonList("text/markdown"));
        FILE_EXTENSION_TO_MIMETYPE_MAPPING.put("mdb", Collections.singletonList("application/msaccess"));
        FILE_EXTENSION_TO_MIMETYPE_MAPPING.put("mdwn", Collections.singletonList("text/markdown"));
        FILE_EXTENSION_TO_MIMETYPE_MAPPING.put("mkd", Collections.singletonList("text/markdown"));
        FILE_EXTENSION_TO_MIMETYPE_MAPPING.put("mef", Collections.singletonList("image/x-dcraw"));
        FILE_EXTENSION_TO_MIMETYPE_MAPPING.put("mkv", Collections.singletonList("video/x-matroska"));
        FILE_EXTENSION_TO_MIMETYPE_MAPPING.put("mobi", Collections.singletonList("application/x-mobipocket-ebook"));
        FILE_EXTENSION_TO_MIMETYPE_MAPPING.put("mov", Collections.singletonList("video/quicktime"));
        FILE_EXTENSION_TO_MIMETYPE_MAPPING.put("mp3", Collections.singletonList("audio/mpeg"));
        FILE_EXTENSION_TO_MIMETYPE_MAPPING.put("mp4", Collections.singletonList("video/mp4"));
        FILE_EXTENSION_TO_MIMETYPE_MAPPING.put("mpeg", Collections.singletonList("video/mpeg"));
        FILE_EXTENSION_TO_MIMETYPE_MAPPING.put("mpg", Collections.singletonList("video/mpeg"));
        FILE_EXTENSION_TO_MIMETYPE_MAPPING.put("mpo", Collections.singletonList("image/jpeg"));
        FILE_EXTENSION_TO_MIMETYPE_MAPPING.put("msi", Collections.singletonList("application/x-msi"));
        FILE_EXTENSION_TO_MIMETYPE_MAPPING.put("mts", Collections.singletonList("video/MP2T"));
        FILE_EXTENSION_TO_MIMETYPE_MAPPING.put("mt2s", Collections.singletonList("video/MP2T"));
        FILE_EXTENSION_TO_MIMETYPE_MAPPING.put("nef", Collections.singletonList("image/x-dcraw"));
        FILE_EXTENSION_TO_MIMETYPE_MAPPING.put("numbers", Collections.singletonList("application/x-iwork-numbers-sffnumbers"));
        FILE_EXTENSION_TO_MIMETYPE_MAPPING.put("odf", Collections.singletonList("application/vnd.oasis.opendocument.formula"));
        FILE_EXTENSION_TO_MIMETYPE_MAPPING.put("odg", Collections.singletonList("application/vnd.oasis.opendocument.graphics"));
        FILE_EXTENSION_TO_MIMETYPE_MAPPING.put("odp", Collections.singletonList("application/vnd.oasis.opendocument.presentation"));
        FILE_EXTENSION_TO_MIMETYPE_MAPPING.put("ods", Collections.singletonList("application/vnd.oasis.opendocument.spreadsheet"));
        FILE_EXTENSION_TO_MIMETYPE_MAPPING.put("odt", Collections.singletonList("application/vnd.oasis.opendocument.text"));
        FILE_EXTENSION_TO_MIMETYPE_MAPPING.put("oga", Collections.singletonList("audio/ogg"));
        FILE_EXTENSION_TO_MIMETYPE_MAPPING.put("ogg", Collections.singletonList("audio/ogg"));
        FILE_EXTENSION_TO_MIMETYPE_MAPPING.put("ogv", Collections.singletonList("video/ogg"));
        FILE_EXTENSION_TO_MIMETYPE_MAPPING.put("opus", Collections.singletonList("audio/ogg"));
        FILE_EXTENSION_TO_MIMETYPE_MAPPING.put("orf", Collections.singletonList("image/x-dcraw"));
        FILE_EXTENSION_TO_MIMETYPE_MAPPING.put("otf", Collections.singletonList("application/font-sfnt"));
        FILE_EXTENSION_TO_MIMETYPE_MAPPING.put("pages", Collections.singletonList("application/x-iwork-pages-sffpages"));
        FILE_EXTENSION_TO_MIMETYPE_MAPPING.put("pdf", Collections.singletonList("application/pdf"));
        FILE_EXTENSION_TO_MIMETYPE_MAPPING.put("pfb", Collections.singletonList("application/x-font"));
        FILE_EXTENSION_TO_MIMETYPE_MAPPING.put("pef", Collections.singletonList("image/x-dcraw"));
        FILE_EXTENSION_TO_MIMETYPE_MAPPING.put("php", Collections.singletonList("application/x-php"));
        FILE_EXTENSION_TO_MIMETYPE_MAPPING.put("pl", Collections.singletonList("application/x-perl"));
        FILE_EXTENSION_TO_MIMETYPE_MAPPING.put("png", Collections.singletonList("image/png"));
        FILE_EXTENSION_TO_MIMETYPE_MAPPING.put("pot", Collections.singletonList("application/vnd.ms-powerpoint"));
        FILE_EXTENSION_TO_MIMETYPE_MAPPING.put("potm", Collections.singletonList("application/vnd.ms-powerpoint.template.macroEnabled.12"));
        FILE_EXTENSION_TO_MIMETYPE_MAPPING.put("potx", Collections.singletonList("application/vnd.openxmlformats-officedocument.presentationml.template"));
        FILE_EXTENSION_TO_MIMETYPE_MAPPING.put("ppa", Collections.singletonList("application/vnd.ms-powerpoint"));
        FILE_EXTENSION_TO_MIMETYPE_MAPPING.put("ppam", Collections.singletonList("application/vnd.ms-powerpoint.addin.macroEnabled.12"));
        FILE_EXTENSION_TO_MIMETYPE_MAPPING.put("pps", Collections.singletonList("application/vnd.ms-powerpoint"));
        FILE_EXTENSION_TO_MIMETYPE_MAPPING.put("ppsm", Collections.singletonList("application/vnd.ms-powerpoint.slideshow.macroEnabled.12"));
        FILE_EXTENSION_TO_MIMETYPE_MAPPING.put("ppsx", Collections.singletonList("application/vnd.openxmlformats-officedocument.presentationml.slideshow"));
        FILE_EXTENSION_TO_MIMETYPE_MAPPING.put("ppt", Collections.singletonList("application/vnd.ms-powerpoint"));
        FILE_EXTENSION_TO_MIMETYPE_MAPPING.put("pptm", Collections.singletonList("application/vnd.ms-powerpoint.presentation.macroEnabled.12"));
        FILE_EXTENSION_TO_MIMETYPE_MAPPING.put("pptx", Collections.singletonList("application/vnd.openxmlformats-officedocument.presentationml.presentation"));
        FILE_EXTENSION_TO_MIMETYPE_MAPPING.put("ps", Collections.singletonList("application/postscript"));
        FILE_EXTENSION_TO_MIMETYPE_MAPPING.put("psd", Collections.singletonList("application/x-photoshop"));
        FILE_EXTENSION_TO_MIMETYPE_MAPPING.put("py", Collections.singletonList("text/x-python"));
        FILE_EXTENSION_TO_MIMETYPE_MAPPING.put("raf", Collections.singletonList("image/x-dcraw"));
        FILE_EXTENSION_TO_MIMETYPE_MAPPING.put("rar", Collections.singletonList("application/x-rar-compressed"));
        FILE_EXTENSION_TO_MIMETYPE_MAPPING.put("reveal", Collections.singletonList("text/reveal"));
        FILE_EXTENSION_TO_MIMETYPE_MAPPING.put("rtf", Collections.singletonList("application/rtf"));
        FILE_EXTENSION_TO_MIMETYPE_MAPPING.put("rw2", Collections.singletonList("image/x-dcraw"));
        FILE_EXTENSION_TO_MIMETYPE_MAPPING.put("sgf", Collections.singletonList("application/sgf"));
        FILE_EXTENSION_TO_MIMETYPE_MAPPING.put("sh-lib", Collections.singletonList("text/x-shellscript"));
        FILE_EXTENSION_TO_MIMETYPE_MAPPING.put("sh", Collections.singletonList("text/x-shellscript"));
        FILE_EXTENSION_TO_MIMETYPE_MAPPING.put("srf", Collections.singletonList("image/x-dcraw"));
        FILE_EXTENSION_TO_MIMETYPE_MAPPING.put("sr2", Collections.singletonList("image/x-dcraw"));
        FILE_EXTENSION_TO_MIMETYPE_MAPPING.put("svg", Arrays.asList("image/svg+xml", "text/plain"));
        FILE_EXTENSION_TO_MIMETYPE_MAPPING.put("swf", Arrays.asList("application/x-shockwave-flash", "application/octet-stream"));
        FILE_EXTENSION_TO_MIMETYPE_MAPPING.put("tar", Collections.singletonList("application/x-tar"));
        FILE_EXTENSION_TO_MIMETYPE_MAPPING.put("gz", Collections.singletonList("application/x-compressed"));
        FILE_EXTENSION_TO_MIMETYPE_MAPPING.put("tex", Collections.singletonList("application/x-tex"));
        FILE_EXTENSION_TO_MIMETYPE_MAPPING.put("tgz", Collections.singletonList("application/x-compressed"));
        FILE_EXTENSION_TO_MIMETYPE_MAPPING.put("tiff", Collections.singletonList("image/tiff"));
        FILE_EXTENSION_TO_MIMETYPE_MAPPING.put("tif", Collections.singletonList("image/tiff"));
        FILE_EXTENSION_TO_MIMETYPE_MAPPING.put("ttf", Collections.singletonList("application/font-sfnt"));
        FILE_EXTENSION_TO_MIMETYPE_MAPPING.put("txt", Collections.singletonList("text/plain"));
        FILE_EXTENSION_TO_MIMETYPE_MAPPING.put("vcard", Collections.singletonList("text/vcard"));
        FILE_EXTENSION_TO_MIMETYPE_MAPPING.put("vcf", Collections.singletonList("text/vcard"));
        FILE_EXTENSION_TO_MIMETYPE_MAPPING.put("vob", Collections.singletonList("video/dvd"));
        FILE_EXTENSION_TO_MIMETYPE_MAPPING.put("wav", Collections.singletonList("audio/wav"));
        FILE_EXTENSION_TO_MIMETYPE_MAPPING.put("webm", Collections.singletonList("video/webm"));
        FILE_EXTENSION_TO_MIMETYPE_MAPPING.put("woff", Collections.singletonList("application/font-woff"));
        FILE_EXTENSION_TO_MIMETYPE_MAPPING.put("wmv", Collections.singletonList("video/x-ms-wmv"));
        FILE_EXTENSION_TO_MIMETYPE_MAPPING.put("xcf", Collections.singletonList("application/x-gimp"));
        FILE_EXTENSION_TO_MIMETYPE_MAPPING.put("xla", Collections.singletonList("application/vnd.ms-excel"));
        FILE_EXTENSION_TO_MIMETYPE_MAPPING.put("xlam", Collections.singletonList("application/vnd.ms-excel.addin.macroEnabled.12"));
        FILE_EXTENSION_TO_MIMETYPE_MAPPING.put("xls", Collections.singletonList("application/vnd.ms-excel"));
        FILE_EXTENSION_TO_MIMETYPE_MAPPING.put("xlsb", Collections.singletonList("application/vnd.ms-excel.sheet.binary.macroEnabled.12"));
        FILE_EXTENSION_TO_MIMETYPE_MAPPING.put("xlsm", Collections.singletonList("application/vnd.ms-excel.sheet.macroEnabled.12"));
        FILE_EXTENSION_TO_MIMETYPE_MAPPING.put("xlsx", Collections.singletonList("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        FILE_EXTENSION_TO_MIMETYPE_MAPPING.put("xlt", Collections.singletonList("application/vnd.ms-excel"));
        FILE_EXTENSION_TO_MIMETYPE_MAPPING.put("xltm", Collections.singletonList("application/vnd.ms-excel.template.macroEnabled.12"));
        FILE_EXTENSION_TO_MIMETYPE_MAPPING.put("xltx", Collections.singletonList("application/vnd.openxmlformats-officedocument.spreadsheetml.template"));
        FILE_EXTENSION_TO_MIMETYPE_MAPPING.put("xml", Arrays.asList("application/xml", "text/plain"));
        FILE_EXTENSION_TO_MIMETYPE_MAPPING.put("xrf", Collections.singletonList("image/x-dcraw"));
        FILE_EXTENSION_TO_MIMETYPE_MAPPING.put("yaml", Arrays.asList("application/yaml", "text/plain"));
        FILE_EXTENSION_TO_MIMETYPE_MAPPING.put("yml", Arrays.asList("application/yaml", "text/plain"));
        FILE_EXTENSION_TO_MIMETYPE_MAPPING.put("zip", Collections.singletonList("application/zip"));
    }
}
