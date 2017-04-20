package android_file.io.storage;

import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.support.v4.os.EnvironmentCompat;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Rahul Verma on 13/02/17 <rv@videoder.com>
 */

public class CStorageItemsLister extends StorageItemsLister{

    public CStorageItemsLister(Context context) {
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
        List<String> results = new ArrayList<>();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) { //Method 1 for KitKat & above
            File[] externalDirs = context.getExternalFilesDirs(null);

            for (File file : externalDirs) {
                String path = file.getPath().split("/Android")[0];

                boolean addPath = false;

                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    addPath = Environment.isExternalStorageRemovable(file);
                }
                else{
                    addPath = Environment.MEDIA_MOUNTED.equals(EnvironmentCompat.getStorageState(file));
                }

                if(addPath){
                    results.add(path);
                }
            }
        }

        if(results.isEmpty()) { //Method 2 for all versions
            // better variation of: http://stackoverflow.com/a/40123073/5002496
            String output = "";
            try {
                final Process process = new ProcessBuilder().command("mount | grep /dev/block/vold")
                        .redirectErrorStream(true).start();
                process.waitFor();
                final InputStream is = process.getInputStream();
                final byte[] buffer = new byte[1024];
                while (is.read(buffer) != -1) {
                    output = output + new String(buffer);
                }
                is.close();
            } catch (final Exception e) {
                e.printStackTrace();
            }
            if(!output.trim().isEmpty()) {
                String devicePoints[] = output.split("\n");
                for(String voldPoint: devicePoints) {
                    results.add(voldPoint.split(" ")[2]);
                }
            }
        }

        //Below few lines is to remove paths which may not be external memory card, like OTG (feel free to comment them out)
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            for (int i = 0; i < results.size(); i++) {
                if (!results.get(i).toLowerCase().matches(".*[0-9a-f]{4}[-][0-9a-f]{4}")) {
                    results.remove(i--);
                }
            }
        } else {
            for (int i = 0; i < results.size(); i++) {
                if (!results.get(i).toLowerCase().contains("ext") && !results.get(i).toLowerCase().contains("sdcard")) {
                    results.remove(i--);
                }
            }
        }
        return results;


    }



   
}
