<%@ include file="/WEB-INF/views/layout/header.jspf" %>

<div class="page-title"><div><h1><i class="bi bi-person-circle text-success me-2"></i>My Profile</h1><p>Update your details and password.</p></div></div>

<div class="row g-4">
  <div class="col-lg-6"><div class="card-soft"><div class="card-header">Account details</div><div class="card-body">
    <div class="d-flex align-items-center gap-3 mb-3"><span class="avatar" style="width:56px;height:56px;font-size:1.4rem">${fn:toUpperCase(fn:substring(me.fullName,0,1))}</span>
      <div><div class="fw-semibold"><c:out value="${me.email}"/></div>
        <span class="badge ${isAdmin ? 'badge-rescheduled' : 'badge-active'}"><c:out value="${isAdmin ? 'Administrator' : 'Maintenance staff'}"/></span></div></div>
    <form method="post" action="${ctx}/profile">
      <input type="hidden" name="_csrf" value="${sessionScope.csrfToken}"><input type="hidden" name="action" value="profile">
      <div class="mb-3"><label class="form-label">Full name</label><input class="form-control" name="fullName" value="<c:out value='${me.fullName}'/>" required minlength="2" maxlength="100"></div>
      <div class="mb-3"><label class="form-label">Phone</label><input class="form-control" name="phone" value="<c:out value='${me.phone}'/>" maxlength="20"></div>
      <button class="btn btn-green">Save changes</button>
    </form></div></div></div>

  <div class="col-lg-6"><div class="card-soft"><div class="card-header">Change password</div><div class="card-body">
    <form method="post" action="${ctx}/profile">
      <input type="hidden" name="_csrf" value="${sessionScope.csrfToken}"><input type="hidden" name="action" value="password">
      <div class="mb-3"><label class="form-label">Current password</label><input type="password" class="form-control" name="currentPassword" required autocomplete="current-password"></div>
      <div class="mb-3"><label class="form-label">New password</label><input type="password" class="form-control" name="newPassword" required minlength="8" maxlength="64" autocomplete="new-password">
        <div class="form-text">8-64 characters, with at least one letter and one digit.</div></div>
      <div class="mb-3"><label class="form-label">Confirm new password</label><input type="password" class="form-control" name="confirmPassword" required autocomplete="new-password"></div>
      <button class="btn btn-green">Change password</button>
    </form></div></div></div>
</div>
<%@ include file="/WEB-INF/views/layout/footer.jspf" %>
