package com.hotpads.setting.cluster;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.quartz.CronExpression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hotpads.datarouter.util.core.DrBooleanTool;
import com.hotpads.datarouter.util.core.DrCollectionTool;
import com.hotpads.datarouter.util.core.DrStringTool;
import com.hotpads.setting.ClusterSettingFinderConfig;
import com.hotpads.setting.ServerType;
import com.hotpads.setting.cached.imp.Duration;

@Singleton
public class ClusterSettingFinder {

	private static final Logger logger = LoggerFactory.getLogger(ClusterSettingFinder.class);

	public static final String EMPTY_STRING = "";

	@Inject
	private ClusterSettingFinderConfig clusterSettingFinderConfig;
	@Inject
	private ClusterSettingNodes clusterSettingNodes;

	public Integer getInteger(String name, Integer defaultValue){
		String valueString = getMostSpecificValue(name);
		if(valueString==null){
			return defaultValue;
		}
		return Integer.valueOf(valueString);
	}

	public Boolean getBoolean(String name, Boolean defaultValue) {
		String valueString = getMostSpecificValue(name);
		if(valueString==null){
			return defaultValue;
		}
		return DrBooleanTool.isTrue(valueString);
	}

	public String getString(String name, String defaultValue){
		String valueString = getMostSpecificValue(name);
		if(valueString==null){
			return defaultValue;
		}
		return valueString;
	}

	public CronExpression getCronExpression(String name, String defaultValue){
		String valueString = getMostSpecificValue(name);
		try {
			if(valueString==null){ 
				valueString = defaultValue;
			}
			if(valueString==null){
				return null;
			}
			return new CronExpression(valueString);
		} catch (ParseException e) {
			logger.warn("ParseException on "+name+" with value "+valueString);
			throw new RuntimeException(e);
		}
	}

	public Duration getDuration(String name, Duration defaultValue){
		String valueString = getMostSpecificValue(name);
		if(valueString==null){
			return defaultValue;
		}
		return new Duration(valueString);
	}
	
	private String getMostSpecificValue(String name){
		List<ClusterSettingKey> keys = generateKeysForSelection(name);
		//		boolean log = "job.event.aggregateEvents".equals(name);
		//		if(log){
		//			logger.warn("searching for "+CollectionTool.size(keys)+":");
		//			for(ClusterSettingKey key : keys){ logger.warn(key); }
		//		}
		List<ClusterSetting> settings = clusterSettingNodes.clusterSetting().getMulti(keys, null);
		//		if(log){
		//			logger.warn("found "+CollectionTool.size(settings)+":");
		//			for(ClusterSetting setting : settings){ System.out.println(setting); }
		//		}
		if(DrCollectionTool.isEmpty(settings)){
			return null;
		}
		Collections.sort(settings, new ClusterSettingScopeComparator());
		return DrCollectionTool.getFirst(settings).getValue();
	}

	//TODO should we be making combinations like serverType/instance?
	private List<ClusterSettingKey> generateKeysForSelection(String name){
		List<ClusterSettingKey> keys = new ArrayList<>();

		//remember to use "" instead of null.  should probably make a new Field type to do that for you
		keys.add(new ClusterSettingKey(name, ClusterSettingScope.defaultScope, ServerType.UNKNOWN, EMPTY_STRING,
				EMPTY_STRING));

		keys.add(new ClusterSettingKey(name, ClusterSettingScope.cluster, ServerType.ALL, EMPTY_STRING, EMPTY_STRING));

		ClusterSettingKey serverTypeSetting = getKeyForServerType(name);
		if(serverTypeSetting != null){
			keys.add(serverTypeSetting);
		}

		ClusterSettingKey instanceSetting = getKeyForInstance(name);
		if(instanceSetting != null){
			keys.add(instanceSetting);
		}

		ClusterSettingKey applicationSetting = getKeyForApplication(name);
		if(applicationSetting != null){
			keys.add(applicationSetting);
		}

		return keys;
	}

	private ClusterSettingKey getKeyForServerType(String name){
		ServerType serverType = clusterSettingFinderConfig.getServerType();
		if(serverType == null || serverType.getPersistentString().equals(ServerType.UNKNOWN)){
			return null;
		}
		return new ClusterSettingKey(name, ClusterSettingScope.serverType, serverType.getPersistentString(),
				EMPTY_STRING, EMPTY_STRING);
	}

	private ClusterSettingKey getKeyForInstance(String name){
		String instance = clusterSettingFinderConfig.getInstanceId();
		if(DrStringTool.isEmpty(instance)){
			return null;
		}
		return new ClusterSettingKey(name, ClusterSettingScope.instance, ServerType.UNKNOWN, instance, EMPTY_STRING);
	}

	private ClusterSettingKey getKeyForApplication(String name){
		String application = clusterSettingFinderConfig.getApplication();
		if(DrStringTool.isEmpty(application)){
			return null;
		}
		return new ClusterSettingKey(name, ClusterSettingScope.application, ServerType.UNKNOWN, application, 
				EMPTY_STRING);
	}

}
