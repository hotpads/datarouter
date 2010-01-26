package com.hotpads.datarouter;

import java.io.IOException;
import java.util.Map;

import org.apache.log4j.Logger;

import com.hotpads.datarouter.client.Clients;
import com.hotpads.datarouter.connection.ConnectionPools;
import com.hotpads.datarouter.node.base.physical.BasePhysicalNode;
import com.hotpads.datarouter.node.base.physical.PhysicalNodes;
import com.hotpads.datarouter.routing.DataRouter;
import com.hotpads.datarouter.storage.databean.Databean;

public class DataRouterFactory<R extends DataRouter> {
	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(DataRouterFactory.class);
	
	/**************** fields ****************************************************/

	protected R router;
	protected ConnectionPools connectionPools;
	protected Clients clients;
	protected PhysicalNodes<Databean,BasePhysicalNode<Databean>> nodes = new PhysicalNodes<Databean,BasePhysicalNode<Databean>>();
	
	/**************** constructors ****************************************************/
	
	public DataRouterFactory(){
	}
	
	public DataRouterFactory(R router) throws IOException{
		this(router, null);
	}
	
	public DataRouterFactory(R router, Map<String,Object> params) throws IOException{
		this.router = router;
		this.connectionPools = new ConnectionPools(router.getConfigLocation(), this, params);
		this.clients = new Clients(router.getConfigLocation(), this, params);
		this.router.setClients(this.clients);
	}

	/********** shutdown *************************************************/
	
	public void shutdown(){
		this.connectionPools.shutdown();
	}
	
	
	/**************** add/remove ****************************************************/
	
	public ConnectionPools getConnectionPools(){
		return this.connectionPools;
	}
	
	public void addNodes(PhysicalNodes<Databean,BasePhysicalNode<Databean>> nodes){
		this.nodes.add(nodes);
	}

	public R getRouter() {
		return router;
	}
	
	
	
	/********************************* sample config file ***********************************/
	/*
	 * 
implementation=development

# connectionPools
connectionPoolNames=animal0,pets0,pets1,pets0_slave0,pets1_slave0

connectionPools.defaultInitMode=lazy
#connectionPools.forceInitMode=eager

connectionPool.animal0.url=localhost:3306/animal0
connectionPool.animal0.maxPoolSize=10

connectionPool.pets0.url=localhost:3306/pets0
connectionPool.pets0.maxPoolSize=10

connectionPool.pets1.url=localhost:3306/pets1
connectionPool.pets1.maxPoolSize=10

connectionPool.pets0_slave0.url=localhost:3306/pets0
connectionPool.pets0_slave0.maxPoolSize=10
connectionPool.pets0_slave0.readOnly=true

connectionPool.pets1_slave0.url=localhost:3306/pets1
connectionPool.pets1_slave0.maxPoolSize=10
connectionPool.pets1_slave0.readOnly=true


# clients
clientNames=testHashMap,animal0,pets0,pets1,pets0_slave0,pets1_slave0

clients.defaultInitMode=lazy
#clients.forceInitMode=eager

client.testHashMap.type=hashMap

client.animal0.type=hibernate

client.pets0.type=hibernate

client.pets1.type=hibernate

client.pets0_slave0.type=hibernate
client.pets0_slave0.slave=true
client.pets0_slave0.initMode=eager

client.pets1_slave0.type=hibernate
client.pets1_slave0.slave=true

client.event.type=hibernate
client.event.springBeanName=sessionFactoryEvent

	 */
	
}
