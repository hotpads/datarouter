package com.hotpads.websocket.session;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import com.hotpads.datarouter.node.op.combo.SortedMapStorage.SortedMapStorageNode;
import com.hotpads.util.core.collections.Range;
import com.hotpads.util.http.client.HotPadsHttpClient;
import com.hotpads.util.http.request.HotPadsHttpRequest;
import com.hotpads.util.http.request.HotPadsHttpRequest.HttpRequestMethod;

@Singleton
public class PushService{

	public static final String PUSH_SERVICE_HTTPCLIENT = "pushServiceHtpClient";

	private final HotPadsHttpClient httpClient;
	private final SortedMapStorageNode<WebSocketSessionKey,WebSocketSession> webSocketSessionNode;

	@Inject
	public PushService(@Named(PUSH_SERVICE_HTTPCLIENT) HotPadsHttpClient httpClient,
			WebSocketSessionNodeProvider webSocketSessionNodeProvider){
		this.httpClient = httpClient;
		this.webSocketSessionNode = webSocketSessionNodeProvider.get();
	}

	public void register(WebSocketSession webSocketSession){
		webSocketSessionNode.put(webSocketSession, null);
	}

	public void unregister(WebSocketSessionKey webSocketSessionKey){
		webSocketSessionNode.delete(webSocketSessionKey, null);
	}

	//TODO Optimization: don't do the http call if the socket is open on the current server
	public void forward(String userToken, String message){
		WebSocketSessionKey prefix = new WebSocketSessionKey(userToken);
		Range<WebSocketSessionKey> range = new Range<>(prefix, true, prefix, true);
		Iterable<WebSocketSession> scan = webSocketSessionNode.scan(range, null);
		for(WebSocketSession webSocketSession : scan){
			String url = "https://" + webSocketSession.getServerName() + WebSocketApiDispatcher.WEBSOCKET_COMMAND + "/"
					+ WebSocketCommandName.PUSH.getPath();
			HotPadsHttpRequest request = new HotPadsHttpRequest(HttpRequestMethod.POST, url, false);
			WebSocketCommand webSocketCommand = new WebSocketCommand(webSocketSession.getKey(), message);
			httpClient.addDtoToPayload(request, webSocketCommand, null);
			httpClient.execute(request);
		}
	}

	public int getNumberOfSession(String userToken){
		WebSocketSessionKey prefix = new WebSocketSessionKey(userToken);
		List<WebSocketSession> activeSessions = webSocketSessionNode.getWithPrefix(prefix, false, null);
		return activeSessions.size();
	}

}
