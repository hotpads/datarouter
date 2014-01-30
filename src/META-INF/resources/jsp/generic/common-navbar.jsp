<%@ include file="../generic/prelude-services.jspf"%>
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