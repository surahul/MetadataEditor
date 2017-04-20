package varunest.com.metadataeditor.folderpicker.viewholders;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import varunest.com.metadataeditor.R;
import varunest.com.metadataeditor.folderpicker.FileListAdapter;

import static com.afollestad.materialdialogs.util.DialogUtils.resolveColor;

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
}