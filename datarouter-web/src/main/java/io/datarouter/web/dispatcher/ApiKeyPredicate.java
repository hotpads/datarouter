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
package io.datarouter.web.dispatcher;

import javax.servlet.http.HttpServletRequest;

import org.testng.Assert;
import org.testng.annotations.Test;

import io.datarouter.util.tuple.Pair;

public interface ApiKeyPredicate{

	Pair<Boolean,String> check(DispatchRule rule, HttpServletRequest request);

	static String obfuscate(String apiKeyCandidate){
		int start = Math.min((apiKeyCandidate.length() - 1) / 2, 2);
		int end;
		if(apiKeyCandidate.length() > 5){
			end = apiKeyCandidate.length() - 2;
		}else{
			end = Math.max(apiKeyCandidate.length() - 1, 3);
		}
		int index = 0;
		var sb = new StringBuilder();
		for(; index < start; index++){
			sb.append(apiKeyCandidate.charAt(index));
		}
		for(; index < Math.min(end, apiKeyCandidate.length()); index++){
			sb.append('*');
		}
		for(; index < apiKeyCandidate.length(); index++){
			sb.append(apiKeyCandidate.charAt(index));
		}
		return sb.toString();
	}

	public static class ApiKeyPredicateTests{

		@Test
		public void obfuscateTest(){
			Assert.assertEquals(ApiKeyPredicate.obfuscate("secret"), "se**et");
			Assert.assertEquals(ApiKeyPredicate.obfuscate("pzazz"), "pz**z");
			Assert.assertEquals(ApiKeyPredicate.obfuscate("1234"), "1**4");
			Assert.assertEquals(ApiKeyPredicate.obfuscate("αβγ"), "α**");
			Assert.assertEquals(ApiKeyPredicate.obfuscate("ab"), "**");
			Assert.assertEquals(ApiKeyPredicate.obfuscate("4T(F~Q2`\\e7<PW@Q"), "4T************@Q");
		}

	}

}