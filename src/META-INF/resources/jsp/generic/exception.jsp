<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="ISO-8859-1"%>
<!DOCTYPE html>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>Error processing request</title>
</head>
<body>
	<pre>
We're sorry, but there was an error processing your request.
test
HTTP error code: ${statusCode}

<c:if test="${not empty message}">
${message}
</c:if>

<c:if test="${not empty stackTraceString}">
stack trace:

${stackTraceString}
</c:if>

</pre>
</body>
</html>