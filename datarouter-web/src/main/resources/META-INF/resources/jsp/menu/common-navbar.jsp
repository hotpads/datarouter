<c:if test="${isAdmin}">
	<c:if test="${isProduction}">
		<c:set var="productionCssClass" value="productionEnv"/>
	</c:if>
	<nav class="navbar navbar-inverse navbar-static-top navbar-thin ${productionCssClass}">
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
					<c:forEach var="webApp" items="${tomcatWebApps}">
						<li id="common-menu-${webApp.key}">
							<a href="${webApp.value}">${webApp.key}</a>
						</li>
					</c:forEach>
					<li class="divider-vertical"></li>
					<li id="common-menu-datarouter"><a href="${contextPath}/datarouter">datarouter</a></li>
					<li class="divider-vertical"></li>
					<li>
						<a href="${traceUrl}">
						j:${durationString}
						r:<span id="requestTiming"></span>
						c:<span id="clientTiming"></span>
						</a>
					</li>
				</ul>
			</div>
		</div> 
	</nav>
	<style>
		.productionEnv{
			background-color: #ff4d4d;
		}
		.productionEnv .navbar-nav > li > a{
			color: #ffffff;
		}
		.navbar-thin{
			margin-bottom: 0;
			min-height: 0;
			border: 0;
		}
		#common-navbar > ul > li > a{
			padding-top: 1px;
			padding-bottom: 1px;
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
		const navPerformance = performance.getEntriesByType("navigation")[0]
		const roundedMs = duration => Math.round(duration) + "ms"
		$('#requestTiming').text(roundedMs(navPerformance.responseEnd))

		$(document).ready(function() {
			var context = "${contextPath}";
			context = context.replace("/", "");
			$("#common-menu-" + context).find("a").addClass("underline");
			if (location.href.indexOf(context + "/datarouter") > -1) {
				$('#common-menu-datarouter').find("a").addClass("underline");
			}

			const checkLoadEndInterval = setInterval(() => {
				if(navPerformance.loadEventEnd === 0){
					return
				}
				clearInterval(checkLoadEndInterval)
				$('#clientTiming').text(roundedMs(navPerformance.loadEventEnd - navPerformance.responseEnd))
			}, 100)
		});
	});
	</script>
</c:if>
<c:set var="navBarType" value="${isDatarouterPage ? datarouterNavBar : webAppNavBar}" />
<c:set var="navBarTarget" value="${isDatarouterPage ? 'dr-navbar' : 'mav-navbar'}" />
<div class="navbar navbar-inverse navbar-static-top">
	<div class="container-fluid">
		<div class="navbar-header">
			<button type="button" class="navbar-toggle collapsed" data-toggle="collapse" data-target="#${navBarTarget}">
				<span class="icon-bar"></span>
				<span class="icon-bar"></span>
				<span class="icon-bar"></span>
			</button>
			<c:if test="${isDatarouterPage}">
				<a class="navbar-brand" href="#">
					<img alt="${navBarType.logoAlt}" src="${contextPath}/${navBarType.logoSrc}" class="logo-brand" onclick="return false">
				</a>
			</c:if>
		</div>
		<c:set var="request" value="<%= request %>" />
		<div class="navbar-collapse collapse" id="${navBarTarget}">
			<ul class="nav navbar-nav">
				<c:forEach items="${navBarType.getMenuItems(request)}" var="menuItem">
					<c:choose>
						<c:when test="${menuItem.isDropdown()}">
							<li class="dropdown">
								<a href="#" class="dropdown-toggle" data-toggle="dropdown">${menuItem.text} <span class="caret"></span></a>
								<ul class="dropdown-menu">
									<c:forEach items="${menuItem.getSubItems(request)}" var="subItem">
										<li><a href="${subItem.getAbsoluteHref(request)}">${subItem.text}</a></li>
									</c:forEach>
								</ul>
							</li>
						</c:when>
						<c:otherwise>
							<li><a href="${menuItem.getAbsoluteHref(request)}">${menuItem.text}</a></li>
						</c:otherwise>
					</c:choose>
				</c:forEach>
			</ul>
			<ul class="nav navbar-nav navbar-right">
				<li><a href="${contextPath}/signout">Sign out</a></li>
			</ul>
		</div>
	</div>
</div>