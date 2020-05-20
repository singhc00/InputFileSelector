package com.singhc00.cordova.plugin;

import android.annotation.TargetApi;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

import com.sap.mp.cordova.plugins.attachmentViewer.AttachmentViewer;
import com.sap.mp.cordova.plugins.attachmentViewer.ExtendedFilePickerChromeClient;
import com.sap.mp.cordova.plugins.attachmentViewer.FileProviderWithWorkaround;

import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PermissionHelper;
import org.apache.cordova.engine.SystemWebView;
import org.apache.cordova.engine.SystemWebViewEngine;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class InputFileSelector extends CordovaPlugin {
    private ExtendedWebChromeClient extendedWebChromeClient;
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {

        super.initialize(cordova, webView);

        this.setFileChooser(webView);
    }

    public void setFileChooser(CordovaWebView webView) {
        // Get the instance of the Extended WebChrome Client
        if (webView.getEngine() instanceof SystemWebViewEngine) {
            SystemWebViewEngine systemWebViewEngine = (SystemWebViewEngine)webView.getEngine();
            SystemWebView systemWebView = (SystemWebView)systemWebViewEngine.getView();
            this.extendedWebChromeClient = new ExtendedWebChromeClient(this, systemWebViewEngine);
            systemWebView.setWebChromeClient(this.extendedWebChromeClient);
        }
    }

    /**
     * Called when the permissions have been granted or denied. Used in the WebChromeClient extension
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    public void onRequestPermissionResult(int requestCode, String[] permissions, int[] grantResults) {
        this.extendedWebChromeClient.onRequestPermissionResult(requestCode, permissions, grantResults);
    }

}