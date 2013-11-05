<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ include file="/jsp/generic/prelude.jspf"%>
<!DOCTYPE html>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>Error processing request</title>
<c:import url="/jsp/css/css-import.jsp" />
</head>
<body>
	<div class="container on-top">
		<div class="alert alert-danger">We're sorry, but there was an error processing your request.</div>
		<pre>
<b>HTTP error code:</b> ${statusCode}
<c:if test="${not empty message}">
${message}
</c:if>
<c:if test="${not empty stackTraceString}">
<b>stack trace:</b>
${stackTraceString}
</c:if>
</pre>
	</div>
</body>
</html>