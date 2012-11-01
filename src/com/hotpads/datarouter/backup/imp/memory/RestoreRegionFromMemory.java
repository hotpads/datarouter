package com.hotpads.datarouter.backup.imp.memory;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;

import com.hotpads.datarouter.backup.BackupRegion;
import com.hotpads.datarouter.backup.RestoreRegion;
import com.hotpads.datarouter.node.op.combo.SortedMapStorage.SortedMapStorageNode;
import com.hotpads.datarouter.routing.DataRouter;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.util.core.IterableTool;
import com.hotpads.util.core.MapTool;

public class RestoreRegionFromMemory<PK extends PrimaryKey<PK>,D extends Databean<PK,D>> 
extends RestoreRegion<PK,D>{
	
	public RestoreRegionFromMemory(byte[] bytes, Class<D> cls, 
			DataRouter router, SortedMapStorageNode<PK,D> node, boolean gzip) throws IOException{
		super(cls, router, node, 1000, false);
		this.is = new ByteArrayInputStream(bytes);
		if(gzip){
			this.is = new BufferedInputStream(new GZIPInputStream(is, BackupRegion.GZIP_BUFFER_BYTES));
		}
		this.fieldByPrefixedName = MapTool.createHashMap();
		for(Field<?> field : IterableTool.nullSafe(node.getFields())){
			this.fieldByPrefixedName.put(field.getPrefixedName(), field);
		}
	}
	
	@Override
	public Void call(){
		importAndCloseInputStream();
		return null;
	}
	
}
