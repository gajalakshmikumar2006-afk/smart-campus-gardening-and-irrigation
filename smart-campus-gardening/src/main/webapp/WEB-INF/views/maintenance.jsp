<%@ include file="/WEB-INF/views/layout/header.jspf" %>

<div class="page-title">
  <div><h1><i class="bi bi-tools text-success me-2"></i>Maintenance</h1>
    <p><c:out value="${isAdmin ? 'Create, assign and track gardening tasks.' : 'Your assigned gardening tasks. Update the status as you work.'}"/></p></div>
  <c:if test="${isAdmin}">
    <button class="btn btn-green" data-bs-toggle="modal" data-bs-target="#taskModal" data-mode="create" data-title="Create maintenance task"><i class="bi bi-plus-lg me-1"></i>New task</button>
  </c:if>
</div>

<div class="d-flex flex-wrap gap-2 mb-3 align-items-center">
  <div class="btn-group" role="group">
    <a href="${ctx}/maintenance" class="btn btn-sm ${empty filterStatus ? 'btn-green' : 'btn-outline-green'}">All</a>
    <a href="${ctx}/maintenance?status=PENDING" class="btn btn-sm ${filterStatus == 'PENDING' ? 'btn-green' : 'btn-outline-green'}">Pending</a>
    <a href="${ctx}/maintenance?status=IN_PROGRESS" class="btn btn-sm ${filterStatus == 'IN_PROGRESS' ? 'btn-green' : 'btn-outline-green'}">In progress</a>
    <a href="${ctx}/maintenance?status=COMPLETED" class="btn btn-sm ${filterStatus == 'COMPLETED' ? 'btn-green' : 'btn-outline-green'}">Completed</a>
  </div>
  <c:if test="${overdueCount > 0}"><span class="badge badge-overdue"><i class="bi bi-exclamation-triangle me-1"></i>${overdueCount} overdue</span></c:if>
  <c:if test="${dueSoonCount > 0}"><span class="badge badge-pending"><i class="bi bi-clock me-1"></i>${dueSoonCount} due within 7 days</span></c:if>
</div>

<div class="table-wrap">
  <div class="table-toolbar"><div class="search-box"><i class="bi bi-search"></i><input type="search" class="form-control form-control-sm" placeholder="Search tasks..." data-table-filter="#taskTable"></div>
    <span class="text-muted small">${fn:length(tasks)} task(s)</span></div>
  <div class="table-responsive">
    <table class="table align-middle" id="taskTable">
      <thead><tr><th>Task</th><th>Garden</th><th>Assigned to</th><th>Priority</th><th>Due</th><th>Status</th><th class="text-end">Actions</th></tr></thead>
      <tbody>
        <c:forEach var="t" items="${tasks}">
          <tr>
            <td><div class="fw-semibold"><c:out value="${t.title}"/></div><small class="text-muted"><c:out value="${t.description}"/></small></td>
            <td><c:out value="${t.gardenName}"/></td>
            <td><c:out value="${empty t.assignedUserName ? 'Unassigned' : t.assignedUserName}"/></td>
            <td><span class="badge badge-${fn:toLowerCase(t.priority)}">${t.priority}</span></td>
            <td class="${t.overdue ? 'text-danger fw-semibold' : ''}"><fmt:formatDate value="${t.dueDate}" pattern="dd MMM yyyy"/>
              <c:if test="${t.overdue}"><br><span class="badge badge-overdue">OVERDUE</span></c:if></td>
            <td><span class="badge badge-${fn:toLowerCase(t.status)}">${fn:replace(t.status, '_', ' ')}</span></td>
            <td class="text-end text-nowrap">
              <c:if test="${isAdmin or t.assignedUserId eq me.userId}">
                <form method="post" action="${ctx}/maintenance" class="d-inline-flex gap-1">
                  <input type="hidden" name="_csrf" value="${sessionScope.csrfToken}"><input type="hidden" name="action" value="status"><input type="hidden" name="id" value="${t.taskId}">
                  <select name="status" class="form-select form-select-sm" style="width:130px">
                    <option value="PENDING" ${t.status == 'PENDING' ? 'selected' : ''}>Pending</option>
                    <option value="IN_PROGRESS" ${t.status == 'IN_PROGRESS' ? 'selected' : ''}>In progress</option>
                    <option value="COMPLETED" ${t.status == 'COMPLETED' ? 'selected' : ''}>Completed</option></select>
                  <button class="btn btn-sm btn-green" title="Update status"><i class="bi bi-check2"></i></button>
                </form>
              </c:if>
              <c:if test="${isAdmin}">
                <button class="btn btn-outline-green btn-icon ms-1" title="Edit" data-bs-toggle="modal" data-bs-target="#taskModal" data-mode="update" data-title="Edit task"
                  data-id="${t.taskId}" data-garden-id="${t.gardenId}" data-assigned-user-id="${empty t.assignedUserId ? 0 : t.assignedUserId}"
                  data-priority="${t.priority}" data-due-date="<fmt:formatDate value='${t.dueDate}' pattern='yyyy-MM-dd'/>"
                  data-status="${t.status}" data-description="<c:out value='${t.description}'/>" data-name-title="<c:out value='${t.title}'/>"><i class="bi bi-pencil"></i></button>
                <form method="post" action="${ctx}/maintenance" class="d-inline" data-confirm="Delete this task?">
                  <input type="hidden" name="_csrf" value="${sessionScope.csrfToken}"><input type="hidden" name="action" value="delete"><input type="hidden" name="id" value="${t.taskId}">
                  <button class="btn btn-outline-danger btn-icon" title="Delete"><i class="bi bi-trash"></i></button></form>
              </c:if>
            </td>
          </tr>
        </c:forEach>
        <c:if test="${empty tasks}"><tr class="empty-row"><td colspan="7"><div class="empty-state"><i class="bi bi-clipboard-check"></i>No tasks found.</div></td></tr></c:if>
      </tbody>
    </table>
  </div>
