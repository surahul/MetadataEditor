package varunest.com.metadataeditor.folderpicker.viewholders;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.LinearLayout;

import varunest.com.metadataeditor.R;


/**
 * @author Rahul Verma on 20/07/16.
 */
public class PadViewHolder extends RecyclerView.ViewHolder{
    private View padView;
    public PadViewHolder(View itemView){
        super(itemView);
        this.padView = itemView.findViewById(R.id.pad);
    }
    public void bind(int pad){
        padView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,pad));
    }
}
