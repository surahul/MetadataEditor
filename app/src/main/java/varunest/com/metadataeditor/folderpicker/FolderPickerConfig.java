package varunest.com.metadataeditor.folderpicker;

import android.os.Environment;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import java.io.IOException;

import android_file.io.File;
import android_file.io.exceptions.SAFRequiredException;

/**
 * @author varun on 25/06/16.
 */
public class FolderPickerConfig implements Parcelable {

    private boolean showHiddenFiles = false;
    private boolean showNonDirectoryFiles = false;
    private File defaultDirectory;
    private boolean showCancelButton = true;
    private boolean enableFilePickmode = false;

    private FolderPickerConfig() {
        defaultDirectory = new File(Environment.getExternalStorageDirectory());
    }

    public File getDefaultDirectory() {
        return defaultDirectory;
    }


    public boolean showHiddenFiles() {
        return showHiddenFiles;
    }

    public boolean showCancelButton() {
        return showCancelButton;
    }

    public boolean showNonDirectoryFiles() {
        return showNonDirectoryFiles;
    }

    public boolean isEnableFilePickmode() {
        return enableFilePickmode;
    }

    public static class Builder {
        private FolderPickerConfig config;

        public Builder() {
            config = new FolderPickerConfig();
        }

        public Builder showHiddenFiles(boolean flag) {
            config.showHiddenFiles = flag;
            return this;
        }

        public Builder showNonDirectoryFiles(boolean flag) {
            config.showNonDirectoryFiles = flag;
            return this;
        }

        public Builder showCancelButton(boolean flag) {
            config.showCancelButton = flag;
            return this;
        }


        public Builder setDefaultDirectory(String defaultDirectoryPath, @NonNull EventCallback safRequiredCallback) {
            config.defaultDirectory = new File(defaultDirectoryPath);
            try {
                config.defaultDirectory.mkDirs();
            } catch (IOException e) {
                if (e instanceof SAFRequiredException)
                    safRequiredCallback.onEvent(null);
            }
            return this;
        }

        public FolderPickerConfig build() {
            return config;
        }

        public Builder enableFilePickMode(boolean b) {
            config.enableFilePickmode = b;
            return this;
        }
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte(showHiddenFiles ? (byte) 1 : (byte) 0);
        dest.writeByte(showNonDirectoryFiles ? (byte) 1 : (byte) 0);
        dest.writeSerializable(this.defaultDirectory.getWrappedFile());
        dest.writeByte(showCancelButton ? (byte) 1 : (byte) 0);
    }

    protected FolderPickerConfig(Parcel in) {
        this.showHiddenFiles = in.readByte() != 0;
        this.showNonDirectoryFiles = in.readByte() != 0;
        this.defaultDirectory = new File((java.io.File) in.readSerializable());
        this.showCancelButton = in.readByte() != 0;
    }

    public static final Parcelable.Creator<FolderPickerConfig> CREATOR = new Parcelable.Creator<FolderPickerConfig>() {
        @Override
        public FolderPickerConfig createFromParcel(Parcel source) {
            return new FolderPickerConfig(source);
        }

        @Override
        public FolderPickerConfig[] newArray(int size) {
            return new FolderPickerConfig[size];
        }
    };
}
