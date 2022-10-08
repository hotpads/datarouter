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
package io.datarouter.secretweb.web;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.httpclient.endpoint.param.RequestBody;
import io.datarouter.instrumentation.changelog.ChangelogRecorder;
import io.datarouter.instrumentation.changelog.ChangelogRecorder.DatarouterChangelogDtoBuilder;
import io.datarouter.scanner.Scanner;
import io.datarouter.secret.config.SecretClientSupplierConfig;
import io.datarouter.secret.op.SecretOpConfig;
import io.datarouter.secret.op.SecretOpReason;
import io.datarouter.secret.op.client.SecretClientOpType;
import io.datarouter.secret.service.SecretJsonSerializer;
import io.datarouter.secret.service.SecretService;
import io.datarouter.secretweb.config.DatarouterSecretFiles;
import io.datarouter.secretweb.config.DatarouterSecretPaths;
import io.datarouter.secretweb.service.WebSecretOpReason;
import io.datarouter.secretweb.web.SecretClientSupplierConfigDto.SecretClientSupplierConfigsDto;
import io.datarouter.secretweb.web.SecretHandlerOpRequestDto.SecretOpDto;
import io.datarouter.util.string.StringTool;
import io.datarouter.web.handler.BaseHandler;
import io.datarouter.web.handler.mav.Mav;
import io.datarouter.web.html.react.bootstrap4.Bootstrap4ReactPageFactory;

public class SecretHandler extends BaseHandler{
	private static final Logger logger = LoggerFactory.getLogger(SecretHandler.class);

	@Inject
	private DatarouterSecretFiles files;
	@Inject
	private DatarouterSecretPaths paths;
	@Inject
	private SecretHandlerPermissions permissions;
	@Inject
	private SecretJsonSerializer jsonSerializer;
	@Inject
	private SecretService secretService;
	@Inject
	private Bootstrap4ReactPageFactory reactPageFactory;
	@Inject
	private ChangelogRecorder changelogRecorder;

	@Handler(defaultHandler = true)
	private Mav index(){
		return reactPageFactory.startBuilder(request)
				.withTitle("Datarouter - Secrets")
				.withReactScript(files.js.secretsJsx)
				.withJsStringConstant(
						"PATH_HANDLE",
						request.getContextPath() + paths.datarouter.secrets.handle.toSlashedString())
				.withJsStringConstant(
						"PATH_CONFIG",
						request.getContextPath() + paths.datarouter.secrets.getSecretClientSupplierConfig
								.toSlashedString())
				.buildMav();
	}

	//TODO add Session-based permission/allowedOp UI features
	@Handler
	public SecretClientSupplierConfigsDto getSecretClientSupplierConfig(){
		List<SecretClientSupplierConfig> secretClientSupplierConfigs = secretService.getSecretClientSupplierConfigs();
		List<String> orderedConfigs = Scanner.of(secretClientSupplierConfigs)
				.map(SecretClientSupplierConfig::getConfigName)
				.list();
		return new SecretClientSupplierConfigsDto(orderedConfigs, Scanner.of(secretClientSupplierConfigs)
				.map(config -> {
					return new SecretClientSupplierConfigDto(
							config.getConfigName(),
							config.getSecretClientSupplierClass().getSimpleName(),
							buildAllowedOps(config.getAllowedOps()),
							config.getAllowedNames()
									.map(Scanner::of)
									.map(Scanner::toMap)
									.orElseGet(Map::of));
				}).toMap(dto -> dto.configName));
	}

	@Handler
	public SecretHandlerOpResultDto handle(@RequestBody SecretHandlerOpRequestDto requestDto){
		SecretHandlerOpResultDto result = validateRequest(requestDto);
		if(result == null){
			result = executeAuthorizedRequest(requestDto);
			if(requestDto.op != SecretOpDto.LIST_ALL){
				var dto = new DatarouterChangelogDtoBuilder(
						"Secrets",
						requestDto.name,
						requestDto.op.getPersistentString(),
						getSessionInfo().getNonEmptyUsernameOrElse("")).build();
				changelogRecorder.record(dto);
			}
		}
		return result;
	}

