<div class="card" id="${escapedName}">
	<nav class="navbar navbar-light bg-light justify-content-between ${lined ? '' : 'border-bottom'}">
		<a class="navbar-brand">${name}</a>
		<form class="form-inline">
			<div class="input-group">
				<input class="form-control border-right-0" type="search" placeholder="Regex filter">
				<div class="input-group-append">
					<span class="bg-white border-left-0 input-group-text text-black-50"><i class="fas fa-filter"></i></span>
				</div>
			</div>
		</form>
	</nav>
	<ul class="list-group list-group-flush border-bottom">
		<c:forEach items="${libs}" var="lib">
			<li class="list-group-item py-1 ${lined ? '' : 'border-0'} filterable-container">
				<c:choose>
					<c:when test="${map}">
						<div class="table-responsive">
							<table class="definition light">
							<tbody>
								<tr title="${lib.value.describeShort}"><td colspan="2" class="filterable-value">${lib.key}</td></tr>
								<tr class="sub"><td>Branch</td><td>${lib.value.branch}</td></tr>
								<tr class="sub" title="${lib.value.commitTime} by ${lib.value.commitUserName}">
									<td>Commit</td><td>${lib.value.idAbbrev}</td>
								</tr>
								<tr class="sub"><td>Build time</td><td>${lib.value.buildTime}</td></tr>
								<tr class="sub"><td>Build id</td><td>${buildDetailedLibraries[lib.key].buildId}</td></tr>
							</tbody>
							</table>
						</div>
					</c:when>
					<c:otherwise>
						<span class="filterable-value">${lib}</span>
					</c:otherwise>
				</c:choose>
			</li>
		</c:forEach>
	</ul>
</div>
<script>
	require(['jquery-ui'], function(){
		const container = $('#${escapedName}')
		container.find('input[type="search"]').on('input copy paste', function(){
			try{
				const regex = new RegExp($(this).val(), 'i')
				container.find('.filterable-value').get().map($).forEach(option => {
					if(!regex.test(option.text())){
						option.closest('.filterable-container').hide()
					}else{
						option.closest('.filterable-container').show()
					}
				})
			}catch(e){
				console.warn(e)// likely just invalid regex as they type
			}
		})
	})
</script>