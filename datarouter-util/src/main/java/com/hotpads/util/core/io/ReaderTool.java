package com.hotpads.util.core.io;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;

import com.hotpads.util.core.iterable.scanner.BatchingScanner;
import com.hotpads.util.core.iterable.scanner.Scanner;

public class ReaderTool{

	/*************** wrap exceptions **********************/

	public static BufferedReader createNewBufferedFileReader(String fullPath){
		try{
			return new BufferedReader(new FileReader(fullPath));
		}catch(FileNotFoundException e){
			throw new RuntimeIOException(e);
		}
	}

	public static void close(Reader reader){
		if(reader == null){ return; }
		try{
			reader.close();
		}catch(IOException e){
			throw new RuntimeIOException(e);
		}
	}

	public static String readLine(BufferedReader reader){
		if(reader == null){ return null; }
		try{
			return reader.readLine();
		}catch(IOException ioe){
			throw new RuntimeIOException(ioe);
		}
	}


	/***************** other *********************/

	public static StringBuilder accumulateStringAndClose(Reader reader){
		BufferedReader bufferedReader = new BufferedReader(reader);
		String line;
		StringBuilder stringBuilder = new StringBuilder();
		try{
			while((line = bufferedReader.readLine()) != null){
				stringBuilder.append(line);
			}
		}catch(IOException e){
			throw new RuntimeIOException(e);
		}finally{
			close(reader);
		}
		return stringBuilder;
	}

	public static StringBuilder accumulateStringAndClose(InputStream inputStream){
		return accumulateStringAndClose(new InputStreamReader(inputStream));
	}


	/*************** scanners *******************/

	public static Scanner<String> scanLines(BufferedReader reader){
		return new ReaderScanner(reader);
	}

	public static Scanner<List<String>> scanLinesInBatches(BufferedReader reader, int batchSize){
		return new BatchingScanner<String>(new ReaderScanner(reader), batchSize);
	}

	public static Scanner<List<String>> scanFileLinesInBatches(String fullPath, int batchSize){
		BufferedReader bufferedReader = createNewBufferedFileReader(fullPath);
		return new BatchingScanner<String>(new ReaderScanner(bufferedReader), batchSize);
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
			if(line != null){ return true; }
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
