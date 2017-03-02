package cn.tellyouwhat.checkinsystem.utils;

/**
 * Created by Harbor-Laptop on 2017/2/24.
 */

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

/**
 * @author Dylan 本类封装了Android中向web服务器提交数据的两种方式四种方法
 */
public class SubmitDataByHttpClientAndOrdinaryWay {
	/**
	 * 使用get请求以普通方式提交数据
	 *
	 * @param map  传递进来的数据，以map的形式进行了封装
	 * @param path 要求服务器servlet的地址
	 * @return 返回的boolean类型的参数
	 * @throws Exception
	 */
	public Boolean submitDataByDoGet(Map<String, String> map, String path)
			throws Exception {
		// 拼凑出请求地址
		StringBuilder sb = new StringBuilder(path);
		sb.append("?");
		for (Map.Entry<String, String> entry : map.entrySet()) {
			sb.append(entry.getKey()).append("=").append(entry.getValue());
			sb.append("&");
		}
		sb.deleteCharAt(sb.length() - 1);
		String str = sb.toString();
		System.out.println(str);
		URL Url = new URL(str);
		HttpURLConnection HttpConn = (HttpURLConnection) Url.openConnection();
		HttpConn.setRequestMethod("GET");
		HttpConn.setReadTimeout(5000);
		// GET方式的请求不用设置什么DoOutPut()之类的吗？
		if (HttpConn.getResponseCode() == HttpURLConnection.HTTP_OK) {
			return true;
		}
		return false;
	}

	/**
	 * 普通方式的DoPost请求提交数据
	 *
	 * @param map  传递进来的数据，以map的形式进行了封装
	 * @param path 要求服务器servlet的地址
	 * @return 返回的boolean类型的参数
	 * @throws Exception
	 */
	public Boolean submitDataByDoPost(Map<String, String> map, String path) throws Exception {
// 注意Post地址中是不带参数的，所以newURL的时候要注意不能加上后面的参数
		URL Url = new URL(path);
		// Post方式提交的时候参数和URL是分开提交的，参数形式是这样子的：name=y&age=6
		StringBuilder sb = new StringBuilder();
		// sb.append("?");
		for (Map.Entry<String, String> entry : map.entrySet()) {
			sb.append(entry.getKey()).append("=").append(entry.getValue());
			sb.append("&");
		}
		sb.deleteCharAt(sb.length() - 1);
		String str = sb.toString();
		HttpURLConnection HttpConn = (HttpURLConnection) Url.openConnection();
		HttpConn.setRequestMethod("POST");
		HttpConn.setReadTimeout(5000);
		HttpConn.setDoOutput(true);
		HttpConn.setRequestProperty("Content-Type",
				"application/x-www-form-urlencoded");
		HttpConn.setRequestProperty("Content-Length",
				String.valueOf(str.getBytes().length));
		OutputStream os = HttpConn.getOutputStream();
		os.write(str.getBytes());
		if (HttpConn.getResponseCode() == HttpURLConnection.HTTP_OK) {
			return true;
		}
		return false;
	}
}