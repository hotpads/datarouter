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

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.clustersetting.ClusterSettingScope;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.servertype.ServerType;
import io.datarouter.storage.servertype.ServerTypes;
import io.datarouter.web.html.form.HtmlFormButtonWithoutSubmitAction;
import io.datarouter.web.html.form.HtmlFormSelect;
import io.datarouter.web.html.form.HtmlFormText;
import io.datarouter.web.html.form.HtmlFormTextArea;
import io.datarouter.web.html.form.HtmlFormValidator;

@Singleton
public class ClusterSettingOverrideForms{

	@Inject
	private ServerTypes serverTypes;

	public HtmlFormText makeSettingNameField(String fieldName, Optional<String> value, boolean validate){
		return new HtmlFormText()
				.withDisplay("Setting Name")
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
				.withDisplay("Scope")
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
				.withDisplay("Server Type")
				.withName(fieldName)
				.withValues(serverTypeStrings)
				.withSelected(value.orElse(null));
	}

	public HtmlFormText makeServerNameField(String fieldName, Optional<String> value, boolean validate){
		return new HtmlFormText()
				.withDisplay("Server Name")
				.withName(fieldName)
				.withValue(
						value.orElse(""),
						validate,
						HtmlFormValidator::notBlank);
	}

	public HtmlFormTextArea makeCommentField(String fieldName, Optional<String> value, boolean validate){
		return new HtmlFormTextArea()
				.withDisplay("Comment")
				.withName(fieldName)
				.withValue(
						value.orElse(""),
						validate,
						HtmlFormValidator::notBlank);
	}

	public HtmlFormTextArea makeSettingValueField(String fieldName, Optional<String> value, boolean validate){
		return new HtmlFormTextArea()
				.withDisplay("Setting Value")
				.withName(fieldName)
				.withValue(
						value.orElse(""),
						validate,
						HtmlFormValidator::notBlank);
	}

	public HtmlFormButtonWithoutSubmitAction makeSubmitButton(String fieldName, String display){
		return new HtmlFormButtonWithoutSubmitAction()
				.withDisplay(display)
				.withName(fieldName)
				.withValue(Boolean.TRUE.toString());
	}
}
