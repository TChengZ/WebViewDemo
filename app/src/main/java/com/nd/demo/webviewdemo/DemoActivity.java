package com.nd.demo.webviewdemo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.DownloadListener;
import android.webkit.GeolocationPermissions;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebStorage;
import android.webkit.WebView;
import android.webkit.WebViewClient;


public class DemoActivity extends Activity {

    private WebView mWebView = null;

    private WebViewClient mWebViewClient;

    {
        mWebViewClient = new WebViewClient() {
            // 处理页面导航
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                mWebView.loadUrl(url);
                // 记得消耗掉这个事件。给不知道的朋友再解释一下，
                // Android中返回True的意思就是到此为止吧,事件就会不会冒泡传递了，我们称之为消耗掉

                return true;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
            }
        };
    }

    private WebChromeClient mChromeClient;

    {
        mChromeClient = new WebChromeClient() {

            private View myView = null;
            private CustomViewCallback myCallback = null;

            // 配置权限 （在WebChromeClinet中实现）
            @Override
            public void onGeolocationPermissionsShowPrompt(String origin,
                                                           GeolocationPermissions.Callback callback) {
                callback.invoke(origin, true, false);
                super.onGeolocationPermissionsShowPrompt(origin, callback);
            }

            // 扩充数据库的容量（在WebChromeClinet中实现）
            @Override
            public void onExceededDatabaseQuota(String url,
                                                String databaseIdentifier, long currentQuota,
                                                long estimatedSize, long totalUsedQuota,
                                                WebStorage.QuotaUpdater quotaUpdater) {

                quotaUpdater.updateQuota(estimatedSize * 2);
            }

            // 扩充缓存的容量
            @Override
            public void onReachedMaxAppCacheSize(long spaceNeeded,
                                                 long totalUsedQuota, WebStorage.QuotaUpdater quotaUpdater) {

                quotaUpdater.updateQuota(spaceNeeded * 2);
            }

            // Android 使WebView支持HTML5 Video（全屏）播放的方法
            @Override
            public void onShowCustomView(View view, CustomViewCallback callback) {
                if (myCallback != null) {
                    myCallback.onCustomViewHidden();
                    myCallback = null;
                    return;
                }

                ViewGroup parent = (ViewGroup) mWebView.getParent();
                parent.removeView(mWebView);
                parent.addView(view);
                myView = view;
                myCallback = callback;
                mChromeClient = this;
            }

            @Override
            public void onHideCustomView() {
                if (myView != null) {
                    if (myCallback != null) {
                        myCallback.onCustomViewHidden();
                        myCallback = null;
                    }

                    ViewGroup parent = (ViewGroup) myView.getParent();
                    parent.removeView(myView);
                    parent.addView(mWebView);
                    myView = null;
                }
            }
        };
    }

    private class MyWebViewDownLoadListener implements DownloadListener {

        @Override
        public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype,
                                    long contentLength) {
            Uri uri = Uri.parse(url);
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
        }

    }

    private void initSettings() {

        WebSettings webSettings = mWebView.getSettings();
        // 开启Javascript脚本
        webSettings.setJavaScriptEnabled(true);

        // 启用localStorage 和 essionStorage
        webSettings.setDomStorageEnabled(true);

        // 开启应用程序缓存
        webSettings.setAppCacheEnabled(true);
        String appCacheDir = this.getApplicationContext()
                .getDir("cache", Context.MODE_PRIVATE).getPath();
        webSettings.setAppCachePath(appCacheDir);
        webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);
        webSettings.setAppCacheMaxSize(1024 * 1024 * 10);// 设置缓冲大小，我设的是10M
        webSettings.setAllowFileAccess(true);

        // 启用Webdatabase数据库
        webSettings.setDatabaseEnabled(true);
        String databaseDir = this.getApplicationContext()
                .getDir("database", Context.MODE_PRIVATE).getPath();
        webSettings.setDatabasePath(databaseDir);// 设置数据库路径

        // 启用地理定位
        webSettings.setGeolocationEnabled(true);
        // 设置定位的数据库路径
        webSettings.setGeolocationDatabasePath(databaseDir);

        // 开启插件（对flash的支持）
        webSettings.setPluginState(WebSettings.PluginState.ON);
        webSettings.setRenderPriority(WebSettings.RenderPriority.HIGH);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);

        //提高渲染的优先级
        if(android.os.Build.VERSION.SDK_INT > 10) {
            webSettings.setEnableSmoothTransition(true);
        }

        mWebView.setWebChromeClient(mChromeClient);
        mWebView.setWebViewClient(mWebViewClient);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        mWebView = (WebView) findViewById(R.id.webview);
        mWebView.setDownloadListener(new MyWebViewDownLoadListener());
        //设置webView可以使用Javascript
        mWebView.getSettings().setJavaScriptEnabled(true);
        initSettings();
        if(android.os.Build.VERSION.SDK_INT > 17) {
            mWebView.getSettings().setMediaPlaybackRequiresUserGesture(false);
        }
        play();
    }


    private void play(){//"http://91yong.com/demo/ppt/index.html";
        String url = BuildConfig.web;
        mWebView.loadUrl(url);
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (android.os.Build.VERSION.SDK_INT > 10){
            mWebView.onResume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(android.os.Build.VERSION.SDK_INT > 10) {
            mWebView.onPause();
        }
        if (isFinishing())
        {
            mWebView.loadUrl("about:blank");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
