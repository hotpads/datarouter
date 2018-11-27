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
package io.datarouter.web.monitoring;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.Optional;
import java.util.Properties;

import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.util.time.LocalDateTimeTool;

@Singleton
public class GitProperties{
	private static final Logger logger = LoggerFactory.getLogger(GitProperties.class);

	public static final String UNKNOWN_STRING = "unknown";

	public static final LocalDateTime UNKNOWN_DATE = LocalDateTimeTool.minParsableDate();

	private static final DateTimeFormatter FORMAT = new DateTimeFormatterBuilder()
			.append(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
			.appendOffset("+HHmm", "")
			.toFormatter();

	private static final String
			GIT_BRANCH = "git.branch",
			GIT_COMMIT_ID_ABBREV = "git.commit.id.abbrev",
			GIT_COMMIT_ID_DESCRIBE_SHORT = "git.commit.id.describe-short",
			GIT_COMMIT_TIME = "git.commit.time",
			GIT_COMMIT_USER_NAME = "git.commit.user.name",
			GIT_BUILD_TIME = "git.build.time",
			GIT_TAGS = "git.tags";

	private Properties properties;

	public GitProperties(){
		ClassLoader classLoader = getClass().getClassLoader();
		try(InputStream resourceAsStream = classLoader.getResourceAsStream("git.properties")){
			if(resourceAsStream == null){
				logger.warn("file \"git.properties\" not found, run an eclipse maven update or a full mvn package. "
						+ "Using default values");
				properties = new Properties();
			}else{
				load(resourceAsStream);
			}
		}catch(IOException e){
			throw new RuntimeException(e);
		}
	}

	public GitProperties(InputStream inputStream){
		load(inputStream);
	}

	private void load(InputStream inputStream){
		properties = new Properties();
		try{
			properties.load(inputStream);
		}catch(IOException e){
			throw new RuntimeException(e);
		}
	}

	public Optional<String> getBranch(){
		return Optional.ofNullable(properties.getProperty(GIT_BRANCH));
	}

	public Optional<String> getIdAbbrev(){
		return Optional.ofNullable(properties.getProperty(GIT_COMMIT_ID_ABBREV));
	}

	public Optional<String> getDescribeShort(){
		return Optional.ofNullable(properties.getProperty(GIT_COMMIT_ID_DESCRIBE_SHORT));
	}

	public Optional<LocalDateTime> getCommitTime(){
		return getDateProperty(GIT_COMMIT_TIME);
	}

	public Optional<String> getCommitUserName(){
		return Optional.ofNullable(properties.getProperty(GIT_COMMIT_USER_NAME));
	}

	public Optional<LocalDateTime> getBuildTime(){
		return getDateProperty(GIT_BUILD_TIME);
	}

	public Optional<String> getTags(){
		return Optional.ofNullable(properties.getProperty(GIT_TAGS));
	}

	private Optional<LocalDateTime> getDateProperty(String propertyName){
		return Optional.ofNullable(properties.getProperty(propertyName))
				.map(value -> LocalDateTime.parse(value, FORMAT));
	}

}
