package cn.robotpen.file.model;

import java.util.List;

import cn.robotpen.file.symbol.ListType;

/**
 * 
 * @author Luis
 * @date 2016年3月28日 下午1:56:10
 *
 * Description
 */
public class ResponseRes {
	
	/**
	 * 资源列队
	 */
	public List<ResFile> Items;
	
	/**
	 * 有剩余条目则返回非空字符串，作为下一次列举的参数传入。如果没有剩余条目则返回空字符串。
	 */
	public String Marker;
	
	/**
	 * 当前资源列队类型
	 */
	public ListType Type;
}
