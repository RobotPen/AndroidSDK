package cn.robotpen.demo;

import cn.robotpen.core.services.PenService;
import cn.robotpen.core.symbol.Keys;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.Toast;

/**
 * 
 * @author Xiaoz
 * @date 2015年9月30日 下午4:38:45
 *
 *       Description
 */
public class StartActivity extends Activity implements OnClickListener {
	private Handler mHandler;
	private Button mBleBut;
	private Button mUsbBut;
	private Button mTestBut;
	private Button mUploadBut;
	private ProgressDialog mProgressDialog;
	public static final String TAG = StartActivity.class.getSimpleName();
	private Context mContext;
	private String SAVE_FILE_DIRECTORY = "";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_start);
		mContext = this;

		
		mHandler = new Handler();
		mBleBut = (Button) findViewById(R.id.bleBut);
		mUsbBut = (Button) findViewById(R.id.usbBut);
		mTestBut = (Button) findViewById(R.id.testBut);
		mUploadBut = (Button) findViewById(R.id.upload_But);

		mBleBut.setOnClickListener(this);
		mUsbBut.setOnClickListener(this);
		mTestBut.setOnClickListener(this);
		mUploadBut.setOnClickListener(this);
		// initData();
	}
	
	@Override
	protected void onStart() {
		super.onStart();
//		initData();
	}

	/**
	 * 获取需要文档文件的路径
	 */
	@SuppressLint("ShowToast")
	private void initData() {
		Intent intent = getIntent();
		if (intent != null) {
			String type = intent.getType();
			boolean isMatch = getFileMimeType(type);
			if (isMatch) {
				Uri uri = intent.getData();
				SAVE_FILE_DIRECTORY = uri.getPath();
			} else {
				Toast.makeText(mContext, "暂不支持该种格式文件上传", 0).show();
				return;
			}
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.bleBut:
			mProgressDialog = ProgressDialog.show(this, "", getString(R.string.service_ble_start), true);
			// 绑定蓝牙笔服务
			RobotPenApplication.getInstance().bindPenService(Keys.APP_PEN_SERVICE_NAME);
			isPenServiceReady(Keys.APP_PEN_SERVICE_NAME);
			break;
		case R.id.usbBut:
			mProgressDialog = ProgressDialog.show(this, "", getString(R.string.service_usb_start), true);
			// 绑定USB笔服务
			RobotPenApplication.getInstance().bindPenService(Keys.APP_USB_SERVICE_NAME);
			isPenServiceReady(Keys.APP_USB_SERVICE_NAME);
			break;
		case R.id.testBut:

			
			break;

		case R.id.upload_But: // 测试上传资源
		
			break;
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		StartActivity.this.finish();
	}

	private void isPenServiceReady(final String svrName) {
		final PenService service = RobotPenApplication.getInstance().getPenService();
		if (service != null) {
			mHandler.postDelayed(new Runnable() {
				@Override
				public void run() {
					dismissProgressDialog();
					if (Keys.APP_PEN_SERVICE_NAME.equals(svrName)) {
						startActivity(new Intent(StartActivity.this, MainActivity.class));
					} else if (Keys.APP_USB_SERVICE_NAME.equals(svrName)) {
						Intent intent = new Intent(StartActivity.this, PenInfo.class);
						intent.putExtra(Keys.KEY_VALUE, svrName);
						startActivity(intent);
					}
				}
			}, 500);
		} else {
			mHandler.postDelayed(new Runnable() {
				@Override
				public void run() {
					isPenServiceReady(svrName);
				}
			}, 1000);
		}
	}

	/** 释放progressDialog **/
	protected void dismissProgressDialog() {
		if (mProgressDialog != null) {
			if (mProgressDialog.isShowing())
				mProgressDialog.dismiss();

			mProgressDialog = null;
		}
	}

	/**
	 * 获取传入的，需要打开文件的类型进行匹配
	 * 
	 * @param type
	 */
	@SuppressLint("DefaultLocale")
	public boolean getFileMimeType(String flieType) {
//		// 获取后缀名前的分隔符"."在fName中的位置。
//		int dotIndex = flieType.lastIndexOf(".");
//		String end = flieType.substring((dotIndex+1), flieType.length()).toLowerCase();
		if (flieType == "")
			return false;
		// 在MIME和文件类型的匹配表中找到对应的MIME类型。
		for (int i = 0; i < MIME_MapTable.length; i++) {
			if (flieType.equals(MIME_MapTable[i][1])) {
				return true;
			}
		}
		return false;

	}

	/**
	 * 类型匹配键值数据
	 */
	private String[][] MIME_MapTable = { { ".doc", "application/msword" },
			{ ".docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document" },
			{ ".ppt", "application/vnd.ms-powerpoint" }, 
			{ ".pdf", "application/pdf" },
			{ ".pptx", "application/vnd.openxmlformats-officedocument.presentationml.presentation" }
	};

}
