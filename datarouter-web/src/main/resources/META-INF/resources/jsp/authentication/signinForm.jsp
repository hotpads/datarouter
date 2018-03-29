<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="/jsp/generic/prelude.jspf"%>
<!DOCTYPE html>
<html lang="en">
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
	<title>Datarouter - Sign in</title>
	<%--TODO possible to include a head here, instead of this stuff? --%>
	<%@ include file="/jsp/css/css-import.jspf"%>
</head>
<body>
	<form action="${contextPath}${authenticationConfig.signinSubmitPath}" method="post" id="user-form">
		<div id="login-box" class="well"> 
			<div class="control-group">
				<div class="controls login-controls">
					<input name="${authenticationConfig.usernameParam}" placeholder="Username" autofocus="autofocus" type="email" />
				</div>
			</div>
			<div class="control-group">
				<div class="controls login-controls">
					<input name="${authenticationConfig.passwordParam}" placeholder="Password" type="password" />
				</div>
			</div>
			<div class="control-group">
				<div class="controls login-controls ">
					<button type="submit" id="user-submit" class="btn btn-primary">Sign in</button>
				</div>
			</div>
		</div>
	</form>
</body>
</html>