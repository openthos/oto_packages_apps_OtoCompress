package com.openthos.compress;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.openthos.compress.bean.CompressedFileBean;
import com.openthos.compress.common.CommonUtils;
import com.openthos.compress.common.OnDoubleClickListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by wm on 17-9-29.
 */

class ArchiveBrowserListAdapter extends BaseAdapter {

    private Context mContext;
    private ArchiveBrowserContract.IPresenter mPresenter;
    private final int TYPE_DIR = 0;
    private final int TYPE_FILE = 1;
    private Map<String, Set<String>> mDirsInfoMap;
    private Map<String, List<CompressedFileBean>> mFilesInfoMap;
    private List<CompressedFileBean> mFilesList;
    private List<String> mDirsList, mDirsTrack;
    private List<Integer> mSelectedList;
    private int mTrackIndex;
    private View mSelectedView;
    private String mSelectedFileName;

    ArchiveBrowserListAdapter(ArchiveBrowserContract.IPresenter iPresenter, Context context,
                              Map<String, Set<String>> dirsInfoMap,
                              Map<String, List<CompressedFileBean>> filesInfoMap) {
        mPresenter = iPresenter;
        mContext = context;
        mDirsInfoMap = dirsInfoMap;
        mFilesInfoMap = filesInfoMap;
        mDirsList = new ArrayList<>();
        mFilesList = new ArrayList<>();
        mDirsTrack = new ArrayList<>();
        mSelectedList = new ArrayList<>();
        initDirsTrack();
        refreshShownList(mDirsTrack.get(mTrackIndex));
    }

    private void initDirsTrack() {
        mTrackIndex = 0;
        mDirsTrack.clear();
        mDirsTrack.add(mTrackIndex, "/");
    }

