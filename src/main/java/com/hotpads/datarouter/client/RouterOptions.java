package com.hotpads.datarouter.client;

import java.util.Collection;
import java.util.Map;
import java.util.Properties;

import com.google.common.base.Preconditions;
import com.hotpads.datarouter.client.imp.hibernate.HibernateClientType;
import com.hotpads.datarouter.client.imp.memory.MemoryClientType;
import com.hotpads.datarouter.routing.BaseDataRouter;
import com.hotpads.util.core.MapTool;
import com.hotpads.util.core.ObjectTool;
import com.hotpads.util.core.StringTool;
import com.hotpads.util.core.java.ReflectionTool;
import com.hotpads.util.core.properties.TypedProperties;

public class RouterOptions extends TypedProperties{
	
	public static final Map<String,String> DEFAULT_TYPE_CLASS_BY_NAME = MapTool.createTreeMap();
	static{
		DEFAULT_TYPE_CLASS_BY_NAME.put("hbase", "com.hotpads.datarouter.client.imp.hbase.HBaseClientType");
		DEFAULT_TYPE_CLASS_BY_NAME.put("hibernate", "com.hotpads.datarouter.client.imp.hibernate.HibernateClientType");
		DEFAULT_TYPE_CLASS_BY_NAME.put("http", "com.hotpads.datarouter.client.imp.http.HttpClientType");
		DEFAULT_TYPE_CLASS_BY_NAME.put("jdbc", "com.hotpads.datarouter.client.imp.jdbc.JdbcClientType");
		DEFAULT_TYPE_CLASS_BY_NAME.put("memcached", "com.hotpads.datarouter.client.imp.memcached.MemcachedClientType");
		DEFAULT_TYPE_CLASS_BY_NAME.put("memory", "com.hotpads.datarouter.client.imp.memory.MemoryClientType");
	}
	
	public static final String CLIENT_NAME_memory = "memory";
	
	public static final ClientType CLIENT_TYPE_DEFAULT = HibernateClientType.INSTANCE;//for now, because our config files assume this

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
	
	public ClientType getClientTypeInstance(String clientName){
		if(CLIENT_NAME_memory.equals(clientName)){ return MemoryClientType.INSTANCE; }
		String typeNameKey = prependClientPrefix(clientName, "type");
		String typeName = getString(typeNameKey);
		if(StringTool.isEmpty(typeName)){ return CLIENT_TYPE_DEFAULT; }
		String typeClassNameKey = "clientType."+typeName;
		String defaultClassNameForType = DEFAULT_TYPE_CLASS_BY_NAME.get(typeName);
		String typeClassName = ObjectTool.nullSafe(getString(typeClassNameKey), defaultClassNameForType);
		Preconditions.checkNotNull(typeClassName, "no implementation specified for type "+typeClassNameKey);
		return ReflectionTool.create(typeClassName);
	}
	
	public String getMode(String routerName){
		return getString(prependRouterPrefix(routerName, "mode"), BaseDataRouter.MODE_production);
	}
	
}






