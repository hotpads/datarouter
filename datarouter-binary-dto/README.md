# datarouter-binary-dto

datarouter-binary-dto enables building nested DTOs that are serialized to a compact byte array format

## Installation with Maven

```xml
<dependency>
	<groupId>io.datarouter</groupId>
	<artifactId>datarouter-binary-dto</artifactId>
	<version>0.0.124</version>
</dependency>
```

## Overview
BinaryDtos serialize data to byte arrays.

Java applications often have a lot of Data Transfer Objects (Dtos) to pass data around internally
  and as a way of passing a unit of data to some serializer, for example a Json serializer.

Dtos are often encoded to Json.
While Json is human readable and supported by many languages,
  it's not the best serialization format for large volumes of data.
BinaryDtos are an alternative to Json.

### Performance
The encoding is compact, having either no overhead for each field, or a few metadata bytes.

Encoding is fast because encoding bytes is usually faster than converting to strings.

Decoding is fast 
  because the length of each field is prefixed to the field data,
  and decoding bytes is usually faster than than parsing strings.

### Formats
There are two encoding formats: Indexed and Comparable.

"Indexed" means that each Dto field is assigned a numeric index,
  and the index is prepended to each field in the encoded bytes.
It allows adding and removing fields of the Dto over time,
  while still being able to read data encoded by an older version of the code.
  
"Comparable" means that the encoded bytes of two Dtos can be compared lexicographically,
  using `Arrays::compareUnsigned`,
  without having access to the Dto code.
The built-in comparator of the `ComparableBinaryDto` will return the same result as comparing the raw bytes.
The trade-off is that the bytes are packed together without indexes,
  meaning you can't add or remove fields after the data is encoded.
  
### Primary motivation: Comparability
The primary motiviation for BinaryDtos was the Comparable encoding format.
It means you can pass the encoded dtos to other systems that know nothing about the dto code,
  and those systems can do interesting things with them.
  
For example, a generic system can collect billions of the encoded dtos, 
  sort them in chunks that fit in memory, 
  persist each chunk,
  merge the chunks in a streaming way,
  then give them back to you.
If you think of something like Hadoop Map/Reduce,
  it is primarily about emitting byte arrays
  and then shuffling, sorting, and merging them.

Because they are lightweight to create,
  you should be able to create a `ComparableBinaryDto` for each step of a large data transformation.
At each step of the transformation, you then have an object which you can sort and/or persist in a generic way.
  
The `ComparableBinaryDto` could be:
  - used as a database primary key in binary databases like BigTable or HBase (or even MySql).
  - used as a Hex-encoded primary key in any database with String fields.
  - used as a Hex-encoded S3 or GCS key for ordered object scanning.
  - fed to Spark, Flink, or Beam without them having to understand the Dto itself.

### External Caches
Because of their 
  lightweight definition format, 
  compact size, 
  quick encoding, 
  and flexible structure, 
  they are good for creating intermediate respresentations of data (views) 
  for storage in things like Memcached or Redis.
  
### In-Memory Caches
For cases where you want to cache a lot of data in heap memory
  you could convert your objects to byte arrays 
  and give those to your in-memory cache implementation.
This would remove the potentially substantial overhead of object references,
  reducing memory footprint (effectively increasing cache capacity),
  and reducing the number of objects that that the garbage collector needs to traverse in the heap.

### Simplicity
They are simple to define and use, written in normal java.

All configuration of the fields is done in each individual dto class.
This aims to avoid some of the confusion that can result from centralized serializer configuration.
For example Gson's TypeAdapter registration can become very confusing in a large, evolving code base.

### Caveats
BinaryDtos are not meant to be a cross-language data exchange mechanism.
They theoretically could be reproduced in other languages with a lot of work,
  but the main use case is for storing and processing data in Java.
  
With everything defined in Java in a large codebase,
  it is a little harder to see what is a persisted object
  compared with something like Protocol Buffers that has a different syntax and files.
Try to use good naming conventions and packaging to split serialization code from logic code.
Avoid putting non-trivial logic in the BinaryDtos,
  and avoid extending them to add logic.

