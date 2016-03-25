package cn.robotpen.core.services;

import java.util.ArrayList;
import java.util.List;

import cn.robotpen.core.common.Listeners.OnConnectStateListener;
import cn.robotpen.core.common.Listeners.OnFixedPointListener;
import cn.robotpen.core.common.Listeners.OnPenGestureListener;
import cn.robotpen.core.common.Listeners.OnPointChangeListener;
import cn.robotpen.core.common.Listeners.OnScanDeviceListener;
import cn.robotpen.core.model.AutoFindConfig;
import cn.robotpen.core.model.DeviceObject;
import cn.robotpen.core.model.PointObject;
import cn.robotpen.core.symbol.ConnectState;
import cn.robotpen.core.symbol.Keys;
import cn.robotpen.core.symbol.LocationState;
import cn.robotpen.core.symbol.SceneType;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

/**
 * 
 * @author Luis
 * @date 2016年1月30日 上午8:40:28
 *
 * Description
 */
public abstract class PenService extends Service{
	public static final String TAG = PenService.class.getSimpleName();
	
	/**
	 * 自定义纸张尺寸最小值
	 */
	public static final int SETTING_SIZE_MIN = 500;
	/**
	 * 自定义纸张尺寸最大值
	 */
	public static final int SETTING_SIZE_MAX = 20000;
	
	protected OnScanDeviceListener onScanDeviceListener = null;
	protected OnConnectStateListener onConnectStateListener = null;
	protected OnPointChangeListener onPointChangeListener = null;
	protected OnFixedPointListener onFixedPointListener = null;
	protected OnPenGestureListener onPenGestureListener = null;

	/**是否发送广播信息**/
	protected boolean isBroadcast;
	/**是否正在扫描**/
	protected boolean isScanning;
	/**扫描时间**/
	protected int mScanTime = 10000;

	/**场景坐标对象**/
	protected PointObject mScenePointObject = new PointObject(); 
	/**检查固定点样本数量**/
	private static final int CHECK_FIXED_SAMPLE_COUNT = 50;
	//路劲计数
	private int mRouteSumX,mRouteSumY;
	private ArrayList<Short> mSamplePointX = new ArrayList<Short>();
	private ArrayList<Short> mSamplePointY = new ArrayList<Short>();
	
	protected PointObject mFirstPointObject = null;
	protected PointObject mSecondPointObject = null;

	private final IBinder mBinder = new LocalBinder();
	
	/**
	 * 标记是否被绑定
	 */
	private boolean isBind = false;
	
	/**
	 * 判断定位第一个坐标按下状态<br />
	 * 这个值用来防止程序判断完成第一个坐标定位后，立即进入第二个坐标判断
	 * **/
	protected boolean mFirstPointDown = false;
	
	/**获取服务标记**/
	abstract public String getSvrTag();
	
	/**获取接收器占用高度，服务应该根据连接上的设备类型判断占用纸张的高度，如XN680夹子夹在纸上占用掉880**/
	abstract public short getReceiverGapHeight();
	
	/**获取当前连接设备名称**/
	abstract public DeviceObject getConnectDevice();
	
	/**检查设备连接状态**/
	abstract public ConnectState checkDeviceConnect();

	/**断开设备连接**/
	abstract public ConnectState disconnectDevice();
	
	/**发送笔状态**/
	abstract public void sendFixedPointState(LocationState state);
	
	abstract public void handlerPointInfo(PointObject point);

	/**
	 * 扫描设备
	 * @param listener
	 */
	abstract public boolean scanDevice(OnScanDeviceListener listener);
	
	/**
	 * 停止扫描
	 */
	abstract public void stopScanDevice();
	
	@Override
	public void onCreate() {
		super.onCreate();
        Log.v(TAG, "onCreate");
        
	}

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
	
	/**
	 * 获取是否被绑定
	 * @return
	 */
	public boolean getIsBind(){
		return isBind;
	}
	
	/**
	 * 设置是否开启广播，默认为false。如果需要通过广播的方式与Service交互，那么请设置为true。
	 * @param value
	 */
	public void setBroadcastEnabled(boolean value){
		this.isBroadcast = value;
	}
	
