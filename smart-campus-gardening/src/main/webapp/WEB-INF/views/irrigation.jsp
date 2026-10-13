<%@ include file="/WEB-INF/views/layout/header.jspf" %>

<div class="page-title">
  <div><h1><i class="bi bi-droplet-half text-primary me-2"></i>Irrigation Scheduling</h1>
    <p>Plan watering and record completion. This is a software schedule - it does not operate any physical equipment.</p></div>
  <c:if test="${isAdmin}">
    <button class="btn btn-green" data-bs-toggle="modal" data-bs-target="#scheduleModal" data-mode="create" data-title="Create irrigation schedule"><i class="bi bi-plus-lg me-1"></i>New schedule</button>
  </c:if>
</div>

<!-- ============ Smart recommendations ============ -->
<h5 class="mb-3"><i class="bi bi-lightbulb text-warning me-1"></i>Smart recommendations</h5>
<div class="row g-3 mb-4">
  <c:forEach var="r" items="${recommendations}">
    <div class="col-md-6 col-xl-4">
      <div class="card-soft reco-card ${r.waterNow ? '' : 'postpone'} h-100">
        <div class="card-body">
          <div class="d-flex justify-content-between align-items-start mb-1">
            <h6 class="mb-0"><c:out value="${r.gardenName}"/></h6>
            <span class="badge ${r.waterNow ? 'badge-completed' : 'badge-pending'}"><c:out value="${r.waterNow ? 'WATER' : 'POSTPONE'}"/></span>
          </div>
          <div class="fw-semibold mb-1"><c:out value="${r.headline}"/>
            <c:if test="${r.liters > 0}"> &middot; <span class="text-success">~<fmt:formatNumber value="${r.liters}" maxFractionDigits="1"/> L (estimated)</span></c:if></div>
          <c:if test="${r.weatherIsDemo}"><span class="badge badge-demo mb-1">Uses DEMO weather</span></c:if>
          <ul><c:forEach var="why" items="${r.reasons}"><li><c:out value="${why}"/></li></c:forEach></ul>
          <c:if test="${isAdmin and r.liters > 0}">
            <button class="btn btn-sm btn-outline-green mt-3" data-bs-toggle="modal" data-bs-target="#scheduleModal" data-mode="create" data-title="Create irrigation schedule"
              data-garden-id="${r.gardenId}" data-estimated-liters="${r.liters}"><i class="bi bi-calendar-plus me-1"></i>Schedule this</button>
          </c:if>
        </div>
      </div>
    </div>
  </c:forEach>
  <c:if test="${empty recommendations}"><div class="col-12"><div class="empty-state">No gardens available.</div></div></c:if>
</div>

