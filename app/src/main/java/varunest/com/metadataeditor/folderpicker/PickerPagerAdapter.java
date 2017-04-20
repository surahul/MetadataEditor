package varunest.com.metadataeditor.folderpicker;

import android.content.Context;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.SparseArray;
import android.view.ViewGroup;

import java.util.List;

import android_file.io.storage.StorageItem;

public class PickerPagerAdapter extends FragmentStatePagerAdapter {

    private Context context;
    private SparseArray<Fragment> registeredFragments = new SparseArray<>();
    private List<StorageItem> storageItems;
    private boolean isEnableFilePickMode = false;

    public PickerPagerAdapter(FragmentManager manager, Context context, List<StorageItem> storageItems, boolean isEnableFilePickMode){
        super(manager);
        this.context = context;
        this.storageItems = storageItems;
        this.isEnableFilePickMode = isEnableFilePickMode;
    }

    @Override
    public int getCount() {
        return storageItems.size();
    }

    @Override
    public Fragment getItem(int position) {
        return FolderPickerPage.newInstance(storageItems.get(position), isEnableFilePickMode);
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return storageItems.get(position).getDisplayName();
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        Fragment fragment = (Fragment) super.instantiateItem(container, position);
        registeredFragments.put(position, fragment);
        return fragment;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        registeredFragments.remove(position);
        super.destroyItem(container, position, object);
    }

    public Fragment getRegisteredFragment(int position) {
        return registeredFragments.get(position);
    }

    @Override
    public void restoreState(Parcelable state, ClassLoader loader) {
        super.restoreState(state, loader);
    }
}
