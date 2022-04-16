<%@ include file="/jsp/generic/prelude.jspf"%>
<!DOCTYPE html>
<html>
<head>
	<title>Logging settings</title>
	<%@ include file="/jsp/generic/datarouterHead.jsp" %>
	<link rel="stylesheet" type="text/css" href="${contextPath}/jee-assets/multiple-select/multiple-select.css" />
	<style>
		table.sortable th:not(.sorttable_sorted):not(.sorttable_sorted_reverse):not(.sorttable_nosort):after {
			content: " \25B4\25BE";
		}
		.ms-parent > button{
			width: 100%;
			height: 100%;
		}
		.ms-drop{
			width: auto;
		}
		.ms-choice{
			border: none;
		}
		.ms-choice > span{
			height: 100%;
			line-height: 32px;
		}
		.ms-drop > ul > li > label > input{
			margin-right: 8px;
		}
	</style>
	<script>
		require(['multiple-select', 'sorttable'], function(){
			$(document).ready(function(){
				$('select[multiple]').multipleSelect();
				$('.table-delete-config').click(function() {
					const { contextpath, configname, configkey } = this.dataset;
					if (confirm('Delete logger config for "' + configname + '"?')) {
						window.location.href = contextpath + "/datarouter/logging/deleteLoggerConfig?name=" + configkey;
					}
				});
			});
		});
	</script>
