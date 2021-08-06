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
package io.datarouter.storage.callsite;

import java.util.Arrays;
import java.util.Comparator;

import io.datarouter.storage.callsite.CallsiteStat.CallsiteCountComparator;
import io.datarouter.storage.callsite.CallsiteStat.CallsiteDurationComparator;
import io.datarouter.util.lang.ReflectionTool;

public enum CallsiteStatComparator{
	COUNT("count", CallsiteCountComparator.class),
	DURATION("duration", CallsiteDurationComparator.class);

	private final String varName;
	private final Class<? extends Comparator<CallsiteStat>> comparatorClass;

	CallsiteStatComparator(String varName, Class<? extends Comparator<CallsiteStat>> comparatorClass){
		this.varName = varName;
		this.comparatorClass = comparatorClass;
	}

	public static CallsiteStatComparator fromVarName(String string){
		return Arrays.stream(values())
				.filter(comparator -> comparator.getVarName().equals(string))
				.findFirst()
				.orElseThrow(() -> new IllegalArgumentException(string + " not found"));
	}

	public Comparator<CallsiteStat> getComparator(){
		return ReflectionTool.create(comparatorClass);
	}

	public String getVarName(){
		return varName;
	}

}
