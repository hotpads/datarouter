package com.hotpads.datarouter.serialize.warnings;

/**
 * Annotation to warn that something accesses this class using reflection, so changing the package or class name may
 * break it.
 */
public @interface ClassNamePersisted{

}
