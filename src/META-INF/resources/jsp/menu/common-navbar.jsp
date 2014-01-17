<%@ include file="../generic/prelude-datarouter.jspf"%>
<!-- This jspf displays only the webapps currently deployed on this server. -->
<%-- The HTML code '${commonNavbarHtml}' is added to the servletContext in --%>
<!-- com.hotpads.handler.AbstractLocalWebbapps. To make it works, You need to -->
<!-- extends this class and override the method you need/want. This subclass -->
<!-- needs to be declared as listener in your web.xml. You also need to bind -->
<!-- AbstractLocalWebbapps to your subclass in one of your module. -->
<c:set var="commonNavbarHtml"
	value="<%=getServletContext().getAttribute("commonNavbarHtml")%>"
	scope="request" />
<c:if test="${isAdmin}">
	<div id="generic-navbar">${commonNavbarHtml}</div>
	<script type="text/javascript">
		$(document).ready(function() {
			var url = window.location.href;
			var context = "${contextPath}";
			context = context.replace("/", "");
			$("#common-menu-" + context).addClass("underline");
		});

		$('.isNotLocal')
				.click(
						function(event) {
							event.preventDefault();
							var r = confirm("Are you sure you want to be redirected to \n"
									+ $(this).attr("href"));
							if (r == true) {
								window.location = $(this).attr('href');
							}
						});
	</script>
</c:if>