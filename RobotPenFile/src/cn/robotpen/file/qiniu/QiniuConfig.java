package cn.robotpen.file.qiniu;

import cn.robotpen.core.utils.StringUtil;

/**
 * 
 * @author Luis
 * @date 2016年3月25日 下午6:26:59
 *
 * Description
 */
public class QiniuConfig {
	public static String ACCESS_KEY;
	public static String SECRET_KEY;
	
    public final static String REMOTE_SERVICE_SERVER = "http://7xs4yq.com2.z0.glb.qiniucdn.com";
    
    //设置上传的空间
    public final static String BUCKET = "robotpen-file";
    
    public final static String RS_HOST = "rs.qbox.me";
    public final static String UP_HOST = "upload.qiniu.com";
    public final static String RSF_HOST = "rsf.qbox.me";
    public final static String PREFETCH_HOST = "iovip.qbox.me";
    public final static String API_HOST = "api.qiniu.com";

    /**
     * 七牛资源管理服务器地址
     */
    public final static String RS_DOMAIN = "http://"+ RS_HOST;
    
    /**
     * 七牛资源上传服务器地址.
     */
    public final static String UP_DOMAIN = "http://"+ UP_HOST;
    
    /**
     * 七牛资源列表服务器地址.
     */
    public final static String RSF_DOMAIN = "http://"+ RSF_HOST;

    public final static String PREFETCH_DOMAIN = "http://"+ PREFETCH_HOST;

    public final static String API_DOMAIN = "http://"+ API_HOST;
    
    /**
     * 获取Token
     * @param path
     * @return
     */
    public static String getToken(String path){
    	return getToken(path,"");
    }
    
    /**
     * 获取Token
     * @param path	请求路径
     * @param query 请求参数
     * @return
     */
    public static String getToken(String path,String query){
    	String encodeSign = StringUtil.hmac_sha1(SECRET_KEY, path+"\n"+query);
		String accessToken = ACCESS_KEY +":"+ encodeSign;
		return accessToken;
    }
}
