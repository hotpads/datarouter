package com.hotpads.util.email;

import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

import com.google.common.collect.Sets;
import com.hotpads.datarouter.storage.field.enums.StringEnum;
import com.hotpads.datarouter.util.core.ArrayTool;
import com.hotpads.datarouter.util.core.StringTool;
import com.hotpads.util.core.collections.Pair;
import com.hotpads.util.domain.InternetTopLevelDomain;

public enum EmailDomain implements StringEnum<EmailDomain>{
	
	// settings are based on info from http://en.wikipedia.org/wiki/Email_address#Common_local-part_semantics
	YAHOO("yahoo.com", false, true, true, false, null, "[\\-]"), 
	YMAIL("ymail.com", false, false, true, true, null, "[\\-]"), 
	ROCKETMAIL("rocketmail.com", false, false, true, true, null, "[\\-]"), 
	HOTMAIL("hotmail.com", false, true, true, false, null, "[\\-]"), 
	LIVE("live.com", false, false, true, false, null, "[\\-]", "outlook.com"), 
	AOL("aol.com", false, false, true, false, null, "[\\-]"), 
	GMAIL("gmail.com", false, false, true, false, "[\\.]", "[\\+]", "googlemail.com"), 
	CJB("cjb.net", true, false, true, true, null, null), 
	USA("usa.com", false, false, true, true, null, null), 
	POST("post.com", false, false, true, true, null, null), 
	INAME("iname.com", false, false, true, true, null, null),
	OUTLOOK("outlook.com", false, false, true, false, null, "[\\-]"),
	MAIL("mail.com", false, false, true, true, null, null),
	LYCOS("lycos.com", false, false, true, true, null, null),
	REDIFFMAIL("rediffmail.com", false, false, true, true, null, null),
	YOPMAIL("yopmail.com", false, false, true, true, null, null),
	MAILINATOR("mailinator.com", false, false, true, true, null, null),
	WRITEME("writeme.com", false, false, true, true, null, null),	
	YANDEX("yandex.com", false, true, true, true, null, null),
	ICLOUD("icloud.com",false,false,true,false,null,null),
	;
		
	private String 
		domain,
		localPartIgnore,
		tagSeparator;
	private boolean 
		hasSubdomains, 
		hasCountrySuffixes, 
		free, 
		skanchy;
	private String[] alternativeDomains;
	
	EmailDomain(String domain, boolean hasSubdomains, boolean hasCountrySuffixes, boolean free, boolean skanchy, 
			String localPartIgnore, String tagSeparator, String... alternatives){
		this.domain = domain;
		this.hasSubdomains = hasSubdomains;
		this.hasCountrySuffixes = hasCountrySuffixes;
		this.free = free;
		this.skanchy = skanchy;
		this.localPartIgnore = localPartIgnore;
		this.tagSeparator = tagSeparator;
		this.alternativeDomains = alternatives;
	}
	
	public String getDomain() {
		return domain;
	}

	public boolean getHasSubdomains() {
		return hasSubdomains;
	}

	public boolean getHasCountrySuffixes() {
		return hasCountrySuffixes;
	}

	public boolean getFree() {
		return free;
	}

	public boolean getSkanchy() {
		return skanchy;
	}

	public String[] getAlternativeDomains(){
		return alternativeDomains;
	}
	
	public String getLocalPartIgnore(){
		return localPartIgnore;
	}
	
	public String getTagSeparator(){
		return tagSeparator;
	}
	
	@Override
	public String getPersistentString() {
		return domain;
	}

	@Override
	public EmailDomain fromPersistentString(String s) {
		return fromPersistentStringStatic(s);
	}
	public static EmailDomain fromPersistentStringStatic(String s){
		if(s==null) return null;
		for(EmailDomain domain : values()){
			if(domain.getPersistentString().equals(s)) return domain;
			for(String alternativeDomain : ArrayTool.nullSafe(domain.getAlternativeDomains())){
				if(alternativeDomain.equals(s)){
					return domain;
				}
			}
		}
		return null;
	}
	public static EmailDomain fromFullDomain(String s){
		if(s==null) return null;
		EmailDomain domain = fromPersistentStringStatic(s);
		if(domain!=null) return domain;
		String[] domainParts = s.split("\\.");
		if(domainParts.length<3) return null;
		//try subdomain
		domain = fromPersistentStringStatic(
				domainParts[domainParts.length-2]+"."+
				domainParts[domainParts.length-1]);
		if(domain!=null && domain.hasSubdomains) return domain;
		//try country
		domain = stripCountry(domainParts);
		if(domain!=null && domain.hasCountrySuffixes) return domain;
		
		//unrecognized
		return null;
	}
	private static EmailDomain stripCountry(String[] domainParts){
		EmailDomain domain = fromPersistentStringStatic(
				domainParts[domainParts.length-3]+"."+
				domainParts[domainParts.length-2]);
		if(domain == null && "co".equals(domainParts[domainParts.length-2])){
			domainParts[domainParts.length-2] = "com";
			return stripCountry(domainParts);
		}
		return domain;
	}

