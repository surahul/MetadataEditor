package android_file.io.storage;

import android.content.Context;
import android.os.Environment;
import android.support.v4.content.ContextCompat;
import android.support.v4.os.EnvironmentCompat;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Rahul Verma on 13/02/17 <rv@videoder.com>
 */

public class EStorageItemsLister extends StorageItemsLister{

    public EStorageItemsLister(Context context) {
        super(context);
    }

    @Override
    List<StorageItem> getStorageItems(String defaultNameForInternal, String defaultNameForExternal) {
        try{
            List<StorageItem> storageItems = new ArrayList<>();

            List<String> storageStrings = getStorageDirectories();
            boolean internalAdded = false;
            for (String storageString : storageStrings) {
                android_file.io.File storageFile = new android_file.io.File(storageString);
                String name;
                @StorageItem.StorageType int type;
                if ("/storage/emulated/legacy".equals(storageString) || "/storage/emulated/0".equals(storageString)) {
                    name = defaultNameForInternal;
                    type = StorageItem.TYPE_INTERNAL;
                    internalAdded = true;
                } else if ("/storage/sdcard1".equals(storageString)) {
                    name = defaultNameForExternal;
                    type = StorageItem.TYPE_EXTERNAL;
                } else {
                    name = storageFile.getName();
                    type = internalAdded ? StorageItem.TYPE_EXTERNAL : StorageItem.TYPE_INTERNAL;
                }
                storageItems.add(new StorageItem(name, storageFile, type));
            }

            if (storageItems.size() == 0) {
                StorageItem item = new StorageItem(defaultNameForInternal, new android_file.io.File(Environment.getExternalStorageDirectory()), StorageItem.TYPE_INTERNAL);
                storageItems.add(item);
            }

            return storageItems;
        }catch (Exception e){
            e.printStackTrace();
            return new ArrayList<>();
        }

    }




    /**
     * Returns all available external SD-Card roots in the system.
     *
     * @return paths to all available external SD-Card roots in the system.
     */
    private List<String> getStorageDirectories() {
        final File[] externalCacheDirs= ContextCompat.getExternalCacheDirs(context);
        if(externalCacheDirs==null||externalCacheDirs.length==0)
            return null;
        if(externalCacheDirs.length==1)
        {
            if(externalCacheDirs[0]==null)
                return null;
            final String storageState= EnvironmentCompat.getStorageState(externalCacheDirs[0]);
            if(!Environment.MEDIA_MOUNTED.equals(storageState))
                return null;
        }
        final List<String> result=new ArrayList<>();
        if(externalCacheDirs.length==1)
            result.add(getRootOfInnerSdCardFolder(externalCacheDirs[0]));
        for(int i=1;i<externalCacheDirs.length;++i)
        {
            final File file=externalCacheDirs[i];
            if(file==null)
                continue;
            final String storageState=EnvironmentCompat.getStorageState(file);
            if(Environment.MEDIA_MOUNTED.equals(storageState))
                result.add(getRootOfInnerSdCardFolder(externalCacheDirs[i]));
        }
        if(result.isEmpty())
            return null;
        return result;
    }
    private static String getRootOfInnerSdCardFolder(File file)
    {
        if(file==null)
            return null;
        final long totalSpace=file.getTotalSpace();
        while(true)
        {
            final File parentFile=file.getParentFile();
            if(parentFile==null||parentFile.getTotalSpace()!=totalSpace)
                return file.getAbsolutePath();
            file=parentFile;
        }
    }



   
}
