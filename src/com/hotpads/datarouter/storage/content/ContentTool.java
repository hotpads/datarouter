package com.hotpads.datarouter.storage.content;

import com.hotpads.util.core.ClassTool;
import com.hotpads.util.core.ObjectTool;

public class ContentTool {

	public static boolean equalsContent(ContentHolder a, ContentHolder b){
		if(ClassTool.differentClass(a, b)){ return false; }
		if(ObjectTool.bothNull(a, b)){ return true; }
		if(ObjectTool.isOneNullButNotTheOther(a, b)){ return false; }
		return a.equalsContent(b);
	}
	
}
