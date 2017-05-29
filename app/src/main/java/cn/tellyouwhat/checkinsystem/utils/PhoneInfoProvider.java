package cn.tellyouwhat.checkinsystem.utils;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.text.format.Formatter;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Date;
import java.util.List;

import static android.content.Context.TELEPHONY_SERVICE;

/**
 * Created by Harbor-Laptop on 2017/5/28.
 * 工具类，用来提供手机基础信息
 */

public class PhoneInfoProvider {
	private static PhoneInfoProvider instance;

	public static PhoneInfoProvider getInstance() {
		if (instance == null) {
			synchronized (PhoneInfoProvider.class) {
				if (instance == null) {
					instance = new PhoneInfoProvider();
				}
			}
		}
		return instance;
	}

	/**
	 * The WIFI_SERVICE must be looked up on the Application context or memory will leak on devices
	 * < Android N. Try changing context to context.getApplicationContext()
	 *
	 * @param context 需要Application的context
	 * @return MAC地址
	 */
	@SuppressLint("HardwareIds")
	public String getMacAddress(Context context) {
		String macAddress;
		WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		WifiInfo wifiInfo = wifiManager.getConnectionInfo();
		macAddress = wifiInfo.getMacAddress();
		return "macAddress:  " + macAddress + "  \n";
	}

	public String getTelephonyInfo(Context context) {
		TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(TELEPHONY_SERVICE);
		@SuppressLint("HardwareIds") String imei = telephonyManager.getDeviceId();
		@SuppressLint("HardwareIds") String imsi = telephonyManager.getSubscriberId();
		@SuppressLint("HardwareIds") String number = telephonyManager.getLine1Number(); // 手机号码，有的可得，有的不可得
		int networkType = telephonyManager.getNetworkType();
		String networkOperator = telephonyManager.getNetworkOperator();
		String networkOperatorName = telephonyManager.getNetworkOperatorName();
		int phoneType = telephonyManager.getPhoneType();
		String simCountryIso = telephonyManager.getSimCountryIso();
		String simOperator = telephonyManager.getSimOperator();
		String simOperatorName = telephonyManager.getSimOperatorName();
		@SuppressLint("HardwareIds") String simSerialNumber = telephonyManager.getSimSerialNumber();
		boolean networkRoaming = telephonyManager.isNetworkRoaming();

		return "IMEI:  " + imei + "  \n" +
				"IMSI:  " + imsi + "  \n" +
				"networkType:  " + networkType + "  \n" +
				"number:  " + number + "  " + number + "  \n" +
				"networkOperator:  " + networkOperator + "  \n" +
				"networkOperatorName:  " + networkOperatorName + "  \n" +
				"phoneType:  " + phoneType + "  \n" +
				"networkType:  " + networkType + "  \n" +
				"simCountryIso:  " + simCountryIso + "  \n" +
				"simOperator:  " + simOperator + "  \n" +
				"simOperatorName:  " + simOperatorName + "  \n" +
				"simSerialNumber:  " + simSerialNumber + "  \n" +
				"networkRoaming:  " + networkRoaming + "  \n";
	}

	public String getBuildInfo() {
		@SuppressLint("HardwareIds") String serial = Build.SERIAL;
		String board = Build.BOARD;
		String bootloader = Build.BOOTLOADER;
		String brand = Build.BRAND;
		String device = Build.DEVICE;
		String display = Build.DISPLAY;
		String fingerprint = Build.FINGERPRINT;
		String hardware = Build.HARDWARE;
		String host = Build.HOST;
		String id = Build.ID;
		String manufacturer = Build.MANUFACTURER;
		String model = Build.MODEL;
		String product = Build.PRODUCT;
		String tags = Build.TAGS;
		String type = Build.TYPE;
		Date time = new Date(Build.TIME);

		return "serial:  " + serial + "  \n" +
				"bootloader:  " + bootloader + "  \n" +
				"brand:  " + brand + "  \n" +
				"device:  " + device + "  \n" +
				"display:  " + display + "  \n" +
				"fingerprint:  " + fingerprint + "  \n" +
				"hardware:  " + hardware + "  \n" +
				"host:  " + host + "  \n" +
				"id:  " + id + "  \n" +
				"manufacturer:  " + manufacturer + "  \n" +
				"model:  " + model + "  \n" +
				"product:  " + product + "  \n" +
				"tags:  " + tags + "  \n" +
				"type:  " + type + "  \n" +
				"time: " + time + "\n";
	}

