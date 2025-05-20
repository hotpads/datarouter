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
package io.datarouter.web.handler.documentation;

import java.util.List;
import java.util.stream.Collectors;

import io.datarouter.scanner.Scanner;

public class ApiDocSchemaDto{

	private final String name;
	private final String type;
	private final String className;
	private final List<ApiDocSchemaDto> fields;
	private final List<ApiDocSchemaDto> parameters;
	private final List<String> enumValues;
	private final boolean isOptional;
	private final boolean isDeprecated;
	private final Long max;
	private final Long min;
	private final Long maxLength;
	private final String description;
	private final String defaultValue;
	private final boolean isArray;

	public ApiDocSchemaDto(String name,
			String type,
			String className,
			List<ApiDocSchemaDto> fields,
			List<ApiDocSchemaDto> parameters,
			List<String> enumValues,
			boolean isOptional,
			boolean isDeprecated,
			Long max,
			Long min,
			Long maxLength,
			String description,
			String defaultValue,
			boolean isArray){
		this.name = name;
		this.type = type;
		this.className = className;
		this.fields = fields;
		this.parameters = parameters;
		this.enumValues = enumValues;
		this.isOptional = isOptional;
		this.isDeprecated = isDeprecated;
		this.max = max;
		this.min = min;
		this.maxLength = maxLength;
		this.description = description;
		this.defaultValue = defaultValue;
		this.isArray = isArray;
	}

	public String getName(){
		return name;
	}

	public String getType(){
		return type;
	}

	public String getClassName(){
		return className;
	}

	public List<ApiDocSchemaDto> getFields(){
		return fields;
	}

	public List<ApiDocSchemaDto> getParameters(){
		return parameters;
	}

	public List<String> getEnumValues(){
		return enumValues;
	}

	public boolean isOptional(){
		return isOptional;
	}

	public boolean isDeprecated(){
		return isDeprecated;
	}

	public Long getMax(){
		return max;
	}

	public Long getMin(){
		return min;
	}

	public Long getMaxLength(){
		return maxLength;
	}

	public String getDescription(){
		return description;
	}

	public String getDefaultValue(){
		return defaultValue;
	}

	public boolean isArray(){
		return isArray;
	}

	public boolean hasFields(){
		return fields != null && !fields.isEmpty();
	}

	public boolean hasParameters(){
		return parameters != null && !parameters.isEmpty();
	}

	public static class ApiDocSchemaDtoBuilder{

		public final String name;
		public final String type;
		public final String className;
		public final List<ApiDocSchemaDto> fields;
		public final List<ApiDocSchemaDto> parameters;
		public final List<String> enumValues;
		public boolean isOptional;
		public boolean isDeprecated;
		public Long max;
		public Long min;
		public Long maxLength;
		public String description;
		public String defaultValue;
		public boolean isArray;

		private ApiDocSchemaDtoBuilder(String name, String type, String className, List<ApiDocSchemaDto> fields,
				List<String> enumValues, List<ApiDocSchemaDto> parameters){
			this.name = name;
			this.type = type;
			this.className = className;
			this.fields = fields;
			this.enumValues = enumValues;
			this.parameters = parameters;
		}

		public static ApiDocSchemaDtoBuilder buildObject(String name, String className, List<ApiDocSchemaDto> fields){
			return new ApiDocSchemaDtoBuilder(name, "object", className, fields, null, null);
		}

		public static ApiDocSchemaDtoBuilder buildEnum(String name, String className, List<String> enumValues){
			return new ApiDocSchemaDtoBuilder(name, "enum", className,null, enumValues, null);
		}

		public static ApiDocSchemaDtoBuilder buildPrimitive(String name, String type){
			return new ApiDocSchemaDtoBuilder(name, type, null, null, null, null);
		}

		public static ApiDocSchemaDtoBuilder buildParametrized(String name,
				String className, List<ApiDocSchemaDto> fields, List<ApiDocSchemaDto> parameters){
			return new ApiDocSchemaDtoBuilder(name, "parameter", className, fields, null, parameters);
		}

		public ApiDocSchemaDtoBuilder withIsOptional(boolean isOptional){
			this.isOptional = isOptional;
			return this;
		}

		public ApiDocSchemaDtoBuilder withIsDeprecated(boolean isDeprecated){
			this.isDeprecated = isDeprecated;
			return this;
		}

		public ApiDocSchemaDtoBuilder withMax(Long max){
			this.max = max;
			return this;
		}

		public ApiDocSchemaDtoBuilder withMin(Long min){
			this.min = min;
			return this;
		}

		public ApiDocSchemaDtoBuilder withMaxLength(Long maxLength){
			this.maxLength = maxLength;
			return this;
		}

		public ApiDocSchemaDtoBuilder withDescription(String description){
			this.description = description;
			return this;
		}

