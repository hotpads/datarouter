package com.hotpads.datarouter.routing;

import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.SortedSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hotpads.datarouter.client.Client;
import com.hotpads.datarouter.client.Clients;
import com.hotpads.datarouter.connection.ConnectionPools;
import com.hotpads.datarouter.node.Nodes;
import com.hotpads.datarouter.util.ApplicationPaths;
import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.IterableTool;
import com.hotpads.util.core.ListTool;
import com.hotpads.util.core.ObjectTool;
import com.hotpads.util.core.PropertiesTool;
import com.hotpads.util.core.SetTool;
import com.hotpads.util.core.StringTool;
import com.hotpads.util.core.concurrent.NamedThreadFactory;

/**
 * DatarouterContext is the top-level scope through which various components can share things like clients,
 * configuration, and thread pools. Almost all applications will have a single DatarouterContext, though it is possible
 * to have multiple contexts for modularized applications or large test suites.
 * 
 * @author mcorgan
 * 
 */
@Singleton
public class DatarouterContext{
	private static final Logger logger = LoggerFactory.getLogger(DatarouterContext.class);

	private static final String
			CONFIG_SERVER_NAME = "server.name",
			CONFIG_ADMINISTRATOR_EMAIL = "administrator.email";
	
	
	/*************************** fields *****************************/

	//injected
	private ApplicationPaths applicationPaths;
	private ConnectionPools connectionPools;
	private Clients clients;
	private Nodes nodes;
	
	//not injected
	private ExecutorService executorService;//for async client init and monitoring

	private List<Datarouter> routers;
	private Set<String> configFilePaths;
	private List<Properties> multiProperties;
	private String serverName;
	private String administratorEmail;
	

	/************************** constructors ***************************/
	
	/*
	 * for some reason, trying to inject an ExecutorService throws guice into an endless loop of ComputationExceptions.
	 * Google doesn't turn up many questions about it.
	 */
	@Inject
	public DatarouterContext(/*@DatarouterExecutorService ExecutorService executorService,*/
			ApplicationPaths applicationPaths, ConnectionPools connectionPools, Clients clients, Nodes nodes){
		int id = System.identityHashCode(this);
		ThreadGroup threadGroup = new ThreadGroup("Datarouter-ThreadGroup-"+id);
		ThreadFactory threadFactory = new NamedThreadFactory(threadGroup, "Datarouter-ThreadFactory-"+id, true);
		this.executorService = new ThreadPoolExecutor(0, Integer.MAX_VALUE, 60L, TimeUnit.SECONDS,
	            new SynchronousQueue<Runnable>(), threadFactory);

//		this.executorService = executorService;
		this.applicationPaths = applicationPaths;
		this.connectionPools = connectionPools;
		this.clients = clients;
		this.nodes = nodes;
		
		this.configFilePaths = SetTool.createTreeSet();
		this.multiProperties = ListTool.createArrayList();
		this.routers = ListTool.createArrayList();
//		createDefaultMemoryClient();//do after this.clients and this.nodes have been instantiated
	}
	
	
	/********************** builder methods ****************************/
	
	public synchronized void registerConfigFile(String configFilePath){
		clients.registerConfigFile(configFilePath);
	}
	
	public synchronized void register(Datarouter router) {
		routers.add(router);
		addConfigIfNew(router);
		connectionPools.registerClientIds(router.getClientIds(), router.getConfigLocation());
		clients.registerClientIds(this, router.getClientIds());
	}
	
	private void addConfigIfNew(Datarouter router){
		String configPath = router.getConfigLocation();
		if(configFilePaths.contains(configPath)){ return; }
		
		logger.warn("adding datarouter config from "+configPath+", currentRouters:"+routers);
		configFilePaths.add(configPath);
		multiProperties.add(PropertiesTool.parse(configPath));
		
		String newServerName = PropertiesTool.getFirstOccurrence(multiProperties, CONFIG_SERVER_NAME);
		if(StringTool.isEmpty(serverName)){
			serverName = newServerName;
		}else if(ObjectTool.notEquals(serverName, newServerName)){
			logger.warn("not replacing existing serverName "+serverName+" with "+newServerName+" from "+configPath);
		}
		
		String newAdministratorEmail = PropertiesTool.getFirstOccurrence(multiProperties, CONFIG_ADMINISTRATOR_EMAIL);
		if(StringTool.isEmpty(administratorEmail)){
			administratorEmail = newAdministratorEmail;
		}else if(ObjectTool.notEquals(administratorEmail, newAdministratorEmail)){
			logger.warn("not replacing existing administratorEmail "+administratorEmail+" with "+newAdministratorEmail
					+" from "+configPath);
		}
	}
	
	public void initializeEagerClients(){
		clients.initializeEagerClients(this);
	}
	
	public void shutdown(){
		clients.shutdown();
		executorService.shutdown();
	}
	
	
	/********************* methods **********************************/

	public Datarouter getRouter(String name){
		for(Datarouter router : CollectionTool.nullSafe(this.routers)){
			if(name.equals(router.getName())){
				return router;
			}
		}
		return null;
	}
	
	public List<Client> getClients(){
		SortedSet<Client> clients = SetTool.createTreeSet();
		for(Datarouter router : IterableTool.nullSafe(getRouters())){
			for(Client client : IterableTool.nullSafe(router.getAllClients())){
				clients.add(client);
			}
		}
		return ListTool.createArrayList(clients);
	}
	
	public Datarouter getRouterForClient(Client client){
		for(Datarouter router : routers){
			for(Client c : router.getAllClients()){
				if(c==client){ return router; }
			}
		}
		return null;
	}

	public void clearThreadSpecificState(){
		if(CollectionTool.isEmpty(this.routers)){ return; }
		for(Datarouter router : this.routers){
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

	public List<Datarouter> getRouters(){
		return routers;
	}

	public ExecutorService getExecutorService(){
		return executorService;
	}

	public Set<String> getConfigFilePaths(){
		return configFilePaths;
	}

	public String getServerName(){
		return serverName;
	}
	
	public String getAdministratorEmail(){
		return administratorEmail;
	}

	public ApplicationPaths getApplicationPaths(){
		return applicationPaths;
	}
	
	
	
}
