package com.hotpads.datarouter.config;

import java.io.File;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Optional;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.hotpads.datarouter.setting.ServerType;
import com.hotpads.datarouter.util.core.DrFileUtils;
import com.hotpads.datarouter.util.core.DrPropertiesTool;
import com.hotpads.datarouter.util.core.DrStringTool;
import com.hotpads.util.core.io.ReaderTool;

public abstract class DatarouterProperties{
	private static final Logger logger = LoggerFactory.getLogger(DatarouterProperties.class);

	private static final String JVM_ARG_PREFIX = "datarouter.";
	private static final String CONFIG_DIRECTORY = "config.directory";
	private static final String CONFIG_STRATEGY = "config.strategy";

	private static final String SERVER_PUBLIC_IP = "server.publicIp";
	private static final String SERVER_PRIVATE_IP = "server.privateIp";
	private static final String SERVER_NAME = "server.name";
	private static final String SERVER_TYPE = "server.type";
	private static final String ADMINISTRATOR_EMAIL = "administrator.email";

	private static final String EC2_PRIVATE_IP_URL = "http://instance-data/latest/meta-data/local-ipv4";
	private static final String EC2_PUBLIC_IP_URL = "http://instance-data/latest/meta-data/public-ipv4";

	protected final Optional<String> configDirectory;
	protected final Optional<String> configStrategy;
	protected final Optional<String> configPath;

	private final String serverName;
	private final ServerType serverType;
	private final String administratorEmail;
	private final String privateIp;
	private final String publicIp;

	/*----------------- construct ------------------*/

	//require directory from jvmArgs
	protected DatarouterProperties(ServerType serverTypeOptions, boolean directoryRequired){
		this(serverTypeOptions, System.getProperty(JVM_ARG_PREFIX + CONFIG_DIRECTORY), directoryRequired, true, null,
				false);
	}

	//require directory and filename constants
	protected DatarouterProperties(ServerType serverTypeOptions, String directory, String filename){
		this(serverTypeOptions, directory, true, false, filename, true);
	}

	private DatarouterProperties(ServerType serverTypeOptions, String directory, boolean directoryRequired,
			boolean directoryFromJvmArg, String filename, boolean fileRequired){
		boolean fileRequiredWithoutDirectoryRequired = fileRequired && !directoryRequired;
		Preconditions.checkState(!fileRequiredWithoutDirectoryRequired, "directory is required if file is required");

		//find configStrategy
		this.configStrategy = findConfigStrategy();

		//find configDirectory
		this.configDirectory = Optional.of(directory);
		if(configDirectory.isPresent()){
			DrFileUtils.createFileParents(configDirectory + "/anything");
			if(directoryFromJvmArg){
				logJvmArgSource(CONFIG_DIRECTORY, configDirectory.get(), JVM_ARG_PREFIX + CONFIG_DIRECTORY);
			}else{
				logSource(CONFIG_DIRECTORY, configDirectory.get(), "constant");
			}
		}else{
			Preconditions.checkState(!directoryRequired, "configDirectory required but not found");
		}

		//find configPath
		if(DrStringTool.isEmpty(filename)){
			Preconditions.checkState(!fileRequired);
			this.configPath = Optional.empty();
		}else{
			this.configPath = Optional.of(configDirectory + "/" + filename);
		}
		if(configPath.isPresent()){
			logSource("config file", configPath.get(), "constant");
		}

		//maybe parse configFileProperties
		Optional<Properties> configFileProperties = Optional.empty();
		if(configPath.isPresent()){
			try{
				configFileProperties = Optional.of(DrPropertiesTool.parse(configPath.get()));
				logConfigFileProperties(configFileProperties);
			}catch(Exception e){
				logger.error("couldn't parse configFileProperties at configPath={}", configPath.get());
			}
		}

		//find remaining fields
		this.serverName = findServerName(configFileProperties);
		this.serverType = serverTypeOptions.fromPersistentString(findServerTypeString(configFileProperties));
		this.administratorEmail = findAdministratorEmail(configFileProperties);
		this.privateIp = findPrivateIp(configFileProperties);
		this.publicIp = findPublicIp(configFileProperties);
	}

	/*--------------- methods to find config values -----------------*/

	private Optional<String> findConfigStrategy(){
		String jvmArgName = JVM_ARG_PREFIX + CONFIG_STRATEGY;
		Optional<String> value = Optional.ofNullable(System.getProperty(jvmArgName));
		if(value.isPresent()){
			logJvmArgSource(CONFIG_STRATEGY, value.get(), jvmArgName);
		}else{
			logger.warn("JVM arg {} not found", jvmArgName);
		}
		return value;
	}

	//prefer configFile then hostname
	private String findServerName(Optional<Properties> configFileProperties){
		if(configFileProperties.isPresent()){
			Optional<String> value = configFileProperties.map(properties -> properties.getProperty(SERVER_NAME));
			if(value.isPresent()){
				logSource(SERVER_NAME, value.get(), configPath.get());
				return value.get();
			}
		}
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

	//prefer jvmArg then configFile
	private String findServerTypeString(Optional<Properties> configFileProperties){
		String jvmArgName = JVM_ARG_PREFIX + SERVER_TYPE;
		String jvmArg = System.getProperty(jvmArgName);
		if(jvmArg != null){
			logJvmArgSource(SERVER_TYPE, jvmArg, jvmArgName);
			return jvmArg;
		}
		if(configFileProperties.isPresent()){
			Optional<String> value = configFileProperties.map(properties -> properties.getProperty(SERVER_TYPE));
			if(value.isPresent()){
				logSource(SERVER_TYPE, value.get(), configPath.get());
				return value.get();
			}
		}
		logger.error("couldn't find {}", SERVER_TYPE);
		return null;
	}

	//prefer jvmArg then configFile
	private String findAdministratorEmail(Optional<Properties> configFileProperties){
		String jvmArgName = JVM_ARG_PREFIX + ADMINISTRATOR_EMAIL;
		String jvmArg = System.getProperty(jvmArgName);
		if(jvmArg != null){
			logJvmArgSource(ADMINISTRATOR_EMAIL, jvmArg, jvmArgName);
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

	//prefer configFile then api call
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

	//prefer configFile then api call
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
				.forEach(logger::info);
	}

	private void logSource(String name, String value, String source){
		logger.warn("found {}={} from {}", name, value, source);
	}

	private void logJvmArgSource(String name, String value, String jvmArgName){
		logger.warn("found {}={} from -D{} JVM arg", name, value, jvmArgName);
	}

	//TODO http://docs.aws.amazon.com/AWSEC2/latest/UserGuide/identify_ec2_instances.html
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
		return Optional.ofNullable(serverType).map(ServerType::getPersistentString).orElse(null);
	}

	public String getConfigPath(){
		return configPath.orElse(null);
	}

	public void assertConfigFileExists(String filename){
		String fileLocation = configDirectory.get() + "/" + filename;
		File file = new File(fileLocation);
		if(!file.exists()){
			throw new RuntimeException("required file " + file.getAbsolutePath() + " is missing");
		}
		logger.warn("required file {} exists", file.getAbsolutePath());
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

	public Optional<String> getConfigDirectory(){
		return configDirectory;
	}

	public Optional<String> getConfigStrategy(){
		return configStrategy;
	}

}