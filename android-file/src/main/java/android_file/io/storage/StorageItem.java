package android_file.io.storage;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import android_file.io.File;

public class StorageItem implements Parcelable {

    public static final int TYPE_INTERNAL = 1;
    public static final int TYPE_EXTERNAL = 2;

    @IntDef({TYPE_INTERNAL, TYPE_EXTERNAL})
    @Retention(RetentionPolicy.SOURCE)
    public @interface StorageType {
    }

    private String displayName;
    private File file;
    private
    @StorageItem.StorageType
    int storageType;

    public StorageItem(String displayName, File file, @StorageType int storageType) {
        this.storageType = storageType;
        this.file = file;
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public File getFile() {
        return file;
    }

    public
    @StorageType
    int getStorageType() {
        return storageType;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.displayName);
        dest.writeSerializable(this.file.getWrappedFile());
        dest.writeInt(this.storageType);
    }

    protected StorageItem(Parcel in) {
        this.displayName = in.readString();
        this.file = new File((java.io.File) in.readSerializable());
        //noinspection WrongConstant
        this.storageType = in.readInt();
    }

    public static final Parcelable.Creator<StorageItem> CREATOR = new Parcelable.Creator<StorageItem>() {
        @Override
        public StorageItem createFromParcel(Parcel source) {
            return new StorageItem(source);
        }

        @Override
        public StorageItem[] newArray(int size) {
            return new StorageItem[size];
        }
    };
}