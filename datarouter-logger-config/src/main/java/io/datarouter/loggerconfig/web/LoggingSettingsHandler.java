/*
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
package io.datarouter.loggerconfig.web;

import static j2html.TagCreator.body;
import static j2html.TagCreator.br;
import static j2html.TagCreator.div;
import static j2html.TagCreator.text;

import java.io.Serializable;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

import javax.inject.Inject;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.appender.ConsoleAppender;
import org.apache.logging.log4j.core.appender.ConsoleAppender.Target;
import org.apache.logging.log4j.core.appender.FileAppender;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.instrumentation.changelog.ChangelogRecorder;
import io.datarouter.instrumentation.changelog.ChangelogRecorder.DatarouterChangelogDtoBuilder;
import io.datarouter.loggerconfig.LoggingConfigService;
import io.datarouter.loggerconfig.LoggingSettingAction;
import io.datarouter.loggerconfig.config.DatarouterLoggerConfigFiles;
import io.datarouter.loggerconfig.config.DatarouterLoggerConfigSettingRoot;
import io.datarouter.loggerconfig.config.DatarouterLoggingConfigPaths;
import io.datarouter.loggerconfig.storage.consoleappender.DatarouterConsoleAppenderDao;
import io.datarouter.loggerconfig.storage.fileappender.DatarouterFileAppenderDao;
import io.datarouter.loggerconfig.storage.loggerconfig.DatarouterLoggerConfigDao;
import io.datarouter.logging.BaseLog4j2Configuration;
import io.datarouter.logging.Log4j2Configurator;
import io.datarouter.scanner.Scanner;
import io.datarouter.util.string.StringTool;
import io.datarouter.web.email.DatarouterHtmlEmailService;
import io.datarouter.web.email.StandardDatarouterEmailHeaderService;
import io.datarouter.web.handler.BaseHandler;
import io.datarouter.web.handler.mav.Mav;
import j2html.tags.ContainerTag;

public class LoggingSettingsHandler extends BaseHandler{

	private static final String DEFAULT_TEST_LOG_MESSAGE = "LoggingSettingsHandler.testLog()";
	private static final String DEFAULT_EMAIL = "System";
	private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
	private static final Boolean ADDITIVE = false;

	private static final Level[] LEVELS = new Level[]{
			Level.ALL,
			Level.TRACE,
			Level.DEBUG,
			Level.INFO,
			Level.WARN,
			Level.ERROR,
			Level.FATAL,// not in slf4j
			Level.OFF};

	@Inject
	private DatarouterHtmlEmailService htmlEmailService;
	@Inject
	private DatarouterLoggerConfigSettingRoot datarouterLoggerConfigSettings;
	@Inject
	private DatarouterLoggerConfigFiles files;
	@Inject
	private DatarouterLoggingConfigPaths paths;
	@Inject
	private Log4j2Configurator log4j2Configurator;
	@Inject
	private LoggingConfigService loggingConfigService;
	@Inject
	private DatarouterLoggerConfigDao loggerConfigDao;
	@Inject
	private DatarouterFileAppenderDao fileAppenderDao;
	@Inject
	private DatarouterConsoleAppenderDao consoleAppenderDao;
	@Inject
	private ChangelogRecorder changelogRecorder;
	@Inject
	private StandardDatarouterEmailHeaderService standardDatarouterEmailHeaderService;

	@Handler(defaultHandler = true)
	protected Mav showForm(){
		Mav mav = new Mav(files.jsp.datarouter.loggerConfig.loggingJsp);
		mav.put("rootLogger", log4j2Configurator.getRootLoggerConfig());
		mav.put("levels", LEVELS);
		mav.put("currentUserEmail", getCurrentUsername());

		Map<String,LoggerConfig> configs = log4j2Configurator.getConfigs();
		List<String> names = Scanner.of(configs.values()).map(LoggerConfig::getName).list();
		Map<String,io.datarouter.loggerconfig.storage.loggerconfig.LoggerConfig> configsWithMetadata = loggerConfigDao
				.getLoggerConfigs(names);
		Map<String,LoggerConfigMetadata> mergedConfigs = new TreeMap<>();
		Map<LoggerConfigMetadata,Collection<String>> appenderMap = new HashMap<>();

		for(LoggerConfig config : configs.values()){
			String name = config.getName();
			io.datarouter.loggerconfig.storage.loggerconfig.LoggerConfig configWithMetadata
					= configsWithMetadata.get(name);
			String email;
			Instant lastUpdated = null;
			Integer ttlMinutes = null;
			boolean canDelete = true;
			if(configWithMetadata != null){
				email = configWithMetadata.getEmail();
				lastUpdated = configWithMetadata.getLastUpdated();
				ttlMinutes = Optional.ofNullable(configWithMetadata.getTtlMillis())
						.map(Duration::ofMillis)
						.map(Duration::toMinutes)
						.map(Long::intValue)
						.orElse(null);
			}else{
				email = DEFAULT_EMAIL;
				canDelete = false;
			}
			var mergedLoggerConfig = new LoggerConfigMetadata(
					config,
					email,
					lastUpdated,
					canDelete,
					ttlMinutes,
					getUserZoneId());
			mergedConfigs.put(name, mergedLoggerConfig);
			appenderMap.put(mergedLoggerConfig, config.getAppenders().keySet());
		}
		mav.put("appenderMap", appenderMap);
		mav.put("configs", mergedConfigs);
		mav.put("appenders", log4j2Configurator.getAppenders());
		return mav;
	}

	@Handler
	private void testLog(){
		String loggerName = params.optional("loggerName").orElse("io.datarouter.web.handler.logging");
		String message = params.required("loggerMessage");
		if(StringTool.isEmpty(message)){
			message = DEFAULT_TEST_LOG_MESSAGE;
		}
		Logger logger = LoggerFactory.getLogger(loggerName);
		logger.trace(message);
		logger.debug(message);
		logger.info(message);
		logger.warn(message);
		logger.error(message);
	}

	@Handler
	private Mav createLoggerConfig(){
		updateOrCreateLoggerConfig(LoggingSettingAction.INSERTED);
		return getRedirectMav();
	}

	private Mav getRedirectMav(){
		return new Mav(Mav.REDIRECT + servletContext.getContextPath() + paths.datarouter.logging.toSlashedString());
	}

	@Handler
	private Mav updateLoggerConfig(){
		updateOrCreateLoggerConfig(LoggingSettingAction.UPDATED);
		return getRedirectMav();
	}

	private void updateOrCreateLoggerConfig(LoggingSettingAction action){
		String[] appenders = request.getParameterValues("appenders");
		String name = params.required("name");
		String oldLevel = loggerConfigDao.getLoggingLevelFromConfigName(name);
		Level level = Level.getLevel(params.required("level"));
		Integer ttlMinutes = params.optionalInteger("ttlMinutes", 0);
		log4j2Configurator.updateOrCreateLoggerConfig(name, level, ADDITIVE, appenders);
		loggerConfigDao.saveLoggerConfig(
				name,
				level,
				ADDITIVE,
				List.of(appenders),
				getCurrentUsername(),
				Duration.ofMinutes(ttlMinutes).toMillis());
		preventSecondApply();
		if(datarouterLoggerConfigSettings.sendLoggerConfigUpdateAlerts.get()){
			sendEmail(makeEmailContent(name, oldLevel, action));
		}
		recordChangelog("LoggerConfig", name, action.getPersistentString());
	}

	@Handler
	private Mav deleteLoggerConfig(){
		handleDeleteLoggerConfig();
		return getRedirectMav();
	}

	private void handleDeleteLoggerConfig(){
		String name = params.required("name");
		String oldLevel = loggerConfigDao.getLoggingLevelFromConfigName(name);
		log4j2Configurator.deleteLoggerConfig(name);
		loggerConfigDao.deleteLoggerConfig(name);
		preventSecondApply();
		if(datarouterLoggerConfigSettings.sendLoggerConfigUpdateAlerts.get()){
			sendEmail(makeEmailContent(name, oldLevel, LoggingSettingAction.DELETED));
		}
		recordChangelog("LoggerConfig", name, LoggingSettingAction.DELETED.getPersistentString());
	}

	@Handler
	private Mav deleteAppender(){
		String name = params.required("name");
		Class<? extends Appender> clazz = log4j2Configurator.getAppender(name).getClass();
		log4j2Configurator.deleteAppender(name);
		if(clazz == ConsoleAppender.class){
			consoleAppenderDao.deleteConsoleAppender(name);
		}else if(clazz == FileAppender.class){
			fileAppenderDao.deleteFileAppender(name);
		}
		preventSecondApply();
		recordChangelog("Appender", name, LoggingSettingAction.DELETED.getPersistentString());
		return getRedirectMav();
	}

	@Handler
	private Mav editConsoleAppender(){
		String action = params.optional("action").orElse(null);
		String name = params.optional("name").orElse(null);
		if("Create".equals(action)){
			String pattern = params.required("layout");
			Target target = Target.valueOf(params.required("target"));
			log4j2Configurator.addConsoleAppender(name, target, pattern);
			consoleAppenderDao.createAndPutConsoleAppender(name, pattern, target);
			preventSecondApply();
			return getRedirectMav();
		}
		Mav mav = new Mav(files.jsp.datarouter.loggerConfig.consoleAppenderJsp);
		mav.put("name", name);
		if(name != null){
			ConsoleAppender appender = (ConsoleAppender)log4j2Configurator.getAppender(name);
			Layout<? extends Serializable> layout = appender.getLayout();
			mav.put("target", appender.getTarget());
			mav.put("layout", layout);
		}else{
			mav.put("layout", BaseLog4j2Configuration.DEFAULT_PATTERN);
		}
		recordChangelog("ConsoleAppender", name, action);
		return mav;
	}

	@Handler
	private Mav editFileAppender(){
		String action = params.optional("action").orElse(null);
		String name = params.optional("name").orElse(null);
		if("Create".equals(action)){
			String pattern = params.required("layout");
			String fileName = params.required("fileName");
			log4j2Configurator.addFileAppender(name, fileName, pattern);
			fileAppenderDao.createAndputFileAppender(name, pattern, fileName);
			preventSecondApply();
			return getRedirectMav();
		}
		Mav mav = new Mav(files.jsp.datarouter.loggerConfig.fileAppenderJsp);
		mav.put("name", name);
		if(name != null){
			FileAppender appender = (FileAppender)log4j2Configurator.getAppender(name);
			Layout<? extends Serializable> layout = appender.getLayout();
			mav.put("layout", layout);
			mav.put("fileName", appender.getFileName());
		}else{
			mav.put("layout", BaseLog4j2Configuration.DEFAULT_PATTERN);
		}
		recordChangelog("FileAppender", name, action);
		return mav;
	}

	/**
	 * Prevent deploying this modification a second time on this webapp by the job (LoggerConfigUpdaterJob)
	 */
	private void preventSecondApply(){
		String signature = loggingConfigService.loadConfig().getSignature();
		loggingConfigService.setPreviousLoggingConfigSignatureForUpdaterJob(signature);
	}

	private void sendEmail(ContainerTag<?> content){
		String primaryHref = htmlEmailService.startLinkBuilder()
				.withLocalPath(paths.datarouter.logging)
				.build();
		var emailBuilder = htmlEmailService.startEmailBuilder()
				.withTitle("Logger Update")
				.withTitleHref(primaryHref)
				.withContent(content)
				.fromAdmin()
				.toSubscribers()
				.to(getCurrentUsername());
		htmlEmailService.trySendJ2Html(emailBuilder);
	}

	private ContainerTag<?> makeEmailContent(
			String loggerConfigName,
			String oldLevel,
			LoggingSettingAction action){
		var header = standardDatarouterEmailHeaderService.makeStandardHeader();
		List<String> lines = new ArrayList<>();
		lines.add("Change details:");
		lines.add("- user: " + getCurrentUsername());
		lines.add("- action: " + action.getPersistentString());
		lines.add("- logger config: " + loggerConfigName);
		lines.add("- old level: " + oldLevel);
		if(LoggingSettingAction.DELETED != action){
			lines.add("- new level: " + params.required("level"));
		}
		lines.add("");
		lines.add("");
		lines.add("If not obvious, please reply-all with any reason for the change.");

		var content = div();
		lines.forEach(line -> content.with(text(line)).with(br()));
		return body(header, content);
	}

	private String getCurrentUsername(){
		return getSessionInfo().getNonEmptyUsernameOrElse("");
	}

	private void recordChangelog(String changelogType, String name, String action){
		changelogRecorder.record(new DatarouterChangelogDtoBuilder(changelogType, name, action, getCurrentUsername())
				.build());
	}

	public static class LoggerConfigMetadata{

		private final String name;
		private final Level level;
		private final Boolean additive;
		private final List<String> appenderRefs;
		private final String email;
		private String lastUpdated;
		private boolean canDelete;
		private final Integer ttlMinutes;

		LoggerConfigMetadata(
				LoggerConfig config,
				String email,
				Instant lastUpdated,
				boolean canDelete,
				Integer ttlMinutes,
				ZoneId zoneId){
			this.name = config.getName();
			this.level = config.getLevel();
			this.additive = config.isAdditive();
			this.appenderRefs = new ArrayList<>(config.getAppenders().keySet());
			this.email = email;
			if(lastUpdated != null){
				this.lastUpdated = DATE_FORMAT.withZone(zoneId).format(lastUpdated);
			}
			this.canDelete = canDelete;
			this.ttlMinutes = ttlMinutes;
		}

		public String getName(){
			return name;
		}

		public Level getLevel(){
			return level;
		}

		public Boolean getAdditive(){
			return additive;
		}

		public List<String> getAppenderRefs(){
			return appenderRefs;
		}

		public String getEmail(){
			return email;
		}

		public String getLastUpdated(){
			return lastUpdated;
		}

		public boolean getCanDelete(){
			return canDelete;
		}

		public Integer getTtlMinutes(){
			return ttlMinutes;
		}

	}

}
