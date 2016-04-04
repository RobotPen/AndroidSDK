package cn.robotpen.file.qiniu;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.json.JSONObject;
import com.qiniu.android.http.ResponseInfo;
import com.qiniu.android.storage.UpCompletionHandler;
import com.qiniu.android.storage.UploadManager;
import android.util.Base64;

/**
 * 文件资源上传接口
 * 
 * @author zoupeng
 *
 */

public class UpLoadResourcesPort {
	private UploadCallBackResult mUploadCallBackResult;
	private static long delayTimes = 3029414400l; // unix时间戳:2065-12-31 00:00:00
	private static final String MAC_NAME = "HmacSHA1";
	private static final String ENCODING = "UTF-8";

	public UpLoadResourcesPort(UploadCallBackResult mUploadCallBackResult) {
		this.mUploadCallBackResult = mUploadCallBackResult;
	}

	public void upLoadResources(String data) {
		new Thread(new UpLoadResourcesRunnable(data)).start();
	}

	/**
	 * 上传
	 *
	 * @param domain
	 *            bucketName的名字
	 * @param path
	 *            上传文件的路径地址
	 */
	public void uploadPic(final String path, final UploadCallBackResult callBack) {
		try {
			// 1:第一种方式 构造上传策略
			JSONObject _json = new JSONObject();
			_json.put("deadline", delayTimes);// 有效时间为一个小时
			_json.put("scope", QiniuConfig.BUCKET);
			final String _uploadToken = getToken(_json);
			UploadManager uploadManager = new UploadManager();
			uploadManager.put(path, null, _uploadToken, new UpCompletionHandler() {
				@Override
				public void complete(String key, ResponseInfo info, JSONObject response) {
					if (info.isOK()) {
						// String urls = getFileUrl("kymobile", keys);
						// callBack.success(urls);
						callBack.result(info);
					} else {
						// callBack.fail(key, info);
					}
				}
			}, null);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 获取token
	 * 
	 * @param _json
	 * @return
	 */
	public String getToken(JSONObject _json) {
		byte[] _sign;
		String _uploadToken = "";
		try {
			String _encodedPutPolicy = encodeToString(_json.toString().getBytes());
			_sign = HmacSHA1Encrypt(_encodedPutPolicy, QiniuConfig.SECRET_KEY);
			String _encodedSign = encodeToString(_sign);
			_uploadToken = QiniuConfig.ACCESS_KEY + ':' + _encodedSign + ':' + _encodedPutPolicy;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return _uploadToken;

	}

	public static byte[] HmacSHA1Encrypt(String encryptText, String encryptKey) throws Exception {
		byte[] data = encryptKey.getBytes(ENCODING);
		// 根据给定的字节数组构造一个密钥,第二参数指定一个密钥算法的名称
		SecretKey secretKey = new SecretKeySpec(data, MAC_NAME);
		// 生成一个指定 Mac 算法 的 Mac 对象
		Mac mac = Mac.getInstance(MAC_NAME);
		// 用给定密钥初始化 Mac 对象
		mac.init(secretKey);
		byte[] text = encryptText.getBytes(ENCODING);
		// 完成 Mac 操作
		return mac.doFinal(text);
	}

	public static String encodeToString(byte[] data) {
		return Base64.encodeToString(data, Base64.URL_SAFE | Base64.NO_WRAP);
	}

	/**
	 * 发起获取资源上传
	 * 
	 * @author Luis
	 * @date 2016年3月30日 上午3:06:36
	 *
	 */
	private class UpLoadResourcesRunnable implements Runnable {
		String path;

		public UpLoadResourcesRunnable(String path) {
			this.path = path;
		}

		@Override
		public void run() {
			uploadPic(path, mUploadCallBackResult);

		}
	}

	/**
	 * 上传资源回调
	 * 
	 * @author Luis
	 * @date 2016年3月28日 下午2:38:03
	 *
	 */
	public interface UploadCallBackResult {
		void result(ResponseInfo res);
	}

}
