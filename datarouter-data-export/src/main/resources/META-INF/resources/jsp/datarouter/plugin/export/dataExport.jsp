<%@ include file="/jsp/generic/prelude.jspf"%>
<!DOCTYPE html>
<html>
<head>
	<title>Data Export</title>
	<%@ include file="/jsp/generic/datarouterHead.jsp"%>
</head>
<body>
	<%@ include file="/jsp/menu/common-navbar-b4.jsp"%>
	<div class="container my-4">
		<h2>Data Export</h2>
		<form id="validationform" name="validationform" method="GET" action="?">
			<input type="hidden" value="exportData" name="submitAction">
			<input type="hidden" name="s3KeyString" value="${s3KeyString}">
			<input type="hidden" name="s3KeyList" value="${s3KeyList}">
			<table class="table table-borderless table-sm">
				<thead>
					<tr>
						<td>Node name</td>
						<td>Start After Key</td>
						<td>End After Key</td>
						<td>Max Rows</td>
						<td></td>
					</tr>
				</thead>
				<tbody>
					<tr>
						<td><input class="form-control" type="text" name="datarouterNodeName" value="${param.nodeName}" required></td>
						<td><input class="form-control" type="text" name="startAfterKeyString" value="${param.startAfterKeyString}"></td>
						<td><input class="form-control" type="text" name="endBeforeKeyString" value="${param.endBeforeKeyString}"></td>
						<td><input class="form-control" type="number" name="maxRows" value="${param.maxRows}"></td>
						<td></td>
					</tr>
				</tbody>
				<tfoot>
					<tr>
						<td colspan="4" class="text-right"><button type="button" class="btn btn-secondary" id="add-more-button">Add more (+)</button></td>
					</tr>
					<tr>
						<td>
							<input class="form-control" type="text" name="email" value="${param.email}" placeholder="Optional email CSV">
							<small class="form-text text-muted">Sends email once export is complete</small>
						</td>
					</tr>
					<tr>
						<td><input type="submit" class="btn btn-primary" value="Export"></td>
					</tr>
				</tfoot>
			</table>
		</form>
	</div>
	<script>
		require(['jquery'], function(){
			$('#add-more-button').click(function(){
				const row = $('<tr>').append($(`
					<td><input class="form-control" type="text" name="datarouterNodeName" required></td>
					<td><input class="form-control" type="text" name="startAfterKeyString"></td>
					<td><input class="form-control" type="text" name="endBeforeKeyString"></td>
					<td><input class="form-control" type="number" name="maxRows"></td>
					<td><button class="btn text-danger" type="button" onclick="$(this).closest('tr').remove()"><i class="fas fa-times"></i></button></td>
				`))
				$('table tbody').append(row)
			})
		})
	</script>
</body>
</html>
