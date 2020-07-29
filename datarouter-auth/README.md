# datarouter-auth

datarouter-auth is a framework that brings in users and apikey accounts to datarouter.

## Installation with Maven

```xml
<dependency>
	<groupId>io.datarouter</groupId>
	<artifactId>datarouter-auth</artifactId>
	<version>0.0.42</version>
</dependency>
```

## Installation with Datarouter

You can install this module by adding its plugin to the `WebappBuilder`.

```java
.addJobPlugin(new DatarouterAuthPluginBuilder(...)
		...
		.build()
```

## DatarouterSession

A single DatarouterUser may have multiple sessions via different computers, browsers, tabs, etc.  A DatarouterSession
 is created for each such session so that users can stay logged in and so the application is aware of the user.

As a DatarouterUser is authenticated into the application, their userToken is recorded in their DatarouterSession.
This allows for some useful features like the ability of the service to get the current user that's viewing a page
 and show data based on what their DatarouterUser allows.  


## DatarouterUser

`DatarouterUser` is a useful part of the datarouter framework that allows users to interact with a service with a login
 system.  Each DatarouterUser has a userId, username, userToken, passwordSalt, passwordDigest, amongst other metadata
 fields. 

When the application runs for the first time, a default user is created with a default userId and password.  The
 username is the same email address as the DatarouterPropeties's datarouterAdministrator. Additional DatarouterUsers
 can be created through the CreateUser UI.

### Roles

Datarouter-auth brings in the ability attribute RouteSet classes with specific roles to control access.  Roles can be
 set for the whole RouteSet class as a default, and specific paths within the RouteSet class can be given additional
 Roles. 

Datarouter brings a default set of users, specified in `DatarouterUserRole`.
- Admin
- Api-User
- Datarouter-Admin
- Datarouter-Job
- Datarouter-Monitoring
- Datarouter-Settings
- Datarouter-Tools
- Doc-User
- Requestor
- User

### Example

```java
import javax.inject.Singleton;

import io.datarouter.web.dispatcher.BaseRouteSet;
import io.datarouter.web.dispatcher.DispatchRule;
import io.datarouter.web.user.role.DatarouterUserRole;

@Singleton
public class AuthExampleUserRouteSet extends BaseRouteSet{

	public AuthExampleUserRouteSet(){
		super("");

		handleDir("/helloWorld")
				.withHandler(AuthHellowWorldHandler.class)
				.allowRoles(DatarouterUserRole.USER);
		handleDir("/docs")
				.withHandler(AuthExampleDocHandler.class)
				.allowRoles(DatarouterUserRole.DOC_USER);
	}

	// Users with the datarouter-admin role can view all the paths registered in this RouteSet
	@Override
	protected DispatchRule applyDefault(DispatchRule rule){
		return rule.allowRoles(DatarouterUserRole.DATAROUTER_ADMIN);
	}

}
```

If a user doesn't have the necessary permissions to a route, they will be redirected to a permission request page.
Users can be updated by anyone with the `datarouter-admin` or `admin` roles and are configured through the
 `AdminEditUserHandler`. When users request permission, an email is sent to the requestor, the datarouter administrator
 and any other supplemented permission request email address. 


### DatarouterAccount

DatarouterAccounts is a tool that allows external services to interact with the application's api in a secure manner.
 A `DatarouterAccount` contains an accountName, apiKey, secretKey.  Each `DatarouterAccount` is mapped to a
 `DatarouterAccountPermission` which contains a list of regex paths that the DatarouterAccount has permission to access.
 The list of possible paths can be set when defining a path and the handler in the RouteSet class, using the
 `withPersistentString` option.  There is also the default path "all" which allows access to all paths registered in
 the RouteSet. 

DatarouterAccounts are created and managed through the `DatarouterAccountManagerHandler`.

```java
import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.auth.service.DatarouterAccountApiKeyPredicate;
import io.datarouter.web.dispatcher.BaseRouteSet;
import io.datarouter.web.dispatcher.DispatchRule;
import io.datarouter.web.handler.TestApiHandler;

@Singleton
public class AuthExampleAccountRouteSet extends BaseRouteSet{

	private final DatarouterAccountApiKeyPredicate datarouterAccountApiKeyPredicate;

	@Inject
	public AuthExampleAccountRouteSet(DatarouterAccountApiKeyPredicate datarouterAccountApiKeyPredicate){
		super("");
		this.datarouterAccountApiKeyPredicate = datarouterAccountApiKeyPredicate;

		handleDir("/testApi")
				.withHandler(TestApiHandler.class)
				.withPersistentString("Test Path");
	}

	@Override
	protected DispatchRule applyDefault(DispatchRule rule){
		return rule.allowAnonymous()
				.withApiKey(datarouterAccountApiKeyPredicate);
	}

}
```

## DatarouterUserAccountMapping
Datarouter provides the option to map DatarouterUsers to DatarouterAccounts. To enable user-mappings for an account,
 the `enableUserMappings` flag needs to be set to true, which is configured on the AccountManger UI. DatarouterUsers
 can be assigned any accounts that allow user-mappings, through the EditUser UI.

For Example:
This can be used to control access for multiple services that interact with a single service to control user tool and
 api access.  Data can be stored with the accountName(which is associated with each request) and the web tools that
 interact with the data can display the data for all the accounts that the user has access to.


## Documentation
Datarouter adds the ability to automatically generate api documentation on the Docs UI for RouteSet classes that
 implement `DocumentationRouteSet`.  The Docs UI shows each handler method with the required request parameters and
 an example response.


## License

This library is licensed under the Apache License, Version 2.0 - see [LICENSE](../LICENSE) for details.
