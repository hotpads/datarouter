package com.hotpads.datarouter.routing;

import java.util.List;
import java.util.SortedSet;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.google.inject.Singleton;
import com.hotpads.datarouter.client.Client;
import com.hotpads.datarouter.client.Clients;
import com.hotpads.datarouter.connection.ConnectionPools;
import com.hotpads.datarouter.node.Nodes;
import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.IterableTool;
import com.hotpads.util.core.ListTool;
import com.hotpads.util.core.SetTool;
import com.hotpads.util.core.concurrent.NamedThreadFactory;

@Singleton
public class DataRouterContext{

	protected ThreadGroup parentThreadGroup;
	protected ThreadFactory threadFactory;
	protected ThreadPoolExecutor executorService;//for async client init and monitoring

	protected List<DataRouter> routers;
	protected List<String> configFilePaths;
	protected ConnectionPools connectionPools;
	protected Clients clients;
	protected Nodes nodes;
	
	public DataRouterContext() {
		this(new ThreadGroup("DataRouter-DefaultThreadGroup"));
	}
	
	public DataRouterContext(ThreadGroup parentThreadGroup){
		this.parentThreadGroup = parentThreadGroup;//new ThreadGroup("DataRouter-"+router.getName());
		this.threadFactory = new NamedThreadFactory(parentThreadGroup, "DataRouterContext-"
				+System.identityHashCode(this), true);
		this.executorService = new ThreadPoolExecutor(0, Integer.MAX_VALUE, 60L, TimeUnit.SECONDS,
	            new SynchronousQueue<Runnable>(), threadFactory);
		
		connectionPools = new ConnectionPools();
		clients = new Clients(this);
		nodes = new Nodes();
		routers = ListTool.createArrayList();
		configFilePaths = ListTool.createArrayList();
	}
	
	public void register(DataRouter router) {
		routers.add(router);
		configFilePaths.add(router.getConfigLocation());
		connectionPools.registerClientIds(router.getClientIds(), router.getConfigLocation());
		clients.registerClientIds(router.getClientIds(), router.getConfigLocation());
		//node registration happens in BaseDataRouter.register()
	}
	
	
	/********************* methods **********************************/

	public DataRouter getRouter(String name){
		for(DataRouter router : CollectionTool.nullSafe(this.routers)){
			if(name.equals(router.getName())){
				return router;
			}
		}
		return null;
	}
	
	public List<Client> getClients(){
		SortedSet<Client> clients = SetTool.createTreeSet();
		for(DataRouter router : IterableTool.nullSafe(getRouters())){
			for(Client client : IterableTool.nullSafe(router.getAllClients())){
				clients.add(client);
			}
		}
		return ListTool.createArrayList(clients);
	}
	
	public DataRouter getRouterForClient(Client client){
		for(DataRouter router : routers){
			for(Client c : router.getAllClients()){
				if(c==client){ return router; }
			}
		}
		return null;
	}

	public void clearThreadSpecificState(){
		if(CollectionTool.isEmpty(this.routers)){ return; }
		for(DataRouter router : this.routers){
			router.clearThreadSpecificState();
		}
	}
	
	/********************* get/set ***************************/

	public ConnectionPools getConnectionPools(){
		return connectionPools;
	}

	public Clients getClientPool(){
		return clients;
	}

	public Nodes getNodes(){
		return nodes;
	}

	public List<DataRouter> getRouters(){
		return routers;
	}

	public ThreadGroup getParentThreadGroup(){
		return parentThreadGroup;
	}

	public ThreadPoolExecutor getExecutorService(){
		return executorService;
	}

	public List<String> getConfigFilePaths(){
		return configFilePaths;
	}
	
	
}
