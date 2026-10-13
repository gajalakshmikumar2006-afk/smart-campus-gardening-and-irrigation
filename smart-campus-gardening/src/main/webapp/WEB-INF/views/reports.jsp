<%@ include file="/WEB-INF/views/layout/header.jspf" %>

<div class="page-title">
  <div><h1><i class="bi bi-file-earmark-text text-success me-2"></i>Reports</h1><p>Filter by date, garden and activity type. Print or export to CSV.</p></div>
  <div class="d-flex gap-2 no-print">
    <button class="btn btn-outline-green" data-print><i class="bi bi-printer me-1"></i>Print</button>
    <a class="btn btn-green" href="${ctx}/reports?from=${from}&amp;to=${to}&amp;gardenId=${filterGardenId}&amp;type=${filterType}&amp;format=csv"><i class="bi bi-filetype-csv me-1"></i>Export CSV</a>
  </div>
</div>

<form method="get" action="${ctx}/reports" class="card-soft p-3 mb-4 no-print">
  <div class="row g-2 align-items-end">
    <div class="col-6 col-md-2"><label class="form-label small mb-1">From</label><input type="date" class="form-control" name="from" value="${from}"></div>
    <div class="col-6 col-md-2"><label class="form-label small mb-1">To</label><input type="date" class="form-control" name="to" value="${to}"></div>
    <div class="col-md-3"><label class="form-label small mb-1">Garden</label>
      <select class="form-select" name="gardenId"><option value="0">All gardens</option>
        <c:forEach var="g" items="${gardens}"><option value="${g.gardenId}" ${filterGardenId == g.gardenId ? 'selected' : ''}><c:out value="${g.gardenName}"/></option></c:forEach></select></div>
    <div class="col-md-3"><label class="form-label small mb-1">Activity type</label>
      <select class="form-select" name="type">
        <option value="ALL" ${filterType == 'ALL' ? 'selected' : ''}>All activities</option>
        <option value="IRRIGATION" ${filterType == 'IRRIGATION' ? 'selected' : ''}>Irrigation</option>
        <option value="MAINTENANCE" ${filterType == 'MAINTENANCE' ? 'selected' : ''}>Maintenance</option></select></div>
    <div class="col-md-2"><button class="btn btn-green w-100"><i class="bi bi-funnel me-1"></i>Generate</button></div>
  </div>
</form>

<div class="row g-3 mb-4">
  <div class="col-md-4"><div class="card-soft stat-card"><div class="stat-icon green"><i class="bi bi-droplet"></i></div><div><div class="stat-value">${irrigationCount}</div><div class="stat-label">Irrigation activities</div></div></div></div>
  <div class="col-md-4"><div class="card-soft stat-card"><div class="stat-icon beige"><i class="bi bi-tools"></i></div><div><div class="stat-value">${taskCount}</div><div class="stat-label">Maintenance tasks</div></div></div></div>
  <div class="col-md-4"><div class="card-soft stat-card"><div class="stat-icon dark"><i class="bi bi-droplet-fill"></i></div><div><div class="stat-value"><fmt:formatNumber value="${reportLitres}" maxFractionDigits="1"/> L</div><div class="stat-label">Estimated water used</div></div></div></div>
</div>

<div class="table-wrap">
  <div class="table-toolbar"><strong>Campus Garden Report</strong><span class="text-muted small">${from} to ${to} &middot; water volumes are estimates</span></div>
  <div class="table-responsive">
    <table class="table align-middle">
      <thead><tr><th>Date</th><th>Type</th><th>Garden</th><th>Details</th><th>Staff</th><th>Status</th></tr></thead>
      <tbody>
        <c:forEach var="r" items="${rows}">
          <tr><td class="text-nowrap"><fmt:formatDate value="${r.activityDate}" pattern="dd MMM yyyy, hh:mm a"/></td>
            <td><span class="badge ${r.activityType == 'IRRIGATION' ? 'badge-rescheduled' : 'badge-pending'}">${r.activityType}</span></td>
            <td><c:out value="${r.gardenName}"/></td><td><c:out value="${r.details}"/></td>
            <td><c:out value="${empty r.staffName ? '-' : r.staffName}"/></td>
            <td><span class="badge badge-${fn:toLowerCase(r.status)}">${fn:replace(r.status,'_',' ')}</span></td></tr>
        </c:forEach>
        <c:if test="${empty rows}"><tr><td colspan="6"><div class="empty-state"><i class="bi bi-file-earmark"></i>No activities match these filters.</div></td></tr></c:if>
      </tbody>
    </table>
  </div>
</div>
<%@ include file="/WEB-INF/views/layout/footer.jspf" %>
