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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jgit.errors.StopWalkException;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.revwalk.filter.RevFilter;

import io.datarouter.scanner.Scanner;

public class DatarouterGitRevFilters{

	public static List<MergeCommitHistory> getMergeCommitHistory(Iterable<RevCommit> commitWalk){
		List<RevCommit> commits = Scanner.of(commitWalk).list();
		Map<String,RevCommit> commitByName = Scanner.of(commits)
				.toMap(RevCommit::name);

		List<RevCommit> merges = Scanner.of(commits)
				.include(commit -> commit.getParentCount() >= 2)
				.include(new FirstParentTraverser()::include)
				.list();

		Set<String> mergeCommits = Scanner.of(merges)
				.map(RevCommit::name)
				.collect(HashSet::new);

		return Scanner.of(merges)
				.map(merge -> {
					List<RevCommit> children = new ArrayList<>();
					RevCommit child = commitByName.get(merge.getParent(1).name());
					while(child != null && !mergeCommits.contains(child.name())){
						children.add(child);

						if(child.getParentCount() == 0){
							break;
						}

						child = commitByName.get(child.getParent(0).name());
					}
					return new MergeCommitHistory(merge, children);
				})
				.list();
	}

	public static class FirstParentTraverser extends RevFilter{

		private String lastFirstParent;

		public boolean include(RevCommit cmit){
			if(cmit.getParentCount() > 0 && (lastFirstParent == null || cmit.name().equals(lastFirstParent))){
				lastFirstParent = cmit.getParent(0).name();
				return true;
			}
			return false;
		}

		@Override
		public boolean include(RevWalk walker, RevCommit cmit) throws StopWalkException{
			return include(cmit);
		}

		@Override
		public RevFilter clone(){
			return new FirstParentTraverser();
		}

	}

	public record MergeCommitHistory(
			RevCommit merge,
			List<RevCommit> history){
	}

}
