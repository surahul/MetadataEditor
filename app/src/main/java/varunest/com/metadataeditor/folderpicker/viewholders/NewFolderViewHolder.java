package varunest.com.metadataeditor.folderpicker.viewholders;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.AttrRes;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import varunest.com.metadataeditor.R;
import varunest.com.metadataeditor.folderpicker.FileListAdapter;

public class NewFolderViewHolder extends RecyclerView.ViewHolder{

    private FileListAdapter.Listener listener;

    public NewFolderViewHolder(View itemView) {
        super(itemView);
        theme();
    }

    private void theme(){
        ((TextView)itemView.findViewById(R.id.add_text)).setTextColor(resolveColor(itemView.getContext(), R.attr.colorAccent, 0));
        ((TextView)itemView.findViewById(R.id.add_icon)).setTextColor(resolveColor(itemView.getContext(), R.attr.colorAccent, 0));
    }

    public void bindItem(FileListAdapter.Listener listener) {
        this.listener = listener;
        itemView.setOnClickListener(itemClickListener);
    }

    private View.OnClickListener itemClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            listener.onNewFolderClicked();
        }
    };

    public static int resolveColor(Context context, @AttrRes int attr, int fallback) {
        TypedArray a = context.getTheme().obtainStyledAttributes(new int[]{attr});
        try {
            return a.getColor(0, fallback);
        } finally {
            a.recycle();
        }
    }
}