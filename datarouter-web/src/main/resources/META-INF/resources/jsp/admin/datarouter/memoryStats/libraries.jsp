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
				<div class="table-responsive">
					<table class="definition light">
						<tbody>
							<c:choose>
								<c:when test="${map}">
									<tr>
										<td colspan="2" class="filterable-value"
												title="${fn:escapeXml(manifests[lib.key].manifestString)}">
											${lib.key}
										</td>
									</tr>
									<tr class="sub"><td>Branch</td><td title="${lib.value.describeShort}">${lib.value.branch}</td></tr>
									<tr class="sub" title="${lib.value.commitTime} by ${lib.value.commitUserName}">
										<td>Commit</td><td>${lib.value.idAbbrev}</td>
									</tr>
									<tr class="sub"><td>Build time</td><td>${lib.value.buildTime}</td></tr>
									<tr class="sub"><td>Build id</td><td>${buildDetailedLibraries[lib.key].buildId}</td></tr>
									<tr class="sub">
										<td>${manifests[lib.key].buildPair.left}</td>
										<td>${manifests[lib.key].buildPair.right}</td>
									</tr>
								</c:when>
								<c:otherwise>
									<tr>
										<td colspan="2" class="filterable-value"
												title="${fn:escapeXml(manifests[lib].manifestString)}">
											${lib}
										</td>
									</tr>
									<c:if test="${not empty manifests[lib].buildPair}">
										<tr class="sub">
											<td>${manifests[lib].buildPair.left}</td>
											<td>${manifests[lib].buildPair.right}</td>
										</tr>
									</c:if>
								</c:otherwise>
							</c:choose>
						</tbody>
					</table>
				</div>
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