package com.rayannovin.boostanezarbus;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.os.Handler;
import android.os.Looper;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.browser.customtabs.CustomTabsIntent;

public class MainActivity extends AppCompatActivity {

    private static final String CHANNEL_ERROR_MESSAGE =
            "امکان دسترسی به کانال اطلاع‌رسانی بوستان زر وجود ندارد.";

    private WebView webView;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // اجازه می‌دهد WebView با بریدگی صفحه، نوار وضعیت و نوار ناوبری
        // روی دستگاه‌های مختلف بهتر هماهنگ شود.
        Window window = getWindow();
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        window.setStatusBarColor(Color.TRANSPARENT);
        window.setNavigationBarColor(Color.TRANSPARENT);

        webView = new WebView(this);
        setContentView(webView);

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setAllowFileAccess(true);
        settings.setUseWideViewPort(true);
        settings.setLoadWithOverviewMode(true);
        settings.setBuiltInZoomControls(false);
        settings.setDisplayZoomControls(false);
        settings.setTextZoom(100);
        settings.setDefaultFontSize(16);

        // پل ارتباطی بین جاوااسکریپت (دکمه خروج و دکمه کانال در index.html) و اندروید.
        // در سمت وب با window.AndroidApp.closeApp() / window.AndroidApp.openChannel(url) صدا زده می‌شود.
        webView.addJavascriptInterface(new AppBridge(), "AndroidApp");

        // Loads app/src/main/assets/www/index.html
        webView.loadUrl("file:///android_asset/www/index.html");
    }

    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        // آزادسازی کامل منابع WebView برای جلوگیری از نشت حافظه
        if (webView != null) {
            webView.destroy();
            webView = null;
        }
        super.onDestroy();
    }

    /** بستن کامل و استاندارد اپلیکیشن، همراه با افکت محو شدن (Fade). */
    private void closeAppStandard() {
        finishAffinity(); // بستن استاندارد تمام صفحات اپ (روش رسمی اندروید)
        overridePendingTransition(0, android.R.anim.fade_out);
    }

    /**
     * باز کردن لینک کانال با Custom Tabs: از موتور کروم واقعی دستگاه استفاده می‌کند
     * ولی به‌شکل «داخل اپ» (از پایین صفحه باز می‌شود، رنگ نوارش با تم اپ هماهنگ است،
     * و دکمه‌ی بازگشت مستقیم به اپ برمی‌گردد). این روش پایدارتر از WebView خام است.
     */
    private void openChannelStandard(String url) {
        try {
            CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
            builder.setToolbarColor(Color.parseColor("#0F5132"));
            builder.setShowTitle(true);
            CustomTabsIntent customTabsIntent = builder.build();
            customTabsIntent.launchUrl(this, Uri.parse(url));
        } catch (Exception e) {
            // حالت جایگزین: اگر به هر دلیلی Custom Tabs در دسترس نبود، مرورگر پیش‌فرض باز شود
            try {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                browserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(browserIntent);
            } catch (Exception e2) {
                Toast.makeText(this, CHANNEL_ERROR_MESSAGE, Toast.LENGTH_LONG).show();
            }
        }
    }

    /**
     * رابط جاوااسکریپت: از سمت index.html با window.AndroidApp.* صدا زده می‌شود.
     * متدهای این کلاس همیشه روی ترد اصلی (UI Thread) اجرا می‌شوند.
     */
    private class AppBridge {
        @JavascriptInterface
        public void closeApp() {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    closeAppStandard();
                }
            });
        }

        @JavascriptInterface
        public void openChannel(final String url) {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    openChannelStandard(url);
                }
            });
        }
    }
}
