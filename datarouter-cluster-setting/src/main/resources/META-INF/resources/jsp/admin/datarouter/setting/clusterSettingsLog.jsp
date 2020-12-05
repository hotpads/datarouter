<%@ include file="/jsp/generic/prelude.jspf"%>
<!DOCTYPE html>
<html lang="en">
<head>
	<%@ include file="/jsp/generic/datarouterHead.jsp" %>
	<c:set var="showName" value="${showingAllSettings or showingNodeSettings}"/>
	<title>${showingAllSettings ? 'All Settings Log' : 'Cluster Settings Log'} | Datarouter</title>
	<script>
		require(["jquery"], function() {
			$("#update-start-timestamp").click(function() {
				const explicitStartInput = $('#manual-start-timestamp').val();
				let href = "?submitAction=${param.submitAction}";
				if (explicitStartInput) {
					href += "&explicitStartIso="+explicitStartInput+":59.999&inclusiveStart=true";
				}
				window.location.href = href;
			});

			$('#manual-start-timestamp').val(function(){
				if(!this.value){
					const date = new Date()
					const ten = i => (i < 10 ? '0' : '') + i
					const YYYY = date.getFullYear()
					const MM = ten(date.getMonth() + 1)
					const DD = ten(date.getDate())
					const HH = ten(date.getHours())
					const II = ten(date.getMinutes())
					return YYYY + '-' + MM + '-' + DD + 'T' + HH + ':' + II
				}else{
					return this.value
				}
			})
		})
	</script>
	<style>
		table {
			width: auto !important; /* override bootstrap 100% */
			margin-left: auto;
			margin-right: auto;
		}

		/* Mobile friendly table styling */
		@media only screen and (max-width: 1200px){
			table, thead, tbody, th, td, tr{
				display: block;
			}

			thead tr{
				position: absolute;
				top: -9999px;
				left: -9999px;
			}

			tr{
				border: 1px solid #a2a2a2;
				overflow: auto;
			}

			td{
				border: none;
				border-bottom: 1px solid #eee;
				position: relative;
				padding-left: 160px !important;
			}

			td:before{
				position: absolute;
				top: 6px;
				left: 6px;
				width: 160px;
				padding-right: 10px;
				white-space: nowrap;
				font-weight: bold;
			}

			/* Add column labels before the content of the cell on every row */
			td:nth-of-type(1):before { content: "Time stamp"; }
			<c:if test="${showName}">
			td:nth-of-type(2):before { content: "Name"; }
			</c:if>
			td:nth-of-type(${showName ? '3' : '2'}):before { content: "Action"; }
			td:nth-of-type(${showName ? '4' : '3'}):before { content: "Scope"; }
			td:nth-of-type(${showName ? '5' : '4'}):before { content: "Server Type"; }
			td:nth-of-type(${showName ? '6' : '5'}):before { content: "Server Name"; }
			td:nth-of-type(${showName ? '7' : '6'}):before { content: "Changed by"; }
			td:nth-of-type(${showName ? '8' : '7'}):before { content: "Comment"; }
			td:nth-of-type(${showName ? '9' : '8'}):before { content: "Value"; }
		}
	</style>
</head>
<body>
	<%@ include file="/jsp/menu/common-navbar-b4.jsp" %>
	<div class="container-fluid mt-5">
		<div class="container">
			<h2 class="pb-2 mb-3 border-bottom text-break">
				Log for
				<c:choose>
					<c:when test="${showingAllSettings}">all cluster settings</c:when>
					<c:otherwise>
						<c:set var="combinedName" value=""/>
						<c:forEach var="part" items="${nameParts}" varStatus="status"><c:set
							var="combinedName" value="${combinedName}${status.first ? '' : '.'}${part}"/><a
							href="?submitAction=browseSettings&name=${combinedName}">${part}</a>${status.last ? '' : '.'}</c:forEach>
						node
					</c:otherwise>
				</c:choose>
			</h2>
			<c:if test="${showingAllSettings}">
				<form class="form-inline mb-3">
					<div class="form-group">
						<label for="manual-start-timestamp">Showing changes earlier than</label>
						<div class="input-group mx-sm-2">
							<input class="form-control" type="datetime-local" id="manual-start-timestamp" value="${param.explicitStartIso.substring(0, 16)}"/>
							<span class="input-group-append">
								<button id="update-start-timestamp" class="btn btn-dark" type="button">
									<i class="fas fa-search"></i>
								</button>
							</span>
						</div>
					</div>
				</form>
			</c:if>
		</div>

		<table class="table table-sm table-striped">
			<thead class="thead-dark">
				<tr>
					<th>Time stamp</th>
					<c:if test="${showingAllSettings or showingNodeSettings}"><th>Name</th></c:if>
					<th>Action</th>
					<th>Scope</th>
					<th>Server Type</th>
					<th>Server Name</th>
					<th>Changed By</th>
					<th>Comment</th>
					<th>Value</th>
				</tr>
			</thead>
			<tbody>
				<c:forEach var="log" items="${logs}">
					<tr>
						<td>${log.created}</td>
						<c:if test="${showingAllSettings or showingNodeSettings}">
							<td>
								<c:set var="combinedName" value=""/>
								<c:forEach var="part" items="${log.nameParts}" varStatus="status"><c:set
									var="combinedName" value="${combinedName}${status.first ? '' : '.'}${part}"/><a
									href="?submitAction=browseSettings&name=${combinedName}">${part}</a>${status.last ? '' : '.'}</c:forEach>
							</td>
						</c:if>
						<td
							<c:choose>
								<c:when test="${log.action == 'INSERTED'}">class="table-success"</c:when>
								<c:when test="${log.action == 'UPDATED'}">class="table-info"</c:when>
								<c:when test="${log.action == 'DELETED'}">class="table-danger"</c:when>
							</c:choose>
						>${log.htmlSafeAction}</td>
						<td>${log.htmlSafeScope}</td>
						<td>${log.htmlSafeServerType}</td>
						<td>${log.htmlSafeServerName}</td>
						<td>${log.htmlSafeChangedBy}</td>
						<td>${log.htmlSafeComment}</td>
						<td>${log.htmlSafeValue}</td>
					</tr>
				</c:forEach>
				<c:if test="${empty logs}">
					<tr><td colspan="10" style="text-align:center;"><h4>End of settings log</h4></td></tr>
				</c:if>
			</tbody>
		</table>

		<c:if test="${showingAllSettings}">
			<nav class="container mb-5">
				<ul class="pagination justify-content-center">
					<li class="page-item ${hasPreviousPage ? "" : "disabled"}">
						<a class="page-link" href="?submitAction=logsForAll">
							&laquo; Most recent
						</a>
					</li>
					<li class="page-item ${hasNextPage ? "" : "disabled"}">
						<a class="page-link"
							<c:if test="${hasNextPage}">href="?submitAction=logsForAll&explicitStartIso=${logs.get(logs.size() - 1).createdIsoLocalDateTime}&inclusiveStart=false"</c:if>
							<c:if test="${not hasNextPage}">disabled="true"</c:if>
						>
							Older &raquo;
						</a>
					</li>
				</ul>
			</nav>
		</c:if>
	</div>
</body>
</html>