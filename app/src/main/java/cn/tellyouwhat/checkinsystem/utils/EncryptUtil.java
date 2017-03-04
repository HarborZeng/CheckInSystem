package cn.tellyouwhat.checkinsystem.utils;

import android.util.Base64;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by Harbor-Laptop on 2017/2/28.
 */

public final class EncryptUtil {
	/**
	 * 以加盐方式返回Base64“加密”的结果，不安全，不推荐使用
	 *
	 * @param string 要加密的内容
	 * @param salt   盐值
	 * @return “加密”后的字符串
	 */
	public static String encryptBase64withSalt(String string, String salt) {
		return Base64.encodeToString((Base64.encodeToString(string.getBytes(), Base64.DEFAULT) + salt).getBytes(), Base64.DEFAULT);
	}

	/**
	 * 以加盐方式返回Base64“解密”的结果，不安全，不推荐使用
	 *
	 * @param string 要解密的内容
	 * @param salt   盐值
	 * @return “解密”后的字符串
	 */
	public static String decryptBase64withSalt(String string, String salt) {
		return new String(
				Base64.decode(
						new String(
								Base64.decode(string, Base64.DEFAULT)
						).replace(salt, ""), Base64.DEFAULT
				)
		);
	}

	/**
	 * 返回Base64“加密”的结果，不安全，不推荐使用
	 *
	 * @param string 要加密的内容
	 * @return “加密”后的字符串
	 */
	public static String encryptBase64(String string) {
		return Base64.encodeToString(string.getBytes(), Base64.DEFAULT);
	}

	/**
	 * 返回Base64“解密”的结果，不安全，不推荐使用
	 *
	 * @param string 要解密的内容
	 * @return “解密”后的字符串
	 */
	public static String decryptBase64(String string) {
		return new String(Base64.decode(string, Base64.DEFAULT));
	}

	private static final char hexDigits[] =
			{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

	private static String toHexString(byte[] bytes) {
		if (bytes == null) return "";
		StringBuilder hex = new StringBuilder(bytes.length * 2);
		for (byte b : bytes) {
			hex.append(hexDigits[(b >> 4) & 0x0F]);
			hex.append(hexDigits[b & 0x0F]);
		}
		return hex.toString();
	}


	public static String md5(String string) {
		byte[] encodeBytes = null;
		try {
			encodeBytes = MessageDigest.getInstance("MD5").digest(string.getBytes("UTF-8"));
		} catch (NoSuchAlgorithmException | UnsupportedEncodingException neverHappened) {
			throw new RuntimeException(neverHappened);
		}

		return toHexString(encodeBytes);
	}

	public static String md5WithSalt(String string, String salt) {
		byte[] encodeBytes = null;
		try {
			encodeBytes = MessageDigest.getInstance("MD5").digest(string.getBytes("UTF-8"));
		} catch (NoSuchAlgorithmException | UnsupportedEncodingException neverHappened) {
			throw new RuntimeException(neverHappened);
		}

		try {
			encodeBytes = MessageDigest.getInstance("MD5").digest((new String(encodeBytes) + salt).getBytes());
		} catch (NoSuchAlgorithmException neverHappened) {
			throw new RuntimeException(neverHappened);
		}
		return toHexString(encodeBytes);
	}
}