	/**
	 * free || skanchy
	 * @return
	 */
	public static boolean isQuestionable(String emailAddress){
		String domainString = getDomain(emailAddress);
		EmailDomain domain = fromFullDomain(domainString);
		if(domain==null) return false;
		return domain.getFree() || domain.getSkanchy();
	}	

	public static boolean isFree(String emailAddress){
		String domainString = getDomain(emailAddress);
		EmailDomain domain = fromFullDomain(domainString);
		if(domain==null) return false;
		return domain.getFree();
	}
	
	public static Set<String> getFreeDomains(){
		Set<String> frees = Sets.newHashSet();
		for(EmailDomain domain : values()){
			if(domain.getFree()){
				frees.add(domain.getDomain());
			}
		}
		return frees;
	}
	public static Set<String> getSkanchyDomains(){
		Set<String> domains = Sets.newHashSet();
		for(EmailDomain domain : values()){
			if(domain.getSkanchy()){
				domains.add(domain.getDomain());
			}
		}
		return domains;		
	}

	/** utilities that depend on provider ************************************/
	public static String getDomain(String emailAddress){
		if(emailAddress==null){ return null; }
		int at = emailAddress.indexOf("@")+1;
		if(at<=0 || at>emailAddress.length()) return null;
		return emailAddress.substring(at).toLowerCase();
	}

	public static String normalizeEmail(final String email){
		if(email==null) return null;
		final String originalDomain = getDomain(email);
		Pair<String,EmailDomain> domainAndEmailDomain = normalizeAndMatchDomain(originalDomain);
		if(domainAndEmailDomain==null) return null;
		String localPart = email.substring(0, email.length()-originalDomain.length()-1);
		if(StringTool.isEmpty(localPart)) return null;
		localPart = localPart.toLowerCase();
		
		EmailDomain emailDomain = domainAndEmailDomain.getRight();
		if(emailDomain != null){
			if(emailDomain.getLocalPartIgnore()!=null){
				localPart = localPart.replaceAll(emailDomain.getLocalPartIgnore(),"");
			}
			if(emailDomain.getTagSeparator()!=null){
				localPart = localPart.split(emailDomain.getTagSeparator(),2)[0];
			}
		}
		return localPart +"@"+ domainAndEmailDomain.getLeft();
	}
	
	public static String normalizeSubstituteDomain(String domain){
		Pair<String,EmailDomain> domainAndEmailDomain = normalizeAndMatchDomain(domain);
		if(domainAndEmailDomain==null) return null;
		return domainAndEmailDomain.getLeft();
	}
	public static Pair<String,EmailDomain> normalizeAndMatchDomain(String domain){
		if(StringTool.isEmpty(domain)) return null;
		domain = domain.toLowerCase();
		EmailDomain emailDomain = EmailDomain.fromFullDomain(domain);
		if(emailDomain == null) return Pair.create(domain,null);
		if( ! emailDomain.getHasCountrySuffixes() 
				&& ! (emailDomain.getHasSubdomains() && domain.endsWith(emailDomain.getDomain()))){
			domain = emailDomain.getDomain();
		}
		return Pair.create(domain,emailDomain);
	}
	
	public static String normalizeDomain(String domain){
		if(StringTool.isEmpty(domain)){
			return null;
		}
		if ("yahoocom".equalsIgnoreCase(domain) 
				|| "ymailcom".equalsIgnoreCase(domain)
				|| "gmailcom".equalsIgnoreCase(domain)
				|| "googlemailcom".equalsIgnoreCase(domain)
				|| "hotmailcom".equalsIgnoreCase(domain)
				|| "aolcom".equalsIgnoreCase(domain)){
			return domain.toLowerCase().replace("com", ".com");
		}
		domain = domain.replaceAll(",", ".");// fix for "gmail,com"
		while(domain.length()>0 && domain.endsWith(".")){
			domain = domain.substring(0, domain.length()-1);
		}
		int tld = domain.lastIndexOf((int) '.');
		if (tld <= 0) {
			return null;// localhost email domain fails here
		}

		String topLevelDomain = InternetTopLevelDomain.normalizeTld(domain.substring(tld + 1));
		if (topLevelDomain == null) {
			return null;
		}
		return domain.substring(0, tld).trim() + "." + topLevelDomain;
	}
	
