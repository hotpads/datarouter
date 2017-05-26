package com.hotpads.datarouter.routing;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.NavigableSet;
import java.util.Properties;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.stream.Stream;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hotpads.datarouter.client.Client;
import com.hotpads.datarouter.client.ClientId;
import com.hotpads.datarouter.client.DatarouterClients;
import com.hotpads.datarouter.client.LazyClientProvider;
import com.hotpads.datarouter.config.DatarouterProperties;
import com.hotpads.datarouter.inject.guice.executor.DatarouterExecutorGuiceModule;
import com.hotpads.datarouter.node.DatarouterNodes;
import com.hotpads.datarouter.node.type.physical.PhysicalNode;
import com.hotpads.datarouter.op.TxnOp;
import com.hotpads.datarouter.op.executor.impl.SessionExecutorImpl;
import com.hotpads.datarouter.util.core.DrPropertiesTool;
import com.hotpads.trace.TracerThreadLocal;
import com.hotpads.trace.TracerTool;
import com.hotpads.util.core.collections.Pair;

/**
 * Datarouter is the top-level scope through which various components can share things like clients,
 * configuration, and thread pools. Almost all applications will have a single Datarouter, though it is possible
 * to have multiple contexts for modularized applications or large test suites.
 */
@Singleton
public class Datarouter{
	private static final Logger logger = LoggerFactory.getLogger(Datarouter.class);

	/*************************** fields *****************************/

	//injected
	private final DatarouterProperties datarouterProperties;
	private final DatarouterClients clients;
	private final DatarouterNodes nodes;
	private final ExecutorService executorService;//for async client init and monitoring
	private final ScheduledExecutorService writeBehindScheduler;
	private final ExecutorService writeBehindExecutor;

	private SortedSet<Router> routers;
	private Set<String> configFilePaths;
	private List<Properties> multiProperties;


	/************************** constructors ***************************/

	@Inject
	public Datarouter(
			DatarouterProperties datarouterProperties,
			DatarouterClients clients,
			DatarouterNodes nodes,
			@Named(DatarouterExecutorGuiceModule.POOL_datarouterExecutor) ExecutorService executorService,
			@Named(DatarouterExecutorGuiceModule.POOL_writeBehindExecutor) ExecutorService writeBehindExecutor,
			@Named(DatarouterExecutorGuiceModule.POOL_writeBehindScheduler) ScheduledExecutorService
				writeBehindScheduler){
		this.datarouterProperties = datarouterProperties;
		this.executorService = executorService;
		this.clients = clients;
		this.nodes = nodes;
		this.writeBehindExecutor = writeBehindExecutor;
		this.writeBehindScheduler = writeBehindScheduler;

		this.configFilePaths = new TreeSet<>();
		this.multiProperties = new ArrayList<>();
		this.routers = new TreeSet<>();
	}


	/********************** builder methods ****************************/

	public synchronized void registerConfigFile(String configFilePath){
		clients.registerConfigFile(configFilePath);
	}

	public Stream<LazyClientProvider> registerClientIds(Collection<ClientId> clientIds){
		return clients.registerClientIds(datarouterProperties, this, clientIds);
	}

	public synchronized void register(Router router){
		routers.add(router);
		addConfigIfNew(router);
	}

	private void addConfigIfNew(Router router){
		String configPath = router.getConfigLocation();
		if(configFilePaths.contains(configPath)){
			return;
		}
		Pair<Properties,URL> propertiesAndLocation = DrPropertiesTool.parseAndGetLocation(configPath);
		logger.warn("adding router config from " + propertiesAndLocation.getRight() + ", currentRouters:" + routers);
		configFilePaths.add(configPath);
		multiProperties.add(propertiesAndLocation.getLeft());
	}

	public void initializeEagerClients(){
		clients.initializeEagerClients();
	}

	public void shutdown(){
		clients.shutdown();
		executorService.shutdown();
	}


	/********************* methods **********************************/

	public Router getRouter(String name){
		for(Router router : routers){
			if(name.equals(router.getName())){
				return router;
			}
		}
		return null;
	}

	public List<Client> getClients(){
		SortedSet<Client> clients = new TreeSet<>();
		for(Router router : routers){
			for(Client client : router.getAllClients()){
				clients.add(client);
			}
		}
		return new ArrayList<>(clients);
	}

	public Router getRouterForClient(Client client){
		for(Router router : routers){
			for(Client c : router.getAllClients()){
				if(c == client){
					return router;
				}
			}
		}
		return null;
	}

	public NavigableSet<PhysicalNode<?,?>> getWritableNodes(){
		NavigableSet<PhysicalNode<?,?>> writableNodes = new TreeSet<>();
		for(Router router : routers){
			for(ClientId clientId : router.getClientIds()){
				if(!clientId.getWritable()){
					continue;
				}
				writableNodes.addAll(getNodes().getPhysicalNodesForClient(clientId.getName()));
			}
		}
		return writableNodes;
	}

	/************************************** run method **************************************/

	public <T> T run(TxnOp<T> parallelTxnOp){
		TracerTool.startSpan(TracerThreadLocal.get(), parallelTxnOp.getClass().getSimpleName());
		try{
			return new SessionExecutorImpl<>(parallelTxnOp).call();
		}catch(Exception e){
			throw new RuntimeException(e);
		}finally{
			TracerTool.finishSpan(TracerThreadLocal.get());
		}
	}


	/********************* get/set ***************************/

	public DatarouterClients getClientPool(){
		return clients;
	}

	public DatarouterNodes getNodes(){
		return nodes;
	}

	public SortedSet<Router> getRouters(){
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

	public DatarouterProperties getDatarouterProperties(){
		return datarouterProperties;
	}

}
