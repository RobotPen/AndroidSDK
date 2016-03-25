package cn.robotpen.core.symbol;
/**
 * 电量状态
 * @author Luis
 * @date 2016年1月7日 上午10:23:49
 */
public enum BatteryState {
	/**
	 * 没有状态
	 */
	NOTHING(0),
	
	/**
	 * 低电量
	 */
	LOW(1),
	
	/**
	 * 良好
	 */
	GOOD(2);
	
	private final int value;

    private BatteryState(int value) {
        this.value = value;
    }

    public final int getValue() {
        return value;
    }
    
    /**
     * int转换为ConnectState，如果溢出那么输出NOTHING
     * @param value 需要转换的int值
     * @return
     */
    public static BatteryState toBatteryState(int value){
    	if(value >= 0 && value < BatteryState.values().length){
    		return BatteryState.values()[value];
    	}else{
    		return NOTHING;
    	}
    }
}
