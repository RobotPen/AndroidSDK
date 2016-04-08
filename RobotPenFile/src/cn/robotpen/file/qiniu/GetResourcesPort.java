package cn.robotpen.file.qiniu;

import android.util.Log;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.SyncHttpClient;

import cz.msebera.android.httpclient.Header;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;

import cn.robotpen.file.model.ResFile;
import cn.robotpen.file.model.ResponseRes;
import cn.robotpen.file.symbol.FileType;
import cn.robotpen.file.symbol.ListType;

/**
 * 
 * @author Luis
 * @date 2016年3月25日 下午6:51:07
 *
 *       Description
 */
public class GetResourcesPort {
	public static final String TAG = GetResourcesPort.class.getSimpleName();

	public static final int GET_ERROR = 0;
	public static final int GET_SUCCESS = 1;

	private final String mUserIdentifier;
	private final OnGetResourcesResult mOnGetResourcesResult;

	private SyncHttpClient mSyncHttpClient;

	/**
	 * 获取资源接口
	 * 
	 * @param userIdentifier
	 *            用户标识
	 * @param result
	 *            获取资源回调
	 */
	public GetResourcesPort(String userIdentifier, OnGetResourcesResult result) {
		this.mUserIdentifier = userIdentifier;
		this.mOnGetResourcesResult = result;
		mSyncHttpClient = new SyncHttpClient();
	}

	/**
	 * 取消所有请求
	 */
	public void cancelAllRequests() {
		mSyncHttpClient.cancelAllRequests(true);
	}

	public void getDirectory(FileType type) {
		getDirectory(type, null);
	}

	public void getDirectory(FileType type, String marker) {
		String path = "/list?bucket=" + QiniuConfig.BUCKET;
		path += "&limit=10";
		path += "&delimiter=%2F";

		String prefix;
		if (type == FileType.ALL) {
			prefix = mUserIdentifier + "/";
		} else {
			prefix = mUserIdentifier + "/" + type.toString() + "/";
		}
		try {
			path += "&prefix=" + URLEncoder.encode(prefix, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		if (marker != null)
			path += "&marker=" + marker;
		new Thread(new GetResourcesRunnable(path, ListType.DIR)).start();
	}

	public void getFile(ResFile file) {
		getFile(file, null);
	}

	public void getFile(ResFile file, String marker) {
		String prefix = "";
		try {
			prefix = URLEncoder.encode(file.DecodePath, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		String path = "/list?bucket=" + QiniuConfig.BUCKET + "&limit=40&prefix=" + prefix;
		if (marker != null)
			path += "&marker=" + marker;

		new Thread(new GetResourcesRunnable(path, ListType.FILE)).start();
	}

	private void getResources(final String path, final ListType getType) {
		String url = QiniuConfig.RSF_DOMAIN + path;

		mSyncHttpClient.removeHeader("Host");
		mSyncHttpClient.removeHeader("Authorization");

		mSyncHttpClient.addHeader("Host", QiniuConfig.RSF_HOST);
		mSyncHttpClient.addHeader("Content-Type", "application/x-www-form-urlencoded");
		mSyncHttpClient.addHeader("Authorization", "QBox " + QiniuConfig.getToken(path));
		Log.v(TAG, "url:" + url);
		mSyncHttpClient.post(url, new JsonHttpResponseHandler() {
			@Override
			public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
				Log.v(TAG, "onSuccess:" + response.toString());
				try {
					handlerResponseResources(response, path, getType);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					handlerResult(GET_ERROR, null);
				}
			}

			public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
				Log.v(TAG, "onFailure:" + (errorResponse != null ? errorResponse.toString() : ""));
				handlerResult(GET_ERROR, null);
			}
		});
	}

	private void handlerResponseResources(JSONObject response, String path, ListType type) throws JSONException {
		ResponseRes res = new ResponseRes();
		if (!response.isNull("marker"))
			res.Marker = response.getString("marker");

		JSONArray items = response.getJSONArray("items");
		if (items.length() > 0) {
			HashMap<String,ResFile> map = new HashMap<String,ResFile>();
			
			res.Type = type;
			//res.Items = new ArrayList<ResFile>();
			ResFile item;
			for (int i = 0; i < items.length(); i++) {
				item = new ResFile(items.getJSONObject(i));
				String[] sp = item.Key.split("/");
				if(sp.length == 3){
					if(!map.containsKey(item.Key)){
						map.put(item.Key, item);
					}else{
						ResFile file = map.get(item.Key);
						file.copy(item);
					}
				}else if(sp.length == 4){
					if(item.Key.endsWith(".jpg")){
						String key = item.Key.replace("/"+sp[sp.length - 1], "");
						if(!map.containsKey(key)){
							ResFile file = new ResFile();
							map.put(key, file);
						}else{
							ResFile file = map.get(item.Key);
							file.addChildResFile(item);
						}
					}
				}
			}
			res.Items = (ArrayList<ResFile>)map.values();
		}
		handlerResult(GET_SUCCESS, res);
	}

	private void handlerResult(int state, ResponseRes res) {
		if (mOnGetResourcesResult != null)
			mOnGetResourcesResult.result(state, res);
	}

	/**
	 * 发起获取资源请求
	 * 
	 * @author Luis
	 * @date 2016年3月30日 上午3:06:36
	 *
	 */

	private class GetResourcesRunnable implements Runnable {
		String path;
		ListType type;

		public GetResourcesRunnable(String path, ListType type) {
			this.path = path;
			this.type = type;
		}

		@Override
		public void run() {
			getResources(path, type);
		}
	}

	/**
	 * 获取资源回调
	 * 
	 * @author Luis
	 * @date 2016年3月28日 下午2:38:03
	 *
	 */
	public interface OnGetResourcesResult {
		void result(int state, ResponseRes res);
	}
}
