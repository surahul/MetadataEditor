package android_file.io.storage;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.StringTokenizer;

import static android.content.ContentValues.TAG;

/**
 * @author Rahul Verma on 13/02/17 <rv@videoder.com>
 */

public class FStorageItemsLister extends StorageItemsLister{

    public FStorageItemsLister(Context context) {
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
        List<String> list = new ArrayList<>();
        String def_path = Environment.getExternalStorageDirectory().getPath();
        boolean def_path_internal = !Environment.isExternalStorageRemovable();
        String def_path_state = Environment.getExternalStorageState();
        boolean def_path_available = def_path_state.equals(Environment.MEDIA_MOUNTED)
                || def_path_state.equals(Environment.MEDIA_MOUNTED_READ_ONLY);
        boolean def_path_readonly = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED_READ_ONLY);
        BufferedReader buf_reader = null;
        try {
            HashSet<String> paths = new HashSet<String>();
            buf_reader = new BufferedReader(new FileReader("/proc/mounts"));
            String line;
            int cur_display_number = 1;
            Log.d(TAG, "/proc/mounts");
            while ((line = buf_reader.readLine()) != null) {
                Log.d(TAG, line);
                if (line.contains("vfat") || line.contains("/mnt")) {
                    StringTokenizer tokens = new StringTokenizer(line, " ");
                    String unused = tokens.nextToken(); //device
                    String mount_point = tokens.nextToken(); //mount point
                    if (paths.contains(mount_point)) {
                        continue;
                    }
                    unused = tokens.nextToken(); //file system
                    List<String> flags = Arrays.asList(tokens.nextToken().split(",")); //flags
                    boolean readonly = flags.contains("ro");

                    if (mount_point.equals(def_path)) {
                        paths.add(def_path);
                        list.add(0,def_path);
                    } else if (line.contains("/dev/block/vold")) {
                        if (!line.contains("/mnt/secure")
                                && !line.contains("/mnt/asec")
                                && !line.contains("/mnt/obb")
                                && !line.contains("/dev/mapper")
                                && !line.contains("tmpfs")) {
                            paths.add(mount_point);
                            list.add(mount_point);
                            cur_display_number++;
                        }
                    }
                }
            }

            if (!paths.contains(def_path) && def_path_available) {
                list.add(def_path);
            }

        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            if (buf_reader != null) {
                try {
                    buf_reader.close();
                } catch (IOException ex) {}
            }
        }
        return list;
    }




   
}
