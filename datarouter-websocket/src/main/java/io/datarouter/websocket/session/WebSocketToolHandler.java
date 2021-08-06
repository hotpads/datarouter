/*
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
package io.datarouter.websocket.session;

import static j2html.TagCreator.div;
import static j2html.TagCreator.h3;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.atomic.LongAdder;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.scanner.Scanner;
import io.datarouter.storage.config.DatarouterProperties;
import io.datarouter.util.number.NumberFormatter;
import io.datarouter.util.string.StringTool;
import io.datarouter.util.tuple.Pair;
import io.datarouter.web.handler.BaseHandler;
import io.datarouter.web.handler.mav.Mav;
import io.datarouter.web.handler.types.optional.OptionalString;
import io.datarouter.web.html.form.HtmlForm;
import io.datarouter.web.html.form.HtmlFormButton;
import io.datarouter.web.html.form.HtmlFormText;
import io.datarouter.web.html.j2html.J2HtmlTable;
import io.datarouter.web.html.j2html.bootstrap4.Bootstrap4FormHtml;
import io.datarouter.web.html.j2html.bootstrap4.Bootstrap4PageFactory;
import io.datarouter.websocket.config.DatarouterWebSocketFiles;
import io.datarouter.websocket.service.ServerAddressProvider;
import io.datarouter.websocket.service.WebSocketConnectionStore;
import io.datarouter.websocket.storage.session.DatarouterWebSocketSessionDao;
import io.datarouter.websocket.storage.session.WebSocketSession;
import io.datarouter.websocket.storage.session.WebSocketSessionKey;
import io.datarouter.websocket.storage.subscription.DatarouterWebSocketSubscriptionDao;
import io.datarouter.websocket.storage.subscription.WebSocketSubscriptionKey;

public class WebSocketToolHandler extends BaseHandler{
	private static final Logger logger = LoggerFactory.getLogger(WebSocketToolHandler.class);

	@Inject
	private WebSocketConnectionStore webSocketConnectionStore;
	@Inject
	private DatarouterWebSocketSessionDao dao;
	@Inject
	private ServerAddressProvider serverAddressProvider;
	@Inject
	private DatarouterProperties datarouterProperties;
	@Inject
	private DatarouterWebSocketFiles files;
	@Inject
	private PushService pushService;
	@Inject
	private DatarouterWebSocketSubscriptionDao subscriptionDao;
	@Inject
	private Bootstrap4PageFactory pageFactory;

	@Handler(defaultHandler = true)
	private Mav list(){
		Mav mav = new Mav(files.jsp.websocketToolJsp);
		mav.put("serverName", datarouterProperties.getServerName());
		mav.put("serverAddress", serverAddressProvider.get());
		mav.put("localStoreSize", webSocketConnectionStore.list().size());
		Map<String,Integer> persistentStorageByServerCount = new HashMap<>();
		LongAdder persistentStorageTotalCount = new LongAdder();
		dao.scan().forEach(wsSession -> {
			persistentStorageByServerCount.merge(wsSession.getServerName(), 1, Integer::sum);
			persistentStorageTotalCount.increment();
		});
		mav.put("persistentStorageByServerCount", persistentStorageByServerCount);
		mav.put("persistentStorageTotalCount", persistentStorageTotalCount.sum());
		Optional<String> destinations = params.optional("destinations");
		Optional<String> message = params.optional("message");
		if(destinations.isPresent()){
			String[] parts = destinations.get().split("\r\n");
			List<Pair<String,String>> results = new ArrayList<>();
			for(String destination : parts){
				try{
					String[] split = destination.split("/");
					String userToken = split[0];
					long sessionId = Long.parseLong(split[1]);
					boolean success = pushService.forward(userToken, sessionId, message.get());
					String resultString = success ? "success" : "failure";
					results.add(new Pair<>(destination, resultString));
				}catch(Exception e){
					logger.warn("", e);
					results.add(new Pair<>(destination, e.toString()));
				}
			}
			mav.put("sendResults", results);
		}
		params.optional("userToken")
				.map(userToken -> new WebSocketSessionKey(userToken, null))
				.map(dao::scanWithPrefix)
				.map(scanner -> scanner.map(WebSocketSessionJspDto::new))
				.map(Scanner::list)
				.ifPresent(userSessions -> mav.put("userSessions", userSessions));
		return mav;
	}

	@Handler
	public Mav subscriptions(OptionalString topic, OptionalString message){
		boolean sent = false;
		if(topic.filter(StringTool::notEmptyNorWhitespace).isPresent()){
			pushService.forwardToTopic(topic.get(), message.orElse(""));
			sent = true;
		}
		List<Pair<String,Long>> rows = subscriptionDao.scanKeys()
				.collect(Collectors.groupingBy(WebSocketSubscriptionKey::getTopic, Collectors.counting()))
				.entrySet().stream()
				.sorted(Comparator.comparing(Entry::getKey))
				.map(entry -> new Pair<>(entry.getKey(), entry.getValue()))
				.collect(Collectors.toList());
		Pair<String,Long> total = new Pair<>("Total", rows.stream()
				.map(Pair::getRight)
				.reduce(0L, Long::sum));
		var table = new J2HtmlTable<Pair<String,Long>>()
				.withClasses("table", "table-sm")
				.withColumn("Topic", Pair::getLeft)
				.withColumn("Count", pair -> NumberFormatter.format(pair.getRight(), 0))
				.build(Scanner.of(rows, List.of(total)).concat(Scanner::of).list());
		var form = new HtmlForm()
				.withMethod("POST")
				.addFields(
						new HtmlFormText()
								.withDisplay("topic")
								.withName("topic")
								.withValue(topic.orElse(""))
								.withRequired(true),
						new HtmlFormText()
								.withDisplay("message")
								.withName("message")
								.withValue(message.orElse(""))
								.withRequired(true),
						new HtmlFormButton()
								.withDisplay("Send")
								.withValue("subscriptions"));
		return pageFactory.startBuilder(request)
				.withTitle("WebSocket Subscriptions")
				.withContent(div(h3("WebSocket Subcriptions"))
						.condWith(sent, div("Send successful").withClasses("alert", "alert-success"))
						.with(div(Bootstrap4FormHtml.render(form)).withClasses("bg-light", "p-2", "border", "rounded"))
						.with(div(table).withClasses("mt-3", "table-responsive"))
						.withClasses("container", "my-5"))
				.buildMav();
	}

	public static class WebSocketSessionJspDto{

		private final String userToken;
		private final Long id;
		private final Date openingDate;
		private final String serverName;

		WebSocketSessionJspDto(WebSocketSession webSocketSession){
			this.userToken = webSocketSession.getKey().getUserToken();
			this.id = webSocketSession.getKey().getId();
			this.openingDate = webSocketSession.getOpeningDate();
			this.serverName = webSocketSession.getServerName();
		}

		public String getUserToken(){
			return userToken;
		}

		public Long getId(){
			return id;
		}

		public Date getOpeningDate(){
			return openingDate;
		}

		public String getServerName(){
			return serverName;
		}

	}

}