</head>
<body>
	<%@ include file="/jsp/menu/common-navbar-b4.jsp"%>
	<div class="alert alert-warning rounded-0">
		If you want to immediately see the effects of your change, be sure to be on a server-specific url.
	</div>
	<div class="container-fluid d-table w-auto my-4">
		<h2 class="mb-4">Log4j2 configuration</h2>

		<div class="border-left border-warning p-3 mb-4">
			<h5 class="card-title">Tester <span class="h6 card-subtitle mb-2 text-muted">(will log the message for each level)</span></h5>
			<form class="form-inline" onsubmit="$.ajax('${contextPath}/datarouter/logging/testLog?loggerName='+$('#loggerName').val()+'&loggerMessage='+$('#loggerMessage').val()); return false;">
				<div class="input-group w-100">
					<input id="loggerName" type="text" class="form-control w-auto" value="io.datarouter.web.handler.logging">
					<input id="loggerMessage" type="text" class="form-control w-auto" placeholder="Message">
					<div class="input-group-append">
						<button class="btn btn-primary">Test</button>
					</div>
				</div>
			</form>
		</div>
		<div>
			<div class="mb-3">
				<h3 class="mb-0">LoggerConfig</h3>
				<a href="http://logging.apache.org/log4j/2.x/manual/architecture.html#LoggerConfig" target="_blank">About LoggerConfig</a>
				&nbsp;|&nbsp;
				<a href="http://logging.apache.org/log4j/2.x/manual/configuration.html#Additivity" target="_blank">About Additivity</a>
			</div>
			<div class="mb-4">
				<h4>Add Config</h4>
				<form class="form-inline" method="post" action="${contextPath}/datarouter/logging/createLoggerConfig">
					<div class="input-group w-100">
						<input class="logger-name form-control" id="createLoggerName" type="text" name="name" placeholder="Name" required>
						<div class="input-group-append">
							<input class="logger-ttl-minutes form-control input-group-append" id="createLoggerTtlMinutes" type="number" name="ttlMinutes" placeholder="TTL (MINUTES)">
							<select name="level" class="form-control input-group-append rounded-0" required>
								<option disabled selected>Level</option>
								<c:forEach items="${levels}" var="level">
									<option>${level}</option>
								</c:forEach>
							</select>
							<select name="appenders" multiple required class="form-control input-group-append rounded-0 p-0" style="display: none;">
								<c:forEach items="${appenders}" var="appender">
									<option>${appender.value.name}</option>
								</c:forEach>
							</select>
							<input type="text" readonly class="form-control input-group-append rounded-0 font-weight-bold" value="${currentUserEmail}">
							<input class="logger-add-config btn btn-primary form-control" type="submit" value="Add">
						</div>
					</div>
				</form>
			</div>
			<div class="mb-4">
				<h4>Current Config</h4>
				<table class="table table-sm table-striped mb-0 sortable">
					<thead class="thead-dark text-center">
						<tr>
							<th>Name</th>
							<th>TTL (Minutes)</th>
							<th>Level</th>
							<th>Appenders</th>
							<th>User</th>
							<th>Updated</th>
							<th class="sorttable_nosort">Action</th>
						</tr>
					</thead>
					<tbody>
					<c:forEach items="${configs}" var="config">
						<tr>
							<form method="post" action="${contextPath}/datarouter/logging/updateLoggerConfig">
								<input type="hidden" name="name" value="${config.value.name}">
								<td class="align-middle">
									<c:if test="${!config.value.name.equals(rootLogger.name)}">
										<c:if test="${empty config.value.link}">
											${config.value.name}
										</c:if>
										<c:if test="${not empty config.value.link}">
											<a href="${config.value.link}" target="_blank">${config.value.name}</a>
										</c:if>
									</c:if>
									<c:if test="${config.value.name.equals(rootLogger.name)}">
										<h4 class="text-center">Root Logger</h4>
									</c:if>
								</td>
								<td class="align-middle" sorttable_customkey="${config.value.ttlMinutes}">
									<c:set var="readonlyTtl" value="${!config.value.canDelete || config.value.name.equals(rootLogger.name)}"/>
									<input ${readonlyTtl ? 'disabled class="form-control-plaintext"' : 'class="form-control"'} type="number" name="ttlMinutes" value="${config.value.ttlMinutes}">
								</td>
								<td class="align-middle" sorttable_customkey="${config.value.level.toString()}">
									<select name="level" class="form-control">
										<c:forEach items="${levels}" var="level">
											<option
													<c:if test="${level == config.value.level}">
														selected
													</c:if>>${level}</option>
										</c:forEach>
									</select>
								</td>
								<td class="align-middle">
									<c:set var="configAppender" value="${appenderMap.get(config.value)}"/>
									<select name="appenders" class="form-control" style="width: 150px; display: none" multiple>
										<c:forEach items="${appenders}" var="appender">
											<option
													<c:forEach items="${configAppender}" var="element">
														<c:if test="${element == appender.value.name}">
															selected
														</c:if>
													</c:forEach>
											>${appender.value.name}</option>
										</c:forEach>
									</select>
								</td>
								<td class="align-middle">
									<c:if test="${config.value.email != null}">
										<span class="separated">${config.value.email}</span>
									</c:if>
								</td>
								<td class="align-middle">
									<c:if test="${config.value.lastUpdated != null}">
										<span class="separated">${config.value.lastUpdated}</span>
									</c:if>
								</td>
								<td class="align-middle 
										<c:if test="${!config.value.canDelete || config.value.name.equals(rootLogger.name)}">
											logger-root-action
										</c:if>
								">
									<div class="logger-action-buttons">
										<input type="submit" class="btn btn-warning separated" value="Update">
										<c:if test="${config.value.canDelete && !config.value.name.equals(rootLogger.name)}">
											<a class="btn btn-danger table-delete-config" data-configkey="${config.key}" 
												data-configname="${config.value.name}" data-contextpath="${contextPath}">
												Delete
											</a>
										</c:if>
									</div>
								</td>
							</form>
						</tr>
					</c:forEach>
					</tbody>
				</table>
			</div>
		</div>
		<div class="mb-4">
			<h4>Appenders</h4>
			<table class="table table-sm sortable">
				<thead class="thead-dark">
					<tr>
						<th>Name</th>
						<th>Type</th>
						<th>Layout</th>
						<th class="sorttable_nosort">Action</th>
					</tr>
				</thead>
				<tbody>
				<c:forEach items="${appenders}" var="appender">
					<tr>
						<td class="align-middle">${appender.value.name}</td>
						<td class="align-middle">${appender.value.getClass().simpleName}</td>
						<td class="align-middle text-monospace">${appender.value.layout}</td>
						<td class="align-middle">
							<a class="btn btn-primary" href="${contextPath}/datarouter/logging/edit${appender.value.getClass().simpleName}?name=${appender.key}">Details</a>
							<a class="btn btn-danger" href="${contextPath}/datarouter/logging/deleteAppender?name=${appender.key}">Delete</a>
						</td>
					</tr>
				</c:forEach>
				</tbody>
				<tfoot>
					<tr>
						<td colspan="4">
							<a href="${contextPath}/datarouter/logging/editConsoleAppender">Create console appender</a>
							&nbsp;|&nbsp;
							<a href="${contextPath}/datarouter/logging/editFileAppender">Create file appender</a>
						</td>
					</tr>
				</tfoot>
			</table>
		</div>
	</div>
</body>
</html>
