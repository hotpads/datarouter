package com.hotpads.datarouter.node.factory;


import com.hotpads.datarouter.client.imp.hibernate.node.HibernateNode;
import com.hotpads.datarouter.routing.DataRouter;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

public class HibernateNodeFactory {

	public static <PK extends PrimaryKey<PK>,D extends Databean<PK>> 
	HibernateNode<PK,D> 
	newHibernate(String clientName, 
			Class<D> databeanClass, 
			DataRouter router){
		
		return new HibernateNode<PK,D>(databeanClass, router, clientName);
	}

	public static <PK extends PrimaryKey<PK>,D extends Databean<PK>> 
	HibernateNode<PK,D> 
	newHibernate(String clientName, 
			String physicalName, String qualifiedPhysicalName,
			Class<D> databeanClass, 
			DataRouter router){
		
		HibernateNode<PK,D> node = new HibernateNode<PK,D>(databeanClass, router, clientName,
				physicalName, qualifiedPhysicalName);
		return node;
	}

	public static <PK extends PrimaryKey<PK>,D extends Databean<PK>> 
	HibernateNode<PK,D> 
	newHibernate(String clientName, 
			Class<D> databeanClass, 
			Class<? super D> baseDatabeanClass,
			DataRouter router){
		
		HibernateNode<PK,D> node = new HibernateNode<PK,D>(
				baseDatabeanClass, databeanClass, router, clientName);
		return node;
	}
}









