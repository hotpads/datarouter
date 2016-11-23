package com.hotpads.handler.admin;

import java.lang.Thread.State;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.inject.Inject;

import com.hotpads.datarouter.config.DatarouterProperties;
import com.hotpads.datarouter.util.core.DrMapTool;
import com.hotpads.handler.BaseHandler;
import com.hotpads.handler.mav.Mav;
import com.hotpads.handler.mav.imp.StringMav;
import com.hp.gagawa.java.Node;
import com.hp.gagawa.java.elements.Button;
import com.hp.gagawa.java.elements.Div;
import com.hp.gagawa.java.elements.Form;
import com.hp.gagawa.java.elements.H4;
import com.hp.gagawa.java.elements.Input;
import com.hp.gagawa.java.elements.Label;
import com.hp.gagawa.java.elements.Pre;
import com.hp.gagawa.java.elements.Span;

public class StackTracesManagerHandler extends BaseHandler{

	@Inject
	private DatarouterProperties datarouterProperties;

	@Override
	protected Mav handleDefault(){
		Mav mav = new Mav("/jsp/admin/stackTraces.jsp");
		Optional<State> state = params.optional("state").map(State::valueOf);
		Optional<String> optWildcard = params.optionalNotEmpty("wildcard");

		Map<Thread,StackTraceElement[]> sts = Thread.getAllStackTraces();
		List<Thread> orderedThreads = new ArrayList<>(sts.keySet());
		Collections.sort(orderedThreads, Comparator.comparing(Thread::getName));

		Div container = new Div();
		int counter = 0;
		Map<State,Long> threadStateCounters = new HashMap<>();

		for(Thread thread : orderedThreads){
			DrMapTool.increment(threadStateCounters, thread.getState());
			if(state.isPresent() && !state.get().equals(thread.getState())){
				continue;
			}
			String stackTrace = Arrays.stream(sts.get(thread))
					.map(StackTraceElement::toString)
					.collect(Collectors.joining("<br />"));
			if(optWildcard.isPresent() && !stackTrace.toLowerCase().contains(optWildcard.get().toLowerCase())){
				continue;
			}
			++counter;
			if(optWildcard.isPresent()){
				stackTrace = stackTrace.replaceAll("(?i)" + optWildcard.get(), "<span style='color:blue;'>$0</span>");
			}
			stackTrace = stackTrace.replaceAll("hotpads", "<span style='color:red;'>hotpads</span>");

			container.appendChild(getThreadDescription(thread, stackTrace));
		}

		mav.put("title", "StackTraces of " + datarouterProperties.getServerName() + " at " + new Date());

		Pre pre = new Pre();
		H4 counterTitle = new H4();
		counterTitle.appendText(counter + " total threads running");
		pre.appendChild(counterTitle);
		pre.setCSSClass("thread-filter");
		pre.appendChild(createSearchForm(optWildcard, state, threadStateCounters));

		mav.put("contentJSP", pre.write() + container.write());
		return mav;
	}

	private List<Node> getAllLinkTypes(Optional<State> stateParam, Map<State,Long> threadStateCounters){
		List<Node> toReturn = new ArrayList<>();
		for(State state : Thread.State.values()){
			toReturn.add(getLinkType(state, stateParam, threadStateCounters));
		}
		return toReturn;
	}

	private Label getLinkType(State state, Optional<State> stateParam, Map<State,Long> threadStateCounters){
		Label label = new Label();
		label.setCSSClass("radio inline");
		label.appendText(state.toString() + "(" + threadStateCounters.getOrDefault(state, 0L) + ")");

		Input radio = new Input();
		radio.setType("radio");
		radio.setName("state");
		radio.setValue(state.toString());
		if(stateParam.map(state::equals).orElse(false)){
			radio.setChecked("true");
		}
		label.appendChild(radio);
		return label;

	}

	private Form createSearchForm(Optional<String> value, Optional<State> stateParam, Map<State,Long> stateCounters){
		Form form = new Form("");
		form.setMethod("get");
		form.setAction(request.getContextPath() + "/datarouter/stackTraces");
		Button submit = new Button();
		submit.setType("submit");
		submit.appendText("Submit");
		submit.setCSSClass("btn btn-success");
		form.appendChild(getAllLinkTypes(stateParam, stateCounters));
		form.appendChild(getSearchInput(value));
		form.appendChild(submit);
		return form;
	}

	private Input getSearchInput(Optional<String> value){
		Input wildcard = new Input();
		wildcard.setType("text");
		wildcard.setName("wildcard");
		wildcard.setCSSClass("span2");
		wildcard.setId("wildcard");
		wildcard.setAttribute("placeholder", "Search");
		value.ifPresent(wildcard::setValue);
		return wildcard;
	}

	private Div getThreadDescription(Thread thread, String highlightedStackTrace){
		Input hidden = new Input()
				.setType("hidden")
				.setName("threadId")
				.setValue(String.valueOf(thread.getId()));
		Input submit = new Input()
				.setType("submit")
				.setCSSClass("btn btn-success")
				.setValue("Interrupt Thread");
		submit.setAttribute("onclick", "window.confirm('are you sure?');");
		Span span = new Span().appendText("<b>State:</b> " + thread.getState());
		Form form = new Form(request.getContextPath() + "/datarouter/stackTraces?submitAction=interruptThread")
				.setMethod("post")
				.appendChild(hidden)
				.appendChild(submit)
				.appendChild(span);
		H4 title = new H4().appendText(thread.getId() + " " + thread);
		Div header = new Div()
				.setCSSClass("header-thread-descritpion")
				.appendChild(form);
		return new Div()
				.setCSSClass("thread-description")
				.appendChild(title)
				.appendChild(header)
				.appendText(highlightedStackTrace);
	}

	@Handler
	public Mav interruptThread(){
		Long threadId = params.requiredLong("threadId");
		for(Thread thread : Thread.getAllStackTraces().keySet()){
			if(threadId.equals(thread.getId())){
				thread.interrupt();
				return new StringMav("thread " + threadId + " interrupted");
			}
		}
		return new StringMav("no thread found for threadId " + threadId);
	}
}
