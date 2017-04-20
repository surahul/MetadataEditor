package varunest.com.metadataeditor;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import android.support.v4.content.ContextCompat;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public final class MaterialValueHelper {

    @SuppressLint("PrivateResource")
    @ColorInt
    public static int getPrimaryTextColor(final Context context, boolean dark) {
        if (dark) {
            return ContextCompat.getColor(context, R.color.primary_text_default_material_light);
        }
        return ContextCompat.getColor(context, R.color.primary_text_default_material_dark);
    }

    @SuppressLint("PrivateResource")
    @ColorInt
    public static int getSecondaryTextColor(final Context context, boolean dark) {
        if (dark) {
            return ContextCompat.getColor(context, R.color.secondary_text_default_material_light);
        }
        return ContextCompat.getColor(context, R.color.secondary_text_default_material_dark);
    }

    @SuppressLint("PrivateResource")
    @ColorInt
    public static int getPrimaryDisabledTextColor(final Context context, boolean dark) {
        if (dark) {
            return ContextCompat.getColor(context, R.color.primary_text_disabled_material_light);
        }
        return ContextCompat.getColor(context, R.color.primary_text_disabled_material_dark);
    }

    @SuppressLint("PrivateResource")
    @ColorInt
    public static int getSecondaryDisabledTextColor(final Context context, boolean dark) {
        if (dark) {
            return ContextCompat.getColor(context, R.color.secondary_text_disabled_material_light);
        }
        return ContextCompat.getColor(context, R.color.secondary_text_disabled_material_dark);
    }

    @SuppressLint("PrivateResource")
    public static Drawable getSelectableItemBackground(final Context context, boolean dark, boolean borderless) {
        return ATHUtil.resolveDrawable(new android.view.ContextThemeWrapper(context, dark ? R.style.Theme_AppCompat : R.style.Theme_AppCompat_Light), borderless ? R.attr.selectableItemBackgroundBorderless : R.attr.selectableItemBackground);
    }

    private MaterialValueHelper() {
    }
}
