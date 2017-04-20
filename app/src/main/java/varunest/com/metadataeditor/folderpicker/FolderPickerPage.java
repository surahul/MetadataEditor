package varunest.com.metadataeditor.folderpicker;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import java.util.List;

import android_file.io.File;
import android_file.io.storage.StorageItem;
import varunest.com.metadataeditor.R;

/**
 * @author Rahul Verma on 20/07/16.
 */
public class FolderPickerPage extends Fragment {

    private static final String ARG_STORAGE_ITEM = "arg_storage_item";
    private static final String ARG_ENABLE_FILE_PICK_MODE = "arg_enable_file_pick_mode";

    public static FolderPickerPage newInstance(StorageItem storageItem, boolean isEnableFilePickMode) {
        FolderPickerPage folderPickerPage = new FolderPickerPage();
        Bundle args = new Bundle();
        args.putParcelable(ARG_STORAGE_ITEM, storageItem);
        args.putBoolean(ARG_ENABLE_FILE_PICK_MODE, isEnableFilePickMode);
        folderPickerPage.setArguments(args);
        return folderPickerPage;
    }

    private StorageItem storageItem;
    private boolean filePickMode;
    private View root;
    private RecyclerView recyclerView;
    private FileListAdapter adapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            this.storageItem = getArguments().getParcelable(ARG_STORAGE_ITEM);
            this.filePickMode = getArguments().getBoolean(ARG_ENABLE_FILE_PICK_MODE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_folder_picker_page, container, false);
        wireUpWidgets();
        return root;
    }

    private void wireUpWidgets() {
        recyclerView = (RecyclerView) root.findViewById(R.id.recycler_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new FileListAdapter(getActivity(), true, filePickMode, listener);
        recyclerView.setAdapter(adapter);

    }

    private FileListAdapter.Listener listener = new FileListAdapter.Listener() {
        @Override
        public void onFolderSelected(File file) {
            if (getParentFragment() != null && getParentFragment() instanceof FolderPickerDialog) {
                ((FolderPickerDialog) getParentFragment()).getHelper().onFolderSelectedFromPage(file, storageItem);
            }
        }

        @Override
        public void onNewFolderClicked() {
            if (getParentFragment() != null && getParentFragment() instanceof FolderPickerDialog) {
                ((FolderPickerDialog) getParentFragment()).getHelper().onNewFolderFromPage(storageItem);
            }
        }

        @Override
        public void onFileSelected(File file) {
            if (getParentFragment() != null && getParentFragment() instanceof FolderPickerDialog) {
                ((FolderPickerDialog) getParentFragment()).getHelper().onFileSelectedFromPage(file, storageItem);
            }
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        requestUpdate();
        setTopPad();
    }

    private void requestUpdate() {
        if (getParentFragment() != null && getParentFragment() instanceof FolderPickerDialog) {
            ((FolderPickerDialog) getParentFragment()).getHelper().update(storageItem);
        }
    }

    private void setTopPad() {
        if (getParentFragment() != null && getParentFragment() instanceof FolderPickerDialog) {
            adapter.setTopPad(((FolderPickerDialog) getParentFragment()).getHelper().getTopBarSize());
            adapter.notifyDataSetChanged();
        }
    }

    public void update(List<File> list) {
        adapter.updateFileList(list);
        adapter.notifyDataSetChanged();
    }

}
