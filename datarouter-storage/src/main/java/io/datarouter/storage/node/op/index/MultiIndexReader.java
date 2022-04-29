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
package io.datarouter.storage.node.op.index;

import java.util.Collection;
import java.util.List;

import io.datarouter.model.databean.Databean;
import io.datarouter.model.index.multi.MultiIndexEntry;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.storage.config.Config;

/**
 * Methods for reading from storage systems that provide secondary indexing. This interface provides powerful iterators
 * for scanning through each IndexEntry in index order or to scan through the indexed table's databeans in the order of
 * the index. Note that scanning through the main table's rows in order of a secondary index will be much slower than
 * scanning the main table directly as it requires random instead of sequential IO and requires many more overall IO
 * operations.
 *
 * RDBMS's provide secondary indexing on tables. Most document oriented stores provide it, like DynamoDB, Mongo, and
 * CouchDB. Others include BerkeleyDB, BerkeleyDB Java, some HBase libraries, Google Cloud Datastore, Amazon SimpleDB,
 * and possibly DynamoDB.
 */
public interface MultiIndexReader<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		IK extends PrimaryKey<IK>,
		IE extends MultiIndexEntry<IK,IE,PK,D>>
extends IndexReader<PK,D,IK,IE>{

	List<D> lookupMulti(IK indexKey, Config config);

	default List<D> lookupMulti(IK indexKey){
		return lookupMulti(indexKey, new Config());
	}

	List<D> lookupMultiMulti(Collection<IK> indexKeys, Config config);

	default List<D> lookupMultiMulti(Collection<IK> indexKeys){
		return lookupMultiMulti(indexKeys, new Config());
	}

}