</div>

<c:if test="${isAdmin}">
<div class="modal fade" id="taskModal" tabindex="-1" aria-hidden="true">
  <div class="modal-dialog modal-dialog-centered modal-lg"><div class="modal-content">
    <form method="post" action="${ctx}/maintenance" class="entity-form" id="taskForm">
      <div class="modal-header"><h5 class="modal-title">Task</h5><button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button></div>
      <div class="modal-body">
        <input type="hidden" name="_csrf" value="${sessionScope.csrfToken}"><input type="hidden" name="action" value="create"><input type="hidden" name="id">
        <div class="row g-3">
          <div class="col-12"><label class="form-label">Title *</label><input class="form-control" name="title" id="taskTitleInput" required minlength="3" maxlength="150"></div>
          <div class="col-md-6"><label class="form-label">Garden *</label>
            <select class="form-select" name="gardenId" required><c:forEach var="g" items="${gardens}"><option value="${g.gardenId}"><c:out value="${g.gardenName}"/></option></c:forEach></select></div>
          <div class="col-md-6"><label class="form-label">Assign to</label>
            <select class="form-select" name="assignedUserId"><option value="0">- Unassigned -</option>
              <c:forEach var="u" items="${staff}"><option value="${u.userId}"><c:out value="${u.fullName}"/></option></c:forEach></select></div>
          <div class="col-md-4"><label class="form-label">Priority *</label>
            <select class="form-select" name="priority"><option value="LOW">Low</option><option value="MEDIUM" selected>Medium</option><option value="HIGH">High</option></select></div>
          <div class="col-md-4"><label class="form-label">Due date *</label><input type="date" class="form-control" name="dueDate" required></div>
          <div class="col-md-4 d-none" data-show-on="update"><label class="form-label">Status</label>
            <select class="form-select" name="status"><option value="PENDING">Pending</option><option value="IN_PROGRESS">In progress</option><option value="COMPLETED">Completed</option></select></div>
          <div class="col-12"><label class="form-label">Description</label><textarea class="form-control" name="description" rows="3" maxlength="2000"></textarea></div>
        </div>
      </div>
      <div class="modal-footer"><button type="button" class="btn btn-light" data-bs-dismiss="modal">Cancel</button><button class="btn btn-green"><i class="bi bi-check2 me-1"></i>Save task</button></div>
    </form>
  </div></div>
</div>
<script>
  // The edit button cannot use data-title (that sets the modal heading), so the task title travels in data-name-title.
  var taskModal = document.getElementById('taskModal');
  var lastBtn = null;
  taskModal.addEventListener('show.bs.modal', function (e) { lastBtn = e.relatedTarget; });
  taskModal.addEventListener('entity:filled', function () {          // fires after the generic form fill
    document.getElementById('taskTitleInput').value = lastBtn ? (lastBtn.getAttribute('data-name-title') || '') : '';
  });
</script>
</c:if>
<%@ include file="/WEB-INF/views/layout/footer.jspf" %>
