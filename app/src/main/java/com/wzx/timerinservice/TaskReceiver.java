package com.wzx.timerinservice;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

/**
 * @author wzx
 * @description 任务接收器
 * @file name TaskReceiver
 * @date：2020/5/15 <author> <time> <version> <desc>
 */
public class TaskReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (TextUtils.isEmpty(action)) {
            return;
        }

        if (action.equals(TaskService.ACTION_DO_TASK)) {
            Intent intent1 = new Intent(context, TaskService.class);
            intent1.setAction(TaskService.ACTION_DO_TASK);
            context.startService(intent1);
        } else if (action.equals(TaskService.ACTION_STOP_TASK)) {
            Intent intent1 = new Intent(context, TaskService.class);
            intent1.setAction(TaskService.ACTION_STOP_TASK);
            context.startService(intent1);
        }
    }
}
