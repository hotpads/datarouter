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
package io.datarouter.storage.setting;

import java.util.function.Supplier;

import io.datarouter.scanner.Scanner;

public enum DatarouterSettingTagType implements Supplier<DatarouterSettingTag>{
	METRIC_PIPELINE("metricPipeline"),
	TRACE2_PIPELINE("trace2Pipeline"),
	CONVEYOR_TRACE_PIPELINE("conveyorTracePipeline"),
	EXCEPTION_PIPELINE("exceptionPipeline"),
	METRIC_TEMPLATE_PIPELINE("metricTemplatePipeline"),
	;

	private final DatarouterSettingTag datarouterSettingTag;

	DatarouterSettingTagType(String persistentString){
		this.datarouterSettingTag = new DatarouterSettingTag(persistentString);
	}

	@Override
	public DatarouterSettingTag get(){
		return datarouterSettingTag;
	}

	public static Scanner<String> scanPersistentStrings(){
		return Scanner.of(DatarouterSettingTagType.values())
				.map(DatarouterSettingTagType::get)
				.map(DatarouterSettingTag::getPersistentString);
	}

}