The output format (bytes) is not easily human readable.
Converting the bytes to Hex makes it slightly more human readable.
Note there is a corresponding upside with this
  in that we don't need to worry about things like date formats.
  
Cycles in the data (circular references between objects) are not detected.
They'll cause the encoder to quickly allocate byte arrays leading to an OutOfMemory crash.
Because these are data transfer objects, 
  they are meant to model simple tree-structure data,
  not including complex patterns that could accidentally lead to cycles.
  
As of this writing, the encoding performance is not fully optimized, but it's still quite fast.
Each field is written to a byte array,
  and the byte arrays are then concatenated.
Each nested Dto is also temporarily encoded to a byte array before they are all concatenated.
So there can be a lot of temporary allocations while encoding.
Todo: optimize that.
  
### Java Records
BinaryDtos were made before Java Records, and unfortuantely are not Records.
So they don't have the succinct Record syntax nor automated accessor methods.

However they do have the benefits of built-in `hashCode`, `equals`, and `toString` methods.

And they have an advantage over records in that they correctly compare the *values* of array fields.

Further, `ComparableBinaryDto` automatically extends `Comparable`
  with a built-in `compareTo` method
  that matches the logic of using `Arrays::compareUnsigned` on the encoded bytes.

BinaryDto fields should be final,
  and there should not be setter methods.
For succinctness, fields can be made public,
  or record-style accessor methods can be added for MethodRef syntax in the callers.
  
A record-compatible serializer could potentially be added in the future
  after figuring out considerations with the features mentioned above.


## Details

### Field Types
- Can include:
    - Any primitive
    - Any primitive array
    - Any boxed primitive
    - Some familiar java types like String, Instant
    - Enums
    - Lists or arrays of the above
    
A field can also be another BinaryDto, or a list or array of nested BinaryDtos.
Therefore you can create complex data representations.
They should form a tree structure without links or cycles between objects.

Once you choose a boxed or unboxed primitive you cannot switch because the encoding changes.
An unboxed primitive is initialized to the default Java value if missing from the encoded bytes.
    
Note that more collections like `Map` are not supported.
The goal is to maintain explicit ordering of the items in the collections,
  with implementations like `HashMap` don't provide.
A map can be represented using a `List<MyKv>`,
  and converted to `Map<K,V>` at runtime using `Scanner.of(myKvs).toMap(MyKv::key, MyKv::value)`.
    
### Field Indexes
Field indexes can be specified using the `@BinaryDtoField(index = 3)` annotation.
This is needed because Java doesn't guarantee any ordering of fields.
They could theoretically change between different invocations of the JVM.

For "lightweight" situations where the ordering of the fields doesn't matter
  and you don't need strong backwards compatibility
  you can omit the annotations and indexes
  and the indexes will be assigned automatically based on alphabetical ordering of the fields.
Use caution because adding or removing fields later could shift the indexes of existing fields.

If you remove a field, it's recommended to leave a comment stating that the index was used previously.
Then future developers will know not to reuse that index, in case there is existing data serialized with it.
  
### Complex Field Types
Fields are encoded by subclasses of `BinaryDtoBaseFieldCodec`.  
You can extend this class to encode complex custom types.

If your custom type maps nicely to an existing type, there is a simpler `BinaryDtoConvertingFieldCodec`.
This would often be used with enums, converting them to `Integer` or `String`.

### Field Overhead
Metadata types:
- field length: varint
- field index: varint
- isNull: 1 byte
- collection size: varint

Variable length integer values 0 to 127 fit in a single byte.
Values up to 16,384 fit in 2 bytes.

Metadata fields are only applied as needed,
  for example primitive values don't have the isNull byte.

### Limits
The only size or nesting limitation is that the encoded bytes must fit in a 2 GiB byte[],
  though it's anticipated that normal Dtos are more in the 0.1 to 100 KiB range.
They'll fit nicely into things like SQS messages, Memcached objects, or database cells.
  
When you are dealing with a large amount of data,
  you can encode the dtos individually
  and chain them together into a stream by prefixing the length of each one.
In which case the limits become things like filesystem max file size or S3 max file size (5 TiB).


## Example
Let's look at a simple example of a grocery order containing multiple items.
We'll focus on the default "indexed" encoding.

