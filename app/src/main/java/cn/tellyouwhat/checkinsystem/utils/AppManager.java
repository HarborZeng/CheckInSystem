package cn.tellyouwhat.checkinsystem.utils;

import android.app.Activity;

import java.util.Stack;

/**
 * 应用程序Activity管理类：用于Activity管理和应用程序退出
 * Created by mumu on 2017/3/15.
 */

public class AppManager {
    private static Stack<Activity> activityStack;
    private static AppManager instance;

    public AppManager() {
    }

    public static AppManager getAppManager() {
        if (instance == null) {
            synchronized (AppManager.class) {
                if (instance == null) {
                    instance = new AppManager();
                }
            }
        }
        return instance;
    }

    //添加Activity到堆栈
    public void addActivity(Activity activity) {
        if (activityStack == null) {
            activityStack = new Stack<>();
        }
        activityStack.add(activity);
    }

    public void finishAllActivity() {
        for (Activity activity : activityStack) {
            if (activity != null)
                activity.finish();
        }
        activityStack.clear();
    }
}
