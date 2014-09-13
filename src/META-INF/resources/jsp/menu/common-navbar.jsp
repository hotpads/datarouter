<%@ include file="../generic/prelude-datarouter.jspf"%>
<%/*
This jspf displays the webapps currently deployed on this server.
The HTML code '${commonNavbarHtml}' is added to the servletContext in
com.hotpads.handler.BaseLocalWebbapps. See references to and subclasses of 
BaseLocalWebapps for example usage.
*/%>
<c:set var="commonNavbarHtml"
	value='<%=getServletContext().getAttribute("commonNavbarHtml")%>'
	scope="request" />
<c:if test="${isAdmin || requestScope.datarouterSession.datarouterAdmin}">
	<div id="generic-navbar">${commonNavbarHtml}</div>
	<script type="text/javascript">
		$(document).ready(function() {
			var context = "${contextPath}";
			context = context.replace("/", "");
			$("#common-menu-" + context).addClass("underline");
			if (location.href.indexOf(context + "/datarouter") > -1) {
				$('#common-menu-datarouter').addClass("underline");
			}
		});
		$('.isNotLocal').click(function(event) {
			event.preventDefault();
			var r = confirm("Are you sure you want to be redirected to \n"
					+ $(this).attr("href"));
			if (r == true) {
				location = $(this).attr('href');
			}
		});
	</script>
</c:if>