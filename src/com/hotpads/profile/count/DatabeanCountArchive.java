package com.hotpads.profile.count;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import com.hotpads.datarouter.node.op.SortedStorageNode;
import com.hotpads.profile.count.key.CountKey;
import com.hotpads.util.core.ListTool;
import com.hotpads.util.core.MapTool;

public class DatabeanCountArchive implements CountArchive{
	
	String serverName;
	
	protected SortedStorageNode<CountKey,Count> node;
	
	public DatabeanCountArchive(SortedStorageNode<CountKey,Count> node,
			String serverName){
		this.node = node;
		this.serverName = serverName;
	}

	@Override
	public List<Integer> getCounts(long period, long startTimeMs, String name){
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> listCounters(){
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void saveCounts(long periodMs, long startTimeMs, Map<String,AtomicLong> countByKey){
		List<Count> toSave = ListTool.create();
		for(Map.Entry<String,AtomicLong> entry : MapTool.nullSafe(countByKey).entrySet()){
			if(entry.getValue()==null || entry.getValue().equals(0L)){ continue; }
			toSave.add(new Count("server", serverName, entry.getKey(), periodMs, startTimeMs, entry.getValue().get()));
		}
		node.putMulti(toSave, null);
	}

	
	
}
