package com.hotpads.datarouter.util;

import junit.framework.Assert;

import org.junit.Test;

import com.hotpads.profile.count.collection.Counters;
import com.hotpads.util.core.StringTool;

public class ImportCounters {
	private static final String PREFIX_IMPORT_ALL = "ImportAll";
	private static final String PREFIX_IMPORT_FEED = "ImportFeed";
	private static final String PREFIX_IMPORT_LISTING_TYPE = "ImportListingType";
	private static final String PREFIX_REALTIME_IMPORT_ALL = "RealTimeImportAll";
	private static final String PREFIX_REALTIME_IMPORT_FEED = "RealTimeImportFeed";
	
	private static final String ACTION_INSERT = "insert";
	private static final String ACTION_UPDATE = "update";
	private static final String ACTION_DEACTIVATE = "deactivate";
	private static final String ACTION_UNMODIFIED = "unmodified";
	private static final String ACTION_OLDER = "olderOrSame";
	private static final String ACTION_MARK_FOR_UPDATE = "markForUpdate";
	private static final String ACTION_RECEIVE_FOR_UPDATE = "receiveForUpdate";
	
	private static final String ACTION_TYPE_HASH = "(hash)";
	private static final String ACTION_TYPE_COMPARE = "(compare)";
	private static final String ACTION_TYPE_PROVIDER_VERSION = "(providerVersion)";
			
	public static void incSuffixInsertListing(String importerName, String feedId, String listingType) {
		incSuffixAction(importerName, feedId, listingType, ACTION_INSERT);
	}
	
	public static void incSuffixUpdateListing(String importerName, String feedId, String listingType) {
		incSuffixAction(importerName, feedId, listingType, ACTION_UPDATE);
	}
	
	public static void incSuffixDeactivateListing(String importerName, String feedId, String listingType) {
		incSuffixAction(importerName, feedId, listingType, ACTION_DEACTIVATE);
	}
	
	public static void incSuffixUnmodifiedCompareListing(String importerName, String feedId, String listingType) {
		incSuffixAction(importerName, feedId, listingType, ACTION_UNMODIFIED, ACTION_TYPE_COMPARE);
	}
	
	public static void incSuffixUnmodifiedHashListing(String importerName, String feedId, String listingType) {
		incSuffixAction(importerName, feedId, listingType, ACTION_UNMODIFIED, ACTION_TYPE_HASH);
	}
	
	public static void incSuffixOlderProviderVersionListing(String importerName, String feedId, String listingType) {
		incSuffixAction(importerName, feedId, listingType, ACTION_OLDER, ACTION_TYPE_PROVIDER_VERSION);
	}
	
	public static void incSuffixRealTimeFeedMarkForUpdate(String feedId, long delta){
		incSuffixRealTimeAction(feedId, ACTION_MARK_FOR_UPDATE, delta);
	}
	
	public static void incSuffixRealTimeFeedReceivedForUpdate(String feedId, long delta){
		incSuffixRealTimeAction(feedId, ACTION_RECEIVE_FOR_UPDATE, delta);
	}

	/********* private ***********/
	
	private static void incSuffixAction(String importerName, String feedId, String listingType, String action) {
		incInternal(getKeyForImportAllAction(importerName, action), 1L);
		incInternal(getKeyForImportFeedAction(importerName, feedId, action), 1L);
		if (StringTool.notEmpty(listingType)) {
			incInternal(getKeyForImportListingType(importerName, listingType, action), 1L);
		}
	}
	
	private static void incSuffixAction(String importerName, String feedId, String listingType, String action,
			String actionType) {
		incInternal(getKeyForImportAllAction(importerName, action, actionType), 1L);
		incInternal(getKeyForImportFeedAction(importerName, feedId, action, actionType), 1L);
		if (StringTool.notEmpty(listingType)) {
			incInternal(getKeyForImportListingType(importerName, listingType, action, actionType), 1L);
		}
	}
	
	private static void incSuffixRealTimeAction(String feedId, String action, long delta){
		incInternal(PREFIX_REALTIME_IMPORT_ALL + " " + action + " Listing", delta);
		incInternal(PREFIX_REALTIME_IMPORT_FEED + " " + feedId + " " + action + " Listing", delta);
	}
	
	private static String getKeyForImportAllAction(String importerName, String action) {
		return importerName + " " + PREFIX_IMPORT_ALL + " " + action + " " + "Listing";
	}

	private static String getKeyForImportAllAction(String importerName, String action, String actionType) {
		return getKeyForImportAllAction(importerName, action) + " " + actionType;
	}

	private static String getKeyForImportFeedAction(String importerName, String feedId, String action) {
		return importerName + " " + PREFIX_IMPORT_FEED + " " + feedId + " " + action + " " + "Listing";
	}

	private static String getKeyForImportFeedAction(String importerName, String feedId, String action, 
			String actionType) {
		return getKeyForImportFeedAction(importerName, feedId, action) + " " + actionType;
	}

	private static String getKeyForImportListingType(String importerName, String listingType, String action) {
		return importerName + " " + PREFIX_IMPORT_LISTING_TYPE + " " + listingType + " " + action + " " + "Listing";
	}

	private static String getKeyForImportListingType(String importerName, String listingType, String action,
			String actionType) {
		return getKeyForImportListingType(importerName, listingType, action) + " " + actionType;
	}

