package cn.robotpen.file;

import java.util.UUID;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import cn.robotpen.core.symbol.Keys;

/**
 * 
 * @author Luis
 * @date 2016年3月25日 下午3:50:02
 *
 * Description
 */
public class FileManageService extends Service{
	public static final String TAG = FileManageService.class.getSimpleName();

	private String mUserFolderKey = null;
	/**
	 * 标记是否被绑定
	 */
	private boolean isBind = false;

	private final IBinder mBinder = new LocalBinder();

	@Override
	public IBinder onBind(Intent intent) {
		Log.v(TAG, "onBind");
		isBind = true;
		return mBinder;
	}
	
	@Override
	public boolean onUnbind(Intent intent) {
		Log.v(TAG, "onUnbind");
		isBind = false;
		return super.onUnbind(intent);
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
        Log.v(TAG, "onCreate");
        
        //创建客户端唯一UUID
        mUserFolderKey = getUserFolderKey();
        if(mUserFolderKey == null || mUserFolderKey.isEmpty()){
        	mUserFolderKey = UUID.randomUUID().toString();
        	setAutoFindConfig(mUserFolderKey);
        }
	}
	
	/**
	 * 获取是否被绑定
	 * @return
	 */
	public boolean getIsBind(){
		return isBind;
	}
	
	/**
	 * 获取用户文件目录KEY
	 * @return
	 */
	public String getUserFolderKey(){
		SharedPreferences preferences = this.getSharedPreferences(Keys.DEFAULT_SETTING_KEY, Context.MODE_PRIVATE);
		String result = preferences.getString(Keys.USER_FOLDER_KEY, null);
		return result;
	}
	
	/**
	 * 设置用户文件目录KEY
	 * @param key
	 */
	public void setAutoFindConfig(String key){
		SharedPreferences preferences = this.getSharedPreferences(Keys.DEFAULT_SETTING_KEY, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = preferences.edit();
		editor.putString(Keys.USER_FOLDER_KEY, key);
		editor.commit();
	}
	
	public class LocalBinder extends Binder {
		/**获取服务对象**/
		public FileManageService getService() {
			return FileManageService.this;
		}
	}

}