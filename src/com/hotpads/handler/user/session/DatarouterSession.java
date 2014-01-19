package com.hotpads.handler.user.session;

import java.io.Serializable;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.hibernate.annotations.AccessType;

import com.hotpads.databean.property.user.ExternalUser.ExternalUserService;
import com.hotpads.databean.property.user.key.UserKey;
import com.hotpads.databean.property.user.security.Authority;
import com.hotpads.databean.property.user.security.UserRole;
import com.hotpads.databean.property.user.util.UserSessionTokenTool;
import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.MySqlColumnType;
import com.hotpads.datarouter.serialize.fielder.BaseDatabeanFielder;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.FieldTool;
import com.hotpads.datarouter.storage.field.imp.DateField;
import com.hotpads.datarouter.storage.field.imp.StringField;
import com.hotpads.datarouter.storage.field.imp.array.IntegerArrayField;
import com.hotpads.datarouter.storage.field.imp.comparable.BooleanField;
import com.hotpads.datarouter.storage.field.imp.comparable.LongField;
import com.hotpads.datarouter.storage.field.imp.enums.IntegerEnumField;
import com.hotpads.handler.user.session.BaseUserSessionDatabean;
import com.hotpads.session.SessionDao;
import com.hotpads.user.AuthorityDao;
import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.DateTool;
import com.hotpads.util.core.ListTool;
import com.hotpads.util.core.SetTool;
import com.hotpads.websupport.constants.RequestKeys;
import com.hotpads.websupport.user.UserTool;

@SuppressWarnings("serial")
@Entity()
@AccessType("field")
public class DatarouterSession extends BaseUserSessionDatabean<DatarouterSessionKey, DatarouterSession> implements Serializable {	
	static Logger logger = Logger.getLogger(DatarouterSession.class);
	
	//private static final long serialVersionUID = 7675243253727085994L;

	private String email;
	private Long userId;
	private boolean anonUser = true;
	
	private ExternalUserService externalService = null; //the external service currently used to log this user in
	private String externalId = null;					  //external service's user id
	
	@Lob @Column(length=1<<27)
	private List<Integer> userRoles = ListTool.create();
	
	private Date userCreationDate;
	
	protected String userToken;
	
	/************************** columns *************************/
	
	public class F {
		public static final String
					KEY_NAME = "key",
					userToken = "userToken",
					email = "email",
					userId = "userId",
					anonUser = "anonUser",
					externalService = "externalService",
					externalId = "externalId",
					userRoles = "userRoles",
					userCreationDate = "userCreationDate";
	}
	
	/************************** fielder *************************/
	
	@Override
	public List<Field<?>> getNonKeyFields() {
		List<Field<?>> fields =  FieldTool.createList(
				new DateField(BaseUserSessionDatabean.F.updated, updated),
				new StringField(F.userToken, userToken, MySqlColumnType.MAX_LENGTH_VARCHAR),
				new StringField(F.email, email, MySqlColumnType.MAX_LENGTH_VARCHAR),
				new LongField(F.userId, userId),
				new BooleanField(F.anonUser, anonUser),
				new StringField(F.externalId, externalId, MySqlColumnType.MAX_LENGTH_VARCHAR),
				new IntegerEnumField<ExternalUserService>(ExternalUserService.class, F.externalService, externalService),
				new IntegerArrayField(F.userRoles, userRoles),
				new DateField(F.userCreationDate, userCreationDate));
		return fields;
	}
	
	public static class UserSessionFielder extends BaseDatabeanFielder<DatarouterSessionKey,DatarouterSession>{
		public UserSessionFielder(){}
		@Override
		public Class<DatarouterSessionKey> getKeyFielderClass(){
			return DatarouterSessionKey.class;
		}
		@Override
		public List<Field<?>> getNonKeyFields(DatarouterSession d){
			return d.getNonKeyFields();
		}
	}
	
	@Override
	public boolean isFieldAware(){
		return true;
	}
	
	public Class<DatarouterSessionKey> getKeyClass() {
		return DatarouterSessionKey.class;
	}

	/************************** constructor *************************/
	
	public DatarouterSession(){
		super(new DatarouterSessionKey());
	}
	
	/*********************** static methods ************************************/
	
	public static DatarouterSession nullSafe(DatarouterSession in){
		return in==null ? new DatarouterSession() : in;
	}
	
	private final static String REQUEST_ATTRIBUTE_NAME = "userSession";
	
	public static DatarouterSession getUserSession(HttpServletRequest request) {
		//try to get userSession out of request cache
		DatarouterSession userSession = (DatarouterSession)request.getAttribute(REQUEST_ATTRIBUTE_NAME);
		if (userSession != null) {
			return userSession;
		} else {
			String sessionToken = UserSessionTokenTool.getSessionTokenFromCookie(request);
			userSession = SessionDao.getUserSession(sessionToken);
			if (userSession != null)
				cacheInRequest(request, userSession);
			return userSession;
		}
	}
		
