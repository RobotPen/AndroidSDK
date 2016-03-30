package cn.robotpen.file.qiniu;

import android.util.Log;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.SyncHttpClient;

import cz.msebera.android.httpclient.Header;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cn.robotpen.file.model.ResFile;
import cn.robotpen.file.model.ResponseRes;
import cn.robotpen.file.symbol.FileType;

/**
 * 
 * @author Luis
 * @date 2016年3月25日 下午6:51:07
 *
 * Description
 */
public class GetResourcesPort {
	public static final String TAG = GetResourcesPort.class.getSimpleName();
	
	public static final int GET_ERROR = 0;
	public static final int GET_SUCCESS = 1;
	public static final int GET_DIR = 1001;
	public static final int GET_FILE = 1002;
	
	private final String mUserIdentifier;
	private final OnGetResourcesResult mOnGetResourcesResult;
	
	/**
	 * 获取资源接口
	 * @param userIdentifier 用户标识
	 * @param result 获取资源回调
	 */
    public GetResourcesPort(String userIdentifier,OnGetResourcesResult result){
    	this.mUserIdentifier = userIdentifier;
    	this.mOnGetResourcesResult = result;
    }

	public void getDirectory(FileType type){
		getDirectory(type,null);
	}
	public void getDirectory(FileType type, String marker){
		String prefix = mUserIdentifier +"/"+ type.toString() +"/";
		String path = "/list?bucket="+ QiniuConfig.BUCKET +"&limit=10&delimiter=%2F&prefix="+ prefix;
		if(marker != null)
			path += "&marker="+marker;
		
		new Thread(new GetResourcesRunnable(path)).start();
	}

	public void getFile(ResFile file){
		getFile(file,null);
	}
	public void getFile(ResFile file, String marker){
		
			
		String prefix = file.Key.replace(file.getSuffix(), "");
		String path = "/list?bucket="+ QiniuConfig.BUCKET +"&limit=10&prefix="+ prefix;
		if(marker != null)
			path += "&marker="+marker;

		new Thread(new GetResourcesRunnable(path)).start();
	}
	
	private void getResources(final String path){
		String url = QiniuConfig.RSF_DOMAIN + path;
		
		SyncHttpClient client = new SyncHttpClient();
		client.removeHeader("Host");
		client.addHeader("Host", QiniuConfig.RSF_HOST);
		client.addHeader("Content-Type", "application/x-www-form-urlencoded");
		client.addHeader("Authorization", "QBox "+QiniuConfig.getToken(path));
		Log.v(TAG, "url:"+url);
		client.post(url, new JsonHttpResponseHandler(){
			@Override
		    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
				Log.v(TAG, "onSuccess:"+response.toString());
				try {
					handlerResponseResources(response,path);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					handlerResult(GET_ERROR, null);
				}
		    }
			public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
				Log.v(TAG, "onFailure:");
				handlerResult(GET_ERROR, null);
		    }
		});
	}
	
	private void handlerResponseResources(JSONObject response,String path) throws JSONException{
		ResponseRes res = new ResponseRes();
		if(!response.isNull("marker"))
			res.Marker = response.getString("marker");
		
		JSONArray items = response.getJSONArray("items");
		if(items.length() > 0){
			//用于检查是否转码完成
			List<String> dirList = null; 
			JSONArray commonPrefixes = null;
			if(!response.isNull("commonPrefixes")){
				commonPrefixes = response.getJSONArray("commonPrefixes");
				if(commonPrefixes.length() > 0){
					dirList = new ArrayList<String>(commonPrefixes.length());
					for(int i = 0;i < commonPrefixes.length();i++){
						dirList.add(commonPrefixes.getString(i));
					}
				}
			}

			res.Items = new ArrayList<ResFile>(items.length());
			ResFile item;
			for(int i = 0;i < items.length();i++){
				item = new ResFile(items.getJSONObject(i));
				//检查是否转码完成
				String dir = item.Key.replace(item.getSuffix(),"") +"/";
				if(dirList != null && dirList.contains(dir)){
					item.isDecode = true;
				}
				res.Items.add(item);
			}
		}
		handlerResult(GET_SUCCESS, res);
	}
	
	private void handlerResult(int state,ResponseRes res){
		if(mOnGetResourcesResult != null)
			mOnGetResourcesResult.result(state, res);
	}
	
	/**
	 * 发起获取资源请求
	 * @author Luis
	 * @date 2016年3月30日 上午3:06:36
	 *
	 */
	private class GetResourcesRunnable implements Runnable{
		private String path;
		public GetResourcesRunnable(String path){
			this.path = path;
		}
		@Override
		public void run() {
			getResources(path);
		}
	}
	
	/**
	 * 获取资源回调
	 * @author Luis
	 * @date 2016年3月28日 下午2:38:03
	 *
	 */
	public interface OnGetResourcesResult{
		void result(int state,ResponseRes res);
	}
}
