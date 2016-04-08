package cn.robotpen.file.model;

import java.io.Serializable;
import java.util.List;

import cn.robotpen.file.symbol.ListType;

/**
 * 
 * @author Luis
 * @date 2016年3月28日 下午1:56:10
 *
 * Description
 */
@SuppressWarnings("serial")
public class ResponseRes implements Serializable {
	
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
	
	
	public List<ResFile> getItems() {
		return Items;
	}

	public void setItems(List<ResFile> items) {
		Items = items;
	}

	public String getMarker() {
		return Marker;
	}

	public void setMarker(String marker) {
		Marker = marker;
	}

	public ListType getType() {
		return Type;
	}

	public void setType(ListType type) {
		Type = type;
	}
	
}
