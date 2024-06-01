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

import org.eclipse.jgit.api.DiffCommand;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;

import io.datarouter.scanner.Scanner;

public class DatarouterGitDiffTool{

	public static DiffCommand diffBetweenRefs(DiffCommand op, String oldRevstr, String newRevstr){
		try{
			Repository repo = op.getRepository();
			return op
					.setOldTree(prepareTreeParser(repo, oldRevstr))
					.setNewTree(prepareTreeParser(repo, newRevstr));
		}catch(Exception e){
			throw new RuntimeException(e);
		}
	}

	public static DiffCommand diffBetweenRefs(DiffCommand op, RevCommit oldTree, RevCommit newTree){
		try{
			Repository repo = op.getRepository();
			return op
					.setOldTree(prepareTreeParser(repo, oldTree))
					.setNewTree(prepareTreeParser(repo, newTree));
		}catch(Exception e){
			throw new RuntimeException(e);
		}
	}

	private static AbstractTreeIterator prepareTreeParser(Repository repository, RevCommit commit) throws Exception{
		try(RevWalk walk = new RevWalk(repository)){
			RevTree tree = walk.parseTree(commit.getTree().getId());
			var treeParser = new CanonicalTreeParser();
			try(ObjectReader reader = repository.newObjectReader()){
				treeParser.reset(reader, tree.getId());
			}
			walk.dispose();
			return treeParser;
		}
	}

	private static AbstractTreeIterator prepareTreeParser(Repository repository, String revstr) throws Exception{
		try(RevWalk walk = new RevWalk(repository)){
			RevCommit commit = walk.parseCommit(repository.resolve(revstr));
			RevTree tree = walk.parseTree(commit.getTree().getId());
			var treeParser = new CanonicalTreeParser();
			try(ObjectReader reader = repository.newObjectReader()){
				treeParser.reset(reader, tree.getId());
			}
			walk.dispose();
			return treeParser;
		}
	}

	public static Scanner<String> getChangedFiles(DiffEntry entry){
		return Scanner.of(entry.getOldPath(), entry.getNewPath())
				.exclude(path -> path.toLowerCase().equals(DiffEntry.DEV_NULL))
				.distinct();
	}

}
