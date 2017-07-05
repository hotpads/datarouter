package io.datarouter.util.iterable.scanner.sorted;

import io.datarouter.util.iterable.scanner.Scanner;

public interface SortedScanner<T extends Comparable<? super T>> extends Scanner<T>, Comparable<SortedScanner<T>>{

}
