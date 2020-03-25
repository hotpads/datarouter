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
package io.datarouter.gcp.spanner.ddl;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class SpannerUpdateStatements{

	private List<String> executeStatements = new ArrayList<>();
	private List<String> printStatements = new ArrayList<>();
	private Boolean preventStartUp = false;

	public void updateFunction(String statment, Function<Boolean,Boolean> updateFunction, Boolean required){
		if(updateFunction.apply(false)){
			executeStatements.add(statment);
		}else{
			printStatements.add(statment);
			preventStartUp = preventStartUp || required;
		}
	}

	public List<String> getExcuteStatments(){
		return executeStatements;
	}

	public List<String> getPrintStatements(){
		return printStatements;
	}

	public Boolean getPreventStartUp(){
		return preventStartUp;
	}

}
