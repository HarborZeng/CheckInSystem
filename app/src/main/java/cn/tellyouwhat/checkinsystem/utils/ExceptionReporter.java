package cn.tellyouwhat.checkinsystem.utils;

import org.json.JSONObject;
import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.x;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Created by Harbor-Laptop on 2017/5/28.
 * 工具类，向方糖报告错误信息
 */

public class ExceptionReporter {
    public static void reportException(String title, String exception) {
        reportException(title, exception, null);
    }


    public static void reportException(String title, String message, String info) {
        String encodedTitle = title;
        String encodedContent = "";
        try {
            encodedTitle = URLEncoder.encode(title, "UTF-8");
            encodedContent = URLEncoder.encode(message + "\n" + (info == null ? "" : info), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        RequestParams requestParams = new RequestParams("http://sc.ftqq.com/SCU6693Tdfc142ce95a8a9fcfbbb14f587cbdf4258c9c7a088af6.send?text=" + encodedTitle + "&desp=" + encodedContent);
        x.http().get(requestParams, new Callback.CommonCallback<JSONObject>() {
            @Override
            public void onSuccess(JSONObject result) {

            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {

            }

            @Override
            public void onCancelled(CancelledException cex) {

            }

            @Override
            public void onFinished() {

            }
        });
    }
}
