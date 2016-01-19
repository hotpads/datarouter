package com.hotpads.datarouter.util;

import org.junit.Assert;
import org.junit.Test;

import com.hotpads.datarouter.profile.counter.Counters;
import com.hotpads.datarouter.util.core.DrStringTool;

public class ImportCounters {
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

	public static void incSuffixInsertListing(String importerName, String feedId, String listingType) {
		incSuffixAction(importerName, feedId, listingType, ACTION_insert);
	}

	public static void incSuffixUpdateListing(String importerName, String feedId, String listingType) {
		incSuffixAction(importerName, feedId, listingType, ACTION_update);
	}

	public static void incSuffixDeactivateListing(String importerName, String feedId, String listingType) {
		incSuffixAction(importerName, feedId, listingType, ACTION_deactivate);
	}

	public static void incSuffixUnmodifiedCompareListing(String importerName, String feedId, String listingType) {
		incSuffixAction(importerName, feedId, listingType, ACTION_unmodified, ACTION_TYPE_compare);
	}

	public static void incSuffixUnmodifiedHashListing(String importerName, String feedId, String listingType) {
		incSuffixAction(importerName, feedId, listingType, ACTION_unmodified, ACTION_TYPE_hash);
	}

	public static void incSuffixOlderProviderVersionListing(String importerName, String feedId, String listingType) {
		incSuffixAction(importerName, feedId, listingType, ACTION_olderOrSame, ACTION_TYPE_providerVersion);
	}

	public static void incSuffixRealTimeFeedMarkForUpdate(String feedId, String feedGroupName, long delta){
		incSuffixRealTimeAction(feedId, feedGroupName, ACTION_markForUpdate, delta);
	}

	public static void incSuffixRealTimeFeedReceivedForUpdate(String feedId, String feedGroupName, long delta){
		incSuffixRealTimeAction(feedId, feedGroupName, ACTION_receiveForUpdate, delta);
	}

	/********* private ***********/

	private static void incSuffixAction(String importerName, String feedId, String listingType, String action) {
		incInternal(getKeyForImportAllAction(importerName, action), 1L);
		incInternal(getKeyForImportFeedAction(importerName, feedId, action), 1L);
		if (DrStringTool.notEmpty(listingType)) {
			incInternal(getKeyForImportListingType(importerName, listingType, action), 1L);
		}
	}

	private static void incSuffixAction(String importerName, String feedId, String listingType, String action,
			String actionType) {
		incInternal(getKeyForImportAllAction(importerName, action, actionType), 1L);
		incInternal(getKeyForImportFeedAction(importerName, feedId, action, actionType), 1L);
		if (DrStringTool.notEmpty(listingType)) {
			incInternal(getKeyForImportListingType(importerName, listingType, action, actionType), 1L);
		}
	}

	private static void incSuffixRealTimeAction(String feedId, String feedGroup, String action, long delta){
		incInternal(PREFIX_RealTimeImport + " " + action + " Listing", delta);
		incInternal(PREFIX_RealTimeImportFeed + " " + feedId + " " + action + " Listing", delta);
		if(feedGroup != null && !feedGroup.isEmpty()) {
			incInternal(PREFIX_RealTimeImportFeedGroup + " " + feedGroup + " " + action + " Listing", delta);
		}
	}

	private static String getKeyForImportAllAction(String importerName, String action) {
		return importerName + " " + PREFIX_Import + " " + action + " " + "Listing";
	}

	private static String getKeyForImportAllAction(String importerName, String action, String actionType) {
		return getKeyForImportAllAction(importerName, action) + " " + actionType;
	}

	private static String getKeyForImportFeedAction(String importerName, String feedId, String action) {
		return importerName + " " + PREFIX_ImportFeed + " " + feedId + " " + action + " " + "Listing";
	}

	private static String getKeyForImportFeedAction(String importerName, String feedId, String action,
			String actionType) {
		return getKeyForImportFeedAction(importerName, feedId, action) + " " + actionType;
	}

	private static String getKeyForImportListingType(String importerName, String listingType, String action) {
		return importerName + " " + PREFIX_ImportListingType + " " + listingType + " " + action + " " + "Listing";
	}

	private static String getKeyForImportListingType(String importerName, String listingType, String action,
			String actionType) {
		return getKeyForImportListingType(importerName, listingType, action) + " " + actionType;
	}

	private static String getKeyForRealTimeImportAllAction(String action) {
		return PREFIX_RealTimeImport + " " + action + " " + "Listing";
	}

	private static String getKeyForRealTimeImportFeedAction(String action) {
		return PREFIX_RealTimeImportFeed + " " + action + " " + "Listing";
	}

