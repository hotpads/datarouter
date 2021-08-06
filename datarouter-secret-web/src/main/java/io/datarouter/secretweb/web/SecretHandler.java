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

import io.datarouter.instrumentation.changelog.ChangelogRecorder;
import io.datarouter.instrumentation.changelog.ChangelogRecorder.DatarouterChangelogDtoBuilder;
import io.datarouter.scanner.Scanner;
import io.datarouter.secret.config.SecretClientConfig;
import io.datarouter.secret.op.SecretOpReason;
import io.datarouter.secret.op.SecretOpType;
import io.datarouter.secret.service.SecretService;
import io.datarouter.secretweb.config.DatarouterSecretFiles;
import io.datarouter.secretweb.config.DatarouterSecretPaths;
import io.datarouter.secretweb.service.WebSecretOpReason;
import io.datarouter.secretweb.web.SecretClientSupplierConfigDto.SecretClientSupplierConfigsDto;
import io.datarouter.secretweb.web.SecretHandlerOpRequestDto.SecretOpDto;
import io.datarouter.util.string.StringTool;
import io.datarouter.web.handler.BaseHandler;
import io.datarouter.web.handler.mav.Mav;
import io.datarouter.web.handler.types.RequestBody;
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
				.withJsStringConstant("PATH_HANDLE", request.getContextPath() + paths.datarouter.secrets.handle
						.toSlashedString())
				.withJsStringConstant("PATH_CONFIG", request.getContextPath() + paths.datarouter.secrets
						.getSecretClientSupplierConfig.toSlashedString())
				.buildMav();
	}

	//TODO add Session-based permission/allowedOp UI features
	@Handler
	public SecretClientSupplierConfigsDto getSecretClientSupplierConfig(){
		List<SecretClientConfig> secretClientSupplierConfigs = secretService.getSecretClientSupplierConfigs();
		List<String> orderedConfigs = Scanner.of(secretClientSupplierConfigs)
				.map(SecretClientConfig::getConfigName)
				.list();
		return new SecretClientSupplierConfigsDto(orderedConfigs, Scanner.of(secretClientSupplierConfigs)
				.map(config -> {
					return new SecretClientSupplierConfigDto(
							config.getConfigName(),
							config.getSecretClientSupplierClass().getSimpleName(),
							buildAllowedOps(config.getAllowedOps()),
							config.getAllowedNames().isPresent() ? Scanner.of(config.getAllowedNames().get()).toMap()
									: Map.of());
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

	private Map<SecretOpDto,SecretOpDto> buildAllowedOps(Set<SecretOpType> serviceOps){
		Set<SecretOpDto> allowedOps = new HashSet<>();
		if(serviceOps.contains(SecretOpType.CREATE) || serviceOps.contains(SecretOpType.PUT)){
			allowedOps.add(SecretOpDto.CREATE);
		}
		if(serviceOps.contains(SecretOpType.READ)){
			allowedOps.add(SecretOpDto.READ);
		}
		if(serviceOps.contains(SecretOpType.UPDATE) || serviceOps.contains(SecretOpType.PUT)){
			allowedOps.add(SecretOpDto.UPDATE);
		}
		if(serviceOps.contains(SecretOpType.DELETE)){
			allowedOps.add(SecretOpDto.DELETE);
		}
		if(serviceOps.contains(SecretOpType.LIST)){
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
			SecretOpReason opReason = WebSecretOpReason.manualOp(getSessionInfo().getRequiredSession(),
					"SecretHandler");
			Optional<String> configName = StringTool.isEmptyOrWhitespace(requestDto.configName) ? Optional.empty()
					: Optional.ofNullable(requestDto.configName);
			switch(requestDto.op){
			case CREATE:
				try{
					secretService.create(configName, requestDto.name, requestDto.value, Class.forName(requestDto
							.secretClass), opReason);
				}catch(ClassNotFoundException e){
					return SecretHandlerOpResultDto.error("Provided class cannot be found.");
				}
				return SecretHandlerOpResultDto.success();
			case UPDATE:
				secretService.updateRaw(configName, requestDto.name, requestDto.value, opReason);
				return SecretHandlerOpResultDto.success();
			case READ:
				return SecretHandlerOpResultDto.read(secretService.readRaw(configName, requestDto.name,
						opReason));
			case DELETE:
				secretService.delete(configName, requestDto.name, opReason);
				return SecretHandlerOpResultDto.success();
			case LIST_ALL:
				List<String> appNames = secretService.listSecretNames(configName, opReason);
				appNames.sort(String.CASE_INSENSITIVE_ORDER);
				List<String> sharedNames = secretService.listSecretNamesShared(configName, opReason);
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
