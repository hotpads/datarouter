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
package io.datarouter.storage.util;

import java.util.Collection;
import java.util.List;

import io.datarouter.scanner.Scanner;

/**
 * Represents an extension of a Path.  Always uses slashes to simplify application development.
 *
 * Empty segments are accepted but not encouraged which allows parsing of things like S3 keys that might have double
 * slashes,
 *
 * Strange characters are allowed by not encouraged which allows parsing paths that you don't control,
 *
 * Immutable: appending returns a new SubpathPath with a new array of segments.
 *
 * When rendered:
 * - starts without slash
 * - empty segments results in empty string
 * - each segment is suffixed with a slash: a/b/c/
 *
 * A rendered copy of the toString value is calculated on creation because some root directories can be read a lot.
 */
public class Subpath{

	private static final String SLASH = "/";
	private static final Subpath EMPTY = new Subpath();

	private final String[] segments;
	private final String rendered;

	public Subpath(String... segments){
		Scanner.of(segments).forEach(Subpath::validateSegment);
		this.segments = segments;
		this.rendered = render();
	}

	public Subpath(List<String> segments){
		this(segments.toArray(String[]::new));
	}

	public Subpath append(String segment){
		validateSegment(segment);
		return Scanner.of(segments)
				.append(segment)
				.listTo(Subpath::new);
	}

	public Subpath append(Subpath suffix){
		return Scanner.of(segments)
				.append(suffix.segments)
				.listTo(Subpath::new);
	}

	public static Subpath join(Collection<Subpath> paths){
		return Scanner.of(paths)
				.map(path -> path.segments)
				.concat(Scanner::of)
				.listTo(Subpath::new);
	}

	public static Subpath empty(){
		return EMPTY;
	}

	/**
	 * Needs to return the exact format described in the class comments.  Tested in SubpathTests
	 */
	@Override
	public String toString(){
		return rendered;
	}

	private static void validateSegment(String segment){
		if(segment == null){
			throw new IllegalArgumentException("segment cannot be null");
		}
		if(segment.contains(SLASH)){
			throw new IllegalArgumentException("segment cannot contain slash");
		}
	}

	private String render(){
		if(segments.length == 0){
			return "";
		}
		return String.join(SLASH, segments) + SLASH;
	}

}
