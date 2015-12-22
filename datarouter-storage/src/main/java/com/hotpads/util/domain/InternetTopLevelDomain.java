package com.hotpads.util.domain;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashSet;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

import com.hotpads.datarouter.util.core.DrStringTool;

/**
 * see current list of tlds here:
 * http://data.iana.org/TLD/tlds-alpha-by-domain.txt
 */
public enum InternetTopLevelDomain {
	AAA, ABB, ABBOTT, ABOGADO, AC, ACADEMY, ACCENTURE, ACCOUNTANT, ACCOUNTANTS, ACO, ACTIVE, ACTOR, AD, ADS, ADULT, AE,
	AEG, AERO, AF, AFL, AG, AGENCY, AI, AIG, AIRFORCE, AIRTEL, AL, ALLFINANZ, ALSACE, AM, AMICA, AMSTERDAM, ANDROID, AO,
	APARTMENTS, APP, AQ, AQUARELLE, AR, ARCHI, ARMY, ARPA, AS, ASIA, ASSOCIATES, AT, ATTORNEY, AU, AUCTION, AUDIO, AUTO,
	AUTOS, AW, AX, AXA, AZ, AZURE, BA, BAND, BANK, BAR, BARCELONA, BARCLAYCARD, BARCLAYS, BARGAINS, BAUHAUS, BAYERN, BB,
	BBC, BBVA, BCN, BD, BE, BEER, BENTLEY, BERLIN, BEST, BET, BF, BG, BH, BHARTI, BI, BIBLE, BID, BIKE, BING, BINGO,
	BIO, BIZ, BJ, BLACK, BLACKFRIDAY, BLOOMBERG, BLUE, BM, BMS, BMW, BN, BNL, BNPPARIBAS, BO, BOATS, BOM, BOND, BOO,
	BOOTS, BOUTIQUE, BR, BRADESCO, BRIDGESTONE, BROKER, BROTHER, BRUSSELS, BS, BT, BUDAPEST, BUILD, BUILDERS, BUSINESS,
	BUZZ, BV, BW, BY, BZ, BZH, CA, CAB, CAFE, CAL, CAMERA, CAMP, CANCERRESEARCH, CANON, CAPETOWN, CAPITAL, CAR, CARAVAN,
	CARDS, CARE, CAREER, CAREERS, CARS, CARTIER, CASA, CASH, CASINO, CAT, CATERING, CBA, CBN, CC, CD, CEB, CENTER, CEO,
	CERN, CF, CFA, CFD, CG, CH, CHANEL, CHANNEL, CHAT, CHEAP, CHLOE, CHRISTMAS, CHROME, CHURCH, CI, CISCO, CITIC, CITY,
	CK, CL, CLAIMS, CLEANING, CLICK, CLINIC, CLOTHING, CLOUD, CLUB, CLUBMED, CM, CN, CO, COACH, CODES, COFFEE, COLLEGE,
	COLOGNE, COM, COMMBANK, COMMUNITY, COMPANY, COMPUTER, CONDOS, CONSTRUCTION, CONSULTING, CONTRACTORS, COOKING, COOL,
	COOP, CORSICA, COUNTRY, COUPONS, COURSES, CR, CREDIT, CREDITCARD, CRICKET, CROWN, CRS, CRUISES, CSC, CU, CUISINELLA,
	CV, CW, CX, CY, CYMRU, CYOU, CZ, DABUR, DAD, DANCE, DATE, DATING, DATSUN, DAY, DCLK, DE, DEALS, DEGREE, DELIVERY,
	DELTA, DEMOCRAT, DENTAL, DENTIST, DESI, DESIGN, DEV, DIAMONDS, DIET, DIGITAL, DIRECT, DIRECTORY, DISCOUNT, DJ, DK,
	DM, DNP, DO, DOCS, DOG, DOHA, DOMAINS, DOOSAN, DOWNLOAD, DRIVE, DURBAN, DVAG, DZ, EARTH, EAT, EC, EDU, EDUCATION,
	EE, EG, EMAIL, EMERCK, ENERGY, ENGINEER, ENGINEERING, ENTERPRISES, EPSON, EQUIPMENT, ER, ERNI, ES, ESQ, ESTATE, ET,
	EU, EUROVISION, EUS, EVENTS, EVERBANK, EXCHANGE, EXPERT, EXPOSED, EXPRESS, FAGE, FAIL, FAITH, FAMILY, FAN, FANS,
	FARM, FASHION, FEEDBACK, FI, FILM, FINAL, FINANCE, FINANCIAL, FIRMDALE, FISH, FISHING, FIT, FITNESS, FJ, FK,
	FLIGHTS, FLORIST, FLOWERS, FLSMIDTH, FLY, FM, FO, FOO, FOOTBALL, FOREX, FORSALE, FORUM, FOUNDATION, FR, FRL,
	FROGANS, FUND, FURNITURE, FUTBOL, FYI, GA, GAL, GALLERY, GAME, GARDEN, GB, GBIZ, GD, GDN, GE, GEA, GENT, GENTING,
	GF, GG, GGEE, GH, GI, GIFT, GIFTS, GIVES, GIVING, GL, GLASS, GLE, GLOBAL, GLOBO, GM, GMAIL, GMO, GMX, GN, GOLD,
	GOLDPOINT, GOLF, GOO, GOOG, GOOGLE, GOP, GOV, GP, GQ, GR, GRAPHICS, GRATIS, GREEN, GRIPE, GROUP, GS, GT, GU, GUGE,
	GUIDE, GUITARS, GURU, GW, GY, HAMBURG, HANGOUT, HAUS, HEALTHCARE, HELP, HERE, HERMES, HIPHOP, HITACHI, HIV, HK, HM,
	HN, HOCKEY, HOLDINGS, HOLIDAY, HOMEDEPOT, HOMES, HONDA, HORSE, HOST, HOSTING, HOTELES, HOTMAIL, HOUSE, HOW, HR,
	HSBC, HT, HU, HYUNDAI, IBM, ICBC, ICE, ICU, ID, IE, IFM, IINET, IL, IM, IMMO, IMMOBILIEN, IN, INDUSTRIES, INFINITI,
	INFO, ING, INK, INSTITUTE, INSURE, INT, INTERNATIONAL, INVESTMENTS, IO, IPIRANGA, IQ, IR, IRISH, IS, IST, ISTANBUL,
	IT, ITAU, IWC, JAVA, JCB, JE, JETZT, JEWELRY, JLC, JLL, JM, JO, JOBS, JOBURG, JP, JPRS, JUEGOS, KAUFEN, KDDI, KE,
	KG, KH, KI, KIA, KIM, KITCHEN, KIWI, KM, KN, KOELN, KOMATSU, KP, KR, KRD, KRED, KW, KY, KYOTO, KZ, LA, LACAIXA,
	LANCASTER, LAND, LASALLE, LAT, LATROBE, LAW, LAWYER, LB, LC, LDS, LEASE, LECLERC, LEGAL, LEXUS, LGBT, LI, LIAISON,
	LIDL, LIFE, LIGHTING, LIMITED, LIMO, LINDE, LINK, LIVE, LIXIL, LK, LOAN, LOANS, LOL, LONDON, LOTTE, LOTTO, LOVE, LR,
	LS, LT, LTD, LTDA, LU, LUPIN, LUXE, LUXURY, LV, LY, MA, MADRID, MAIF, MAISON, MAN, MANAGEMENT, MANGO, MARKET,
	MARKETING, MARKETS, MARRIOTT, MBA, MC, MD, ME, MEDIA, MEET, MELBOURNE, MEME, MEMORIAL, MEN, MENU, MG, MH, MIAMI,
	MICROSOFT, MIL, MINI, MK, ML, MM, MMA, MN, MO, MOBI, MODA, MOE, MOM, MONASH, MONEY, MONTBLANC, MORMON, MORTGAGE,
	MOSCOW, MOTORCYCLES, MOV, MOVIE, MOVISTAR, MP, MQ, MR, MS, MT, MTN, MTPC, MU, MUSEUM, MV, MW, MX, MY, MZ, NA, NADEX,
	NAGOYA, NAME, NAVY, NC, NE, NEC, NET, NETBANK, NETWORK, NEUSTAR, NEW, NEWS, NEXUS, NF, NG, NGO, NHK, NI, NICO,
	NINJA, NISSAN, NL, NO, NOKIA, NP, NR, NRA, NRW, NTT, NU, NYC, NZ, OBI, OFFICE, OKINAWA, OM, OMEGA, ONE, ONG, ONL,
	ONLINE, OOO, ORACLE, ORANGE, ORG, ORGANIC, OSAKA, OTSUKA, OVH, PA, PAGE, PANERAI, PARIS, PARTNERS, PARTS, PARTY, PE,
	PET, PF, PG, PH, PHARMACY, PHILIPS, PHOTO, PHOTOGRAPHY, PHOTOS, PHYSIO, PIAGET, PICS, PICTET, PICTURES, PINK, PIZZA,
	PK, PL, PLACE, PLAY, PLUMBING, PLUS, PM, PN, POHL, POKER, PORN, POST, PR, PRAXI, PRESS, PRO, PROD, PRODUCTIONS,
	PROF, PROPERTIES, PROPERTY, PROTECTION, PS, PT, PUB, PW, PY, QA, QPON, QUEBEC, RACING, RE, REALTOR, REALTY, RECIPES,
	RED, REDSTONE, REHAB, REISE, REISEN, REIT, REN, RENT, RENTALS, REPAIR, REPORT, REPUBLICAN, REST, RESTAURANT, REVIEW,
	REVIEWS, RICH, RICOH, RIO, RIP, RO, ROCKS, RODEO, RS, RSVP, RU, RUHR, RUN, RW, RYUKYU, SA, SAARLAND, SAKURA, SALE,
	SAMSUNG, SANDVIK, SANDVIKCOROMANT, SANOFI, SAP, SARL, SAXO, SB, SC, SCA, SCB, SCHMIDT, SCHOLARSHIPS, SCHOOL, SCHULE,
	SCHWARZ, SCIENCE, SCOR, SCOT, SD, SE, SEAT, SECURITY, SEEK, SENER, SERVICES, SEVEN, SEW, SEX, SEXY, SG, SH, SHIKSHA,
	SHOES, SHOW, SHRIRAM, SI, SINGLES, SITE, SJ, SK, SKI, SKY, SKYPE, SL, SM, SN, SNCF, SO, SOCCER, SOCIAL, SOFTWARE,
	SOHU, SOLAR, SOLUTIONS, SONY, SOY, SPACE, SPIEGEL, SPREADBETTING, SR, SRL, ST, STADA, STARHUB, STATOIL, STC,
	STCGROUP, STOCKHOLM, STUDIO, STUDY, STYLE, SU, SUCKS, SUPPLIES, SUPPLY, SUPPORT, SURF, SURGERY, SUZUKI, SV, SWATCH,
	SWISS, SX, SY, SYDNEY, SYSTEMS, SZ, TAIPEI, TATAMOTORS, TATAR, TATTOO, TAX, TAXI, TC, TD, TEAM, TECH, TECHNOLOGY,
	TEL, TELEFONICA, TEMASEK, TENNIS, TF, TG, TH, THD, THEATER, THEATRE, TICKETS, TIENDA, TIPS, TIRES, TIROL, TJ, TK,
	TL, TM, TN, TO, TODAY, TOKYO, TOOLS, TOP, TORAY, TOSHIBA, TOURS, TOWN, TOYOTA, TOYS, TR, TRADE, TRADING, TRAINING,
	TRAVEL, TRUST, TT, TUI, TV, TW, TZ, UA, UBS, UG, UK, UNIVERSITY, UNO, UOL, US, UY, UZ, VA, VACATIONS, VC, VE, VEGAS,
	VENTURES, VERSICHERUNG, VET, VG, VI, VIAJES, VIDEO, VILLAS, VIN, VISION, VISTA, VISTAPRINT, VIVA, VLAANDEREN, VN,
	VODKA, VOTE, VOTING, VOTO, VOYAGE, VU, WALES, WALTER, WANG, WATCH, WEBCAM, WEBSITE, WED, WEDDING, WEIR, WF, WHOSWHO,
	WIEN, WIKI, WILLIAMHILL, WIN, WINDOWS, WINE, WME, WORK, WORKS, WORLD, WS, WTC, WTF, XBOX, XEROX, XIN, XPERIA, XXX,
	XYZ, YACHTS, YANDEX, YE, YODOBASHI, YOGA, YOKOHAMA, YOUTUBE, YT, ZA, ZIP, ZM, ZONE, ZUERICH, ZW
	;
	private static Set<String> tlds = new HashSet<>();
	static{
		for(InternetTopLevelDomain  tld : values()){
			tlds.add(tld.name().toLowerCase());
		}
	}
	private static Set<String> comAlternatives = new HashSet<>();
	static {
		String[] alternatives = new String[] { "con", "vom", "ocm", "ccom", "cpom", "coom", "cocm", "comm", "copm" };
		for (String comAlternative : alternatives) {
			if (!exists(comAlternative)) {
				comAlternatives.add(comAlternative);
			}
		}
	}
	
