<%@ include file="/jsp/generic/prelude.jspf"%>
<!DOCTYPE html>
<html>
<head>
	<title>${user.username} - Reset Password</title>
	<%@ include file="/jsp/generic/baseHead.jsp"%>
</head>
<body>
	<%@ include file="/jsp/menu/common-navbar.jsp" %>
	<div class="container">
		<h2>Admin: Reset Password</h2>
		<c:choose>
			<c:when test="${enabled == true}">
				<form method="POST" action="${contextPath}${authenticationConfig.resetPasswordSubmitPath}">
				<input type="hidden" name="${authenticationConfig.userIdParam}" value="${user.id}"/>
				<table>
					<tr>
						<td>Enter new password for user ${user.username}</td>
					</tr>
					<tr>
						<td>
							<input type="password" name="${authenticationConfig.passwordParam}"/>
						</td>
					</tr>
					<tr>
						<td colspan="2"><input type="submit"/></td>
					</tr>
				</table>
				</form>
			</c:when>
			<c:otherwise>
				<h3>This user is externally authenticated and cannot have a password.</h3>
			</c:otherwise>
		</c:choose>
	</div>
</body>
</html>
