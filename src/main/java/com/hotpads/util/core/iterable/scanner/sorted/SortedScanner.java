package com.hotpads.util.core.iterable.scanner.sorted;

import com.hotpads.util.core.iterable.scanner.Scanner;

public interface SortedScanner<T extends Comparable<? super T>>
extends Scanner<T>, Comparable<SortedScanner<T>>{

}
