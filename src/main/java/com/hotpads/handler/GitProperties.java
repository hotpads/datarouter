package com.hotpads.handler;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class GitProperties{
	private static final Logger logger = LoggerFactory.getLogger(GitProperties.class);
	private static final String GIT_BRANCH = "git.branch";
	private static final String GIT_COMMIT_ID_ABBREV = "git.commit.id.abbrev";
	private Properties properties;

	public GitProperties() throws IOException {
		properties = new Properties();
		ClassLoader classLoader = getClass().getClassLoader();
		InputStream resourceAsStream = classLoader.getResourceAsStream("git.properties");
		if (resourceAsStream == null) {
			logger.error("file \"git.properties\" not found. Try to run an eclipse maven update or a full mvn package");
		} else {
			properties.load(resourceAsStream);
		}
	}
	
	public String getBranch() {
		return properties.getProperty(GIT_BRANCH);
	}

	public String getIdAbbrev(){
		return properties.getProperty(GIT_COMMIT_ID_ABBREV);
	}

}