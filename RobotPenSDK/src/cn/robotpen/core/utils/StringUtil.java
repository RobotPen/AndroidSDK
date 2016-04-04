package cn.robotpen.core.utils;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import android.util.Base64;

/**
 * 
 * @author Luis
 * @date 2016年3月26日 上午11:50:58
 *
 * Description
 */
public class StringUtil {

	public static String hmac_sha1(String key, String datas){  
        String reString = "";  

        try{  
            byte[] data = key.getBytes("UTF-8");    
            //根据给定的字节数组构造一个密钥,第二参数指定一个密钥算法的名称     
            SecretKey secretKey = new SecretKeySpec(data, "HmacSHA1");
            //生成一个指定 Mac 算法 的 Mac 对象     
            Mac mac = Mac.getInstance("HmacSHA1");
            //用给定密钥初始化 Mac 对象     
            mac.init(secretKey);
                        
            byte[] text = datas.getBytes("UTF-8");      
            //完成 Mac 操作      
            byte[] text1 = mac.doFinal(text);     
            
            reString = Base64.encodeToString(text1, Base64.DEFAULT);  
  
        } catch (Exception e) {  
            // TODO: handle exception   
        }  
          
        return reString;  
    }  
	
	
	public static String printHexString(byte[] b) { 
		String result = "";
		for (int i = 0; i < b.length; i++) { 
			String hex = Integer.toHexString(b[i] & 0xFF); 
			if (hex.length() == 1) { 
				hex = '0' + hex; 
			}
			result += hex;
		} 
		return result;
	}
}
