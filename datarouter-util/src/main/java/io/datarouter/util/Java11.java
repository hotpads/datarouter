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
package io.datarouter.util;

import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.Optional;

public class Java11{

	public static boolean isBlank(String string){
		// return string.isBlank();
		return string.trim().isEmpty();
	}

	public static <T> boolean isEmpty(Optional<T> optional){
		// return optional.isEmpty();
		return !optional.isPresent();
	}

	public static Path pathOf(String localPath){
		// return Path.of(localPath);
		return FileSystems.getDefault().getPath(localPath);
	}

}