	/**
	 * 设置扫描持续时间
	 * @param millisecond
	 */
	public void setScanTime(int millisecond){
		this.mScanTime = millisecond;
	}
	
	/**
	 * 设置连接状态变更监听
	 * @param listener
	 */
	public void setOnConnectStateListener(OnConnectStateListener listener){
		this.onConnectStateListener = listener;
	}
	
	/**
	 * 设置笔坐标变更监听
	 * @param listener
	 */
	public void setOnPointChangeListener(OnPointChangeListener listener){
		this.onPointChangeListener = listener;
	}
	
	/**
	 * 设置笔手势监听
	 * @param listener
	 */
	public void setOnPenGestureListener(OnPenGestureListener listener){
		this.onPenGestureListener = listener;
	}
	
	/**
	 * 设置坐标定点监听
	 * @param listener
	 */
	public void setOnFixedPointListener(OnFixedPointListener listener){
		this.onFixedPointListener = listener;
	}
	

	/**
	 * 发送连接状态
	 * @param address
	 * @param state
	 */
	public void sendConnectState(String address,ConnectState state){
		if(onConnectStateListener != null){
			onConnectStateListener.stateChange(address,state);
		}
		
		if(isBroadcast){
			Intent intent = new Intent(Keys.ACTION_SERVICE_BLE_CONNECT_STATE);
			intent.putExtra(Keys.KEY_DEVICE_ADDRESS, address);
			intent.putExtra(Keys.KEY_CONNECT_STATE, state.getValue());
			sendBroadcast(intent);
		}
	}
	
	/**
	 * 发送笔的点坐标
	 * @param point
	 */
	public void sendPointInfo(PointObject point){
		if(onPointChangeListener != null)
			onPointChangeListener.change(point);
		
		//检查是否触发笔手势
		checkPenGestureStatus(point);
		
		if(isBroadcast){
			//发送笔迹JSON格式广播包
			Intent intent = new Intent(Keys.ACTION_SERVICE_SEND_POINT);
			intent.putExtra(Keys.KEY_PEN_POINT, point.toJsonString());
			sendBroadcast(intent);
		}
	}
	
	
	/**
	 * 设置当前场景类型
	 * @param value
	 * @return
	 */
	public boolean setSceneType(SceneType value){
		return setSceneType(value,0,0);
	}
	
	/**
	 * 设置当前场景类型
	 * @param value
	 * @param width
	 * @param height
	 */
	public boolean setSceneType(SceneType value,int width,int height){
		return setSceneType(value,width,height,0,0);
	}
	
