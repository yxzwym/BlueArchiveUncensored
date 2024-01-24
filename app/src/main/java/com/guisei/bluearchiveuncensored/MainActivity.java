package com.guisei.bluearchiveuncensored;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.UriPermission;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.documentfile.provider.DocumentFile;

import com.guisei.bluearchiveuncensored.config.PackEnum;
import com.guisei.bluearchiveuncensored.dialog.ChannelDialog;
import com.guisei.bluearchiveuncensored.util.PackUtil;

import java.io.File;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {

    private static final int REQUEST_ANDROID_DATA = 101;
    private static final int REQUEST_STORAGE_PERMISSION = 102;
    private static final String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    /**
     * 选中的包名临时保存
     */
    private String mPackName;

    /**
     * Android/data/包名 文件夹
     */
    private DocumentFile mDocumentFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.btn_main).setOnClickListener(v -> showAppDialog());
        refreshImage(R.drawable.pic1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_ANDROID_DATA:
                if (!hasFolderPermission()) {
                    if (resultCode == Activity.RESULT_OK) {
                        if (data != null && data.getData() != null) {
                            mDocumentFile = DocumentFile.fromTreeUri(this, data.getData());
                            try {
                                getContentResolver().takePersistableUriPermission(data.getData(), Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                            } catch (Exception e) {
                                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                }
                if (hasFolderPermission()) {
                    uncensored();
                }
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_STORAGE_PERMISSION:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    uncensored();
                    return;
                }
                break;
        }
    }

    /**
     * 弹出所有渠道服选择的弹窗
     */
    private void showAppDialog() {
        mPackName = null;
        mDocumentFile = null;

        List<PackageInfo> packList = PackUtil.getPacksBySystem(this);
        List<PackageInfo> filterList = new ArrayList<>();
        for (PackageInfo packInfo : packList) {
            for (PackEnum packEnum : PackEnum.values()) {
                if (packInfo.packageName != null && packEnum.getPackName() != null && packInfo.packageName.equals(packEnum.getPackName())) {
                    filterList.add(packInfo);
                    break;
                }
            }
        }
        if (filterList.size() > 0) {
            new ChannelDialog(this, filterList, packName -> {
                mPackName = packName;
                uncensored();
            }).show();
        } else {
            Toast.makeText(this, "没有安装蔚蓝档案", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 反和谐
     */
    private void uncensored() {
        // 权限判断
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // >= Android 11，获取 Android/data 目录的权限
            if (!hasFolderPermission()) {
                new AlertDialog.Builder(this)
                        .setTitle("温馨提示")
                        .setMessage("点击“确认”后将会打开系统文件管理器\n需要手动点击最底部的“使用此文件夹”\n然后点击“允许”即可")
                        .setPositiveButton("确认", (dialogInterface, i) -> {
                            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
                            intent.putExtra("android.provider.extra.SHOW_ADVANCED", true);
                            intent.putExtra("android.content.extra.SHOW_ADVANCED", true);
                            intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, getFolderUri(true));
                            intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                            startActivityForResult(intent, REQUEST_ANDROID_DATA);
                        }).show();
                return;
            } else if (mDocumentFile == null) {
                Uri folderUri = getFolderUri(false);
                if (folderUri != null) {
                    mDocumentFile = DocumentFile.fromTreeUri(this, folderUri);
                }
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // >= Android 6.0，动态申请权限
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(PERMISSIONS_STORAGE, REQUEST_STORAGE_PERMISSION);
                return;
            }
        }
        // < Android 6.0，不需要权限
        if (mDocumentFile == null) {
            File channelFile = getChannelFile();
            if (channelFile.exists()) {
                mDocumentFile = DocumentFile.fromFile(channelFile);
            }
        }
        if (mDocumentFile != null) {
            // 反和谐
            new Thread(() -> {
                DocumentFile filesFolder = mDocumentFile.findFile("files");
                if (filesFolder == null) {
                    filesFolder = mDocumentFile.createDirectory("files");
                }
                if (filesFolder != null && filesFolder.isDirectory()) {
                    DocumentFile localization = filesFolder.findFile("LocalizeConfig.txt");
                    try {
                        if (localization != null) {
                            // 删除已存在的文件
                            localization.delete();
                        }
                        localization = filesFolder.createFile("application/txt", "LocalizeConfig.txt");
                        OutputStream outputStream = getContentResolver().openOutputStream(localization.getUri());
                        String content = "Env=dev\nIsLocalize=false\nResUrls=http://mx.jvav.net.cn/asdf;http://mx.jvav.net.cn/asdf;http://mx.jvav.net.cn/asdf";
                        outputStream.write(content.getBytes());
                        outputStream.flush();
                        outputStream.close();
                        runOnUiThread(() -> {
                            refreshImage(R.drawable.pic2);
                            new AlertDialog.Builder(this)
                                    .setTitle("恭喜")
                                    .setMessage("反和谐成功，重启游戏即可生效\n这个APP可以卸载掉了")
                                    .setPositiveButton("知道了", null)
                                    .show();
                        });
                    } catch (Exception e) {
                        runOnUiThread(() -> {
                            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
                    }
                }
            }).start();
        }
    }

    /**
     * 获取 Android/data 文件夹 Uri
     *
     * @param isRequestPermission 是否是申请权限
     * @return Uri
     */
    private Uri getFolderUri(boolean isRequestPermission) {
        if (mPackName != null) {
            if (isRequestPermission) {
                return DocumentsContract.buildDocumentUri("com.android.externalstorage.documents", "primary:Android/data/" + mPackName);
            } else {
                return DocumentsContract.buildTreeDocumentUri("com.android.externalstorage.documents", "primary:Android/data/" + mPackName);
            }
        }
        return null;
    }

    /**
     * 是否有 Android/data 文件夹权限
     *
     * @return bool
     */
    private boolean hasFolderPermission() {
        Uri folderUri = getFolderUri(false);
        if (folderUri != null) {
            List<UriPermission> permissionList = getContentResolver().getPersistedUriPermissions();
            for (UriPermission uriPermission : permissionList) {
                if (uriPermission.getUri().equals(folderUri) && (uriPermission.isReadPermission() || uriPermission.isWritePermission())) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 获取 Android/data/包名 文件夹路径
     *
     * @return 只有 Android 11 以下才需要这个
     */
    private File getChannelFile() {
        return new File("/sdcard/Android/data/" + mPackName);
    }

    /**
     * 刷新图片
     *
     * @param drawableId 资源 id
     */
    private void refreshImage(int drawableId) {
        ImageView img = findViewById(R.id.img);
        img.setImageResource(drawableId);
    }
}