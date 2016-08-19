<%@ include file="/WEB-INF/prelude.jspf"%>
<!DOCTYPE html>
<html>
<head>
	<title>${title}</title>
	<%@ include file="/jsp/generic/datarouterHead.jsp" %>
</head>
<body>
	<%@ include file="/jsp/menu/common-navbar.jsp" %>
	<%@ include file="/jsp/menu/dr-navbar.jsp"%>
	<div class="container">${contentJSP}</div>
</body>