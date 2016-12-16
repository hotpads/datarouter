package com.hotpads.notification.timing;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.hotpads.datarouter.serialize.fielder.BaseDatabeanFielder;
import com.hotpads.datarouter.storage.databean.BaseDatabean;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.imp.comparable.LongField;
import com.hotpads.datarouter.storage.field.imp.comparable.LongFieldKey;
import com.hotpads.util.core.Duration;

public class NotificationTimingStrategy extends BaseDatabean<NotificationTimingStrategyKey,NotificationTimingStrategy>{
	private NotificationTimingStrategyKey key;
	private Long minSendableAgeMs;
	private Long maxItems;
	private Long droppableAgeMs;
	private Long delayForChannelMs;
	private Long minDelayMs;
	private Long standardDelayMs;
	private Long maxDelayMs;

	public NotificationTimingStrategy(){
		this.key = new NotificationTimingStrategyKey();
	}

	public NotificationTimingStrategy(String name, Long minSendableAgeMs, Long maxItems, Long droppableAgeMs,
			Long delayForChannelMs, Long minDelayMs, Long standardDelayMs, Long maxDelayMs){
		this.key = new NotificationTimingStrategyKey(name);
		this.minSendableAgeMs = minSendableAgeMs;
		this.maxItems = maxItems;
		this.droppableAgeMs = droppableAgeMs;
		this.delayForChannelMs = delayForChannelMs;
		this.minDelayMs = minDelayMs;
		this.standardDelayMs = standardDelayMs;
		this.maxDelayMs = maxDelayMs;
	}

	public NotificationTimingStrategy(String name, Duration minSendableAge, Long maxItems, Duration droppableAge,
			Duration delayForChannel, Duration minDelay, Duration standardDelay, Duration maxDelay){
		this(name,
				minSendableAge.to(TimeUnit.MILLISECONDS),
				maxItems,
				droppableAge.to(TimeUnit.MILLISECONDS),
				delayForChannel.to(TimeUnit.MILLISECONDS),
				minDelay.to(TimeUnit.MILLISECONDS),
				standardDelay.to(TimeUnit.MILLISECONDS),
				maxDelay.to(TimeUnit.MILLISECONDS));
	}

	public NotificationTimingStrategy(String name, String minSendableAge, Long maxItems, String droppableAge,
			String delayForChannel, String minDelay, String standardDelay, String maxDelay){
		this(name,
				new Duration(minSendableAge),
				maxItems,
				new Duration(droppableAge),
				new Duration(delayForChannel),
				new Duration(minDelay),
				new Duration(standardDelay),
				new Duration(maxDelay));
	}

	private static String toDurationString(long value){
		return new Duration(value, TimeUnit.MILLISECONDS).toString();
	}

	@Override
	public Class<NotificationTimingStrategyKey> getKeyClass(){
		return NotificationTimingStrategyKey.class;
	}

	@Override
	public NotificationTimingStrategyKey getKey(){
		return key;
	}

	/**
	 * @return The minimum age that a request should have to be processed by the notification service
	 */
	public Long getMinSendableAgeMs(){
		return minSendableAgeMs;
	}

	public String getMinSendableAge(){
		return toDurationString(minSendableAgeMs);
	}

	public void setMinSendableAgeMs(Long minSendableAgeMs){
		this.minSendableAgeMs = minSendableAgeMs;
	}

	/**
	 * @return The max number of requests in a notification
	 * When the number of requests reach this number the notification is triggered
	 */
	public Long getMaxItems(){
		return maxItems;
	}

	public void setMaxItems(Long maxItems){
		this.maxItems = maxItems;
	}

	/**
	 * @return The age after which a request can be dropped if not sent
	 */
	public Long getDroppableAgeMs(){
		return droppableAgeMs;
	}

	public String getDroppableAge(){
		return toDurationString(droppableAgeMs);
	}

	public void setDroppableAgeMs(Long droppableAgeMs){
		this.droppableAgeMs = droppableAgeMs;
	}

	/**
	 * @return Minimum delay between two same type and same channel Notification sends
	 */
	public Long getDelayForChannelMs(){
		return delayForChannelMs;
	}

	public String getDelayForChannel(){
		return toDurationString(delayForChannelMs);
	}

	public void setDelayForChannelMs(Long delayForChannelMs){
		this.delayForChannelMs = delayForChannelMs;
	}

	/**
	 * @return A group where the last request is older than this age will be sent
	 * If no new notification Request have been received during this delay the group is sent
	 */
	public Long getMinDelayMs(){
		return minDelayMs;
	}

	public String getMinDelay(){
		return toDurationString(minDelayMs);
	}

	public void setMinDelayMs(Long minDelayMs){
		this.minDelayMs = minDelayMs;
	}

	/**
	 * @return Minimum delay between two same type Notification sending
	 */
	public Long getStandardDelayMs(){
		return standardDelayMs;
	}

	public String getStandardDelay(){
		return toDurationString(standardDelayMs);
	}

	public void setStandardDelayMs(Long standardDelayMs){
		this.standardDelayMs = standardDelayMs;
	}

	/**
	 * @return A group where the first request is older than this age will be sent
	 * Any request older than this delay will definitely trigger the send for its group
	 */
	public Long getMaxDelayMs(){
		return maxDelayMs;
	}

	public String getMaxDelay(){
		return toDurationString(maxDelayMs);
	}

	public void setMaxDelayMs(Long maxDelayMs){
		this.maxDelayMs = maxDelayMs;
	}

	public static class FieldKeys{
		public static final LongFieldKey minSendableAgeMs = new LongFieldKey("minSendableAgeMs");
		public static final LongFieldKey maxItems = new LongFieldKey("maxItems");
		public static final LongFieldKey droppableAgeMs = new LongFieldKey("droppableAgeMs");
		public static final LongFieldKey delayForChannelMs = new LongFieldKey("delayForChannelMs");
		public static final LongFieldKey minDelayMs = new LongFieldKey("minDelayMs");
		public static final LongFieldKey standardDelayMs = new LongFieldKey("standardDelayMs");
		public static final LongFieldKey maxDelayMs = new LongFieldKey("maxDelayMs");
	}

	public static class NotificationTimingStrategyFielder
	extends BaseDatabeanFielder<NotificationTimingStrategyKey,NotificationTimingStrategy>{
		public NotificationTimingStrategyFielder(){
			super(NotificationTimingStrategyKey.class);
		}

		@Override
		public List<Field<?>> getNonKeyFields(NotificationTimingStrategy databean){
			return Arrays.asList(
					new LongField(FieldKeys.minSendableAgeMs, databean.minSendableAgeMs),
					new LongField(FieldKeys.maxItems, databean.maxItems),
					new LongField(FieldKeys.droppableAgeMs, databean.droppableAgeMs),
					new LongField(FieldKeys.delayForChannelMs, databean.delayForChannelMs),
					new LongField(FieldKeys.minDelayMs, databean.minDelayMs),
					new LongField(FieldKeys.standardDelayMs, databean.standardDelayMs),
					new LongField(FieldKeys.maxDelayMs, databean.maxDelayMs));
		}
	}
}
