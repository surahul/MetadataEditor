package android_file.io.exceptions;

import java.io.IOException;

/**
 * @author Rahul Verma on 02/12/16.
 */

public class SAFRequiredException extends IOException {

    private static final long serialVersionUID = 7250870679677244543L;

    public SAFRequiredException() {
        super();
    }


    public SAFRequiredException(String detailMessage) {
        super(detailMessage);
    }
}
