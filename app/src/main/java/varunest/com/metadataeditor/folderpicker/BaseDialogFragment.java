
package varunest.com.metadataeditor.folderpicker;

import android.app.Dialog;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.Window;
import android.view.WindowManager;

import varunest.com.metadataeditor.R;


/**
 * Created by sur on 22/10/15.
 */
public class BaseDialogFragment extends DialogFragment {

    protected Dialog d;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        d = super.onCreateDialog(savedInstanceState);
        d.getWindow().setWindowAnimations(R.style.DialogAnimation);
        d.getWindow().setBackgroundDrawable(new ColorDrawable(0X00000000));
        d.requestWindowFeature(Window.FEATURE_NO_TITLE);
        d.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
        WindowManager.LayoutParams lp = d.getWindow().getAttributes();
        lp.dimAmount = .5f;
        d.getWindow().setAttributes(lp);
        d.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);

        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.LOLLIPOP){
            Window window = d.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
        return d;
    }

}
