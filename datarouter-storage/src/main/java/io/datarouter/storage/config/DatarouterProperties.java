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

import io.datarouter.storage.config.environment.EnvironmentType;
import io.datarouter.storage.servertype.ServerType;
import io.datarouter.storage.servertype.ServerTypes;
import io.datarouter.util.Require;
import io.datarouter.util.collection.CollectionTool;
import io.datarouter.util.io.FileTool;
import io.datarouter.util.io.ReaderTool;
import io.datarouter.util.properties.PropertiesTool;
import io.datarouter.util.serialization.GsonTool;
import io.datarouter.util.string.StringTool;

public abstract class DatarouterProperties{
	private static final Logger logger = LoggerFactory.getLogger(DatarouterProperties.class);

	private static final String JVM_ARG_PREFIX = "datarouter.";
	private static final String CONFIG_DIRECTORY_PROP = "config.directory";

	private static final String ENVIRONMENT = "environment";
	private static final String ENVIRONMENT_DOMAIN = "environmentDomain";
	private static final String ENVIRONMENT_TYPE = "environmentType";
	private static final String SERVER_PUBLIC_IP = "server.publicIp";
	private static final String SERVER_PRIVATE_IP = "server.privateIp";
	private static final String SERVER_NAME = "server.name";
	private static final String SERVER_TYPE = "server.type";
	private static final String SERVER_CLUSTER_DOMAINS = "server.clusterDomains";
	private static final String ADMINISTRATOR_EMAIL = "administrator.email";
	private static final String INTERNAL_CONFIG_DIRECTORY_PROP = "internalConfigDirectory";

	private static final String EC2_INSTANCE_IDENTITY_DOCUMENT_URL =
			"http://169.254.169.254/latest/dynamic/instance-identity/document";
	private static final String EC2_PRIVATE_IP_URL = "http://169.254.169.254/latest/meta-data/local-ipv4";
	private static final String EC2_PUBLIC_IP_URL = "http://169.254.169.254/latest/meta-data/public-ipv4";

	private static final String BASE_CONFIG_DIRECTORY_ENV_VARIABLE = "BASE_CONFIG_DIRECTORY";
	private static final String SERVER_CONFIG_FILE_NAME = "server.properties";
	private static final String DEFAULT_BASE_CONFIG_DIRECTORY = "/etc/datarouter";
	private static final String CONFIG_DIRECTORY;
	private static final String TEST_CONFIG_DIRECTORY;
	private static String source;

	static{
		String baseConfigDirectory = System.getenv(BASE_CONFIG_DIRECTORY_ENV_VARIABLE);
		source = "environment variable";
		if(StringTool.isEmpty(baseConfigDirectory)){
			baseConfigDirectory = DEFAULT_BASE_CONFIG_DIRECTORY;
			source = "default constant";
		}
		CONFIG_DIRECTORY = baseConfigDirectory + "/config";
		TEST_CONFIG_DIRECTORY = baseConfigDirectory + "/test";
	}

	private final String webappName;
	private final String configDirectory;
	private final String testConfigDirectory;
	private final String configFileLocation;
	private final String environment;
	private final String environmentDomain;
	private final String environmentType;
	private final String serverName;
	private final ServerType serverType;
	private final String administratorEmail;
	private final String privateIp;
	private final String publicIp;
	private final Collection<String> clusterDomains;
	private final String internalConfigDirectory;

	/*----------------- construct ------------------*/

	protected DatarouterProperties(ServerTypes serverTypeOptions, String serviceName){
		this(serverTypeOptions, serviceName, CONFIG_DIRECTORY, SERVER_CONFIG_FILE_NAME);
	}

	protected DatarouterProperties(ServerTypes serverTypeOptions, String serviceName, String configDirectory,
			String filename){
		this(serverTypeOptions, serviceName, configDirectory, true, false, filename, true);
	}

