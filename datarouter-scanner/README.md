# datarouter-scanner

A Scanner is similar to Java's Stream but targeted at common operations for working with databases. Datarouter uses 
scanners internally and often returns them so the application can chain more operations to them.

A Scanner can be converted to a single-use Iterable with `.iterable()` or to a Stream with `.stream()`.

## Installation with Maven

```xml
<dependency>
	<groupId>io.datarouter</groupId>
	<artifactId>datarouter-scanner</artifactId>
	<version>0.0.78</version>
</dependency>
```

## Features
##### - [Scanner methods](./src/main/java/io/datarouter/scanner/Scanner.java)

### Similarities to Stream

These methods share behavior with those in Stream but are implemented independently:
- `map`
- `distinct`
- `sort` (Stream `sorted`)
- `limit`
- `skip`
- `forEach`
- `reduce`
- `findMin` (Stream `min`)
- `findMax` (Stream `max`)
- `count`
- `anyMatch`
- `allMatch`
- `noneMatch`
- `findFirst`
- `empty`
- `of`
- `toArray`
- `concat`

### Differences from Stream

- Not built into java, so you must call `Scanner.of(something)` instead of `something.stream()`
- No primitive support
- Less overhead as there are fewer objects involved
- Less focused on behind-the-scenes parallelism for simplicity
  - Scanner is missing findAny() as it's equivalent to findFirst()
- More explicit parallelism on a step by step basis
  - Specify an executor and thread count for each parallel step

#### Additional terminal ops
- `hasAny` - return true when the first item is seen
- `isEmpty` - return true if the scanner completes without seeing any items
- `findLast` - returns `Optional<T>` with the last item, if any found
- `collect` - uses the `Supplier<Collection>` to create a collection, then `add`s each item to it
  - equivalent to `stream.collect(Collectors.toCollection(TreeSet::new))`
- `list` - collect all items to a `List`
  - equivalent to `stream.collect(Collectors.toList())`
- `listTo` - collect all items to a `List` and pass it to a `Function`
  - equivalent to `stream.collect(Collectors.collectingAndThen(Collectors.toList(), function))`
- `toMap` - collect all items to a `Map`.  By default existing values will be overwritten
  - `keyFunction` - required to extract the map key
- `groupBy` - collect all items to a `Map` where each value is a `Collection`, with 4 variants
  - `keyFunction` - required to extract the map key
  - `valueFunction` - optionally transform each item before collecting in the map
  - `mapSupplier` - optional `Supplier<Map>` to replace the default `HashMap::new`
  - `collectionSupplier` - optional `Supplier<Collection>` to replace the default `ArrayList::new`

#### Accepting Consumer
- `each` - each item passed to a `Consumer`
  - unlike `Stream::peek` all items are guaranteed to be consumed
- `flush` - all items collected to a `List` and passed to a `Consumer`
  - the `Scanner` can be continued with the logic unchanged

#### Discard items based on Predicate
- `include` - keep items matching the `Predicate`
  - equivalent to `Stream::filter`
- `exclude` - discard items matching the `Predicate`
- `distinctBy` - remove items where the output of the function has already been seen
- `deduplicateConsecutive` - remove *consecutive* duplicates
  - as opposed to `distinct()` which removes all duplicates
- `deduplicateConsecutiveBy` - remove items where the function maps to the previously mapped value

#### Stop scanning based on Predicate
- `advanceUntil` - terminate the Scanner when the `Predicate` passes
- `advanceWhile` - terminate the Scanner when the `Predicate` fails
  - equivalent to `Stream::takeWhile`

#### Combining Scanners
- `concat`
  - similar to Stream's flatMap or concat
  - output the contents of the first scanner, followed by the second, third, etc
  - efficient, requiring no memory buffering
- `collate`
  - no equivalent in Stream
  - assuming the input scanners are sorted, merges them into a sorted output stream, useful for scanning partitioned tables
  - the first item of each scanner must be in memory (sometimes triggering a batch of items loaded into memory), potentially making this expensive with many input scanners

#### Other
- `take` - collect N items to a List
- `batch` - convert `Scanner<T>` to `Scanner<List<T>>` with batch size N
- `sample` - return every Nth item
- `retain` - convert `Scanner<T>` to `Scanner<RetainingGroup<T>>` which gives access to the previous N items
- `prefetch` - load the next N items using the provided `ExecutorService`
- `shuffle` - collect the items internally and randomly select one of the remaining items on each `advance()`
- `splitBy` - split `Scanner<T>` into `Scanner<Scanner<T>>` based on the provided mapper `Function<T,R>`
- `apply` - Apply the provided Function which returns another Scanner.  The returned Scanner is responsible for consuming the input Scanner.
- `then` - pass the Scanner to a method that accepts it, and invoke the method.  The method is responsible for terminating the Scanner.

### Collectors

`Scanner` supports Java's comprehensive `Collector` library by internally converting to `Stream` before collecting.

### ParallelScanner
##### - [source code](./src/main/java/io/datarouter/scanner/ParallelScanner.java)
Calling `.parallel(..)` returns a `ParallelScanner` which executes the next operation in an executor, useful when the operations are CPU or IO intensive. It has these methods:
- `map`
- `exclude`
- `include`
- `each`
- `forEach`

The argument passed to parallel(..) is a `ParallelScannerContext` which contains:
- an executor service
- `boolean enabled`
  - for toggling parallelism with a feature flag
- `int maxThreads`
  - constrains threads used by this scanner, despite a potentially larger executor
- `boolean allowUnorderedResults`
  - return items in the order they finish processing, which might be different than the original order, potentially
    avoiding stalling other threads on slow items

## License

This library is licensed under the Apache License, Version 2.0 - see [LICENSE](../LICENSE) for details.
