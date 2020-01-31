<%@ include file="/jsp/generic/prelude.jspf"%>
<!DOCTYPE html>
<html>
<head>
	<title>${user.username} - Reset Password</title>
	<%@ include file="/jsp/generic/baseHead-b4.jsp"%>
</head>
<body>
<%@ include file="/jsp/menu/common-navbar-b4.jsp" %>
<div class="container">
	<h2>Reset Password</h2>
	<c:choose>
		<c:when test="${enabled == true}">
		<div class="card card-body bg-light">
			<form method="POST" action="${contextPath}${resetPasswordSubmitPath}">
				<input type="hidden" name="${authenticationConfig.userIdParam}" value="${user.id}"/>
				<div class="form-group">
					<label for="password">Enter new password for user ${user.username}:</label>
					<input type="password" class="form-control" id="password" name="${authenticationConfig.passwordParam}" required />
				</div>
				<button type="submit" class="btn btn-primary">Submit</button>
			</form>
		</div>
		</c:when>
		<c:otherwise>
			<h3>This user is externally authenticated and cannot have a password.</h3>
		</c:otherwise>
	</c:choose>
</div>
</body>
</html>
