package com.hotpads.spark.data.compressors;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.compress.CompressionCodec;
import org.apache.hadoop.io.compress.SnappyCodec;
import org.apache.hadoop.util.ReflectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//TODO snappy is not splittable and S3 will split before uploading
public class SnappyDataCompressor implements DataCompressor{
	private static final Logger logger = LoggerFactory.getLogger(SnappyDataCompressor.class);

	private static final int BUFFER_SIZE = 64 * 1024;

	@Override
	public boolean compress(String inputPath, String outputPath) throws IOException{
		try {
			logger.info("About to compress {} -> {}", inputPath, outputPath);
			CompressionCodec codec = ReflectionUtils.newInstance(SnappyCodec.class, new Configuration());

			InputStream inputStream = new BufferedInputStream(new FileInputStream(inputPath));
			OutputStream outputStream = codec.createOutputStream(new BufferedOutputStream(
					new FileOutputStream(outputPath)));
			int readCount;
			byte[] buffer = new byte[BUFFER_SIZE];
			while ((readCount = inputStream.read(buffer)) > 0) {
				outputStream.write(buffer, 0, readCount);
			}
			inputStream.close();
			outputStream.close();
			logger.info("File Compressed");
		} catch (Exception e) {
			logger.error("Exception during compression", e);
			return false;
		}

		return true;
	}

	@Override
	public String getFileExtension(){
		return ".snappy";
	}
}