    private void refreshShownList(String dirKey) {
        mDirsList.clear();
        if (!"/".equals(dirKey)) {
            mDirsList.add("..");
        }
        Set<String> tempSet = mDirsInfoMap.get(dirKey);
        if (tempSet != null) {
            mDirsList.addAll(tempSet);
        }
        mFilesList.clear();
        List<CompressedFileBean> tempList = mFilesInfoMap.get(dirKey);
        if (tempList != null) {
            mFilesList.addAll(tempList);
        }
        notifyDataSetChanged();
        if ("/".equals(mDirsTrack.get(mTrackIndex))) {
            mPresenter.updateUIData(dirKey, mDirsList.size(), mFilesList.size());
        } else {
            mPresenter.updateUIData(dirKey, mDirsList.size() - 1, mFilesList.size());
        }
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getItemViewType(int position) {
        if (getItem(position) instanceof CompressedFileBean) {
            return TYPE_FILE;
        } else if (getItem(position) instanceof String) {
            return TYPE_DIR;
        }
        return -1;
    }

    @Override
    public int getCount() {
        return mDirsList.size() + mFilesList.size();
    }

    @Override
    public Object getItem(int i) {
        if (i < mDirsList.size()) {
            return mDirsList.get(i);
        } else {
            return mFilesList.get(i - mDirsList.size());
        }
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(final int i, View view, ViewGroup viewGroup) {
        final ViewHolder holder;
        if (view == null) {
            view = LayoutInflater.from(mContext)
                    .inflate(R.layout.item_file_browser, viewGroup, false);
            holder = new ViewHolder(view);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }
        if (mSelectedList.contains(i)) {
            view.setBackground(mContext.getDrawable(R.color.gray_light));
            mSelectedView = view;
        } else {
            view.setBackground(mContext.getDrawable(R.color.white));
        }
        if (getItemViewType(i) == TYPE_DIR) {
            final String dirStr = mDirsList.get(i);
            holder.tvName.setText(dirStr);
            if ("/".equals(mDirsTrack.get(mTrackIndex)) || i != 0) {
                holder.tvType.setText(mContext.getResources().getString(R.string.folder));
            }
            holder.imgIcon.setImageResource(R.mipmap.folder);
            view.setOnTouchListener(new OnDoubleClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (!mSelectedList.contains(i)) {
                                mSelectedList.clear();
                                mSelectedList.add(i);
                                view.setBackground(mContext.getDrawable(R.color.gray_light));
                                if (mSelectedView != null) {
                                    mSelectedView.setBackground(mContext.getDrawable(R.color.white));
                                }
                                mSelectedView = view;
                            }
                            mSelectedFileName = dirStr;
                            mPresenter.clearFocus();
                        }
                    },
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (!"/".equals(mDirsTrack.get(mTrackIndex)) && i == 0) {
                                toParentDir();
                                return;
                            }
                            if (mTrackIndex == 0) {
                                initDirsTrack();
                            }
                            mTrackIndex++;
                            if (mTrackIndex < mDirsTrack.size() && !mDirsTrack.get(mTrackIndex - 1)
                                    .endsWith(dirStr)) {
                                for (int i = mTrackIndex; i < mDirsTrack.size(); i++) {
                                    mDirsTrack.remove(mTrackIndex);
                                }
                            }
                            if ("/".equals(mDirsTrack.get(mTrackIndex - 1))) {
                                mDirsTrack.add(mTrackIndex, "/" + dirStr);
                            } else {
                                mDirsTrack.add(mTrackIndex, mDirsTrack.get(mTrackIndex - 1) + "/"
                                        + dirStr);
                            }
                            refreshShownList(mDirsTrack.get(mTrackIndex));
                            mSelectedList.clear();
                            mSelectedView = null;
                            mSelectedFileName = "";
                        }
                    }));
        } else {
            final CompressedFileBean bean = (CompressedFileBean) getItem(i);
            if (!TextUtils.isEmpty(bean.getSuffix())) {
                holder.tvName.setText(bean.getFileName() + "." + bean.getSuffix());
                holder.tvType.setText(bean.getSuffix());
            } else {
                holder.tvName.setText(bean.getFileName());
                holder.tvType.setText(mContext.getString(R.string.file));
            }
            String size = CommonUtils.sizeUnitFormat(mContext, bean.getOriginalSize());
            holder.tvSize.setText(size);
            String size2 = CommonUtils.sizeUnitFormat(mContext, bean.getCompressedSize());
            holder.tvCompressedSize.setText(size2);
            if (bean.getCompressedSize() == 0) {
                holder.tvCompressedSize.setText("");
            }
            holder.tvModifiedTime.setText(bean.getModifiedTime());
            holder.imgIcon.setImageResource(R.mipmap.file);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (!mSelectedList.contains(i)) {
                        mSelectedList.clear();
                        mSelectedList.add(i);
                        view.setBackground(mContext.getDrawable(R.color.gray_light));
                        if (mSelectedView != null) {
                            mSelectedView.setBackground(mContext.getDrawable(R.color.white));
                        }
                        mSelectedView = view;
                    }
                    mSelectedFileName = holder.tvName.getText().toString();
                    mPresenter.clearFocus();
                }
            });
        }
        return view;
    }

    private class ViewHolder {
        TextView tvName, tvSize, tvCompressedSize, tvModifiedTime, tvType;
        ImageView imgIcon;

        ViewHolder(View view) {
            tvName = (TextView) view.findViewById(R.id.tv_name);
            tvSize = (TextView) view.findViewById(R.id.tv_size);
            tvCompressedSize = (TextView) view.findViewById(R.id.tv_compressed_size);
            tvType = (TextView) view.findViewById(R.id.tv_type);
            tvModifiedTime = (TextView) view.findViewById(R.id.tv_modified_time);
            imgIcon = (ImageView) view.findViewById(R.id.img_icon);
        }
    }

    public void searchByDir(String dirPath) {
        if (!dirPath.equals(mDirsTrack.get(mTrackIndex))) {
            mTrackIndex++;
            mDirsTrack.add(mTrackIndex, dirPath);
            refreshShownList(dirPath);
        }
    }

    public void refreshDataList() {
        refreshShownList(mDirsTrack.get(mTrackIndex));
    }

    public void goHome() {
        mTrackIndex = 0;
        refreshShownList(mDirsTrack.get(mTrackIndex));
    }

    public boolean toParentDir() {
        if (mTrackIndex != 0) {
            String path = mDirsTrack.get(mTrackIndex);
            int index = path.lastIndexOf("/");
            path = path.substring(0, index);
            if ("".equals(path)) {
                path = "/";
            }
            mTrackIndex++;
            mDirsTrack.add(mTrackIndex, path);
            refreshShownList(path);
            return true;
        } else {
            return false;
        }
    }

    public boolean toLastDir() {
        if (mTrackIndex == 0) {
            return false;
        } else {
            mTrackIndex--;
            refreshShownList(mDirsTrack.get(mTrackIndex));
            return true;
        }
    }

    public boolean toNextDir() {
        if (mTrackIndex == mDirsTrack.size() - 1 ||
                TextUtils.isEmpty(mDirsTrack.get(mTrackIndex + 1))) {
            return false;
        } else {
            mTrackIndex++;
            refreshShownList(mDirsTrack.get(mTrackIndex));
            return true;
        }
    }

    public String getSelectedFileName() {
        return mSelectedFileName;
    }
}