<!-- ============ Upcoming ============ -->
<h5 class="mb-3">Upcoming schedules</h5>
<div class="table-wrap mb-4">
  <div class="table-responsive">
    <table class="table align-middle">
      <thead><tr><th>Garden</th><th>Date &amp; time</th><th>Est. water</th><th>Assigned to</th><th>Status</th><th class="text-end">Actions</th></tr></thead>
      <tbody>
        <c:forEach var="s" items="${upcoming}">
          <tr>
            <td class="fw-semibold"><c:out value="${s.gardenName}"/><br><small class="text-muted"><c:out value="${s.notes}"/></small></td>
            <td><fmt:formatDate value="${s.scheduledAt}" pattern="dd MMM yyyy, hh:mm a"/></td>
            <td><fmt:formatNumber value="${s.estimatedWaterLiters}" maxFractionDigits="1"/> L</td>
            <td><c:out value="${empty s.assignedUserName ? 'Unassigned' : s.assignedUserName}"/></td>
            <td><span class="badge badge-${fn:toLowerCase(s.status)}">${s.status}</span></td>
            <td class="text-end text-nowrap">
              <c:if test="${isAdmin or s.assignedUserId eq me.userId}">
                <button class="btn btn-sm btn-green" data-bs-toggle="modal" data-bs-target="#completeModal" data-mode="status" data-id="${s.scheduleId}"
                  data-liters="${s.estimatedWaterLiters}" data-garden-label="<c:out value='${s.gardenName}'/>"><i class="bi bi-check2-circle me-1"></i>Complete</button>
                <div class="btn-group">
                  <button class="btn btn-sm btn-outline-secondary dropdown-toggle" data-bs-toggle="dropdown">Status</button>
                  <ul class="dropdown-menu dropdown-menu-end">
                    <c:forEach var="st" items="${['PENDING','RESCHEDULED']}">
                      <li><form method="post" action="${ctx}/irrigation"><input type="hidden" name="_csrf" value="${sessionScope.csrfToken}">
                        <input type="hidden" name="action" value="status"><input type="hidden" name="id" value="${s.scheduleId}"><input type="hidden" name="status" value="${st}">
                        <button class="dropdown-item">Mark ${fn:toLowerCase(st)}</button></form></li>
                    </c:forEach>
                  </ul>
                </div>
              </c:if>
              <c:if test="${isAdmin}">
                <button class="btn btn-outline-green btn-icon" title="Edit" data-bs-toggle="modal" data-bs-target="#scheduleModal" data-mode="update" data-title="Edit irrigation schedule"
                  data-id="${s.scheduleId}" data-garden-id="${s.gardenId}" data-schedule-date="<fmt:formatDate value='${s.scheduledAt}' pattern='yyyy-MM-dd'/>"
                  data-schedule-time="<fmt:formatDate value='${s.scheduledAt}' pattern='HH:mm'/>" data-estimated-liters="${s.estimatedWaterLiters}"
                  data-assigned-user-id="${empty s.assignedUserId ? 0 : s.assignedUserId}" data-status="${s.status}" data-notes="<c:out value='${s.notes}'/>"><i class="bi bi-pencil"></i></button>
                <form method="post" action="${ctx}/irrigation" class="d-inline" data-confirm="Cancel this irrigation schedule?">
                  <input type="hidden" name="_csrf" value="${sessionScope.csrfToken}"><input type="hidden" name="action" value="cancel"><input type="hidden" name="id" value="${s.scheduleId}">
                  <button class="btn btn-outline-danger btn-icon" title="Cancel schedule"><i class="bi bi-x-circle"></i></button></form>
              </c:if>
            </td>
          </tr>
        </c:forEach>
        <c:if test="${empty upcoming}"><tr><td colspan="6"><div class="empty-state"><i class="bi bi-calendar-check"></i>No upcoming schedules.</div></td></tr></c:if>
      </tbody>
    </table>
  </div>
</div>

<!-- ============ History ============ -->
<h5 class="mb-3">Completed &amp; cancelled</h5>
<div class="table-wrap">
  <div class="table-responsive">
    <table class="table align-middle">
      <thead><tr><th>Garden</th><th>Date</th><th>Estimated water</th><th>Staff</th><th>Status</th></tr></thead>
      <tbody>
        <c:forEach var="s" items="${history}">
          <tr><td class="fw-semibold"><c:out value="${s.gardenName}"/></td>
            <td><fmt:formatDate value="${s.scheduledAt}" pattern="dd MMM yyyy, hh:mm a"/></td>
            <td><fmt:formatNumber value="${s.estimatedWaterLiters}" maxFractionDigits="1"/> L</td>
            <td><c:out value="${empty s.assignedUserName ? '-' : s.assignedUserName}"/></td>
            <td><span class="badge badge-${fn:toLowerCase(s.status)}">${s.status}</span></td></tr>
        </c:forEach>
        <c:if test="${empty history}"><tr><td colspan="5"><div class="empty-state">No history yet.</div></td></tr></c:if>
      </tbody>
    </table>
  </div>
</div>

<!-- ============ Complete modal (admin or assigned staff) ============ -->
<div class="modal fade" id="completeModal" tabindex="-1" aria-hidden="true">
  <div class="modal-dialog modal-dialog-centered"><div class="modal-content">
    <form method="post" action="${ctx}/irrigation" class="entity-form">
      <div class="modal-header"><h5 class="modal-title">Mark irrigation completed</h5><button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button></div>
      <div class="modal-body">
        <input type="hidden" name="_csrf" value="${sessionScope.csrfToken}"><input type="hidden" name="action" value="status"><input type="hidden" name="id">
        <input type="hidden" name="status" value="COMPLETED">
        <div class="mb-3"><label class="form-label">Estimated water used (litres)</label>
          <input type="number" step="0.1" min="0.1" class="form-control" name="liters">
          <div class="form-text">Defaults to the scheduled estimate. This is an estimate - no flow sensor is used.</div></div>
        <div class="mb-0"><label class="form-label">Notes</label><input class="form-control" name="notes" maxlength="255"></div>
      </div>
      <div class="modal-footer"><button type="button" class="btn btn-light" data-bs-dismiss="modal">Cancel</button><button class="btn btn-green">Save as completed</button></div>
    </form>
  </div></div>
