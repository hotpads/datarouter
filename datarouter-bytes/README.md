# datarouter-bytes

datarouter-bytes includes utility classes for serializing objects to bytes.

## Installation with Maven

```xml
<dependency>
	<groupId>io.datarouter</groupId>
	<artifactId>datarouter-bytes</artifactId>
	<version>0.0.126</version>
</dependency>
```

## Codec
Each serialization format is constructed as a "codec" with encode and decode methods.  For example, the `RawIntCodec` has these methods:

Encode
```
public int encode(int value, byte[] bytes, int offset){
	bytes[offset] = (byte) (value >>> 24);
	bytes[offset + 1] = (byte) (value >>> 16);
	bytes[offset + 2] = (byte) (value >>> 8);
	bytes[offset + 3] = (byte) value;
	return LENGTH;
}
```

Decode
```
public int decode(byte[] bytes, int offset){
	return (bytes[offset] & 0xff) << 24
			| (bytes[offset + 1] & 0xff) << 16
			| (bytes[offset + 2] & 0xff) << 8
			| bytes[offset + 3] & 0xff;
}
```

These can be combined to encode primitives, arrays, or compound objects to bytes for storage in files, caches, or
databases with binary interfaces.

## Comparable
Many of the encoders support "comparable" encodings, where bits are flipped or escaped so that an unsigned byte
comparison will match the comparison of the original data type.  For example `ComparableIntCodec` will flip the
first bit so that positive ints will sort after negative ints in a lexicographic comparison, such as with `Arrays.compareUnsigned`.

Encode
```
public int encode(int value, byte[] bytes, int offset){
	int shifted = value ^ Integer.MIN_VALUE;
	return RAW_CODEC.encode(shifted, bytes, offset);
}
```

Decode
```
public int decode(byte[] bytes, int offset){
	return Integer.MIN_VALUE ^ RAW_CODEC.decode(bytes, offset);
}
```

## License

This library is licensed under the Apache License, Version 2.0 - see [LICENSE](../LICENSE) for details.
