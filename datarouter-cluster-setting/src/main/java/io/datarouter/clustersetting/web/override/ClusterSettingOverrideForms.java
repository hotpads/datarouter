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
package io.datarouter.clustersetting.web.override;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import io.datarouter.clustersetting.enums.ClusterSettingScope;
import io.datarouter.clustersetting.service.ClusterSettingValidationService;
import io.datarouter.clustersetting.storage.clustersettinglog.ClusterSettingLog;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.servertype.ServerType;
import io.datarouter.storage.servertype.ServerTypes;
import io.datarouter.web.html.form.HtmlFormSelect;
import io.datarouter.web.html.form.HtmlFormSubmitWithoutSubmitActionButton;
import io.datarouter.web.html.form.HtmlFormText;
import io.datarouter.web.html.form.HtmlFormTextArea;
import io.datarouter.web.html.form.HtmlFormValidator;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class ClusterSettingOverrideForms{

	@Inject
	private ServerTypes serverTypes;
	@Inject
	private ClusterSettingValidationService validationService;

	public HtmlFormText makeSettingNameField(String fieldName, Optional<String> value, boolean validate){
		return new HtmlFormText()
				.withLabel("Setting Name")
				.withName(fieldName)
				.withValue(
						value.orElse(""),
						validate,
						HtmlFormValidator::notBlank);
	}

	public HtmlFormSelect makeScopeField(String fieldName, ClusterSettingScope value){
		Map<String,String> scopeDisplayByValue = Scanner.of(ClusterSettingScope.values())
				.toMapSupplied(
						scopeValue -> scopeValue.persistentString,
						scopeValue -> scopeValue.display,
						LinkedHashMap::new);
		return new HtmlFormSelect()
				.withLabel("Scope")
				.withName(fieldName)
				.withDisplayByValue(scopeDisplayByValue)
				.withSelected(value.persistentString)
				.withSubmitOnChange();
	}

	public HtmlFormSelect makeServerTypeField(String fieldName, Optional<String> value){
		List<String> serverTypeStrings = serverTypes.values()
				.exclude(type -> type.equals(ServerType.ALL))
				.exclude(type -> type.equals(ServerType.UNKNOWN))
				.map(ServerType::getPersistentString)
				.list();
		return new HtmlFormSelect()
				.withLabel("Server Type")
				.withName(fieldName)
				.withValues(serverTypeStrings)
				.withSelected(value.orElse(null));
	}

	public HtmlFormText makeServerNameField(String fieldName, Optional<String> value, boolean validate){
		return new HtmlFormText()
				.withLabel("Server Name")
				.withName(fieldName)
				.withValue(
						value.orElse(""),
						validate,
						HtmlFormValidator::notBlank);
	}

	public HtmlFormTextArea makeCommentField(String fieldName, Optional<String> optValue, boolean validate){
		List<Function<String,Optional<String>>> errorFinders = List.of(
				HtmlFormValidator::notBlank,
				value -> HtmlFormValidator.maxLength(
						value,
						ClusterSettingLog.FieldKeys.comment.getSize()));
		return new HtmlFormTextArea()
				.withLabel("Comment")
				.withName(fieldName)
				.withValue(
						optValue.orElse(""),
						validate,
						errorFinders);
	}

	public HtmlFormTextArea makeSettingValueField(
			String fieldName,
			Optional<String> value,
			boolean validate,
			Optional<String> optSettingName){
		return new HtmlFormTextArea()
				.withLabel("Setting Value")
				.withName(fieldName)
				.withValue(
						value.orElse(""),
						validate,
						valueToValidate -> optSettingName
								.map(settingName -> validationService.findErrorForSettingValue(
										settingName,
										valueToValidate))
								.orElse(Optional.empty()));
	}

	public HtmlFormSubmitWithoutSubmitActionButton makeSubmitButton(String fieldName, String display){
		return new HtmlFormSubmitWithoutSubmitActionButton()
				.withLabel(display)
				.withName(fieldName)
				.withValue(Boolean.TRUE.toString());
	}
}
