<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="/jsp/generic/prelude-datarouter.jspf"%>
<!DOCTYPE html >
<html lang="en">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Hotpads - Sign in</title>
<%@ include file="/jsp/css/css-import.jspf"%>
</head>
<body>
	<form class="form-horizontal" action="${contextPath}/signin/submit" method="post">
		<div id="login-box" class="well"> 
			<div class="control-group">
				<div class="controls login-controls">
					<input name="signinUsername" placeholder="Username" autofocus="autofocus" type="email" />
				</div>
			</div>
			<div class="control-group">
				<div class="controls login-controls">

					<input name="signinPassword" placeholder="Password" type="password" />

				</div>
			</div>
			<div class="control-group">
				<div class="controls login-controls ">
					<label for="_acegi_security_remember_me" class="checkbox">
						<input type="checkbox" name="_acegi_security_remember_me" checked="checked"> Remember me
					</label>
					<button type="submit" class="btn btn-primary">Sign in</button>
				</div>
			</div>
		</div>
	</form>
</body>
</html>