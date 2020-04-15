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
package io.datarouter.trace.web;

import static j2html.TagCreator.div;
import static j2html.TagCreator.join;
import static j2html.TagCreator.rawHtml;
import static j2html.TagCreator.span;
import static j2html.TagCreator.td;
import static j2html.TagCreator.tr;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.instrumentation.trace.TraceSpanDto;
import io.datarouter.instrumentation.trace.TraceThreadDto;
import io.datarouter.util.number.NumberFormatter;
import io.datarouter.util.string.StringTool;

//wrapper for BaseTraceThread to create a tree structure
public class TraceThreadGroup{
	private static final Logger logger = LoggerFactory.getLogger(TraceThreadGroup.class);

	private TraceThreadDto thread;
	private TraceThreadGroup parent;
	private SortedSet<TraceThreadGroup> children = new TreeSet<>(
			Comparator.comparing((TraceThreadGroup ttg) -> ttg.thread.getRunningDuration())
				.thenComparing(Comparator.comparing(ttg -> ttg.thread.getThreadId())));
	private SortedMap<TraceThreadDto,SortedSet<TraceSpanDto>> spansByThreadKey;


	public TraceThreadGroup(TraceThreadDto thread){
		this.thread = thread;
	}

	/*---------------------------- get/set ---------------------------------*/

	public Long getGroupId(){
		return thread.getThreadId();
	}

	public void setSpans(Collection<TraceSpanDto> spans){
		spansByThreadKey = getSpansByThreadKey(spans);
	}

	/*---------------------------- build ------------------------------------*/

