package com.hotpads.handler.admin;

import java.lang.Thread.State;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Maps;
import com.hotpads.handler.BaseHandler;
import com.hotpads.handler.mav.Mav;
import com.hotpads.handler.mav.imp.StringMav;
import com.hotpads.util.core.ListTool;
import com.hotpads.util.core.MapTool;
import com.hotpads.util.core.StringTool;
import com.hp.gagawa.java.Node;
import com.hp.gagawa.java.elements.A;
import com.hp.gagawa.java.elements.Button;
import com.hp.gagawa.java.elements.Div;
import com.hp.gagawa.java.elements.Form;
import com.hp.gagawa.java.elements.H4;
import com.hp.gagawa.java.elements.Input;
import com.hp.gagawa.java.elements.Label;
import com.hp.gagawa.java.elements.Pre;
import com.hp.gagawa.java.elements.Span;

public class StackTracesManagerHandler extends BaseHandler{
	private int counterTotal;
	private int counterNew;
	private int counterRunnable;
	private int counterBlocked;
	private int counterWating;
	private int counterTimedWaiting;
	private int counterTerminated;
	private HashMap<State,Integer> threadStateCounters;
	private String stateParam;
	private String wildcardParam;

	@Override
	@Handler
	protected Mav handleDefault(){
		counterTotal = 0;
		counterNew = 0;
		counterRunnable = 0;
		counterBlocked = 0;
		counterWating = 0;
		counterTimedWaiting = 0;
		counterTerminated = 0;
		threadStateCounters = MapTool.createHashMap();

		return getStackTraces();
	}

	@Handler
	public Mav getStackTraces(){
		Mav mav = new Mav("/jsp/admin/stackTraces.jsp");
		boolean showAll = params.optionalBoolean("showAll", false);
		 stateParam = params.optional("state", null);
		 wildcardParam = params.optional("wildcard", null);

		Map<Thread,StackTraceElement[]> sts = Thread.getAllStackTraces();
		Map<Thread,StackTraceElement[]> orderedSts = getMapWithComparatorThread();
		orderedSts.putAll(sts);

		Div container = new Div();
		Div description;
		int counter = 0;

		for(Thread thread : orderedSts.keySet()){
			incrementCounter(thread);
			if(stateParam == null || thread.getState().toString().equals(stateParam)){
				StringBuilder stackTraceBuilder = new StringBuilder();
				for(StackTraceElement ste : sts.get(thread)){
					stackTraceBuilder.append(ste.toString() + "<br />");
				}
				if(!StringTool.notEmpty(wildcardParam)
						|| stackTraceBuilder.toString().toLowerCase().contains(wildcardParam.toLowerCase())){
					++counter;
					String highlightedStackTrace = stackTraceBuilder.toString().replaceAll("hotpads",
							"<span style='color:red;'>hotpads</span>");
					if(StringTool.notEmpty(wildcardParam)){
						highlightedStackTrace = highlightedStackTrace.replaceAll(wildcardParam, "<span style='color:blue;'>"
								+ wildcardParam + "</span>");
					}
					boolean interestingThread = highlightedStackTrace.contains("hotpads");
					if(!showAll){
//						if(!interestingThread){
//							continue;
//						}
					}

					description = getThreadDescription(sts, thread, highlightedStackTrace, counter, showAll);
					container.appendChild(description);
				}
			}
		}

		saveStateCounters();
		mav.put("title", "StackTraces of " + request.getLocalAddr());
		A link = getLinkShowAll(showAll);

		Pre pre = new Pre();
		H4 counterTitle = new H4();
		counterTitle.appendText(counter + " total threads running");
		pre.appendChild(counterTitle);
		pre.appendChild(link);
		pre.setCSSClass("thread-filter");
		pre.appendChild(createSearchForm(wildcardParam));

		mav.put("contentJSP", pre.write() + container.write());
		return mav;
	}

	private void saveStateCounters(){
		threadStateCounters.put(State.BLOCKED, counterBlocked);
		threadStateCounters.put(State.NEW, counterNew);
		threadStateCounters.put(State.RUNNABLE, counterRunnable);
		threadStateCounters.put(State.TERMINATED, counterTerminated);
		threadStateCounters.put(State.TIMED_WAITING, counterTimedWaiting);
		threadStateCounters.put(State.WAITING, counterWating);

	}

