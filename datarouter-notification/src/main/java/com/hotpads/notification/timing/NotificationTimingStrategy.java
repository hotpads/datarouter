package com.hotpads.notification.timing;

import java.util.Arrays;
import java.util.List;

import com.hotpads.datarouter.serialize.fielder.BaseDatabeanFielder;
import com.hotpads.datarouter.storage.databean.BaseDatabean;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.imp.comparable.LongField;
import com.hotpads.datarouter.storage.field.imp.comparable.LongFieldKey;

public class NotificationTimingStrategy extends BaseDatabean<NotificationTimingStrategyKey, NotificationTimingStrategy> {
  private NotificationTimingStrategyKey key;

  private Long minSendableAgeMs;

  private Long maxItems;

  private Long droppableAgeMs;

  private Long delayForChannelMs;

  private Long minDelayMs;

  private Long standardDelayMs;

  private Long maxDelayMs;

  public NotificationTimingStrategy() {
    this.key = new NotificationTimingStrategyKey();
  }

  public NotificationTimingStrategy(String name, Long minSendableAgeMs, Long maxItems,
      Long droppableAgeMs, Long delayForChannelMs, Long minDelayMs, Long standardDelayMs,
      Long maxDelayMs) {
    this.key = new NotificationTimingStrategyKey(name);
    this.minSendableAgeMs = minSendableAgeMs;
    this.maxItems = maxItems;
    this.droppableAgeMs = droppableAgeMs;
    this.delayForChannelMs = delayForChannelMs;
    this.minDelayMs = minDelayMs;
    this.standardDelayMs = standardDelayMs;
    this.maxDelayMs = maxDelayMs;
  }

  @Override
  public Class<NotificationTimingStrategyKey> getKeyClass() {
    return NotificationTimingStrategyKey.class;
  }

  @Override
  public NotificationTimingStrategyKey getKey() {
    return key;
  }

  public Long getMinSendableAgeMs() {
    return minSendableAgeMs;
  }

  public void setMinSendableAgeMs(Long minSendableAgeMs) {
    this.minSendableAgeMs = minSendableAgeMs;
  }

  public Long getMaxItems() {
    return maxItems;
  }

  public void setMaxItems(Long maxItems) {
    this.maxItems = maxItems;
  }

  public Long getDroppableAgeMs() {
    return droppableAgeMs;
  }

  public void setDroppableAgeMs(Long droppableAgeMs) {
    this.droppableAgeMs = droppableAgeMs;
  }

  public Long getDelayForChannelMs() {
    return delayForChannelMs;
  }

  public void setDelayForChannelMs(Long delayForChannelMs) {
    this.delayForChannelMs = delayForChannelMs;
  }

  public Long getMinDelayMs() {
    return minDelayMs;
  }

  public void setMinDelayMs(Long minDelayMs) {
    this.minDelayMs = minDelayMs;
  }

  public Long getStandardDelayMs() {
    return standardDelayMs;
  }

  public void setStandardDelayMs(Long standardDelayMs) {
    this.standardDelayMs = standardDelayMs;
  }

  public Long getMaxDelayMs() {
    return maxDelayMs;
  }

  public void setMaxDelayMs(Long maxDelayMs) {
    this.maxDelayMs = maxDelayMs;
  }

  public static class FieldKeys {
    public static final LongFieldKey minSendableAgeMs = new LongFieldKey("minSendableAgeMs");

    public static final LongFieldKey maxItems = new LongFieldKey("maxItems");

    public static final LongFieldKey droppableAgeMs = new LongFieldKey("droppableAgeMs");

    public static final LongFieldKey delayForChannelMs = new LongFieldKey("delayForChannelMs");

    public static final LongFieldKey minDelayMs = new LongFieldKey("minDelayMs");

    public static final LongFieldKey standardDelayMs = new LongFieldKey("standardDelayMs");

    public static final LongFieldKey maxDelayMs = new LongFieldKey("maxDelayMs");
  }

  public static class NotificationTimingStrategyFielder extends BaseDatabeanFielder<NotificationTimingStrategyKey, NotificationTimingStrategy> {
    public NotificationTimingStrategyFielder() {
      super(NotificationTimingStrategyKey.class);
    }

    @Override
    public List<Field<?>> getNonKeyFields(NotificationTimingStrategy databean) {
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
