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
package io.datarouter.plugin.dataexport.test;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;

import javax.inject.Inject;

import org.testng.Assert;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import io.datarouter.bytes.InputStreamTool;
import io.datarouter.client.memory.test.DatarouterMemoryTestClientIds;
import io.datarouter.plugin.dataexport.config.DatarouterDataExportExecutors.DatabeanExportPrefetchExecutor;
import io.datarouter.plugin.dataexport.service.DatabeanExport;
import io.datarouter.plugin.dataexport.service.DatabeanImportService;
import io.datarouter.plugin.dataexport.test.storage.BackupBean;
import io.datarouter.plugin.dataexport.test.storage.BackupBeanDao;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.Datarouter;
import io.datarouter.storage.node.factory.NodeFactory;
import io.datarouter.storage.test.node.basic.sorted.SortedBeans;
import io.datarouter.util.tuple.Range;

@Guice(moduleFactory = DatarouterDataExportTestNgModuleFactory.class)
@Test(singleThreaded = true)
public class MemoryBackupIntegrationTests{

	private static final SortedSet<String> STRINGS = SortedBeans.STRINGS;
	private static final int NUM_ELEMENTS = SortedBeans.NUM_ELEMENTS;
	private static final List<Integer> INTEGERS = SortedBeans.INTEGERS;
	private static final int TOTAL_RECORDS = SortedBeans.TOTAL_RECORDS;
	private static final String EMPTY_STRING = "";

	private final DatabeanExportPrefetchExecutor databeanExportPrefetchExec;
	private final DatabeanImportService regionRestoreService;
	private final BackupBeanDao dao;

	@Inject
	public MemoryBackupIntegrationTests(
			Datarouter datarouter,
			NodeFactory nodeFactory,
			DatabeanExportPrefetchExecutor databeanExportPrefetchExec,
			DatabeanImportService regionRestoreService){
		this.regionRestoreService = regionRestoreService;
		this.databeanExportPrefetchExec = databeanExportPrefetchExec;
		dao = new BackupBeanDao(
				datarouter,
				nodeFactory,
				DatarouterMemoryTestClientIds.MEMORY);
	}

	private void resetTable(){
		clearTable();

		List<String> as = Scanner.of(STRINGS).shuffle().list();
		List<String> bs = Scanner.of(STRINGS).shuffle().list();
		List<Integer> cs = Scanner.of(INTEGERS).shuffle().list();
		List<String> ds = Scanner.of(STRINGS).shuffle().list();

		List<BackupBean> toSave = new ArrayList<>();
		for(int a = 0; a < NUM_ELEMENTS; ++a){
			for(int b = 0; b < NUM_ELEMENTS; ++b){
				for(int c = 0; c < NUM_ELEMENTS; ++c){
					for(int d = 0; d < NUM_ELEMENTS; ++d){
						BackupBean bean = new BackupBean(
								as.get(a),
								bs.get(b),
								cs.get(c),
								ds.get(d),
								EMPTY_STRING,
								null,
								null,
								null);
						toSave.add(bean);
					}
				}
			}
		}
		dao.putMultiOrBust(toSave);
		Assert.assertEquals(TOTAL_RECORDS, dao.scan().count());
	}

	private void clearTable(){
		dao.deleteAll();
		Assert.assertEquals(dao.scan().count(), 0);
	}

	private void checkImportedData(){
		List<BackupBean> databeans = dao.scan().list();
		Assert.assertEquals(databeans.size(), TOTAL_RECORDS);
		databeans.forEach(databean -> Assert.assertEquals(databean.getF1(), ""));
		databeans.forEach(databean -> Assert.assertEquals(databean.getF3(), null));
	}

	@Test
	public void testRoundTripMemory(){
		resetTable();
		var backup = new DatabeanExport<>(
				"myExportId",
				dao.getNode(),
				DatabeanExport.DATABEAN_CONFIG,
				Range.everything(),
				null,
				Long.MAX_VALUE,
				databeanExportPrefetchExec);
		byte[] result = InputStreamTool.toArray(backup.makeGzipInputStream());

		// clear table
		clearTable();

		// now read back in
		regionRestoreService.importFromMemory(dao.getNode(), result);
		checkImportedData();
	}

}
