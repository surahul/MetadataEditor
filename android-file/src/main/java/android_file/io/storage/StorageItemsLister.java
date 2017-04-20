package android_file.io.storage;

import android.content.Context;

import java.io.File;
import java.util.List;

/**
 * @author Rahul Verma on 13/02/17 <rv@videoder.com>
 */

abstract class StorageItemsLister{
    Context context;

    public StorageItemsLister(Context context){
        this.context = context;
    }
    abstract List<StorageItem> getStorageItems(String defaultNameForInternal, String defaultNameForExternal);

    protected boolean canListFiles(File f) {
        try {
            if (f.canRead() && f.isDirectory())
                return true;
            else
                return false;
        } catch (Exception e) {
            return false;
        }
    }
}
