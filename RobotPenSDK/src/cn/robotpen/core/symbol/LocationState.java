package cn.robotpen.core.symbol;
/**
 * 
 * @author Luis
 * @date 2016年1月17日 下午6:23:56
 *
 * Description
 */
public enum LocationState {
	/** 已经确定第一个坐标 **/
	FirstComp,
	/** 已经完成定位 **/
	SecondComp,
	/** 没有定位 **/
	DontLocation,
	/** 定位范围过小 **/
	LocationSmall
}
