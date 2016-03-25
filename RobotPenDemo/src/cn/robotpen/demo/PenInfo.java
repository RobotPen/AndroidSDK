package cn.robotpen.demo;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.AlertDialog.Builder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.DialogInterface.OnCancelListener;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import cn.robotpen.core.common.Listeners.OnConnectStateListener;
import cn.robotpen.core.common.Listeners.OnPointChangeListener;
import cn.robotpen.core.model.FrameSizeObject;
import cn.robotpen.core.model.PointObject;
import cn.robotpen.core.services.PenService;
import cn.robotpen.core.services.SmartPenService;
import cn.robotpen.core.services.UsbPenService;
import cn.robotpen.core.symbol.BatteryState;
import cn.robotpen.core.symbol.ConnectState;
import cn.robotpen.core.symbol.Keys;
import cn.robotpen.core.symbol.SceneType;
import cn.robotpen.core.utils.SystemUtil;
import cn.robotpen.core.views.MultipleCanvasView;
import cn.robotpen.core.views.MultipleCanvasView.PenModel;

/**
 * 笔信息显示
 * @author Xiaoz
 * @date 2015年6月12日 下午3:34:58
 *
 * Description
 */
public class PenInfo extends Activity{
	public static final String TAG = PenInfo.class.getSimpleName();
	public static final int REQUEST_SETTING_SIZE = 1000;
	
	/**笔服务广播处理**/
	private PenServiceReceiver mPenServiceReceiver;
	private PenService mPenService;
	private ProgressDialog mProgressDialog;
	private int mShowType = 0;
	private Button mXYBut;
	private Button mLineBut;

	private LinearLayout mXYFrame;
	private RelativeLayout mLineFrame;
	private FrameLayout mLineWindow;
	
	//笔的实际坐标
	private TextView mOriginalX;
	private TextView mOriginalY;
	
	//是否是写入状态
	private TextView mIsRoute;
	private TextView mIsSw1;
	private TextView mPenPressure;
	
	//纸张尺寸
	private TextView mSceneWidth;
	private TextView mSceneHeight;
	
	//纸张坐标
	private TextView mSceneX;
	private TextView mSceneY;
	
	//屏幕坐标
	private TextView mWindowX;
	private TextView mWindowY;
	
	/** 笔画布 **/
	private MultipleCanvasView mPenCanvasView;
	
	//笔视图
	private PenView mPenView;
	
	//当前设备屏幕宽度
	private int mDisplayWidth;
	//屏幕高度
	private int mDisplayHeight;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//控制屏幕常亮
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(R.layout.activity_info);

        ActionBar actionBar = getActionBar();
		actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
		
		DisplayMetrics metric = new DisplayMetrics();
	    getWindowManager().getDefaultDisplay().getMetrics(metric);
	    mDisplayWidth = metric.widthPixels;  // 屏幕宽度（像素）
	    mDisplayHeight = metric.heightPixels;  // 屏幕高度（像素）
	    
		mXYBut = (Button) findViewById(R.id.xyBut);
		mLineBut = (Button) findViewById(R.id.lineBut);
		
		mXYFrame = (LinearLayout) findViewById(R.id.xyFrame);
		mLineFrame = (RelativeLayout) findViewById(R.id.lineFrame);
		mLineWindow = (FrameLayout) findViewById(R.id.lineWindow);
		mPenCanvasView = (MultipleCanvasView) findViewById(R.id.penCanvasView);
		
		mOriginalX = (TextView) findViewById(R.id.originalX);
		mOriginalY = (TextView) findViewById(R.id.originalY);
		mIsRoute = (TextView) findViewById(R.id.isRoute);
		mIsSw1 = (TextView) findViewById(R.id.isSw1);
		mPenPressure = (TextView) findViewById(R.id.penPressure);
		mSceneWidth = (TextView) findViewById(R.id.sceneWidth);
		mSceneHeight = (TextView) findViewById(R.id.sceneHeight);
		mSceneX = (TextView) findViewById(R.id.sceneX);
		mSceneY = (TextView) findViewById(R.id.sceneY);
		mWindowX = (TextView) findViewById(R.id.windowX);
		mWindowY = (TextView) findViewById(R.id.windowY);

		//添加笔视图
		mPenView = new PenView(this);
		mLineWindow.addView(mPenView);
		
