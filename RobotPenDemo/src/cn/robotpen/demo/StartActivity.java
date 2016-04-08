package cn.robotpen.demo;

import cn.robotpen.core.services.PenService;
import cn.robotpen.core.symbol.Keys;
import cn.robotpen.file.model.ResFile;
import cn.robotpen.file.model.ResponseRes;
import cn.robotpen.file.qiniu.GetResourcesPort;
import cn.robotpen.file.qiniu.GetResourcesPort.OnGetResourcesResult;
import cn.robotpen.file.qiniu.QiniuConfig;
import cn.robotpen.file.symbol.FileType;
import com.qiniu.android.http.ResponseInfo;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.Toast;
import cn.robotpen.file.qiniu.UpLoadResourcesPort;
import cn.robotpen.file.qiniu.UpLoadResourcesPort.UploadCallBackResult;

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
	private GetResourcesPort mGetResourcesPort;
	private UpLoadResourcesPort mUpLoadResourcesPort;
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
		// QiniuConfig.ACCESS_KEY = "";
		// QiniuConfig.SECRET_KEY = "";

		QiniuConfig.ACCESS_KEY = "i2hg6Yvl2mo86rEdgdgifL8eKAjHTVeWbXp9ZAa4";
		QiniuConfig.SECRET_KEY = "ZQ6H3fFzcRPNeLwQIkVLyZCFYxhuRW4Ct5E1Zjld";
		
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

			mGetResourcesPort = new GetResourcesPort("10001", new OnGetResourcesResult() {
				@Override
				public void result(int arg0, ResponseRes arg1) {
					if (arg0 == GetResourcesPort.GET_SUCCESS) {
						for (int i = 0; i < arg1.Items.size(); i++) {
							if (arg1.Items.get(i).ChildRes != null && arg1.Items.get(i).getChildCount() == arg1.Items.get(i).getChildCountMax()) {
								getFileTest(arg1.Items.get(i));
								break;
							}
						}
					}
				}
			});
			mGetResourcesPort.getDirectory(FileType.PDF);
			break;

		case R.id.upload_But: // 测试上传资源
			initData();
			mUpLoadResourcesPort = new UpLoadResourcesPort(new UploadCallBackResult() {
				@Override
				public void result(ResponseInfo res) {
					Log.v(TAG, "onSuccess:" + res.toString());
					Toast.makeText(StartActivity.this, res.toString(), Toast.LENGTH_LONG).show();
				}
			});
			if (!SAVE_FILE_DIRECTORY.equals("") || SAVE_FILE_DIRECTORY != null) {
				//mUpLoadResourcesPort.upLoadResources(SAVE_FILE_DIRECTORY);
			}
			break;
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		StartActivity.this.finish();
	}

	private void getFileTest(ResFile file) {
		mGetResourcesPort.getFile(file);
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
