package com.hotpads.datarouter.client;

import java.util.Collection;
import java.util.Map;
import java.util.Properties;

import com.google.common.base.Preconditions;
import com.hotpads.datarouter.client.imp.hibernate.HibernateClientType;
import com.hotpads.datarouter.client.imp.memory.MemoryClientType;
import com.hotpads.datarouter.routing.BaseDatarouter;
import com.hotpads.util.core.MapTool;
import com.hotpads.util.core.ObjectTool;
import com.hotpads.util.core.StringTool;
import com.hotpads.util.core.java.ReflectionTool;
import com.hotpads.util.core.properties.TypedProperties;

public class RouterOptions extends TypedProperties{
	
	private static final boolean REQUIRE_CLIENT_TYPE = false;
	private static final ClientType CLIENT_TYPE_DEFAULT = HibernateClientType.INSTANCE;//for now, because our config files assume this

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
		String typeNameKey = prependClientPrefix(clientName, "type");
		String typeName = getString(typeNameKey);
		if(REQUIRE_CLIENT_TYPE){
			Preconditions.checkState(StringTool.notEmpty(typeName), "no value found for "+typeNameKey);
		}else{
			if(StringTool.isEmpty(typeName)){ return CLIENT_TYPE_DEFAULT; }
		}
		String typeClassNameKey = "clientType."+typeName;
		String defaultClassNameForType = DefaultClientTypes.CLASS_BY_NAME.get(typeName);
		String typeClassName = ObjectTool.nullSafe(getString(typeClassNameKey), defaultClassNameForType);
		Preconditions.checkNotNull(typeClassName, "no implementation specified for type "+typeClassNameKey);
		return ReflectionTool.create(typeClassName);
	}
	
	public String getMode(String routerName){
		return getString(prependRouterPrefix(routerName, "mode"), BaseDatarouter.MODE_production);
	}
	
}






