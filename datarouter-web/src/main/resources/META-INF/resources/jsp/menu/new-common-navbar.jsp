<header>
	<c:if test="${isAdmin}">
		<c:set var="isCollapsable" value="${fn:length(tomcatWebApps) > 1}"/>
		<style>
			#common-navbar.productionEnv{
				background-color: #ff4d4d !important;
			}
			#common-navbar.productionEnv .nav-link{
				color: #fff !important;
			}
			#common-navbar .nav-link,
			#app-navbar .nav-link{
				padding-right: 15px;
				padding-left: 15px;
			}

			<c:choose>
			<c:when test="${isCollapsable}">
				@media (min-width: 768px){
					#common-navbar .nav-link{
						padding-top: 1px;
						padding-bottom: 1px;
					}
				}
			</c:when>
			<c:otherwise>
				#common-navbar .nav-link{
					padding-top: 1px;
					padding-bottom: 1px;
				}
			</c:otherwise>
			</c:choose>
		</style>
		<nav id="common-navbar" class="navbar ${isCollapsable ? 'navbar-expand-md py-md-0' : 'navbar-expand py-0'} navbar-dark bg-dark  ${isProduction ? 'productionEnv' : ''}">
			<button class="navbar-toggler ml-auto" type="button" data-toggle="collapse" data-target="#common-navbar-content">
				<span class="navbar-toggler-icon"></span>
			</button>
			<div class="collapse navbar-collapse" id="common-navbar-content">
				<ul class="navbar-nav">
					<c:forEach var="webApp" items="${tomcatWebApps}">
						<li class="nav-item">
							<a class="nav-link" data-target="${webApp.key}" href="${webApp.value}">${webApp.key}</a>
						</li>
					</c:forEach>
					<li class="border-right border-secondary d-none d-md-block"></li>
					<li class="nav-item" id="common-menu-datarouter">
						<a class="nav-link" data-target="datarouter" href="${contextPath}/datarouter">datarouter</a>
					</li>
					<li class="border-right border-secondary d-none d-md-block"></li>
					<li class="nav-item">
						<a href="${traceUrl}" class="nav-link">
							<span title="Java processing duration">j:${durationString}</span>
							<span title="Request duration">r:<span id="requestTiming"></span></span>
							<span title="Client document load duration">c:<span id="clientTiming"></span></span>
						</a>
					</li>
				</ul>
			</div>
		</nav>
		<script type="text/javascript">
		require(['jquery'], function($){
			const navPerformance = performance.getEntriesByType("navigation")[0] || performance.timing
			if(!navPerformance){
				console.log("Unable to find navigation performance from: " + performance)
				return
			}
			$('#requestTiming').text(Math.ceil(navPerformance.responseEnd - navPerformance.requestStart) + 'ms')

			$(function(){
				const app = "${contextPath}".replace('/', '')
				const isDatarouterPage = location.pathname.indexOf("${app}/datarouter") !== -1
				const target = isDatarouterPage ? "datarouter" : app
				$('#common-navbar a[data-target="' + target + '"]').addClass('active')

				const checkLoadEndInterval = setInterval(() => {
					if(navPerformance.loadEventEnd === 0){
						return
					}
					clearInterval(checkLoadEndInterval)
					$('#clientTiming').text(Math.ceil(navPerformance.loadEventEnd - navPerformance.responseEnd) + 'ms')
				}, 100)
			})
		})
		</script>
	</c:if>
	<c:set var="navBarType" value="${isDatarouterPage ? datarouterNavBar : webAppNavBar}"/>
	<c:set var="navBarTarget" value="${isDatarouterPage ? '#dr-navbar' : '#mav-navbar'}"/>
	<nav id="app-navbar" class="navbar navbar-expand-md navbar-dark bg-dark">
		<c:if test="${isDatarouterPage}">
			<a class="navbar-brand d-inline-flex" href="${contextPath}/datarouter">
				<img src="${contextPath}/${navBarType.logoSrc}" alt="${navBarType.logoAlt}" class="align-top" style="height: 1.5rem">
			</a>
		</c:if>
		<button class="navbar-toggler" type="button" data-toggle="collapse" data-target="#app-navbar-content">
			<span class="navbar-toggler-icon"></span>
		</button>
		<div id="app-navbar-content" class="collapse navbar-collapse">
			<ul class="navbar-nav mr-auto">
				<c:set var="request" value="<%= request %>"/>
				<c:forEach items="${navBarType.getMenuItems(request)}" var="menuItem">
					<c:choose>
						<c:when test="${menuItem.isDropdown()}">
							<li class="nav-item dropdown">
								<a href="#" class="nav-link dropdown-toggle" data-toggle="dropdown">${menuItem.text} <span class="caret"></span></a>
								<div class="dropdown-menu">
									<c:forEach items="${menuItem.getSubItems(request)}" var="subItem">
										<a class="dropdown-item" href="${subItem.getAbsoluteHref(request)}">${subItem.text}</a>
									</c:forEach>
								</div>
							</li>
						</c:when>
						<c:otherwise>
							<li class="nav-item"><a class="nav-link" href="${menuItem.getAbsoluteHref(request)}">${menuItem.text}</a></li>
						</c:otherwise>
					</c:choose>
				</c:forEach>
			</ul>
			<ul class="navbar-nav">
				<li class="nav-item"><a class="nav-link" href="${contextPath}/signout">Sign out</a></li>
			</ul>
		</div>
	</nav>
</header>