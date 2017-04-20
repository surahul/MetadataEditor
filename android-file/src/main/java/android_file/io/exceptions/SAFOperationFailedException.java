package android_file.io.exceptions;

/**
 * @author Rahul Verma on 02/12/16.
 */

public class SAFOperationFailedException extends Exception {

    private static final long serialVersionUID = 7250870679677244542L;

    public SAFOperationFailedException() {
        super();
    }

    public SAFOperationFailedException(String detailMessage) {
        super(detailMessage);
    }
}
