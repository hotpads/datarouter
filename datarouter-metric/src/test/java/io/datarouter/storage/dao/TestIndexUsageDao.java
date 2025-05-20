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
package io.datarouter.storage.dao;

import java.util.List;

import io.datarouter.model.field.Field;
import io.datarouter.model.key.primary.base.BaseRegularPrimaryKey;
import io.datarouter.storage.Datarouter;
import io.datarouter.storage.node.op.combo.IndexedSortedMapStorage.IndexedSortedMapStorageNode;
import io.datarouter.storage.node.op.index.IndexReader;
import io.datarouter.storage.node.op.index.IndexUsage;
import io.datarouter.storage.node.op.index.IndexUsage.IndexUsageType;
import io.datarouter.storage.node.op.index.MultiIndexReader;
import io.datarouter.storage.node.op.index.UniqueIndexReader;
import jakarta.inject.Singleton;

@Singleton
public class TestIndexUsageDao extends BaseDao{

	// Sample index fields for testing
	private final IndexedSortedMapStorageNode<MapSampleKey,?,?> node;
	private final UniqueIndexReader<?,?,UniqueSampleKey,?> uniqueIndexReader;
	private final IndexReader<?,?,StandardSampleKey,?> indexReader;
	private final IndexReader<?,?,IgnoreSampleKey,?> indexReaderIgnore;
	private final MultiIndexReader<?,?,MultiSampleKey,?> multiIndexReader;// Base abstract class

	// For IndexedSortedMapStorage
	public static class MapSampleKey extends BaseRegularPrimaryKey<MapSampleKey>{

		// Map-specific fields and methods
		@Override
		public List<Field<?>> getFields(){
			return List.of(/* map-specific fields */);
		}

	}

	// For UniqueIndexReader
	public static class UniqueSampleKey extends BaseRegularPrimaryKey<UniqueSampleKey>{

		// Unique index-specific fields and methods
		@Override
		public List<Field<?>> getFields(){
			return List.of(/* unique index-specific fields */);
		}

	}

	// For IndexReader
	public static class StandardSampleKey extends BaseRegularPrimaryKey<StandardSampleKey>{

		// Standard index-specific fields and methods
		@Override
		public List<Field<?>> getFields(){
			return List.of(/* standard index-specific fields */);
		}

	}

	// For MultiIndexReader
	public static class MultiSampleKey extends BaseRegularPrimaryKey<MultiSampleKey>{

		// Multi-index-specific fields and methods
		@Override
		public List<Field<?>> getFields(){
			return List.of(/* multi-index-specific fields */);
		}

	}

	@IndexUsage(usageType = IndexUsageType.IGNORE_USAGE)
	public static class IgnoreSampleKey extends BaseRegularPrimaryKey<IgnoreSampleKey>{

		// Multi-index-specific fields and methods
		@Override
		public List<Field<?>> getFields(){
			return List.of(/* multi-index-specific fields */);
		}

	}

	public TestIndexUsageDao(){
		super(new Datarouter());
		// Initialize with null as we're just testing field type detection
		this.uniqueIndexReader = null;
		this.indexReader = null;
		this.multiIndexReader = null;
		this.indexReaderIgnore = null;
		this.node = null;
	}

}
