# datarouter-binary-dto

datarouter-binary-dto enables building nested DTOs that are serialized to a compact byte array format

## Installation with Maven

```xml
<dependency>
	<groupId>io.datarouter</groupId>
	<artifactId>datarouter-binary-dto</artifactId>
	<version>0.0.119</version>
</dependency>
```

## Features

A class extending `BinaryDto` or `ComparableBinaryDto`

- Can include:
    - Any primitive or boxed primitive
    - Familiar java types like String, Instant
    - Enums
    - Other BinaryDtos
    - Lists or Object arrays of the above
    - Any object when:
        - Either a simple `BinaryDtoConvertingFieldCodec` is implemented to convert a value to an existing `BinaryDtoBaseFieldCodec`
        - Or a more complicated `BinaryDtoBaseFieldCodec` is implemented for any custom value
- Any level of nesting is possible
- Primarily focused on fast and compact serialization
    - Not meant to form complex object oriented object graphs, only tree-structured data
    - Should be converted to other objects for complex program logic
    - Cycles are not detected, so encoding circular object graphs will quickly crash the application
- Can be converted to bytes and back to an object
    - `BinaryDto` encodes to a field-indexed format using `encode()` where each field is identified by a VarInt ID
      - Field indexes can be specified using the `@BinaryDtoField(index = 3)` annotation
          - The index adds one byte of overhead for the first 128 fields, 2+ bytes thereafter
      - Or by omitting annotations and relying on alphabetical ordering of the fields
          - Alphabetical ordering should only be used when fields will not be added or removed when dealing with persisted data
    - `ComparableBinaryDto` encodes to a concatenated, sortable format using `encodeComparable()` that matches the sorting of the encoded bytes via `Arrays::compareUnsigned`
- Object methods
    - `BinaryDto` subclasses automatically include `equals()` and `hashCode()`
    - `ComparableBinaryDto` subclasses also include `compareTo()` that sorts equivalently to `Arrays::compareUnsigned` on the encoded byte array


## License

This library is licensed under the Apache License, Version 2.0 - see [LICENSE](../LICENSE) for details.