	/** Tests *****************************************************************/
	public static class Tests {
		@Test public void testFromFullDomain(){
			Assert.assertEquals(YAHOO, fromFullDomain(YAHOO.getDomain()));
			Assert.assertEquals(YAHOO, fromFullDomain(YAHOO.getDomain()+".in"));
			Assert.assertEquals(CJB, fromFullDomain("crack."+CJB.getDomain()));
			Assert.assertNull(fromFullDomain(CJB.getDomain()+".in"));
			Assert.assertNull(fromFullDomain("crack."+YAHOO.getDomain()));
			Assert.assertNull(fromFullDomain("crack."+GMAIL.getDomain()));
			Assert.assertNull(fromFullDomain(GMAIL.getDomain()+".in"));
			Assert.assertNull(fromFullDomain("crack.example.com"));
			Assert.assertNull(fromFullDomain("example.com.uk"));
			Assert.assertNull(fromFullDomain("example.com"));
			Assert.assertNull(fromFullDomain("example"));
			Assert.assertNull(fromFullDomain(".com"));
			Assert.assertNull(fromFullDomain(""));
			Assert.assertNull(fromFullDomain(null));

			Assert.assertEquals(HOTMAIL, fromFullDomain("hotmail.co.uk"));
		}

		@Test public void testGetDomain(){
			Assert.assertEquals("dook.com", getDomain("me@dook.com"));
			//crazy lady
			Assert.assertEquals(null, getDomain("betty.gmail.com"));
		}
		
		@Test public void testNormalizeSubstituteDomain(){
			Assert.assertEquals(GMAIL.getDomain(), normalizeSubstituteDomain(GMAIL.getDomain()));
			Assert.assertEquals(GMAIL.getDomain(), normalizeSubstituteDomain("googlemail.com"));
			Assert.assertEquals(YMAIL.getDomain(), normalizeSubstituteDomain(YMAIL.getDomain()));
			//country
			String yahooIndia = YAHOO.getDomain() +".in";
			Assert.assertEquals(YAHOO, normalizeAndMatchDomain(yahooIndia).getRight());
			Assert.assertEquals(yahooIndia, normalizeSubstituteDomain(yahooIndia));
			//subdomain
			String testCjb = "test."+CJB.getDomain();
			Assert.assertEquals(CJB, normalizeAndMatchDomain(testCjb).getRight());
			Assert.assertEquals(testCjb, normalizeSubstituteDomain(testCjb));
		}
		
		@Test public void testNormalizeEmail(){
			Assert.assertEquals("samham@gmail.com", normalizeEmail("sam.ham@gmail.com"));
			Assert.assertEquals("samham@gmail.com", normalizeEmail("samham@gmail.com"));
			Assert.assertEquals("samham@gmail.com", normalizeEmail("s.am.h.a.m@gmail.com"));
			Assert.assertEquals("samham@gmail.com", normalizeEmail("samham+whisker@gmail.com"));
			Assert.assertEquals("samham@gmail.com", normalizeEmail("s.am.h.a.m+bologna@gmail.com"));
			Assert.assertEquals("samham@gmail.com", normalizeEmail("samham+bologna+sandwich@gmail.com"));
			Assert.assertEquals("s@gmail.com", normalizeEmail("s@gmail.com"));
			Assert.assertEquals("s-s@gmail.com", normalizeEmail("s-s@gmail.com"));
			Assert.assertEquals("s@gmail.com", normalizeEmail("s@googlemail.com"));

			Assert.assertEquals("xyz@ymail.com", normalizeEmail("xyz@ymail.com"));
			Assert.assertEquals("xyz.abc@ymail.com", normalizeEmail("xyz.abc@ymail.com"));
			Assert.assertEquals("xyz.abc+123@ymail.com", normalizeEmail("xyz.abc+123@ymail.com"));
			Assert.assertEquals("xyz.abc@ymail.com", normalizeEmail("xyz.abc-123@ymail.com"));

			Assert.assertEquals("xyz@example.com", normalizeEmail("xyz@example.com"));
			Assert.assertEquals("xyz.abc@example.com", normalizeEmail("xyz.abc@example.com"));
			Assert.assertEquals("xyz.abc+123@example.com", normalizeEmail("xyz.abc+123@example.com"));
			Assert.assertEquals("xyz.abc-123@example.com", normalizeEmail("xyz.abc-123@example.com"));

			Assert.assertEquals(null, normalizeEmail("xyz"));
			Assert.assertEquals(null, normalizeEmail(null));
			Assert.assertEquals(null, normalizeEmail("xyz@"));
			Assert.assertEquals(null, normalizeEmail("@xyz"));
			Assert.assertEquals(null, normalizeEmail("@xyz.com"));
		}
		
		@Test public void testNormalizeDomain(){
			Assert.assertEquals("example.com", normalizeDomain("example.com,"));
			Assert.assertEquals("example.com", normalizeDomain("example.com"));
			Assert.assertEquals("example1.com", normalizeDomain("example1.com"));
			Assert.assertEquals("example.com", normalizeDomain("example.com?MO"));
		}
	}
}
