package com.hotpads.notification.timing;

import java.util.Arrays;
import java.util.List;

import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.imp.StringField;
import com.hotpads.datarouter.storage.field.imp.StringFieldKey;
import com.hotpads.datarouter.storage.key.primary.BasePrimaryKey;

public class NotificationTimingStrategyMappingKey extends BasePrimaryKey<NotificationTimingStrategyMappingKey> {
  private String type;

  private String channel;

  public NotificationTimingStrategyMappingKey() {
  }

  public NotificationTimingStrategyMappingKey(String type, String channel) {
    this.type = type;
    this.channel = channel;
  }

  @Override
  public List<Field<?>> getFields() {
    return Arrays.asList(
        new StringField(FieldKeys.type, type),
        new StringField(FieldKeys.channel, channel));
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getChannel() {
    return channel;
  }

  public void setChannel(String channel) {
    this.channel = channel;
  }

  public static class FieldKeys {
    public static final StringFieldKey type = new StringFieldKey("type");

    public static final StringFieldKey channel = new StringFieldKey("channel");
  }
}
