package com.hotpads.datarouter.backup.imp.s3;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;

import com.hotpads.datarouter.backup.BackupRegion;
import com.hotpads.datarouter.backup.RestoreRegion;
import com.hotpads.datarouter.client.imp.s3.S3GetTool;
import com.hotpads.datarouter.node.op.raw.MapStorage.MapStorageNode;
import com.hotpads.datarouter.routing.Datarouter;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.util.core.FileUtils;
import com.hotpads.util.core.IterableTool;
import com.hotpads.util.core.MapTool;

public class RestoreRegionFromS3<PK extends PrimaryKey<PK>,D extends Databean<PK,D>> 
extends RestoreRegion<PK,D>{

	protected String s3Bucket;
	protected String s3Key;
	protected boolean gzip;
	protected boolean deleteLocalFile;
	protected boolean downloadNewestCopy;

	protected String localPath;

	public RestoreRegionFromS3(String s3Bucket, String s3Key, Class<D> cls, Datarouter router,
			MapStorageNode<PK,D> node, Integer putBatchSize, Boolean ignoreNullFields, Boolean downloadNewestCopy,
			boolean gzip, boolean deleteLocalFile){
		super(cls, router, node, putBatchSize, ignoreNullFields);
		this.s3Bucket = s3Bucket;
		this.s3Key = s3Key;
		this.localPath = BackupRegionToS3.getLocalPath(s3Key);
		this.fieldByPrefixedName = MapTool.createHashMap();
		for(Field<?> field : IterableTool.nullSafe(node.getFields())){
			this.fieldByPrefixedName.put(field.getPrefixedName(), field);
		}
		this.gzip = gzip;
		this.deleteLocalFile = deleteLocalFile;
		this.downloadNewestCopy = downloadNewestCopy;
	}
	
	@Override
	public Void call(){
		try{
			logger.warn("starting restore of "+s3Key);
			if(downloadNewestCopy){ downloadFromS3(); }
			is = new FileInputStream(localPath);
			if(gzip){
				is = new GZIPInputStream(is, BackupRegion.GZIP_BUFFER_BYTES);
			}
			/*
			 * 
			 * WARNING - without buffering the GZIPInputStream, it sometimes returns invalid bytes!!!  no idea why =(
			 * 
			 */
			is = new BufferedInputStream(is, 1<<20);
			importAndCloseInputStream();
			if(deleteLocalFile){
				FileUtils.delete(localPath);
			}
			logger.warn("completed restore of "+s3Key);
		}catch(IOException ioe){
			throw new RuntimeException(ioe);
		}
		return null;
	}
	
	protected void downloadFromS3() throws IOException{
		FileUtils.createFileParents(localPath);
		File localFile = new File(localPath);
		S3GetTool.getFile(s3Bucket, s3Key, localFile);
	}
}
