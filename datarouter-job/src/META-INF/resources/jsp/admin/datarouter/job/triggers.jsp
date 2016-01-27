<%@page import="com.hotpads.util.core.web.HTMLSelectOptionBean"%>
<%@ include file="/WEB-INF/prelude.jspf"%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html lang="en">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Job Triggers | Job | HotPads</title>
<%@ include file="/jsp/css/css-import.jspf" %>
<link rel="stylesheet" href="${pageContext.request.contextPath}/css/job.css" />
<script type="text/javascript" data-main="${contextPath}/js/core-common" src="${contextPath}/js/require-jquery.js"></script>
<script>require(["bootstrap/bootstrap"]);</script>
<script type="text/javascript">
	function confirmRunJob(jobName, shouldRun, serverName) {
		var displayText = "shouldRun is " + shouldRun +  ". Are you sure you want to run "+jobName+" on "+serverName+"?";
		return confirm(displayText);
	}
</script>
</head>
<body>
	<%@ include file="/jsp/menu/common-navbar.jsp"%>
	<%@ include file="/jsp/menu/dr-navbar.jsp"%>
	<div class="container" style="">
		<h2 class="page-header">Job triggers</h2>
		<div class="well">
			<form class="form" method="get" action="?">
				<div class="form-inline">
					<c:forEach items="${categoryOptions}" var="categoryOption">
						<a class="btn btn-mini <c:if test="${categoryOption.value==param.category ||
						 (categoryOption.value=='all' && empty param.category)}">btn-success</c:if>
						"
							href="?category=${categoryOption.value}&custom=${param.custom}&bydefault=${param.bydefault}
						&disabled=${param.disabled}&enabled=${param.enabled}">${categoryOption.name}</a>
					</c:forEach>
				</div>
				<br />
				<div class="form-inline">
					<input type="text" name="keyword" placeholder="Job keyword or char sequence" autofocus="autofocus" /> <input type="hidden" name="category" value="${param.category}"></input>
					<button type="submit" class="btn btn-mini btn-primary">Filter</button>
				</div>
				<br />
				<div class="form-inline">
					<label class="checkbox" for="custom"><input type="checkbox" name="custom" <c:if test="${param.custom=='on'}">checked</c:if>>Hide Custom</label> <label class="checkbox" for="bydefault"><input type="checkbox" name="bydefault"
						<c:if test="${param.bydefault=='on'}">checked</c:if>>Hide Default</label> <label class="checkbox" for="disabled"><input type="checkbox" name="disabled" <c:if test="${param.disabled=='on'}">checked</c:if>>Hide Disabled</label> <label
						class="checkbox" for="enabled"><input type="checkbox" name="enabled" <c:if test="${param.enabled=='on'}">checked</c:if>>Hide Enabled</label>
				</div>
			</form>
		</div>
		<table class="table table-condensed table-bordered">
			<tr>
				<th>Run</th>
				<th>Interrupt</th>
				<th>Disable/Enable</th>
				<th>Job</th>
				<th>CronExpression</th>
				<th>Category</th>
				<th>LastCompletion</th>
				<th>runningOnServers</th>
			</tr>
			<c:forEach items="${jobs}" var="job">
				 <tr>
					<td><a class="btn <c:choose><c:when test="${job.shouldRun()}">btn-success</c:when><c:otherwise>btn-warning</c:otherwise></c:choose> btn-mini" href="?submitAction=run&name=${job.getClass().getName()}" onclick="return confirmRunJob('${job.getClass().getSimpleName()}', '${job.shouldRun()}', '${job.getServerName()}')">run</a></td>
					<td><a class="btn btn-danger btn-mini" href="?submitAction=interrupt&name=${job.getClass().getName()}" onclick="return window.confirm('Are you sure?');">interrupt</a></td>
					<td><c:choose>
							<c:when test="${job.getIsDisabled()}">
								<a class="btn btn-mini btn-success" href="?submitAction=enable&name=${job.getClass().getName()}" onclick="return window.confirm('Are you sure?');">enable</a>
							</c:when>
							<c:otherwise>
								<a class="btn btn-mini btn-danger" href="?submitAction=disable&name=${job.getClass().getName()}" onclick="return window.confirm('Are you sure?');">disable</a>
							</c:otherwise>
						</c:choose></td>
					<td <c:choose><c:when test="${currentlyRunningTasks[job.jobClass].status == 2}">style="background: #FF9999;"</c:when>
						<c:when test="${currentlyRunningTasks[job.jobClass].status == 1}">style="background: #FFEE00;"</c:when>
						<c:when test="${currentlyRunningTasks[job.jobClass].status == 0}">style="background: #99FF99;"</c:when></c:choose>>${job.getClass().getSimpleName()}</td>
					<td>${job.getTrigger().getCronExpression()}</td>
					<td>${job.getJobCategory()}</td>
					<td>${lastCompletions[job.jobClass].finishTimeString}</td>
					<td></td>
				</tr>
				<%
					/*mcorgan:commenting these out for readability in the jsp.  don't think they're important enough yet
						 <tr>
						 <td>Last trigger time : ${job.getLastFired()}</td>
						 <td>Next trigger time : ${job.getNextScheduled()}</td>
						 <td>Last Interval between two triggers : ${job.getLastIntervalDurationMs()}</td>
						 <td>Last Execution duration : ${job.getLastExecutionDurationMs()}</td>
						 <td colspan="3">Last Error time : ${job.getLastErrorTime()}</td>
						 <td colspan="2">${job.getPercentageOfSuccess()}% of success</td>
						 </tr>
						 */
				%>
			</c:forEach>
		</table>
		<span style="background-color: #99FF99">Running job, last heartbeat within 2 seconds</span>
		<br/>
		<span style="background-color: #FFEE00">Running job, last heartbeat within 2-10 seconds</span>
		<br/>
		<span style="background-color: #FF9999">Running job, last heartbeat over 10 seconds ago</span>
		<br/>
	</div>
</body>
</html>