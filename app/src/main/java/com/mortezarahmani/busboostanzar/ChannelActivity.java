package com.mortezarahmani.busboostanzar;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

/**
 * صفحه‌ی داخلی نمایش کانال اطلاع‌رسانی (ایتا).
 * به‌جای باز شدن در اپ ایتا یا مرورگر بیرونی، این صفحه داخل خود برنامه
 * باز می‌شود و یک دکمه‌ی «بستن» برای بازگشت به صفحه‌ی اصلی دارد.
 */
public class ChannelActivity extends AppCompatActivity {

    public static final String EXTRA_URL = "extra_url";

    private static final String COLOR_GREEN_DEEP = "#0F5132";
    private static final String COLOR_GREEN_MAIN = "#1F8A4C";
    private static final String ERROR_MESSAGE = "امکان دسترسی به کانال اطلاع‌رسانی بوستان زر وجود ندارد.";

    private WebView webView;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String url = getIntent() != null ? getIntent().getStringExtra(EXTRA_URL) : null;

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        root.addView(buildHeader());

        final ProgressBar progressBar = new ProgressBar(this);
        LinearLayout.LayoutParams progressParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(3));
        progressBar.setLayoutParams(progressParams);
        progressBar.setIndeterminate(true);
        root.addView(progressBar);

        webView = new WebView(this);
        webView.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);
        // برخی سایت‌ها درخواست‌های داخل WebView را به‌خاطر User-Agent پیش‌فرض مسدود می‌کنند؛
        // اینجا WebView را شبیه یک مرورگر معمولی کروم معرفی می‌کنیم تا این محدودیت برداشته شود.
        settings.setUserAgentString(
                "Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 (KHTML, like Gecko) "
                        + "Chrome/120.0.0.0 Mobile Safari/537.36");
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        // رفع خطای شناخته‌شده‌ی ERR_CACHE_MISS در WebView: کش را غیرفعال می‌کنیم
        // تا صفحه همیشه مستقیم از شبکه بارگذاری شود، نه از حافظه‌ی کش ناقص/خالی.
        settings.setCacheMode(WebSettings.LOAD_NO_CACHE);

        // پاک‌سازی کامل کش قدیمی WebView؛ این باگ شناخته‌شده‌ی اندروید (ERR_CACHE_MISS)
        // معمولاً به‌خاطر داده‌ی کش ناهماهنگ/خراب اتفاق می‌افتد.
        webView.clearCache(true);

        final boolean[] retried = {false};

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String finishedUrl) {
                super.onPageFinished(view, finishedUrl);
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                super.onReceivedError(view, request, error);
                if (!request.isForMainFrame()) return;

                String desc = error != null && error.getDescription() != null
                        ? error.getDescription().toString() : "";

                // باگ شناخته‌شده‌ی اندروید: بار اول معمولاً با یک‌بار reload خودکار حل می‌شود.
                if (desc.contains("ERR_CACHE_MISS") && !retried[0]) {
                    retried[0] = true;
                    view.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            view.reload();
                        }
                    }, 300);
                    return;
                }

                progressBar.setVisibility(View.GONE);
                showErrorState();
            }
        });

        root.addView(webView);
        setContentView(root);

        if (url != null && !url.trim().isEmpty()) {
            webView.loadUrl(url);
        } else {
            showErrorState();
        }
    }

    /** نوار بالای صفحه شامل عنوان و دکمه‌ی بستن، هماهنگ با تم سبز برنامه. */
    private LinearLayout buildHeader() {
        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setBackgroundColor(Color.parseColor(COLOR_GREEN_DEEP));
        header.setPadding(dp(12), dp(12), dp(12), dp(12));
        header.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        TextView closeBtn = new TextView(this);
        closeBtn.setText("✕  بستن");
        closeBtn.setTextColor(Color.WHITE);
        closeBtn.setTextSize(14f);
        closeBtn.setTypeface(closeBtn.getTypeface(), Typeface.BOLD);
        closeBtn.setPadding(dp(14), dp(8), dp(14), dp(8));
        closeBtn.setBackgroundColor(Color.parseColor(COLOR_GREEN_MAIN));
        closeBtn.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams closeParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        closeBtn.setLayoutParams(closeParams);
        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                overridePendingTransition(0, android.R.anim.fade_out);
            }
        });

        TextView title = new TextView(this);
        title.setText("کانال اطلاع‌رسانی بوستان زر");
        title.setTextColor(Color.WHITE);
        title.setTextSize(14f);
        title.setTypeface(title.getTypeface(), Typeface.BOLD);
        title.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        titleParams.setMarginStart(dp(10));
        title.setLayoutParams(titleParams);

        // ترتیب افزودن با توجه به راست‌چین بودن (RTL) طراحی برنامه
        header.addView(closeBtn);
        header.addView(title);

        return header;
    }

    /** نمایش پیام خطای استاندارد در صورت عدم امکان بارگذاری کانال (مثلاً نبود اینترنت). */
    private void showErrorState() {
        if (webView == null) return;
        String html = "<html><body style='display:flex;align-items:center;justify-content:center;"
                + "height:100vh;margin:0;font-family:sans-serif;text-align:center;padding:24px;"
                + "box-sizing:border-box;color:#0F5132;background:#eaf5ee;'>"
                + "<div>" + ERROR_MESSAGE + "</div></body></html>";
        webView.loadData(android.util.Base64.encodeToString(html.getBytes(), android.util.Base64.NO_PADDING),
                "text/html; charset=utf-8", "base64");
    }

    @Override
    protected void onDestroy() {
        if (webView != null) {
            webView.destroy();
            webView = null;
        }
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(0, android.R.anim.fade_out);
    }

    private int dp(int value) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(value * density);
    }
}
