<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="/jsp/generic/prelude-datarouter.jspf"%>
<html>
<head>
<title>${user.username} - Reset Password</title>
<%@ include file="/jsp/generic/head.jsp" %>
<%@ include file="/jsp/css/css-import.jspf" %>
</head>
<body>
<%@ include file="/jsp/menu/common-navbar.jsp" %>
<%@ include file="/WEB-INF/jsp/menu/reputation-navbar.jsp"%>
	<div class="container">
		<h2>Admin: Reset Password</h2>
		<form method="POST" action="${contextPath}${authenticationConfig.resetPasswordSubmitPath}">
		<input type="hidden" name="${authenticationConfig.userIdParam}" value="${user.id}"/>
		
		<table>
			<tr>
				<td>Enter new password for user ${user.username}</td>
			</tr>
			<tr>
				<td>
					<input name="${authenticationConfig.passwordParam}"/>
				</td>
			</tr>
			<tr>
				<td colspan="2"><input type="submit"/></td>
			</tr>
		</table>

		</form>
	</div>
</body>
</html>