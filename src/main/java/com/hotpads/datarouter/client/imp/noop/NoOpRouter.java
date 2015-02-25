package com.hotpads.datarouter.client.imp.noop;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.SortedSet;
import java.util.TreeSet;

import com.hotpads.datarouter.client.Client;
import com.hotpads.datarouter.client.ClientId;
import com.hotpads.datarouter.client.ClientType;
import com.hotpads.datarouter.client.RouterOptions;
import com.hotpads.datarouter.connection.ConnectionPools;
import com.hotpads.datarouter.node.Node;
import com.hotpads.datarouter.op.TxnOp;
import com.hotpads.datarouter.routing.Datarouter;
import com.hotpads.datarouter.routing.DatarouterContext;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.Key;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

public class NoOpRouter implements Datarouter{

	@Override
	public String getConfigLocation(){
		return null;
	}

	@Override
	public RouterOptions getRouterOptions(){
		return new RouterOptions(new ArrayList<Properties>());
	}

	@Override
	public <PK extends PrimaryKey<PK>, D extends Databean<PK, D>, N extends Node<PK, D>> N register(N node){
		return node;
	}

	@Override
	public void registerWithContext() throws IOException{
		
	}

	@Override
	public SortedSet<Node> getNodes(){
		return new TreeSet<>();
	}

	@Override
	public DatarouterContext getContext(){
		return null;
	}

	@Override
	public <T> T run(TxnOp<T> parallelTxnOp){
		return null;
	}

	@Override
	public List<ClientId> getClientIds(){
		return Collections.emptyList();
	}

	@Override
	public List<String> getClientNames(){
		return Collections.emptyList();
	}

	@Override
	public Client getClient(String clientName){
		return null;
	}

	@Override
	public ClientType getClientType(String clientName){
		return null;
	}

	@Override
	public List<Client> getAllClients(){
		return Collections.emptyList();
	}

	@Override
	public <K extends Key<K>> List<String> getClientNamesForKeys(Collection<? extends Key<K>> keys){
		return Collections.emptyList();
	}

	@Override
	public <PK extends PrimaryKey<PK>, D extends Databean<PK, D>> List<String> getClientNamesForDatabeans(
			Collection<D> databeans){
		return Collections.emptyList();
	}

	@Override
	public <PK extends PrimaryKey<PK>, D extends Databean<PK, D>> List<Client> getClientsForDatabeanType(
			Class<D> databeanType){
		return Collections.emptyList();
	}

	@Override
	public <K extends Key<K>> List<Client> getClientsForKeys(Collection<? extends Key<K>> keys){
		return Collections.emptyList();
	}

	@Override
	public <PK extends PrimaryKey<PK>, D extends Databean<PK, D>> List<Client> getClientsForDatabeans(
			Collection<D> databeans){
		return Collections.emptyList();
	}

	@Override
	public ConnectionPools getConnectionPools(){
		return null;
	}

	@Override
	public String getName(){
		return "";
	}

}
