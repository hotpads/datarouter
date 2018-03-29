<c:if test="${isAdmin}">
	<nav class="navbar navbar-inverse navbar-static-top navbar-thin">
		<div class="container-fluid">
			<div class="navbar-header">
				<button type="button" class="navbar-toggle collapsed" data-toggle="collapse" 
						data-target="#common-navbar">
					<span class="icon-bar"></span>
					<span class="icon-bar"></span>
					<span class="icon-bar"></span>
				</button>
			</div>
			<div class="collapse navbar-collapse" id="common-navbar">
				<ul class="nav navbar-nav">
					<c:forEach var="webApp" items="${tomcatWebApps}" varStatus="loop">
						<li id="common-menu-${webApp.key}">
							<a href="${webApp.value}">${webApp.key}</a>
						</li>
					</c:forEach>
					<li class="divider-vertical"></li>
					<li id="common-menu-datarouter"><a href="${contextPath}/datarouter">datarouter</a></li>
				</ul>
			</div>
		</div> 
	</nav>
	<style>
		.navbar-thin{
			margin-bottom:0;
			min-height:0;
			border:0;
		}
		#common-navbar > ul > li > a{
			padding-top:1px;
			padding-bottom:1px;
		}
		.navbar .divider-vertical {
			height: 22px;
			margin: 0 9px;
			border-right: 1px solid #9d9d9d;
			border-left: 1px solid #9d9d9d;
		}
		@media (max-width: 767px) {
			.navbar-collapse .nav > .divider-vertical {
				display: none;
			}
		}
	</style>
	<script type="text/javascript">
	require(['jquery'], function($){
		$(document).ready(function() {
			var context = "${contextPath}";
			context = context.replace("/", "");
			$("#common-menu-" + context).find("a").addClass("underline");
			if (location.href.indexOf(context + "/datarouter") > -1) {
				$('#common-menu-datarouter').find("a").addClass("underline");
			}
		});
	});
	</script>
</c:if>