/**
 * Copyright © 2009 HotPads (admin@hotpads.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.datarouter.storage.profile.trace;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;

import com.google.common.collect.Sets;

import io.datarouter.storage.profile.trace.key.TraceThreadKey;
import io.datarouter.util.ComparableTool;
import io.datarouter.util.collection.CollectionTool;
import io.datarouter.util.collection.MapTool;
import io.datarouter.util.iterable.IterableTool;
import io.datarouter.util.number.NumberFormatter;
import io.datarouter.util.string.StringTool;

//wrapper for TraceThread to create a tree structure
public class TraceThreadGroup{
	private TraceThread thread;
	private TraceThreadGroup parent;
	private SortedSet<TraceThreadGroup> children = Sets.newTreeSet(Collections.reverseOrder(
			new TraceThreadGroupSlownessComparator()));
	private SortedMap<TraceThreadKey,SortedSet<TraceSpan>> spansByThreadKey;

	public TraceThreadGroup(TraceThread thread){
		this.thread = thread;
	}

	public void setSpans(Collection<TraceSpan> spans){
		spansByThreadKey = TraceSpan.getByThreadKey(spans);
	}

	public boolean attemptToAddThread(TraceThread newThread){
		if(Objects.equals(thread.getId(), newThread.getParentId())){
			TraceThreadGroup childGroup = new TraceThreadGroup(newThread);
			childGroup.parent = this;
			children.add(childGroup);
			return true;
		}
		for(TraceThreadGroup child : children){
			if(child.attemptToAddThread(newThread)){
				return true;
			}
		}
		return false;// must be missing a link to the root
	}

	public boolean isRoot(){
		return thread.getParentId() == null;
	}

	// 1 means insert 1 tab
	public int numNestedLevels(){
		TraceThreadGroup treeClimber = this;
		int branchesClimbed = 0;
		while(true){
			if(treeClimber.isRoot()){
				return branchesClimbed;
			}
			treeClimber = treeClimber.parent;
			++branchesClimbed;
		}
	}

	public List<TraceThread> getOrderedThreads(){
		List<TraceThread> outs = new ArrayList<>();
		appendGroup(outs, this);
		return outs;
	}

	private void appendGroup(List<TraceThread> outs, TraceThreadGroup group){
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
		sb.append(children);
		return sb.toString();
	}

	/***************************** rendering *****************************/

	//used in viewTrace.jsp
	public String getHtml(){
		StringBuilder sb = new StringBuilder();
		appendThreadGroupHtml(sb, this, 0);
		return sb.toString();
	}

	//used in viewTrace.jsp and thread.jsp
	public int getNumThreads(){
		return children.stream().mapToInt(TraceThreadGroup::getNumThreads).sum() + 1;
	}

	private void appendThreadGroupHtml(StringBuilder sb, TraceThreadGroup group, int leafNum){
		String divStyle = "margin:3px 6px 3px 40px;border:1px solid #aaa;";
		sb.append("<div style=\"" + divStyle + "\">");
		appendLeafThreadHtml(sb, group.thread, leafNum);
		int childNumZeroBased = 0;
		for(TraceThreadGroup child : group.children){
			appendThreadGroupHtml(sb, child, childNumZeroBased);
			++childNumZeroBased;
		}
		sb.append("</div>");
	}

	private void appendLeafThreadHtml(StringBuilder sb, TraceThread thread, int leafNum){
		sb.append("<div style=\"font-weight:bold;margin:5px;\"");
		sb.append("<span>" + leafNum + ") " + thread.getName() + "</span>");
		String floatingDivStyle = "float:right;";
		sb.append("<div style=\"" + floatingDivStyle + "\">");
		String spanStyle = "float:right;text-align:right;width:100px;";
		sb.append("<span style=\"" + spanStyle + "\">" + NumberFormatter.addCommas(thread.getRunningDuration())
				+ "ms</span>");
		sb.append("<span style=\"" + spanStyle + "\">" + NumberFormatter.addCommas(thread.getQueuedDuration())
				+ "ms</span>");
		sb.append("</div>");
		String threadInfoStyle = "margin:0px 0px 0px 40px;font-weight:normal;";
		if(StringTool.notEmpty(thread.getInfo())){
			sb.append("<div class=\"threadInfo\" style=\"" + threadInfoStyle + "\">" + thread.getInfo() + "</div>");
		}
		sb.append("</div>");
		String divStyle = "margin:0px 0px 5px 80px;";
		sb.append("<div style=\"" + divStyle + "\">");
		sb.append("<table class=\"table table-striped table-bordered table-collapse sortable\">");
		for(TraceSpan span : IterableTool.nullSafe(MapTool.nullSafe(spansByThreadKey).get(thread.getKey()))){
			sb.append("<tr>");
			sb.append("<td>" + span.getSequence() + "</td>");
			sb.append("<td>" + span.getParentSequence() + "</td>");
			sb.append("<td>" + span.getDuration() + " ms</td>");
			sb.append("<td>" + span.getName() + "</td>");
			sb.append("<td>" + span.getInfo() + "</td>");
			sb.append("</tr>");
		}
		sb.append("</table>");
		sb.append("</div>");
	}

	/*************************** static **********************************/

	public static class TraceThreadGroupSlownessComparator implements Comparator<TraceThreadGroup>{
		@Override
		public int compare(TraceThreadGroup groupA, TraceThreadGroup groupB){
			return ComparableTool.nullFirstCompareTo(groupA.thread.getTotalDuration(), groupB.thread
					.getTotalDuration());
		}
	}

	public static TraceThreadGroup create(Collection<TraceThread> threads){
		TraceThreadGroup rootGroup = null;
		Set<TraceThreadKey> placedThreadKeys = new HashSet<>();
		while(CollectionTool.size(placedThreadKeys) < CollectionTool.size(threads)){
			boolean placedAtLeastOneThreadInThisLoop = false;
			for(TraceThread thread : IterableTool.nullSafe(threads)){
				if(placedThreadKeys.contains(thread.getKey())){// already got it
					continue;
				}
				boolean added = false;
				if(rootGroup == null){
					if(thread.getParentId() == null){
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
				return rootGroup;// won't be complete, but might as well display what we have
			}
		}
		return rootGroup;
	}

	/************************** get/set ***********************************/

	public Long getGroupId(){
		return thread == null ? 0 : thread.getId();
	}

}