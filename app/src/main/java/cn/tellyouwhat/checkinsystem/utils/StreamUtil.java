package cn.tellyouwhat.checkinsystem.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by Harbor-Laptop on 2017/2/19.
 * including some awesome tools
 */
public class StreamUtil {
	/**
	 * Transform a Stream to String, using {@link java.util.Arrays ByteArrayOutputStream }
	 * @param inputStream Stream that you are going to transform to {@code String}
	 * @return return a String created by parameter{@code inputStream}
	 * @throws IOException
	 */
	public static String stream2String(InputStream inputStream) throws IOException {
		byte[] b = new byte[1024];
		int len ;
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		while((len = inputStream.read(b)) != -1){
			//把读到的字节先写入字节数组输出流中存起来
			bos.write(b, 0, len);
		}
		//把字节数组输出流中的内容转换成字符串
		//默认使用utf-8
		String string = new String(bos.toByteArray());
		bos.close();
		return string;
	}
}
