package com.hotpads.datarouter.node.factory;


import com.hotpads.datarouter.node.Node;
import com.hotpads.datarouter.routing.DataRouter;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

public class HibernateNodeFactory {

	
	//String physicalName, String qualifiedPhysicalName,
	public static <PK extends PrimaryKey<PK>,D extends Databean<PK,D>,F extends DatabeanFielder<PK,D>> 
	Node<PK,D> 
	newHibernate(String clientName, 
			String physicalName, String qualifiedPhysicalName,
			Class<D> databeanClass, 
			DataRouter router){
		return newHibernate(clientName, physicalName, qualifiedPhysicalName, databeanClass, null, router);
	}
	
	public static <PK extends PrimaryKey<PK>,D extends Databean<PK,D>,F extends DatabeanFielder<PK,D>> 
	Node<PK,D> 
	newHibernate(String clientName, 
			String physicalName, String qualifiedPhysicalName,
			Class<D> databeanClass, 
			Class<F> fielderClass,
			DataRouter router){
		return NodeFactory.create(clientName, physicalName, qualifiedPhysicalName, databeanClass, fielderClass, router);
	}

	
	public static <PK extends PrimaryKey<PK>,D extends Databean<PK,D>,F extends DatabeanFielder<PK,D>> 
	Node<PK,D> 
	createSubclass(String clientName, 
			Class<D> databeanClass, 
			Class<F> fielderClass,
			Class<? super D> baseDatabeanClass,
			DataRouter router){
		return NodeFactory.createWithBaseDatabeanClass(clientName, databeanClass, baseDatabeanClass, router);
	}
}









