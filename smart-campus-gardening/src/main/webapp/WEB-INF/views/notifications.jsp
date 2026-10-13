<%@ include file="/WEB-INF/views/layout/header.jspf" %>

<div class="page-title">
  <div><h1><i class="bi bi-bell text-warning me-2"></i>Notifications &amp; Alerts</h1><p>In-app reminders, task alerts and schedule changes.</p></div>
  <c:if test="${not empty unread}">
    <form method="post" action="${ctx}/notifications"><input type="hidden" name="_csrf" value="${sessionScope.csrfToken}"><input type="hidden" name="action" value="readAll">
      <button class="btn btn-outline-green"><i class="bi bi-check2-all me-1"></i>Mark all as read</button></form>
  </c:if>
</div>

<div class="row g-4">
  <div class="col-lg-7">
    <h5 class="mb-3">Unread <span class="badge text-bg-warning">${fn:length(unread)}</span></h5>
    <c:forEach var="n" items="${unread}">
      <div class="notif-item">
        <div><span class="badge badge-active me-2">${n.notificationType}</span><c:out value="${n.message}"/><br>
          <small class="text-muted"><fmt:formatDate value="${n.createdAt}" pattern="dd MMM yyyy, hh:mm a"/></small></div>
        <form method="post" action="${ctx}/notifications"><input type="hidden" name="_csrf" value="${sessionScope.csrfToken}">
          <input type="hidden" name="action" value="read"><input type="hidden" name="id" value="${n.notificationId}">
          <button class="btn btn-sm btn-outline-green text-nowrap"><i class="bi bi-check2"></i> Mark read</button></form>
      </div>
    </c:forEach>
    <c:if test="${empty unread}"><div class="card-soft"><div class="empty-state"><i class="bi bi-bell-slash"></i>No unread notifications.</div></div></c:if>

    <h5 class="mt-4 mb-3">Read</h5>
    <c:forEach var="n" items="${read}">
      <div class="notif-item read"><div><span class="badge badge-inactive me-2">${n.notificationType}</span><c:out value="${n.message}"/><br>
        <small class="text-muted"><fmt:formatDate value="${n.createdAt}" pattern="dd MMM yyyy, hh:mm a"/></small></div></div>
    </c:forEach>
    <c:if test="${empty read}"><p class="text-muted">Nothing here yet.</p></c:if>
  </div>

  <div class="col-lg-5">
    <div class="card-soft mb-3"><div class="card-header"><i class="bi bi-droplet me-1 text-primary"></i>Irrigation reminders (next 48 hours)</div>
      <div class="list-group list-group-flush">
        <c:forEach var="s" items="${dueSoon}">
          <div class="list-group-item"><div class="fw-semibold"><c:out value="${s.gardenName}"/></div>
            <small class="text-muted"><fmt:formatDate value="${s.scheduledAt}" pattern="dd MMM, hh:mm a"/> &middot; <c:out value="${empty s.assignedUserName ? 'Unassigned' : s.assignedUserName}"/></small></div>
        </c:forEach>
        <c:if test="${empty dueSoon}"><div class="empty-state">Nothing due soon.</div></c:if>
      </div></div>
    <div class="card-soft"><div class="card-header"><i class="bi bi-tools me-1 text-success"></i>Pending maintenance tasks</div>
      <div class="list-group list-group-flush">
        <c:forEach var="t" items="${pendingTasks}">
          <div class="list-group-item d-flex justify-content-between align-items-center">
            <div><div class="fw-semibold"><c:out value="${t.title}"/></div><small class="text-muted"><c:out value="${t.gardenName}"/> &middot; due <fmt:formatDate value="${t.dueDate}" pattern="dd MMM"/></small></div>
            <c:choose><c:when test="${t.overdue}"><span class="badge badge-overdue">OVERDUE</span></c:when><c:otherwise><span class="badge badge-${fn:toLowerCase(t.status)}">${fn:replace(t.status,'_',' ')}</span></c:otherwise></c:choose>
          </div>
        </c:forEach>
        <c:if test="${empty pendingTasks}"><div class="empty-state">No pending tasks.</div></c:if>
      </div></div>
  </div>
</div>
<div class="alert alert-light border mt-4 small"><i class="bi bi-envelope me-1"></i>E-mail notifications are an optional extension - see the README section "Optional: e-mail notifications".</div>
<%@ include file="/WEB-INF/views/layout/footer.jspf" %>