	private static String getKeyForRealTimeImportAllAction(String action) {
		return PREFIX_REALTIME_IMPORT_ALL + " " + action + " " + "Listing";
	}
	
	private static String getKeyForRealTimeImportFeedAction(String action) {
		return PREFIX_REALTIME_IMPORT_FEED + " " + action + " " + "Listing";
	}
	
	private static Long incInternal(String key, long delta) {
		return Counters.inc(key, delta);
	}
	
	/** tests *****************************************************************************/
	public static class ImportCountersTests{
		@Test	public void testGetKeyForImportAllAction() {
			String importerName = "ListingImporter";
			Assert.assertEquals("ListingImporter ImportAll insert Listing",
					getKeyForImportAllAction(importerName, ACTION_INSERT));
			Assert.assertEquals("ListingImporter ImportAll update Listing",
					getKeyForImportAllAction(importerName, ACTION_UPDATE));
			Assert.assertEquals("ListingImporter ImportAll deactivate Listing",
					getKeyForImportAllAction(importerName, ACTION_DEACTIVATE));
			Assert.assertEquals("ListingImporter ImportAll unmodified Listing (hash)",
					getKeyForImportAllAction(importerName, ACTION_UNMODIFIED, ACTION_TYPE_HASH));
			Assert.assertEquals("ListingImporter ImportAll unmodified Listing (compare)",
					getKeyForImportAllAction(importerName, ACTION_UNMODIFIED, ACTION_TYPE_COMPARE));
			Assert.assertEquals("ListingImporter ImportAll olderOrSame Listing (providerVersion)",
					getKeyForImportAllAction(importerName, ACTION_OLDER, ACTION_TYPE_PROVIDER_VERSION));
		}
		
		@Test	public void testGetKeyForImportFeedAction() {
			String feedId = "FeedId";
			String importerName = "ListingImporter";
			Assert.assertEquals("ListingImporter ImportFeed FeedId insert Listing",
					getKeyForImportFeedAction(importerName, feedId, ACTION_INSERT));
			Assert.assertEquals("ListingImporter ImportFeed FeedId update Listing",
					getKeyForImportFeedAction(importerName, feedId, ACTION_UPDATE));
			Assert.assertEquals("ListingImporter ImportFeed FeedId deactivate Listing",
					getKeyForImportFeedAction(importerName, feedId, ACTION_DEACTIVATE));
			Assert.assertEquals("ListingImporter ImportFeed FeedId unmodified Listing (hash)",
					getKeyForImportFeedAction(importerName, feedId, ACTION_UNMODIFIED, ACTION_TYPE_HASH));
			Assert.assertEquals("ListingImporter ImportFeed FeedId unmodified Listing (compare)",
					getKeyForImportFeedAction(importerName, feedId, ACTION_UNMODIFIED, ACTION_TYPE_COMPARE));
			Assert.assertEquals("ListingImporter ImportFeed FeedId olderOrSame Listing (providerVersion)",
					getKeyForImportFeedAction(importerName, feedId, ACTION_OLDER, ACTION_TYPE_PROVIDER_VERSION));
		}
		
		@Test	public void testGetKeyForImportListingType() {
			String listingType = "rental";
			String importerName = "ListingImporter";
			Assert.assertEquals("ListingImporter ImportListingType rental insert Listing",
					getKeyForImportListingType(importerName, listingType, ACTION_INSERT));
			Assert.assertEquals("ListingImporter ImportListingType rental update Listing",
					getKeyForImportListingType(importerName, listingType, ACTION_UPDATE));
			Assert.assertEquals("ListingImporter ImportListingType rental deactivate Listing",
					getKeyForImportListingType(importerName, listingType, ACTION_DEACTIVATE));
			Assert.assertEquals("ListingImporter ImportListingType rental unmodified Listing (hash)",
					getKeyForImportListingType(importerName, listingType, ACTION_UNMODIFIED, ACTION_TYPE_HASH));
			Assert.assertEquals("ListingImporter ImportListingType rental unmodified Listing (compare)",
					getKeyForImportListingType(importerName, listingType, ACTION_UNMODIFIED, ACTION_TYPE_COMPARE));
			Assert.assertEquals("ListingImporter ImportListingType rental olderOrSame Listing (providerVersion)",
					getKeyForImportListingType(importerName, listingType, ACTION_OLDER, ACTION_TYPE_PROVIDER_VERSION));
		}
		
		@Test	public void testGetKeyForRealTimeImportAllAction() {
			Assert.assertEquals("RealTimeImportAll markForUpdate Listing",
					getKeyForRealTimeImportAllAction(ACTION_MARK_FOR_UPDATE));
			Assert.assertEquals("RealTimeImportAll receiveForUpdate Listing",
					getKeyForRealTimeImportAllAction(ACTION_RECEIVE_FOR_UPDATE));
		}
		
		@Test	public void testGetKeyForRealTimeImportFeedAction() {
			Assert.assertEquals("RealTimeImportFeed markForUpdate Listing",
					getKeyForRealTimeImportFeedAction(ACTION_MARK_FOR_UPDATE));
			Assert.assertEquals("RealTimeImportFeed receiveForUpdate Listing",
					getKeyForRealTimeImportFeedAction(ACTION_RECEIVE_FOR_UPDATE));
		}
	}
}