	/**
	 * 设置当前场景类型
	 * @param value
	 * @param width
	 * @param height
	 * @param offsetX	x偏移量
	 * @param offsetY	y偏移量
	 * @return
	 */
	public boolean setSceneType(SceneType value,int width,int height,int offsetX,int offsetY){
		boolean result = false;
		SharedPreferences preferences = this.getSharedPreferences(Keys.DEFAULT_SETTING_KEY, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = preferences.edit();
		editor.putInt(Keys.DEFAULT_SCENE_KEY, value.getValue());
		editor.putInt(Keys.DEFAULT_SCENE_WIDTH_KEY, width);
		editor.putInt(Keys.DEFAULT_SCENE_HEIGHT_KEY, height);
		editor.putInt(Keys.DEFAULT_SCENE_OFFSET_X_KEY, offsetX);
		editor.putInt(Keys.DEFAULT_SCENE_OFFSET_Y_KEY, offsetY);
		if(result = editor.commit()){
			mScenePointObject = new PointObject();
			mScenePointObject.setSceneType(value);
			
			if(value == SceneType.CUSTOM){
				mScenePointObject.setCustomScene((short)width, (short)height,(short)offsetX,(short)offsetY);
			}else{
				mScenePointObject.setOffset((short)offsetX,(short)offsetY);
			}
		}
		return result;
	}
	
	public boolean setSceneOffset(int offsetX,int offsetY){
		boolean result = false;
		SharedPreferences preferences = this.getSharedPreferences(Keys.DEFAULT_SETTING_KEY, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = preferences.edit();
		editor.putInt(Keys.DEFAULT_SCENE_OFFSET_X_KEY, offsetX);
		editor.putInt(Keys.DEFAULT_SCENE_OFFSET_Y_KEY, offsetY);
		if(result = editor.commit()){
			mScenePointObject.setOffset((short)offsetX,(short)offsetY);
		}
		return result;
	}
	
	/**
	 * 获取是否自动发现
	 * @return
	 */
	public AutoFindConfig getAutoFindConfig(){
		AutoFindConfig result = new AutoFindConfig();
		SharedPreferences preferences = this.getSharedPreferences(Keys.DEFAULT_SETTING_KEY, Context.MODE_PRIVATE);
		result.isAutoFind = preferences.getBoolean(Keys.DEFAULT_AUTO_FIND_DEVICE_KEY, false);
		result.scanTime = preferences.getInt(Keys.DEFAULT_AUTO_FIND_SCAN_KEY, 0);
		result.gapTime = preferences.getInt(Keys.DEFAULT_AUTO_FIND_GAP_KEY, 0);
		return result;
	}
	
	/**
	 * 设置是否自动发现设备
	 * @param value
	 */
	public void setAutoFindConfig(AutoFindConfig config){
		SharedPreferences preferences = this.getSharedPreferences(Keys.DEFAULT_SETTING_KEY, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = preferences.edit();
		editor.putBoolean(Keys.DEFAULT_AUTO_FIND_DEVICE_KEY, config.isAutoFind);
		editor.putInt(Keys.DEFAULT_AUTO_FIND_SCAN_KEY, config.scanTime);
		editor.putInt(Keys.DEFAULT_AUTO_FIND_GAP_KEY, config.gapTime);
		editor.commit();
	}
	
	/**
	 * 获取场景宽度
	 * @return
	 */
	public short getSceneWidth(){
		return mScenePointObject.getWidth();
	}
	
	/**
	 * 获取场景高度
	 * @return
	 */
	public short getSceneHeight(){
		return mScenePointObject.getHeight();
	}
	
	/**获取x轴偏移**/
	public short getSceneOffsetX(){
		return mScenePointObject.getOffsetX();
	}
	
	/**获取x轴偏移**/
	public short getSceneOffsetY(){
		return mScenePointObject.getOffsetY();
	}
	
	/**
	 * 获取当前场景类型
	 * @return
	 */
	public SceneType getSceneType(){
		SharedPreferences preferences = this.getSharedPreferences(Keys.DEFAULT_SETTING_KEY, Context.MODE_PRIVATE);
		SceneType type = SceneType.toSceneType(preferences.getInt(Keys.DEFAULT_SCENE_KEY, SceneType.NOTHING.getValue()));
		
		if(type != SceneType.NOTHING){
			mScenePointObject = new PointObject();
			mScenePointObject.setSceneType(type);
			
			short offsetX = (short)preferences.getInt(Keys.DEFAULT_SCENE_OFFSET_X_KEY, 0);
			short offsetY = (short)preferences.getInt(Keys.DEFAULT_SCENE_OFFSET_Y_KEY, 0);
			
			if(type == SceneType.CUSTOM){
				short width = (short)preferences.getInt(Keys.DEFAULT_SCENE_WIDTH_KEY, 0);
				short height = (short)preferences.getInt(Keys.DEFAULT_SCENE_HEIGHT_KEY, 0);
				
				mScenePointObject.setCustomScene(width, height, offsetX, offsetY);
			}else{
				mScenePointObject.setOffset(offsetX, offsetY);
			}
		}
		return type;
	}
	
	
	protected void handlerPointList(List<PointObject> pointList){
		PointObject item = null;
		if(pointList != null && pointList.size() > 0){
			for(int i = 0;i<pointList.size();i++){
				item = pointList.get(i);
				item.setTopGap(getReceiverGapHeight());
				if(mScenePointObject.getSceneType() == SceneType.CUSTOM){
					item.setCustomScene(mScenePointObject.getWidth(), 
										mScenePointObject.getHeight(),
										mScenePointObject.getOffsetX(),
										mScenePointObject.getOffsetY());
				}else{
					item.setSceneType(mScenePointObject.getSceneType());
					item.setOffset(mScenePointObject.getOffsetX(), mScenePointObject.getOffsetY());
				}
				handlerPointInfo(item);
				//Log.v(TAG, "out point:"+item.toString());
				addSamplePoint(item);
				
				//定位完第一个坐标，笔被抬起后，记录状态
				if(mFirstPointDown && !item.isRoute)mFirstPointDown = false;
			}
			
			//处理固定点坐标信息
			handlerFixedPointInfo(item);
		}
	}
	
	/**
	 * 添加坐标样本
	 * @param x
	 * @param y
	 */
	protected void addSamplePoint(PointObject point){
		//当笔尖和笔上按钮被按下，开始收集坐标样本
		if(point.isRoute && point.isSw1){
			mRouteSumX += point.originalX;
			mRouteSumY += point.originalY;
			
			mSamplePointX.add(point.originalX);
			mSamplePointY.add(point.originalY);
		}else{
			clearSamplePoint();
		}
	}
	
	/**
	 * 当前状态是否是固定在一个点上
	 * @return
	 */
	protected boolean isFixedPoint(){
		int result = 0;
		if(mSamplePointX != null && mSamplePointY != null){
			int sizeX = mSamplePointX.size();
			int sizeY = mSamplePointY.size();
			
			if(sizeX >= CHECK_FIXED_SAMPLE_COUNT && sizeY >= CHECK_FIXED_SAMPLE_COUNT){
				int gapX = (mRouteSumX / sizeX) - mSamplePointX.get(0);
				int gapY = (mRouteSumY / sizeY) - mSamplePointY.get(0);
				
				if(Math.abs(gapX) < 50)result++;
				if(Math.abs(gapY) < 50)result++;
			}
		}
		return result == 2;
	}

	/**
	 * 应用自定义坐标
	 */
	public void applyFixedPoint(){
		if(mFirstPointObject != null && mSecondPointObject != null){
			int width = Math.abs(mSecondPointObject.originalX - mFirstPointObject.originalX);
			//根据定位规则，第2个点Y必须大于第1个点Y
			int height = mSecondPointObject.originalY - mFirstPointObject.originalY;
			int offsetX = width / 2 - mSecondPointObject.originalX;

			setSceneType(SceneType.CUSTOM,width,height,offsetX,mFirstPointObject.originalY);
		}
		againFixedPoint();
	}
	
	/**
	 * 重新定位纸张尺寸
	 */
	public void againFixedPoint(){
		mFirstPointObject = null;
		mSecondPointObject = null;
		
		mFirstPointDown = false;
		
		clearSamplePoint();
	}
	
	/**
	 * 清除样本坐标
	 */
	public void clearSamplePoint(){
		mRouteSumX = mRouteSumY = 0;
		mSamplePointX.clear();
		mSamplePointY.clear();
	}
	
	/**
	 * 处理固定点坐标信息
	 * @param point
	 */
	protected void handlerFixedPointInfo(PointObject point){
		if(onFixedPointListener == null)return;
		
		LocationState state = LocationState.SecondComp;
		if(mFirstPointObject == null){
			if(isFixedPoint()){
				mFirstPointObject = point;
				mFirstPointDown = true;
				state = LocationState.FirstComp;
			}else{
				state = LocationState.DontLocation;
			}
		}else if(mSecondPointObject == null){
			if(!mFirstPointDown && isFixedPoint()){
				//判断第二个点只能出现在右下角
				if(point.originalX < mFirstPointObject.originalX || point.originalY < mFirstPointObject.originalY){
					//如果不是，那么提示已定位第一个点，请在右下角定位第二个点
					state = LocationState.FirstComp;
				}else if(point.originalX - mFirstPointObject.originalX < SETTING_SIZE_MIN
						|| point.originalY - mFirstPointObject.originalY < SETTING_SIZE_MIN){
					state = LocationState.LocationSmall;
				}else{
					mSecondPointObject = point;
					state = LocationState.SecondComp;
				}
			}else{
				state = LocationState.FirstComp;
			}
		}
		sendFixedPointState(state);
	}
	
	/**
	 * 检查笔手势状态
	 * @param point
	 */
	private void checkPenGestureStatus(PointObject point){
		if(onPenGestureListener == null)return;
		
		if(isFixedPoint()){
			onPenGestureListener.longClick(point);
			clearSamplePoint();
		}
	}
	

	public class LocalBinder extends Binder {
		/**获取服务对象**/
		public PenService getService() {
			return PenService.this;
		}
	}
}
