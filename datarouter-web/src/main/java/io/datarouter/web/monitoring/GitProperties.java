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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class GitProperties{
	private static final Logger logger = LoggerFactory.getLogger(GitProperties.class);

	private static final String
			GIT_BRANCH = "git.branch",
			GIT_COMMIT_ID_ABBREV = "git.commit.id.abbrev",
			GIT_COMMIT_ID_DESCRIBE_SHORT = "git.commit.id.describe-short",
			GIT_COMMIT_TIME = "git.commit.time",
			GIT_COMMIT_USER_NAME = "git.commit.user.name",
			GIT_BUILD_TIME = "git.build.time",
			GIT_TAGS = "git.tags";

	private static final SimpleDateFormat dateParser = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");

	private Properties properties;

	public GitProperties(){
		ClassLoader classLoader = getClass().getClassLoader();
		try(InputStream resourceAsStream = classLoader.getResourceAsStream("git.properties")){
			if(resourceAsStream == null){
				logger.error(
						"file \"git.properties\" not found. Try to run an eclipse maven update or a full mvn package");
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

	public String getBranch(){
		return properties.getProperty(GIT_BRANCH);
	}

	public String getIdAbbrev(){
		return properties.getProperty(GIT_COMMIT_ID_ABBREV);
	}

	public String getDescribeShort(){
		return properties.getProperty(GIT_COMMIT_ID_DESCRIBE_SHORT);
	}

	public Date getCommitTime(){
		try{
			return dateParser.parse(properties.getProperty(GIT_COMMIT_TIME));
		}catch(ParseException e){
			throw new RuntimeException(e);
		}
	}

	public String getCommitUserName(){
		return properties.getProperty(GIT_COMMIT_USER_NAME);
	}

	public Date getBuildTime(){
		try{
			return dateParser.parse(properties.getProperty(GIT_BUILD_TIME));
		}catch(ParseException e){
			throw new RuntimeException(e);
		}
	}

	public String getTags(){
		return properties.getProperty(GIT_TAGS);
	}
}
