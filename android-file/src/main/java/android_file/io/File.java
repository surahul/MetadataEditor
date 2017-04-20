package android_file.io;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.annotation.Nullable;

import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.List;

import android_file.io.exceptions.SAFRequiredException;
import android_file.io.storage.StorageItem;
import android_file.io.storage.StorageItemsHelper;

/**
 * @author Rahul Verma on 01/12/16 <rv@videoder.com>
 */

/**
 * Replacement for java.io.File for supporting write operations on external SD card on android 4.4+
 * Android removed the write capability for applications on external SD card on android 4.4 and
 * later introduced Storage Access Framework to indirectly access it that too with users' consent
 * <p>
 * <p>This class wraps all the workarounds and SAF operations and provides you standard File api.</p>
 * <p>It also provides additional methods like getOutputStream and getInputStream for wiriting and
 * reading from file</p>
 */
public class File {

    public static String separator = java.io.File.separator;

    /**
     * the wrapped standard java file
     */
    private java.io.File wrappedFile;

    /**
     * initialize the File module. This has to be called before using any {@code File} operation, ideally from onCreate of Application class
     *
     * @param context application context
     */
    public static void init(Context context) {
        OperationsHelper.init(context);
    }

    /**
     * Constructs a new File using the specified directory path and file name,
     * placing a path separator between the two.
     *
     * @param dirPath the path to the directory where the file is stored.
     * @param name    the file's name.
     * @throws NullPointerException if {@code name == null}.
     */
    public File(String dirPath, String name) {
        this.wrappedFile = new java.io.File(dirPath, name);
    }

    /**
     * Constructs a new file using the specified directory and name.
     *
     * @param dir  the directory where the file is stored.
     * @param name the file's name.
     * @throws NullPointerException if {@code name} is {@code null}.
     */
    public File(File dir, String name) {
        this.wrappedFile = new java.io.File(dir.getWrappedFile(), name);
    }

    /**
     * Constructs a new file using the specified directory and name.
     *
     * @param dir  the directory where the file is stored.
     * @param name the file's name.
     * @throws NullPointerException if {@code name} is {@code null}.
     */
    public File(java.io.File dir, String name) {
        this.wrappedFile = new java.io.File(dir, name);
    }

    /**
     * Constructs a new file using the specified path.
     *
     * @param path the path to be used for the file.
     * @throws NullPointerException if {@code path} is {@code null}.
     */
    public File(String path) {
        this.wrappedFile = new java.io.File(path);
    }

    public File(java.io.File file) {
        this.wrappedFile = file;
    }

    /**
     * Creates the directory named by this file, creating missing parent
     * directories if necessary.
     *
     * @throws SAFRequiredException if SAF Permission is required
     *                              {@link IOException}
     *                              if any other error occurs
     */
    public void mkDirs() throws IOException {
        parse(OperationsHelper.getInstance().makeDirectory(wrappedFile));
    }

    public void mkDirsSilently() {
        try {
            mkDirs();
        } catch (Exception ignored) {

        }
    }

    /**
     * Creates a new, empty file on the file system according to the path
     * information stored in this file.
     *
     * @throws SAFRequiredException if SAF Permission is required
     *                              {@link IOException} if it's not possible to create the file due to any other reason.
     */
    public void createNewFile() throws IOException {
        parse(OperationsHelper.getInstance().createNewFile(wrappedFile));
    }

    /**
     * Returns the length of this file in bytes.
     * Returns 0 if the file does not exist.
     * The result for a directory is not defined.
     *
     * @return the number of bytes in this file.
     */
    public long length() {
        return wrappedFile.length();
    }

    /**
     * Returns a boolean indicating whether this file can be found on the
     * underlying file system.
     *
     * @return {@code true} if this file exists, {@code false} otherwise.
     */
    public boolean exists() {
        return wrappedFile.exists();
    }

    /**
     * Deletes this file, even if its a directory and has contents (USE WITH CAUTION)
     *
     * @throws SAFRequiredException if SAF Permission is required
     *                              {@link IOException} if it's not possible to create the file due to any other reason.
     */
    public void delete() throws IOException {
        boolean wasFileAndExisted = wrappedFile.exists() && wrappedFile.isFile();
        FileOperationResponse response = OperationsHelper.getInstance().deleteFileOrDir(wrappedFile);
        if (response.getResponseCode() == FileOperationResponse.SUCCESS && wasFileAndExisted) {
            removeFromMediaStore();
        }
        parse(response);
    }


