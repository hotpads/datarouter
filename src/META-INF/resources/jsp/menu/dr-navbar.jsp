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
						<a href="${contextPath}/datarouter/routers" title="List routers">Routers</a>
					</li>
					<li class="dropdown">
						<a href="#" title="Settings" id="drop" role="button" class="dropdown-toggle" data-toggle="dropdown">
							Settings
							<b class="caret"></b>
						</a>
						<ul class="dropdown-menu" role="menu" aria-labelledby="drop">
							<li data-role="presentation">
								<a data-role="menuitem" 
									href="${contextPath}/datarouter/settings?submitAction=browseSettings"
									title="Browse Settings">Browse Settings</a>
							</li>
							<li data-role="presentation">
								<a data-role="menuitem" 
									href="${contextPath}/datarouter/settings" 
									title="Custom Settings">Custom Settings</a>
							</li>
						</ul>
					</li>
					<li class="dropdown">
						<a href="#" id="monitoring" role="button" class="dropdown-toggle" data-toggle="dropdown">
							Monitoring
							<b class="caret"></b>
						</a>
						<ul class="dropdown-menu" role="menu" aria-labelledby="monitoring">
							<li data-role="presentation">
								<a data-role="menuitem"
									href="${contextPath}/datarouter/memory">Server Status</a>
							</li>
							<li data-role="presentation">
								<a data-role="menuitem"
									href="${contextPath}/datarouter/executors">Executors</a>
							</li>
							<li data-role="presentation">
								<a data-role="menuitem"
									href="${contextPath}/datarouter/stackTraces"
									title="See the strackTraces of this server">Stack Traces</a>
							</li>
							<li data-role="presentation">
								<a data-role="menuitem"
									href="${contextPath}/datarouter/logging"
									title="Logging Config">Logging Config</a>
							</li>
							<li data-role="presentation">
								<a data-role="menuitem"
									href="${contextPath}/datarouter/traces"
									title="View a Trace">Traces</a>
							</li>
							<li data-role="presentation">
								<a data-role="menuitem"
									href="${contextPath}/datarouter/callsite"
									title="Callsite Profiling">Callsite Profiling</a>
							</li>
						</ul>
					</li>
					<li class="dropdown">
						<a href="#" id="notification" role="button" class="dropdown-toggle" data-toggle="dropdown">
							Notifications
							<b class="caret"></b>
						</a>
						<ul class="dropdown-menu" role="menu" aria-labelledby="notification">
							<li data-role="presentation">
								<a data-role="menuitem" 
									href="${contextPath}/datarouter/notification/alias">Aliases</a>
							</li>
						</ul>
					</li>
					<li class="dropdown">
						<a href="#" title="Jobs" id="drop" role="button" class="dropdown-toggle" data-toggle="dropdown">
							Jobs
							<b class="caret"></b>
						</a>
						<ul class="dropdown-menu" role="menu" aria-labelledby="drop">
							<li data-role="presentation">
								<a data-role="menuitem" href="${contextPath}/datarouter/triggers" title="Triggers">Triggers</a>
							</li>
						</ul>
					</li>
					<li>
						<a href="${contextPath}/datarouter/databeanGenerator" 
							title="Databean Generator">Databean Generator</a>
					</li>
				</ul>
			</div>
		</div>
	</div>
</div>
