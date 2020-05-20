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
package io.datarouter.secret.handler;

import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.instrumentation.changelog.ChangelogRecorder;
import io.datarouter.secret.config.DatarouterSecretFiles;
import io.datarouter.secret.config.DatarouterSecretPaths;
import io.datarouter.secret.handler.SecretHandlerOpRequestDto.SecretOpDto;
import io.datarouter.secret.service.SecretOpReason;
import io.datarouter.secret.service.SecretService;
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
				.withJsStringConstant("PATH", request.getContextPath() + paths.datarouter.secrets.handle
						.toSlashedString())
				.buildMav();
	}

	@Handler
	public SecretHandlerOpResultDto handle(@RequestBody SecretHandlerOpRequestDto requestDto){
		SecretHandlerOpResultDto result = validateRequest(requestDto);
		if(result == null){
			result = executeAuthorizedRequest(requestDto);
			if(requestDto.op != SecretOpDto.LIST_ALL){
				changelogRecorder.record(
						"Secrets",
						requestDto.name,
						requestDto.op.getPersistentString(),
						getSessionInfo().getNonEmptyUsernameOrElse(""));
			}
		}
		return result;
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
		case READ_SHARED:
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
		if(!permissions.isAuthorized(getSessionInfo().getRequiredSession(), requestDto.op)){
			return SecretHandlerOpResultDto.denied("Permission denied for " + requestDto.op + " op.");
		}
		return null;
	}

	private SecretHandlerOpResultDto executeAuthorizedRequest(SecretHandlerOpRequestDto requestDto){
		try{
			SecretOpReason opReason = SecretOpReason.manualOp(getSessionInfo().getRequiredSession(), "SecretHandler");
			switch(requestDto.op){
			case CREATE:
				try{
					secretService.create(requestDto.name, requestDto.value, Class.forName(requestDto.secretClass),
							opReason);
				}catch(ClassNotFoundException e){
					return SecretHandlerOpResultDto.error("Provided class cannot be found.");
				}
				return SecretHandlerOpResultDto.success();
			case UPDATE:
				secretService.updateRaw(requestDto.name, requestDto.value, opReason);
				return SecretHandlerOpResultDto.success();
			case READ:
				return SecretHandlerOpResultDto.read(secretService.readRaw(requestDto.name, opReason));
			case READ_SHARED:
				return SecretHandlerOpResultDto.read(secretService.readRawShared(requestDto.name, opReason));
			case DELETE:
				secretService.delete(requestDto.name, opReason);
				return SecretHandlerOpResultDto.success();
			case LIST_ALL:
				List<String> appNames = secretService.listSecretNames(Optional.ofNullable(requestDto.name));
				appNames.sort(String.CASE_INSENSITIVE_ORDER);
				List<String> sharedNames = secretService.listSecretNamesShared();
				sharedNames.sort(String.CASE_INSENSITIVE_ORDER);
				return SecretHandlerOpResultDto.list(appNames, sharedNames);
			default:
				return SecretHandlerOpResultDto.error("Unknown op.");
			}
		}catch(RuntimeException e){
			logger.warn("Failed SecretsHandler operation: ", e);
			return SecretHandlerOpResultDto.error(e.getMessage());
		}
	}

}
