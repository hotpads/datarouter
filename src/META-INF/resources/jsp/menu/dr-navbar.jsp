<div class="navbar navbar-inverse ">
	<div class="navbar-inner">
		<div class="container">
			<div class="btn-group pull-right">
				<a class="btn btn-bavbar" href="${contextPath}/logout">Logout</a>
			</div>
			<a type="button" class="btn btn-navbar" data-toggle="collapse" data-target=".nav-collapse">
				<span class="icon-bar"></span>
				<span class="icon-bar"></span>
				<span class="icon-bar"></span>
			</a>
			<a class="brand hotpads-brand" href="/">
				<img alt="HotPads logo"	src="http://filenet.hotpads.com/images/frontpage/frontpage-logo-no-dot-com.png" id="hotpads-logo">
			</a>
			<div class="nav-collapse collapse">
				<ul class="nav" role="navigation">
					<li>
						<a href="${contextPath}/datarouter/routers" title="List all the routers">List Routers</a>
					</li>
					<li class="dropdown">
						<a href="#" title="Settings" id="drop" role="button" class="dropdown-toggle" data-toggle="dropdown">
							Settings
							<b class="caret"></b>
						</a>
						<ul class="dropdown-menu" role="menu" aria-labelledby="drop">
							<li data-role="presentation">
								<a data-role="menuitem" href="${contextPath}/datarouter/settings?submitAction=browseSettings" title="Browse settings">Browse settings</a>
							</li>
							<li data-role="presentation">
								<a data-role="menuitem" href="${contextPath}/datarouter/settings" title="All settings">All settings</a>
							</li>
						</ul>
					</li>
					<li>
						<a href="${contextPath}/datarouter/stackTraces" title="See the strackTraces of this server">StackTraces</a>
					</li>
					<li>
						<a href="${contextPath}/datarouter/databeanGenerator" title="Databean generator">Databean Generator</a>
					</li>
					<li>
						<a href="${contextPath}/datarouter/logging">Logging</a>
					</li>
					<li>
						<a href="${contextPath}/datarouter/memory">Memory</a>
					</li>
					<li class="dropdown">
						<a href="#" id="notification" role="button" class="dropdown-toggle" data-toggle="dropdown">
							Notification
							<b class="caret"></b>
						</a>
						<ul class="dropdown-menu" role="menu" aria-labelledby="notification">
							<li data-role="presentation">
								<a data-role="menuitem" href="${contextPath}/datarouter/notification/alias">
									Alias
								</a>
							</li>
						</ul>
					</li>
				</ul>
			</div>
		</div>
	</div>
</div>
