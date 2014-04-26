package com.hotpads.profile.count.databean.alert;

import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.persistence.Entity;
import javax.persistence.Id;

import org.hibernate.annotations.AccessType;
import org.quartz.CronExpression;

import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.MySqlColumnType;
import com.hotpads.datarouter.serialize.fielder.BaseDatabeanFielder;
import com.hotpads.datarouter.serialize.fielder.Fielder;
import com.hotpads.datarouter.storage.databean.BaseDatabean;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.FieldTool;
import com.hotpads.datarouter.storage.field.imp.DateField;
import com.hotpads.datarouter.storage.field.imp.StringField;
import com.hotpads.datarouter.storage.field.imp.comparable.LongField;
import com.hotpads.datarouter.storage.key.multi.BaseLookup;
import com.hotpads.profile.count.databean.key.CounterAlertKey;
import com.hotpads.util.core.HashMethods;
import com.hotpads.util.core.MapTool;

@SuppressWarnings("serial")
@Entity()
@AccessType("field")
public class CounterAlert extends BaseDatabean<CounterAlertKey,CounterAlert>{
	public static final int LENGTH_counterName = MySqlColumnType.MAX_LENGTH_VARCHAR;
	public static final int LENGTH_comment= MySqlColumnType.MAX_LENGTH_VARCHAR;
	public static final int LENGTH_creator= MySqlColumnType.MAX_LENGTH_VARCHAR;
	public static final int LENGTH_alertTimeRange= MySqlColumnType.MAX_LENGTH_VARCHAR;
	
	/************* fileds ************************/
	@Id
	private Long counterAlertId;
	
	private String counterName;// eg "get Listing" or "rawSearch"
	private Long periodMs;
	private Long minThreshold;
	private Long maxThreshold;
	private String creator;
	private String alertTimeRange;
	private String comment;
	private Date createdDate = new Date();

	CounterAlert(){
		
	}
	
	public CounterAlert(String counterName, Long periodMs, Long minThreshold, Long maxThreshold, String creator, String alertTimeRange, String comment){
		this.counterName = counterName;
		this.periodMs = periodMs;
		this.minThreshold = minThreshold;
		this.maxThreshold = maxThreshold;
		this.creator = creator;
		this.alertTimeRange = alertTimeRange;
		this.comment = comment;
		buildId();
	}
	
	public static class F{
		public static final String
		id = "counterAlertId",
		counterName = "counterName",
		periodMs = "periodMs",
		minThreshold = "minThreshold",
		maxThreshold = "maxThreshold",
		creator = "creator",
		alertTimeRange = "alertTimeRange",
		comment = "comment",
		createdDate = "createdDate"
		;
	}
	
	/********************* build Id **************************/
	private void buildId(){
		setId(HashMethods.longDJBHash(counterName + periodMs + minThreshold + maxThreshold + creator + alertTimeRange));
	}
	
	/*************************** databean ****************************************/
	@Override
	public Class<CounterAlertKey> getKeyClass(){
		return CounterAlertKey.class;
	}

	@Override
	public CounterAlertKey getKey(){
		return new CounterAlertKey(counterAlertId);
	}	
	
	@Override
	public String getKeyFieldName(){
		return null;
	}
	
	@Override
	public List<Field<?>> getNonKeyFields(){
		List<Field<?>> fields = FieldTool.createList(
				new StringField(F.counterName, counterName, LENGTH_counterName),
				new LongField(F.periodMs, periodMs), 
				new LongField(F.minThreshold, minThreshold), 
				new LongField(F.maxThreshold, maxThreshold),
				new StringField(F.creator, creator, LENGTH_creator), 
				new StringField(F.alertTimeRange, alertTimeRange, LENGTH_alertTimeRange), 
				new StringField(F.comment, comment, LENGTH_comment), 
				new DateField(F.createdDate, createdDate)
				);

		return fields;
	}
	
	
	/***************************** MySQL fielder ******************************/	
	public static class CounterAlertFielder extends BaseDatabeanFielder<CounterAlertKey, CounterAlert>{
		public CounterAlertFielder(){}
		@Override
		public Class<? extends Fielder<CounterAlertKey>> getKeyFielderClass(){
			return CounterAlertKey.class;
		}

		@Override
		public List<Field<?>> getNonKeyFields(CounterAlert databean){
			return databean.getNonKeyFields();
		}
		
		@Override
		public Map<String,List<Field<?>>> getIndexes(CounterAlert counterAlert){
			Map<String,List<Field<?>>> indexesByName = MapTool.createTreeMap();
			indexesByName.put(F.counterName, new CounterAlertByCounterNameLookup(null).getFields());
			indexesByName.put(F.creator, new CounterAlertByCreatorLookup(null).getFields());
			return indexesByName;
		}
	}
	
	/******************************** indexes / lookup ******************************/
	public static class CounterAlertByCounterNameLookup extends BaseLookup<CounterAlertKey>{
		String counterName;
		public CounterAlertByCounterNameLookup(String counterName){
			this.counterName = counterName;
		}
		@Override
		public List<Field<?>> getFields(){
			return FieldTool.createList( new StringField(F.counterName, counterName, LENGTH_counterName));
		}
	}

	public static class CounterAlertByCreatorLookup extends BaseLookup<CounterAlertKey>{
		String creator;
		public CounterAlertByCreatorLookup(String creator){
			this.creator = creator;
		}
		@Override
		public List<Field<?>> getFields(){
			return FieldTool.createList( new StringField(F.creator, creator, LENGTH_creator));
		}
	}
	
	/******************************** getter/setter ***********************/
	public Long getId(){
		return counterAlertId;
	}

	public void setId(Long id){
		this.counterAlertId = id;
	}

	public String getCounterName(){
		return counterName;
	}

	public void setCounterName(String counterName){
		this.counterName = counterName;
	}

	public Long getPeriodMs(){
		return periodMs;
	}

	public void setPeriodMs(Long periodMs){
		this.periodMs = periodMs;
	}

	public long getMinThreshold(){
		return minThreshold;
	}

	public void setMinThreshold(long minThreshold){
		this.minThreshold = minThreshold;
	}

	public long getMaxThreshold(){
		return maxThreshold;
	}

	public void setMaxThreshold(long maxThreshold){
		this.maxThreshold = maxThreshold;
	}

	public String getCreator(){
		return creator;
	}

	public void setCreator(String creator){
		this.creator = creator;
	}

	public String getAlertTimeRange(){
		return alertTimeRange;
	}

	public void setAlertTimeRange(String alertTimeRange){
		this.alertTimeRange = alertTimeRange;
	}

	public String getComment(){
		return comment;
	}

	public void setComment(String comment){
		this.comment = comment;
	}

	public Date getCreatedDate(){
		return createdDate;
	}

	public void setCreatedDate(Date createdDate){
		this.createdDate = createdDate;
	}

	public static void main(String[] args) throws ParseException{
		CronExpression cron = new CronExpression("* * 11 25 * ?");
		Date date = new Date();
		System.out.println(date);
		System.out.println(cron.isSatisfiedBy(date));
	}
	

	
}
