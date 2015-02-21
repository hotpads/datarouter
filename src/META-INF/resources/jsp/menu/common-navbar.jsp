<%@page import="com.hotpads.handler.LocalWebAppNamesLoader"%>
<%@ include file="../generic/prelude-datarouter.jspf"%>
<c:if test="${isAdmin || requestScope.datarouterSession.datarouterAdmin}">
	<c:set var="webApps" value="<%= ((LocalWebAppNamesLoader) getServletContext().getAttribute(
			LocalWebAppNamesLoader.SERVLET_CONTEXT_ATTRIBUTE_NAME)).getTomcatWebApps() %>" />
	<div id="generic-navbar">
		<ul>
			<c:forEach var="webApp" items="${webApps}" varStatus="loop">
				<li id="common-menu-${webApp.key}">
					<a href="${webApp.value}">${webApp.key}</a>
				</li>
			</c:forEach>
			<li>|</li>
			<a href="${contextPath}/datarouter">Datarouter</a>
		</ul>
	</div>
	<script type="text/javascript">
		$(document).ready(function() {
			var context = "${contextPath}";
			context = context.replace("/", "");
			$("#common-menu-" + context).find("a").addClass("underline");
			if (location.href.indexOf(context + "/datarouter") > -1) {
				$('#common-menu-datarouter').find("a").addClass("underline");
			}
		});
	</script>
</c:if>