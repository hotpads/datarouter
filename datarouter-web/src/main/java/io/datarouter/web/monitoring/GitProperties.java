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
import java.net.URL;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.Optional;
import java.util.Properties;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

import io.datarouter.util.net.UrlTool;
import io.datarouter.web.app.ApplicationPaths;

@Singleton
public class GitProperties{
	private static final Logger logger = LoggerFactory.getLogger(GitProperties.class);

	public static final String FILE_NAME = "git.properties";

	public static final String UNKNOWN_STRING = "unknown";
	public static final Instant UNKNOWN_DATE = Instant.EPOCH;

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

	private final Properties properties = new Properties();

	@Inject
	public GitProperties(ApplicationPaths applicationPaths){
		URL url = UrlTool.create("file:" + applicationPaths.getResourcesPath() + "/" + FILE_NAME);
		logger.warn("loading git info from {}", url);
		try(InputStream resourceAsStream = url.openStream()){
			load(resourceAsStream);
		}catch(IOException e){
			logger.warn("could not load git info {}", e.toString());
		}
	}

	public GitProperties(InputStream inputStream){
		load(inputStream);
	}

	private void load(InputStream inputStream){
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

	public Optional<Instant> getCommitTime(){
		return getDateProperty(GIT_COMMIT_TIME);
	}

	public Optional<String> getCommitUserName(){
		return Optional.ofNullable(properties.getProperty(GIT_COMMIT_USER_NAME));
	}

	public Optional<Instant> getBuildTime(){
		return getDateProperty(GIT_BUILD_TIME);
	}

	public Optional<String> getTags(){
		return Optional.ofNullable(properties.getProperty(GIT_TAGS));
	}

	private Optional<Instant> getDateProperty(String propertyName){
		return Optional.ofNullable(properties.getProperty(propertyName))
				.map(value -> FORMAT.parse(value, Instant::from));
	}

	public static class GitPropertiesTests{

		@Test
		public void testDateParsing(){
			String input = "2019-12-16T17:10:27-0500";
			Assert.assertEquals(FORMAT.parse(input, Instant::from).toString(), "2019-12-16T22:10:27Z");
		}

	}
}
