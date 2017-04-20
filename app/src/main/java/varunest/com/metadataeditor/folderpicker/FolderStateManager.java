package varunest.com.metadataeditor.folderpicker;

import android.app.Activity;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android_file.io.File;
import android_file.io.exceptions.SAFRequiredException;
import android_file.io.storage.StorageItem;

/**
 * @author varun on 25/06/16.
 */
class FolderStateManager {
    private FolderPickerConfig config;
    private StorageItem storageItem;
    private File currentFolder;
    private List<File> folderContents;

    private Listener listener;


    FolderStateManager(Listener listener, FolderPickerConfig config, StorageItem storageItem) {
        this.listener = listener;
        this.config = config;
        this.storageItem = storageItem;
        if (config.getDefaultDirectory().getAbsolutePath().startsWith(storageItem.getFile().getAbsolutePath())) {
            this.currentFolder = config.getDefaultDirectory();
        } else {
            this.currentFolder = storageItem.getFile();
        }
    }

    public boolean moveUpFolder() {
        if (canMoveUp(currentFolder)) {
            switchToFolder(currentFolder.getParentFile());
            return true;
        } else {
            return false;
        }
    }

    public void switchToFolder(File folder) {
        currentFolder = folder;
        folderContents = listDirectories(currentFolder);
        listener.folderUpdated(currentFolder, folderContents, canMoveUp(currentFolder), storageItem);
    }

    public void update() {
        switchToFolder(currentFolder);
    }

    public StorageItem getStorageItem() {
        return storageItem;
    }


    public void createAndMoveToFolder(final Activity context, final String folderName) {
        File folder = new File(currentFolder.getAbsolutePath() + File.separator + folderName);
        try {
            if(folder.exists()&&folder.isDirectory()){
                switchToFolder(folder);
            }else {
                folder.mkDirs();
                switchToFolder(folder);
            }
        } catch (IOException e) {
            if (e instanceof SAFRequiredException) {
                Toast.makeText(context, "You need to give SAF permission to create file in this directory.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, "Failed to create folder.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public File getSelectedFolder() {
        return currentFolder;
    }

    public boolean canMoveUp(File directory) {
        if (directory.getParent() != null
                && directory.getParentFile() != null
                && directory.getParentFile().listFiles() != null
                && !directory.getAbsolutePath().equals(storageItem.getFile().getAbsolutePath())
                ) {
            return true;
        }
        return false;
    }

    private List<File> listDirectories(File directory) {
        File[] contents = directory.listFiles();
        List<File> results = new ArrayList<>();
        if (contents != null) {
            for (File fi : contents) {
                if (fi.isDirectory() || config.showNonDirectoryFiles()) {
                    if (!(!config.showHiddenFiles() && fi.isHidden())) {
                        results.add(fi);
                    }
                }
            }
            Collections.sort(results, new FolderSorter());
            return results;
        }
        return results;
    }

    public boolean presentInFolderContents(String folderName) {
        for (int i = 0; i < folderContents.size(); i++) {
            if (folderContents.get(i).getName().equals(folderName)) {
                return true;
            }
        }
        return false;
    }


    private static class FolderSorter implements Comparator<File> {
        @Override
        public int compare(File lhs, File rhs) {
            return lhs.getName().compareTo(rhs.getName());
        }
    }

    interface Listener {
        void folderUpdated(File currentFolder, List<File> folderContents, boolean canMoveUp, StorageItem storageItem);
    }
}
