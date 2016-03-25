package cn.robotpen.core.symbol;
/**
 * 录制等级
 * @author Luis
 * @date 2016年1月12日 上午2:34:56
 *
 * Description
 */
public class RecordLevel {
	/**标准320p 5fps**/
	public static final int level_2 = 2;
	/**标准320p 10fps**/
	public static final int level_3 = 3;
	/**标准320p 20fps**/
	public static final int level_4 = 4;
	
	/**标准480p 2fps**/
	public static final int level_11 = 11;
	/**标准480p 5fps**/
	public static final int level_12 = 12;
	/**标准480p 10fps**/
	public static final int level_13 = 13;
	/**标准480p 20fps**/
	public static final int level_14 = 14;

	/**清晰720p 2fps**/
	public static final int level_21 = 21;
	/**清晰720p 5fps**/
	public static final int level_22 = 22;
	/**清晰720p 10fps**/
	public static final int level_23 = 23;
	/**清晰720p 20fps**/
	public static final int level_24 = 24;
	
	/**
     * 获取视频Rate
     * @return
     */
    public static int getFrameRate(int level){
        switch (level){
	        case level_2:
	            return 5;
	        case level_3:
	            return 10;
	        case level_4:
	            return 20;
            
	        case level_11:
	            return 2;
            case level_12:
                return 5;
            case level_13:
                return 10;
            case level_14:
                return 20;
                
	        case level_21:
	            return 2;
            case level_22:
                return 5;
            case level_23:
                return 10;
            case level_24:
                return 20;
                
            default:
                return 10;
        }
    }

    /**
     * 获取视频质量
     * @return
     */
    public static int getFrameProgressive(int level){
    	if(level < 10){
    		return 320;
    	}else if(level < 20){
    		return 480;
    	}else if(level < 30){
    		return 720;
    	}else{
    		return 480;
    	}
    }
}
