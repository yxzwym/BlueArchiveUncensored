package com.guisei.bluearchiveuncensored.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.guisei.bluearchiveuncensored.R;
import com.guisei.bluearchiveuncensored.config.PackEnum;

import java.util.List;

/**
 * 渠道选择弹窗
 */
public class ChannelDialog {

    private Context mContext;
    private List<PackageInfo> mList;

    public interface OnListener {
        void onSelect(String packName);
    }

    private OnListener mOnListener;

    public ChannelDialog(Context context, List<PackageInfo> list, OnListener listener) {
        mContext = context;
        mList = list;
        mOnListener = listener;
    }

    public void show() {
        final Dialog dialog = new Dialog(mContext);
        dialog.setContentView(R.layout.dialog_channel);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
        ListView listView = dialog.findViewById(R.id.list_view);
        ArrayAdapter arrayAdapter = new ChannelAdapter(mContext, mList);
        listView.setAdapter(arrayAdapter);
        listView.setOnItemClickListener((adapterView, view, which, l) -> {
            if (mOnListener != null) {
                mOnListener.onSelect(mList.get(which).packageName);
            }
            dialog.dismiss();
        });
        dialog.show();
    }

    public class ChannelAdapter extends ArrayAdapter<PackageInfo> {
        public ChannelAdapter(Context context, List<PackageInfo> list) {
            super(context, 0, list);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = LayoutInflater.from(getContext()).inflate(R.layout.item_channel, parent, false);

            ImageView iv_app = view.findViewById(R.id.iv_app);
            TextView tv_app = view.findViewById(R.id.tv_app);
            TextView tv_pack_name = view.findViewById(R.id.tv_pack_name);

            PackageInfo packInfo = (PackageInfo) getItem(position);
            if (packInfo != null) {
                // 图标
                iv_app.setImageDrawable(packInfo.applicationInfo.loadIcon(getContext().getPackageManager()));
                // 渠道名称
                for (PackEnum packEnum : PackEnum.values()) {
                    if (packEnum.getPackName().equals(packInfo.packageName)) {
                        tv_app.setText(packEnum.getChannel());
                    }
                }
                // 包名
                tv_pack_name.setText(packInfo.packageName);
            }

            return view;
        }
    }
}
