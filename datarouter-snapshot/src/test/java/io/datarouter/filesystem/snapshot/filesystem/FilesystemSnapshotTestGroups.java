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

import java.util.function.Function;

import io.datarouter.filesystem.client.FilesystemTestClientIds;
import io.datarouter.filesystem.snapshot.group.SnapshotGroup;
import io.datarouter.filesystem.snapshot.group.SnapshotGroupFactory;
import io.datarouter.pathnode.PathNode;
import io.datarouter.pathnode.PathsRoot;
import io.datarouter.storage.Datarouter;
import io.datarouter.storage.file.Directory;
import io.datarouter.storage.node.factory.BlobNodeFactory;
import io.datarouter.storage.node.op.raw.BlobStorage.PhysicalBlobStorageNode;
import io.datarouter.storage.util.Subpath;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class FilesystemSnapshotTestGroups{

	@Singleton
	private static class Paths extends PathNode implements PathsRoot{

		final PathNode large = leaf("large");
		final PathNode medium = leaf("medium");
		final PathNode small = leaf("small");
		final SortingPaths sorting = branch(SortingPaths::new, "sorting");
		final PathNode word = leaf("word");

		static class SortingPaths extends PathNode{
			final PathNode input = leaf("input");
			final PathNode chunk = leaf("chunk");
			final PathNode output = leaf("output");
		}

	}

	public final SnapshotGroup large;
	public final SnapshotGroup medium;
	public final SnapshotGroup small;
	public final SnapshotGroup sortingInput;
	public final SnapshotGroup sortingChunk;
	public final SnapshotGroup sortingOutput;
	public final SnapshotGroup word;

	@Inject
	public FilesystemSnapshotTestGroups(
			Datarouter datarouter,
			BlobNodeFactory blobNodeFactory,
			SnapshotGroupFactory snapshotGroupFactory,
			Paths paths){
		PhysicalBlobStorageNode node = blobNodeFactory.create(
				FilesystemTestClientIds.TEST,
				"snapshot",
				Subpath.empty());
		datarouter.register(node);

		Function<PathNode,SnapshotGroup> builder = path -> snapshotGroupFactory
				.startBuilder(path.join("/"), new Directory(node, new Subpath(path)))
				.build();
		large = builder.apply(paths.large);
		medium = builder.apply(paths.medium);
		small = builder.apply(paths.small);
		sortingInput = builder.apply(paths.sorting.input);
		sortingChunk = builder.apply(paths.sorting.chunk);
		sortingOutput = builder.apply(paths.sorting.output);
		word = builder.apply(paths.word);
	}

}
