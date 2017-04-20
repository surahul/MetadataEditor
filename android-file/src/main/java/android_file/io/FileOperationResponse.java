package android_file.io;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @author Rahul Verma on 01/12/16 <rv@videoder.com>
 */

final class FileOperationResponse {
    static final int ERROR_SAF_PERMISSION_REQUIRED = 0;
    static final int ERROR_IO = 1;
    static final int SUCCESS = 2;

    @IntDef({ERROR_SAF_PERMISSION_REQUIRED, ERROR_IO, SUCCESS})
    @Retention(RetentionPolicy.SOURCE)
    @interface ResponseCode {
    }

    @ResponseCode
    private int responseCode;

    FileOperationResponse(@ResponseCode int responseCode) {
        this.responseCode = responseCode;
    }

    @ResponseCode
    int getResponseCode() {
        return responseCode;
    }
}
