<%@ include file="/WEB-INF/views/layout/header.jspf" %>

<div class="page-title">
  <div><h1><i class="bi bi-flower1 text-success me-2"></i>Plants</h1><p>Plant records linked to gardens, with watering requirements.</p></div>
  <c:if test="${isAdmin}">
    <button class="btn btn-green" data-bs-toggle="modal" data-bs-target="#plantModal" data-mode="create" data-title="Add plant"><i class="bi bi-plus-lg me-1"></i>Add plant</button>
  </c:if>
</div>

<div class="table-wrap">
  <div class="table-toolbar">
    <form method="get" action="${ctx}/plants" class="d-flex flex-wrap gap-2 align-items-center">
      <select name="gardenId" class="form-select form-select-sm" style="width:auto" onchange="this.form.submit()">
        <option value="0">All gardens</option>
        <c:forEach var="g" items="${gardens}"><option value="${g.gardenId}" ${filterGardenId == g.gardenId ? 'selected' : ''}><c:out value="${g.gardenName}"/></option></c:forEach>
      </select>
      <select name="category" class="form-select form-select-sm" style="width:auto" onchange="this.form.submit()">
        <option value="">All categories</option>
        <c:forEach var="c" items="${categories}"><option value="${c}" ${filterCategory == c ? 'selected' : ''}>${c}</option></c:forEach>
      </select>
      <c:if test="${filterGardenId > 0 or not empty filterCategory}"><a href="${ctx}/plants" class="btn btn-sm btn-light">Clear filters</a></c:if>
    </form>
    <div class="search-box"><i class="bi bi-search"></i><input type="search" class="form-control form-control-sm" placeholder="Search plants..." data-table-filter="#plantTable"></div>
  </div>
  <div class="table-responsive">
    <table class="table align-middle" id="plantTable">
      <thead><tr><th>Plant</th><th>Garden</th><th>Category</th><th>Qty</th><th>Watering requirement</th><th>Per watering</th><c:if test="${isAdmin}"><th class="text-end">Actions</th></c:if></tr></thead>
      <tbody>
        <c:forEach var="p" items="${plants}">
          <tr>
            <td><div class="fw-semibold"><c:out value="${p.plantName}"/></div><small class="text-muted"><c:out value="${p.description}"/></small></td>
            <td><c:out value="${p.gardenName}"/></td>
            <td><span class="badge text-bg-light border"><c:out value="${p.category}"/></span></td>
            <td><fmt:formatNumber value="${p.quantity}"/></td>
            <td><i class="bi bi-droplet text-primary"></i> Every <strong>${p.wateringFrequencyDays}</strong> day(s) &middot; <strong><fmt:formatNumber value="${p.waterRequirementLiters}" maxFractionDigits="2"/> L</strong>/plant</td>
            <td><span class="badge badge-completed"><fmt:formatNumber value="${p.quantity * p.waterRequirementLiters}" maxFractionDigits="1"/> L</span></td>
            <c:if test="${isAdmin}">
              <td class="text-end text-nowrap">
                <button class="btn btn-outline-green btn-icon" title="Edit" data-bs-toggle="modal" data-bs-target="#plantModal" data-mode="update" data-title="Edit plant"
                  data-id="${p.plantId}" data-garden-id="${p.gardenId}" data-plant-name="<c:out value='${p.plantName}'/>" data-category="<c:out value='${p.category}'/>"
                  data-quantity="${p.quantity}" data-watering-frequency-days="${p.wateringFrequencyDays}" data-water-requirement-liters="${p.waterRequirementLiters}"
                  data-description="<c:out value='${p.description}'/>"><i class="bi bi-pencil"></i></button>
                <form method="post" action="${ctx}/plants" class="d-inline" data-confirm="Delete this plant record?">
                  <input type="hidden" name="_csrf" value="${sessionScope.csrfToken}"><input type="hidden" name="action" value="delete"><input type="hidden" name="id" value="${p.plantId}">
                  <button class="btn btn-outline-danger btn-icon" title="Delete"><i class="bi bi-trash"></i></button></form>
              </td>
            </c:if>
          </tr>
        </c:forEach>
        <c:if test="${empty plants}"><tr class="empty-row"><td colspan="7"><div class="empty-state"><i class="bi bi-flower3"></i>No plants match.</div></td></tr></c:if>
      </tbody>
    </table>
  </div>
</div>

<c:if test="${isAdmin}">
<div class="modal fade" id="plantModal" tabindex="-1" aria-hidden="true">
  <div class="modal-dialog modal-dialog-centered modal-lg"><div class="modal-content">
    <form method="post" action="${ctx}/plants" class="entity-form">
      <div class="modal-header"><h5 class="modal-title">Plant</h5><button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button></div>
      <div class="modal-body">
        <input type="hidden" name="_csrf" value="${sessionScope.csrfToken}"><input type="hidden" name="action" value="create"><input type="hidden" name="id">
        <div class="row g-3">
          <div class="col-md-6"><label class="form-label">Garden *</label>
            <select class="form-select" name="gardenId" required><c:forEach var="g" items="${gardens}"><option value="${g.gardenId}"><c:out value="${g.gardenName}"/></option></c:forEach></select></div>
          <div class="col-md-6"><label class="form-label">Plant name *</label><input class="form-control" name="plantName" required maxlength="100"></div>
          <div class="col-md-4"><label class="form-label">Category *</label>
            <select class="form-select" name="category" required><c:forEach var="c" items="${categories}"><option value="${c}">${c}</option></c:forEach></select></div>
          <div class="col-md-4"><label class="form-label">Quantity *</label><input type="number" min="1" max="100000" class="form-control" name="quantity" value="1" required></div>
          <div class="col-md-4"><label class="form-label">Watering every (days) *</label><input type="number" min="1" max="60" class="form-control" name="wateringFrequencyDays" value="2" required></div>
          <div class="col-md-6"><label class="form-label">Water per plant per watering (litres) *</label><input type="number" min="0.01" step="0.01" class="form-control" name="waterRequirementLiters" value="1" required></div>
          <div class="col-12"><label class="form-label">Description</label><textarea class="form-control" name="description" rows="2" maxlength="1000"></textarea></div>
        </div>
      </div>
      <div class="modal-footer"><button type="button" class="btn btn-light" data-bs-dismiss="modal">Cancel</button><button class="btn btn-green"><i class="bi bi-check2 me-1"></i>Save plant</button></div>
    </form>
  </div></div>
</div>
</c:if>
<%@ include file="/WEB-INF/views/layout/footer.jspf" %>
