<%@ include file="/jsp/generic/prelude-datarouter.jspf"%>
<html>
<head>
<title>Sign in</title>
<%@ include file="/jsp/generic/head.jsp" %>
<%@ include file="/jsp/css/css-import.jspf" %>
<script type="text/javascript" data-main="${contextPath}/js/core-common" src="${contextPath}/js/require-jquery.js"></script>
<style>
</style>
</head>
<body>
	<div class="wide-container">
		<h2>Sign in</h2>
		<form method="POST" action="${contextPath}${authenticationConfig.signinSubmitPath}"/>
			username:<input name="${authenticationConfig.usernameParam}" value="${param[authenticationConfig.usernameParam]}"/><br/>
			password:<input type="password" name="${authenticationConfig.passwordParam}"/><br/>
			<input type="submit"/>
		</form>
	</div>
	<script type="text/javascript">
		require([ "bootstrap/bootstrap" ], function() {});
	</script>
</body>
</html>