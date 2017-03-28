package cn.tellyouwhat.checkinsystem.utils;

import android.os.Build;

import java.lang.reflect.Method;

public final class FlymeUtil {

	public static boolean isFlyme() {
		return Build.DISPLAY.contains("Flyme");
	}

}
