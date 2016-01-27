package com.hotpads.handler;

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
			GIT_COMMIT_ID_DESCRIBE_SHORT="git.commit.id.describe-short",
			GIT_COMMIT_TIME="git.commit.time",
			GIT_COMMIT_USER_NAME="git.commit.user.name",
			GIT_BUILD_TIME = "git.build.time",
			GIT_TAGS= "git.tags";

	private static final SimpleDateFormat dateParser = new SimpleDateFormat("dd.MM.yyyy @ HH:mm:ss z");

	private Properties properties;

	public GitProperties(){
		ClassLoader classLoader = getClass().getClassLoader();
		try(InputStream resourceAsStream = classLoader.getResourceAsStream("git.properties")){
			if(resourceAsStream == null){
				logger.error("file \"git.properties\" not found. Try to run an eclipse maven update or a full mvn package");
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
