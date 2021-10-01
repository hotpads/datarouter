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

import java.util.Objects;
import java.util.Set;

public class DocumentedExampleDto{

	public final Object exampleObject;
	public final Set<DocumentedExampleEnumDto> exampleEnumDtos;

	public DocumentedExampleDto(Object exampleObject, Set<DocumentedExampleEnumDto> enums){
		this.exampleObject = exampleObject;
		this.exampleEnumDtos = enums;
	}

	public static class DocumentedExampleEnumDto{

		public final String enumName;
		public final String enumValuesDisplay;

		public DocumentedExampleEnumDto(String enumName, String enumValuesDisplay){
			this.enumName = enumName;
			this.enumValuesDisplay = enumValuesDisplay;
		}

		@Override
		public int hashCode(){
			return Objects.hash(enumName, enumValuesDisplay);
		}

		@Override
		public boolean equals(Object other){
			if(this == other){
				return true;
			}
			if(!(other instanceof DocumentedExampleEnumDto)){
				return false;
			}
			DocumentedExampleEnumDto that = (DocumentedExampleEnumDto) other;
			return this.enumName.equals(that.enumName) && this.enumValuesDisplay.equals(that.enumValuesDisplay);
		}

	}

}
