package com.hotpads.datarouter.node.op.index;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.storage.view.index.multi.MultiIndexEntry;
import com.hotpads.util.core.collections.Range;
import com.hotpads.util.core.stream.StreamTool;

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
		IE extends MultiIndexEntry<IK, IE, PK, D>>
extends IndexReader<PK,D,IK,IE>{

	List<D> lookupMulti(IK indexKey, Config config);
	List<D> lookupMultiMulti(Collection<IK> indexKeys, Config config);

	Iterable<D> scanDatabeans(Range<IK> range, Config config);

	default Stream<D> streamDatabeans(Range<IK> range, Config config){
		return StreamTool.stream(scanDatabeans(range, config));
	}

}
