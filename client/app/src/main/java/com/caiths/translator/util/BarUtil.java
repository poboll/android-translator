package com.caiths.translator.util;

import android.os.Build;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

public class BarUtil {
    private static final String TAG = "BarUtil";

    // 状态栏Mode
    public enum StatusBarMode {
        Light, Dark
    }

    // 去除标题栏
    public static void removeActionBar(AppCompatActivity appCompatActivity) {
        if (appCompatActivity.getSupportActionBar() != null) {
            appCompatActivity.getSupportActionBar().hide();
        } else {
            Log.w(TAG, "未发现标题栏");
        }
    }

    // 去除状态栏
    public static void removeStatusBar(AppCompatActivity appCompatActivity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            // 允许内容延伸到刘海区域
            WindowManager.LayoutParams lp = appCompatActivity.getWindow().getAttributes();
            lp.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
            appCompatActivity.getWindow().setAttributes(lp);
        } else {
            // 无需更改参数
            Log.d(TAG, "API Level小于28，无需更改参数");
        }

        appCompatActivity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    // 设置状态栏颜色
    public static void setStatusBarColor(AppCompatActivity appCompatActivity, StatusBarMode statusBarMode, int statusBarColor) {
        appCompatActivity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        
        if (statusBarMode == StatusBarMode.Light && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            appCompatActivity.getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        } else {
            appCompatActivity.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }
        
        appCompatActivity.getWindow().setStatusBarColor(statusBarColor);
    }
}
