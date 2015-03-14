package com.hotpads.datarouter.routing;

import java.util.ArrayList;
import java.util.List;
import java.util.NavigableSet;
import java.util.Properties;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hotpads.datarouter.client.Client;
import com.hotpads.datarouter.client.ClientId;
import com.hotpads.datarouter.client.Clients;
import com.hotpads.datarouter.connection.ConnectionPools;
import com.hotpads.datarouter.node.Nodes;
import com.hotpads.datarouter.node.op.raw.write.SortedStorageWriter;
import com.hotpads.datarouter.node.type.physical.PhysicalNode;
import com.hotpads.datarouter.util.ApplicationPaths;
import com.hotpads.datarouter.util.core.DrCollectionTool;
import com.hotpads.datarouter.util.core.DrIterableTool;
import com.hotpads.datarouter.util.core.DrListTool;
import com.hotpads.datarouter.util.core.DrObjectTool;
import com.hotpads.datarouter.util.core.DrPropertiesTool;
import com.hotpads.datarouter.util.core.DrStringTool;
import com.hotpads.guice.DatarouterExecutorGuiceModule;

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
	private final ApplicationPaths applicationPaths;
	private final ConnectionPools connectionPools;
	private final Clients clients;
	private final Nodes nodes;
	private final ExecutorService executorService;//for async client init and monitoring
	private final ScheduledExecutorService writeBehindScheduler;
	private final ExecutorService writeBehindExecutor;

	private List<Datarouter> routers;
	private Set<String> configFilePaths;
	private List<Properties> multiProperties;
	private String serverName;
	private String administratorEmail;
	

	/************************** constructors ***************************/

	@Inject
	public DatarouterContext(
			ApplicationPaths applicationPaths,
			ConnectionPools connectionPools,
			Clients clients,
			Nodes nodes,
			@Named(DatarouterExecutorGuiceModule.POOL_datarouterContextExecutor) ExecutorService executorService,
			@Named(DatarouterExecutorGuiceModule.POOL_writeBehindExecutor) ExecutorService writeBehindExecutor,
			@Named(DatarouterExecutorGuiceModule.POOL_writeBehindScheduler) ScheduledExecutorService 
				writeBehindScheduler){
		this.executorService = executorService;
		this.applicationPaths = applicationPaths;
		this.connectionPools = connectionPools;
		this.clients = clients;
		this.nodes = nodes;
		this.writeBehindExecutor = writeBehindExecutor;
		this.writeBehindScheduler = writeBehindScheduler;
		
		this.configFilePaths = new TreeSet<>();
		this.multiProperties = new ArrayList<>();
		this.routers = new ArrayList<>();
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
		if(configFilePaths.contains(configPath)){
			return;
		}
		
		logger.warn("adding datarouter config from "+configPath+", currentRouters:"+routers);
		configFilePaths.add(configPath);
		multiProperties.add(DrPropertiesTool.parse(configPath));
		
		String newServerName = DrPropertiesTool.getFirstOccurrence(multiProperties, CONFIG_SERVER_NAME);
		if(DrStringTool.isEmpty(serverName)){
			serverName = newServerName;
		}else if(DrObjectTool.notEquals(serverName, newServerName)){
			logger.warn("not replacing existing serverName "+serverName+" with "+newServerName+" from "+configPath);
		}
		
		String newAdministratorEmail = DrPropertiesTool.getFirstOccurrence(multiProperties, CONFIG_ADMINISTRATOR_EMAIL);
		if(DrStringTool.isEmpty(administratorEmail)){
			administratorEmail = newAdministratorEmail;
		}else if(DrObjectTool.notEquals(administratorEmail, newAdministratorEmail)){
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
		for(Datarouter router : DrCollectionTool.nullSafe(this.routers)){
			if(name.equals(router.getName())){
				return router;
			}
		}
		return null;
	}
	
	public List<Client> getClients(){
		SortedSet<Client> clients = new TreeSet<>();
		for(Datarouter router : DrIterableTool.nullSafe(getRouters())){
			for(Client client : DrIterableTool.nullSafe(router.getAllClients())){
				clients.add(client);
			}
		}
		return DrListTool.createArrayList(clients);
	}
	
	public Datarouter getRouterForClient(Client client){
		for(Datarouter router : routers){
			for(Client c : router.getAllClients()){
				if(c==client){ 
					return router; 
				}
			}
		}
		return null;
	}
	
	public NavigableSet<PhysicalNode<?,?>> getWritableNodes(){
		NavigableSet<PhysicalNode<?,?>> writableNodes = new TreeSet<>();
		for(Datarouter router : routers){
			for(ClientId clientId : router.getClientIds()){
				if(!clientId.getWritable()){
					continue;
				}
				List<? extends PhysicalNode<?,?>> nodes = getNodes().getPhysicalNodesForClient(clientId.getName());

				for(PhysicalNode<?,?> node : nodes){
					if(!(node instanceof SortedStorageWriter<?,?>)){
						continue;
					}
					if(writableNodes.contains(node)){
						continue;
					}
					writableNodes.add(node);


				}
			}
		}
		return writableNodes;
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

	public ExecutorService getWriteBehindExecutor(){
		return writeBehindExecutor;
	}
	
	public ScheduledExecutorService getWriteBehindScheduler(){
		return writeBehindScheduler;
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
