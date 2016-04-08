package cn.robotpen.file.model;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * 
 * @author Luis
 * @date 2016年3月28日 下午2:04:11
 *
 * Description
 */
public class ResFile {
	
	/**
	 * 资源文件名称
	 */
	public String Key;

	/**
	 * 资源内容的大小，单位：字节
	 */
	public long FileSize;
	
	/**
	 * 上传时间
	 */
	public long PutTime;
	
	public String Hash;
	
	/**
	 * 转换完成后的目录，如果为NULL那么表示还未转换完成
	 */
	public String DecodePath;
	
	/**
	 * 资源内容的MIME类型
	 */
	public String MimeType;
	
	/**
	 * 子资源
	 */
	public List<ResFile> ChildRes;
	
	public ResFile(){
		
	}
	
	public ResFile(JSONObject jsonObj) throws JSONException{
		Key = jsonObj.getString("key");
		PutTime = jsonObj.getLong("putTime");
		Hash = jsonObj.getString("hash");
		MimeType = jsonObj.getString("mimeType");
		FileSize = jsonObj.getLong("fsize");
	}
	
	/**
	 * copy参数
	 * @param value
	 */
	public void copy(ResFile value){
		Key = value.Key;
		PutTime = value.PutTime;
		Hash = value.Hash;
		MimeType = value.MimeType;
		FileSize = value.FileSize;
	}
	
	/**
	 * 添加子资源
	 * @param file
	 */
	public void addChildResFile(ResFile file){
		if(ChildRes == null){
			ChildRes = new ArrayList<ResFile>();
		}
		ChildRes.add(file);
	}
	
	/**
	 * 获取子资源总数
	 */
	public int getChildCountMax(){
		int result = 0;
		if(ChildRes != null){
			ResFile item;
			for(int i = 0;i < ChildRes.size();i++){
				item = ChildRes.get(i);
				String[] sp = item.Key.split("/");
				try{
					int num = Integer.valueOf(sp[sp.length - 1].replace(".jpg", ""));
					if(num > result)result = num;
				}catch(Exception e){
					e.printStackTrace();
				}
			}
		}
		return result;
	}

	/**
	 * 获取当前子资源数量
	 * @return
	 */
	public int getChildCount(){
		if(ChildRes != null)return ChildRes.size();
		return 0;
	}
	
	/**
	 * 获取文件名称
	 * @return
	 */
	public String getName(){
		String[] arr = Key.split("/");
		String name = arr[arr.length - 1];
		return name;
	}
	
	/**
	 * 获取文件后缀
	 * @return
	 */
	public String getSuffix(){
		String suffix = "";
		String[] arr = getName().split("\\.");
		if(arr.length > 0)suffix = arr[arr.length - 1];
		return "."+suffix;
	}
}
