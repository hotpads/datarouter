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
package io.datarouter.storage.config;

import java.util.Set;

/**
 * Defines the strategy used when writing a databean to a datastore, especially the behavior when trying to put a
 * databean which has a key that already exists.
 */
public enum PutMethod{

	/**
	 * Get the primary key first to determine whether to update or insert. Slow, and may not be thread safe.
	 */
	SELECT_FIRST_OR_LOOK_AT_PRIMARY_KEY(false),
	/**
	 * Try to update, and in case of failure, issue an insert. Good to use when rows are usually there.
	 */
	UPDATE_OR_INSERT(false),
	/**
	 * Try to insert, and in case of failure, issue an update.
	 */
	INSERT_OR_UPDATE(false),
	/**
	 * Try to insert the databean, and throw an exception if the primary key already exists.
	 */
	INSERT_OR_BUST(true),
	/**
	 * Try to update the row at this primary key, and throw an exception if the primary key does not exist.
	 */
	UPDATE_OR_BUST(true),
	MERGE(false),
	/**
	 * Try to insert the databean and ignore any error that may come up. If the key already exists, or any other
	 * error happens, silently abort the put.
	 */
	INSERT_IGNORE(false),
	/**
	 * Try to insert, or update all the non-key fields if the key already exists. Performs the put as a single operation
	 * if the datastore supports it. This is the default.
	 */
	INSERT_ON_DUPLICATE_UPDATE(false),
	/**
	 * Try to update the databean and ignore any error that may come up, like if the primary key does not exist.
	 */
	UPDATE_IGNORE(false);

	//need to flush immediately so we can catch insert/update exceptions if they are thrown,
	//   otherwise the exception will ruin the whole batch
	public static final Set<PutMethod> METHODS_TO_FLUSH_IMMEDIATELY = Set.of(UPDATE_OR_INSERT, INSERT_OR_UPDATE);

	public static final PutMethod DEFAULT_PUT_METHOD = PutMethod.INSERT_ON_DUPLICATE_UPDATE;

	private boolean shouldAutoCommit;

	private PutMethod(boolean shouldAutoCommit){
		this.shouldAutoCommit = shouldAutoCommit;
	}

	public boolean getShouldAutoCommit(){
		return shouldAutoCommit;
	}

}
