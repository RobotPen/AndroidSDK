package cn.robotpen.file.symbol;
/**
 * 
 * @author Luis
 * @date 2016年3月25日 下午7:27:09
 *
 * Description
 */
public enum FileType {
	ALL,
	PDF,
	PPT,
	WORD;
	
	public static FileType getType(String type){
		if("pdf".equals(type)){
			return PDF;
		}else if("doc".equals(type) || "docx".equals(type)){
			return WORD;
		}else if("ppt".equals(type)||"pptx".equals(type)){
			return PPT;
		}
		return ALL;
	}
}
