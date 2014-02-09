package com.hotpads.datarouter.client;

import java.util.Collection;
import java.util.Properties;

import com.google.common.base.Preconditions;
import com.hotpads.datarouter.client.imp.hibernate.HibernateClientType;
import com.hotpads.datarouter.client.imp.memory.MemoryClientType;
import com.hotpads.datarouter.routing.BaseDataRouter;
import com.hotpads.util.core.StringTool;
import com.hotpads.util.core.java.ReflectionTool;
import com.hotpads.util.core.properties.TypedProperties;

public class RouterOptions extends TypedProperties{
	
	public static final String CLIENT_NAME_memory = "memory";
	
	public static final DClientType CLIENT_TYPE_DEFAULT = HibernateClientType.INSTANCE;//for now, because our config files assume this

	public RouterOptions(Collection<Properties> propertiesList){
		super(propertiesList);
	}
	
	public RouterOptions(String propertiesPath){
		super(propertiesPath);
	}
	
	
	protected String getRouterPrefix(String routerName){
		return "router."+routerName+".";
	}
	
	protected String getClientPrefix(String clientName){
		return "client."+clientName+".";
	}
	
	
	protected String prependRouterPrefix(String routerName, String toAppend){
		return getRouterPrefix(routerName)+toAppend;
	}
	
	protected String prependClientPrefix(String clientName, String toAppend){
		return getClientPrefix(clientName)+toAppend;
	}
	
	
	/***************** actual variables *********************************/
	
	public ClientType getClientType(String clientName){
		if(CLIENT_NAME_memory.equals(clientName)){ return ClientType.memory; }
		String type = getString(prependClientPrefix(clientName, "type"));
		ClientType clientType = ClientType.fromString(type);
//		return Preconditions.checkNotNull(clientType, "unknown clientType:"+clientType+" for clientName:"+clientName);
		return clientType==null?ClientType.hibernate:clientType;
	}
	
	public DClientType getClientTypeInstance(String clientName){
		if(CLIENT_NAME_memory.equals(clientName)){ return MemoryClientType.INSTANCE; }
		String typeNameKey = prependClientPrefix(clientName, "type");
		String typeName = getString(typeNameKey);
		if(StringTool.isEmpty(typeName)){ return CLIENT_TYPE_DEFAULT; }
		String typeClassNameKey = "clientType."+typeName;
		String typeClassName = Preconditions.checkNotNull(getString(typeClassNameKey), typeClassNameKey);
		return ReflectionTool.create(typeClassName);
	}
	
	public String getMode(String routerName){
		return getString(prependRouterPrefix(routerName, "mode"), BaseDataRouter.MODE_production);
	}
	
}






