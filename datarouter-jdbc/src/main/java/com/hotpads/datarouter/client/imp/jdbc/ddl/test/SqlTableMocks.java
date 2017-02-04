package com.hotpads.datarouter.client.imp.jdbc.ddl.test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.MySqlCharacterSet;
import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.MySqlCollation;
import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.MySqlColumnType;
import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.MySqlRowFormat;
import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.MySqlTableEngine;
import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.SqlColumn;
import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.SqlIndex;
import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.SqlTable;
import com.hotpads.datarouter.client.imp.jdbc.ddl.generate.SqlTableDiffGenerator;

public class SqlTableMocks{

	// columns
	public static final SqlColumn KWILU = new SqlColumn("kwilu", MySqlColumnType.VARCHAR, 255, false, false);
	public static final SqlColumn KWILU_TEXT = new SqlColumn("kwilu", MySqlColumnType.TEXT);
	public static final SqlColumn SAFARI = new SqlColumn("safari", MySqlColumnType.VARCHAR, 255, false, false);
	public static final SqlColumn CHARRETTE = new SqlColumn("charrette", MySqlColumnType.VARCHAR, 255, false, false);
	public static final SqlColumn SAVANNA = new SqlColumn("savanna", MySqlColumnType.TEXT, 0, false, false,
			MySqlCharacterSet.utf8, MySqlCollation.utf8_unicode_ci);
	public static final SqlColumn SAVANNA_BIN = new SqlColumn("savanna", MySqlColumnType.TEXT, 0, false, false,
			MySqlCharacterSet.utf8, MySqlCollation.utf8_bin);
	public static final SqlColumn SAVANNA_LATIN = new SqlColumn("savanna", MySqlColumnType.TEXT, 0, false, false,
			MySqlCharacterSet.latin1, MySqlCollation.latin1_bin);
	public static final SqlColumn VIDZAR = new SqlColumn("vidzar", MySqlColumnType.BIGINT, 8, false, true);

	// indexes
	public static final SqlIndex BOMBORA = new SqlIndex("Bombora", Arrays.asList(SAFARI));
	public static final SqlIndex COORANBONG = new SqlIndex("Cooranbong", Arrays.asList(CHARRETTE));
	public static final SqlIndex DOWNUNDER = new SqlIndex("Downunder", Arrays.asList(VIDZAR, KWILU));

