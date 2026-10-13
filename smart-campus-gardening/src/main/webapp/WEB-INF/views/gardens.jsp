<%@ include file="/WEB-INF/views/layout/header.jspf" %>

<div class="page-title">
  <div><h1><i class="bi bi-tree text-success me-2"></i>Gardens</h1><p>All campus gardens, their size, type and current status.</p></div>
  <c:if test="${isAdmin}">
    <button class="btn btn-green" data-bs-toggle="modal" data-bs-target="#gardenModal" data-mode="create" data-title="Add new garden">
      <i class="bi bi-plus-lg me-1"></i>Add garden</button>
  </c:if>
</div>

<div class="table-wrap">
  <div class="table-toolbar">
    <div class="search-box"><i class="bi bi-search"></i><input type="search" class="form-control" placeholder="Search gardens..." data-table-filter="#gardenTable"></div>
    <span class="text-muted small">${fn:length(gardens)} garden(s)</span>
  </div>
  <div class="table-responsive">
    <table class="table align-middle" id="gardenTable">
      <thead><tr><th>Garden</th><th>Location</th><th>Area (m²)</th><th>Type</th><th>Plants</th><th>Status</th><c:if test="${isAdmin}"><th class="text-end">Actions</th></c:if></tr></thead>
      <tbody>
        <c:forEach var="g" items="${gardens}">
          <tr>
            <td><div class="fw-semibold"><c:out value="${g.gardenName}"/></div><small class="text-muted"><c:out value="${g.description}"/></small></td>
            <td><c:out value="${g.location}"/></td>
            <td><fmt:formatNumber value="${g.areaSqm}" maxFractionDigits="1"/></td>
            <td><span class="badge text-bg-light border"><c:out value="${g.gardenType}"/></span></td>
            <td><fmt:formatNumber value="${g.plantCount}"/></td>
            <td><span class="badge badge-${fn:toLowerCase(g.status)}">${fn:replace(g.status, '_', ' ')}</span></td>
            <c:if test="${isAdmin}">
              <td class="text-end text-nowrap">
                <button class="btn btn-outline-green btn-icon" title="Edit" data-bs-toggle="modal" data-bs-target="#gardenModal"
                  data-mode="update" data-title="Edit garden" data-id="${g.gardenId}"
                  data-garden-name="<c:out value='${g.gardenName}'/>" data-location="<c:out value='${g.location}'/>"
                  data-area-sqm="${g.areaSqm}" data-garden-type="<c:out value='${g.gardenType}'/>"
                  data-description="<c:out value='${g.description}'/>" data-status="${g.status}"><i class="bi bi-pencil"></i></button>
                <form method="post" action="${ctx}/gardens" class="d-inline" data-confirm="Delete '${fn:escapeXml(g.gardenName)}'? Its plants, irrigation schedules and tasks will also be deleted.">
                  <input type="hidden" name="_csrf" value="${sessionScope.csrfToken}"><input type="hidden" name="action" value="delete"><input type="hidden" name="id" value="${g.gardenId}">
                  <button class="btn btn-outline-danger btn-icon" title="Delete"><i class="bi bi-trash"></i></button></form>
              </td>
            </c:if>
          </tr>
        </c:forEach>
        <c:if test="${empty gardens}"><tr class="empty-row"><td colspan="7"><div class="empty-state"><i class="bi bi-tree"></i>No gardens yet.</div></td></tr></c:if>
      </tbody>
    </table>
  </div>
</div>

<c:if test="${isAdmin}">
<div class="modal fade" id="gardenModal" tabindex="-1" aria-hidden="true">
  <div class="modal-dialog modal-dialog-centered modal-lg"><div class="modal-content">
    <form method="post" action="${ctx}/gardens" class="entity-form">
      <div class="modal-header"><h5 class="modal-title">Garden</h5><button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button></div>
      <div class="modal-body">
        <input type="hidden" name="_csrf" value="${sessionScope.csrfToken}"><input type="hidden" name="action" value="create"><input type="hidden" name="id">
        <div class="row g-3">
          <div class="col-md-6"><label class="form-label">Garden name *</label><input class="form-control" name="gardenName" required maxlength="100"></div>
          <div class="col-md-6"><label class="form-label">Location *</label><input class="form-control" name="location" required maxlength="150"></div>
          <div class="col-md-4"><label class="form-label">Area (m²) *</label><input type="number" step="0.01" min="0.01" class="form-control" name="areaSqm" required></div>
          <div class="col-md-4"><label class="form-label">Garden type *</label>
            <select class="form-select" name="gardenType" required><c:forEach var="t" items="${gardenTypes}"><option value="${t}">${t}</option></c:forEach></select></div>
          <div class="col-md-4"><label class="form-label">Status *</label>
            <select class="form-select" name="status" required><option value="ACTIVE">Active</option><option value="UNDER_MAINTENANCE">Under maintenance</option><option value="INACTIVE">Inactive</option></select></div>
          <div class="col-12"><label class="form-label">Description</label><textarea class="form-control" name="description" rows="3" maxlength="1000"></textarea></div>
        </div>
      </div>
      <div class="modal-footer"><button type="button" class="btn btn-light" data-bs-dismiss="modal">Cancel</button><button class="btn btn-green"><i class="bi bi-check2 me-1"></i>Save garden</button></div>
    </form>
  </div></div>
</div>
</c:if>
<%@ include file="/WEB-INF/views/layout/footer.jspf" %>
