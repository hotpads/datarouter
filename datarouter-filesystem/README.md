# datarouter-filesystem

A client that implements the `BlobStorage` interface using the local filesystem.

## Installation with Maven

```xml
<dependency>
	<groupId>io.datarouter</groupId>
	<artifactId>datarouter-filesystem</artifactId>
	<version>0.0.107</version>
</dependency>
```

## Features

For single-node clusters that don't need to coordinate with other nodes, the filesystem is a simple place to store things:
 - no separate process to install, start, or stop
 - no reserved port
 - no security keys or passwords

Datarouter-filesystem helps simplify access to the filesystem by splitting configuration from usage.  In the 
configuration part of your app you create Node objects for the roots of your filesystems, and then create Directory 
objects for each subdirectory you'll be using.  In the logic part of your application you have simple Directory objects 
for reading, writing, and deleting files.

The myriad of Java's filesystem access options is simplified to working with byte arrays, InputStreams, and OutputStreams.

The `BlobStorage` interface is also implemented by `datarouter-aws-s3` and `datarouter-gcp-gcs`.  A `ClientId` can be configured
to use S3 or GCS in production and staging, while using the local filesystem on developer machines.  Note that it isn't a
great fit if you're using advanced features of those systems, but those advanced features can still be be accessed through
other code in the same app.
 
## Usage

You can configure a `BlobStorage` node for each "root" of the filesystem that you want to be visible inside an app.
For example, you might allocate a directory at `/var/local/datarouter` which would be configured like this:

```java
new FilesystemClientOptionsBuilder(MyClientIds.MY_CLIENT_ID)
		.withRoot("/var/local/datarouter");
```

After establishing each root of the filesystem with nodes, you can create `Directory` objects representing subdirectories
at any level:

```java
Directory root = new Directory(node);
Directory vegetables = root.subdirectory(new Subpath("vegetables"));
Directory lettuces = vegetables.subdirectory(new Subpath("lettuces"));
```

And then read, write, and delete byte arrays into the directories.
For example, adding a `spinach.txt` file to the `lettuces` directory:

```java
var key = PathbeanKey.of("spinach.txt");

String content = "Content that would normally reside in a spinach file.";
lettuces.write(key, content.getBytes());

String content2 = new String(lettuces.read(key));
Assert.assertEquals(content, content2);

lettuces.delete(key);
```

The parent directories `vegetables` and `lettuces` will be lazily created when files are added to them, which
is similar in behavior to S3 and GCS.  They'll appear nested in the Node's root:

```
/var/local/datarouter/vegetables
/var/local/datarouter/vegetables/lettuces
/var/local/datarouter/vegetables/lettuces/spinach.txt
```

You can also scan the filesystem, which will work similarly on S3 and GCS. 
This will delete any files stored under the `vegetables` directory.

```
vegetables.scanKeys(Subpath.empty())
		.forEach(vegetables::delete);
```

## License

This library is licensed under the Apache License, Version 2.0 - see [LICENSE](../LICENSE) for details.