	private Map<SecretOpDto,SecretOpDto> buildAllowedOps(Set<SecretClientOpType> serviceOps){
		Set<SecretOpDto> allowedOps = new HashSet<>();
		if(serviceOps.contains(SecretClientOpType.CREATE) || serviceOps.contains(SecretClientOpType.PUT)){
			allowedOps.add(SecretOpDto.CREATE);
		}
		if(serviceOps.contains(SecretClientOpType.READ)){
			allowedOps.add(SecretOpDto.READ);
		}
		if(serviceOps.contains(SecretClientOpType.UPDATE) || serviceOps.contains(SecretClientOpType.PUT)){
			allowedOps.add(SecretOpDto.UPDATE);
		}
		if(serviceOps.contains(SecretClientOpType.DELETE)){
			allowedOps.add(SecretOpDto.DELETE);
		}
		if(serviceOps.contains(SecretClientOpType.LIST)){
			allowedOps.add(SecretOpDto.LIST_ALL);
		}
		return Scanner.of(allowedOps).toMap();
	}

	private SecretHandlerOpResultDto validateRequest(SecretHandlerOpRequestDto requestDto){
		if(requestDto.op == null){
			return SecretHandlerOpResultDto.error("Op is required.");
		}
		switch(requestDto.op){
		case CREATE:
			if(StringTool.isNullOrEmptyOrWhitespace(requestDto.secretClass)){
				return SecretHandlerOpResultDto.error("SecretClass is required for this op.");
			}
		case UPDATE:
			if(StringTool.isNullOrEmptyOrWhitespace(requestDto.name)){
				return SecretHandlerOpResultDto.error("Name is required for this op.");
			}
			if(StringTool.isNullOrEmptyOrWhitespace(requestDto.value)){
				return SecretHandlerOpResultDto.error("Value is required for this op.");
			}
			break;
		case READ:
		case DELETE:
			if(StringTool.isNullOrEmptyOrWhitespace(requestDto.name)){
				return SecretHandlerOpResultDto.error("Name is required for this op.");
			}
			break;
		case LIST_ALL:
			break;
		default:
			return SecretHandlerOpResultDto.error("Unknown op.");
		}
		if(!permissions.isAuthorized(getSessionInfo().getRequiredSession(), requestDto)){
			return SecretHandlerOpResultDto.denied("Permission denied for " + requestDto.op + " op.");
		}
		return null;
	}

	private SecretHandlerOpResultDto executeAuthorizedRequest(SecretHandlerOpRequestDto requestDto){
		try{
			SecretOpReason opReason = WebSecretOpReason.manualOp(
					getSessionInfo().getRequiredSession(),
					"SecretHandler");
			Optional<String> configName = StringTool.isEmptyOrWhitespace(requestDto.configName)
					? Optional.empty()
					: Optional.ofNullable(requestDto.configName);
			SecretOpConfig config;
			switch(requestDto.op){
			case CREATE:
				try{
					String secretValue = requestDto.value;
					//test that the raw value is de/serializable
					jsonSerializer.serialize(
							jsonSerializer.deserialize(secretValue, Class.forName(requestDto.secretClass)));
					config = SecretOpConfig.builder(opReason)
							.useTargetSecretClientConfig(configName)
							.disableSerialization()
							.build();
					secretService.create(requestDto.name, secretValue, config);
				}catch(ClassNotFoundException e){
					return SecretHandlerOpResultDto.error("Provided class cannot be found.");
				}
				return SecretHandlerOpResultDto.success();
			case UPDATE:
				config = SecretOpConfig.builder(opReason)
						.useTargetSecretClientConfig(configName)
						.disableSerialization()
						.build();
				secretService.update(requestDto.name, requestDto.value, config);
				return SecretHandlerOpResultDto.success();
			case READ:
				config = SecretOpConfig.builder(opReason)
						.useTargetSecretClientConfig(configName)
						.disableSerialization()
						.build();
				return SecretHandlerOpResultDto.read(secretService.read(requestDto.name, String.class, config));
			case DELETE:
				config = SecretOpConfig.builder(opReason)
						.useTargetSecretClientConfig(configName)
						.build();
				secretService.delete(requestDto.name, config);
				return SecretHandlerOpResultDto.success();
			case LIST_ALL:
				config = SecretOpConfig.builder(opReason)
						.useTargetSecretClientConfig(configName)
						.build();
				List<String> appNames = secretService.listSecretNames(Optional.empty(), config);
				appNames.sort(String.CASE_INSENSITIVE_ORDER);
				config = SecretOpConfig.builder(opReason)
						.useSharedNamespace()
						.useTargetSecretClientConfig(configName)
						.build();
				List<String> sharedNames = secretService.listSecretNames(Optional.empty(), config);
				sharedNames.sort(String.CASE_INSENSITIVE_ORDER);
				return SecretHandlerOpResultDto.list(appNames, sharedNames);
			default:
				return SecretHandlerOpResultDto.error("Unknown op.");
			}
		}catch(RuntimeException e){
			logger.warn("Failed SecretHandler operation: ", e);
			return SecretHandlerOpResultDto.error(e.getMessage());
		}
	}

}
