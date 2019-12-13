# datarouter-scanner

A Scanner is similar to Java's Stream but targeted at common operations for working with databases. Datarouter uses scanners internally and often returns them so the application can chain more operations to them.

A Scanner can be converted to a single-use Iterable with `.iterable()` or to a Stream with `.stream()`.

## Installation with Maven

```xml
<dependency>
	<groupId>io.datarouter</groupId>
	<artifactId>datarouter-scanner</artifactId>
	<version>0.0.14</version>
</dependency>
```

## Features
##### - [Scanner methods](./src/main/java/io/datarouter/scanner/Scanner.java)

### Relying on Stream

These methods use Stream internally:
- `reduce`
- `collect`

### Similar to Stream

These methods share behavior with those in Stream but are implemented independently:
- `map`
- `distinct`
- `sorted`
- `peek`
- `limit`
- `skip`
- `forEach`
- `min`
- `max`
- `count`
- `anyMatch`
- `allMatch`
- `noneMatch`
- `findFirst`
- `findAny`
- `empty`
- `of`
- `toArray`

### Different from Stream

`Scanner` has these methods not available in Stream:
- `findLast`
- `hasAny`
- `isEmpty`
- `list`
- `take`
- `advanceUntil`
- `advanceWhile`
- `batch`
- `deduplicate`
- `exclude`
- `include`
- `prefetch`
- `step`
- `sample`
- `retain`

### ScannerScanner
##### - [source code](./src/main/java/io/datarouter/scanner/ScannerScanner.java)
To combine multiple scanners, calling `mapToScanner` will return a `ScannerScanner` with these methods:
- `concatenate`
  - similar to Stream's flatMap or concat
  - output the contents of the first scanner, followed by the second, third, etc
  - efficient, requiring no memory buffering
- `collate`
  - no equivalent in Stream
  - assuming the input scanners are sorted, merges them into a sorted output stream, useful for scanning partitioned tables
  - the first item of each scanner must be in memory (sometimes triggering a batch of items loaded into memory), potentially making this expensive with many input scanners

### ParallelScanner
##### - [source code](./src/main/java/io/datarouter/scanner/ParallelScanner.java)
Calling `.parallel(..)` returns a `ParallelScanner` which executes each operation in an executor, useful when the operations are CPU or IO intensive. It has these methods:
- `map`
- `exclude`
- `include`
- `peek`
- `forEach`

The argument passed to parallel(..) is a `ParallelScannerContext` which contains:
- an executor service
- `boolean enabled`
  - for toggling parallelism with a feature flag
- `int maxThreads`
  - constrains threads used by this scanner, despite a potentially larger executor
- `boolean allowUnorderedResults`
  - return items in the order they finish processing, which can be faster than waiting for the earliest submitted item

## License

This library is licensed under the Apache License, Version 2.0 - see [LICENSE](../LICENSE) for details.
