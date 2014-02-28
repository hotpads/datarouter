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
			<c:set var="userId" value="${user.getId()}"/>
			<tr>
				<td>
					<table class="datarouterUser">
						<tr>
							<th colspan="2">
								<h4>
									<a href="${contextPath}/admin/editUser?userId=${userId}">${user.getUsername()}</a>
								</h4>
							</th>
						</tr>
						<tr>
							<td>ID:</td>
							<td>${userId}</td>
						</tr>
						<tr>
							<td>User token:</td>
							<td>${user.getUserToken()}</td>
						</tr>
						<tr>
							<td>Enabled:</td>
							<td>${user.isEnabled()}</td>
						</tr>
						<tr>
							<td>Last sign in:</td>
							<td>${user.getLastLoggedIn()}</td>
						</tr>
						<tr>
							<td>Roles:</td>
							<td>${user.getRoles()}</td>
						</tr>
					</table>
				</td>
			</tr>
		</c:forEach>
	</table>
</div>
</body>
</html>