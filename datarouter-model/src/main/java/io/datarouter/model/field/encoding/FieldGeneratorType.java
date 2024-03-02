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
package io.datarouter.model.field.encoding;


public enum FieldGeneratorType{
	NONE(false),
	/**
	 * @deprecated prefer RANDOM. Managed will front load tables and create hot spots. A managed index might also make
	 * migrating to a different datastore harder. RANDOM can be directly replaced as long as the business
	 * logic does not rely on a sequential PK, in which case the logic should be refactored.
	 */
	@Deprecated
	MANAGED(true),
	RANDOM(true),
	;

	private final boolean generated;

	FieldGeneratorType(boolean generated){
		this.generated = generated;
	}

	public boolean isGenerated(){
		return generated;
	}

}
