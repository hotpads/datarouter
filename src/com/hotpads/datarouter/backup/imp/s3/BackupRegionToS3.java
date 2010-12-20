package com.hotpads.datarouter.backup.imp.s3;

import java.io.File;
import java.io.IOException;
import java.util.zip.GZIPOutputStream;

import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.hotpads.datarouter.backup.BackupRegion;
import com.hotpads.datarouter.backup.databean.BackupRecord;
import com.hotpads.datarouter.backup.databean.BackupRecordKey;
import com.hotpads.datarouter.client.imp.s3.S3Headers;
import com.hotpads.datarouter.client.imp.s3.S3PutTool;
import com.hotpads.datarouter.node.Node;
import com.hotpads.datarouter.node.op.raw.MapStorage;
import com.hotpads.datarouter.node.op.raw.SortedStorage.SortedStorageNode;
import com.hotpads.datarouter.routing.DataRouter;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.util.core.FileUtils;
import com.hotpads.util.core.io.FileIOFactory;

public class BackupRegionToS3<PK extends PrimaryKey<PK>,D extends Databean<PK>> 
extends BackupRegion<PK,D>{
	
	public static final String DEFAULT_BUCKET = "backup.hotpads.com";
	protected String s3Bucket;
	protected String sourceName;
	protected String s3Key;
	protected String localPath;
	protected boolean gzip;
	protected boolean deleteLocalFile;

	public BackupRegionToS3(String s3Bucket, String sourceName,
			DataRouter router, SortedStorageNode<PK,D> node, 
			PK startKeyInclusive, PK endKeyExclusive,
			boolean gzip, boolean deleteLocalFile,
			MapStorage<BackupRecordKey,BackupRecord> backupRecordNode){
		super(router, node, startKeyInclusive, endKeyExclusive, backupRecordNode);
		this.s3Bucket = s3Bucket;
		this.sourceName = sourceName;
		this.s3Key = getS3Key(sourceName, router, node);
		this.localPath = getLocalPath(this.s3Key);
		this.gzip = gzip;
		this.deleteLocalFile = deleteLocalFile;
	}
	
	@Override
	public void execute(){
		FileUtils.delete(localPath);
		try{
			os = FileIOFactory.makeFileOutputStream(localPath, true, true);
			if(gzip){
				os = new GZIPOutputStream(os, BackupRegion.GZIP_BUFFER_BYTES);
			}
			try{
				exportWithoutClosingOutputStream();
			}finally{
				try{
					if(os!=null){ 
						if(os instanceof GZIPOutputStream){//these may not be necessary
							((GZIPOutputStream)os).finish();
							((GZIPOutputStream)os).flush();
						}
						os.close(); 
					}
				}catch(IOException ioe){
					throw new RuntimeException(ioe);
				}
			}
			uploadToS3();
			super.recordMeta();
			if(deleteLocalFile){
				FileUtils.delete(localPath);
			}
		}catch(IOException ioe){
			throw new RuntimeException(ioe);
		}
	}
	
	protected void uploadToS3() throws IOException{
		File localFile = new File(localPath);
		S3PutTool.putFile(false, s3Bucket, localFile, s3Key, CannedAccessControlList.Private,
				S3Headers.CONTENT_TYPE_GZIP, S3Headers.CACHE_CONTROL_NO_CACHE);
	}
	
	public static String getS3Key(String sourceName, DataRouter router, Node<?,?> node){
		return "datarouter/"+sourceName+"/"+router.getName()+"/"+node.getName();
	}
	
	public static String getLocalPath(String s3Key){
		return "/hotpads/backup/"+s3Key;
	}
}
