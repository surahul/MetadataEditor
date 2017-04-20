package varunest.com.metadataeditor.folderpicker;

import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.os.Build;
import android.support.design.widget.TabLayout;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import java.util.ArrayList;
import java.util.List;

import android_file.io.File;
import android_file.io.storage.StorageItem;
import varunest.com.metadataeditor.ATHUtil;
import varunest.com.metadataeditor.GeneralUtils;
import varunest.com.metadataeditor.R;
import varunest.com.metadataeditor.TintHelper;

class FolderPickerDialogHelper implements View.OnClickListener, FolderStateManager.Listener {
    private DialogFragment dialogFragment;
    private FolderPickerDialog.FolderSelectCallback folderSelectionCallback;
    private ViewPager pager;
    private PickerPagerAdapter adapter;
    private TabLayout tabLayout;
    private TextView singleStorageItemTitle;
    private TextView sizeTextView;
    private View cancel, choose;

    private View folderUpButton;
    private HorizontalScrollView currentPathIndicatorHSV;
    private LinearLayout hsvHouse;
    private FolderPickerConfig config;


    private AppCompatActivity context;
    private List<StorageItem> storageItems;
    private ArrayList<FolderStateManager> folderStateManagers;

    private int toolbarHeight;
    private int tabsHeight;

    FolderPickerDialogHelper(FolderPickerConfig config, View dialogRootView, DialogFragment dialogFragment, AppCompatActivity context) {
        this.context = context;
        this.dialogFragment = dialogFragment;
        this.config = config;

        initValues();
        initStateManagers(config);
        pager = (ViewPager) dialogRootView.findViewById(R.id.pager);
        pager.addOnPageChangeListener(pageChangeListener);
        createAdapter();
        pager.setAdapter(adapter);
        tabLayout = (TabLayout) dialogRootView.findViewById(R.id.tab_layout);
        singleStorageItemTitle = (TextView) dialogRootView.findViewById(R.id.single_storage_item_text_view);

        if (storageItems.size() > 1) {
            tabLayout.setupWithViewPager(pager);
            tabLayout.setVisibility(View.VISIBLE);
            singleStorageItemTitle.setVisibility(View.GONE);
        } else if (storageItems.size() == 1) {
            tabLayout.setVisibility(View.GONE);
            singleStorageItemTitle.setVisibility(View.VISIBLE);
            singleStorageItemTitle.setText(storageItems.get(0).getDisplayName());
        }


        currentPathIndicatorHSV = (HorizontalScrollView) dialogRootView.findViewById(R.id.current_folder_hsv);
        hsvHouse = (LinearLayout) dialogRootView.findViewById(R.id.current_directory_house);
        folderUpButton = dialogRootView.findViewById(R.id.up_button);
        folderUpButton.setOnClickListener(this);

        sizeTextView = (TextView) dialogRootView.findViewById(R.id.size_text_view);
        cancel = dialogRootView.findViewById(R.id.cancel);
        choose = dialogRootView.findViewById(R.id.choose);
        if (config.isEnableFilePickmode()) {
            choose.setVisibility(View.GONE);
        } else {
            choose.setOnClickListener(this);
        }
        cancel.setOnClickListener(this);

        setCurrentPage(config);
        updateSize();

    }

    private void initValues() {
        TypedValue typedValue = new TypedValue();
        int[] toolbarSizeAttr = new int[]{R.attr.actionBarSize};
        int indexOfAttrTextSize = 0;
        TypedArray a = context.obtainStyledAttributes(typedValue.data, toolbarSizeAttr);
        int toolbarSize = a.getDimensionPixelSize(indexOfAttrTextSize, -1);
        a.recycle();
        this.toolbarHeight = toolbarSize;
        this.tabsHeight = (int) context.getResources().getDimension(R.dimen.folder_picker_tabs_size);
    }


    private void initStateManagers(FolderPickerConfig folderPickerConfig) {
        folderStateManagers = new ArrayList<>();
        storageItems = File.getStorageItems("External Storage", "Internal Storage");
        for (StorageItem storageItem : storageItems) {
            FolderStateManager folderStateManager = new FolderStateManager(this, folderPickerConfig, storageItem);
            folderStateManagers.add(folderStateManager);
        }
    }

    private void setCurrentPage(FolderPickerConfig config) {
        for (int i = 0; i < folderStateManagers.size(); i++) {
            if (config.getDefaultDirectory().getAbsolutePath().startsWith(folderStateManagers.get(i).getStorageItem().getFile().getAbsolutePath())) {
                pager.setCurrentItem(i);
                break;
            }
        }
    }

    private ViewPager.OnPageChangeListener pageChangeListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {

        }

