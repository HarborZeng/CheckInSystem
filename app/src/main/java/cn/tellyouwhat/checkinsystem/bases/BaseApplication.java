package cn.tellyouwhat.checkinsystem.bases;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.UserManager;

import com.oasisfeng.condom.CondomProcess;
import com.squareup.leakcanary.LeakCanary;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

import org.xutils.common.util.LogUtil;
import org.xutils.x;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import cn.tellyouwhat.checkinsystem.BuildConfig;
import cn.tellyouwhat.checkinsystem.handler.CrashHandler;
import cn.tellyouwhat.checkinsystem.utils.ConstantValues;

public class BaseApplication extends Application {
    /**
     * 维护Activity 的list
     */
    private static final List<Activity> sActivities = Collections.synchronizedList(new LinkedList<Activity>());
    public static IWXAPI api;

    /**
     * get current Activity 获取当前Activity（栈中最后一个压入的）
     */
    public static Activity currentActivity() {
        if (sActivities == null || sActivities.isEmpty()) {
            return null;
        }
        return sActivities.get(sActivities.size() - 1);
    }

    /**
     * 结束当前Activity（栈中最后一个压入的）
     */
    public static void finishCurrentActivity() {
        if (sActivities == null || sActivities.isEmpty()) {
            return;
        }
        Activity activity = sActivities.get(sActivities.size() - 1);
        finishActivity(activity);
    }

    /**
     * 结束指定的Activity
     */
    public static void finishActivity(Activity activity) {
        if (sActivities == null || sActivities.isEmpty()) {
            return;
        }
        if (activity != null) {
            sActivities.remove(activity);
            activity.finish();
        }
    }

    /**
     * 结束指定类名的Activity
     */
    public static void finishActivity(Class<?> cls) {
        if (sActivities == null || sActivities.isEmpty()) {
            return;
        }
        for (Activity activity : sActivities) {
            if (activity.getClass().equals(cls)) {
                finishActivity(activity);
            }
        }
    }

    /**
     * 按照指定类名找到activity
     *
     * @param cls 类名
     * @return 要寻找的类
     */
    public static Activity findActivity(Class<?> cls) {
        Activity targetActivity = null;
        if (sActivities != null) {
            for (Activity activity : sActivities) {
                if (activity.getClass().equals(cls)) {
                    targetActivity = activity;
                    break;
                }
            }
        }
        return targetActivity;
    }

    /**
     * 结束所有Activity
     */
    public static void finishAllActivity() {
        if (sActivities == null) {
            return;
        }
        for (Activity activity : sActivities) {
            activity.finish();
        }
        sActivities.clear();
    }

    /**
     * 退出应用程序
     */
    public static void appExit() {
        try {
            LogUtil.e("app exit");
            finishAllActivity();
        } catch (Exception ignored) {
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();

        registerActivityListener();

        CondomProcess.installExceptDefaultProcess(this);

        registerToWeChat();
        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return;
        }
        LeakCanary.install(this);
        // Normal app init code...

        x.Ext.init(this);
//		x.Ext.setDebug(true);
        solveUserManagerMemoryLeakProblem();

        CrashHandler.getInstance().init();

    }

    private void solveUserManagerMemoryLeakProblem() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR1) {
            //fix android leak fix which is caused by UserManager holding on to a activity ctx
            try {
                final Method m;

                m = UserManager.class.getMethod("get", Context.class);

                m.setAccessible(true);
                m.invoke(null, this);

                //above is reflection for below...
                //UserManager.get();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (Throwable e) {
                if (BuildConfig.DEBUG) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    private void registerToWeChat() {
        api = WXAPIFactory.createWXAPI(super.getApplicationContext(), ConstantValues.WX_APP_ID, true);
        api.registerApp(ConstantValues.WX_APP_ID);
    }

    /**
     * @param activity 作用说明 ：添加一个activity到管理里
     */
    public void pushActivity(Activity activity) {
        sActivities.add(activity);
        LogUtil.d("activityList:size:" + sActivities.size());
    }

    /**
     * @param activity 作用说明 ：删除一个activity在管理里
     */
    public void popActivity(Activity activity) {
        sActivities.remove(activity);
        LogUtil.d("activityList:size:" + sActivities.size());
    }

    /**
     * @return 作用说明 ：获取当前最顶部activity的实例
     */
    public Activity getTopActivity() {
        Activity mBaseActivity;
        synchronized (sActivities) {
            final int size = sActivities.size() - 1;
            if (size < 0) {
                return null;
            }
            mBaseActivity = sActivities.get(size);
        }
        return mBaseActivity;

    }

    /**
     * @return 作用说明 ：获取当前最顶部的acitivity 名字
     */
    public String getTopActivityName() {
        Activity mBaseActivity = null;
        synchronized (sActivities) {
            final int size = sActivities.size() - 1;
            if (size < 0) {
                return null;
            }
            mBaseActivity = sActivities.get(size);
        }
        return mBaseActivity.getClass().getName();
    }

    private void registerActivityListener() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
                @Override
                public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

                    //监听到 Activity创建事件 将该 Activity 加入list
                    pushActivity(activity);

                }

                @Override
                public void onActivityStarted(Activity activity) {

                }

                @Override
                public void onActivityResumed(Activity activity) {

                }

                @Override
                public void onActivityPaused(Activity activity) {

                }

                @Override
                public void onActivityStopped(Activity activity) {

                }

                @Override
                public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

                }

                @Override
                public void onActivityDestroyed(Activity activity) {
                    if (null == sActivities || sActivities.isEmpty()) {
                        return;
                    }
                    if (sActivities.contains(activity)) {

                        //监听到 Activity销毁事件 将该Activity 从list中移除
                        popActivity(activity);
                    }
                }
            });
        }
    }
}
