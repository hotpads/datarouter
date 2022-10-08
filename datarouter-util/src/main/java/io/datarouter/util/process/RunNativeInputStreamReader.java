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
package io.datarouter.util.process;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import io.datarouter.scanner.BaseLinkedScanner;
import io.datarouter.scanner.Scanner;

public class RunNativeInputStreamReader{

	private final InputStreamReader input;

	public RunNativeInputStreamReader(InputStream stream){
		this.input = new InputStreamReader(stream);
	}

	public Scanner<String> linesWithReplacement(){
		return lines()
				.link(TerminalLineReplacingScanner::new);
	}

	public Scanner<TerminalLine> lines(){
		Iterator<TerminalLine> iterator = new Iterator<>(){

			StringBuilder sb = new StringBuilder();
			StringBuilder next = new StringBuilder();

			@Override
			public boolean hasNext(){
				try{
					int chr;
					while((chr = input.read()) != -1){
						if(chr == '\n'){
							return true;
						}
						sb.append((char)chr);
						if(chr == '\r'){
							if((chr = input.read()) != -1){
								if(chr == '\n'){
									sb.deleteCharAt(sb.length() - 1);
								}else{
									next.append((char)chr);
								}
							}
							return true;
						}
					}
					if(sb.length() > 0){
						return true;
					}
					return false;
				}catch(IOException e){
					throw new UncheckedIOException(e);
				}
			}

			@Override
			public TerminalLine next(){
				String res = sb.toString();
				sb = next;
				next = new StringBuilder();
				return new TerminalLine(res.endsWith("\r"), res);
			}

		};
		Spliterator<TerminalLine> spliterator = Spliterators.spliteratorUnknownSize(iterator,
				Spliterator.ORDERED | Spliterator.NONNULL);
		Stream<TerminalLine> stream = StreamSupport.stream(spliterator, false);
		return Scanner.of(stream);
	}

	private static class TerminalLineReplacingScanner extends BaseLinkedScanner<TerminalLine,String>{

		private boolean done = false;
		private TerminalLine prev;

		public TerminalLineReplacingScanner(Scanner<TerminalLine> input){
			super(input);
		}

		@Override
		protected boolean advanceInternal(){
			if(done){
				return false;
			}
			if(prev == null){
				if(!input.advance()){
					return false;
				}
				prev = input.current();
			}
			if(!input.advance()){
				current = prev.line;
				prev = null;
				done = true;
				return true;
			}
			TerminalLine curr = input.current();
			while(prev.replace){
				prev = curr;
				if(input.advance()){
					curr = input.current();
				}else{
					done = true;
					break;
				}
			}
			current = prev.line;
			prev = curr;
			return true;
		}

	}

	record TerminalLine(boolean replace, String line){
	}

}