        @Override
        public void onPageScrollStateChanged(int state) {
            if (state == ViewPager.SCROLL_STATE_IDLE) {
                updateFolderHSVIndicator(folderStateManagers.get(pager.getCurrentItem()));
                updateSize();
            }
        }
    };


    private void updateSize() {
        sizeTextView.setText("");
        try {
            File file = folderStateManagers.get(pager.getCurrentItem()).getStorageItem().getFile();
            long totalSpace = file.getTotalSpace();
            long freeSpace = file.getFreeSpace();
            String totalSpaceString = GeneralUtils.formatSize(totalSpace, "--");
            String freeSpaceString = GeneralUtils.formatSize(freeSpace, "--");
            if (!totalSpaceString.equals("--") && !freeSpaceString.equals("--")) {
                String finalString = freeSpaceString + " " + "FREE" + " / " + totalSpaceString;
                sizeTextView.setText(finalString);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void createAdapter() {
        adapter = new PickerPagerAdapter(dialogFragment.getChildFragmentManager(), context, storageItems, config.isEnableFilePickmode());
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.up_button:
                if (folderStateManagers.size() == 0)
                    initStateManagers(config);
                if (folderStateManagers.size() > 0) {
                    folderStateManagers.get(pager.getCurrentItem()).moveUpFolder();
                }
                break;
            case R.id.cancel:
                dialogFragment.dismiss();
                break;
            case R.id.choose:
                onChoose();
                break;
        }
    }

    private void onChoose() {
        if (folderSelectionCallback != null) {
            if (folderStateManagers.size() == 0) {
                initStateManagers(config);
            }
            if (folderStateManagers.size() > 0) {
                File folder = folderStateManagers.get(pager.getCurrentItem()).getSelectedFolder();
                if (folder.isWritableNormallyOrBySAF() || (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)) {
                    folderSelectionCallback.onFolderSelection(folder);
                    try {
                        dialogFragment.dismiss();
                    } catch (Exception ignored) {
                    }
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        Toast.makeText(context, "Need to give SAF Permissionn", Toast.LENGTH_LONG).show();
                    }
                }
            } else {
                if (dialogFragment != null)
                    dialogFragment.dismiss();
            }

        } else {
            try {
                dialogFragment.dismiss();
            } catch (Exception ignored) {
            }
        }
    }


    public boolean onBackPressed() {
        FolderStateManager folderStateManager = getCurrentFolderStateManager();
        if (folderStateManager != null)
            return folderStateManager.moveUpFolder();
        return false;
    }

    private FolderStateManager getCurrentFolderStateManager() {
        if (folderStateManagers == null || folderStateManagers.size() == 0)
            return null;
        return folderStateManagers.get(pager.getCurrentItem());
    }

    public void onFolderSelectedFromPage(File file, StorageItem storageItem) {
        for (int i = 0; i < storageItems.size(); i++) {
            if (storageItems.get(i).getFile().getAbsolutePath().equals(storageItem.getFile().getAbsolutePath())) {
                folderStateManagers.get(i).switchToFolder(file);
                break;
            }
        }
    }

    public void onFileSelectedFromPage(File file, StorageItem storageItem) {
        for (int i = 0; i < storageItems.size(); i++) {
            if (storageItems.get(i).getFile().getAbsolutePath().equals(storageItem.getFile().getAbsolutePath())) {
                folderSelectionCallback.onFileSelection(file);
                if (dialogFragment != null) {
                    dialogFragment.dismiss();
                    dialogFragment = null;
                }
                break;
            }
        }
    }

    public void onNewFolderFromPage(final StorageItem storageItem) {


        for (int i = 0; i < storageItems.size(); i++) {
            if (storageItems.get(i).getFile().getAbsolutePath().equals(storageItem.getFile().getAbsolutePath())) {
                FolderStateManager folderStateManager = folderStateManagers.get(i);
                if (folderStateManager.getSelectedFolder().isWritableNormallyOrBySAF() || (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)) {
                    showNewFolderDialog(folderStateManager);
                } else {
                    Toast.makeText(context, "Need to give SAF Permission", Toast.LENGTH_LONG).show();
                }


                break;
            }
        }
    }

    public void update(StorageItem storageItem) {

        for (int i = 0; i < folderStateManagers.size(); i++) {
            if (folderStateManagers.get(i).getStorageItem().getFile().getAbsolutePath().equals(storageItem.getFile().getAbsolutePath())) {
                folderStateManagers.get(i).update();
                break;
            }
        }
    }

    @Override
    public void folderUpdated(File currentFolder, List<File> folderContents, boolean canMoveUp, StorageItem storageItem) {
        if (folderStateManagers == null)
            return;

        int index = -1;

        for (int i = 0; i < folderStateManagers.size(); i++) {
            if (folderStateManagers.get(i).getStorageItem().equals(storageItem)) {
                index = i;
                break;
            }
        }

        if (index >= 0) {

            Fragment page = adapter.getRegisteredFragment(index);
            if (page != null) {
                ((FolderPickerPage) page).update(folderContents);
            }


            int currentItem = pager.getCurrentItem();
            updateFolderHSVIndicator(folderStateManagers.get(currentItem));
        }

    }

    public int getTopBarSize() {
        return tabsHeight + toolbarHeight;
    }


    private void disableBackFolderIcon(boolean flag) {
        if (flag) {
            folderUpButton.setAlpha(.2f);
        } else {
            folderUpButton.setAlpha(1f);
        }
    }

    private void updateFolderHSVIndicator(final FolderStateManager folderStateManager) {
        File currentFolder = new File(folderStateManager.getSelectedFolder().getAbsolutePath());
        final ArrayList<File> linkedFolders = new ArrayList<>();
        while (folderStateManager.canMoveUp(currentFolder)) {
            linkedFolders.add(0, currentFolder);
            currentFolder = currentFolder.getParentFile();
        }

        hsvHouse.removeAllViews();

        View segmentView = LayoutInflater.from(context).inflate(R.layout.item_storate_root_segment, hsvHouse, false);
        ImageView indicator = (ImageView) segmentView.findViewById(R.id.indicator);
        indicator.setImageResource(folderStateManager.getStorageItem().getStorageType() == StorageItem.TYPE_EXTERNAL ? R.drawable.ic_sd_storage : R.drawable.ic_hard_disk);
        TintHelper.setTint(indicator, ATHUtil.resolveColor(context, android.R.attr.textColorPrimaryInverse));
        indicator.setAlpha(linkedFolders.size() == 0 ? 1f : .4f);
        hsvHouse.addView(segmentView);
        segmentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                folderStateManager.switchToFolder(folderStateManager.getStorageItem().getFile());
            }
        });

        for (int i = 0; i < linkedFolders.size(); i++) {
            segmentView = LayoutInflater.from(context).inflate(R.layout.item_directory_segment, hsvHouse, false);
            TintHelper.setTint((ImageView)segmentView.findViewById(R.id.indicator), ATHUtil.resolveColor(context, android.R.attr.textColorPrimaryInverse));
            segmentView.findViewById(R.id.indicator).setAlpha(.4f);
            TextView directoryTextView = (TextView) segmentView.findViewById(R.id.directory_name);
            if (linkedFolders.get(i).getName() == null || linkedFolders.get(i).getName().isEmpty()) {
                directoryTextView.setText("/");
            } else {
                directoryTextView.setText(linkedFolders.get(i).getName());
            }
            if (i == linkedFolders.size() - 1) {
                directoryTextView.setTypeface(null, Typeface.BOLD);
                directoryTextView.setAlpha(1);
            } else {
                directoryTextView.setAlpha(.5f);
            }

            final int finalI = i;
            segmentView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    folderStateManager.switchToFolder(linkedFolders.get(finalI));
                }
            });

            hsvHouse.addView(segmentView);
        }

        hsvHouse.post(new Runnable() {
            @Override
            public void run() {
                currentPathIndicatorHSV.scrollTo(hsvHouse.getMeasuredWidth(), 0);
            }
        });

        if (folderStateManager.canMoveUp(folderStateManager.getSelectedFolder())) {
            disableBackFolderIcon(false);
        } else {
            disableBackFolderIcon(true);
        }
    }

    public TextWatcher getTextWatcher() {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
//                fileSearchHelper.interruptSearch();
//                if (!GeneralUtils.isInvalid(folderQueryEditText.getText().toString())) {
//                    searchProgressBar.setVisibility(View.VISIBLE);
//                    fileSearchHelper.searchFile(folderQueryEditText.getText().toString());
//                } else {
//                    searchProgressBar.setVisibility(View.GONE);
//                    searchedFoldersListAdapter.updateFileList(new ArrayList<File>());
//                    searchedFoldersListAdapter.notifyDataSetChanged();
//                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        };
    }


    private void showNewFolderDialog(final FolderStateManager folderStateManager) {

        final MaterialDialog dialog = new MaterialDialog.Builder(context)
                .title("Create Folder")
                .customView(R.layout.dialog_new_folder, false)
                .positiveText("Confirm")
                .negativeText("Cancel")
                .autoDismiss(false)
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(MaterialDialog dialog, DialogAction which) {
                        dialog.dismiss();
                    }
                })
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(MaterialDialog dialog, DialogAction which) {
                        String folderName = ((EditText) dialog.findViewById(R.id.folder_name)).getText().toString();
                        if (folderName != null && !folderName.isEmpty()) {
                            if (folderStateManager.presentInFolderContents(folderName)) {
                                ((TextInputLayout) dialog.findViewById(R.id.folder_name_text_input_layout)).setError("Directory already exists.");
                            } else {
                                folderStateManager.createAndMoveToFolder(context, folderName);
                                dialog.dismiss();
                            }
                        } else {
                            Toast.makeText(context, "Wrong folder name", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .build();
        dialog.show();


        TextInputEditText inputEditText = (TextInputEditText) dialog.findViewById(R.id.folder_name);
        TextInputLayout textInputLayout = (TextInputLayout) dialog.findViewById(R.id.folder_name_text_input_layout);

        dialog.findViewById(R.id.folder_name).postDelayed(new Runnable() {
            @Override
            public void run() {
                GeneralUtils.showSoftKeyboard(dialog.findViewById(R.id.folder_name));
            }
        }, 200);
    }


    public void setFolderSelectionCallback(FolderPickerDialog.FolderSelectCallback folderSelectionCallback) {
        this.folderSelectionCallback = folderSelectionCallback;
    }

}