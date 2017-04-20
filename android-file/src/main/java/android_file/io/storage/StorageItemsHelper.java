package android_file.io.storage;

import android.content.Context;
import android.os.Build;
import android.os.Environment;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import android_file.io.File;

/**
 * @author Rahul Verma on 13/02/17 <rv@videoder.com>
 */

public class StorageItemsHelper {

    private List<StorageItemsLister> storageItemsListerList;

    public StorageItemsHelper(Context context){
        storageItemsListerList = new ArrayList<>();
        storageItemsListerList.add(new GStorageItemsLister(context));
        storageItemsListerList.add(new AStorageItemsLister(context));
        storageItemsListerList.add(new BStorageItemsLister(context));
        storageItemsListerList.add(new CStorageItemsLister(context));
        storageItemsListerList.add(new DStorageItemsLister(context));
        storageItemsListerList.add(new EStorageItemsLister(context));
        storageItemsListerList.add(new FStorageItemsLister(context));
    }

    public List<StorageItem> getStorageItems(String defaultInternalStorageName, String defaultExternalStorageName){
        List<StorageItem> result = new ArrayList<>();
        for(StorageItemsLister lister : storageItemsListerList){
            List<StorageItem> currentItems = lister.getStorageItems(defaultInternalStorageName,defaultExternalStorageName);

            for(StorageItem currentItem : currentItems){
                if(!isValid(currentItem))
                    continue;
                boolean alreadyContains = false;

                for(StorageItem prevItem : result){
                    boolean equals = false;
                    try{
                       equals = (prevItem.getFile().getCanonicalPath().equals(currentItem.getFile().getCanonicalPath()))||(prevItem.getFile().getTotalSpace()==currentItem.getFile().getTotalSpace()&&prevItem.getFile().getFreeSpace()==currentItem.getFile().getFreeSpace());
                    }catch (Exception e){
                        equals = (prevItem.getFile().getAbsolutePath().equals(currentItem.getFile().getAbsolutePath()))||(prevItem.getFile().getTotalSpace()==currentItem.getFile().getTotalSpace()&&prevItem.getFile().getFreeSpace()==currentItem.getFile().getFreeSpace());
                    }
                    if(equals){
                        alreadyContains = true;
                        break;
                    }
                }
                if(!alreadyContains)
                    result.add(currentItem);
            }

        }

        try{
            if(result.size()==1){
                File storageFile = result.get(0).getFile();
                File externalStorageFile = new File(Environment.getExternalStorageDirectory());
                if(storageFile.getAbsolutePath().equals(externalStorageFile.getAbsolutePath())||(storageFile.getTotalSpace()==externalStorageFile.getTotalSpace()&&storageFile.getFreeSpace()==externalStorageFile.getFreeSpace())){
                    result.set(0,new StorageItem(defaultInternalStorageName,new File(Environment.getExternalStorageDirectory().getAbsoluteFile()),StorageItem.TYPE_INTERNAL));
                }
            }
        }catch (Exception ignored){}

        return result;
    }

    private boolean isValid(StorageItem storageItem){
        if(storageItem==null)
            return false;
        if(!storageItem.getFile().isDirectory()||storageItem.getFile().getTotalSpace()<=0)
            return false;
        if(Build.VERSION.SDK_INT<Build.VERSION_CODES.KITKAT){
            //pre kitkat
            int i = 0;
            android_file.io.File dummyDiagnosisFile = null;
            do {
                String dummyFileName = "dummyDiagnosisFile" + (++i);
                dummyDiagnosisFile = new android_file.io.File(storageItem.getFile().getWrappedFile(), dummyFileName);
            } while (dummyDiagnosisFile.exists());

            //first lets check normal writability
            if (dummyDiagnosisFile.isWritableNormally()) {
                dummyDiagnosisFile.deleteSilently();
                return true;
            }
        }else if(Build.VERSION.SDK_INT<Build.VERSION_CODES.LOLLIPOP){
            // kitkat
            int i = 0;
            android_file.io.File dummyDiagnosisFile = null;
            do {
                String dummyFileName = "dummyDiagnosisFile" + (++i);
                dummyDiagnosisFile = new android_file.io.File(storageItem.getFile().getWrappedFile(), dummyFileName);
            } while (dummyDiagnosisFile.exists());

            //first lets check normal writability
            OutputStream outputStream = dummyDiagnosisFile.getOutputStream(true);
            if (outputStream!=null) {
                try{
                    outputStream.close();
                }catch (Exception e){e.printStackTrace();}
                dummyDiagnosisFile.deleteSilently();
                return true;
            }
        }else if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.LOLLIPOP){
            return true;
        }
        return false;
    }

}
