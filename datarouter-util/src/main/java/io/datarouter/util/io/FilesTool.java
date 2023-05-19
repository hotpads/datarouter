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
package io.datarouter.util.io;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.FileTime;
import java.util.stream.Stream;

/**
 * Use UncheckedIOException for Files methods
 */
public class FilesTool{

	public static FileTime getLastModifiedTime(Path path, LinkOption... options){
		try{
			return Files.getLastModifiedTime(path, options);
		}catch(IOException e){
			throw new UncheckedIOException("", e);
		}
	}

	public static Path createTempDirectory(){
		try{
			return Files.createTempDirectory(null);
		}catch(IOException e){
			throw new UncheckedIOException("", e);
		}
	}

	public static Path createDirectories(Path dir, FileAttribute<?>... attrs){
		try{
			return Files.createDirectories(dir, attrs);
		}catch(IOException e){
			throw new UncheckedIOException("", e);
		}
	}

	public static void delete(Path path){
		try{
			Files.delete(path);
		}catch(IOException e){
			throw new UncheckedIOException("", e);
		}
	}

	public static byte[] readAllBytes(Path path){
		try{
			return Files.readAllBytes(path);
		}catch(IOException e){
			throw new UncheckedIOException("", e);
		}
	}

	public static Stream<Path> walk(Path path, FileVisitOption... options){
		try{
			return Files.walk(path, options);
		}catch(IOException e){
			throw new UncheckedIOException("", e);
		}
	}

	public static Path write(Path path, Iterable<String> lines, OpenOption... options){
		try{
			return Files.write(path, lines, options);
		}catch(IOException e){
			throw new UncheckedIOException("", e);
		}
	}

}