</div>

<!-- ============ Create / edit modal (admin) ============ -->
<c:if test="${isAdmin}">
<div class="modal fade" id="scheduleModal" tabindex="-1" aria-hidden="true">
  <div class="modal-dialog modal-dialog-centered modal-lg"><div class="modal-content">
    <form method="post" action="${ctx}/irrigation" class="entity-form">
      <div class="modal-header"><h5 class="modal-title">Irrigation schedule</h5><button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button></div>
      <div class="modal-body">
        <input type="hidden" name="_csrf" value="${sessionScope.csrfToken}"><input type="hidden" name="action" value="create"><input type="hidden" name="id">
        <div class="row g-3">
          <div class="col-md-6"><label class="form-label">Garden *</label>
            <select class="form-select" name="gardenId" id="schedGarden" required>
              <c:forEach var="g" items="${gardens}"><option value="${g.gardenId}"><c:out value="${g.gardenName}"/></option></c:forEach></select></div>
          <div class="col-md-6"><label class="form-label">Assign to staff</label>
            <select class="form-select" name="assignedUserId"><option value="0">- Unassigned -</option>
              <c:forEach var="u" items="${staff}"><option value="${u.userId}"><c:out value="${u.fullName}"/></option></c:forEach></select></div>
          <div class="col-md-4"><label class="form-label">Date *</label><input type="date" class="form-control" name="scheduleDate" required></div>
          <div class="col-md-4"><label class="form-label">Time *</label><input type="time" class="form-control" name="scheduleTime" value="06:00" required></div>
          <div class="col-md-4"><label class="form-label">Estimated water (L) *</label><input type="number" step="0.1" min="0.1" class="form-control" name="estimatedLiters" id="schedLiters" required></div>
          <div class="col-md-6 d-none" data-show-on="update"><label class="form-label">Status</label>
            <select class="form-select" name="status"><option value="PENDING">Pending</option><option value="RESCHEDULED">Rescheduled</option></select></div>
          <div class="col-12"><label class="form-label">Notes</label><input class="form-control" name="notes" maxlength="255"></div>
          <div class="col-12"><div id="recoBox" class="notice-estimate d-none"></div></div>
        </div>
      </div>
      <div class="modal-footer"><button type="button" class="btn btn-light" data-bs-dismiss="modal">Cancel</button><button class="btn btn-green"><i class="bi bi-check2 me-1"></i>Save schedule</button></div>
    </form>
  </div></div>
</div>
<script>
  // Shows the recommendation (why water / why postpone) for the selected garden inside the form.
  (function () {
    var modal = document.getElementById('scheduleModal'), sel = document.getElementById('schedGarden'),
        box = document.getElementById('recoBox'), liters = document.getElementById('schedLiters');
    function esc(s) { var d = document.createElement('div'); d.textContent = s; return d.innerHTML; }
    function load() {
      box.classList.add('d-none');
      if (!sel.value) return;
      fetch('${ctx}/irrigation?action=recommend&gardenId=' + encodeURIComponent(sel.value), { credentials: 'same-origin' })
        .then(function (r) { return r.json(); })
        .then(function (d) {
          var html = '<strong>' + (d.waterNow ? '&#9989; ' : '&#9888;&#65039; ') + esc(d.headline) + '</strong>' +
            (d.liters > 0 ? ' &middot; suggested ~' + d.liters + ' L (estimated)' : '') +
            '<ul class="mb-1 mt-1">' + d.reasons.map(function (x) { return '<li>' + esc(x) + '</li>'; }).join('') + '</ul>';
          if (d.liters > 0) html += '<button type="button" class="btn btn-sm btn-outline-secondary" id="useReco">Use suggested quantity</button>';
          box.innerHTML = html; box.classList.remove('d-none');
          var b = document.getElementById('useReco');
          if (b) b.addEventListener('click', function () { liters.value = d.liters; });
        }).catch(function () { box.classList.add('d-none'); });
    }
    sel.addEventListener('change', load);
    modal.addEventListener('entity:filled', load);
  })();
</script>
</c:if>
<%@ include file="/WEB-INF/views/layout/footer.jspf" %>
