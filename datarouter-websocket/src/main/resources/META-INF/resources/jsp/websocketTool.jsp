<%@ include file="/jsp/generic/prelude.jspf"%>
<!DOCTYPE html>
<html>
	<head>
		<title>Websocket admin tool</title>
		<%@ include file="/jsp/generic/datarouterHead.jsp" %>
	</head>
	<body>
		<%@ include file="/jsp/menu/common-navbar-b4.jsp" %>
		<div class="container my-4">
			<h2>WebSocket tool</h2>
			<dl>
				<dt>Current server</dt>
				<dd>${serverAddress} (${serverName})</dd>
				<dt>Local store count</dt>
				<dd>${localStoreSize}</dd>
			</dl>
			<table>
				<thead>
					<tr>
						<th>Server</th>
						<th>Count</th>
					</tr>
				</thead>
				<tbody>
					<c:forEach items="${persistentStorageByServerCount}" var="entry">
						<tr>
							<td>${entry.key}</td>
							<td>${entry.value}</td>
						</tr>
					</c:forEach>
				</tbody>
				<tfoot>
					<tr>
						<td>Total</td>
						<td>${persistentStorageTotalCount}</td>
					</tr>
				</tfoot>
			</table>
			<form>
				<label>
					UserToken
					<input name="userToken" value="${param.userToken}" size="60">
				</label>
				<input type="submit">
			</form>
			<c:if test="${not empty userSessions}">
				<script>
				const useDestination = event => {
					destinations.value += event.target.dataset.user + "/" + event.target.dataset.id + "\n";
				}
				</script>
				<table>
					<thead>
						<tr>
							<th></th>
							<th>Id</th>
							<th>Opening date</th>
							<th>Server</th>
						</tr>
					</thead>
					<tbody>
						<c:forEach items="${userSessions}" var="session">
							<tr>
								<td><input type="submit" data-id="${session.id}" data-user="${session.userToken}" value="+" onclick="useDestination(event)"></td>
								<td>${session.id}</td>
								<td>${session.openingDate}</td>
								<td>${session.serverName}</td>
							</tr>
						</c:forEach>
					</tbody>
				</table>
			</c:if>
			<form>
				<input type="hidden" name="userToken" value="${param.userToken}">
				<label>
					Destinations
					<br>
					<textarea cols="75" rows="10" name="destinations" id="destinations">${param.destinations}</textarea>
				</label>
				<br>
				<label>
					Message
					<br>
					<textarea cols="75" rows="10" name="message">${param.message}</textarea>
				</label>
				<br>
				<input type="submit">
			</form>
			<table>
				<tbody>
					<c:forEach items="${sendResults}" var="sendResult">
						<tr>
							<td>${sendResult.left}</td>
							<td>${sendResult.right}</td>
						</tr>
					</c:forEach>
				</tbody>
			</table>
		</div>
	</body>
</html>