	public boolean attemptToAddThread(TraceThreadDto newThread){
		if(thread.getThreadId() == newThread.getParentId()){
			TraceThreadGroup childGroup = new TraceThreadGroup(newThread);
			childGroup.parent = this;
			children.add(childGroup);
			return true;
		}
		for(TraceThreadGroup child : children){
			if(child.attemptToAddThread(newThread)){
				return true;
			}
			logger.warn("{}", newThread.getParentId());
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

	public List<TraceThreadDto> getOrderedThreads(){
		List<TraceThreadDto> outs = new ArrayList<>();
		appendGroup(outs, this);
		return outs;
	}

	private void appendGroup(List<TraceThreadDto> outs, TraceThreadGroup group){
		outs.add(group.thread);
		for(TraceThreadGroup child : group.children){
			appendGroup(outs, child);
		}
	}

	/*---------------------------- standard ---------------------------------*/

	@Override
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append(StringTool.repeat("-", numNestedLevels()));
		sb.append(thread.getName());
		sb.append(children);
		return sb.toString();
	}

	/*---------------------------- rendering --------------------------------*/

	public String getHtml(){
		StringBuilder sb = new StringBuilder();
		sb.append("<table style=\"width: 100%;\" class=\"table border\">");
		appendThreadGroupHtml(sb, this, 0);
		sb.append("</table>");
		return sb.toString();
	}

	//used in viewTrace.jsp
	public int getNumThreads(){
		return children.stream()
				.mapToInt(TraceThreadGroup::getNumThreads)
				.sum() + 1;
	}

	private void appendThreadGroupHtml(StringBuilder sb, TraceThreadGroup group, int leafNum){
		appendLeafThreadHtml(sb, group.thread, leafNum);
		int childNumZeroBased = 0;
		for(TraceThreadGroup child : group.children){
			appendThreadGroupHtml(sb, child, childNumZeroBased);
			++childNumZeroBased;
		}
	}

	private void appendLeafThreadHtml(StringBuilder sb, TraceThreadDto thread, int leafNum){
		var tr = tr(
				td(leafNum + ") " + thread.getName() + " " + thread.getHostThreadName()),
				td(NumberFormatter.addCommas(thread.getTotalDuration()) + "ms").withStyle("text-align: right;"))
				.withStyle("font-weight: bold;");
		sb.append(tr.render());

		if(StringTool.notEmpty(thread.getInfo())){
			var div = div(thread.getInfo())
					.withClass("threadInfo")
					.withStyle("margin:0px 0px 0px 40px; font-weight:normal;");
			sb.append(div.render());
		}
		Set<TraceSpanDto> spans = spansByThreadKey == null
				? Collections.emptySet()
				: spansByThreadKey.getOrDefault(thread, new TreeSet<>());
		Map<Integer,List<TraceSpanDto>> spanByParentSequenceId = spans.stream()
				.collect(Collectors.groupingBy(TraceSpanDto::getParentSequenceOrMinusOne));
		sb.append(buildSubSpans(new TimeDto(thread), spanByParentSequenceId.get(-1), spanByParentSequenceId, 0));
	}

	private static StringBuilder buildSubSpans(TimeDto parentSpan, List<TraceSpanDto> spans,
			Map<Integer,List<TraceSpanDto>> spanByParentSequenceId, int indentation){
		StringBuilder sb = new StringBuilder();
		if(spans == null){
			return sb;
		}
		if(parentSpan.queued > 0){
			var tr = tr(join(makeFirstCell(parentSpan.queued, "in queue", indentation).toString(), td()));
			sb.append(tr.render());
		}
		Long prevEnd = null;
		for(TraceSpanDto span : spans){
			if(prevEnd == null){
				prevEnd = parentSpan.created + parentSpan.queued;
			}
			// interspan
			long interspanDuration = span.getCreated() - prevEnd;
			if(interspanDuration != 0){
				var tr = tr(join(makeFirstCell(interspanDuration, "", indentation).toString(), td()))
						.withStyle("background-color:#f9f9f9");
				sb.append(tr.render());
			}
			// span
			String info = Optional.ofNullable(span.getInfo())
					.orElse("");
			var tr = tr(join(makeFirstCell(span.getDuration(), span.getName(), indentation).toString(), td(info)));
			sb.append(tr.render());
			TimeDto dto = new TimeDto(span);
			sb.append(buildSubSpans(dto, spanByParentSequenceId.get(span.getSequence()), spanByParentSequenceId,
					indentation + 1));
			prevEnd = span.getCreated() + span.getDuration();
		}
		// last interspan
		long lastInterspanDuration = parentSpan.created + parentSpan.duration - prevEnd;
		if(lastInterspanDuration != 0){
			var tr = tr(join(makeFirstCell(lastInterspanDuration, null, indentation).toString(), td()))
					.withStyle("background-color:#f9f9f9");
			sb.append(tr.render());
		}
		return sb;
	}

	private static StringBuilder makeFirstCell(long durationMs, String name, int indentation){
		StringBuilder sb = new StringBuilder()
				.append("<td style=\"white-space: nowrap;\">");

		for(int i = 0; i < indentation; i++){
			var span = span(rawHtml("&nbsp"))
					.withStyle("border-right:solid 0px grey;"
							+ "margin-left:20px;"
							+ "margin-right:9px;"
							+ "display:inline-block;");
			sb.append(span.render());
		}

		var span = span(NumberFormatter.addCommas(durationMs) + " ms")
				.withStyle("width:50px; display:inline-block;");
		return sb.append(span.render())
				.append(name)
				.append("</td>");
	}

	public static TraceThreadGroup create(Collection<TraceThreadDto> threads, TraceThreadDto fakeThread){
		TraceThreadGroup rootGroup = null;
		for(TraceThreadDto thread : threads){
			if(rootGroup == null){
				if(thread.getParentId() != null){
					// build a fake for corrupted data
					thread = fakeThread;
				}
				rootGroup = new TraceThreadGroup(thread);
			}else{
				rootGroup.attemptToAddThread(thread);
			}
		}
		return rootGroup;
	}

	public SortedMap<TraceThreadDto,SortedSet<TraceSpanDto>> getSpansByThreadKey(Collection<TraceSpanDto> spans){
		SortedMap<TraceThreadDto,SortedSet<TraceSpanDto>> out = new TreeMap<>(
				Comparator.comparing(TraceThreadDto::getThreadId));
		for(TraceSpanDto span : spans){
			var threadKey = new TraceThreadDto(span.getTraceId(), span.getThreadId(), null, null, null, null, null);
			if(out.get(threadKey) == null){
				out.put(threadKey, new TreeSet<>(Comparator.comparing(TraceSpanDto::getSequence)));
			}
			out.get(threadKey).add(span);
		}
		return out;
	}

	private static class TimeDto{

		private final long created;
		private final long queued;
		private final long duration;

		public TimeDto(TraceSpanDto span){
			this.created = span.getCreated();
			this.queued = 0;
			this.duration = span.getDuration();
		}

		public TimeDto(TraceThreadDto thread){
			this.created = thread.getCreated();
			this.queued = thread.getQueuedDuration();
			this.duration = thread.getTotalDuration();
		}

	}
}
