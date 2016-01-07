package com.hotpads.datarouter.client;

import java.util.Collection;
import java.util.Properties;

import com.google.common.base.Preconditions;
import com.hotpads.datarouter.routing.BaseRouter;
import com.hotpads.datarouter.util.core.DrObjectTool;
import com.hotpads.datarouter.util.core.DrStringTool;
import com.hotpads.util.core.lang.ClassTool;
import com.hotpads.util.core.properties.TypedProperties;

public class RouterOptions extends TypedProperties{

	private static final boolean REQUIRE_CLIENT_TYPE = false;
	private static final String DEFAULT_CLIENT_TYPE_NAME = DefaultClientTypes.CLIENT_TYPE_hibernate;

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

	public Class<? extends ClientType> getClientType(String clientName){
		String typeNameKey = prependClientPrefix(clientName, "type");
		String typeName = getString(typeNameKey);
		if(REQUIRE_CLIENT_TYPE){
			Preconditions.checkState(DrStringTool.notEmpty(typeName), "no value found for "+typeNameKey);
		}else{
			if(DrStringTool.isEmpty(typeName)){
				typeName = DEFAULT_CLIENT_TYPE_NAME;
			}
		}
		String typeClassNameKey = "clientType."+typeName;
		String defaultClassNameForType = DefaultClientTypes.CLASS_BY_NAME.get(typeName);
		String typeClassName = DrObjectTool.nullSafe(getString(typeClassNameKey), defaultClassNameForType);
		return ClassTool.forName(typeClassName).asSubclass(ClientType.class);
	}

	public boolean getDisableable(String clientName){
		return getBoolean(prependClientPrefix(clientName, "disableable"), false);
	}

	public String getMode(String routerName){
		return getString(prependRouterPrefix(routerName, "mode"), BaseRouter.MODE_production);
	}

}