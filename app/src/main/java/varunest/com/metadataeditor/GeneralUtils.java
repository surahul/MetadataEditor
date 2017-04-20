package varunest.com.metadataeditor;

import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import java.util.regex.Pattern;

public class GeneralUtils {
    public static final String INVALID_STRING_VALUE = "INVALID";


    public static String formatSize(long size, String naReplacement) {
        if (size <= 1)
            return naReplacement.equals(INVALID_STRING_VALUE) ? "n.a." : naReplacement;
        if (size > 1024 * 1024 * 1024) {
            return String.format("%.2f GB", size / (float) (1024 * 1024 * 1024));
        } else if (size > 1024 * 1024) {
            return String.format("%.1f MB", size / (float) (1024 * 1024));
        } else if (size > 1024) {
            return String.format("%.1f KB", size / 1024.0f);
        } else {
            return String.format("%d B", size);
        }

    }

    public static String determineExtension(String url) {
        if (url == null)
            return "unknown_ext";
        String guess = url.split("\\?")[0];
        if (guess.lastIndexOf(".") != -1) {
            guess = guess.substring(guess.lastIndexOf(".") + 1);
        } else {
            guess = "";
        }
        Pattern pattern = Pattern.compile("^[A-Za-z0-9]+$");
        if (pattern.matcher(guess).matches()) {
            return guess;
        } else if (guess.lastIndexOf("/") != -1) {
            guess = guess.substring(0, guess.lastIndexOf("/"));
            String[] knownExts = new String[]{"mp4", "m4a", "m4p", "m4b", "m4r", "m4v", "aac",
                    "flv", "f4v", "f4a", "f4b",
                    "webm", "ogg", "ogv", "oga", "ogx", "spx", "opus",
                    "mkv", "mka", "mk3d",
                    "avi", "divx",
                    "mov",
                    "asf", "wmv", "wma",
                    "3gp", "3g2",
                    "mp3",
                    "flac",
                    "ape",
                    "wav",
                    "f4f", "f4m", "m3u8", "smil"};
            for (String known : knownExts) {
                if (guess.equals(known))
                    return guess;
            }
        }
        return "unknown_ext";
    }

    public static void showSoftKeyboard(View view) {
        try {
            if (view.requestFocus()) {
                InputMethodManager imm = (InputMethodManager)
                        view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
