package com.hotpads.data.compressors;

import java.io.IOException;

public interface DataDecompressor{
	boolean decompress(String inputPath, String outputPath) throws IOException;
}
