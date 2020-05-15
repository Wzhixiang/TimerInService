package com.wzx.timerinservice;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.view.KeyEvent;
import android.view.View;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.btn_running_in_background).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                serviceAliveOnDevicesSetting();
            }
        });

        //是否加入电池优化白名单
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !isIgnoringBatteryOptimizations()) {
            requestIgnoreBatteryOptimizations();
        }

        startTask();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        stopTask();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            //app切换到后台
            backToHome();
            return false;
        } else {
            return super.onKeyDown(keyCode, event);
        }
    }

    @Override
    public void onBackPressed() {
        backToHome();
        super.onBackPressed();
    }

    /**
     * 还可以通过 {@link android.app.Activity#moveTaskToBack(boolean)} 返回Launcher页面
     */
    private void backToHome() {
        Intent home = new Intent(Intent.ACTION_MAIN);
        home.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        home.addCategory(Intent.CATEGORY_HOME);
        startActivity(home);
    }

    private void startTask() {
        Intent intent = new Intent(this, TaskReceiver.class);
        intent.setAction(TaskService.ACTION_DO_TASK);
        sendBroadcast(intent);
    }

    private void stopTask() {
        Intent intent = new Intent(this, TaskReceiver.class);
        intent.setAction(TaskService.ACTION_STOP_TASK);
        sendBroadcast(intent);
    }

    /**
     * 判断app是否在电池管理白名单中
     * 需要在Manifestes中添加权限配置：<uses-permission android:name="android.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS" />
     *
     * @return
     */
    @SuppressLint({"NewApi", "BatteryLife"})
    @RequiresApi(api = Build.VERSION_CODES.M)
    public boolean isIgnoringBatteryOptimizations() {
        boolean isIgnoring = false;
        PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        if (powerManager != null) {
            isIgnoring = powerManager.isIgnoringBatteryOptimizations(getPackageName());
        }
        return isIgnoring;
    }

    /**
     * 申请加入白名单
     */
    public void requestIgnoreBatteryOptimizations() {
        try {
            Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
            intent.setData(Uri.parse("package:" + getPackageName()));
            startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 跳转服务后台运行设置
     * <p>
     * 此方法借鉴技术大牛，并非原创
     *
     * @return 返回自启动管理页面的Intent
     */
    public void serviceAliveOnDevicesSetting() {
        ComponentName componentName = null;
        String brand = Build.MANUFACTURER;
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        switch (brand.toLowerCase()) {
            case "samsung":
                componentName = new ComponentName("com.samsung.android.sm", "com.samsung.android.sm.app.dashboard.SmartManagerDashBoardActivity");
                break;
            case "huawei":
                /**
                 * 荣耀V8，EMUI 8.0.0，Android 8.0上，以下两者效果一样
                 * componentName = new ComponentName("com.huawei.systemmanager", "com.huawei.systemmanager.appcontrol.activity.StartupAppControlActivity");
                 * 目前看是通用的
                 * componentName = new ComponentName("com.huawei.systemmanager", "com.huawei.systemmanager.startupmgr.ui.StartupNormalAppListActivity");
                 */
                componentName = new ComponentName("com.huawei.systemmanager", "com.huawei.systemmanager.startupmgr.ui.StartupNormalAppListActivity");
                break;
            case "xiaomi":
                componentName = new ComponentName("com.miui.securitycenter", "com.miui.permcenter.autostart.AutoStartManagementActivity");
                break;
            case "vivo":
                /**
                 * componentName = new ComponentName("com.iqoo.secure", "com.iqoo.secure.safaguard.PurviewTabActivity");
                 *
                 * componentName = new ComponentName("com.iqoo.secure", "com.iqoo.secure.ui.phoneoptimize.AddWhiteListActivity");
                 */
                componentName = new ComponentName("com.vivo.permissionmanager", "com.vivo.permissionmanager.activity.BgStartUpManagerActivity");
                break;
            case "oppo":
                /**
                 * componentName = new ComponentName("com.oppo.safe", "com.oppo.safe.permission.startup.StartupAppListActivity");
                 */
                componentName = new ComponentName("com.coloros.oppoguardelf", "com.coloros.powermanager.fuelgaue.PowerUsageModelActivity");
                break;
            case "yulong":
            case "360":
                componentName = new ComponentName("com.yulong.android.coolsafe", "com.yulong.android.coolsafe.ui.activity.autorun.AutoRunListActivity");
                break;
            case "meizu":
                componentName = new ComponentName("com.meizu.safe", "com.meizu.safe.permission.SmartBGActivity");
                break;
            case "oneplus":
                componentName = new ComponentName("com.oneplus.security", "com.oneplus.security.chainlaunch.view.ChainLaunchAppListActivity");
                break;
            case "letv":
                intent.setAction("com.letv.android.permissionautoboot");
            default:
                //其他
                intent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
                intent.setData(Uri.fromParts("package", getPackageName(), null));
                break;
        }

        intent.setComponent(componentName);

        List<ResolveInfo> resolveInfos = getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        if (resolveInfos.size() > 0) {
            startActivity(intent);
        }
    }
}
