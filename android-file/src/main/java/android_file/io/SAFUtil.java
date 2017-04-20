package android_file.io;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import android_file.io.exceptions.SAFOperationFailedException;

/**
 * @author Rahul Verma on 02/12/16 <rv@videoder.com>
 */


/**
 * Utility class for initiating the SAF Permission request flow
 */
public class SAFUtil {

    private static final String KEY_CUSTOM_KEY = "android_file_key_custom_key";
    private static final String DEFAULT_KEY = "android_file_saf_permission_key";

    private static final int REQUEST_CODE_SAF = 3157;

    private Runnable runPostSAFComplete;

    /**
     * start the document picker activity to request SAF permission for any folder (mostly you request this for external SD card)
     *
     * @param activity           the activity whose onResult will be called on returning from document picker activity
     * @param runPostSAFComplete a runnable to execute when SAF permission has been requested successfully
     * @throws ActivityNotFoundException if document picker activity is not available
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void requestSAFPermission(Activity activity, @Nullable Runnable runPostSAFComplete) throws ActivityNotFoundException {
        this.runPostSAFComplete = runPostSAFComplete;
        activity.startActivityForResult(new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE), REQUEST_CODE_SAF);
    }

    /**
     * call this method from the activity's onResult which was passed to requestSAFPermission method
     *
     * @param activity                         the activity whose onResult has been called on returning from document picker activity
     * @param customKeyForStoringPermissionKey a custom key for storing saf permission uri
     * @throws SAFOperationFailedException if the user did not gave the permission
     */
    public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent data, @Nullable String customKeyForStoringPermissionKey) throws SAFOperationFailedException {
        if (requestCode == REQUEST_CODE_SAF && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity);
            String p = sharedPreferences.getString(getKeyForStoringPermissionKey(customKeyForStoringPermissionKey), null);
            Uri oldUri = null;
            if (p != null) oldUri = Uri.parse(p);
            Uri treeUri = null;
            if (resultCode == Activity.RESULT_OK) {
                treeUri = data.getData();
                if (treeUri != null)
                    sharedPreferences.edit().putString(getKeyForStoringPermissionKey(customKeyForStoringPermissionKey), treeUri.toString()).commit();
                if (!TextUtils.isEmpty(customKeyForStoringPermissionKey))
                    sharedPreferences.edit().putString(KEY_CUSTOM_KEY, customKeyForStoringPermissionKey).commit();
            }
            // If not confirmed SAF, or if still not writable, then revert settings.
            if (resultCode != Activity.RESULT_OK) {
                if (treeUri != null)
                    sharedPreferences.edit().putString(getKeyForStoringPermissionKey(customKeyForStoringPermissionKey), oldUri.toString()).commit();
                throw new SAFOperationFailedException("responseCode of result for saf request is not RESULT_OK. Its value is " + String.valueOf(resultCode));
            }
            activity.getContentResolver().takePersistableUriPermission(treeUri, data.getFlags()
                    & (Intent.FLAG_GRANT_READ_URI_PERMISSION
                    | Intent.FLAG_GRANT_WRITE_URI_PERMISSION));
            if (runPostSAFComplete != null)
                runPostSAFComplete.run();
        }
    }

    private String getKeyForStoringPermissionKey(String customKeyForStoringPermissionKey) {
        if (TextUtils.isEmpty(customKeyForStoringPermissionKey))
            return DEFAULT_KEY;
        else
            return customKeyForStoringPermissionKey;
    }

    protected static String getKeyForPermissionKey(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getString(KEY_CUSTOM_KEY, DEFAULT_KEY);
    }


}
