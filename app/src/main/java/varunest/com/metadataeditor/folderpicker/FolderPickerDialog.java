package varunest.com.metadataeditor.folderpicker;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.lang.reflect.Field;

import android_file.io.File;
import varunest.com.metadataeditor.ColorUtil;
import varunest.com.metadataeditor.GeneralUtils;
import varunest.com.metadataeditor.MaterialValueHelper;
import varunest.com.metadataeditor.R;
import varunest.com.metadataeditor.TintHelper;

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

        setStyle(DialogFragment.STYLE_NORMAL, R.style.AppTheme);
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
        theme(view);
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

    private void theme(View root) {
        int colorPrimary = getResources().getColor(R.color.colorPrimary);
        int accentColor = getResources().getColor(R.color.colorAccent);
        boolean isPrimaryDark = true;
        int contentColor = GeneralUtils.resolveColor(getContext(), android.R.attr.textColorPrimaryInverse, 0);

        root.findViewById(R.id.top_bar_house).setBackgroundColor(colorPrimary);
        ((TextView) root.findViewById(R.id.single_storage_item_text_view)).setTextColor(contentColor);

        TabLayout tabLayout = (TabLayout) root.findViewById(R.id.tab_layout);
        tabLayout.setBackgroundColor(colorPrimary);
        tabLayout.setTabTextColors(ColorUtil.withAlpha(contentColor, .75f), contentColor);
        tabLayout.setSelectedTabIndicatorColor(contentColor);
        try {
            for (int i = 0; i < tabLayout.getTabCount(); i++) {
                TabLayout.Tab tab = tabLayout.getTabAt(i);
                Field fTabView = TabLayout.Tab.class.getDeclaredField("mView");
                fTabView.setAccessible(true);
                View tabView = (View) fTabView.get(tab);
                GeneralUtils.setBackgroundDrawable(tabView, MaterialValueHelper.getSelectableItemBackground(getActivity(), isPrimaryDark, false));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


        TintHelper.setTint((ImageView) root.findViewById(R.id.up_button), GeneralUtils.resolveColor(getActivity(), android.R.attr.textColorPrimaryInverse, 0));
        ((TextView) root.findViewById(R.id.cancel)).setTextColor(accentColor);
        ((TextView) root.findViewById(R.id.choose)).setTextColor(accentColor);

//        ATH.setStatusbarColor(d.getWindow(), ColorUtil.darkenColor(colorPrimary));
    }

    public interface FolderSelectCallback {
        void onFolderSelection(File folder);

        void onFileSelection(File file);
    }
}