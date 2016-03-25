package cn.robotpen.demo;

import cn.robotpen.core.PenApplication;

/**
 * 
 * @author Xiaoz
 * @date 2015年6月12日 上午11:39:48
 *
 * Description
 */
public class RobotPenApplication extends PenApplication{
    private static RobotPenApplication instance = null;
    
	 @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }

    public static RobotPenApplication getInstance() {
        return instance;
    }
}
