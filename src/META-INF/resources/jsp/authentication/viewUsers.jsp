<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/prelude.jspf"%>
<html>
<head>
<title>Reputation</title>
<base href="${contextPath}"/>
<%@ include file="/WEB-INF/jsp/generic/head.jsp"%> 
<%@ include file="/jsp/css/css-import.jspf" %>
</head>
<body>
<%@ include file="/jsp/menu/common-navbar.jsp" %>
<%@ include file="/WEB-INF/jsp/menu/reputation-navbar.jsp"%>
<div class="container">
	<table>
		<c:forEach var="user" items="${userList}">
			<c:set var="userId" value="${user.id}"/>
			<tr>
				<td>
					<table class="datarouterUser">
						<tr>
							<th colspan="2">
								<h4>
									<a href="${contextPath}/admin/editUser?${authenticationConfig.userIdParam}=${userId}">${user.username}</a>
								</h4>
							</th>
						</tr>
						<tr>
							<td>ID:</td>
							<td>${userId}</td>
						</tr>
						<tr>
							<td>User token:</td>
							<td>${user.userToken}</td>
						</tr>
						<tr>
							<td>Enabled:</td>
							<td>${user.isEnabled()}</td>
						</tr>
						<tr>
							<td>Last sign in:</td>
							<td>${user.lastLoggedIn}</td>
						</tr>
						<tr>
							<td>Roles:</td>
							<td>${user.roles}</td>
						</tr>
					</table>
				</td>
			</tr>
		</c:forEach>
	</table>
</div>
</body>
</html>