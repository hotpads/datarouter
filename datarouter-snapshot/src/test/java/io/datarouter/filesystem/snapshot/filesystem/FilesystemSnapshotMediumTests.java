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
package io.datarouter.filesystem.snapshot.filesystem;

import java.util.List;

import javax.inject.Inject;

import org.testng.annotations.Guice;

import io.datarouter.filesystem.DatarouterSnapshotModuleFactory;
import io.datarouter.filesystem.snapshot.BaseSnapshotTests;
import io.datarouter.filesystem.snapshot.group.SnapshotGroup;
import io.datarouter.scanner.Scanner;
import io.datarouter.util.string.StringTool;

@Guice(moduleFactory = DatarouterSnapshotModuleFactory.class)
public class FilesystemSnapshotMediumTests extends BaseSnapshotTests{

	private final SnapshotGroup group;

	@Inject
	public FilesystemSnapshotMediumTests(FilesystemSnapshotTestGroups groups){
		group = groups.medium;
	}

	@Override
	protected SnapshotGroup getGroup(){
		return group;
	}

	@Override
	protected List<String> getInputs(){
		return Scanner.iterate(0, i -> i + 2)//even numbers
				.limit(1000)
				.map(Object::toString)
				.map(s -> StringTool.pad(s, '0', 10))
				.list();
	}

	@Override
	protected int getNumThreads(){
		return getNumVcpus();
	}

}