	public static boolean exists(String tld) {
		return tlds.contains(DrStringTool.toLowerCase(tld));
	}
	
	public static String normalizeTld(String topLevelDomain) {
		if (DrStringTool.isEmpty(topLevelDomain)) {
			return null;
		}
		topLevelDomain = topLevelDomain.toLowerCase();

		for(String comAlternative: comAlternatives){
			if(comAlternative.equalsIgnoreCase(topLevelDomain)){
				return "com";
			}
		}

		for (int i = 0; i < topLevelDomain.length(); i++) {
			if (!Character.isLetterOrDigit(topLevelDomain.charAt(i))) {
				topLevelDomain = topLevelDomain.substring(0, i);
				break;
			}
		}

		topLevelDomain = DrStringTool.enforceAlphabetic(topLevelDomain);

		return exists(topLevelDomain) ? topLevelDomain : null;
	}
	
	/** helpers ****************************************************/
	private static void generateEnum(PrintStream out) throws IOException {
		URL url = new URL("http://data.iana.org/TLD/tlds-alpha-by-domain.txt");
		URLConnection conn = url.openConnection();
		BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		String line = null;
		StringBuilder outLine = new StringBuilder();
		boolean first = true;
		while ((line = br.readLine()) != null) {
			if (line.contains("#") || line.contains("--")) {
				continue;
			}
			if (outLine.length() + line.length() >= 114) {
				out.print(outLine.toString() + ",\n");
				outLine = new StringBuilder();
				first = true;
			}
			outLine.append((first ? "" : ", ") + line);
			first = false;
		}
		out.print(outLine.toString() + "\n");
		out.print(";");
	}

	public static void main(String[] args) throws IOException {
		generateEnum(System.out);
	}
	
	/** tests ****************************************************************/
	public static class Tests {
		@Test
		public void testNormalizeTld(){
			Assert.assertEquals("com", normalizeTld("COM"));
			Assert.assertEquals("com", normalizeTld("com"));
			Assert.assertEquals("com", normalizeTld("con"));
			Assert.assertEquals("org", normalizeTld("org"));
			Assert.assertEquals("com", normalizeTld("com1231"));
			Assert.assertEquals("com", normalizeTld("c1o2m1"));
			Assert.assertEquals("com", normalizeTld("c1o2m12"));
			Assert.assertEquals("com", normalizeTld("c1o2m"));
			Assert.assertEquals("com", normalizeTld("c1om"));
			Assert.assertEquals("com", normalizeTld("c1om,"));
			Assert.assertEquals("com", normalizeTld("com,"));
			Assert.assertEquals("com", normalizeTld("com?MO"));
		}
	}
}
