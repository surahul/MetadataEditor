package varunest.com.metadataeditor.folderpicker;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import android_file.io.File;
import varunest.com.metadataeditor.R;

public class FolderPickerDialog extends BaseDialogFragment {

    private static final String ARG_FOLDER_PICKER_CONFIG = "arg_folder_picker_config";
    private FolderPickerDialogHelper folderSelectHelper;
    private FolderPickerConfig folderPickerConfig;
    private FolderSelectCallback folderSelectCallback;


    public static FolderPickerDialog newInstance(FolderPickerConfig folderPickerConfig) {
        FolderPickerDialog folderPickerDialog = new FolderPickerDialog();
        Bundle args = new Bundle();
        args.putParcelable(ARG_FOLDER_PICKER_CONFIG, folderPickerConfig);
        folderPickerDialog.setArguments(args);
        return folderPickerDialog;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            Bundle args = getArguments();
            if (args.containsKey(ARG_FOLDER_PICKER_CONFIG)) {
                this.folderPickerConfig = getArguments().getParcelable(ARG_FOLDER_PICKER_CONFIG);
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.dialog_folder_picker, container, false);
        folderSelectHelper = new FolderPickerDialogHelper(folderPickerConfig, root, this, (AppCompatActivity) getActivity());
        folderSelectHelper.setFolderSelectionCallback(folderSelectCallback);
        return root;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == MotionEvent.ACTION_UP) {
                    boolean couldMoveUp = folderSelectHelper.onBackPressed();
                    return couldMoveUp;
                }
                return false;
            }
        });
        return dialog;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }


    public FolderPickerDialog setFolderSelectionCallback(FolderSelectCallback callback) {
        this.folderSelectCallback = callback;
        if (folderSelectHelper != null)
            folderSelectHelper.setFolderSelectionCallback(callback);
        return this;
    }


    public void show(AppCompatActivity context) {
        try {
            show(context.getSupportFragmentManager(), "FOLDER_SELECTOR");
        } catch (Exception e) {
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    public FolderPickerDialogHelper getHelper() {
        return folderSelectHelper;
    }


    public interface FolderSelectCallback {
        void onFolderSelection(File folder);

        void onFileSelection(File file);
    }
}