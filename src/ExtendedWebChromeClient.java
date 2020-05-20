package com.singhc00.cordova.plugin;

//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

import android.annotation.TargetApi;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import android.webkit.ValueCallback;
import android.webkit.WebView;
import android.webkit.WebChromeClient.FileChooserParams;

import com.sap.mp.cordova.plugins.attachmentViewer.AttachmentViewer;
import com.sap.mp.cordova.plugins.attachmentViewer.FileProviderWithWorkaround;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PermissionHelper;
import org.apache.cordova.engine.SystemWebChromeClient;
import org.apache.cordova.engine.SystemWebViewEngine;

public class ExtendedWebChromeClient extends SystemWebChromeClient {
    private static final int FILECHOOSER_RESULTCODE = 6174;
    private CordovaPlugin plugin;
    private File attachmentDir;
    private boolean permissionRequested = false;
    ExtendedWebChromeClient.PermissionRequestListener permissionRequestListener = null;
    File photoFile = null;

    public ExtendedWebChromeClient(CordovaPlugin plugin, SystemWebViewEngine parentEngine) {
        super(parentEngine);
        this.plugin = plugin;
        this.attachmentDir = new File(plugin.cordova.getActivity().getApplicationContext().getCacheDir(), "filepicker");
        plugin.cordova.getThreadPool().execute(new Runnable() {
            public void run() {
                File[] files = com.singhc00.cordova.plugin.ExtendedWebChromeClient.this.attachmentDir.listFiles();
                if (files != null) {
                    File[] var2 = files;
                    int var3 = files.length;

                    for(int var4 = 0; var4 < var3; ++var4) {
                        File file = var2[var4];
                        file.delete();
                    }
                }

            }
        });
    }

    public void onRequestPermissionResult(int requestCode, String[] permissions, int[] grantResults) {
        if (this.permissionRequestListener != null) {
            this.permissionRequestListener.onRequestPermissionResult(requestCode, permissions, grantResults);
            this.permissionRequestListener = null;
        }

    }

    @TargetApi(21)
    public boolean onShowFileChooser(final WebView webView, final ValueCallback<Uri[]> filePathsCallback, final FileChooserParams fileChooserParams) {
        if (!this.permissionRequested && !PermissionHelper.hasPermission(this.plugin, "android.permission.CAMERA")) {
            this.permissionRequested = true;
            this.permissionRequestListener = new ExtendedWebChromeClient.PermissionRequestListener() {
                public void onRequestPermissionResult(int requestCode, String[] permissions, int[] grantResults) {
                    ExtendedWebChromeClient.this.showFileChooser(webView, filePathsCallback, fileChooserParams);
                }
            };
            PermissionHelper.requestPermission(this.plugin, 0, "android.permission.CAMERA");
        } else {
            this.showFileChooser(webView, filePathsCallback, fileChooserParams);
        }

        return true;
    }

    @TargetApi(21)
    private void showFileChooser(WebView webView, final ValueCallback<Uri[]> filePathsCallback, FileChooserParams fileChooserParams) {
        Intent takePictureIntent = null;
        Intent videoCameraIntent = null;
        if (PermissionHelper.hasPermission(this.plugin, "android.permission.CAMERA")) {
            takePictureIntent = new Intent("android.media.action.IMAGE_CAPTURE");
            if (takePictureIntent.resolveActivity(this.plugin.cordova.getActivity().getPackageManager()) != null) {
                this.photoFile = this.createImageFile();
                if (this.photoFile != null) {
                    Uri uri = FileProviderWithWorkaround.getUriForFile(this.plugin.cordova.getActivity(), this.plugin.cordova.getContext().getPackageName() + ".KapselAttachmentViewer", this.photoFile);
                    takePictureIntent.putExtra("output", uri);
                    takePictureIntent.addFlags(2);
                } else {
                    //AttachmentViewer.clientLogger.logWarning("ExtendedWebChromeClient could not create image file.");
                    takePictureIntent = null;
                }
            }
            /** Create the video content  */
            videoCameraIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        }

        Intent contentSelectionIntent = new Intent("android.intent.action.GET_CONTENT");
        contentSelectionIntent.addCategory("android.intent.category.OPENABLE");
        contentSelectionIntent.setType("*/*");
        if (fileChooserParams.getMode() == 1) {
            contentSelectionIntent.putExtra("android.intent.extra.ALLOW_MULTIPLE", true);
        }


        Intent[] intentArray;
        // If both camera and video intent available
        if (takePictureIntent != null && videoCameraIntent != null) {
            intentArray = new Intent[]{takePictureIntent, videoCameraIntent};
        } else if(takePictureIntent == null && videoCameraIntent != null) {
            // If only video camera intent available
            intentArray = new Intent[]{videoCameraIntent};
        } else if(videoCameraIntent == null && takePictureIntent != null) {
            // If only image camera intent available
            intentArray = new Intent[]{takePictureIntent};
        }
        else {
            intentArray = new Intent[0];
        }


        Intent chooserIntent = new Intent("android.intent.action.CHOOSER");
        chooserIntent.putExtra("android.intent.extra.INTENT", contentSelectionIntent);
        chooserIntent.putExtra("android.intent.extra.INITIAL_INTENTS", intentArray);

        try {
            this.plugin.cordova.startActivityForResult(new CordovaPlugin() {
                public void onActivityResult(int requestCode, int resultCode, Intent intent) {
                    Uri[] result = FileChooserParams.parseResult(resultCode, intent);
                    if (com.singhc00.cordova.plugin.ExtendedWebChromeClient.this.photoFile != null) {
                        if (com.singhc00.cordova.plugin.ExtendedWebChromeClient.this.photoFile.length() > 0L && result == null) {
                            result = new Uri[]{Uri.fromFile(com.singhc00.cordova.plugin.ExtendedWebChromeClient.this.photoFile)};
                        } else {
                            com.singhc00.cordova.plugin.ExtendedWebChromeClient.this.photoFile.delete();
                        }

                        com.singhc00.cordova.plugin.ExtendedWebChromeClient.this.photoFile = null;
                    }

                    filePathsCallback.onReceiveValue(result);
                }
            }, chooserIntent, 6174);
        } catch (ActivityNotFoundException var9) {
            filePathsCallback.onReceiveValue(null);
        }

    }

    private File createImageFile() {
        String timeStamp = (new SimpleDateFormat("yyyyMMdd_HHmmss")).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";

        try {
            if (!this.attachmentDir.exists()) {
                this.attachmentDir.mkdirs();
            }

            File file = File.createTempFile(imageFileName, ".jpg", this.attachmentDir);
            return file;
        } catch (IOException var4) {
            return null;
        }
    }

    interface PermissionRequestListener {
        void onRequestPermissionResult(int var1, String[] var2, int[] var3);
    }
}
