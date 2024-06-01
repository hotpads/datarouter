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
package io.datarouter.client.git;

import java.io.IOException;
import java.util.List;

import org.eclipse.jgit.lib.AbbreviatedObjectId;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;

import io.datarouter.httpclient.response.Conditional;

public class DatarouterGitCommitTool{

	public static Conditional<ObjectId> handleAbbreviatedSha1(AbbreviatedObjectId abbreviated, Repository repository){
		List<ObjectId> matched = resolveAbbreviated(repository, abbreviated);
		return switch(matched.size()){
		case 0 -> Conditional.failure(new RuntimeException("Cannot find ObjectId for " + abbreviated.name()));
		case 1 -> Conditional.success(matched.getFirst());
		default -> Conditional.failure(new RuntimeException("More than one ObjectId matches " + abbreviated.name()));
		};
	}

	private static List<ObjectId> resolveAbbreviated(Repository repository, AbbreviatedObjectId abbreviated){
		try{
			return List.copyOf(repository.getObjectDatabase().newReader().resolve(abbreviated));
		}catch(RuntimeException | IOException e){
			throw new RuntimeException(e);
		}
	}

}
