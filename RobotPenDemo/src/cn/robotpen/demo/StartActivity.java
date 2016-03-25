package cn.robotpen.demo;

import cn.robotpen.core.services.PenService;
import cn.robotpen.core.services.UsbPenService;
import cn.robotpen.core.symbol.DeviceVersion;
import cn.robotpen.core.symbol.Keys;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;

/**
 * 
 * @author Xiaoz
 * @date 2015年9月30日 下午4:38:45
 *
 * Description
 */
public class StartActivity extends Activity implements OnClickListener{
	private Handler mHandler;
			
	private Button mBleBut;
	private Button mUsbBut;
	private ProgressDialog mProgressDialog;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_start);
		
		mHandler = new Handler();
		
		mBleBut = (Button) findViewById(R.id.bleBut);
		mUsbBut = (Button) findViewById(R.id.usbBut);
		
		mBleBut.setOnClickListener(this);
		mUsbBut.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch(v.getId()){
		case R.id.bleBut:
			mProgressDialog = ProgressDialog.show(this, "", getString(R.string.service_ble_start), true);
			//绑定蓝牙笔服务
			RobotPenApplication.getInstance().bindPenService(Keys.APP_PEN_SERVICE_NAME);
			isPenServiceReady(Keys.APP_PEN_SERVICE_NAME);
			break;
		case R.id.usbBut:
			mProgressDialog = ProgressDialog.show(this, "", getString(R.string.service_usb_start), true);
			//绑定USB笔服务
			RobotPenApplication.getInstance().bindPenService(Keys.APP_USB_SERVICE_NAME);
			isPenServiceReady(Keys.APP_USB_SERVICE_NAME);
			break;
		}
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		StartActivity.this.finish();
	}
	
	private void isPenServiceReady(final String svrName){
		final PenService service = RobotPenApplication.getInstance().getPenService();
		if(service != null){
			mHandler.postDelayed(new Runnable() {
				@Override
				public void run() {
					dismissProgressDialog();
					if(Keys.APP_PEN_SERVICE_NAME.equals(svrName)){
						startActivity(new Intent(StartActivity.this, MainActivity.class));
					}else if(Keys.APP_USB_SERVICE_NAME.equals(svrName)){
						Intent intent = new Intent(StartActivity.this, PenInfo.class);
						intent.putExtra(Keys.KEY_VALUE, svrName);
						startActivity(intent);
					}
				}
			}, 500);
		}else{
			mHandler.postDelayed(new Runnable() {
				@Override
				public void run() {
					isPenServiceReady(svrName);
				}
			}, 1000);
		}
	}
	

	/**释放progressDialog**/
	protected void dismissProgressDialog(){
		if(mProgressDialog != null){
			if(mProgressDialog.isShowing())
				mProgressDialog.dismiss();
			
			mProgressDialog = null;
		}
	}
}
