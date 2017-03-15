package com.hotpads.datarouter.config;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Optional;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hotpads.datarouter.setting.ServerType;
import com.hotpads.datarouter.util.core.DrPropertiesTool;

public abstract class DatarouterProperties{
	private static final Logger logger = LoggerFactory.getLogger(DatarouterProperties.class);

	private static final String SERVER_PUBLIC_IP = "server.publicIp";
	private static final String SERVER_PRIVATE_IP = "server.privateIp";
//	private static final String SERVER_NAME = "server.name";
	private static final String SERVER_TYPE = "server.type";
	private static final String ADMINISTRATOR_EMAIL = "administrator.email";

	//dynamic from InetAddress.getLocalHost().getHostName()
	private final String serverName;

	//JVM args
	private final Optional<String> configPath;

	//from config file
	private final String publicIp;
	private final String privateIp;
	private final String administratorEmail;

	private ServerType serverType;


	/*----------------- construct ------------------*/

	protected DatarouterProperties(ServerType serverTypeOptions){
		this(serverTypeOptions, System.getProperty("configDirectory"));
	}

	protected DatarouterProperties(ServerType serverTypeOptions, String filePath){
		this.configPath = Optional.ofNullable(filePath);
		logger.warn("configPath={}", configPath.orElse("unknown"));
		this.serverName = findServerName();
		Optional<Properties> configFileProperties = Optional.empty();
		if(configPath.isPresent()){
			try{
				configFileProperties = Optional.of(DrPropertiesTool.parse(configPath.get()));
				logConfigFileProperties(configFileProperties);
			}catch(Exception e){
				logger.warn("couldn't parse configFileProperties at configPath={}", configPath);
			}
		}
		this.serverType = serverTypeOptions.fromPersistentString(findServerTypeString(configFileProperties));
		this.publicIp = findPublicIp(configFileProperties);
		this.privateIp = findPrivateIp(configFileProperties);
		this.administratorEmail = findAdministratorEmail(configFileProperties);
	}


	private String findServerName(){
		try{
			return InetAddress.getLocalHost().getHostName();
		}catch(UnknownHostException e){
			throw new RuntimeException(e);
		}
	}

	private String findServerTypeString(Optional<Properties> configFileProperties){
		String arg = System.getProperty(SERVER_TYPE);
		if(arg != null){
			logger.warn("found server.type={} from JVM arg", arg);
			return arg;
		}
		String prop = configFileProperties.map(properties -> properties.getProperty(SERVER_TYPE)).orElse(null);
		logger.warn("found server.type={} from {}", arg, configPath);
		return prop;
	}

	private String findPublicIp(Optional<Properties> configFileProperties){
		if(configFileProperties == null){
			return null;
		}
		return configFileProperties.map(properties -> properties.getProperty(SERVER_PUBLIC_IP)).orElse(null);
	}

	private String findPrivateIp(Optional<Properties> configFileProperties){
		if(configFileProperties == null){
			return null;
		}
		return configFileProperties.map(properties -> properties.getProperty(SERVER_PRIVATE_IP)).orElse(null);
	}

	private String findAdministratorEmail(Optional<Properties> configFileProperties){
		if(configFileProperties == null){
			return null;
		}
		return configFileProperties.map(properties -> properties.getProperty(ADMINISTRATOR_EMAIL)).orElse(null);
	}

	private void logConfigFileProperties(final Optional<Properties> configFileProperties){
		configFileProperties.get().stringPropertyNames().stream()
				.map(name -> name + "=" + configFileProperties.get().getProperty(name))
				.sorted()
				.forEach(logger::warn);
	}

	/*------------------ methods ---------------*/

	public String getServerTypeString(){
		return serverType.getPersistentString();
	}

	public String getConfigPath(){
		return configPath.orElse(null);
	}

	/*---------------- getters -------------------*/

	public String getServerName(){
		return serverName;
	}

	public ServerType getServerType(){
		return serverType;
	}

	public String getServerPublicIp(){
		return publicIp;
	}

	public String getServerPrivateIp(){
		return privateIp;
	}

	public String getAdministratorEmail(){
		return administratorEmail;
	}

}