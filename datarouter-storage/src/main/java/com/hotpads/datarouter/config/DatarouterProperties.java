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
	private static final String SERVER_NAME = "server.name";
	private static final String SERVER_TYPE = "server.type";
	private static final String ADMINISTRATOR_EMAIL = "administrator.email";

	private final Optional<String> configPath;

	private final String serverName;
	private final ServerType serverType;
	private final String administratorEmail;
	private final String privateIp;
	private final String publicIp;



	/*----------------- construct ------------------*/

	protected DatarouterProperties(ServerType serverTypeOptions){
		this(serverTypeOptions, System.getProperty("configDirectory"));
	}

	protected DatarouterProperties(ServerType serverTypeOptions, String filePath){
		this.configPath = Optional.ofNullable(filePath);
		logger.error("configPath={}", configPath.orElse("unknown"));
		this.serverName = findServerName();
		Optional<Properties> configFileProperties = Optional.empty();
		if(configPath.isPresent()){
			try{
				configFileProperties = Optional.of(DrPropertiesTool.parse(configPath.get()));
				logConfigFileProperties(configFileProperties);
			}catch(Exception e){
				logger.error("couldn't parse configFileProperties at configPath={}", configPath);
			}
		}
		this.serverType = serverTypeOptions.fromPersistentString(findServerTypeString(configFileProperties));
		this.administratorEmail = findAdministratorEmail(configFileProperties);
		this.privateIp = findPrivateIp(configFileProperties);
		this.publicIp = findPublicIp(configFileProperties);
	}


	private String findServerName(){
		try{
			String hostname = InetAddress.getLocalHost().getHostName();
			logger.error("found {}={} from InetAddress.getLocalHost().getHostName()", SERVER_NAME, hostname);
			return hostname;
		}catch(UnknownHostException e){
			throw new RuntimeException(e);
		}
	}

	private String findServerTypeString(Optional<Properties> configFileProperties){
		String jvmArg = System.getProperty(SERVER_TYPE);
		if(jvmArg != null){
			logger.error("found {}={} from JVM arg", SERVER_TYPE, jvmArg);
			return jvmArg;
		}
		if(configFileProperties.isPresent()){
			String serverType = configFileProperties.map(properties -> properties.getProperty(SERVER_TYPE)).get();
			logger.error("found {}={} from {}", SERVER_TYPE, jvmArg, configPath);
			return serverType;
		}
		logger.error("couldn't find {}", SERVER_TYPE);
		return null;
	}

	private String findAdministratorEmail(Optional<Properties> configFileProperties){
		String jvmArg = System.getProperty(ADMINISTRATOR_EMAIL);
		if(jvmArg != null){
			logger.error("found {}={} from JVM arg", ADMINISTRATOR_EMAIL, jvmArg);
			return jvmArg;
		}
		if(configFileProperties.isPresent()){
			Optional<String> value = configFileProperties.map(properties -> properties.getProperty(
					ADMINISTRATOR_EMAIL));
			if(value.isPresent()){
				logger.error("found {}={} from {}", ADMINISTRATOR_EMAIL, value.get(), configPath);
				return value.get();
			}
		}
		logger.error("couldn't find {}", ADMINISTRATOR_EMAIL);
		return null;
	}

	private String findPrivateIp(Optional<Properties> configFileProperties){
		if(configFileProperties.isPresent()){
			Optional<String> value = configFileProperties.map(properties -> properties.getProperty(SERVER_PRIVATE_IP));
			if(value.isPresent()){
				logger.error("found {}={} from {}", SERVER_PRIVATE_IP, value.get(), configPath);
				return value.get();
			}
		}
		logger.error("couldn't find {}", SERVER_PRIVATE_IP);
		return null;
	}

	private String findPublicIp(Optional<Properties> configFileProperties){
		if(configFileProperties.isPresent()){
			Optional<String> value = configFileProperties.map(properties -> properties.getProperty(SERVER_PUBLIC_IP));
			if(value.isPresent()){
				logger.error("found {}={} from {}", SERVER_PUBLIC_IP, value.get(), configPath);
				return value.get();
			}
		}
		logger.error("couldn't find {}", SERVER_PUBLIC_IP);
		return null;
	}

	private void logConfigFileProperties(final Optional<Properties> configFileProperties){
		configFileProperties.get().stringPropertyNames().stream()
				.map(name -> name + "=" + configFileProperties.get().getProperty(name))
				.sorted()
				.forEach(logger::error);
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