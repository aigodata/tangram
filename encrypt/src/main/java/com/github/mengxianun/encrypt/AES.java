package com.github.mengxianun.encrypt;

import com.github.mengxianun.core.ResultStatus;
import com.github.mengxianun.core.exception.JsonDataException;
import com.github.mengxianun.core.utils.Utils;
import org.apache.commons.codec.binary.Base64;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.security.*;
import java.util.Arrays;

/**
 *
 * @author ngh
 * AES128 算法
 *
 * CBC 模式
 *
 * PKCS7Padding 填充模式
 *
 * CBC模式需要添加一个参数iv
 *
 * java不支持PKCS7Padding，只支持PKCS5Padding
 * 要实现java端用PKCS7Padding填充，需要bouncycastle组件来实现
 */
public class AES {
	// 算法名称
	final static String KEY_ALGORITHM = "AES";
	// 加解密算法/模式/填充方式
	final static String TRANSFORMATION = "AES/CBC/PKCS7Padding";

	final static String PROVIDER = "BC";

	final static String KEY = "key";
	final static String IV = "iv";
	final static String UID = "uid";

	/**
	 * encrypt
	 *
	 * @param data
	 *            要加密的字符串
	 * @param keyStr
	 *            加密密钥
	 * @param ivStr
	 *            iv偏移量
	 * @return
	 */
	private static String encrypt(String data, String keyStr, String ivStr) throws Exception {

		try {
			return new String(new Base64().encode(doFinal(Cipher.ENCRYPT_MODE, keyStr, ivStr, data.getBytes())));
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(ResultStatus.JSON_FORMAT_ERROR.message());
		}
	}
	/**
	 * decrypt
	 *
	 * @param data
	 *            要解密的字符串
	 * @param keyStr
	 *            解密密钥
	 * @param ivStr
	 *            iv偏移量
	 * @return
	 */
	public static String decrypt(String data, String keyStr, String ivStr) throws Exception {

		try {
			byte[] encryptedData = new Base64().decode(data);
			return new String(doFinal(Cipher.DECRYPT_MODE, keyStr, ivStr, encryptedData));
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(ResultStatus.JSON_FORMAT_ERROR.message());
		}
	}
	private static byte[] getSecretKeyBytes(String keyStr) {

		byte[] keyBytes = keyStr.getBytes();

		// 如果密钥不足16位，那么就补足.  这个if 中的内容很重要
		int base = 16;

		if (keyBytes.length % base != 0) {
			int groups = keyBytes.length / base + (keyBytes.length % base != 0 ? 1 : 0);
			byte[] temp = new byte[groups * base];
			Arrays.fill(temp, (byte) 0);
			System.arraycopy(keyBytes, 0, temp, 0, keyBytes.length);
			keyBytes = temp;
		}
		return keyBytes;
	}
	private static byte[] doFinal(int type, String keyStr, String ivStr, byte[] data) throws JsonDataException {

		try {
			// 初始化
			Security.addProvider(new BouncyCastleProvider());
			// 转化成JAVA的密钥格式
			Key key = new SecretKeySpec(getSecretKeyBytes(keyStr), KEY_ALGORITHM);
			// 初始化cipher
			Cipher cipher = Cipher.getInstance(TRANSFORMATION, PROVIDER);

			cipher.init(type, key, new IvParameterSpec(ivStr.getBytes()));
			return cipher.doFinal(data);
		} catch (Exception e) {
			e.printStackTrace();
			throw new JsonDataException("");
		}
	}
	public static void main(String[] args) throws Exception {

		AES aes = new AES();
		// 加解密 密钥
		String keys = "1530dfab199e4e24";
		String iv = "b97bcdb3ff1684aa";
		System.out.println(encode(keys));
		System.out.println(encode(iv));
////		byte[] iv = { 0x30, 0x31, 0x30, 0x32, 0x30, 0x33, 0x30, 0x34, 0x30, 0x35, 0x30, 0x36, 0x30, 0x37, 0x30, 0x38 };
//		String iv = "01020304050607q8";
//
//		String content = "{'a':'123,'b':'456','c':'789','e':'789','f':'789','g':'789','h':'789','i':'789','j':'789','k':'000'}";
		String content = "{'select': 'es.traffic','native': {'size': 0,'query': {'constant_score': {'filter': {'range': {'stat_time': {'gt': '2018-07-08 14:00:00','lt': '2018-07-08 15:00:00'}}}}},'aggs': {'top': {'terms': {'field': 'probe_name','order': {'total_traffic': 'desc'},'size': 3},'aggs': {'total_traffic': {'sum': {'script': {'source': 'doc.count_up.value + doc.count_down.value'}}},'interval_data': {'date_histogram': {'field': 'stat_time','interval': '5m','format': 'yyyy-MM-dd HH:mm:ss','min_doc_count': 0,'extended_bounds': {'min': '2018-07-08 14:00:00','max': '2018-07-08 14:55:00'}},'aggs': {'interval_traffic': {'sum': {'script': {'source': 'doc.count_up.value + doc.count_down.value'}}}}}}}}}}";
//		// 加密字符串
//		System.out.println("加密前的：" + content);
//		System.out.println("加密密钥：" + keys);
//		// 加密方法
		String encrypt = aes.encrypt(content, keys, iv);
		System.out.println("加密后的内容：" + encrypt);

		/** 解密方法 */
		String decrypt = aes.decrypt(encrypt, keys, iv);
		System.out.println("解密后的内容：" + decrypt);

	}
	public static String getUUID() {
		return Utils.getUUID();
	}
	public static String[] getKeyAndIV() {
		String str = getUUID();
		System.out.println("UUID------->" + str);
		return new String[]{str.substring(0, 16), str.substring(16)};
	}
	private static String encode(String str) {
		return new String(new Base64().encode(str.getBytes()));
	}
	private static String decode(String str) throws IOException {
		return new String(new Base64().decode(str));
	}

}