	// tables
	public static final SqlTable ABERFELDY = new SqlTable("Aberfeldy",
			new SqlIndex("PRIMARY", Arrays.asList(KWILU)),
			Arrays.asList(KWILU, SAFARI, SAVANNA),
			new HashSet<>(Arrays.asList(BOMBORA)),
			Collections.emptySet(),
			MySqlCharacterSet.utf8mb4,
			MySqlCollation.utf8mb4_bin,
			MySqlRowFormat.DYNAMIC,
			MySqlTableEngine.INNODB);
	public static final SqlTable ABERLOUR = new SqlTable("Aberlour",
			new SqlIndex("PRIMARY", Arrays.asList(CHARRETTE)),
			Arrays.asList(KWILU_TEXT, CHARRETTE, SAVANNA_BIN),
			Collections.emptySet(),
			new HashSet<>(Arrays.asList(COORANBONG)),
			MySqlCharacterSet.latin1,
			MySqlCollation.latin1_bin,
			MySqlRowFormat.COMPACT,
			MySqlTableEngine.MYISAM);
	public static final SqlTable ABHAINN_DEARG = new SqlTable("AbhainnDearg",
			new SqlIndex("PRIMARY", Arrays.asList(KWILU)),
			Arrays.asList(KWILU, SAFARI, SAVANNA_LATIN),
			Collections.emptySet(),
			Collections.emptySet(),
			MySqlCharacterSet.utf8mb4,
			MySqlCollation.utf8mb4_bin,
			MySqlRowFormat.DYNAMIC,
			MySqlTableEngine.INNODB);
	public static final SqlTable ALISA_BAY = new SqlTable("AlisaBay",
			new SqlIndex("PRIMARY", Arrays.asList(KWILU)),
			Arrays.asList(KWILU, SAFARI, SAVANNA, CHARRETTE),
			new HashSet<>(Arrays.asList(BOMBORA)),
			Collections.emptySet(),
			MySqlCharacterSet.utf8mb4,
			MySqlCollation.utf8mb4_bin,
			MySqlRowFormat.DYNAMIC,
			MySqlTableEngine.INNODB);
	public static final SqlTable ALLT_A_BHAINNE = new SqlTable("AlltABhaine",
			new SqlIndex("PRIMARY", Arrays.asList(KWILU)),
			Arrays.asList(KWILU, SAFARI, SAVANNA),
			new HashSet<>(Arrays.asList(BOMBORA)),
			Collections.emptySet(),
			MySqlCharacterSet.utf8mb4,
			MySqlCollation.utf8mb4_bin,
			MySqlRowFormat.DYNAMIC,
			MySqlTableEngine.MYISAM);
	public static final SqlTable ANNANDALE = new SqlTable("Annandale",
			new SqlIndex("PRIMARY", Arrays.asList(KWILU)),
			Arrays.asList(KWILU, SAFARI, SAVANNA),
			Collections.emptySet(),
			Collections.emptySet(),
			MySqlCharacterSet.utf8mb4,
			MySqlCollation.utf8mb4_bin,
			MySqlRowFormat.DYNAMIC,
			MySqlTableEngine.INNODB);
	public static final SqlTable ARBIKIE = new SqlTable("Arbikie",
			new SqlIndex("PRIMARY", Arrays.asList(KWILU)),
			Arrays.asList(KWILU, SAFARI, SAVANNA),
			new HashSet<>(Arrays.asList(BOMBORA)),
			Collections.emptySet(),
			MySqlCharacterSet.latin1,
			MySqlCollation.latin1_bin,
			MySqlRowFormat.DYNAMIC,
			MySqlTableEngine.INNODB);
	public static final SqlTable ARDBEG = new SqlTable("Ardbeg",
			new SqlIndex("PRIMARY", Arrays.asList(KWILU)),
			Arrays.asList(KWILU, SAFARI, SAVANNA),
			new HashSet<>(Arrays.asList(BOMBORA)),
			Collections.emptySet(),
			MySqlCharacterSet.utf8mb4,
			MySqlCollation.utf8mb4_unicode_ci,
			MySqlRowFormat.DYNAMIC,
			MySqlTableEngine.INNODB);
	public static final SqlTable ARDMORE = new SqlTable("Ardmore",
			null,
			Arrays.asList(VIDZAR),
			Collections.emptySet(),
			Collections.emptySet(),
			MySqlCharacterSet.utf8mb4,
			MySqlCollation.utf8mb4_unicode_ci,
			MySqlRowFormat.DYNAMIC,
			MySqlTableEngine.INNODB);
	public static final SqlTable ARDNAMURCHAN = new SqlTable("Ardnamurchan",
			new SqlIndex("PRIMARY", Arrays.asList(KWILU, VIDZAR)),
			Arrays.asList(KWILU, VIDZAR, CHARRETTE),
			new HashSet<>(Arrays.asList(COORANBONG, DOWNUNDER)),
			new HashSet<>(Arrays.asList(DOWNUNDER)),
			MySqlCharacterSet.utf8mb4,
			MySqlCollation.utf8mb4_unicode_ci,
			MySqlRowFormat.DYNAMIC,
			MySqlTableEngine.INNODB);
	public static final SqlTableDiffGenerator DIFF_ABERFELDY_ABERLOUR = new SqlTableDiffGenerator(ABERFELDY, ABERLOUR);
	public static final SqlTableDiffGenerator DIFF_ABERLOUR_ABERFELDY = new SqlTableDiffGenerator(ABERLOUR, ABERFELDY);
	public static final SqlTableDiffGenerator DIFF_ABERLOUR_ABERLOUR = new SqlTableDiffGenerator(ABERLOUR, ABERLOUR);

}
