package varunest.com.metadataeditor;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.support.annotation.AttrRes;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.StyleRes;
import android.support.v7.view.ContextThemeWrapper;
import android.util.TypedValue;

/**
 * @author Aidan Follestad (afollestad)
 */
public final class ATHUtil {

    public static boolean isWindowBackgroundDark(Context context) {
        return !ColorUtil.isColorLight(ATHUtil.resolveColor(context, android.R.attr.windowBackground));
    }

    @ColorInt
    public static int resolveColor(Context context, @AttrRes int attr) {
        return resolveColor(context, attr, 0);
    }

    public static int resolveColor(Context context, @AttrRes int attr, int fallback) {
        TypedArray a = context.getTheme().obtainStyledAttributes(new int[]{attr});
        try {
            return a.getColor(0, fallback);
        } finally {
            a.recycle();
        }
    }

    public static Drawable resolveDrawable(Context context, @AttrRes int attr) {
        return resolveDrawable(context, attr, null);
    }


    public static Drawable resolveDrawable(Context context, @AttrRes int attr, Drawable fallback) {
        return resolveDrawable(context, 0, attr, fallback);
    }

    public static int resolveDimen(Context context, @AttrRes int attr) {
        TypedValue typedValue = new TypedValue();
        int[] toolbarSizeAttr = new int[]{attr};
        int indexOfAttrTextSize = 0;
        TypedArray a = context.obtainStyledAttributes(typedValue.data, toolbarSizeAttr);
        int result = a.getDimensionPixelSize(indexOfAttrTextSize, -1);
        a.recycle();
        return result;
    }


    public static Drawable resolveDrawable(Context context, @StyleRes int theme, @AttrRes int attr, Drawable fallback) {

        TypedArray a = theme == 0 ? context.getTheme().obtainStyledAttributes(new int[]{attr}) : new ContextThemeWrapper(context, theme).obtainStyledAttributes(new int[]{attr});
        try {
            Drawable result = a.getDrawable(0);
            return result == null ? fallback : result;
        } finally {
            a.recycle();
        }
    }


    public static boolean isInClassPath(@NonNull String clsName) {
        try {
            return inClassPath(clsName) != null;
        } catch (Throwable t) {
            return false;
        }
    }

    public static Class<?> inClassPath(@NonNull String clsName) {
        try {
            return Class.forName(clsName);
        } catch (Throwable t) {
            throw new IllegalStateException(String.format("%s is not in your class path! You must include the associated library.", clsName));
        }
    }

    private ATHUtil() {
    }
}