    public void deleteSilently() {
        try {
            delete();
        } catch (Exception ignored) {
        }
    }

    /**
     * Renames this file to {@code newPath}. This operation is supported for both
     * files and directories.
     * <p>
     * This method tries its best to even copy files if both paths are not on the same mount point.
     * On Android, applications can use this to copy between internal storage and an SD card.
     *
     * @param newPath   the new path.
     * @param overwrite overwrite newPath file or directory if it already exists
     * @throws SAFRequiredException if SAF Permission is required
     *                              {@link IOException} if it's not possible to create the file due to any other reason.
     */
    public void rename(File newPath, boolean overwrite) throws IOException {
        rename(newPath, overwrite, null);
    }

    public void scanForMediaLibrary() {
        if(isFile()&&exists()){
            OperationsHelper.getInstance().runMediaScan(getAbsolutePath());
        }
    }

    public void removeFromMediaStore(){
        OperationsHelper.getInstance().removeFromMediaStore(wrappedFile.getAbsolutePath());
    }

    public interface ProgressCallback {
        void onProgress(int progress);
    }

    public void rename(File newPath, boolean overwrite, @Nullable ProgressCallback progressCallback) throws IOException {
        if (this.exists()) {
            if (this.isFile()) {
                FileOperationResponse response = OperationsHelper.getInstance().renameFile(wrappedFile, newPath.getWrappedFile(), overwrite, progressCallback);
                if (response.getResponseCode() == FileOperationResponse.SUCCESS) {
                    removeFromMediaStore();
                }
                parse(response);
                return;
            } else if (this.isDirectory()) {
                parse(OperationsHelper.getInstance().renameDirectory(wrappedFile, newPath.getWrappedFile(), overwrite));
                return;
            }
        }
        parse(new FileOperationResponse(FileOperationResponse.ERROR_IO));
    }


    public void copyTo(File copyTo, boolean overwrite) throws IOException {
        copyTo(copyTo, overwrite, null);
    }

    public void copyTo(File copyTo, boolean overwrite, @Nullable ProgressCallback progressCallback) throws IOException {
        parse(OperationsHelper.getInstance().copyFile(wrappedFile, copyTo.getWrappedFile(), overwrite, progressCallback));
    }

    public boolean copyToReturnBool(File copyTo, boolean overWrite, @Nullable ProgressCallback progressCallback) {
        try {
            this.copyTo(copyTo, overWrite, progressCallback);
            return true;
        } catch (IOException ioException) {
            ioException.printStackTrace();
            return false;
        }
    }

    public boolean copyToReturnBool(File copyTo, boolean overWrite) {
        return this.copyToReturnBool(copyTo, overWrite, null);
    }

    /**
     * Indicates if this file represents a <em>file</em> on the underlying
     * file system.
     *
     * @return {@code true} if this file is a file, {@code false} otherwise.
     */
    public boolean isFile() {
        return wrappedFile.isFile();
    }

    /**
     * Indicates if this file represents a <em>directory</em> on the
     * underlying file system.
     *
     * @return {@code true} if this file is a directory, {@code false}
     * otherwise.
     */
    public boolean isDirectory() {
        return wrappedFile.isDirectory();
    }

    /**
     * Returns a writable FileOutputStream to which anything can be written. This file can even be
     * present on external SD card. Internally it create a standard FileOutputStream if writable
     * normally or uses SAF / MediaStore hack in case a workaround is required on android 4.4 and above
     *
     * @param append if you need to append to the file if already exists
     */
    @Nullable
    public FileOutputStream getOutputStream(boolean append) {
        return OperationsHelper.getInstance().getOutputStream(wrappedFile, append);
    }

    /**
     * Returns a readable FileOutputStream from which can be read.
     */
    @Nullable
    public FileInputStream getInputStream() {
        return OperationsHelper.getInstance().getInputStream(wrappedFile);
    }


