package com.hotpads.setting.cluster;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.text.ParseException;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.log4j.Logger;
import org.quartz.CronExpression;

import com.google.inject.BindingAnnotation;
import com.hotpads.datarouter.node.op.combo.SortedMapStorage.SortedMapStorageNode;
import com.hotpads.setting.ClusterSettingFinderConfig;
import com.hotpads.setting.DatarouterServerType;
import com.hotpads.setting.DatarouterServerType.DatarouterServerTypeTool;
import com.hotpads.setting.cached.imp.Duration;
import com.hotpads.util.core.BooleanTool;
import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.ListTool;
import com.hotpads.util.core.StringTool;

@Singleton
public class ClusterSettingFinder {

	@BindingAnnotation 
	@Target({ ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD }) 
	@Retention(RetentionPolicy.RUNTIME)
	public @interface ClusterSettingNode {}

	protected static Logger logger = Logger.getLogger(ClusterSettingFinder.class);

	public static final String PREFIX_trigger = "trigger.";
	public static final String EMPTY_STRING = "";

	protected ClusterSettingFinderConfig clusterSettingFinderConfig;
	protected DatarouterServerTypeTool datarouterServerTypeTool;
	protected SortedMapStorageNode<ClusterSettingKey,ClusterSetting> clusterSetting;

	@Inject
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public ClusterSettingFinder(ClusterSettingFinderConfig clusterSettingFinderConfig, DatarouterServerTypeTool datarouterServerTypeTool ,@ClusterSettingNode SortedMapStorageNode clusterSetting) {
		this.clusterSettingFinderConfig = clusterSettingFinderConfig;
		this.datarouterServerTypeTool = datarouterServerTypeTool;
		this.clusterSetting = clusterSetting;
	}

	public SortedMapStorageNode<ClusterSettingKey, ClusterSetting> getClusterSetting() {
		return clusterSetting;
	}

	public void setClusterSetting(SortedMapStorageNode<ClusterSettingKey, ClusterSetting> clusterSetting) {
		this.clusterSetting = clusterSetting;
	}

	public Integer getInteger(String name, Integer defaultValue){
		String valueString = getMostSpecificValue(name);
		if(valueString==null){ return defaultValue; }
		return Integer.valueOf(valueString);
	}

	public Boolean getBoolean(String name, Boolean defaultValue) {
		String valueString = getMostSpecificValue(name);
		if(valueString==null){ return defaultValue; }
		return BooleanTool.isTrue(valueString);
	}

	public String getString(String name, String defaultValue){
		String valueString = getMostSpecificValue(name);
		if(valueString==null){ return defaultValue; }
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
		if(valueString==null){ return defaultValue; }
		try{
			return new Duration(valueString);
		}catch(RuntimeException e){
			logger.warn("ParseException on "+name+" with value "+valueString);
			throw new RuntimeException(e);
		}
	}
	
	private String getMostSpecificValue(String name){
		List<ClusterSettingKey> keys = generateKeysForSelection(name);
		//		boolean log = "job.event.aggregateEvents".equals(name);
		//		if(log){
		//			logger.warn("searching for "+CollectionTool.size(keys)+":");
		//			for(ClusterSettingKey key : keys){ logger.warn(key); }
		//		}
		List<ClusterSetting> settings = clusterSetting.getMulti(keys, null);
		//		if(log){
		//			logger.warn("found "+CollectionTool.size(settings)+":");
		//			for(ClusterSetting setting : settings){ System.out.println(setting); }
		//		}
		if(CollectionTool.isEmpty(settings)){ return null; }
		Collections.sort(settings, new ClusterSettingScopeComparator());
		return CollectionTool.getFirst(settings).getValue();
	}

	//TODO should we be making combinations like serverType/instance?
	private List<ClusterSettingKey> generateKeysForSelection(String name){
		List<ClusterSettingKey> keys = ListTool.createArrayList();

		//remember to use "" instead of null.  should probably make a new Field type to do that for you
		keys.add(new ClusterSettingKey(name, ClusterSettingScope.defaultScope, datarouterServerTypeTool.getUNKNOWNPersistentString(), EMPTY_STRING,
				EMPTY_STRING));

		keys.add(new ClusterSettingKey(name, ClusterSettingScope.cluster, datarouterServerTypeTool.getALLPersistentString(), EMPTY_STRING, EMPTY_STRING));

		ClusterSettingKey serverTypeSetting = getKeyForServerType(name);
		if(serverTypeSetting != null){ keys.add(serverTypeSetting); }

		ClusterSettingKey instanceSetting = getKeyForInstance(name);
		if(instanceSetting != null){ keys.add(instanceSetting); }

		ClusterSettingKey applicationSetting = getKeyForApplication(name);
		if(applicationSetting != null){ keys.add(applicationSetting); }

		return keys;
	}

	private ClusterSettingKey getKeyForServerType(String name){
		DatarouterServerType serverType = clusterSettingFinderConfig.getServerType();
		if(serverType == null || serverType.getPersistentString().equals(datarouterServerTypeTool.getUNKNOWNPersistentString())){ return null; }
		return new ClusterSettingKey(name, ClusterSettingScope.serverType, serverType.getPersistentString(), EMPTY_STRING,
				EMPTY_STRING);
	}

	private ClusterSettingKey getKeyForInstance(String name){
		String instance = clusterSettingFinderConfig.getInstanceId();
		if(StringTool.isEmpty(instance)){ return null; }
		return new ClusterSettingKey(name, ClusterSettingScope.instance, datarouterServerTypeTool.getUNKNOWNPersistentString(), instance, EMPTY_STRING);
	}

	private ClusterSettingKey getKeyForApplication(String name){
		String application = clusterSettingFinderConfig.getApplication();
		if(StringTool.isEmpty(application)){ return null; }
		return new ClusterSettingKey(name, ClusterSettingScope.application, datarouterServerTypeTool.getUNKNOWNPersistentString(), application, 
				EMPTY_STRING);
	}

}
