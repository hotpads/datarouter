/**
 * Copyright © 2009 HotPads (admin@hotpads.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.datarouter.storage.config;

import java.io.InputStreamReader;
import java.io.Reader;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

import io.datarouter.storage.config.profile.ConfigProfile;
import io.datarouter.storage.servertype.ServerType;
import io.datarouter.util.collection.CollectionTool;
import io.datarouter.util.io.FileTool;
import io.datarouter.util.io.ReaderTool;
import io.datarouter.util.properties.PropertiesTool;
import io.datarouter.util.string.StringTool;

public abstract class DatarouterProperties{
	private static final Logger logger = LoggerFactory.getLogger(DatarouterProperties.class);

	private static final String JVM_ARG_PREFIX = "datarouter.";
	private static final String CONFIG_DIRECTORY = "config.directory";

	private static final String ENVIRONMENT = "environment";
	private static final String CONFIG_PROFILE = "configProfile";
	private static final String SERVER_PUBLIC_IP = "server.publicIp";
	private static final String SERVER_PRIVATE_IP = "server.privateIp";
	private static final String SERVER_NAME = "server.name";
	private static final String SERVER_TYPE = "server.type";
	private static final String SERVER_CLUSTER_DOMAINS = "server.clusterDomains";
	private static final String ADMINISTRATOR_EMAIL = "administrator.email";
	protected static final String INTERNAL_CONFIG_DIRECTORY = "internalConfigDirectory";

	private static final String EC2_PRIVATE_IP_URL = "http://instance-data/latest/meta-data/local-ipv4";
	private static final String EC2_PUBLIC_IP_URL = "http://instance-data/latest/meta-data/public-ipv4";

	private final String webappName;
	protected final String configDirectory;
	protected final String configFileLocation;

	private final String environment;
	private final String configProfile;
	private final String serverName;
	private final ServerType serverType;
	private final String administratorEmail;
	private final String privateIp;
	private final String publicIp;
	private final Collection<String> clusterDomains;
	protected final String internalConfigDirectory;

	/*----------------- construct ------------------*/

	protected DatarouterProperties(ServerType serverTypeOptions, String serviceName,
			String configDirectory, String filename){
		this(serverTypeOptions, serviceName, configDirectory, true, false, filename, true);
	}

	private DatarouterProperties(ServerType serverTypeOptions, String webappName,
			String configDirectory, boolean directoryRequired, boolean directoryFromJvmArg, String filename,
			boolean fileRequired){
		boolean fileRequiredWithoutDirectoryRequired = fileRequired && !directoryRequired;
		Preconditions.checkState(!fileRequiredWithoutDirectoryRequired, "directory is required if file is required");

		this.webappName = webappName;

		//find configDirectory first
		this.configDirectory = configDirectory;
		if(configDirectory != null){
			FileTool.createFileParents(configDirectory + "/anything");
			if(directoryFromJvmArg){
				logJvmArgSource(CONFIG_DIRECTORY, configDirectory, JVM_ARG_PREFIX + CONFIG_DIRECTORY);
			}else{
				logSource(CONFIG_DIRECTORY, configDirectory, "constant");
			}
		}else{
			Preconditions.checkState(!directoryRequired, "configDirectory required but not found");
		}

		//find configPath
		if(StringTool.isEmpty(filename)){
			Preconditions.checkState(!fileRequired);
			this.configFileLocation = null;
		}else{
			this.configFileLocation = configDirectory + "/" + filename;
		}
		if(configFileLocation != null){
			logSource("config file", configFileLocation, "constant");
		}

		//maybe parse configFileProperties
		Optional<Properties> configFileProperties = Optional.empty();
		if(configFileLocation != null){
			try{
				configFileProperties = Optional.of(PropertiesTool.parse(configFileLocation));
				logConfigFileProperties(configFileProperties);
			}catch(Exception e){
				logger.error("couldn't parse properties file {}", configFileLocation);
			}
		}

		//find remaining fields
		this.environment = findEnvironment(configFileProperties);
		this.configProfile = findConfigProfile(configFileProperties);
		this.serverName = findServerName(configFileProperties);
		this.serverType = serverTypeOptions.fromPersistentString(findServerTypeString(configFileProperties));
		this.administratorEmail = findAdministratorEmail(configFileProperties);
		this.privateIp = findPrivateIp(configFileProperties);
		this.publicIp = findPublicIp(configFileProperties);
		this.clusterDomains = findClusterDomains(configFileProperties);
		this.internalConfigDirectory = findInternalConfigDirectory(configFileProperties);
	}

	/*--------------- methods to find config values -----------------*/

	//prefer jvmArg then configFile
	private String findEnvironment(Optional<Properties> configFileProperties){
		Optional<String> jvmValue = getJvmArg(ENVIRONMENT);
		if(jvmValue.isPresent()){
			return jvmValue.get();
		}
		if(configFileProperties.isPresent()){
			Optional<String> value = configFileProperties.map(properties -> properties.getProperty(ENVIRONMENT));
			if(value.isPresent()){
				logSource(ENVIRONMENT, value.get(), configFileLocation);
				return value.get();
			}
		}
		logger.error("couldn't find {}", ENVIRONMENT);
		return null;
	}

	//prefer jvmArg then configFile
	private String findConfigProfile(Optional<Properties> configFileProperties){
		Optional<String> jvmValue = getJvmArg(CONFIG_PROFILE);
		if(jvmValue.isPresent()){
			return jvmValue.get();
		}
		if(configFileProperties.isPresent()){
			Optional<String> value = configFileProperties.map(properties -> properties.getProperty(CONFIG_PROFILE));
			if(value.isPresent()){
				logSource(CONFIG_PROFILE, value.get(), configFileLocation);
				return value.get();
			}
		}
		String defaultValue = ConfigProfile.DEVELOPMENT.get().getPersistentString();
		logger.error("couldn't find {}, defaulting to {}", CONFIG_PROFILE, defaultValue);
		return defaultValue;
	}

	//prefer configFile then hostname
	private String findServerName(Optional<Properties> configFileProperties){
		Optional<String> jvmValue = getJvmArg(SERVER_NAME);
		if(jvmValue.isPresent()){
			return jvmValue.get();
		}
		if(configFileProperties.isPresent()){
			Optional<String> value = configFileProperties.map(properties -> properties.getProperty(SERVER_NAME));
			if(value.isPresent()){
				logSource(SERVER_NAME, value.get(), configFileLocation);
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
		Optional<String> jvmValue = getJvmArg(SERVER_TYPE);
		if(jvmValue.isPresent()){
			return jvmValue.get();
		}
		if(configFileProperties.isPresent()){
			Optional<String> value = configFileProperties.map(properties -> properties.getProperty(SERVER_TYPE));
			if(value.isPresent()){
				logSource(SERVER_TYPE, value.get(), configFileLocation);
				return value.get();
			}
		}
		logger.error("couldn't find {}", SERVER_TYPE);
		return null;
	}

	//prefer jvmArg then configFile
	private String findAdministratorEmail(Optional<Properties> configFileProperties){
		Optional<String> jvmValue = getJvmArg(ADMINISTRATOR_EMAIL);
		if(jvmValue.isPresent()){
			return jvmValue.get();
		}
		if(configFileProperties.isPresent()){
			Optional<String> value = configFileProperties.map(properties -> properties.getProperty(
					ADMINISTRATOR_EMAIL));
			if(value.isPresent()){
				logSource(ADMINISTRATOR_EMAIL, value.get(), configFileLocation);
				return value.get();
			}
		}
		logger.error("couldn't find {}", ADMINISTRATOR_EMAIL);
		return null;
	}

	//prefer jvmArg, configFile then api call
	private String findPrivateIp(Optional<Properties> configFileProperties){
		Optional<String> jvmValue = getJvmArg(SERVER_PRIVATE_IP);
		if(jvmValue.isPresent()){
			return jvmValue.get();
		}
		if(configFileProperties.isPresent()){
			Optional<String> value = configFileProperties.map(properties -> properties.getProperty(SERVER_PRIVATE_IP));
			if(value.isPresent()){
				logSource(SERVER_PRIVATE_IP, value.get(), configFileLocation);
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

	//prefer jvmArg, then, configFile then api call
	private String findPublicIp(Optional<Properties> configFileProperties){
		Optional<String> jvmValue = getJvmArg(SERVER_PUBLIC_IP);
		if(jvmValue.isPresent()){
			return jvmValue.get();
		}
		if(configFileProperties.isPresent()){
			Optional<String> value = configFileProperties.map(properties -> properties.getProperty(SERVER_PUBLIC_IP));
			if(value.isPresent()){
				logSource(SERVER_PUBLIC_IP, value.get(), configFileLocation);
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

	//prefer jvmArg then configFile
	private Collection<String> findClusterDomains(Optional<Properties> configFileProperties){
		Optional<String> jvmValue = getJvmArg(SERVER_CLUSTER_DOMAINS);
		if(jvmValue.isPresent()){
			return parseClusterDomains(jvmValue.get());
		}
		if(configFileProperties.isPresent()){
			Optional<String> value = configFileProperties.map(properties -> properties.getProperty(
					SERVER_CLUSTER_DOMAINS));
			if(value.isPresent()){
				logSource(SERVER_CLUSTER_DOMAINS, value.get(), configFileLocation);
				return value.map(this::parseClusterDomains).get();
			}
		}
		logger.error("couldn't find {}", SERVER_CLUSTER_DOMAINS);
		return Collections.emptyList();
	}

	//prefer jvmArg then configFile
	private String findInternalConfigDirectory(Optional<Properties> configFileProperties){
		Optional<String> jvmValue = getJvmArg(INTERNAL_CONFIG_DIRECTORY);
		if(jvmValue.isPresent()){
			return jvmValue.get();
		}
		if(configFileProperties.isPresent()){
			Optional<String> value = configFileProperties.map(properties -> properties.getProperty(
					INTERNAL_CONFIG_DIRECTORY));
			if(value.isPresent()){
				logSource(INTERNAL_CONFIG_DIRECTORY, value.get(), configFileLocation);
				return value.get();
			}
		}
		logger.error("couldn't find {}", INTERNAL_CONFIG_DIRECTORY);
		return null;
	}

	/*------------------- private -------------------------*/

	private Optional<String> getJvmArg(String jvmArg){
		String jvmArgName = JVM_ARG_PREFIX + jvmArg;
		String jvmArgValue = System.getProperty(jvmArgName);
		if(jvmArgValue == null){
			return Optional.empty();
		}
		logJvmArgSource(jvmArg, jvmArgValue, jvmArgName);
		return Optional.of(jvmArgValue);
	}

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
			String content = ReaderTool.accumulateStringAndClose(reader);
			return Optional.of(content);
		}catch(Exception e){
			if(logError){
				logger.error("error reading {}", location, e);
			}
			return Optional.empty();
		}
	}

	private Collection<String> parseClusterDomains(String clusterDomainsProperty){
		if(StringTool.isNullOrEmptyOrWhitespace(clusterDomainsProperty)){
			return Collections.emptyList();
		}
		return Collections.unmodifiableList(Stream.of(clusterDomainsProperty.split(","))
				.filter(StringTool::notEmptyNorWhitespace)
				.map(String::trim)
				.collect(Collectors.toList()));
	}

	/*------------------ methods ---------------*/

	public String getServerTypeString(){
		return Optional.ofNullable(serverType).map(ServerType::getPersistentString).orElse(null);
	}

	public String findConfigFile(String filename){
		String externalLocation = configDirectory + "/" + filename;
		if(Files.exists(Paths.get(externalLocation))){
			return externalLocation;
		}
		Objects.requireNonNull(internalConfigDirectory, externalLocation + " doesn't exist and "
				+ INTERNAL_CONFIG_DIRECTORY + " property is not set");
		return "/config/" + internalConfigDirectory + "/" + filename;
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

	public Collection<String> getServerClusterDomains(){
		return clusterDomains;
	}

	public String getAdministratorEmail(){
		return administratorEmail;
	}

	public String getConfigDirectory(){
		return configDirectory;
	}

	public String getConfigFileLocation(){
		return configFileLocation;
	}

	public String getEnvironment(){
		return environment;
	}

	public String getConfigProfile(){
		return configProfile;
	}

	public String getWebappName(){
		return webappName;
	}

	public String getFirstServerClusterDomain(){
		return CollectionTool.getFirst(getServerClusterDomains());
	}

	public abstract String getDatarouterPropertiesFileLocation();

}