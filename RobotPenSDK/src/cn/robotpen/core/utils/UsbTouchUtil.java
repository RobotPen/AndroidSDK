package cn.robotpen.core.utils;

import java.util.ArrayList;
import java.util.List;

import cn.robotpen.core.model.PointObject;

/**
 * 
 * @author Luis
 * @date 2016年2月8日 下午1:34:30
 *
 * Description
 */
public class UsbTouchUtil extends PenDataUtil{
	/**
	 * 获取点对象
	 * @param usbData usb数据
	 * @return
	 */
	public static List<PointObject> getPointList(byte[] usbData){
		List<PointObject> list = new ArrayList<PointObject>();
		if(usbData != null && usbData.length > 0){
			fillPointList(list,usbData);
		}
		return list;
	}
	
	/**
	 * 填充点坐标集合
	 * @param list
	 * @param penData	笔数据
	 */
	private static void fillPointList(List<PointObject> list,byte[] penData){
		byte[] byX = new byte[2];
		byte[] byY = new byte[2];
		byte[] byPressure = new byte[2];
		
		PointObject item;
		for(int i = 0;i < penData.length;i = i + TOUCH_DATA_VALID_LENGTH){
			if(isPenData(penData[i])){
				byX[0] = penData[i+2];
				byX[1] = penData[i+3];
				byY[0] = penData[i+4];
				byY[1] = penData[i+5];
				byPressure[0] = penData[i+6];
				byPressure[1] = penData[i+7];
				
				item = new PointObject();
				item.originalX = (short)(byteToshort(byY) - PointObject.VALUE_INCH_116_WIDTH / 2);
				item.originalY = byteToshort(byX);
				item.isRoute = isPenRoute(penData,i);
				item.isSw1 = isPenSw1(penData,i);
				item.isSw2 = isPenSw2(penData,i);
				item.pressure = getPressure(byteToshort(byPressure));
				list.add(item);
			}
		}
	}
	
	/**
	 * 获取0.5~1.5范围的压力
	 * @param value
	 * @return
	 */
	private static float getPressure(short value){
		return (float)value / 1023f + 0.5f;
	}
	
	/**
	 * 判断是否是书写笔迹
	 * @param data
	 * @param i
	 * @return
	 */
	private static boolean isPenRoute(byte[] data,int i){
		boolean result = false;
		if(data[i + 1] == 0x11)result = true;
		return result;
	}
	
	/**
	 * 判断是否按下按键1
	 * @param data
	 * @param i
	 * @return
	 */
	private static boolean isPenSw1(byte[] data,int i){
		boolean result = false;
		if(data[i + 1] == 0x13)result = true;
		return result;
	}
	
	/**
	 * 判断是否按下按键2
	 * @param data
	 * @param i
	 * @return
	 */
	private static boolean isPenSw2(byte[] data,int i){
		boolean result = false;
		if(data[i + 1] == 0x30)result = true;
		return result;
	}
	
	/**
	 * 检查是否是笔数据
	 * @param data
	 * @param i
	 * @return
	 */
	private static boolean isPenData(byte b1){
		boolean result = false;
		if(b1 == 0x02){
			result = true;
		}
		return result;
	}
}
