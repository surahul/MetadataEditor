package varunest.com.metadataeditor.folderpicker;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import android_file.io.File;
import varunest.com.metadataeditor.R;
import varunest.com.metadataeditor.folderpicker.viewholders.FileItemViewHolder;
import varunest.com.metadataeditor.folderpicker.viewholders.NewFolderViewHolder;
import varunest.com.metadataeditor.folderpicker.viewholders.PadViewHolder;

public class FileListAdapter extends RecyclerView.Adapter {
    private static final int ITEM_TYPE_PAD = 1;
    private static final int ITEM_TYPE_FILE = 2;
    private static final int ITEM_TYPE_NEW_FOLDER = 3;


    private List<File> fileList;
    private List<BuiltItem> builtItems = new ArrayList<>();
    private Context context;
    private Listener listener;
    private boolean showNewFolder;
    private int topPad = 0;
    private int bottomPad = 0;
    private boolean modePickFile;


    public FileListAdapter(Context context, boolean showNewFolder, boolean modePickFile, Listener listener) {
        this.showNewFolder = showNewFolder;
        this.fileList = new ArrayList<>();
        this.context = context;
        this.listener = listener;
        this.modePickFile = modePickFile;

        registerAdapterDataObserver(dataSetObserver);
    }

    public void setTopPad(int topPad){
        this.topPad = topPad;
    }
    public void setBottomPad(int bottomPad){
        this.bottomPad = bottomPad;
    }


    @Override
    public int getItemViewType(int position) {
        return builtItems.get(position).itemType;
    }

    private RecyclerView.AdapterDataObserver dataSetObserver = new RecyclerView.AdapterDataObserver() {
        @Override
        public void onChanged() {
            builtItems = new ArrayList<>();
            if(topPad>0)
                builtItems.add(new BuiltItem(topPad));
            if(showNewFolder)
                builtItems.add(new BuiltItem());
            for(File file : fileList)
                builtItems.add(new BuiltItem(file));
            if(bottomPad>0)
                builtItems.add(new BuiltItem(bottomPad));
            super.onChanged();
        }
    };



    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = null;

        switch (viewType){
            case ITEM_TYPE_FILE:
                view = LayoutInflater.from(context).inflate(R.layout.item_file, parent, false);
                return new FileItemViewHolder(view);
            case ITEM_TYPE_NEW_FOLDER:
                view = LayoutInflater.from(context).inflate(R.layout.item_new_folder, parent, false);
                return new NewFolderViewHolder(view);
            case ITEM_TYPE_PAD:
                view = LayoutInflater.from(context).inflate(R.layout.pad, parent, false);
                return new PadViewHolder(view);

        }

        return null;

    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {

        switch (getItemViewType(position)){
            case ITEM_TYPE_FILE:
                ((FileItemViewHolder) holder).bindItem(builtItems.get(position).file, listener, modePickFile);
                break;
            case ITEM_TYPE_PAD:
                ((PadViewHolder) holder).bind(builtItems.get(position).pad);
                break;
            case ITEM_TYPE_NEW_FOLDER:
                ((NewFolderViewHolder) holder).bindItem(listener);
                break;
        }

    }

    @Override
    public int getItemCount() {
        return  builtItems.size();
    }

    public void updateFileList(List<File> contentsArray) {
        if (contentsArray != null) {
            this.fileList = contentsArray;
        } else {
            fileList = new ArrayList<>();
        }
    }

    public interface Listener {
        void onFolderSelected(File file);
        void onNewFolderClicked();
        void onFileSelected(File file);
    }


    private static class BuiltItem{
        private int itemType;
        private File file;
        private int pad;

        public BuiltItem(File file){
            this.file = file;
            itemType = ITEM_TYPE_FILE;
        }

        public BuiltItem(int pad){
            this.pad = pad;
            itemType = ITEM_TYPE_PAD;
        }

        public BuiltItem(){
            itemType = ITEM_TYPE_NEW_FOLDER;
        }
    }
}