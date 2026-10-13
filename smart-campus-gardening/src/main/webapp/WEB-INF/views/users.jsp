<%@ include file="/WEB-INF/views/layout/header.jspf" %>

<div class="page-title">
  <div><h1><i class="bi bi-people text-success me-2"></i>User Management</h1><p>Administrators and maintenance staff. Deactivated users cannot sign in.</p></div>
  <button class="btn btn-green" data-bs-toggle="modal" data-bs-target="#userModal" data-mode="create" data-title="Add user"><i class="bi bi-person-plus me-1"></i>Add user</button>
</div>

<div class="table-wrap">
  <div class="table-toolbar"><div class="search-box"><i class="bi bi-search"></i><input type="search" class="form-control form-control-sm" placeholder="Search users..." data-table-filter="#userTable"></div></div>
  <div class="table-responsive">
    <table class="table align-middle" id="userTable">
      <thead><tr><th>Name</th><th>Email</th><th>Phone</th><th>Role</th><th>Status</th><th class="text-end">Actions</th></tr></thead>
      <tbody>
        <c:forEach var="u" items="${users}">
          <tr>
            <td class="fw-semibold"><c:out value="${u.fullName}"/><c:if test="${u.userId == me.userId}"> <span class="badge text-bg-light border">You</span></c:if></td>
            <td><c:out value="${u.email}"/></td><td><c:out value="${empty u.phone ? '-' : u.phone}"/></td>
            <td><span class="badge ${u.admin ? 'badge-rescheduled' : 'badge-active'}">${u.role}</span></td>
            <td><span class="badge badge-${fn:toLowerCase(u.status)}">${u.status}</span></td>
            <td class="text-end text-nowrap">
              <button class="btn btn-outline-green btn-icon" title="Edit" data-bs-toggle="modal" data-bs-target="#userModal" data-mode="update" data-title="Edit user"
                data-id="${u.userId}" data-full-name="<c:out value='${u.fullName}'/>" data-email="<c:out value='${u.email}'/>" data-phone="<c:out value='${u.phone}'/>"
                data-role="${u.role}" data-status="${u.status}"><i class="bi bi-pencil"></i></button>
              <button class="btn btn-outline-secondary btn-icon" title="Reset password" data-bs-toggle="modal" data-bs-target="#pwModal" data-id="${u.userId}" data-mode="resetPassword"><i class="bi bi-key"></i></button>
              <c:if test="${u.userId != me.userId}">
                <form method="post" action="${ctx}/users" class="d-inline" data-confirm="${u.active ? 'Deactivate' : 'Activate'} this user?">
                  <input type="hidden" name="_csrf" value="${sessionScope.csrfToken}"><input type="hidden" name="id" value="${u.userId}">
                  <input type="hidden" name="action" value="${u.active ? 'deactivate' : 'activate'}">
                  <button class="btn ${u.active ? 'btn-outline-danger' : 'btn-outline-success'} btn-icon" title="${u.active ? 'Deactivate' : 'Activate'}"><i class="bi ${u.active ? 'bi-person-x' : 'bi-person-check'}"></i></button></form>
              </c:if>
            </td>
          </tr>
        </c:forEach>
      </tbody>
    </table>
  </div>
</div>

<div class="modal fade" id="userModal" tabindex="-1" aria-hidden="true">
  <div class="modal-dialog modal-dialog-centered modal-lg"><div class="modal-content">
    <form method="post" action="${ctx}/users" class="entity-form">
      <div class="modal-header"><h5 class="modal-title">User</h5><button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button></div>
      <div class="modal-body">
        <input type="hidden" name="_csrf" value="${sessionScope.csrfToken}"><input type="hidden" name="action" value="create"><input type="hidden" name="id">
        <div class="row g-3">
          <div class="col-md-6"><label class="form-label">Full name *</label><input class="form-control" name="fullName" required minlength="2" maxlength="100"></div>
          <div class="col-md-6"><label class="form-label">Email *</label><input type="email" class="form-control" name="email" required maxlength="120"></div>
          <div class="col-md-4"><label class="form-label">Phone</label><input class="form-control" name="phone" maxlength="20"></div>
          <div class="col-md-4"><label class="form-label">Role *</label><select class="form-select" name="role"><option value="STAFF">Maintenance staff</option><option value="ADMIN">Administrator</option></select></div>
          <div class="col-md-4"><label class="form-label">Status *</label><select class="form-select" name="status"><option value="ACTIVE">Active</option><option value="INACTIVE">Inactive</option></select></div>
          <div class="col-md-6" data-show-on="create"><label class="form-label">Password *</label><input type="password" class="form-control" name="password" minlength="8" maxlength="64" autocomplete="new-password">
            <div class="form-text">8-64 characters, with at least one letter and one digit.</div></div>
        </div>
      </div>
      <div class="modal-footer"><button type="button" class="btn btn-light" data-bs-dismiss="modal">Cancel</button><button class="btn btn-green">Save user</button></div>
    </form>
  </div></div>
</div>

<div class="modal fade" id="pwModal" tabindex="-1" aria-hidden="true">
  <div class="modal-dialog modal-dialog-centered"><div class="modal-content">
    <form method="post" action="${ctx}/users" class="entity-form">
      <div class="modal-header"><h5 class="modal-title">Reset password</h5><button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button></div>
      <div class="modal-body">
        <input type="hidden" name="_csrf" value="${sessionScope.csrfToken}"><input type="hidden" name="action" value="resetPassword"><input type="hidden" name="id">
        <label class="form-label">New password *</label><input type="password" class="form-control" name="newPassword" required minlength="8" maxlength="64" autocomplete="new-password">
        <div class="form-text">8-64 characters, with at least one letter and one digit.</div>
      </div>
      <div class="modal-footer"><button type="button" class="btn btn-light" data-bs-dismiss="modal">Cancel</button><button class="btn btn-green">Reset password</button></div>
    </form>
  </div></div>
</div>
<%@ include file="/WEB-INF/views/layout/footer.jspf" %>
