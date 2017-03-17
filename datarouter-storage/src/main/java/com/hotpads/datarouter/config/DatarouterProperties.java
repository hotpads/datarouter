package com.hotpads.datarouter.config;

import java.io.InputStreamReader;
import java.io.Reader;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Optional;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hotpads.datarouter.setting.ServerType;
import com.hotpads.datarouter.util.core.DrPropertiesTool;
import com.hotpads.datarouter.util.core.DrStringTool;
import com.hotpads.util.core.io.ReaderTool;

public abstract class DatarouterProperties{
	private static final Logger logger = LoggerFactory.getLogger(DatarouterProperties.class);

	private static final String JVM_ARG_PREFIX = "datarouter.";
	private static final String CONFIG_DIRECTORY = "config.directory";

	private static final String SERVER_PUBLIC_IP = "server.publicIp";
	private static final String SERVER_PRIVATE_IP = "server.privateIp";
	private static final String SERVER_NAME = "server.name";
	private static final String SERVER_TYPE = "server.type";
	private static final String ADMINISTRATOR_EMAIL = "administrator.email";

	private static final String EC2_PRIVATE_IP_URL = "http://instance-data/latest/meta-data/local-ipv4";
	private static final String EC2_PUBLIC_IP_URL = "http://instance-data/latest/meta-data/public-ipv4";

	private final Optional<String> configDirectory;
	private final Optional<String> configPath;

	private final String serverName;
	private final ServerType serverType;
	private final String administratorEmail;
	private final String privateIp;
	private final String publicIp;

	/*----------------- construct ------------------*/

	protected DatarouterProperties(ServerType serverTypeOptions){
		this(serverTypeOptions, System.getProperty(JVM_ARG_PREFIX + CONFIG_DIRECTORY), null);
	}

	/**
	 * @deprecated pass path via JVM arg
	 */
	@Deprecated
	protected DatarouterProperties(ServerType serverTypeOptions, String directory, String filename){
		this.configDirectory = Optional.ofNullable(directory);
		if(configDirectory.isPresent()){
			logSource(CONFIG_DIRECTORY, configDirectory.get(), "?");
		}
		if(configDirectory.isPresent() && DrStringTool.notEmpty(filename)){
			this.configPath = Optional.of(configDirectory.get() + "/" + filename);
		}else{
			this.configPath = Optional.empty();
		}
		if(configPath.isPresent()){
			logSource("config file", configPath.get(), "constructor");
		}
		this.serverName = findServerName();
		Optional<Properties> configFileProperties = Optional.empty();
		if(configPath.isPresent()){
			try{
				configFileProperties = Optional.of(DrPropertiesTool.parse(configPath.get()));
				logConfigFileProperties(configFileProperties);
			}catch(Exception e){
				logger.warn("couldn't parse configFileProperties at configPath={}", configPath.get());
			}
		}
		this.serverType = serverTypeOptions.fromPersistentString(findServerTypeString(configFileProperties));
		this.administratorEmail = findAdministratorEmail(configFileProperties);
		this.privateIp = findPrivateIp(configFileProperties);
		this.publicIp = findPublicIp(configFileProperties);
	}

	/*--------------- methods to find config values -----------------*/

	private String findServerName(){
		try{
			String hostname = InetAddress.getLocalHost().getHostName();
			String source = "InetAddress.getLocalHost().getHostName()";
			if(hostname.contains(".")){
				hostname = hostname.substring(0, hostname.indexOf('.'));//drop the dns suffixes
				source += ".substring(0, hostname.indexOf('.')";
			}
			logSource(SERVER_NAME, hostname, source);
			return hostname;
		}catch(UnknownHostException e){
			throw new RuntimeException(e);
		}
	}

	private String findServerTypeString(Optional<Properties> configFileProperties){
		String jvmArgName = JVM_ARG_PREFIX + SERVER_TYPE;
		String jvmArg = System.getProperty(jvmArgName);
		if(jvmArg != null){
			logSource(SERVER_TYPE, jvmArg, jvmArgName + " JVM arg");
			return jvmArg;
		}
		if(configFileProperties.isPresent()){
			String serverType = configFileProperties.map(properties -> properties.getProperty(SERVER_TYPE)).get();
			logSource(SERVER_TYPE, jvmArg, configPath.get());
			return serverType;
		}
		logger.error("couldn't find {}", SERVER_TYPE);
		return null;
	}

	private String findAdministratorEmail(Optional<Properties> configFileProperties){
		String jvmArgName = JVM_ARG_PREFIX + ADMINISTRATOR_EMAIL;
		String jvmArg = System.getProperty(jvmArgName);
		if(jvmArg != null){
			logSource(ADMINISTRATOR_EMAIL, jvmArg, jvmArgName + " JVM arg");
			return jvmArg;
		}
		if(configFileProperties.isPresent()){
			Optional<String> value = configFileProperties.map(properties -> properties.getProperty(
					ADMINISTRATOR_EMAIL));
			if(value.isPresent()){
				logSource(ADMINISTRATOR_EMAIL, value.get(), configPath.get());
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
				logSource(SERVER_PRIVATE_IP, value.get(), configPath.get());
				return value.get();
			}
		}
		if(isEc2()){
			Optional<String> ip = curl(EC2_PRIVATE_IP_URL, true);
			if(ip.isPresent()){
				logSource(SERVER_PRIVATE_IP, ip.get(), EC2_PRIVATE_IP_URL);
				return ip.get();
			}
		}
		logger.error("couldn't find {}", SERVER_PRIVATE_IP);
		return null;
	}

	private String findPublicIp(Optional<Properties> configFileProperties){
		if(configFileProperties.isPresent()){
			Optional<String> value = configFileProperties.map(properties -> properties.getProperty(SERVER_PUBLIC_IP));
			if(value.isPresent()){
				logSource(SERVER_PUBLIC_IP, value.get(), configPath.get());
				return value.get();
			}
		}
		if(isEc2()){
			Optional<String> ip = curl(EC2_PUBLIC_IP_URL, true);
			if(ip.isPresent()){
				logSource(SERVER_PUBLIC_IP, ip.get(), EC2_PUBLIC_IP_URL);
				return ip.get();
			}
		}
		logger.error("couldn't find {}", SERVER_PUBLIC_IP);
		return null;
	}

	/*------------------- private -------------------------*/

	private void logConfigFileProperties(final Optional<Properties> configFileProperties){
		configFileProperties.get().stringPropertyNames().stream()
				.map(name -> name + "=" + configFileProperties.get().getProperty(name))
				.sorted()
				.forEach(logger::error);
	}

	private void logSource(String name, String value, String source){
		logger.warn("found {}={} from {}", name, value, source);
	}

	private boolean isEc2(){
		return curl(EC2_PRIVATE_IP_URL, false).isPresent();
	}

	private Optional<String> curl(String location, boolean logError){
		try{
			URL url = new URL(location);
			Reader reader = new InputStreamReader(url.openStream(), "UTF-8");
			String content = ReaderTool.accumulateStringAndClose(reader).toString();
			return Optional.of(content);
		}catch(Exception e){
			if(logError){
				logger.error("error reading {}", location, e);
			}
			return Optional.empty();
		}
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