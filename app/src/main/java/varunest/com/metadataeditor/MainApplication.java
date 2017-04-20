package varunest.com.metadataeditor;

import android.app.Application;
import android.content.res.ColorStateList;

import com.afollestad.materialdialogs.internal.ThemeSingleton;

import android_file.io.File;

/**
 * @author varun on 17/04/17.
 */

public class MainApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        File.init(this);
        initMaterialDialogTheme();
    }

    public void initMaterialDialogTheme() {
        final ThemeSingleton md = ThemeSingleton.get();
        md.titleColor = ATHUtil.resolveColor(this, android.R.attr.textColorPrimaryInverse);
        md.contentColor = ATHUtil.resolveColor(this, android.R.attr.textColorSecondaryInverse);
        md.itemColor = md.titleColor;
        md.widgetColor = getResources().getColor(R.color.colorAccent);
        md.linkColor = ColorStateList.valueOf(md.widgetColor);
        md.positiveColor = ColorStateList.valueOf(md.widgetColor);
        md.neutralColor = ColorStateList.valueOf(md.widgetColor);
        md.negativeColor = ColorStateList.valueOf(md.widgetColor);
        md.darkTheme = true;
        md.backgroundColor = getResources().getColor(R.color.dialog_background);
    }

}
