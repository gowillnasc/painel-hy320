package com.hy320.painel;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.view.View;
import android.view.WindowManager;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class MainActivity extends Activity {
    private WebView web;
    private ValueCallback<Uri[]> pending;
    private final Handler h = new Handler(Looper.getMainLooper());
    private final Runnable tick = new Runnable() {
        @Override public void run() { boost(); h.postDelayed(this, 15 * 60 * 1000L); }
    };

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        web = new WebView(this);
        web.setBackgroundColor(Color.BLACK);
        web.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        WebSettings s = web.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setMediaPlaybackRequiresUserGesture(false);
        web.setWebViewClient(new WebViewClient());
        web.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onShowFileChooser(WebView w, ValueCallback<Uri[]> cb, FileChooserParams p) {
                if (pending != null) pending.onReceiveValue(null);
                pending = cb;
                try { startActivityForResult(p.createIntent(), 1); }
                catch (Exception e) { pending = null; return false; }
                return true;
            }
        });
        setContentView(web);
        web.loadUrl("file:///android_asset/painel.html");
        askOverlayOnce();
    }

    @Override
    protected void onActivityResult(int rc, int res, Intent d) {
        if (rc == 1 && pending != null) {
            pending.onReceiveValue(WebChromeClient.FileChooserParams.parseResult(res, d));
            pending = null;
        }
    }

    @Override protected void onResume() { super.onResume(); web.onResume(); immersive(); h.post(tick); }
    @Override protected void onPause() { h.removeCallbacks(tick); web.onPause(); super.onPause(); }
    @Override public void onWindowFocusChanged(boolean f) { super.onWindowFocusChanged(f); if (f) immersive(); }
    @Override public void onBackPressed() { /* modo quiosque: o painel nao fecha */ }

    private void immersive() {
        getWindow().getDecorView().setSystemUiVisibility(
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }

    /** Modo desempenho: libera memoria e reduz animacoes do sistema (a cada 15 min). */
    private void boost() {
        new Thread(() -> {
            try {
                ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
                for (ApplicationInfo a : getPackageManager().getInstalledApplications(0)) {
                    if ((a.flags & ApplicationInfo.FLAG_SYSTEM) == 0 && !a.packageName.equals(getPackageName()))
                        am.killBackgroundProcesses(a.packageName);
                }
            } catch (Exception ignored) {}
            try { // exige: adb shell pm grant com.hy320.painel android.permission.WRITE_SECURE_SETTINGS
                for (String k : new String[]{"window_animation_scale", "transition_animation_scale", "animator_duration_scale"})
                    Settings.Global.putFloat(getContentResolver(), k, 0.5f);
            } catch (Exception ignored) {}
        }).start();
    }

    /** Android 10+ so deixa abrir o app no boot com a permissao "sobrepor outros apps". */
    private void askOverlayOnce() {
        if (Build.VERSION.SDK_INT < 29 || Settings.canDrawOverlays(this)) return;
        SharedPreferences p = getSharedPreferences("p", 0);
        if (p.getBoolean("ov", false)) return;
        p.edit().putBoolean("ov", true).apply();
        try {
            startActivity(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName())));
        } catch (Exception ignored) {}
    }
}
