package com.hotpads.trace;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;

import com.google.common.collect.Sets;
import com.hotpads.trace.TraceThread.TraceThreadComparator;
import com.hotpads.trace.key.TraceThreadKey;
import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.IterableTool;
import com.hotpads.util.core.ListTool;
import com.hotpads.util.core.ObjectTool;
import com.hotpads.util.core.SetTool;
import com.hotpads.util.core.StringTool;
	
//wrapper for TraceThread to create a tree structure
public class TraceThreadGroup{
	TraceThread thread;
	TraceThreadGroup parent;
	SortedSet<TraceThreadGroup> children = Sets.newTreeSet(new TraceThreadGroupComparator());

	public TraceThreadGroup(TraceThread thread){
		this.thread = thread;
	}
	
	public boolean attemptToAddThread(TraceThread newThread){
		if(ObjectTool.equals(thread.getId(), newThread.getParentId())){
			TraceThreadGroup childGroup = new TraceThreadGroup(newThread);
			childGroup.parent = this;
			children.add(childGroup);
			return true;
		}
		for(TraceThreadGroup child : children){
			if(child.attemptToAddThread(newThread)){ return true; }
		}
		return false;//must be missing a link to the root
	}
	
	public boolean isRoot(){
		return thread.getParentId()==null;
	}
	
	//1 means insert 1 tab
	public int numNestedLevels(){
		TraceThreadGroup treeClimber = this;
		int branchesClimbed = 0;
		while(true){
			if(treeClimber.isRoot()){ return branchesClimbed; }
			treeClimber = treeClimber.parent;
			++branchesClimbed;
		}
	}
	
	public List<TraceThread> getOrderedThreads(){
		List<TraceThread> outs = ListTool.create();
		appendGroup(outs, this);
		return outs;
	}
	
	protected void appendGroup(List<TraceThread> outs, TraceThreadGroup group){
		outs.add(group.thread);
		for(TraceThreadGroup child : group.children){
			appendGroup(outs, child);
		}
	}
	
	/***************************** standard *****************************/
	
	@Override
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append(StringTool.repeat("-", numNestedLevels()));
		sb.append(thread.getName());
//			sb.append("{\n");
		sb.append(children);
//			sb.append("}");
		return sb.toString();
	}
	
	
	/*************************** static **********************************/
	
	public static class TraceThreadGroupComparator implements Comparator<TraceThreadGroup>{
		@Override
		public int compare(TraceThreadGroup a, TraceThreadGroup b){
			return new TraceThreadComparator().compare(a.thread, b.thread);
		}
	}
	
	public static TraceThreadGroup create(Collection<TraceThread> threads){
		TraceThreadGroup rootGroup = null;
		Set<TraceThreadKey> placedThreadKeys = SetTool.createHashSet();
		while(CollectionTool.size(placedThreadKeys) < CollectionTool.size(threads)){
			boolean placedAtLeastOneThreadInThisLoop = false;
			for(TraceThread thread : IterableTool.nullSafe(threads)){
				if(placedThreadKeys.contains(thread.getKey())){ continue; }//already got it
				boolean added = false;
				if(rootGroup==null){
					if(thread.getParentId()==null){
						rootGroup = new TraceThreadGroup(thread);
						added = true;
					}
				}else{
					added = rootGroup.attemptToAddThread(thread);
				}
				if(added){
					placedThreadKeys.add(thread.getKey());
					placedAtLeastOneThreadInThisLoop = true;
				}
			}
			if(!placedAtLeastOneThreadInThisLoop){
				throw new RuntimeException("could not construct thread hierarchy");
			}
		}
		return rootGroup;
	}
	
	/************************** get/set ***********************************/

	public Long getGroupId(){
		return thread==null?0:thread.getId();
	}
	
	
}