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
package io.datarouter.gcp.gcs.node;

import com.google.api.gax.paging.Page;

import io.datarouter.scanner.BaseScanner;

public class GcsPageScanner<T> extends BaseScanner<Page<T>>{

	private Page<T> firstPage;

	public GcsPageScanner(Page<T> firstPage){
		this.firstPage = firstPage;
	}

	@Override
	public boolean advance(){
		if(firstPage != null){
			current = firstPage;
			firstPage = null;
			return notEmpty(current);
		}
		if(current.hasNextPage()){
			current = current.getNextPage();
			return notEmpty(current);
		}
		return false;
	}

	private static boolean notEmpty(Page<?> page){
		return page.getValues().iterator().hasNext();
	}

}
