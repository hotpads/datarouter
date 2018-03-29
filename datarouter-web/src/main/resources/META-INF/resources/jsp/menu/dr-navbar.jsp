<div class="navbar navbar-inverse navbar-static-top">
	<div class="container-fluid">
		<div class="navbar-header">
			<button type="button" class="navbar-toggle collapsed" data-toggle="collapse" data-target="#dr-navbar">
				<span class="icon-bar"></span>
				<span class="icon-bar"></span>
				<span class="icon-bar"></span>
			</button>
			<a class="navbar-brand" href="${contextPath}/datarouter/routers">
				<img alt="Datarouter logo" src="${contextPath}/jee-assets/datarouter-logo.png" class="logo-brand">
			</a>
		</div>
		<div class="collapse navbar-collapse" id="dr-navbar">
			<ul class="nav navbar-nav">
				<li class="dropdown">
					<a href="#" title="Jobs" id="drop" role="button" class="dropdown-toggle" data-toggle="dropdown">
						Jobs
						<b class="caret"></b>
					</a>
					<ul class="dropdown-menu" role="menu" aria-labelledby="drop">
						<li data-role="presentation">
							<a data-role="menuitem" 
								href="${contextPath}/datarouter/longRunningTasks" 
								title="LongRunningTasks">Long Running Tasks</a>
							<a data-role="menuitem" 
								href="${contextPath}/datarouter/triggers" 
								title="Triggers">Triggers</a>
							<a data-role="menuitem" 
								href="${contextPath}/datarouter/joblets" 
								title="Joblets">Joblets</a>
						</li>
					</ul>
				</li>
				<li class="dropdown">
					<a href="#" class="dropdown-toggle" data-toggle="dropdown" role="button" aria-haspopup="true" aria-expanded="false">Settings <span class="caret"></span></a>
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
						<li data-role="presentation">
							<a data-role="menuitem"
								href="${contextPath}/datarouter/logging"
								title="Logging Config">Logging Config</a>
						</li>
						<li data-role="presentation">
							<a data-role="menuitem" 
								href="${contextPath}/datarouter/tableRowCount/threshold"
								title="Row Count Settings">Row Count Settings</a>
						</li>
						<li role="separator" class="divider"></li>
						<li data-role="presentation">
							<a data-role="menuitem"
							   href="${contextPath}/datarouter/emailTest"
							   title="Email Test">Email Test</a>
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
								href="${contextPath}/datarouter/callsite"
								title="Callsite Profiling">Callsite Profiling</a>
						</li>
						<li data-role="presentation">
							<a data-role="menuitem"
								href="${contextPath}/datarouter/exception/browse"
								title="Exceptions">Exceptions</a>
						</li>
						<li data-role="presentation">
							<a data-role="menuitem"
								href="${contextPath}/datarouter/executors">Executors</a>
						</li>
						<li data-role="presentation">
							<a data-role="menuitem"
								href="${contextPath}/datarouter/latency"
								title="Latency">Latency</a>
						</li>
						<li data-role="presentation">
							<a data-role="menuitem"
								href="${contextPath}/datarouter/memory">Server Status</a>
						</li>
						<li data-role="presentation">
							<a data-role="menuitem"
								href="${contextPath}/datarouter/stackTraces"
								title="See the strackTraces of this server">Stack Traces</a>
						</li>
						<li data-role="presentation">
							<a data-role="menuitem"
								href="${contextPath}/datarouter/tableRowCount"
								title="TableRowCount">TableRowCount</a>
						</li>
						<li data-role="presentation">
							<a data-role="menuitem"
								href="${contextPath}/datarouter/traces"
								title="View a Trace">Traces</a>
						</li>
						<li data-role="presentation">
							<a data-role="menuitem"
								href="${contextPath}/datarouter/webAppInstances"
								title="Web App Instances">Web App Instances</a>
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
						<li data-role="presentation">
							<a data-role="menuitem"
								title="View and change notification timing settings and addign them to notification types"
								href="${contextPath}/datarouter/notification/timing">Timing</a>
						</li>
						<li data-role="presentation">
							<a data-role="menuitem" 
								href="${contextPath}/datarouter/notification/tester">Tester</a>
						</li>
					</ul>
				</li>
				<li>
					<a href="${contextPath}/datarouter/databeanGenerator" title="Databean Generator">Databean Generator</a>
				</li>
			</ul>
		</div>
	</div>
</div>
<script>
	require(['bootstrap']);
</script>