    /**
     * Returns the pathname of the parent of this file. This is the path up to
     * but not including the last name. {@code null} is returned if there is no
     * parent.
     *
     * @return this file's parent pathname or {@code null}.
     */
    public String getParent() {
        return wrappedFile.getParent();
    }

    /**
     * Returns a new file made from the pathname of the parent of this file.
     * This is the path up to but not including the last name. {@code null} is
     * returned when there is no parent.
     *
     * @return a new file representing this file's parent or {@code null}.
     */
    public File getParentFile() {
        return new File(wrappedFile.getParent());
    }

    /**
     * Returns the name of the file or directory represented by this file.
     *
     * @return this file's name or an empty string if there is no name part in
     * the file's path.
     */
    public String getName() {
        return wrappedFile.getName();
    }

    /**
     * returns the underlying wrapped java file. Any changes made to it will change
     * the wrapper File as well
     */
    public java.io.File getWrappedFile() {
        return wrappedFile;
    }

    /**
     * Returns the absolute path of this file. An absolute path is a path that starts at a root
     * of the file system. On Android, there is only one root: {@code /}.
     * <p>
     * <p>A common use for absolute paths is when passing paths to a {@code Process} as
     * command-line arguments, to remove the requirement implied by relative paths, that the
     * child must have the same working directory as its parent.
     */
    public String getAbsolutePath() {
        return wrappedFile.getAbsolutePath();
    }

    /**
     * Returns true if this File is writable normally with standards such as {@code new FileOutputStream}
     * or returns false if this File uses SAF for android 5.0+ or MediaStore Hack for android 4.4
     * for writing to it
     */

    public String getDisplayableName(String externalStorageName, String internalStorageName) {
        return OperationsHelper.getInstance().getDisplayableName(wrappedFile, externalStorageName, internalStorageName);
    }

    public boolean isWritableNormally() {
        return OperationsHelper.getInstance().isWritableNormally(wrappedFile);
    }

    public boolean isWritableNormallyOrBySAF() {
        return OperationsHelper.getInstance().isWritableNormallyOrBySAF(wrappedFile);
    }

    public File getRootStorageItemFile() {
        return new File(OperationsHelper.getInstance().getRootStorageItemFile(wrappedFile));
    }

    public String getCanonicalPath() throws IOException {
        return wrappedFile.getCanonicalPath();
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public boolean isOnExternalSDCard() {
        return OperationsHelper.getInstance().isOnExternalSdCard(wrappedFile);
    }

    public File[] listFiles() {
        return convertToArray(wrappedFile.listFiles());
    }

    public File[] listFiles(FilenameFilter filenameFilter) {
        return convertToArray(wrappedFile.listFiles(filenameFilter));
    }

    public File[] listFiles(FileFilter fileFilter) {
        return convertToArray(wrappedFile.listFiles(fileFilter));
    }

    public boolean isHidden() {
        return wrappedFile.isHidden();
    }

    public boolean canRead() {
        return wrappedFile.canRead();
    }

    public long getTotalSpace() {
        return wrappedFile.getTotalSpace();
    }

    public long getFreeSpace() {
        return OperationsHelper.getInstance().getFreeSpace(wrappedFile);
    }

    private void parse(FileOperationResponse response) throws IOException {
        switch (response.getResponseCode()) {

            case FileOperationResponse.ERROR_IO:
                throw new IOException("Error while performing File Operation");
            case FileOperationResponse.ERROR_SAF_PERMISSION_REQUIRED:
                throw new SAFRequiredException("SAF Permission is required");
            case FileOperationResponse.SUCCESS:
                break;
        }
    }


    public static File[] convertToArray(java.io.File... files) {
        if (files == null || files.length == 0)
            return new File[]{};
        else {
            File[] result = new File[files.length];
            int i = 0;
            for (java.io.File file : files) {
                result[i] = new File(file);
                i++;
            }
            return result;
        }
    }

    public static List<StorageItem> getStorageItems(String externalStorageString, String internalStorageString) {
        StorageItemsHelper storageItemsHelper = new StorageItemsHelper(OperationsHelper.getInstance().getContext());
        return storageItemsHelper.getStorageItems(internalStorageString, externalStorageString);
    }


}
