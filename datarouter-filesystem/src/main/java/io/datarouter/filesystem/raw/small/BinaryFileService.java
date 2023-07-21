/*
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

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Optional;
import java.util.concurrent.ExecutorService;

import io.datarouter.bytes.split.ChunkScannerTool;
import io.datarouter.scanner.Scanner;
import io.datarouter.scanner.Threads;
import io.datarouter.util.tuple.Range;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class BinaryFileService{

	@Inject
	private CheckedBinaryFileService checkedService;

	public void writeBytes(Path fullPath, byte[] bytes){
		try{
			checkedService.writeBytes(fullPath, bytes);
		}catch(IOException e){
			throw new UncheckedIOException(e);
		}
	}

	public void writeBytes(Path fullPath, InputStream inputStream){
		try{
			checkedService.writeBytes(fullPath, inputStream);
		}catch(IOException e){
			throw new UncheckedIOException(e);
		}
	}

	public Optional<Long> length(Path fullPath){
		try{
			return checkedService.length(fullPath);
		}catch(IOException e){
			throw new UncheckedIOException(e);
		}
	}

	public byte[] readBytes(Path fullPath){
		try{
			return checkedService.readBytes(fullPath);
		}catch(IOException e){
			throw new UncheckedIOException(e);
		}
	}

	public byte[] readBytes(Path fullPath, long offset, int length){
		try{
			return checkedService.readBytes(fullPath, offset, length);
		}catch(IOException e){
			throw new UncheckedIOException(e);
		}
	}

	public InputStream readInputStream(Path fullPath){
		try{
			return checkedService.readInputStream(fullPath);
		}catch(IOException e){
			throw new UncheckedIOException(e);
		}
	}

	public Scanner<byte[]> scanChunks(
			Path fullPath,
			Range<Long> range,
			ExecutorService exec,
			int numThreads,
			int chunkSize){
		long fromInclusive = range.hasStart() ? range.getStart() : 0;
		long toExclusive = range.hasEnd()
				? range.getEnd()
				: length(fullPath).orElseThrow();// extra operation
		return ChunkScannerTool.scanChunks(fromInclusive, toExclusive, chunkSize)
				.parallelOrdered(new Threads(exec, numThreads))
				.map(chunkRange -> readBytes(fullPath, chunkRange.start, chunkRange.length));
	}

	@Singleton
	public static class CheckedBinaryFileService{

		public void writeBytes(Path fullPath, byte[] contents)
		throws IOException{
			fullPath.getParent().toFile().mkdirs();
			Files.write(fullPath, contents);
		}

		public void writeBytes(Path fullPath, InputStream inputStream)
		throws IOException{
			fullPath.getParent().toFile().mkdirs();
			fullPath.toFile().createNewFile();
			try(OutputStream outputStream = Files.newOutputStream(fullPath)){
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

		public InputStream readInputStream(Path fullPath)
		throws IOException{
			// BufferedInputStream appears important for performance here.
			// If removing for some reason, be sure to check that callers buffer.
			return new BufferedInputStream(Files.newInputStream(fullPath));
		}

	}

}
