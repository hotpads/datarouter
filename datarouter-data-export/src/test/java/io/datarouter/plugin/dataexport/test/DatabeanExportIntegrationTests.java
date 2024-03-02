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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import io.datarouter.client.memory.test.DatarouterMemoryTestClientIds;
import io.datarouter.plugin.dataexport.service.blockfile.DatabeanExportBlockfileStorageService;
import io.datarouter.plugin.dataexport.service.exporting.DatabeanExportService;
import io.datarouter.plugin.dataexport.service.importing.DatabeanImportService;
import io.datarouter.plugin.dataexport.test.storage.BackupBean;
import io.datarouter.plugin.dataexport.test.storage.BackupBeanDao;
import io.datarouter.plugin.dataexport.util.DatabeanExportFilenameTool;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.Datarouter;
import io.datarouter.storage.file.PathbeanKey;
import io.datarouter.storage.node.factory.NodeFactory;
import io.datarouter.storage.test.node.basic.sorted.SortedBeans;
import io.datarouter.storage.util.Subpath;
import io.datarouter.types.Ulid;
import io.datarouter.util.tuple.Range;
import jakarta.inject.Inject;

@Guice(moduleFactory = DatarouterDataExportTestNgModuleFactory.class)
@Test(singleThreaded = true)
public class DatabeanExportIntegrationTests{
	private static final Logger logger = LoggerFactory.getLogger(DatabeanExportIntegrationTests.class);

	private final DatabeanExportBlockfileStorageService kvFileStorageService;
	private final DatabeanExportService databeanExportService;
	private final DatabeanImportService databeanImportService;
	private final BackupBeanDao dao;

	@Inject
	public DatabeanExportIntegrationTests(
			Datarouter datarouter,
			NodeFactory nodeFactory,
			DatabeanExportBlockfileStorageService kvFileStorageService,
			DatabeanExportService databeanExportService,
			DatabeanImportService databeanImportService){
		this.kvFileStorageService = kvFileStorageService;
		this.databeanExportService = databeanExportService;
		this.databeanImportService = databeanImportService;
		dao = new BackupBeanDao(
				datarouter,
				nodeFactory,
				DatarouterMemoryTestClientIds.MEMORY);
	}

	@Test
	public void testRoundTrip(){
		// make a fake table
		List<BackupBean> inputDatabeans = makeDatabeans();
		dao.putMulti(inputDatabeans);
		Assert.assertEquals(dao.scan().count(), inputDatabeans.size());

		// export
		Ulid exportId = new Ulid();
		databeanExportService.exportNode(
				exportId,
				dao.getNode(),
				Range.everything(),
				Long.MAX_VALUE,
				100,
				1,
				2,
				false);

		// check output files
		kvFileStorageService.makeExportIdDirectory(exportId).scanKeys(Subpath.empty())
				.forEach(key -> logger.warn("key={}", key));
		var metaDirectory = kvFileStorageService.makeExportMetaDirectory(exportId);
		var tableDataDirectory = kvFileStorageService.makeTableDataDirectory(exportId, dao.getNode());
		List<String> metaFilenames = metaDirectory.scanKeys(Subpath.empty())
				.map(PathbeanKey::getFile)
				.list();
		Assert.assertEquals(metaFilenames.size(), 1);
		Assert.assertEquals(metaFilenames.getFirst(), DatabeanExportFilenameTool.makeClientAndTableName(dao.getNode()));
		List<String> dataFilenames = tableDataDirectory.scanKeys(Subpath.empty())
				.map(PathbeanKey::getFile)
				.list();
		Assert.assertEquals(dataFilenames.size(), 1);
		Assert.assertEquals(dataFilenames.getFirst(), DatabeanExportFilenameTool.makePartFilename(0));

		// clear table
		dao.deleteAll();
		Assert.assertEquals(dao.scan().count(), 0);

		// now read back in
		databeanImportService.importAllTables(exportId);

		// validate imported data
		List<BackupBean> outputDatabeans = dao.scan().list();
		Assert.assertEquals(outputDatabeans, inputDatabeans);
	}

	private static List<BackupBean> makeDatabeans(){
		List<String> as = Scanner.of(SortedBeans.STRINGS).shuffle().list();
		List<String> bs = Scanner.of(SortedBeans.STRINGS).shuffle().list();
		List<Integer> cs = Scanner.of(SortedBeans.INTEGERS).shuffle().list();
		List<String> ds = Scanner.of(SortedBeans.STRINGS).shuffle().list();

		List<BackupBean> databeans = new ArrayList<>();
		for(int a = 0; a < SortedBeans.NUM_ELEMENTS; ++a){
			for(int b = 0; b < SortedBeans.NUM_ELEMENTS; ++b){
				for(int c = 0; c < SortedBeans.NUM_ELEMENTS; ++c){
					for(int d = 0; d < SortedBeans.NUM_ELEMENTS; ++d){
						var bean = new BackupBean(
								as.get(a),
								bs.get(b),
								cs.get(c),
								ds.get(d),
								"",
								null,
								null,
								null);
						databeans.add(bean);
					}
				}
			}
		}
		return Scanner.of(databeans)
				.sort()
				.list();
	}

}
