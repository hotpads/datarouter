package com.hotpads.datarouter.backup.imp.s3;

import java.io.File;
import java.io.IOException;
import java.util.function.Predicate;
import java.util.zip.GZIPOutputStream;

import com.hotpads.datarouter.backup.BackupRegion;
import com.hotpads.datarouter.client.imp.s3.S3PutTool;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.Node;
import com.hotpads.datarouter.node.op.raw.read.SortedStorageReader.SortedStorageReaderNode;
import com.hotpads.datarouter.routing.Router;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.util.DrFileIOFactory;
import com.hotpads.datarouter.util.core.DrFileUtils;
import com.hotpads.datarouter.util.core.DrNumberFormatter;
import com.hotpads.util.core.profile.PhaseTimer;

public class BackupRegionToS3<PK extends PrimaryKey<PK>,D extends Databean<PK,D>>
extends BackupRegion<PK,D>{

	public static final String DEFAULT_BUCKET = "backup.hotpads.com";
	public static final String DEFAULT_EXPORT_FOLDER = "datarouter";
	public static final String MIGRATION_EXPORT_FOLDER = "migration";


	private final String s3Bucket;
	private String s3Key;
	private String localPath;
	private final boolean gzip;
	private final boolean deleteLocalFile;
	private final PhaseTimer timer;


	public BackupRegionToS3(String s3Bucket, String sourceName, Router router, SortedStorageReaderNode<PK,D> node,
			String exportFolder, Config config, PK startKeyInclusive, PK endKeyExclusive, Predicate<D> predicate,
			long maxRows, boolean gzip, boolean deleteLocalFile, PhaseTimer timer){
		super(node, config, startKeyInclusive, endKeyExclusive, predicate, maxRows);
		this.s3Bucket = s3Bucket;
		String s3Key = makeS3Key(exportFolder, sourceName, router, node);
		this.s3Key = s3Key;
		this.localPath = makeLocalPath(s3Key);
		this.gzip = gzip;
		this.deleteLocalFile = deleteLocalFile;
		this.timer = timer;
	}

	public BackupRegionToS3(String s3Bucket, String sourceName, Router router, SortedStorageReaderNode<PK,D> node,
			String exportFolder, Config config, String startKeyInclusive, String endKeyExclusive,
			Predicate<D> predicate, long maxRows, boolean gzip, boolean deleteLocalFile, PhaseTimer timer){
		this(s3Bucket, sourceName, router, node, exportFolder, config, convertStringToPk(startKeyInclusive, node),
				convertStringToPk(endKeyExclusive, node), predicate, maxRows, gzip, deleteLocalFile, timer);
	}

	@Override
	public void execute(){
		try{
			DrFileUtils.delete(localPath);
			os = DrFileIOFactory.makeFileOutputStream(localPath, true, true);
			if(gzip){
				os = new GZIPOutputStream(os, BackupRegion.GZIP_BUFFER_BYTES);
			}
			try{
				exportWithoutClosingOutputStream();
				timer.add("exported " + DrNumberFormatter.addCommas(numRecords) + ", " + DrNumberFormatter.addCommas(
						rawBytes) + "b");
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
			logger.warn(timer.toString());
			try{
				uploadToS3();
			}catch(InterruptedException e){
				throw new RuntimeException(e);
			}
			if(deleteLocalFile){
				DrFileUtils.delete(localPath);
			}
		}catch(IOException e){
			throw new RuntimeException(e);
		}
	}

	protected void uploadToS3() throws IOException, InterruptedException{
		File localFile = new File(localPath);
		S3PutTool.uploadLargeFile(localFile, s3Bucket, s3Key);
		timer.add("uploaded to s3: " + getS3Key());
		logger.warn(timer.toString());
	}

	public String getS3Key(){
		return s3Key;
	}

	public void setS3Key(String s3Key){
		this.s3Key = s3Key;
	}

	public String getLocalPath(){
		return localPath;
	}

	public void setLocalPath(String localPath){
		this.localPath = localPath;
	}


	public static String makeLocalPath(String s3Key){
		return "/hotpads/backup/"+s3Key;
	}

	public static String makeS3Key(String exportFolder, String sourceName, Router router, Node<?,?> node){
		return exportFolder+"/"+sourceName+"/"+router.getName()+"/"+node.getName();
	}


}
