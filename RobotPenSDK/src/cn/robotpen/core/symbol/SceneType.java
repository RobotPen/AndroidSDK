package cn.robotpen.core.symbol;
/**
 * 
 * @author Luis
 * @date 2016年1月17日 下午10:23:45
 *
 * Description
 */
public enum SceneType {
	/**未设置**/
	NOTHING(0),
	A4(1),
	A5(2),
	A4_horizontal(3),
	A5_horizontal(4),
	/**自定义**/
	CUSTOM(5),
	INCH_116(6),
	INCH_116_horizontal(7);
	
	private final int value;

    private SceneType(int value) {
        this.value = value;
    }

    public final int getValue() {
        return value;
    }
    
    /**
     * int转换为SceneType，如果溢出那么输出NOTHING
     * @param value 需要转换的int值
     * @return
     */
    public static SceneType toSceneType(int value){
    	if(value >= 0 && value < SceneType.values().length){
    		return SceneType.values()[value];
    	}else{
    		return NOTHING;
    	}
    }
}