	private DatarouterProperties(ServerTypes serverTypeOptions, String webappName, String configDirectory,
			boolean directoryRequired, boolean directoryFromJvmArg, String filename, boolean fileRequired){
		boolean fileRequiredWithoutDirectoryRequired = fileRequired && !directoryRequired;
		Require.isTrue(!fileRequiredWithoutDirectoryRequired, "directory is required if file is required");

		//find configDirectory first
		this.configDirectory = configDirectory;
		if(configDirectory != null){
			FileTool.createFileParents(configDirectory + "/anything");
			if(directoryFromJvmArg){
				logJvmArgSource(CONFIG_DIRECTORY_PROP, configDirectory, JVM_ARG_PREFIX + CONFIG_DIRECTORY_PROP);
			}else{
				logSource("config directory", configDirectory, source);
			}
		}else{
			Require.isTrue(!directoryRequired, "config directory required but not found");
		}

		//find configPath
		if(StringTool.isEmpty(filename)){
			Require.isTrue(!fileRequired);
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

		this.webappName = webappName;

		//find remaining fields
		this.testConfigDirectory = TEST_CONFIG_DIRECTORY;
		this.environment = findProperty(configFileProperties, ENVIRONMENT);
		this.environmentDomain = findProperty(configFileProperties, ENVIRONMENT_DOMAIN);
		this.environmentType = findEnvironmentType(configFileProperties);
		this.serverName = findServerName(configFileProperties);
		this.serverType = serverTypeOptions.fromPersistentString(findProperty(configFileProperties, SERVER_TYPE));
		this.administratorEmail = findProperty(configFileProperties, ADMINISTRATOR_EMAIL);
		this.privateIp = findPrivateIp(configFileProperties);
		this.publicIp = findPublicIp(configFileProperties);
		this.clusterDomains = findClusterDomains(configFileProperties);
		this.internalConfigDirectory = findProperty(configFileProperties, INTERNAL_CONFIG_DIRECTORY_PROP);
	}

	/*--------------- methods to find config values -----------------*/

	//prefer jvmArg then configFile
	private String findProperty(Optional<Properties> configFileProperties, String propertyName){
		Optional<String> jvmValue = getJvmArg(propertyName);
		if(jvmValue.isPresent()){
			return jvmValue.get();
		}
		if(configFileProperties.isPresent()){
			Optional<String> value = configFileProperties.map(properties -> properties.getProperty(propertyName));
			if(value.isPresent()){
				logSource(propertyName, value.get(), configFileLocation);
				return value.get();
			}
		}
		logger.error("couldn't find {}", propertyName);
		return null;
	}

	private String findEnvironmentType(Optional<Properties> configFileProperties){
		String environmentType = findProperty(configFileProperties, ENVIRONMENT_TYPE);
		if(environmentType != null){
			return environmentType;
		}
		String defaultValue = EnvironmentType.DEVELOPMENT.get().getPersistentString();
		logger.error("couldn't find {}, defaulting to {}", ENVIRONMENT_TYPE, defaultValue);
		return defaultValue;
	}

	//prefer jvmArg, then configFile, then hostname
	private String findServerName(Optional<Properties> configFileProperties){
		String serverName = findProperty(configFileProperties, SERVER_NAME);
		if(serverName != null){
			return serverName;
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

	//prefer jvmArg, configFile then api call
	private String findPrivateIp(Optional<Properties> configFileProperties){
		String privateIp = findProperty(configFileProperties, SERVER_PRIVATE_IP);
		if(privateIp != null){
			return privateIp;
		}
		Optional<String> ip = curl(EC2_PRIVATE_IP_URL, true);
		if(ip.isPresent()){
			logSource(SERVER_PRIVATE_IP, ip.get(), EC2_PRIVATE_IP_URL);
			return ip.get();
		}
		logger.error("couldn't find {}", SERVER_PRIVATE_IP);
		return null;
	}

	//prefer jvmArg, then, configFile then api call
	private String findPublicIp(Optional<Properties> configFileProperties){
		String publicIp = findProperty(configFileProperties, SERVER_PUBLIC_IP);
		if(publicIp != null){
			return publicIp;
		}
		Optional<String> ip = curl(EC2_PUBLIC_IP_URL, true);
		if(ip.isPresent()){
			logSource(SERVER_PUBLIC_IP, ip.get(), EC2_PUBLIC_IP_URL);
			return ip.get();
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

	private void logConfigFileProperties(Optional<Properties> configFileProperties){
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
		return Optional.ofNullable(serverType)
				.map(ServerType::getPersistentString)
				.orElse(null);
	}

	public String findConfigFile(String filename){
		String externalLocation = configDirectory + "/" + filename;
		if(Files.exists(Paths.get(externalLocation))){
			return externalLocation;
		}
		Objects.requireNonNull(internalConfigDirectory, externalLocation + " doesn't exist and "
				+ INTERNAL_CONFIG_DIRECTORY_PROP + " property is not set");
		return "/config/" + internalConfigDirectory + "/" + filename;
	}

	public Optional<Ec2InstanceDetailsDto> getEc2InstanceDetails(){
		Optional<String> ec2InstanceIdentityDocumentResponse = curl(EC2_INSTANCE_IDENTITY_DOCUMENT_URL, false);
		return ec2InstanceIdentityDocumentResponse.map(json -> GsonTool.GSON.fromJson(json,
				Ec2InstanceDetailsDto.class));
	}

	public boolean isEc2(){
		return getEc2InstanceDetails().isPresent();
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

	public String getTestConfigDirectory(){
		return testConfigDirectory;
	}

	public String getConfigFileLocation(){
		return configFileLocation;
	}

	public String getEnvironment(){
		return environment;
	}

	public String getEnvironmentDomain(){
		return environmentDomain;
	}

	public String getEnvironmentType(){
		return environmentType;
	}

	public String getWebappName(){
		return webappName;
	}

	public String getFirstServerClusterDomain(){
		return CollectionTool.getFirst(getServerClusterDomains());
	}

	public String getInternalConfigDirectory(){
		return internalConfigDirectory;
	}

	public abstract String getDatarouterPropertiesFileLocation();

}