		public ApiDocSchemaDtoBuilder withDefaultValue(String defaultValue){
			this.defaultValue = defaultValue;
			return this;
		}

		public ApiDocSchemaDtoBuilder withIsArray(boolean isArray){
			this.isArray = isArray;
			return this;
		}

		public ApiDocSchemaDto build(){
			return new ApiDocSchemaDto(
					name,
					type,
					className,
					fields,
					parameters,
					enumValues,
					isOptional,
					isDeprecated,
					max,
					min,
					maxLength,
					description,
					defaultValue,
					isArray);
		}

	}

	public String toSimpleClassName(){
		return className.substring(className.lastIndexOf(".") + 1);
	}

	public String toEnumString(){
		return Scanner.of(this.enumValues)
				.sort()
				.collect(Collectors.joining(", "));
	}

	public String toFieldString(){
		String arrayString = isArray() ? "[]" : "";
		if("parameter".equals(type)){
			return toSimpleClassName() + arrayString + "<"
					+ parameters.stream()
					.map(ApiDocSchemaDto::toFieldString)
					.collect(Collectors.joining(", "))
					+ ">";
		}
		if(className != null){
			return toSimpleClassName() + arrayString;
		}
		return type + arrayString;
	}

	//--------------------- String functions mostly for debugging -----------------------------//
	@Override
	public String toString(){
		return stringify(0);
	}

	public String stringify(int numTabs){
		StringBuilder sb = new StringBuilder();
		buildSpaces(numTabs, sb);
		sb.append("name: ")
				.append(name)
				.append(", type: ")
				.append(type)
				.append(", className: ")
				.append(className)
				.append("\n");
		if(fields != null){
			sb.append(fieldsStringify(numTabs + 2));
		}
		if(parameters != null){
			sb.append(parametersStringify(numTabs + 2));
		}
		if(enumValues != null){
			sb.append(enumValuesStringify(numTabs));
		}
		sb.append(isOptionalStringify(numTabs));
		sb.append(isDeprecatedStringify(numTabs));
		sb.append(maxStringify(numTabs));
		sb.append(minStringify(numTabs));
		sb.append(descriptionStringify(numTabs));
		sb.append(isArrayStringify(numTabs));
		return sb.toString();
	}

	private String fieldsStringify(int numTabs){
		StringBuilder sb = new StringBuilder();
		buildSpaces(numTabs, sb);
		sb.append("fields:\n");
		for(ApiDocSchemaDto field : fields){
			sb.append(field.stringify(numTabs + 2));
		}
		return sb.toString();
	}

	private String parametersStringify(int numTabs){
		StringBuilder sb = new StringBuilder();
		buildSpaces(numTabs, sb);
		sb.append("parameters:\n");
		for(ApiDocSchemaDto parameter : parameters){
			if(parameter != null){
				sb.append(parameter.stringify(numTabs + 2));
			}
		}
		return sb.toString();
	}

	private String enumValuesStringify(int numTabs){
		StringBuilder sb = new StringBuilder();
		buildSpaces(numTabs, sb);
		sb.append("enumValues:\n");
		for(String enumValue : enumValues){
			buildSpaces(numTabs, sb);
			sb.append(enumValue).append("\n");
		}
		return sb.toString();
	}

	private String isOptionalStringify(int numTabs){
		StringBuilder sb = new StringBuilder();
		buildSpaces(numTabs, sb);
		sb.append("isOptional: ").append(isOptional).append("\n");
		return sb.toString();
	}

	private String isDeprecatedStringify(int numTabs){
		StringBuilder sb = new StringBuilder();
		buildSpaces(numTabs, sb);
		sb.append("isDeprecated: ").append(isDeprecated).append("\n");
		return sb.toString();
	}

	private String maxStringify(int numTabs){
		StringBuilder sb = new StringBuilder();
		buildSpaces(numTabs, sb);
		sb.append("max: ").append(max).append("\n");
		return sb.toString();
	}

	private String minStringify(int numTabs){
		StringBuilder sb = new StringBuilder();
		buildSpaces(numTabs, sb);
		sb.append("min: ").append(min).append("\n");
		return sb.toString();
	}

	private String descriptionStringify(int numTabs){
		StringBuilder sb = new StringBuilder();
		buildSpaces(numTabs, sb);
		sb.append("description: ").append(description).append("\n");
		return sb.toString();
	}

	private String isArrayStringify(int numTabs){
		StringBuilder sb = new StringBuilder();
		buildSpaces(numTabs, sb);
		sb.append("isArray: ").append(isArray).append("\n");
		return sb.toString();
	}

	private static void buildSpaces(int numTabs, StringBuilder sb){
		sb.append("\t".repeat(Math.max(0, numTabs)));
	}
}
