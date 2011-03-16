package com.hotpads.datarouter.backup.imp.memory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPOutputStream;

import com.hotpads.datarouter.backup.BackupRegion;
import com.hotpads.datarouter.backup.databean.BackupRecord;
import com.hotpads.datarouter.backup.databean.BackupRecordKey;
import com.hotpads.datarouter.node.op.raw.MapStorage;
import com.hotpads.datarouter.node.op.raw.SortedStorage.SortedStorageNode;
import com.hotpads.datarouter.routing.DataRouter;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

public class BackupRegionToMemory<PK extends PrimaryKey<PK>,D extends Databean<PK,D>> 
extends BackupRegion<PK,D>{
	
	protected ByteArrayOutputStream byteArrayOutputStream;

	public BackupRegionToMemory(DataRouter router, SortedStorageNode<PK,D> node, 
			PK startKeyInclusive, PK endKeyExclusive,
			boolean gzip,
			MapStorage<BackupRecordKey,BackupRecord> backupRecordNode) throws IOException{
		super(router, node, startKeyInclusive, endKeyExclusive, backupRecordNode);
		this.byteArrayOutputStream = new ByteArrayOutputStream();
		this.os = byteArrayOutputStream;
		if(gzip){
			this.os = new GZIPOutputStream(os, BackupRegion.GZIP_BUFFER_BYTES);
		}
		super.os = this.os;//better way to do this?
	}
	
	@Override
	public void execute() throws IOException{
		exportWithoutClosingOutputStream();
		if(os!=null){ 
			if(os instanceof GZIPOutputStream){
				((GZIPOutputStream)os).finish();
				((GZIPOutputStream)os).flush();
			}
			os.close(); 
		}
	}
	
	public byte[] getResult(){
		return byteArrayOutputStream.toByteArray();
	}
}
