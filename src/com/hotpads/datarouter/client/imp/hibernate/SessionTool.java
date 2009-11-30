package com.hotpads.datarouter.client.imp.hibernate;

import org.hibernate.Session;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.config.PutMethod;
import com.hotpads.datarouter.storage.databean.Databean;

public class SessionTool {

	public static void putUsingMethod(Session session, String entityName, Databean databean, 
			final Config config, PutMethod defaultPutMethod){
		
		PutMethod putMethod = defaultPutMethod;
		if(config!=null && config.getPutMethod()!=null){
			putMethod = config.getPutMethod();
		}
		if(PutMethod.INSERT_OR_BUST == putMethod){
			session.save(entityName, databean);
		}else if(PutMethod.UPDATE_OR_BUST == putMethod){
			session.update(entityName, databean);
		}else if(PutMethod.INSERT_OR_UPDATE == putMethod){
			try{
				session.save(entityName, databean);
				session.flush();//seems like it tries to save 3 times before throwing an exception
			}catch(Exception e){  
				session.evict(databean);  //must evict or it will ignore future actions for the databean?
				session.update(entityName, databean);
			}
		}else if(PutMethod.UPDATE_OR_INSERT == putMethod){
			try{
				session.update(entityName, databean);
				session.flush();
			}catch(Exception e){
				session.evict(databean);  //must evict or it will ignore future actions for the databean?
				session.save(entityName, databean);
			}
		}else{
			session.saveOrUpdate(entityName, databean);
		}
	}
}
