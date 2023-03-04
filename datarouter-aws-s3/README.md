# datarouter-aws-s3

datarouter-aws-s3 is a library that wraps the AWS SDK for Java, providing a simple interface to S3.
It bridges the two versions of the AWS SDK for Java by using the version 2 whenever possible, but also taking advantage of the `TransferManager` from version 1.
It supports automated resolution of bucket region.
It supports uploading using an `OutputStream` or `BufferedWriter` of unpredictable size.

## Installation with Maven

```xml
<dependency>
	<groupId>io.datarouter</groupId>
	<artifactId>datarouter-aws-s3</artifactId>
	<version>0.0.119</version>
</dependency>
```
## Usage
```java
import io.datarouter.aws.s3.BaseDatarouterS3Client;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;

public class MyDatarouterS3Client extends BaseDatarouterS3Client{

	public MyDatarouterS3Client(){
		super(StaticCredentialsProvider.create(AwsBasicCredentials.create("********************",
				"****************************************")));
	}

}
```

## License

This library is licensed under the Apache License, Version 2.0 - see [LICENSE](../LICENSE) for details.
