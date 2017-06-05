package cn.tellyouwhat.checkinsystem.handler;

import org.xutils.common.util.LogUtil;
import org.xutils.x;

import cn.tellyouwhat.checkinsystem.utils.ExceptionReporter;
import cn.tellyouwhat.checkinsystem.utils.PhoneInfoProvider;

/**
 * 全局异常捕获处理
 * Created by mli on 17-3-14.
 */

public class CrashHandler implements Thread.UncaughtExceptionHandler {

    private static CrashHandler inst;
    private Thread.UncaughtExceptionHandler mExceptionHandler;

    private CrashHandler() {
    }

    public static CrashHandler getInstance() {
        if (inst == null) inst = new CrashHandler();
        return inst;
    }

    public void init() {
        mExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(this);
    }


    private void handleUncaughtException(Throwable ex) {
        if (ex == null) return;
        // 把异常信息发送到服务器
        sendCrashReportsToServer(ex);

    }

    @Override
    public void uncaughtException(Thread thread, Throwable throwable) {
        handleUncaughtException(throwable);
        if (throwable != null) LogUtil.w(throwable);
        mExceptionHandler.uncaughtException(thread, throwable);
    }

    private void sendCrashReportsToServer(final Throwable ex) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                ExceptionReporter.reportException("CheckIn又双叒叕崩溃啦~~~",
                        "# 异常信息：   \n" + ex.getMessage() + "\n\n",
                        "# 机器信息:    \n" + PhoneInfoProvider.getInstance().getAllInfo(x.app()));
            }
        }).start();
    }

}
