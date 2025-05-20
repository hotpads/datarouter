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
package io.datarouter.bytes.blockfile.io.storage.impl;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.stream.Stream;

import io.datarouter.bytes.ByteLength;
import io.datarouter.bytes.blockfile.io.storage.BlockfileLocation;
import io.datarouter.bytes.blockfile.io.storage.BlockfileNameAndSize;
import io.datarouter.bytes.blockfile.io.storage.BlockfileStorage;
import io.datarouter.scanner.Scanner;
import io.datarouter.scanner.Threads;

public class BlockfileLocalStorage implements BlockfileStorage{

	private final String directoryPath;

	public BlockfileLocalStorage(String directoryPath){
		if(!directoryPath.endsWith("/")){
			String message = String.format("directoryPath=%s must end with /", directoryPath);
			throw new IllegalArgumentException(message);
		}
		this.directoryPath = directoryPath;
	}

	/*--------- read -----------*/

	@Override
	public List<BlockfileNameAndSize> list(){
		Path directory = Path.of(directoryPath);
		try(Stream<Path> paths = Files.walk(directory)){
			return Scanner.of(paths)
					.exclude(Files::isDirectory)
					.exclude(Files::isSymbolicLink)
					.include(Files::isReadable)
					.map(Path::getFileName)
					.map(Path::toString)
					.map(name -> new BlockfileNameAndSize(name, length(name)))
					.list();
		}catch(IOException e){
			throw new UncheckedIOException(e);
		}
	}

	@Override
	public long length(String name){
		Path fullPath = fullPath(name);
		try{
			return Files.size(fullPath);
		}catch(IOException e){
			throw new UncheckedIOException(e);
		}
	}

	@Override
	public byte[] read(String name){
		Path fullPath = fullPath(name);
		try{
			return Files.readAllBytes(fullPath);
		}catch(IOException e){
			throw new UncheckedIOException(e);
		}
	}

	@Override
	public byte[] readPartial(String name, BlockfileLocation location){
		Path fullPath = fullPath(name);
		ByteBuffer buffer = ByteBuffer.allocate(location.length());
		try(SeekableByteChannel channel = Files.newByteChannel(fullPath, StandardOpenOption.READ)){
			channel.position(location.from());
			channel.read(buffer);
		}catch(IOException e){
			throw new UncheckedIOException(e);
		}
		return buffer.array();
	}

	@Override
	public byte[] readEnding(String name, int length){
		Path fullPath = fullPath(name);
		try(SeekableByteChannel channel = Files.newByteChannel(fullPath, StandardOpenOption.READ)){
			long fileLength = channel.size();
			int numBytes = Math.toIntExact(Math.min(fileLength, length));
			ByteBuffer buffer = ByteBuffer.allocate(numBytes);
			channel.position(fileLength - numBytes);
			channel.read(buffer);
			return buffer.array();
		}catch(IOException e){
			throw new UncheckedIOException(e);
		}
	}

	@Override
	public InputStream readInputStream(String name, Threads threads, ByteLength chunkSize){
		// Parallel reads not implemented, but could be
		Path fullPath = fullPath(name);
		try{
			return new BufferedInputStream(Files.newInputStream(fullPath));
		}catch(IOException e){
			throw new UncheckedIOException(e);
		}
	}

	@Override
	public Scanner<FilenameAndInputStream> readInputStreams(
			Scanner<String> filenames,
			Threads threads,
			ByteLength chunkSize,
			ByteLength bufferSize,
			ExecutorService prefetchExec){
		return filenames
				.map(filename -> {
					InputStream inputStream = readInputStream(filename, threads, chunkSize);
					return new FilenameAndInputStream(filename, inputStream);
				});
	}

	/*--------- write -----------*/

	@Override
	public void write(String name, byte[] bytes){
		Path fullPath = fullPath(name);
		try{
			Files.write(fullPath, bytes);
		}catch(IOException e){
			throw new UncheckedIOException(e);
		}
	}

	@Override
	public void write(String name, InputStream inputStream, Threads threads, ByteLength minWritePartSize){
		Path fullPath = fullPath(name);
		fullPath.getParent().toFile().mkdirs();
		try(InputStream _ = inputStream;
				OutputStream outputStream = Files.newOutputStream(fullPath)){
			fullPath.toFile().createNewFile();
			inputStream.transferTo(outputStream);
		}catch(IOException e){
			throw new UncheckedIOException(e);
		}
	}

	/*--------- delete -----------*/

	@Override
	public void deleteMulti(List<String> names){
		Scanner.of(names)
				.map(this::fullPath)
				.forEach(fullPath -> {
					try{
						Files.delete(fullPath);
					}catch(IOException e){
						throw new UncheckedIOException(e);
					}
				});
	}

	/*-------- path ----------*/

	private Path fullPath(String name){
		return Path.of(directoryPath + name);
	}

}