	public static void store(HttpServletRequest request, DatarouterSession userSession) {
		cacheInRequest(request, userSession);
		Set<UserRole> roles = SetTool.create();
		boolean rep = request.getAttribute(RequestKeys.REP.toString()) != null && ((Boolean)request.getAttribute(RequestKeys.REP.toString()));
		if (rep){ //dont store rep permissions for changedUser
			roles = SetTool.createHashSet(userSession.getUserRoles());
			roles.remove(UserRole.ROLE_REP);
			userSession.setUserRoles(roles);
		}
		//store userSession in memcached for 3 days
		if (userSession != null) {
			if(userSession.getUserToken() == null){
				userSession.setUserToken(UserTool.getUserToken(request));
				userSession.setSessionToken(UserTool.getSessionToken(request));									
			}
			SessionDao.saveUserSession(userSession);
		}
		
		if (rep) {
			roles.add(UserRole.ROLE_REP);
			userSession.setUserRoles(roles);
		}
	}
	
	public static void cacheInRequest(HttpServletRequest request, DatarouterSession userSession) {
		request.setAttribute(REQUEST_ATTRIBUTE_NAME, userSession); 
	}
	
	public static void populateAndCache(HttpServletRequest request, DatarouterSession userSession, User user){
		List<Authority> authorities = AuthorityDao.getAuthorities(user.getKey());
		userSession.setEmail(user.getEmail());
		userSession.setId(user.getId());
		userSession.setAnonUser(false);
		userSession.setUserToken(user.getToken());
		
		userSession.setUserRoles(Authority.getUserRoles(authorities));

			

		store(request, userSession);
		
		if (request.getAttribute(RequestKeys.REP.toString()) != null && ((Boolean)request.getAttribute(RequestKeys.REP.toString()))){
			Set<UserRole> roRoles = userSession.getUserRoles();
			Set<UserRole> roles = SetTool.createHashSet(roRoles);
			roles.add(UserRole.ROLE_REP);
			userSession.setUserRoles(roles);
		}
		cacheInRequest(request, userSession);
		
	}


	/******************** ThreadLocal *******************************************************/
	
	public static ThreadLocal<DatarouterSession> userSessionHolder = new ThreadLocal<DatarouterSession>();

	public static DatarouterSession bindToThread(DatarouterSession userSession) {
		userSessionHolder.set(userSession);
		return userSession;
	}

	public static DatarouterSession getFromThread() {
		if(userSessionHolder==null){ return null; }
		return userSessionHolder.get();
	}

	public static void clearFromThread() {
		userSessionHolder.set(null);
	}
	
	/********************** methods *************************************/
	
	public List<UserRole> getRolesFromIntegers(){
		List<UserRole> roles = ListTool.create();
		for(Integer role : userRoles){
			roles.add(UserRole.fromInteger(role));
		}
		return roles;
	}
	
	public List<Integer> getIntegersFromRoles(List<UserRole> roles){
		List<Integer> integers = ListTool.create();
		for(UserRole role : roles){
			integers.add(role.getInteger());
		}
		return integers;
	}
	
	public UserKey getUserKey(){
		if(userId==null){ return null; }
		return new UserKey(userId);
	}

	public boolean isLoggedIn(){
		return !anonUser;
	}	
	
	public boolean doesUserHaveRole(UserRole requiredRole) {
		for(UserRole role : CollectionTool.nullSafe(getRolesFromIntegers())){
			if(requiredRole == role){
				return true;
			}
		}
		return false;
	}
	
	@Override
	public String toString() {
		return getClass().getSimpleName()+"{"
			+ "email:" + email 
			+ ",id:" + userId 
			+ ",anonUser:" + anonUser 
			+ ",token:" + getUserToken()
			+ ",sessionToken:" + getSessionToken()
			+ ",external:" + (externalService == null ? "null" : externalService.getDisplay()) 
			+ ",externaId:" + externalId
			+ ","+CollectionTool.getCsvList(userRoles)
			+ "," + (updated == null ? "null" : DateTool.getYYYYMMDDHHMMSS(updated))
			+ "}";
	}
	
	/*********************** get/set ************************************/
	
	public DatarouterSessionKey getKey() {
		return key;
	}
	
	public void setKey(DatarouterSessionKey key) {
		this.key = key;
	}
	
	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}
	public Long getId() {
		return userId;
	}
	public void setId(Long id) {
		this.userId = id;
	}
	public boolean getAnonUser() {
		return anonUser;
	}
	public void setAnonUser(boolean anonUser) {
		this.anonUser = anonUser;
	}
	public Set<UserRole> getUserRoles() {
		return Collections.unmodifiableSet(SetTool.create(getRolesFromIntegers()));
	}

	public void setUserRoles(Set<UserRole> userRoles) {
		this.userRoles = getIntegersFromRoles(ListTool.createArrayList(userRoles));
	}

	public ExternalUserService getExternalService() {
		return externalService;
	}

	public void setExternalService(ExternalUserService externalService) {
		this.externalService = externalService;
	}

	public String getExternalId() {
		return externalId;
	}

	public void setExternalId(String externalId) {
		this.externalId = externalId;
	}
	
	public Date getUserCreationDate() {
		return userCreationDate;
	}

	public void setUserCreationDate(Date creationDate) {
		this.userCreationDate = creationDate;
	}
	
	public String getUserToken(){
		return userToken;
	}

	public void setUserToken(String userToken){
		this.userToken = userToken;
	}
}