	private void incrementCounter(Thread thread){
		switch(thread.getState()){
		case BLOCKED:
			counterBlocked++;
			break;
		case NEW:
			counterNew++;
			break;
		case RUNNABLE:
			counterRunnable++;
			break;
		case TERMINATED:
			counterTerminated++;
			break;
		case TIMED_WAITING:
			counterTimedWaiting++;
			break;
		case WAITING:
			counterWating++;
			break;
		default:
			break;
		}

	}

	private A getLinkShowAll(boolean showAll){
		A link = new A();
		if(showAll){
			link.setHref("?showAll=false");
			link.appendText("Hide All");
		}else{
			link.setHref("?showAll=true");
			link.appendText("Show All");
		}
		return link;
	}

	private List<Node> getAllLinkTypes(){
		List<Node> toReturn = ListTool.create();
		for(State state : Thread.State.values()){
			toReturn.add(getLinkType(state));
		}
		return toReturn;
	}

	private Label getLinkType(State state){
		Label label = new Label();
		label.setCSSClass("radio inline");
		label.appendText(state.toString()+ "("+threadStateCounters.get(state)+")");

		Input radio = new Input();
		radio.setType("radio");
		radio.setName("state");
		radio.setValue(state.toString());
		if(state.toString().equals(stateParam)){
			radio.setChecked("true");
		}
		label.appendChild(radio);
		
		return label;

	}

	private Form createSearchForm(String value){
		Form form = new Form("");
		form.setMethod("get");
		form.setAction(request.getContextPath() + "/datarouter/stackTraces");
		Button submit = new Button();
		submit.setType("submit");
		submit.appendText("Submit");
		submit.setCSSClass("btn btn-success");
		form.appendChild(getAllLinkTypes());
		form.appendChild(getSearchInput(value));
		form.appendChild(submit);
		return form;
	}

	private Input getSearchInput(String value){
		Input wildcard = new Input();
		wildcard.setType("text");
		wildcard.setName("wildcard");
		wildcard.setCSSClass("span2");
		wildcard.setId("wildcard");
		wildcard.setAttribute("placeholder", "Search");
		if(StringTool.notEmpty(value)){
			wildcard.setValue(value);
		}
		return wildcard;
	}

	private Div getThreadDescription(Map<Thread,StackTraceElement[]> sts, Thread thread, String highlightedStackTrace,
			int counter, boolean showAll){
		H4 title;
		Div header;
		Form form;
		Input hidden;
		Input submit;
		Span span;
		Div description = new Div();
		description.setCSSClass("thread-description");

		title = new H4();
		title.appendText(thread.getId() + " " + thread.toString());

		header = new Div();
		header.setCSSClass("header-thread-descritpion");

		form = new Form("");
		form.setMethod("post");
		form.setAction(request.getContextPath() + "/datarouter/stackTraces?submitAction=interruptThread");
		hidden = new Input();
		hidden.setType("hidden");
		hidden.setName("threadId");
		hidden.setValue("" + thread.getId());
		submit = new Input();
		submit.setType("submit");
		submit.setCSSClass("btn btn-success");
		submit.setAttribute("onclick", "window.confirm('are you sure?');");
		submit.setValue("Interrupt Thread");
		span = new Span();
		span.appendText("<b>State:</b> " + thread.getState().toString());

		form.appendChild(hidden);
		form.appendChild(submit);
		form.appendChild(span);
		header.appendChild(form);

		description.appendChild(title);
		description.appendChild(header);
		description.appendText(highlightedStackTrace);
		return description;
	}

	private Map<Thread,StackTraceElement[]> getMapWithComparatorThread(){
		return Maps.newTreeMap(new Comparator<Thread>(){
			@Override
			public int compare(Thread o1, Thread o2){
				if(o1 != null)
					return o1.toString().compareTo(o2.toString());
				else if(o2 != null)
					return 1;
				else
					return 0;
			}
		});
	}

	@Handler
	public Mav interruptThread(){
		Long threadId = params.requiredLong("threadId");
		Map<Thread,StackTraceElement[]> threadMap = Thread.getAllStackTraces();
		for(Thread thread : threadMap.keySet()){
			if(threadId.equals(thread.getId())){
				thread.interrupt();
				return new StringMav("thread " + threadId + " interrupted");
			}
		}
		return new StringMav("no thread found for threadId " + threadId);
	}
}