	private static void incInternal(String key, long delta) {
		Counters.inc(key, delta);
	}

	/** tests *****************************************************************************/
	public static class ImportCountersTests{
		@Test	public void testGetKeyForImportAllAction() {
			String importerName = "ListingImporter";
			Assert.assertEquals("ListingImporter Import insert Listing",
					getKeyForImportAllAction(importerName, ACTION_insert));
			Assert.assertEquals("ListingImporter Import update Listing",
					getKeyForImportAllAction(importerName, ACTION_update));
			Assert.assertEquals("ListingImporter Import deactivate Listing",
					getKeyForImportAllAction(importerName, ACTION_deactivate));
			Assert.assertEquals("ListingImporter Import unmodified Listing hash",
					getKeyForImportAllAction(importerName, ACTION_unmodified, ACTION_TYPE_hash));
			Assert.assertEquals("ListingImporter Import unmodified Listing compare",
					getKeyForImportAllAction(importerName, ACTION_unmodified, ACTION_TYPE_compare));
			Assert.assertEquals("ListingImporter Import olderOrSame Listing providerVersion",
					getKeyForImportAllAction(importerName, ACTION_olderOrSame, ACTION_TYPE_providerVersion));
		}

		@Test	public void testGetKeyForImportFeedAction() {
			String feedId = "FeedId";
			String importerName = "ListingImporter";
			Assert.assertEquals("ListingImporter ImportFeed FeedId insert Listing",
					getKeyForImportFeedAction(importerName, feedId, ACTION_insert));
			Assert.assertEquals("ListingImporter ImportFeed FeedId update Listing",
					getKeyForImportFeedAction(importerName, feedId, ACTION_update));
			Assert.assertEquals("ListingImporter ImportFeed FeedId deactivate Listing",
					getKeyForImportFeedAction(importerName, feedId, ACTION_deactivate));
			Assert.assertEquals("ListingImporter ImportFeed FeedId unmodified Listing hash",
					getKeyForImportFeedAction(importerName, feedId, ACTION_unmodified, ACTION_TYPE_hash));
			Assert.assertEquals("ListingImporter ImportFeed FeedId unmodified Listing compare",
					getKeyForImportFeedAction(importerName, feedId, ACTION_unmodified, ACTION_TYPE_compare));
			Assert.assertEquals("ListingImporter ImportFeed FeedId olderOrSame Listing providerVersion",
					getKeyForImportFeedAction(importerName, feedId, ACTION_olderOrSame, ACTION_TYPE_providerVersion));
		}

		@Test	public void testGetKeyForImportListingType() {
			String listingType = "rental";
			String importerName = "ListingImporter";
			Assert.assertEquals("ListingImporter ImportListingType rental insert Listing",
					getKeyForImportListingType(importerName, listingType, ACTION_insert));
			Assert.assertEquals("ListingImporter ImportListingType rental update Listing",
					getKeyForImportListingType(importerName, listingType, ACTION_update));
			Assert.assertEquals("ListingImporter ImportListingType rental deactivate Listing",
					getKeyForImportListingType(importerName, listingType, ACTION_deactivate));
			Assert.assertEquals("ListingImporter ImportListingType rental unmodified Listing hash",
					getKeyForImportListingType(importerName, listingType, ACTION_unmodified, ACTION_TYPE_hash));
			Assert.assertEquals("ListingImporter ImportListingType rental unmodified Listing compare",
					getKeyForImportListingType(importerName, listingType, ACTION_unmodified, ACTION_TYPE_compare));
			Assert.assertEquals("ListingImporter ImportListingType rental olderOrSame Listing providerVersion",
					getKeyForImportListingType(importerName, listingType, ACTION_olderOrSame, ACTION_TYPE_providerVersion));
		}

		@Test	public void testGetKeyForRealTimeImportAllAction() {
			Assert.assertEquals("RealTimeImport markForUpdate Listing",
					getKeyForRealTimeImportAllAction(ACTION_markForUpdate));
			Assert.assertEquals("RealTimeImport receiveForUpdate Listing",
					getKeyForRealTimeImportAllAction(ACTION_receiveForUpdate));
		}

		@Test	public void testGetKeyForRealTimeImportFeedAction() {
			Assert.assertEquals("RealTimeImportFeed markForUpdate Listing",
					getKeyForRealTimeImportFeedAction(ACTION_markForUpdate));
			Assert.assertEquals("RealTimeImportFeed receiveForUpdate Listing",
					getKeyForRealTimeImportFeedAction(ACTION_receiveForUpdate));
		}
	}
}
