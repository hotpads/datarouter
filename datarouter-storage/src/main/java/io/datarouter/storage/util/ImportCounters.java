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

import org.testng.Assert;
import org.testng.annotations.Test;

import io.datarouter.storage.profile.counter.Counters;
import io.datarouter.util.string.StringTool;

public class ImportCounters{
	private static final String PREFIX_Import = "Import";
	private static final String PREFIX_ImportFeed = "ImportFeed";
	private static final String PREFIX_ImportListingType = "ImportListingType";
	private static final String PREFIX_RealTimeImport = "RealTimeImport";
	private static final String PREFIX_RealTimeImportFeed = "RealTimeImportFeed";
	private static final String PREFIX_RealTimeImportFeedGroup = "RealTimeImportFeedGroup";

	private static final String ACTION_insert = "insert";
	private static final String ACTION_update = "update";
	private static final String ACTION_deactivate = "deactivate";
	private static final String ACTION_unmodified = "unmodified";
	private static final String ACTION_olderOrSame = "olderOrSame";
	private static final String ACTION_markForUpdate = "markForUpdate";
	private static final String ACTION_receiveForUpdate = "receiveForUpdate";

	private static final String ACTION_TYPE_hash = "hash";
	private static final String ACTION_TYPE_compare = "compare";
	private static final String ACTION_TYPE_providerVersion = "providerVersion";

	public static void incSuffixInsertListing(String importerName, String feedId, String listingType){
		incSuffixAction(importerName, feedId, listingType, ACTION_insert);
	}

	public static void incSuffixUpdateListing(String importerName, String feedId, String listingType){
		incSuffixAction(importerName, feedId, listingType, ACTION_update);
	}

	public static void incSuffixDeactivateListing(String importerName, String feedId, String listingType){
		incSuffixAction(importerName, feedId, listingType, ACTION_deactivate);
	}

	public static void incSuffixUnmodifiedCompareListing(String importerName, String feedId, String listingType){
		incSuffixAction(importerName, feedId, listingType, ACTION_unmodified, ACTION_TYPE_compare);
	}

	public static void incSuffixUnmodifiedHashListing(String importerName, String feedId, String listingType){
		incSuffixAction(importerName, feedId, listingType, ACTION_unmodified, ACTION_TYPE_hash);
	}

	public static void incSuffixOlderProviderVersionListing(String importerName, String feedId, String listingType){
		incSuffixAction(importerName, feedId, listingType, ACTION_olderOrSame, ACTION_TYPE_providerVersion);
	}

	public static void incSuffixRealTimeFeedMarkForUpdate(String feedId, String feedGroupName, long delta){
		incSuffixRealTimeAction(feedId, feedGroupName, ACTION_markForUpdate, delta);
	}

	public static void incSuffixRealTimeFeedReceivedForUpdate(String feedId, String feedGroupName, long delta){
		incSuffixRealTimeAction(feedId, feedGroupName, ACTION_receiveForUpdate, delta);
	}

	/********* private ***********/

	private static void incSuffixAction(String importerName, String feedId, String listingType, String action){
		incInternal(getKeyForImportAllAction(importerName, action), 1L);
		incInternal(getKeyForImportFeedAction(importerName, feedId, action), 1L);
		if(StringTool.notEmpty(listingType)){
			incInternal(getKeyForImportListingType(importerName, listingType, action), 1L);
		}
	}

	private static void incSuffixAction(String importerName, String feedId, String listingType, String action,
			String actionType){
		incInternal(getKeyForImportAllAction(importerName, action, actionType), 1L);
		incInternal(getKeyForImportFeedAction(importerName, feedId, action, actionType), 1L);
		if(StringTool.notEmpty(listingType)){
			incInternal(getKeyForImportListingType(importerName, listingType, action, actionType), 1L);
		}
	}

	private static void incSuffixRealTimeAction(String feedId, String feedGroup, String action, long delta){
		incInternal(PREFIX_RealTimeImport + " " + action + " Listing", delta);
		incInternal(PREFIX_RealTimeImportFeed + " " + feedId + " " + action + " Listing", delta);
		if(feedGroup != null && !feedGroup.isEmpty()){
			incInternal(PREFIX_RealTimeImportFeedGroup + " " + feedGroup + " " + action + " Listing", delta);
		}
	}

	private static String getKeyForImportAllAction(String importerName, String action){
		return importerName + " " + PREFIX_Import + " " + action + " " + "Listing";
	}

	private static String getKeyForImportAllAction(String importerName, String action, String actionType){
		return getKeyForImportAllAction(importerName, action) + " " + actionType;
	}

	private static String getKeyForImportFeedAction(String importerName, String feedId, String action){
		return importerName + " " + PREFIX_ImportFeed + " " + feedId + " " + action + " " + "Listing";
	}

	private static String getKeyForImportFeedAction(String importerName, String feedId, String action,
			String actionType){
		return getKeyForImportFeedAction(importerName, feedId, action) + " " + actionType;
	}

	private static String getKeyForImportListingType(String importerName, String listingType, String action){
		return importerName + " " + PREFIX_ImportListingType + " " + listingType + " " + action + " " + "Listing";
	}

