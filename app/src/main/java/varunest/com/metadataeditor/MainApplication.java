package varunest.com.metadataeditor;

import android.app.Application;

import android_file.io.File;

/**
 * @author varun on 17/04/17.
 */

public class MainApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        File.init(this);
    }
}
