package com.hotpads.datarouter.node.factory;


import com.hotpads.datarouter.client.imp.hibernate.node.HibernateNode;
import com.hotpads.datarouter.routing.DataRouter;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

public class HibernateNodeFactory {

	
	//String physicalName, String qualifiedPhysicalName,
	public static <PK extends PrimaryKey<PK>,D extends Databean<PK>,F extends DatabeanFielder<PK,D>> 
	HibernateNode<PK,D,F> 
	newHibernate(String clientName, 
			String physicalName, String qualifiedPhysicalName,
			Class<D> databeanClass, 
			DataRouter router){
		return newHibernate(clientName, physicalName, qualifiedPhysicalName, databeanClass, null, router);
	}
	
	public static <PK extends PrimaryKey<PK>,D extends Databean<PK>,F extends DatabeanFielder<PK,D>> 
	HibernateNode<PK,D,F> 
	newHibernate(String clientName, 
			String physicalName, String qualifiedPhysicalName,
			Class<D> databeanClass, 
			Class<F> fielderClass,
			DataRouter router){
		
		HibernateNode<PK,D,F> node = new HibernateNode<PK,D,F>(databeanClass, fielderClass, router, clientName,
				physicalName, qualifiedPhysicalName);
		return node;
	}

	
	//Class<? super D> baseDatabeanClass,
	public static <PK extends PrimaryKey<PK>,D extends Databean<PK>,F extends DatabeanFielder<PK,D>> 
	HibernateNode<PK,D,F> 
	createSubclass(String clientName, 
			Class<D> databeanClass, 
			Class<? super D> baseDatabeanClass,
			DataRouter router){
		return createSubclass(clientName, databeanClass, null, baseDatabeanClass, router);
	}
	
	public static <PK extends PrimaryKey<PK>,D extends Databean<PK>,F extends DatabeanFielder<PK,D>> 
	HibernateNode<PK,D,F> 
	createSubclass(String clientName, 
			Class<D> databeanClass, 
			Class<F> fielderClass,
			Class<? super D> baseDatabeanClass,
			DataRouter router){
		
		HibernateNode<PK,D,F> node = new HibernateNode<PK,D,F>(
				baseDatabeanClass, databeanClass, fielderClass, router, clientName);
		return node;
	}
}









