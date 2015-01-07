package com.hotpads.util.email;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashSet;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

import com.hotpads.util.core.StringTool;

/**
 * see current list of tlds here:
 * http://data.iana.org/TLD/tlds-alpha-by-domain.txt
 */
public enum InternetTopLevelDomain {
	AC, ACADEMY, ACCOUNTANTS, ACTIVE, ACTOR, AD, AE, AERO, AF, AG, AGENCY, AI, AIRFORCE, AL, AM, AN, AO, AQ, AR, ARCHI,
	ARMY, ARPA, AS, ASIA, ASSOCIATES, AT, ATTORNEY, AU, AUCTION, AUDIO, AUTOS, AW, AX, AXA, AZ, BA, BAR, BARGAINS,
	BAYERN, BB, BD, BE, BEER, BERLIN, BEST, BF, BG, BH, BI, BID, BIKE, BIO, BIZ, BJ, BLACK, BLACKFRIDAY, BLUE, BM, BMW,
	BN, BO, BOUTIQUE, BR, BRUSSELS, BS, BT, BUILD, BUILDERS, BUZZ, BV, BW, BY, BZ, BZH, CA, CAB, CAMERA, CAMP,
	CANCERRESEARCH, CAPETOWN, CAPITAL, CARDS, CARE, CAREER, CAREERS, CASH, CAT, CATERING, CC, CD, CENTER, CEO, CF, CG,
	CH, CHEAP, CHRISTMAS, CHURCH, CI, CITIC, CITY, CK, CL, CLAIMS, CLEANING, CLINIC, CLOTHING, CLUB, CM, CN, CO, CODES,
	COFFEE, COLLEGE, COLOGNE, COM, COMMUNITY, COMPANY, COMPUTER, CONDOS, CONSTRUCTION, CONSULTING, CONTRACTORS, COOKING,
	COOL, COOP, COUNTRY, CR, CREDIT, CREDITCARD, CRUISES, CU, CUISINELLA, CV, CW, CX, CY, CZ, DANCE, DATING, DE, DEALS,
	DEGREE, DEMOCRAT, DENTAL, DENTIST, DESI, DIAMONDS, DIGITAL, DIRECT, DIRECTORY, DISCOUNT, DJ, DK, DM, DNP, DO,
	DOMAINS, DURBAN, DZ, EC, EDU, EDUCATION, EE, EG, EMAIL, ENGINEER, ENGINEERING, ENTERPRISES, EQUIPMENT, ER, ES,
	ESTATE, ET, EU, EUS, EVENTS, EXCHANGE, EXPERT, EXPOSED, FAIL, FARM, FEEDBACK, FI, FINANCE, FINANCIAL, FISH, FISHING,
	FITNESS, FJ, FK, FLIGHTS, FLORIST, FM, FO, FOO, FOUNDATION, FR, FROGANS, FUND, FURNITURE, FUTBOL, GA, GAL, GALLERY,
	GB, GD, GE, GENT, GF, GG, GH, GI, GIFT, GIVES, GL, GLASS, GLOBAL, GLOBO, GM, GMO, GN, GOP, GOV, GP, GQ, GR,
	GRAPHICS, GRATIS, GREEN, GRIPE, GS, GT, GU, GUIDE, GUITARS, GURU, GW, GY, HAMBURG, HAUS, HIPHOP, HIV, HK, HM, HN,
	HOLDINGS, HOLIDAY, HOMES, HORSE, HOST, HOUSE, HR, HT, HU, ID, IE, IL, IM, IMMOBILIEN, IN, INDUSTRIES, INFO, INK,
	INSTITUTE, INSURE, INT, INTERNATIONAL, INVESTMENTS, IO, IQ, IR, IS, IT, JE, JETZT, JM, JO, JOBS, JOBURG, JP, JUEGOS,
	KAUFEN, KE, KG, KH, KI, KIM, KITCHEN, KIWI, KM, KN, KOELN, KP, KR, KRD, KRED, KW, KY, KZ, LA, LACAIXA, LAND, LAWYER,
	LB, LC, LEASE, LGBT, LI, LIFE, LIGHTING, LIMITED, LIMO, LINK, LK, LOANS, LONDON, LOTTO, LR, LS, LT, LU, LUXE,
	LUXURY, LV, LY, MA, MAISON, MANAGEMENT, MANGO, MARKET, MARKETING, MC, MD, ME, MEDIA, MEET, MELBOURNE, MENU, MG, MH,
	MIAMI, MIL, MINI, MK, ML, MM, MN, MO, MOBI, MODA, MOE, MONASH, MORTGAGE, MOSCOW, MOTORCYCLES, MP, MQ, MR, MS, MT,
	MU, MUSEUM, MV, MW, MX, MY, MZ, NA, NAGOYA, NAME, NAVY, NC, NE, NET, NEUSTAR, NF, NG, NGO, NHK, NI, NINJA, NL, NO,
	NP, NR, NRA, NRW, NU, NYC, NZ, OKINAWA, OM, ONL, ORG, ORGANIC, OVH, PA, PARIS, PARTNERS, PARTS, PE, PF, PG, PH,
	PHOTO, PHOTOGRAPHY, PHOTOS, PHYSIO, PICS, PICTURES, PINK, PK, PL, PLACE, PLUMBING, PM, PN, POST, PR, PRESS, PRO,
	PRODUCTIONS, PROPERTIES, PS, PT, PUB, PW, PY, QA, QPON, QUEBEC, RE, RECIPES, RED, REHAB, REISE, REISEN, REN,
	RENTALS, REPAIR, REPORT, REPUBLICAN, REST, REVIEWS, RICH, RIO, RO, ROCKS, RODEO, RS, RU, RUHR, RW, RYUKYU, SA,
	SAARLAND, SB, SC, SCB, SCHMIDT, SCHULE, SCOT, SD, SE, SERVICES, SEXY, SG, SH, SHIKSHA, SHOES, SI, SINGLES, SJ, SK,
	SL, SM, SN, SO, SOCIAL, SOFTWARE, SOHU, SOLAR, SOLUTIONS, SOY, SPACE, SPIEGEL, SR, ST, SU, SUPPLIES, SUPPLY,
	SUPPORT, SURF, SURGERY, SUZUKI, SV, SX, SY, SYSTEMS, SZ, TATTOO, TAX, TC, TD, TECHNOLOGY, TEL, TF, TG, TH, TIENDA,
	TIPS, TIROL, TJ, TK, TL, TM, TN, TO, TODAY, TOKYO, TOOLS, TOWN, TOYS, TP, TR, TRADE, TRAINING, TRAVEL, TT, TV, TW,
	TZ, UA, UG, UK, UNIVERSITY, UNO, US, UY, UZ, VA, VACATIONS, VC, VE, VEGAS, VENTURES, VERSICHERUNG, VET, VG, VI,
	VIAJES, VILLAS, VISION, VLAANDEREN, VN, VODKA, VOTE, VOTING, VOTO, VOYAGE, VU, WANG, WATCH, WEBCAM, WEBSITE, WED,
	WF, WHOSWHO, WIEN, WIKI, WORKS, WS, WTC, WTF, XXX, XYZ, YACHTS, YANDEX, YE, YOKOHAMA, YT, ZA, ZM, ZONE, ZW
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
		return tlds.contains(StringTool.toLowerCase(tld));
	}
	
	public static String normalizeTld(String topLevelDomain) {
		if (StringTool.isEmpty(topLevelDomain)) {
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

		topLevelDomain = StringTool.enforceAlphabetic(topLevelDomain);

		return exists(topLevelDomain) ? topLevelDomain : null;
	}
	
	/** helpers ****************************************************/
	private static void generateEnum(PrintStream out) throws IOException {
		URL url = new URL("http://data.iana.org/TLD/tlds-alpha-by-domain.txt");
		URLConnection conn = url.openConnection();
		BufferedReader br = new BufferedReader(new InputStreamReader((InputStream) conn.getInputStream()));
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
