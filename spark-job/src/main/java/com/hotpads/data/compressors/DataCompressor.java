package com.hotpads.data.compressors;

import java.io.IOException;

public interface DataCompressor{
	/*
	 * compress the file from the given inputPath ,writes the compressed file to the given outPath
	 * and returns whether the compression was successful or not
	 */
	boolean compress(String inputPath, String outputPath) throws IOException;
	String getFileExtension();
}
