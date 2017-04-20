package varunest.com.metadataeditor.folderpicker.viewholders;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import android_file.io.File;
import varunest.com.metadataeditor.ATHUtil;
import varunest.com.metadataeditor.R;
import varunest.com.metadataeditor.TintHelper;
import varunest.com.metadataeditor.folderpicker.FileListAdapter;

public class FileItemViewHolder extends RecyclerView.ViewHolder {
    private File file;
    private TextView name;
    private ImageView icon;
    private FileListAdapter.Listener listener;

    public FileItemViewHolder(View itemView) {
        super(itemView);
        name = (TextView) itemView.findViewById(R.id.file_name);
        icon = (ImageView) itemView.findViewById(R.id.file_icon);
        TintHelper.setTint(icon, ATHUtil.resolveColor(itemView.getContext(), android.R.attr.textColorPrimaryInverse));
    }

    public void bindItem(final File file, FileListAdapter.Listener listener, boolean modePickFile) {
        this.file = file;
        this.listener = listener;
        name.setText(file.getName());
        icon.setImageResource(file.isDirectory() ? R.drawable.ic_folder_black : R.drawable.ic_file);

        if (modePickFile) {
            itemView.setOnClickListener(file.isFile() ? fileClickListener : folderClickListener);
        } else {
            itemView.setClickable(file.isDirectory());
            itemView.setAlpha(file.isDirectory() ? 1 : .2f);
            itemView.setOnClickListener(file.isDirectory() ? folderClickListener : null);
        }
    }

    private View.OnClickListener folderClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            listener.onFolderSelected(file);
        }
    };

    private View.OnClickListener fileClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            listener.onFileSelected(file);
        }
    };
}