### Step 1: Data Model
```java
class GroceryOrderItem{
	Long id;
	String productCode;
	Integer quantity;
}
```

```java
enum GroceryOrderDeliveryType{
	DELIVERY,
	PICKUP;
}
```

```java
class GroceryOrder{
	Long id;
	String customerName;
	GroceryOrderDeliveryType deliveryType;
	List<GroceryOrderItem> items;
}
```

### Step 2: Real Classes
```java
public final class GroceryOrderItem{
	public final Long id;
	public final String productCode;
	public final Integer quantity;

	public GroceryOrderItem(
			Long id, 
			String productCode, 
			Integer quantity){
		this.id = id;
		this.productCode = productCode;
		this.quantity = quantity;
	}
}
```

```java
public enum GroceryOrderDeliveryType{
	DELIVERY,
	PICKUP;
}
```

```java
public final class GroceryOrder{
	public final Long id;
	public final String customerName;
	public final GroceryOrderDeliveryType deliveryType;
	public final List<GroceryOrderItem> items;

	public GroceryOrder(
			Long id,
			String customerName,
			GroceryOrderDeliveryType deliveryType,
			List<GroceryOrderItem> items){
		this.id = id;
		this.customerName = customerName;
		this.deliveryType = deliveryType;
		this.items = items;
	}
}
```

### Step 3: Extend BinaryDto
With the following we have a usable BinaryDto.
However it's assigning field indexes alphabetically which is a bit fragile.

```java
public final class GroceryOrderItem extends BinaryDto<GroceryOrderItem>{
	public final Long id;
	public final String productCode;
	public final Integer quantity;

	public GroceryOrderItem(
			Long id, 
			String productCode, 
			Integer quantity){
		this.id = id;
		this.productCode = productCode;
		this.quantity = quantity;
	}

	public static GroceryOrderItem decode(byte[] bytes){
		return BinaryDtoIndexedCodec.of(GroceryOrderItem.class).decode(bytes);
	}
}
```

```java
public enum GroceryOrderDeliveryType{
	DELIVERY,
	PICKUP;
}
```

```java
public final class GroceryOrder extends BinaryDto<GroceryOrder>{
	public final Long id;
	public final String customerName;
	public final GroceryOrderDeliveryType deliveryType;
	public final List<GroceryOrderItem> items;

	public GroceryOrder(
			Long id,
			String customerName,
			GroceryOrderDeliveryType deliveryType,
			List<GroceryOrderItem> items){
		this.id = id;
		this.customerName = customerName;
		this.deliveryType = deliveryType;
		this.items = items;
	}

	public static GroceryOrder decode(byte[] bytes){
		return BinaryDtoIndexedCodec.of(GroceryOrder.class).decode(bytes);
	}
}
```

### Step 4: Field Indexes
Adding field indexes allows us to add/remove fields later,
  keeping backwards compatibility with existing bytes that might live in a file, database, cache, or message.
  
```java
public final class GroceryOrderItem extends BinaryDto<GroceryOrderItem>{
	@BinaryDtoField(index = 0)
	public final Long id;
	@BinaryDtoField(index = 1)
	public final String productCode;
	@BinaryDtoField(index = 2)
	public final Integer quantity;

	public GroceryOrderItem(Long id, String productCode, Integer quantity){
		this.id = id;
		this.productCode = productCode;
		this.quantity = quantity;
	}

	public static GroceryOrderItem decode(byte[] bytes){
		return BinaryDtoIndexedCodec.of(GroceryOrderItem.class).decode(bytes);
	}
}
```

```java
public enum GroceryOrderDeliveryType{
	DELIVERY,
	PICKUP;
}
```

```java
public final class GroceryOrder extends BinaryDto<GroceryOrder>{
	@BinaryDtoField(index = 0)
	public final Long id;
	@BinaryDtoField(index = 1)
	public final String customerName;
	@BinaryDtoField(index = 2)
	public final GroceryOrderDeliveryType deliveryType;
	@BinaryDtoField(index = 3)
	public final List<GroceryOrderItem> items;

	public GroceryOrder(
			Long id,
			String customerName,
			GroceryOrderDeliveryType deliveryType,
			List<GroceryOrderItem> items){
		this.id = id;
		this.customerName = customerName;
		this.deliveryType = deliveryType;
		this.items = items;
	}

	public static GroceryOrder decode(byte[] bytes){
		return BinaryDtoIndexedCodec.of(GroceryOrder.class).decode(bytes);
	}
}
```

