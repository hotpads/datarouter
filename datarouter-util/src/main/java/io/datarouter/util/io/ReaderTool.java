/**
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
package io.datarouter.util.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import io.datarouter.util.iterable.scanner.BatchingScanner;
import io.datarouter.util.iterable.scanner.Scanner;

public class ReaderTool{

	/*------------------------- wrap exceptions -----------------------------*/

	public static BufferedReader createNewBufferedFileReader(String fullPath){
		try{
			return Files.newBufferedReader(Paths.get(fullPath));
		}catch(IOException e){
			throw new UncheckedIOException(e);
		}
	}

	public static void close(Reader reader){
		if(reader == null){
			return;
		}
		try{
			reader.close();
		}catch(IOException e){
			throw new UncheckedIOException(e);
		}
	}

	public static String readLine(BufferedReader reader){
		if(reader == null){
			return null;
		}
		try{
			return reader.readLine();
		}catch(IOException ioe){
			throw new UncheckedIOException(ioe);
		}
	}

	/*------------------------- other ---------------------------------------*/

	public static String accumulateStringAndClose(Reader reader){
		StringBuilder stringBuilder = new StringBuilder();
		String line;
		try(BufferedReader bufferedReader = new BufferedReader(reader)){
			while((line = bufferedReader.readLine()) != null){
				stringBuilder.append(line);
			}
		}catch(IOException e){
			throw new UncheckedIOException(e);
		}
		return stringBuilder.toString();
	}

	public static String accumulateStringAndClose(InputStream inputStream){
		return accumulateStringAndClose(new InputStreamReader(inputStream));
	}

	/*------------------------- scanners ------------------------------------*/

	public static Scanner<List<String>> scanFileLinesInBatches(String fullPath, int batchSize){
		BufferedReader bufferedReader = createNewBufferedFileReader(fullPath);
		return new BatchingScanner<>(new ReaderScanner(bufferedReader), batchSize);
	}

	public static class ReaderScanner implements Scanner<String>{
		private BufferedReader reader;
		private String line;

		private ReaderScanner(BufferedReader reader){
			this.reader = reader;
		}

		@Override
		public boolean advance(){
			line = readLine(reader);
			if(line != null){
				return true;
			}
			close(reader);
			reader = null;//need to nullify so future calls to advance don't try to readLine on closed reader
			return false;
		}

		@Override
		public String getCurrent(){
			return line;
		}
	}

}
