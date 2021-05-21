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
package io.datarouter.filesystem.snapshot;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.scanner.Scanner;
import io.datarouter.util.io.FileTool;
import io.datarouter.util.io.ReaderTool;
import io.datarouter.util.number.NumberFormatter;

public class WordDataset{
	private static final Logger logger = LoggerFactory.getLogger(WordDataset.class);

	//originally: https://raw.githubusercontent.com/dwyl/english-words/master/words_alpha.txt
	private static final String REMOTE_LOCATION = "http://files.hotpads.com/datarouter/test/words_alpha.txt";
	private static final String LOCAL_DIRECTORY = "/tmp/snapshot/words/";

	public static Scanner<String> scanWords(String filename){
		String localLocation = LOCAL_DIRECTORY + filename;
		FileTool.cacheRemoteFile(REMOTE_LOCATION, localLocation);
		return ReaderTool.scanFileLines(localLocation)
				.sort();
	}

	public static void main(String[] args){
		List<String> words = scanWords(WordDataset.class.getSimpleName()).list();
		logger.warn("loaded {} words", NumberFormatter.addCommas(words.size()));
	}

}