### Step 5: Make an Order
Assemble an order:

```java
List<GroceryOrderItem> items = List.of(
		new GroceryOrderItem(1L, "egg-dozen-3", 1),
		new GroceryOrderItem(2L, "pringles-sco", 6),
		new GroceryOrderItem(3L, "banana-2", 3));
var order = new GroceryOrder(
		55L,
		"Arthur",
		GroceryOrderDeliveryType.DELIVERY,
		items);
```

Encode and decode a single item:

```java
byte[] item0Bytes = items.get(0).encodeIndexed();
GroceryOrderItem item0Decoded = GroceryOrderItem.decode(item0Bytes);
```

Encode and decode the full order:

```java
byte[] orderBytes = order.encodeIndexed();
GroceryOrder orderDecoded = GroceryOrder.decode(orderBytes);
```

### Step 6: View the Encoded Bytes
```java
HexBlockTool.print(item0Bytes);
HexBlockTool.print(orderBytes);
```

Single item output:
```
##### hex start tabs=0 width=80 #####
00088000000000000001010b6567672d646f7a656e2d33020480000001
##### hex end #####
```

Full order output:
```
##### hex start tabs=0 width=80 #####
000880000000000000370106417274687572020844454c4956455259035c03011d00088000000000
000001010b6567672d646f7a656e2d33020480000001011e00088000000000000002010c7072696e
676c65732d73636f020480000006011a00088000000000000003010862616e616e612d3202048000
0003
##### hex end #####
```

## HexBlockTool
The `HexBlockTool` used above outputs Strings that are easy to paste into unit tests,
  to validate that you didn't accidentally break the definition of a dto.
  
```java
String orderHex = """
		000880000000000000370106417274687572020844454c4956455259035c03011d00088000000000
		000001010b6567672d646f7a656e2d33020480000001011e00088000000000000002010c7072696e
		676c65732d73636f020480000006011a00088000000000000003010862616e616e612d3202048000
		0003""";
GroceryOrder orderFromHex = GroceryOrder.decode(HexBlockTool.fromHexBlock(orderHex));
Assert.assertEquals(orderFromHex, order);
```

## "Converting" Field Codec
You can apply a codec to each field to control what is serialized.
Let's serialize the `GroceryOrderDeliveryType` enum to an int.
This will give us a potentially smaller encoding than the default `name()` String.
We can use a `BinaryDtoConvertingFieldCodec` that internally uses the built-in `IntBinaryDtoFieldCodec`.

Add an int mapping and codec definition. 
We'll put them in the enum for this example so they can be reused by other BinaryDtos.

```java
public enum GroceryOrderDeliveryType{
	DELIVERY(0),
	PICKUP(1);

	private int intValue;

	private GroceryOrderDeliveryType(int intValue){
		this.intValue = intValue;
	}

	public static final Map<Integer,GroceryOrderDeliveryType> BY_INT = Scanner.of(values())
			.toMap(value -> value.intValue);

	public static class GroceryOrderDeliveryTypeBinaryDtoIntCodec
	extends BinaryDtoConvertingFieldCodec<GroceryOrderDeliveryType,Integer>{
		public GroceryOrderDeliveryTypeBinaryDtoIntCodec(){
			super(value -> value.intValue, BY_INT::get, new IntBinaryDtoFieldCodec(), true);
		}
	}
}
```

Then specify the codec in the BinaryDto.

```java
public final class GroceryOrder extends BinaryDto<GroceryOrder>{
	...
	@BinaryDtoField(index = 2, codec = GroceryOrderDeliveryTypeBinaryDtoIntCodec.class)
	public final GroceryOrderDeliveryType deliveryType;
	...
```

Note that this changes the encoding, so the test above will now fail.


## License

This library is licensed under the Apache License, Version 2.0 - see [LICENSE](../LICENSE) for details.
