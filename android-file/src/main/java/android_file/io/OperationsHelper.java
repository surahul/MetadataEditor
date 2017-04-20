package android_file.io;

import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.StatFs;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.provider.DocumentFile;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import android_file.io.storage.StorageItem;

import static android_file.io.File.getStorageItems;

/**
 * @author Rahul Verma on 01/12/16 <rv@videoder.com>
 */

final class OperationsHelper {

    private static OperationsHelper instance;

    static void init(Context context) {
        if (instance == null) {
            instance = new OperationsHelper(context);
        }
    }

    static OperationsHelper getInstance() {
        if (instance == null)
            throw new IllegalStateException("OperationHelper must be init before performing any operation");
        return instance;
    }

    private Context context;
    private MediaStoreHack mediaStoreHack;

    private OperationsHelper(Context context) {
        this.context = context;
        this.mediaStoreHack = new MediaStoreHack();
    }

    FileOperationResponse makeDirectory(File dir) {
        if (dir.exists())
            return new FileOperationResponse(FileOperationResponse.SUCCESS);

        if (dir.mkdirs())
            return new FileOperationResponse(FileOperationResponse.SUCCESS);


        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            //could not make directory and no work around exists
            return new FileOperationResponse(FileOperationResponse.ERROR_IO);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            if (internalMakeDirectory(dir))
                return new FileOperationResponse(FileOperationResponse.SUCCESS);
            else
                return new FileOperationResponse(FileOperationResponse.ERROR_IO);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (!isDirectoryWritableViaNormalOrSaf(dir.getParentFile())) {
                return new FileOperationResponse(FileOperationResponse.ERROR_SAF_PERMISSION_REQUIRED);
            } else {
                if (internalMakeDirectory(dir))
                    return new FileOperationResponse(FileOperationResponse.SUCCESS);
                else
                    return new FileOperationResponse(FileOperationResponse.ERROR_IO);
            }
        }
        return new FileOperationResponse(FileOperationResponse.ERROR_IO);
    }

    FileOperationResponse createNewFile(File file) {
        if (file.exists()) {
            return file.isDirectory() ? new FileOperationResponse(FileOperationResponse.ERROR_IO) : new FileOperationResponse(FileOperationResponse.SUCCESS);
        }
        try {
            if (file.createNewFile()) {
                return new FileOperationResponse(FileOperationResponse.SUCCESS);
            }
        } catch (IOException ignored) {

        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            //could not create file and no work around exists
            return new FileOperationResponse(FileOperationResponse.ERROR_IO);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            if (internalCreateFile(file))
                return new FileOperationResponse(FileOperationResponse.SUCCESS);
            else
                return new FileOperationResponse(FileOperationResponse.ERROR_IO);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (!isDirectoryWritableViaNormalOrSaf(file.getParentFile())) {
                return new FileOperationResponse(FileOperationResponse.ERROR_SAF_PERMISSION_REQUIRED);
            } else {
                if (internalCreateFile(file))
                    return new FileOperationResponse(FileOperationResponse.SUCCESS);
                else
                    return new FileOperationResponse(FileOperationResponse.ERROR_IO);
            }
        }
        return new FileOperationResponse(FileOperationResponse.ERROR_IO);
    }


    FileOperationResponse deleteFileOrDir(File file) {
        if (deleteFileOrDirWithAllWorkAround(file))
            return new FileOperationResponse(FileOperationResponse.SUCCESS);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return new FileOperationResponse(FileOperationResponse.ERROR_SAF_PERMISSION_REQUIRED);
        }
        return new FileOperationResponse(FileOperationResponse.ERROR_IO);
    }

    public void removeFromMediaStore(String... paths) {
        try {
            final String[] FIELDS = {MediaStore.MediaColumns._ID, MediaStore.MediaColumns.DATA, MediaStore.MediaColumns.TITLE};
            if (paths == null || paths.length == 0) return;
            String select = "";
            for (String path : paths) {
                if (!select.equals("")) select += " OR ";
                select += MediaStore.MediaColumns.DATA + "=?";
            }
            Uri uri;
            Cursor ca;
            uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
            ca = context.getContentResolver().query(uri, FIELDS, select, paths, null);
            for (ca.moveToFirst(); !ca.isAfterLast(); ca.moveToNext()) {
                int id = ca.getInt(ca.getColumnIndex(MediaStore.MediaColumns._ID));
                uri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id);
                context.getContentResolver().delete(uri, null, null);
            }
            ca.close();
        } catch (Exception ignored) {
        }

    }

    public void runMediaScan(String path) {
        AddToMediaLibraryHelper helper = new AddToMediaLibraryHelper(context);
        helper.scan(path,null);
    }


    FileOperationResponse renameDirectory(File source, File target, boolean overwriteTarget) {
        if (overwriteTarget) {
            deleteFileOrDir(target);
        }

        if (renameNormally(source, target)) {
            return new FileOperationResponse(FileOperationResponse.SUCCESS);
        }

        // Try the Storage Access Framework if it is just a rename within the same parent folder.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
                && source.getParent().equals(target.getParent()) && isOnExternalSdCard(source)) {
            DocumentFile document = getDocumentFile(source, true);
            if (document == null)
                return new FileOperationResponse(FileOperationResponse.ERROR_IO);
            if (document.renameTo(target.getName())) {
                return new FileOperationResponse(FileOperationResponse.SUCCESS);
            }
        }

        // Try the manual way, moving files individually.
        if (makeDirectory(target).getResponseCode() != FileOperationResponse.SUCCESS) {
            return new FileOperationResponse(FileOperationResponse.ERROR_IO);
        }

        File[] sourceFiles = source.listFiles();

        if (sourceFiles == null) {
            return new FileOperationResponse(FileOperationResponse.SUCCESS);
        }

        for (File sourceFile : sourceFiles) {
            String fileName = sourceFile.getName();
            File targetFile = new File(target, fileName);
            if (!copyFile(sourceFile, targetFile)) {
                // stop on first error
                return new FileOperationResponse(FileOperationResponse.ERROR_IO);
            }
        }
        // Only after successfully copying all files, delete files on source folder.
        for (File sourceFile : sourceFiles) {
            if (deleteFileOrDir(sourceFile).getResponseCode() != FileOperationResponse.SUCCESS) {
                // stop on first error
                return new FileOperationResponse(FileOperationResponse.ERROR_IO);
            }
        }
        return new FileOperationResponse(FileOperationResponse.SUCCESS);
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    boolean isOnExternalSdCard(final File file) {
        return getExternalSdCardFolder(file) != null;
    }


    FileOperationResponse renameFile(File source, File target, boolean overwriteTarget, android_file.io.File.ProgressCallback progressCallback) {

        if (overwriteTarget) {
            deleteFileOrDir(target);
        }

        if (renameNormally(source, target)) {
            return new FileOperationResponse(FileOperationResponse.SUCCESS);
        }


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
                && source.getParent().equals(target.getParent()) && isOnExternalSdCard(source)) {
            DocumentFile document = getDocumentFile(source, false);
            if (document != null && document.renameTo(target.getName()))
                return new FileOperationResponse(FileOperationResponse.SUCCESS);
        }

        InputStream inputStream = null;
        OutputStream outputStream = null;
        try {
            if (createNewFile(target).getResponseCode() != FileOperationResponse.SUCCESS)
                return new FileOperationResponse(FileOperationResponse.ERROR_IO);
            inputStream = getInputStream(source);
            outputStream = getOutputStream(target, false);
            if (outputStream == null || inputStream == null)
                return new FileOperationResponse(FileOperationResponse.ERROR_IO);
            outputStream = new BufferedOutputStream(outputStream);
            byte[] readBuffer = new byte[1024 * 8];
            int read = 0;
            long total = source.length();
            long current = 0;
            while ((read = inputStream.read(readBuffer)) > 0) {
                outputStream.write(readBuffer, 0, read);
                current += read;
                if (progressCallback != null)
                    progressCallback.onProgress((int) (((float) current / total) * 100));
            }

            deleteFileOrDir(source);
            return new FileOperationResponse(FileOperationResponse.SUCCESS);
        } catch (IOException ioException) {
            ioException.printStackTrace();
            deleteFileOrDir(target);
            return new FileOperationResponse(FileOperationResponse.ERROR_IO);
        } finally {
            try {
                if (inputStream != null)
                    inputStream.close();
            } catch (Exception ignored) {
            }
            try {
                if (outputStream != null)
                    outputStream.close();
            } catch (Exception ignored) {
            }
        }

    }

    FileOperationResponse copyFile(File from, File to, boolean overwrite, android_file.io.File.ProgressCallback progressCallback) {
        if (overwrite) {
            deleteFileOrDir(to);
        }

        InputStream inputStream = null;
        OutputStream outputStream = null;
        try {
            if (createNewFile(to).getResponseCode() != FileOperationResponse.SUCCESS)
                return new FileOperationResponse(FileOperationResponse.ERROR_IO);
            inputStream = getInputStream(from);
            outputStream = getOutputStream(to, false);
            if (outputStream == null || inputStream == null)
                return new FileOperationResponse(FileOperationResponse.ERROR_IO);
            outputStream = new BufferedOutputStream(outputStream);
            byte[] readBuffer = new byte[1024 * 8];
            int read = 0;
            long total = from.length();
            long current = 0;
            while ((read = inputStream.read(readBuffer)) > 0) {
                outputStream.write(readBuffer, 0, read);
                current += read;
                if (progressCallback != null)
                    progressCallback.onProgress((int) (((float) current / total) * 100));
            }

            return new FileOperationResponse(FileOperationResponse.SUCCESS);
        } catch (IOException ioException) {
            ioException.printStackTrace();
            deleteFileOrDir(to);
            return new FileOperationResponse(FileOperationResponse.ERROR_IO);
        } finally {
            try {
                if (inputStream != null)
                    inputStream.close();
            } catch (Exception ignored) {
            }
            try {
                if (outputStream != null)
                    outputStream.close();
            } catch (Exception ignored) {
            }
        }

    }

    FileOutputStream getOutputStream(File file, boolean append) {
        FileOutputStream outStream = null;
        try {
            // First try the normal way
            if (isFileWritableNormally(file)) {
                outStream = new FileOutputStream(file, append);
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    // Storage Access Framework
                    int retry = 0;
                    while (retry < 5) {
                        try {
                            DocumentFile targetDocument = getDocumentFile(file, false);
                            if (file.getName().equals(targetDocument.getName())) {
                                outStream =
                                        (FileOutputStream) context.getContentResolver().openOutputStream(targetDocument.getUri(), append ? "rw" : "w");
                                break;
                            } else {
                                Thread.sleep(1000 + new Random().nextInt(2000));
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            Thread.sleep(1000 + new Random().nextInt(1000));
                        }
                        retry++;
                    }

                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                    // Workaround for Kitkat ext SD card
                    return mediaStoreHack.getOutputStream(context, file.getPath(), append);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return outStream;
    }


    @Nullable
    FileInputStream getInputStream(File file) {
        FileInputStream fileIn = null;
        try {
            fileIn = new FileInputStream(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return fileIn;
    }

    boolean isWritableNormally(File file) {
        return isFileWritableNormally(file);
    }

    boolean isWritableNormallyOrBySAF(File file) {
        return isDirectoryWritableViaNormalOrSaf(file);
    }


    File getRootStorageItemFile(File file) {
        List<StorageItem> storageItems = getStorageItems("External", "Internal");
        if (storageItems == null || storageItems.size() == 0)
            return file;
        for (StorageItem storageItem : storageItems) {
            if (file.getAbsolutePath().startsWith(storageItem.getFile().getAbsolutePath()))
                return storageItem.getFile().getWrappedFile();
        }
        return file;
    }


    long getFreeSpace(File file) {
        try {
            StatFs stat = new StatFs(file.getAbsolutePath());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                return (long) stat.getBlockSizeLong() * (long) stat.getAvailableBlocksLong();
            } else {
                return (long) stat.getBlockSize() * (long) stat.getAvailableBlocks();
            }
        } catch (Exception e) {
            return 0;
        }

    }


    public String getDisplayableName(File file, String externalStorageName, String internalStorageName) {
        List<StorageItem> storageItems = getStorageItems(externalStorageName, internalStorageName);
        if (storageItems == null || storageItems.size() == 0)
            return file.getAbsolutePath();
        for (StorageItem storageItem : storageItems) {
            if (file.getAbsolutePath().startsWith(storageItem.getFile().getAbsolutePath()))
                return storageItem.getDisplayName() + file.getAbsolutePath().replaceFirst(storageItem.getFile().getAbsolutePath(), "").replace("//", "/");
        }
        return file.getAbsolutePath();
    }

    //-------------------------------------------------------------------------

    private boolean renameNormally(File source, File target) {
        return source.renameTo(target);
    }


    private boolean internalMakeDirectory(final File dir) {
        if (dir == null)
            return false;
        if (dir.exists()) {
            return dir.isDirectory();
        }

        if (dir.mkdirs()) {
            return true;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && isOnExternalSdCard(dir)) {
            DocumentFile document = getDocumentFile(dir, true);
            if (document == null)
                return false;
            return document.exists();
        }

        // Try the Kitkat workaround.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            try {
                return mediaStoreHack.mkdir(context, dir);
            } catch (IOException e) {
                return false;
            }
        }

        return false;
    }

    private boolean internalCreateFile(final File file) {
        if (file == null)
            return false;
        if (file.exists()) {
            // nothing to create.
            return !file.isDirectory();
        }

        // Try the normal way
        try {
            if (file.createNewFile()) {
                return true;
            }
        } catch (IOException e) {

        }
        boolean b = true;
        // Try with Storage Access Framework.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && isOnExternalSdCard(file)) {
            DocumentFile document = getDocumentFile(file.getParentFile(), true);
            // getDocumentFile implicitly creates the directory.
            try {
                b = document.createFile(MimeTypes.getMimeType(file), file.getName()) != null;
                return b;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            try {
                return mediaStoreHack.mkfile(context, file);
            } catch (Exception e) {
                return false;
            }
        }
        return false;
    }


    private boolean canListFiles(File f) {
        try {
            if (f.canRead() && f.isDirectory())
                return true;
            else
                return false;
        } catch (Exception e) {
            return false;
        }
    }


    /**
     * Check for a directory if it is possible to create files within this directory, either via normal writing or via
     * Storage Access Framework.
     *
     * @param dir a directory inside which we need to write. Make sure you pass a directory that already exists
     * @return boolean as result
     */
    private boolean isDirectoryWritableViaNormalOrSaf(File dir) {

        // check if the passed parameter is actually a directory
        if (dir.exists() && !dir.isDirectory()) {
            return false;
        }

        // try to generate a dummy diagnosis file which does not already exists exists
        int i = 0;
        File dummyDiagnosisFile = null;
        do {
            String dummyFileName = "dummyDiagnosisFile" + (++i);
            dummyDiagnosisFile = new File(dir, dummyFileName);
        } while (dummyDiagnosisFile.exists());

        //first lets check normal writability
        if (isFileWritableNormally(dummyDiagnosisFile)) {
            deleteFileOrDirWithAllWorkAround(dummyDiagnosisFile);
            return true;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // Next check SAF writability.
            DocumentFile documentFile = getDocumentFile(dummyDiagnosisFile, false);
            if (documentFile == null) {
                return false;
            }

            // This should have created the file - otherwise something is wrong with access URL.
            boolean result = documentFile.canWrite() && dummyDiagnosisFile.exists();

            //make sure dummy file is deleted
            deleteFileOrDirWithAllWorkAround(dummyDiagnosisFile);
            return result;
        }

        return false;

    }


    /**
     * Delete a file or directory by all workarounds possible.
     *
     * @param fileOrDir the file or directory to be deleted.
     * @return True if successfully deleted.
     */
    private boolean deleteFileOrDirWithAllWorkAround(@NonNull final File fileOrDir) {

        //TODO : Test if this actually deletes a directory with content on external sd card on Lollipop And Kitkat

        try {
            if (!fileOrDir.exists())
                return true;

            boolean normalyDeleted = normalyDeleteFileOrDirectory(fileOrDir);
            if (fileOrDir.delete() || normalyDeleted)
                return true;

            // Try with Storage Access Framework.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && isOnExternalSdCard(fileOrDir)) {
                DocumentFile document = getDocumentFile(fileOrDir, false);
                if (document == null)
                    return false;
                return document.delete();
            }

            // Try the Kitkat workaround.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                ContentResolver resolver = context.getContentResolver();
                try {
                    Uri uri = mediaStoreHack.getUriFromFile(fileOrDir.getAbsolutePath(), context);
                    resolver.delete(uri, null, null);
                    return !fileOrDir.exists();
                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }
            }

            return !fileOrDir.exists();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

    }

    /**
     * Delete all files in a directory with the directory itself.
     *
     * @param dir the folder
     * @return true if successful.
     */
    private boolean normalyDeleteFileOrDirectory(@NonNull final File dir) {
        try {
            boolean totalSuccess = true;
            if (dir.isDirectory()) {
                for (File child : dir.listFiles()) {
                    normalyDeleteFileOrDirectory(child);
                }
                if (!dir.delete())
                    totalSuccess = false;
            } else {
                if (!dir.delete())
                    totalSuccess = false;
            }
            return totalSuccess;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

    }


    /**
     * Check if a file is writable.
     *
     * @param file The file (not a directory)
     * @return true if the file is writable.
     */
    private boolean isFileWritableNormally(final File file) {
        boolean isExisting = file.exists();
        try {
            FileOutputStream output = new FileOutputStream(file, true);
            try {
                output.close();
            } catch (IOException ignore) {
            }
        } catch (FileNotFoundException e) {
            return false;
        }
        boolean result = file.canWrite();

        if (!isExisting) {
            file.delete();
        }

        return result;
    }


    /**
     * Get a DocumentFile corresponding to the given file (for writing on ExtSdCard on Android 5). If the file is not
     * existing, it is created.
     *
     * @param file        The file.
     * @param isDirectory flag indicating if the file should be a directory.
     * @return The DocumentFile or null if failed
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private DocumentFile getDocumentFile(final File file, final boolean isDirectory) {

        try {
            String baseFolder = getExternalSdCardFolder(file).getAbsolutePath();
            boolean originalDirectory = false;
            if (baseFolder == null) {
                return null;
            }

            String relativePath = null;
            try {
                String fullPath = file.getCanonicalPath();
                if (!baseFolder.equals(fullPath))
                    relativePath = fullPath.substring(baseFolder.length() + 1);
                else originalDirectory = true;
            } catch (IOException e) {
                return null;
            } catch (Exception f) {
                originalDirectory = true;
                //continue
            }
            String as = PreferenceManager.getDefaultSharedPreferences(context).getString(SAFUtil.getKeyForPermissionKey(context), null);

            Uri treeUri = null;
            if (as != null) treeUri = Uri.parse(as);
            if (treeUri == null) {
                return null;
            }

            // start with root of SD card and then parse through document tree.
            DocumentFile document = DocumentFile.fromTreeUri(context, treeUri);
            if (originalDirectory) return document;
            String[] parts = relativePath.split("\\/");
            for (int i = 0; i < parts.length; i++) {
                DocumentFile nextDocument = document.findFile(parts[i]);
                if (nextDocument == null) {
                    if ((i < parts.length - 1) || isDirectory) {
                        nextDocument = document.createDirectory(parts[i]);
                    } else {
                        nextDocument = document.createFile("image", parts[i]);
                    }
                }
                document = nextDocument;
            }

            return document;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private boolean copyFile(File source, File target) {
        FileInputStream inStream = null;
        OutputStream outStream = null;
        FileChannel inChannel = null;
        FileChannel outChannel = null;
        try {
            inStream = new FileInputStream(source);

            // First try the normal way
            if (isFileWritableNormally(target)) {
                // standard way
                outStream = new FileOutputStream(target);
                inChannel = inStream.getChannel();
                outChannel = ((FileOutputStream) outStream).getChannel();
                inChannel.transferTo(0, inChannel.size(), outChannel);
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    // Storage Access Framework
                    DocumentFile targetDocument = getDocumentFile(target, false);
                    outStream =
                            context.getContentResolver().openOutputStream(targetDocument.getUri());
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                    // Workaround for Kitkat ext SD card
                    Uri uri = mediaStoreHack.getUriFromFile(target.getAbsolutePath(), context);
                    outStream = context.getContentResolver().openOutputStream(uri);
                } else {
                    return false;
                }

                if (outStream != null) {
                    // Both for SAF and for Kitkat, write to output stream.
                    byte[] buffer = new byte[16384];
                    int bytesRead;
                    while ((bytesRead = inStream.read(buffer)) != -1) {
                        outStream.write(buffer, 0, bytesRead);
                    }
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (inStream != null)
                    inStream.close();
            } catch (Exception e) {
                // ignore exception
            }
            try {
                if (outStream != null)
                    outStream.close();
            } catch (Exception e) {
                // ignore exception
            }
            try {
                if (inChannel != null)
                    inChannel.close();
            } catch (Exception e) {
                // ignore exception
            }
            try {
                if (outChannel != null)
                    outChannel.close();
            } catch (Exception e) {
                // ignore exception
            }
        }
        return true;
    }


    /**
     * Determine the main folder of the external SD card containing the given file.
     *
     * @param file the file.
     * @return The main folder of the external SD card containing this file, if the file is on an SD card. Otherwise,
     * null is returned.
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    private File getExternalSdCardFolder(final File file) {
        File[] extSdCards = getExternalSdCards();
        try {
            for (int i = 0; i < extSdCards.length; i++) {
                if (file.getCanonicalPath().startsWith(extSdCards[i].getAbsolutePath())) {
                    return extSdCards[i];
                }
            }
        } catch (IOException e) {
            return null;
        }
        return null;
    }

    /**
     * Get a list of external SD card paths. (Kitkat or higher. not emulated)
     *
     * @return A list of external SD card paths.
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    private File[] getExternalSdCards() {
        List<String> paths = new ArrayList<String>();
        for (File file : context.getExternalFilesDirs("external")) {
            if (file != null && !file.equals(context.getExternalFilesDir("external"))) {
                int index = file.getAbsolutePath().lastIndexOf("/Android/data");
                if (index >= 0) {
                    String path = file.getAbsolutePath().substring(0, index);
                    try {
                        path = new File(path).getCanonicalPath();
                    } catch (IOException e) {
                        // Keep non-canonical path.
                    }
                    paths.add(path);
                }
            }
        }
        if (paths.isEmpty())
            paths.add("/storage/sdcard1");
        File[] result = new File[paths.size()];
        int i = 0;
        for (String path : paths) {
            result[i] = new File(path);
            i++;
        }
        return result;
    }

    Context getContext() {
        return context;
    }

    private static class AddToMediaLibraryHelper {
        public interface OnMediaScanListener {
            void onError();

            void onSuccess(Uri uri);
        }

        private MediaScannerConnection conn;
        private Context context;

        public AddToMediaLibraryHelper(Context context) {
            this.context = context;
        }

        public void scan(final String path, final OnMediaScanListener lis) {
            if (conn != null)
                conn.disconnect();

            if (!new File(path).isFile()) {
                if (lis != null)
                    lis.onError();
                return;
            }

            conn = new MediaScannerConnection(context, new MediaScannerConnection.MediaScannerConnectionClient() {
                public void onMediaScannerConnected() {
                    conn.scanFile(path, null);
                }

                public void onScanCompleted(String arg0, Uri arg1) {
                    conn.disconnect();
                    if (arg1 == null) {
                        if (lis != null)
                            lis.onError();
                    }
                    if (lis != null)
                        lis.onSuccess(arg1);

                }
            });
            conn.connect();
        }

    }
}
