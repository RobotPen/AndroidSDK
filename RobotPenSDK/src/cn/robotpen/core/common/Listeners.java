package cn.robotpen.core.common;

import java.util.HashMap;

import cn.robotpen.core.model.DeviceObject;
import cn.robotpen.core.model.PointObject;
import cn.robotpen.core.symbol.ConnectState;
import cn.robotpen.core.symbol.LocationState;

/**
 * 监听集合
 * @author Luis
 * @date 2015年12月21日 下午8:30:02
 *
 * Description
 */
public class Listeners {
	
	/**
	 * 扫描设备监听
	 */
	public interface OnScanDeviceListener {
		/**
		 * 发现设备
		 * @param device
		 */
		void find(DeviceObject device);
		
		/**
		 * 扫描完成
		 * @param list
		 */
		void complete(HashMap<String,DeviceObject> list);
	}
	
	/**
	 * 连接设备监听
	 */
	public interface OnConnectStateListener{
		/**
		 * 状态更改
		 * @param state
		 */
		void stateChange(String address,ConnectState state);
	}
	
	/**
	 * 笔坐标更改监听
	 */
	public interface OnPointChangeListener{
		/**
		 * 坐标更改
		 * @param point
		 */
		void change(PointObject point);
	}
	
	/**手势监听**/
	public interface OnPenGestureListener{
		
		/**长按**/
		void longClick(PointObject point);
	}
	
	/**
	 * 坐标定点监听
	 *
	 */
	public interface OnFixedPointListener{
		/**
		 * 定位状态
		 * @param first		第一个点
		 * @param second	第二个点
		 * @param state		定位状态
		 */
		void location(PointObject first,PointObject second,LocationState state);
	}
}
