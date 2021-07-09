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
package io.datarouter.filesystem.raw.small;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Optional;
import java.util.concurrent.ExecutorService;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.scanner.ObjectScanner;
import io.datarouter.scanner.ParallelScannerContext;
import io.datarouter.scanner.Scanner;
import io.datarouter.util.split.ChunkScannerTool;

@Singleton
public class BinaryFileService{

	@Inject
	private CheckedBinaryFileService checkedService;

	public void writeBytes(Path fullPath, byte[] contents){
		writeBytes(fullPath, ObjectScanner.of(contents));
	}

	public void writeBytes(Path fullPath, Scanner<byte[]> chunks){
		try{
			checkedService.writeBytes(fullPath, chunks);
		}catch(IOException e){
			throw new RuntimeException(e);
		}
	}

	public void writeBytes(Path fullPath, InputStream inputStream){
		try{
			checkedService.writeBytes(fullPath, inputStream);
		}catch(IOException e){
			throw new RuntimeException(e);
		}
	}

	public Optional<Long> length(Path fullPath){
		try{
			return checkedService.length(fullPath);
		}catch(IOException e){
			throw new RuntimeException(e);
		}
	}

	public byte[] readBytes(Path fullPath){
		try{
			return checkedService.readBytes(fullPath);
		}catch(IOException e){
			throw new RuntimeException(e);
		}
	}

	public byte[] readBytes(Path fullPath, long offset, int length){
		try{
			return checkedService.readBytes(fullPath, offset, length);
		}catch(IOException e){
			throw new RuntimeException(e);
		}
	}

	public Scanner<byte[]> scanChunks(
			Path fullPath,
			ExecutorService exec,
			int numThreads,
			int chunkSize){
		long totalLength = length(fullPath).orElseThrow();
		return ChunkScannerTool.scanChunks(totalLength, chunkSize)
				.parallel(new ParallelScannerContext(exec, numThreads, false))
				.map(range -> readBytes(fullPath, range.start, range.length));
	}

	@Singleton
	public static class CheckedBinaryFileService{

		public void writeBytes(Path fullPath, byte[] contents)
		throws IOException{
			writeBytes(fullPath, ObjectScanner.of(contents));
		}

		public void writeBytes(Path fullPath, Scanner<byte[]> chunks)
		throws IOException{
			fullPath.getParent().toFile().mkdirs();
			fullPath.toFile().createNewFile();
			try(OutputStream outputStream = Files.newOutputStream(fullPath, StandardOpenOption.APPEND)){
				for(byte[] chunk : chunks.iterable()){
					outputStream.write(chunk);
				}
			}
		}

		public void writeBytes(Path fullPath, InputStream inputStream)
		throws IOException{
			fullPath.getParent().toFile().mkdirs();
			fullPath.toFile().createNewFile();
			try(OutputStream outputStream = Files.newOutputStream(fullPath, StandardOpenOption.APPEND)){
				inputStream.transferTo(outputStream);
			}
		}

		public Optional<Long> length(Path fullPath)
		throws IOException{
			try{
				return Optional.of(Files.size(fullPath));
			}catch(NoSuchFileException e){
				return Optional.empty();
			}
		}

		public byte[] readBytes(Path fullPath)
		throws IOException{
			return Files.readAllBytes(fullPath);
		}

		public byte[] readBytes(Path fullPath, long offset, int length)
		throws IOException{
			ByteBuffer buffer = ByteBuffer.allocate(length);
			try(SeekableByteChannel channel = Files.newByteChannel(fullPath, StandardOpenOption.READ)){
				channel.position(offset);
				channel.read(buffer);
			}
			return buffer.array();
		}

	}

}