	public String getScreenSize(Context context) {
		Resources resources = context.getResources();
		DisplayMetrics displayMetrics = resources.getDisplayMetrics();
		float density = displayMetrics.density;
		int width = displayMetrics.widthPixels;
		int height = displayMetrics.heightPixels;

		return "density: " + density + "\n" +
				"width:  " + width + "  \n" +
				"height:  " + height + "  \n";
	}

	public String getCpuInfo() {
		String str1 = "/proc/cpuinfo";
		String str2 = "";
		String cpuModel = "";
		String cpuRate = "";
		String[] arrayOfString;
		try {
			FileReader fr = new FileReader(str1);
			BufferedReader localBufferedReader = new BufferedReader(fr, 8192);
			str2 = localBufferedReader.readLine();
			arrayOfString = str2.split("\\s+");
			for (int i = 2; i < arrayOfString.length; i++) {
				cpuModel = cpuModel + arrayOfString[i] + " ";
			}
			str2 = localBufferedReader.readLine();
			arrayOfString = str2.split("\\s+");
			cpuRate += arrayOfString[2];
			localBufferedReader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "cpuModel:  " + cpuModel + "  \n" +
				"cpuRate:  " + cpuRate + "  \n";
	}

	public String getTotalMemory(Context context) {
		ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
		ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		activityManager.getMemoryInfo(memoryInfo);
		long mTotalMem = 0;
		long mAvailMem = memoryInfo.availMem;
		String str1 = "/proc/meminfo";
		String str2;
		String[] arrayOfString;
		try {
			FileReader localFileReader = new FileReader(str1);
			BufferedReader localBufferedReader = new BufferedReader(localFileReader, 8192);
			str2 = localBufferedReader.readLine();
			arrayOfString = str2.split("\\s+");
			mTotalMem = Integer.valueOf(arrayOfString[1]) * 1024;
			localBufferedReader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		String totalMemory = Formatter.formatFileSize(context, mTotalMem);
		String availableMemory = Formatter.formatFileSize(context, mAvailMem);

		return "totalMemory:  " + totalMemory + "  \n" +
				"availableMemory:  " + availableMemory + "  \n";
	}

	public String getAllApp(Context context) {
		String result = "";
		List<PackageInfo> packages = context.getPackageManager().getInstalledPackages(0);
		for (PackageInfo i : packages) {
			if ((i.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
				result += i.applicationInfo.loadLabel(context.getPackageManager()).toString() + ":  " + i.versionName + "  \n";
			}
		}
		return result.substring(0, result.length() - 1) + "\n";
	}

	public String getAllInfo(Context context) {
		int localVersionCode = getLocalVersionCode(context);
		String buildInfo = getBuildInfo();
		String telephonyInfo = getTelephonyInfo(context);
		String screenSize = getScreenSize(context);
		String totalMemory = getTotalMemory(context);
		String cpuInfo = getCpuInfo();
		String macAddress = getMacAddress(context);
		String allApp = getAllApp(context);

		return
				"\n# Version Code:   \n" + localVersionCode + "  \n" +
						"# Build Info:   \n" + buildInfo + "  " +
						"# Telephony Information: \n" + telephonyInfo + "  " +
						"# Screen Size:   \n" + screenSize + "  " +
						"# Memory Status:   \n" + totalMemory + "  " +
						"# CPU Information:   \n" + cpuInfo + "  " +
						"# MAC address:   \n" + macAddress + "  " +
						"# All App Installed:  \n" + allApp + "  ";
	}

	public static int getLocalVersionCode(Context context) {
		PackageManager packageManager = context.getPackageManager();
		PackageInfo packageInfo;
		try {
			packageInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
		} catch (PackageManager.NameNotFoundException e) {
			e.printStackTrace();
			return 0;
		}
		return packageInfo.versionCode;
	}
}
