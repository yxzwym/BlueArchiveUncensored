package com.guisei.bluearchiveuncensored.util;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.ResolveInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * 包管理工具类
 */
public class PackUtil {

    /**
     * 使用系统方法获取包列表
     *
     * @param context context
     * @return list
     */
    public static List<PackageInfo> getPacksBySystem(Context context) {
        return context.getPackageManager().getInstalledPackages(0);
    }

    /**
     * 使用queryIntentActivities获取包名列表
     * 这种方法获取到的并没有另外两种多，除非是国产手机只回复了部分列表，这个方法能获取全部
     * 而且通过包名获取包信息，又浪费了一次性能
     *
     * @param context context
     * @return list
     */
    public static List<PackageInfo> getPacksByQueryIntent(Context context) {
        List<String> packNameList = new ArrayList<>();
        List<ResolveInfo> resolveInfoList = context.getPackageManager().queryIntentActivities(new Intent(Intent.ACTION_MAIN), 0);
        for (ResolveInfo resolveInfo : resolveInfoList) {
            String packName = resolveInfo.activityInfo.applicationInfo.packageName;
            if (!packNameList.contains(packName)) {
                packNameList.add(packName);
            }
        }
        List<PackageInfo> packageInfoList = new ArrayList<>();
        for (String packName : packNameList) {
            packageInfoList.add(getPackInfo(context, packName));
        }
        return packageInfoList;
    }

    /**
     * 根据包名获取包信息
     *
     * @param context  context
     * @param packName 包名
     * @return packInfo
     */
    public static PackageInfo getPackInfo(Context context, String packName) {
        PackageInfo packInfo = null;
        try {
            packInfo = context.getPackageManager().getPackageInfo(packName, 0);
        } catch (Exception ignored) {
        }
        return packInfo;
    }
}
