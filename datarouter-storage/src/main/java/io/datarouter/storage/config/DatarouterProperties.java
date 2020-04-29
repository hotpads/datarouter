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

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.storage.config.environment.EnvironmentType;
import io.datarouter.storage.servertype.ServerType;
import io.datarouter.storage.servertype.ServerTypes;
import io.datarouter.util.Require;
import io.datarouter.util.SystemTool;
import io.datarouter.util.aws.Ec2InstanceTool;
import io.datarouter.util.collection.CollectionTool;
import io.datarouter.util.io.FileTool;
import io.datarouter.util.properties.PropertiesTool;
import io.datarouter.util.string.StringTool;
import io.datarouter.util.tuple.Pair;

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
	private static final String INTERNAL_CONFIG_DIRECTORY = "internalConfigDirectory";
	private static final List<String> REQUIRED_PROPERTIES = List.of(
			ENVIRONMENT,
			ENVIRONMENT_TYPE,
			SERVER_NAME,
			SERVER_TYPE,
			ADMINISTRATOR_EMAIL,
			INTERNAL_CONFIG_DIRECTORY);

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
	private final Properties allComputedServerProperties;

	private Optional<Properties> propertiesFromConfigFile = Optional.empty();

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

		this.configDirectory = validateConfigDirectory(configDirectory, directoryRequired, directoryFromJvmArg);
		this.configFileLocation = findConfigFileLocation(filename, fileRequired);

		this.allComputedServerProperties = new Properties();
		this.webappName = webappName;
		this.testConfigDirectory = TEST_CONFIG_DIRECTORY;

		this.environment = findProperty(ENVIRONMENT);
		this.environmentDomain = findProperty(ENVIRONMENT_DOMAIN);
		this.environmentType = findProperty(ENVIRONMENT_TYPE, () -> EnvironmentType.DEVELOPMENT.get()
				.getPersistentString());
		this.serverName = findProperty(SERVER_NAME, SystemTool::getHostname,
				"InetAddress.getLocalHost().getHostName()");
		this.serverType = serverTypeOptions.fromPersistentString(findProperty(SERVER_TYPE));
		this.administratorEmail = findProperty(ADMINISTRATOR_EMAIL);
		this.privateIp = findProperty(SERVER_PRIVATE_IP, Ec2InstanceTool::getEc2InstancePrivateIp,
				Ec2InstanceTool.EC2_PRIVATE_IP_URL);
		this.publicIp = findProperty(SERVER_PUBLIC_IP, Ec2InstanceTool::getEc2InstancePublicIp,
				Ec2InstanceTool.EC2_PUBLIC_IP_URL);
		this.clusterDomains = findClusterDomains();
		this.internalConfigDirectory = findProperty(INTERNAL_CONFIG_DIRECTORY);

		checkRequiredProperties();
	}

	private void checkRequiredProperties(){
		REQUIRED_PROPERTIES.stream()
				.collect(Collectors.toMap(Function.identity(), allComputedServerProperties::getProperty)).entrySet()
				.stream()
				.filter(entry -> entry.getValue().isEmpty())
				.peek(entry -> logger.error("Value missing for required property {}", entry.getKey()))
				.findAny()
				.ifPresent($ -> {
					throw new RuntimeException("One or more required properties are empty/null");
				});
	}

	/*--------------- methods to find config values -----------------*/

	private String validateConfigDirectory(String configDirectory, boolean directoryRequired,
			boolean directoryFromJvmArg){
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
		return configDirectory;
	}

	private String findConfigFileLocation(String filename, boolean fileRequired){
		String configFileLocation = null;
		if(StringTool.isEmpty(filename)){
			Require.isTrue(!fileRequired);
		}else{
			configFileLocation = configDirectory + "/" + filename;
			if(Files.notExists(Paths.get(configFileLocation))){
				logger.error("couldn't find config file {}", configFileLocation);
			}else{
				logger.warn("found config file {}", configFileLocation);
				try{
					propertiesFromConfigFile = Optional.of(PropertiesTool.parse(configFileLocation));
					logConfigFileProperties();
				}catch(Exception e){
					logger.error("couldn't parse config file {}", configFileLocation);
				}
			}
		}
		return configFileLocation;
	}

	private String findProperty(String propertyName){
		return findProperty(propertyName, null, null);
	}

	private String findProperty(String propertyName, Supplier<String> defaultValue){
		return findProperty(propertyName, defaultValue, null);
	}

	//use what available in the following order: jvmArg then configFile then default then null
	private String findProperty(String propertyName, Supplier<String> defaultValueSupplier, String defaultSource){
		Optional<Pair<String,String>> propertyValueBySource = getPropFromJvmArg(propertyName)
				.or(() -> getPropFromConfigFile(propertyName));

		if(propertyValueBySource.isPresent() && !propertyValueBySource.get().getLeft().isEmpty()){
			allComputedServerProperties.setProperty(propertyName, propertyValueBySource.get().getLeft());
			return propertyValueBySource.get().getLeft();
		}

		if(defaultValueSupplier != null){
			String defaultValue = defaultValueSupplier.get();
			if(defaultValue != null){
				allComputedServerProperties.setProperty(propertyName, defaultValue);
				logSource(propertyName, defaultValue, defaultSource == null ? "default" : defaultSource);
				return defaultValue;
			}
		}

		if(propertyValueBySource.isPresent()){
			logger.error("found {} with empty value from {}", propertyName, propertyValueBySource.get().getRight());
		}else{
			logger.error("couldn't find " + propertyName + ", no default provided");
		}
		allComputedServerProperties.setProperty(propertyName, "");
		return null;
	}

	private Optional<Pair<String,String>> getPropFromConfigFile(String propertyName){
		Optional<String> propertyValue = propertiesFromConfigFile
				.map(properties -> properties.getProperty(propertyName));
		if(propertyValue.isEmpty()){
			return Optional.empty();
		}
		if(!propertyValue.get().isEmpty()){
			logSource(propertyName, propertyValue.get(), configFileLocation);
		}
		return Optional.of(new Pair<>(propertyValue.get(), configFileLocation));
	}

	private Optional<Pair<String,String>> getPropFromJvmArg(String jvmArg){
		String jvmArgName = JVM_ARG_PREFIX + jvmArg;
		String jvmArgValue = System.getProperty(jvmArgName);
		if(jvmArgValue == null){
			return Optional.empty();
		}
		if(!jvmArgValue.isEmpty()){
			logJvmArgSource(jvmArg, jvmArgValue, jvmArgName);
		}
		return Optional.of(new Pair<>(jvmArgValue, jvmArgName));
	}

	private Collection<String> findClusterDomains(){
		String propertyValue = findProperty(SERVER_CLUSTER_DOMAINS);
		if(StringTool.isNullOrEmptyOrWhitespace(propertyValue)){
			return Collections.emptyList();
		}
		return Stream.of(propertyValue.split(","))
				.filter(StringTool::notEmptyNorWhitespace)
				.map(String::trim)
				.collect(Collectors.toUnmodifiableList());
	}

	private void logConfigFileProperties(){
		Properties allProperties = propertiesFromConfigFile.orElseGet(Properties::new);
		allProperties.stringPropertyNames().stream()
				.map(name -> name + "=" + allProperties.getProperty(name))
				.sorted()
				.forEach(logger::info);
	}

	private void logSource(String name, String value, String source){
		logger.warn("found {}={} from {}", name, value, source);
	}

	private void logJvmArgSource(String name, String value, String jvmArgName){
		logger.warn("found {}={} from -D{} JVM arg", name, value, jvmArgName);
	}

	/*------------------ helper methods ---------------*/

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
				+ INTERNAL_CONFIG_DIRECTORY + " property is not set");
		externalLocation = configDirectory + "/" + internalConfigDirectory + "/" + filename;
		if(Files.exists(Paths.get(externalLocation))){
			return externalLocation;
		}
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

	public Properties getAllComputedServerProperties(){
		return allComputedServerProperties;
	}

	public abstract String getDatarouterPropertiesFileLocation();

}