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
package io.datarouter.instrumentation.relay.dto;

import java.util.List;

import io.datarouter.instrumentation.relay.rml.RmlDoc;
import io.datarouter.instrumentation.validation.DatarouterInstrumentationValidationConstants.RelayInstrumentationConstants;
import io.datarouter.instrumentation.validation.DatarouterInstrumentationValidationTool;

public record RelayStartThreadRequestDto(
		List<String> topics,
		String from,
		String subject,
		RelayMessageBlockDto content,
		List<String> fileIds){

	public RelayStartThreadRequestDto{
		DatarouterInstrumentationValidationTool.throwIfExceedsMaxSize(
				subject,
				RelayInstrumentationConstants.MAX_SIZE_THREAD_SUBJECT,
				"subject");
	}

	public RelayStartThreadRequestDto(
			List<String> topics,
			String from,
			String subject,
			RmlDoc content,
			List<String> fileIds){
		this(topics, from, subject, content.build(), fileIds);
	}

	public RelayStartThreadRequestDto(
			List<String> topics,
			String from,
			String subject,
			RmlDoc content){
		this(topics, from, subject, content, List.of());
	}

}