	private static String getKeyForImportListingType(String importerName, String listingType, String action,
			String actionType){
		return getKeyForImportListingType(importerName, listingType, action) + " " + actionType;
	}

	private static String getKeyForRealTimeImportAllAction(String action){
		return PREFIX_RealTimeImport + " " + action + " " + "Listing";
	}

	private static String getKeyForRealTimeImportFeedAction(String action){
		return PREFIX_RealTimeImportFeed + " " + action + " " + "Listing";
	}

	private static void incInternal(String key, long delta){
		Counters.inc(key, delta);
	}

	/** tests *****************************************************************************/
	public static class ImportCountersTests{
		@Test
		public void testGetKeyForImportAllAction(){
			String importerName = "ListingImporter";
			Assert.assertEquals(getKeyForImportAllAction(importerName, ACTION_insert),
					"ListingImporter Import insert Listing");
			Assert.assertEquals(getKeyForImportAllAction(importerName, ACTION_update),
					"ListingImporter Import update Listing");
			Assert.assertEquals(getKeyForImportAllAction(importerName, ACTION_deactivate),
					"ListingImporter Import deactivate Listing");
			Assert.assertEquals(getKeyForImportAllAction(importerName, ACTION_unmodified, ACTION_TYPE_hash),
					"ListingImporter Import unmodified Listing hash");
			Assert.assertEquals(getKeyForImportAllAction(importerName, ACTION_unmodified, ACTION_TYPE_compare),
					"ListingImporter Import unmodified Listing compare");
			Assert.assertEquals(getKeyForImportAllAction(importerName, ACTION_olderOrSame, ACTION_TYPE_providerVersion),
					"ListingImporter Import olderOrSame Listing providerVersion");
		}

		@Test
		public void testGetKeyForImportFeedAction(){
			String feedId = "FeedId";
			String importerName = "ListingImporter";
			Assert.assertEquals(getKeyForImportFeedAction(importerName, feedId, ACTION_insert),
					"ListingImporter ImportFeed FeedId insert Listing");
			Assert.assertEquals(getKeyForImportFeedAction(importerName, feedId, ACTION_update),
					"ListingImporter ImportFeed FeedId update Listing");
			Assert.assertEquals(getKeyForImportFeedAction(importerName, feedId, ACTION_deactivate),
					"ListingImporter ImportFeed FeedId deactivate Listing");
			Assert.assertEquals(getKeyForImportFeedAction(importerName, feedId, ACTION_unmodified, ACTION_TYPE_hash),
					"ListingImporter ImportFeed FeedId unmodified Listing hash");
			Assert.assertEquals(getKeyForImportFeedAction(importerName, feedId, ACTION_unmodified, ACTION_TYPE_compare),
					"ListingImporter ImportFeed FeedId unmodified Listing compare");
			Assert.assertEquals(getKeyForImportFeedAction(importerName, feedId, ACTION_olderOrSame,
					ACTION_TYPE_providerVersion),
					"ListingImporter ImportFeed FeedId olderOrSame Listing providerVersion");
		}

		@Test
		public void testGetKeyForImportListingType(){
			String listingType = "rental";
			String importerName = "ListingImporter";
			Assert.assertEquals(getKeyForImportListingType(importerName, listingType, ACTION_insert),
					"ListingImporter ImportListingType rental insert Listing");
			Assert.assertEquals(getKeyForImportListingType(importerName, listingType, ACTION_update),
					"ListingImporter ImportListingType rental update Listing");
			Assert.assertEquals(getKeyForImportListingType(importerName, listingType, ACTION_deactivate),
					"ListingImporter ImportListingType rental deactivate Listing");
			Assert.assertEquals(getKeyForImportListingType(importerName, listingType, ACTION_unmodified,
					ACTION_TYPE_hash), "ListingImporter ImportListingType rental unmodified Listing hash");
			Assert.assertEquals(getKeyForImportListingType(importerName, listingType, ACTION_unmodified,
					ACTION_TYPE_compare), "ListingImporter ImportListingType rental unmodified Listing compare");
			Assert.assertEquals(getKeyForImportListingType(importerName, listingType, ACTION_olderOrSame,
					ACTION_TYPE_providerVersion),
					"ListingImporter ImportListingType rental olderOrSame Listing providerVersion");
		}

		@Test
		public void testGetKeyForRealTimeImportAllAction(){
			Assert.assertEquals(getKeyForRealTimeImportAllAction(ACTION_markForUpdate),
					"RealTimeImport markForUpdate Listing");
			Assert.assertEquals(getKeyForRealTimeImportAllAction(ACTION_receiveForUpdate),
					"RealTimeImport receiveForUpdate Listing");
		}

		@Test
		public void testGetKeyForRealTimeImportFeedAction(){
			Assert.assertEquals(getKeyForRealTimeImportFeedAction(ACTION_markForUpdate),
					"RealTimeImportFeed markForUpdate Listing");
			Assert.assertEquals(getKeyForRealTimeImportFeedAction(ACTION_receiveForUpdate),
					"RealTimeImportFeed receiveForUpdate Listing");
		}
	}
}
