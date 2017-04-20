package android_file.io.storage;

import android.content.Context;
import android.os.Environment;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

/**
 * @author Rahul Verma on 13/02/17 <rv@videoder.com>
 */

public class DStorageItemsLister extends StorageItemsLister{

    public DStorageItemsLister(Context context) {
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
        final HashSet<String> out = new HashSet<String>();
        String reg = "(?i).*vold.*(vfat|ntfs|exfat|fat32|ext3|ext4).*rw.*";
        String s = "";
        try {
            final Process process = new ProcessBuilder().command("mount")
                    .redirectErrorStream(true).start();
            process.waitFor();
            final InputStream is = process.getInputStream();
            final byte[] buffer = new byte[1024];
            while (is.read(buffer) != -1) {
                s = s + new String(buffer);
            }
            is.close();
        } catch (final Exception e) {
            e.printStackTrace();
        }

        // parse output
        final String[] lines = s.split("\n");
        for (String line : lines) {
            if (!line.toLowerCase(Locale.US).contains("asec")) {
                if (line.matches(reg)) {
                    String[] parts = line.split(" ");
                    for (String part : parts) {
                        if (part.startsWith("/"))
                            if (!part.toLowerCase(Locale.US).contains("vold"))
                                out.add(part);
                    }
                }
            }
        }
        return new ArrayList<>(out);
    }



   
}
