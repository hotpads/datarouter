package com.hotpads.datarouter.storage.key;

import java.util.Collection;

import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.util.core.CollectionTool;

public class KeyTool {

	public static String getWhereClauseDisjunction(Collection<? extends Key<? extends Databean>> keys){
		if(CollectionTool.isEmpty(keys)){ return null; }
		StringBuilder sb = new StringBuilder();
		int counter = 0;
		for(Key<? extends Databean> key : keys){
			if(counter > 0){
				sb.append(" or ");
			}
			sb.append("("+key.getSqlNameValuePairsEscapedConjunction()+")");
			++counter;
		}
		return sb.toString();
	}
	
}