		//看坐标
		mXYBut.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mShowType = 0;
				initPage();
			}
		});
		
		//看实际划线
		mLineBut.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mShowType = 1;
				initPage();
			}
		});
		
		mPenService = RobotPenApplication.getInstance().getPenService();

		String address = getIntent().getStringExtra(Keys.KEY_DEVICE_ADDRESS);
		if(address != null && !address.isEmpty()){
			connectDevice(address);
		}else{
			String isUsbSvr = getIntent().getStringExtra(Keys.KEY_VALUE);
			if(isUsbSvr != null && !isUsbSvr.isEmpty() && isUsbSvr.equals(Keys.APP_USB_SERVICE_NAME)){
				((UsbPenService)mPenService).checkDeviceConnect();
				initSceneType();
			}else{
				alertError("IP address error.");
			}
		}
	}
	
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.pen_info, menu);
        return true;
    }

    @Override
	public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
        case android.R.id.home:
            PenInfo.this.finish();
            break;
    	case R.id.action_settings:
    		initSceneType(true);
    		break;
    	}
    	return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	if(resultCode == RESULT_OK){
    		if(requestCode == REQUEST_SETTING_SIZE){
    			Log.v(TAG, "onActivityResult:"+REQUEST_SETTING_SIZE);
    			
    			initSceneType();
    		}
    	}
	}
    
    @Override
	public void onResume() {
		super.onResume();
		
		if(mPenService != null){
			//设置笔坐标监听
			mPenService.setOnPointChangeListener(onPointChangeListener);
		}else{
			//注册笔服务通过广播方式发送的笔迹坐标信息
			mPenServiceReceiver = new PenServiceReceiver();
			IntentFilter intentFilter = new IntentFilter();
			intentFilter.addAction(Keys.ACTION_SERVICE_SEND_POINT);
			registerReceiver(mPenServiceReceiver, intentFilter);
		}
	}
	
	@Override
	public void onPause(){
		if(mPenService != null){
			mPenService.setOnPointChangeListener(null);
		}else{
			unregisterReceiver(mPenServiceReceiver);
		}
		super.onPause();
	}
	
	@Override
	protected void onDestroy() {
		//断开设备
		PenService service = RobotPenApplication.getInstance().getPenService();
		if(service != null){
			service.disconnectDevice();
		}
		
		super.onDestroy();
	}
	
	private void initPage(){
		if(mShowType == 0){
			mXYBut.setEnabled(false);
			mLineBut.setEnabled(true);
			mXYFrame.setVisibility(View.VISIBLE);
			mLineFrame.setVisibility(View.GONE);
		}else{
			mXYBut.setEnabled(true);
			mLineBut.setEnabled(false);
			
			mXYFrame.setVisibility(View.GONE);
			mLineFrame.setVisibility(View.VISIBLE);
		}
		PenService service = RobotPenApplication.getInstance().getPenService();
		
		//状态栏+ActionBar+菜单按钮高
		Rect frame = new Rect();  
		getWindow().getDecorView().getWindowVisibleDisplayFrame(frame); 
		int menuHeight = SystemUtil.dip2px(PenInfo.this,40) + getActionBar().getHeight() + frame.top;
		
		//设置画布尺寸信息
		FrameSizeObject sizeObj = new FrameSizeObject();
		
		sizeObj.frameWidth = mDisplayWidth;
		sizeObj.frameHeight = mDisplayHeight - menuHeight;
		
		sizeObj.sceneWidth = service.getSceneWidth();
		sizeObj.sceneHeight = service.getSceneHeight();
		
		sizeObj.initWindowSize();

		Log.v(TAG, "sceneWidth:"+sizeObj.sceneWidth+",sceneHeight:"+sizeObj.sceneHeight);
		Log.v(TAG, "DisplayWidth:"+mDisplayWidth+",DisplayHeight:"+mDisplayHeight);
		Log.v(TAG, "menuHeight:"+menuHeight);
		Log.v(TAG, "windowWidth:"+sizeObj.windowWidth+",windowHeight:"+sizeObj.windowHeight);
		Log.v(TAG, "windowLeft:"+sizeObj.windowLeft+",windowTop:"+sizeObj.windowTop);
		
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(sizeObj.windowWidth, sizeObj.windowHeight);
		params.setMargins(sizeObj.windowLeft, sizeObj.windowTop, 0, 0);
		mLineWindow.setLayoutParams(params);
		mPenCanvasView.setPenModel(PenModel.Pen);
		mPenCanvasView.setSize(sizeObj.windowWidth, sizeObj.windowHeight);
	}
	
	/**
	 * 初始化纸张尺寸
	 */
	private void initSceneType(){
		initSceneType(false);
	}
	/**
	 * 初始化纸张尺寸
	 * @param isShow 是否强制显示
	 */
	private void initSceneType(boolean isShow){
		//检查是否有默认纸张
		SceneType sceneType = mPenService.getSceneType();
		if(sceneType == SceneType.NOTHING || isShow){
			//没有设置默认纸张尺寸，弹出选择框
			final String[] menus = new String[]{"A4(纵向)","A4(横向)","A5(纵向)","A5(横向)","自定义"};
			
			Builder alert = new AlertDialog.Builder(this);
			alert.setTitle(R.string.select_please);
			alert.setItems(menus, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
					switch(which){
					case 0:
						mPenService.setSceneType(SceneType.A4);
						break;
					case 1:
						mPenService.setSceneType(SceneType.A4_horizontal);
						break;
					case 2:
						mPenService.setSceneType(SceneType.A5);
						break;
					case 3:
						mPenService.setSceneType(SceneType.A5_horizontal);
						break;
					case 4:
						//mSmartPenService.setSceneType(SceneType.CUSTOM);
						//跳转到尺寸设置界面
						Intent intent = new Intent();
						intent.setClass(PenInfo.this, SettingSize.class);
						PenInfo.this.startActivityForResult(intent, REQUEST_SETTING_SIZE);
						return;
					}
					initPage();
				}
			});
			alert.setNegativeButton(R.string.canceled, new DialogInterface.OnClickListener() {
				
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
					initSceneType();
				}
			});
			alert.setOnCancelListener(new OnCancelListener(){
				@Override
				public void onCancel(DialogInterface dialog) {
					initSceneType();
				}
			});
			alert.show();
		}else{
			initPage();
		}
	}
	
	private void alertError(String msg){
		Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("Warning");
        alert.setMessage(msg);
        alert.setPositiveButton("OK",new DialogInterface.OnClickListener(){
			@Override
			public void onClick(DialogInterface dialog, int which) {
				PenInfo.this.finish();
			}
        });
        alert.show();
	}
	
	/**释放progressDialog**/
	protected void dismissProgressDialog(){
		if(mProgressDialog != null){
			if(mProgressDialog.isShowing())
				mProgressDialog.dismiss();
			
			mProgressDialog = null;
		}
	}
	
	private void connectDevice(String address){
		PenService service = RobotPenApplication.getInstance().getPenService();
		if(service != null){
			ConnectState state = ((SmartPenService)service).connectDevice(onConnectStateListener, address);
			if(state != ConnectState.CONNECTING){
				alertError("The pen service connection failure.");
			}else{
				mProgressDialog = ProgressDialog.show(PenInfo.this, "", getString(R.string.connecting), true);
			}
		}
	}
	
	//处理笔服务通过广播方式发送的笔迹坐标信息
	//示例仅用作演示有这个功能，没有特殊需求可删除以下代码
	private class PenServiceReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals(Keys.ACTION_SERVICE_SEND_POINT)){
				//广播的形式接收笔迹信息
				String pointJson = intent.getStringExtra(Keys.KEY_PEN_POINT);
				if(pointJson != null && !pointJson.isEmpty()){

					Toast.makeText(PenInfo.this, pointJson, Toast.LENGTH_SHORT).show();
					//Log.v(TAG, "pointJson:"+pointJson);
					
					//更新笔坐标信息
					//如果注册了service.setOnPointChangeListener监听，那么请注释掉下面的代码，否则信息会冲突
					//反之如果需要使用Receiver，那么就不要使用setOnPointChangeListener
					//PointObject item = new PointObject(pointJson);
					//onPointChangeListener.change(item);
				}
				return;
			}
		}
	}
	
	private OnConnectStateListener onConnectStateListener = new OnConnectStateListener(){
		@Override
		public void stateChange(String address,ConnectState state) {
			switch(state){
			case PEN_READY:
				
				break;
			case PEN_INIT_COMPLETE:
				dismissProgressDialog();
				Toast.makeText(PenInfo.this, R.string.connected, Toast.LENGTH_SHORT).show();
				initSceneType();
				break;
			case CONNECTED:
				
				break;
			case SERVICES_FAIL:
				dismissProgressDialog();
				alertError("The pen service discovery failed.");
				break;
			case CONNECT_FAIL:
				dismissProgressDialog();
				alertError("The pen service connection failure.");
				break;
			case DISCONNECTED:
				dismissProgressDialog();
				Toast.makeText(PenInfo.this, R.string.disconnected, Toast.LENGTH_SHORT).show();
				
				mXYBut.setEnabled(false);
				mLineBut.setEnabled(false);
				break;
			default:
				
				break;
			}
		}
	};
	
	private OnPointChangeListener onPointChangeListener = new OnPointChangeListener(){
		
		@Override
		public void change(PointObject point) {
			
			//设置看坐标中的各个字段
			mOriginalX.setText(String.valueOf(point.originalX));
			mOriginalY.setText(String.valueOf(point.originalY));
			mIsRoute.setText(String.valueOf(point.isRoute));
			mIsSw1.setText(String.valueOf(point.isSw1));
			mPenPressure.setText(String.valueOf(point.pressure));
			
			mSceneWidth.setText(String.valueOf(point.getWidth()));
			mSceneHeight.setText(String.valueOf(point.getHeight()));
			
			mSceneX.setText(String.valueOf(point.getSceneX()));
			mSceneY.setText(String.valueOf(point.getSceneY()));
			
			if(point.battery == BatteryState.LOW){
				Toast.makeText(PenInfo.this, R.string.battery_low, Toast.LENGTH_LONG).show();
			}
			
			//获取显示窗口比例缩放坐标
			int windowX = point.getSceneX(mPenCanvasView.getWindowWidth());
			int windowY = point.getSceneY(mPenCanvasView.getWindowHeight());
			
			mWindowX.setText(String.valueOf(windowX));
			mWindowY.setText(String.valueOf(windowY));
			
			if(mShowType != 1)return;
			
			//绘制笔
			mPenView.bitmapX = windowX;
			mPenView.bitmapY = windowY;
			mPenView.isRoute = point.isRoute;
			mPenView.invalidate();
			
			//绘制笔迹
			mPenCanvasView.drawLine(windowX, windowY, point.isRoute,point.pressure);
		}
	};
}
	 
