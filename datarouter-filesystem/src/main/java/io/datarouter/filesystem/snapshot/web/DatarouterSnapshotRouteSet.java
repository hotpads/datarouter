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
package io.datarouter.filesystem.snapshot.web;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.storage.tag.Tag;
import io.datarouter.web.dispatcher.BaseRouteSet;
import io.datarouter.web.dispatcher.DispatchRule;
import io.datarouter.web.user.role.DatarouterUserRole;

@Singleton
public class DatarouterSnapshotRouteSet extends BaseRouteSet{

	@Inject
	public DatarouterSnapshotRouteSet(DatarouterSnapshotPaths paths){
		super(paths.datarouter);
		handle(paths.datarouter.snapshot.benchmark).withHandler(DatarouterSnapshotBenchmarkHandler.class);
		handle(paths.datarouter.snapshot.group.listGroups).withHandler(DatarouterSnapshotGroupsHandler.class);
		handle(paths.datarouter.snapshot.group.listSnapshots).withHandler(DatarouterSnapshotGroupHandler.class);
		handle(paths.datarouter.snapshot.individual.summary).withHandler(DatarouterSnapshotHandler.class);
		handle(paths.datarouter.snapshot.individual.entries).withHandler(DatarouterSnapshotEntriesHandler.class);
		handle(paths.datarouter.snapshot.individual.entry).withHandler(DatarouterSnapshotEntryHandler.class);
	}

	@Override
	protected DispatchRule applyDefault(DispatchRule rule){
		return rule
				.allowRoles(DatarouterUserRole.DATAROUTER_ADMIN)
				.withTag(Tag.DATAROUTER);
	}

}
