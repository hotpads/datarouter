package com.hotpads.webappinstance.databean;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.hotpads.datarouter.config.DatarouterProperties;
import com.hotpads.datarouter.serialize.fielder.BaseDatabeanFielder;
import com.hotpads.datarouter.storage.databean.BaseDatabean;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.imp.DateField;
import com.hotpads.datarouter.storage.field.imp.DateFieldKey;
import com.hotpads.datarouter.storage.field.imp.StringField;
import com.hotpads.datarouter.storage.field.imp.StringFieldKey;
import com.hotpads.datarouter.util.core.DrDateTool;
import com.hotpads.datarouter.util.core.DrIterableTool;
import com.hotpads.handler.GitProperties;

public class WebAppInstance extends BaseDatabean<WebAppInstanceKey,WebAppInstance> {

	/** fields ****************************************************************/

	private WebAppInstanceKey key;

	private String serverType;
	private String serverPublicIp;
	private Date refreshedLast;
	private Date startupDate;
	private Date buildDate;
	private String commitId;

	/** columns ***************************************************************/

	public static class FieldKeys {
		public static final StringFieldKey serverType = new StringFieldKey("serverType");
		public static final StringFieldKey serverPublicIp = new StringFieldKey("serverPublicIp");
		public static final DateFieldKey refreshedLast = new DateFieldKey("refreshedLast");
		public static final DateFieldKey startupDate = new DateFieldKey("startupDate");
		public static final DateFieldKey buildDate = new DateFieldKey("buildDate");
		public static final StringFieldKey commitId = new StringFieldKey("commitId");
	}

	/** fielder ***************************************************************/

	public static class WebAppInstanceFielder extends BaseDatabeanFielder<WebAppInstanceKey, WebAppInstance>{

		public WebAppInstanceFielder(){
			super(WebAppInstanceKey.class);
		}

		@Override
		public List<Field<?>> getNonKeyFields(WebAppInstance databean){
			return Arrays.asList(
					new StringField(FieldKeys.serverType, databean.serverType),
					new StringField(FieldKeys.serverPublicIp, databean.serverPublicIp),
					new DateField(FieldKeys.refreshedLast, databean.refreshedLast),
					new DateField(FieldKeys.startupDate, databean.startupDate),
					new DateField(FieldKeys.buildDate, databean.buildDate),
					new StringField(FieldKeys.commitId, databean.commitId));
		}
	}

	/** construct *************************************************************/

	public WebAppInstance(){
		this.key = new WebAppInstanceKey(null, null);
	}

	public WebAppInstance(String webAppName, String serverName, String serverType){
		this.key = new WebAppInstanceKey(webAppName, serverName);
		this.serverType = serverType;
	}

	public WebAppInstance(String webAppName, DatarouterProperties datarouterProperties, Date startupDate,
			GitProperties gitProperties){
		this(webAppName, datarouterProperties.getServerName(), datarouterProperties.getServerTypeString());
		this.serverPublicIp = datarouterProperties.getServerPublicIp();
		this.refreshedLast = new Date();//webApp update job assumes this is happening
		this.startupDate = startupDate;
		this.buildDate = gitProperties.getBuildTime();
		this.commitId = gitProperties.getIdAbbrev();
	}

	/** databean **************************************************************/

	@Override
	public Class<WebAppInstanceKey> getKeyClass() {
		return WebAppInstanceKey.class;
	}

	@Override
	public WebAppInstanceKey getKey() {
		return key;
	}

	/*********************** static methods ***********************************/

	public static List<String> getUniqueServerNames(Iterable<WebAppInstance> ins){
		Set<String> outs = new HashSet<>();
		for(WebAppInstance in : DrIterableTool.nullSafe(ins)){
			outs.add(in.getKey().getServerName());
		}
		return new ArrayList<>(outs);
	}

	/****************************** methods ***********************************/

	public String getLastUpdatedTimeAgoPrintable(){
		if(refreshedLast == null){
			return "inactive";
		}
		return DrDateTool.getAgoString(refreshedLast);
	}

	public String getStartupDatePrintable(){
		return DrDateTool.getDateTime(startupDate);
	}

	public String getBuildDatePrintable(){
		return DrDateTool.getDateTime(buildDate);
	}

	/** get/set ***************************************************************/

	public String getServerType(){
		return serverType;
	}

	public Date getRefreshedLast(){
		return refreshedLast;
	}

	public String getServerPublicIp(){
		return serverPublicIp;
	}

	public Date getStartupDate(){
		return startupDate;
	}

	public Date getBuildDate(){
		return buildDate;
	}

	public String getCommitId(){
		return commitId;
	}
}

