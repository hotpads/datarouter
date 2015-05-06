package com.hotpads.websocket.session;

import javax.inject.Provider;

import com.hotpads.datarouter.node.op.combo.SortedMapStorage.SortedMapStorageNode;

public interface WebSocketSessionNodeProvider
extends Provider<SortedMapStorageNode<WebSocketSessionKey,WebSocketSession>>{

}
