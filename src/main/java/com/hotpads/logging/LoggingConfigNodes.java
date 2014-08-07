package com.hotpads.logging;

import com.hotpads.datarouter.node.op.combo.SortedMapStorage.SortedMapStorageNode;
import com.hotpads.logging.databean.ConsoleAppender;
import com.hotpads.logging.databean.ConsoleAppenderKey;
import com.hotpads.logging.databean.FileAppender;
import com.hotpads.logging.databean.FileAppenderKey;
import com.hotpads.logging.databean.LoggerConfig;
import com.hotpads.logging.databean.LoggerConfigKey;

public interface LoggingConfigNodes{

	SortedMapStorageNode<LoggerConfigKey,LoggerConfig> getLoggerConfig();

	SortedMapStorageNode<ConsoleAppenderKey,ConsoleAppender> getConsoleAppender();

	SortedMapStorageNode<FileAppenderKey,FileAppender> getFileAppender();

}
