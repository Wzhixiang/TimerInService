package com.wzx.timerinservice;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

import static android.app.PendingIntent.FLAG_UPDATE_CURRENT;
import static android.os.PowerManager.ACQUIRE_CAUSES_WAKEUP;
import static android.os.PowerManager.SCREEN_DIM_WAKE_LOCK;

/**
 * @author wzx
 * @description 任务服务--利用 {@link AlarmManager} 达到定时器效果，需要注意 {@link #startAlarm()}
 * @file name TaskService
 * @date：2020/5/15 <author> <time> <version> <desc>
 */
public class TaskService extends Service {

    private static final String TAG = TaskService.class.getSimpleName();

    public static final String ACTION_DO_TASK = "do.task";
    public static final String ACTION_STOP_TASK = "stop.task";

    public static final int REQUEST_CODE_ALARM = 1;

    private static final int INTERVAL = 10 * 1000;

    private PowerManager.WakeLock wakeLock;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent.getAction();
        if (!TextUtils.isEmpty(action)) {
            if (action.equals(ACTION_DO_TASK)) {
                wakeup();
                //启动警报
                startAlarm();
                //do what you want
                doTask();
            } else if (action.equals(ACTION_STOP_TASK)) {
                //取消警报
                cancelAlarm();
                //停止服务
                stopSelf();
            }
        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        releaseWakeLock();
    }

    /**
     * 启动报警
     * <p>
     * AlarmManager将发送一个Action为 {@link TaskService#ACTION_DO_TASK} 的警报给 {@link TaskReceiver}
     */
    private void startAlarm() {
        Intent intent1 = new Intent(this, TaskReceiver.class);
        intent1.setAction(ACTION_DO_TASK);
        PendingIntent pIntent = PendingIntent.getBroadcast(this, REQUEST_CODE_ALARM, intent1, FLAG_UPDATE_CURRENT);

        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

        /**
         * 在不同的Android api中，控制时间精度的方法：
         * api >= 23，该使用 {@link AlarmManager#setExactAndAllowWhileIdle(int, long, PendingIntent)}
         * api = [19, 23), 该使用 {@link AlarmManager#setExact(int, long, PendingIntent)}
         * api < 19, 该使用 {@link AlarmManager#set(int, long, PendingIntent)}
         */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + INTERVAL, pIntent);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + INTERVAL, pIntent);
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + INTERVAL, pIntent);
        }
    }

    /**
     * 取消报警
     */
    private void cancelAlarm() {
        Intent intent = new Intent(this, TaskReceiver.class);
        intent.setAction(ACTION_DO_TASK);
        PendingIntent pIntent = PendingIntent.getBroadcast(this, REQUEST_CODE_ALARM, intent, FLAG_UPDATE_CURRENT);

        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarmManager.cancel(pIntent);
    }

    private void doTask() {
        Toast.makeText(this, "执行任务", Toast.LENGTH_SHORT).show();
        Log.i(TAG, "执行任务");
    }

    @SuppressLint("InvalidWakeLockTag")
    private void wakeup() {
        if (wakeLock == null) {
            PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
            wakeLock = powerManager.newWakeLock(SCREEN_DIM_WAKE_LOCK | ACQUIRE_CAUSES_WAKEUP, "keepActive");
            wakeLock.acquire();
        }

        if (wakeLock.isHeld()) {
            wakeLock.release();
            wakeLock = null;
        }
    }

    private void releaseWakeLock() {
        if (wakeLock != null && wakeLock.isHeld()) {
            wakeLock.release();
            wakeLock = null;
        }
    }
}
