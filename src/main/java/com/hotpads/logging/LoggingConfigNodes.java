package com.hotpads.logging;

import com.hotpads.datarouter.node.op.combo.SortedMapStorage.SortedMapStorageNode;

public interface LoggingConfigNodes{

	SortedMapStorageNode<LoggerConfigKey,LoggerConfig> getLoggerConfig();

	SortedMapStorageNode<ConsoleAppenderKey,ConsoleAppender> getConsoleAppender();

	SortedMapStorageNode<FileAppenderKey,FileAppender> getFileAppender();

}
