<%@ include file="/WEB-INF/views/layout/header.jspf" %>

<div class="hero mb-4">
  <h2>Welcome back, <c:out value="${fn:substringBefore(me.fullName, ' ')}"/> 🌿</h2>
  <p>Here is today's overview of the campus gardens.
    <jsp:useBean id="now" class="java.util.Date"/>
    <fmt:formatDate value="${now}" pattern="EEEE, dd MMMM yyyy"/></p>
</div>

<!-- ============ Key figures ============ -->
<div class="row g-3 mb-4">
  <div class="col-6 col-md-4 col-xl-3"><a href="${ctx}/gardens" class="text-decoration-none"><div class="card-soft hoverable stat-card">
    <div class="stat-icon green"><i class="bi bi-tree"></i></div>
    <div><div class="stat-value">${totalGardens}</div><div class="stat-label">Total gardens</div></div></div></a></div>
  <div class="col-6 col-md-4 col-xl-3"><a href="${ctx}/plants" class="text-decoration-none"><div class="card-soft hoverable stat-card">
    <div class="stat-icon beige"><i class="bi bi-flower1"></i></div>
    <div><div class="stat-value"><fmt:formatNumber value="${totalPlants}"/></div><div class="stat-label">Total plants</div></div></div></a></div>
  <div class="col-6 col-md-4 col-xl-3"><a href="${ctx}/irrigation" class="text-decoration-none"><div class="card-soft hoverable stat-card">
    <div class="stat-icon blue"><i class="bi bi-calendar-event"></i></div>
    <div><div class="stat-value">${upcomingIrrigations}</div><div class="stat-label">Upcoming irrigations</div></div></div></a></div>
  <div class="col-6 col-md-4 col-xl-3"><a href="${ctx}/irrigation" class="text-decoration-none"><div class="card-soft hoverable stat-card">
    <div class="stat-icon green"><i class="bi bi-check2-circle"></i></div>
    <div><div class="stat-value">${completedIrrigations}</div><div class="stat-label">Completed irrigations</div></div></div></a></div>
  <div class="col-6 col-md-4 col-xl-3"><a href="${ctx}/maintenance" class="text-decoration-none"><div class="card-soft hoverable stat-card">
    <div class="stat-icon beige"><i class="bi bi-tools"></i></div>
    <div><div class="stat-value">${pendingTasks}</div><div class="stat-label">Pending tasks
      <c:if test="${overdueTasks > 0}"><span class="badge badge-overdue ms-1">${overdueTasks} overdue</span></c:if></div></div></div></a></div>
  <div class="col-6 col-md-4 col-xl-3"><a href="${ctx}/water-analytics" class="text-decoration-none"><div class="card-soft hoverable stat-card">
    <div class="stat-icon dark"><i class="bi bi-droplet-fill"></i></div>
    <div><div class="stat-value"><fmt:formatNumber value="${totalWater}" maxFractionDigits="0"/> L</div><div class="stat-label">Estimated water used</div></div></div></a></div>
  <div class="col-6 col-md-4 col-xl-3"><a href="${ctx}/notifications" class="text-decoration-none"><div class="card-soft hoverable stat-card">
    <div class="stat-icon red"><i class="bi bi-bell"></i></div>
    <div><div class="stat-value">${unreadCount}</div><div class="stat-label">Unread notifications</div></div></div></a></div>
  <div class="col-6 col-md-4 col-xl-3"><a href="${ctx}/weather" class="text-decoration-none"><div class="card-soft hoverable stat-card">
    <div class="stat-icon blue"><i class="bi bi-cloud-sun"></i></div>
    <div>
      <c:choose>
        <c:when test="${not empty weather}"><div class="stat-value"><fmt:formatNumber value="${weather.temperatureC}" maxFractionDigits="0"/>&deg;C</div>
          <div class="stat-label"><c:out value="${weather.conditions}"/></div></c:when>
        <c:otherwise><div class="stat-value">--</div><div class="stat-label">No weather data</div></c:otherwise>
      </c:choose></div></div></a></div>
</div>

<!-- ============ Charts ============ -->
<div class="row g-3 mb-4">
  <div class="col-lg-6"><div class="card-soft h-100"><div class="card-header">Monthly water usage (estimated, litres)</div>
    <div class="card-body"><div class="chart-box"><canvas id="monthlyChart"></canvas></div></div></div></div>
  <div class="col-md-6 col-lg-3"><div class="card-soft h-100"><div class="card-header">Irrigation status</div>
    <div class="card-body"><div class="chart-box"><canvas id="irrigationChart"></canvas></div></div></div></div>
  <div class="col-md-6 col-lg-3"><div class="card-soft h-100"><div class="card-header">Maintenance status</div>
    <div class="card-body"><div class="chart-box"><canvas id="taskChart"></canvas></div></div></div></div>
</div>

<!-- ============ Weather / irrigation / alerts ============ -->
<div class="row g-3 mb-4">
  <div class="col-lg-4"><div class="card-soft h-100"><div class="card-header d-flex justify-content-between">Weather summary
      <c:if test="${weather.demo}"><span class="badge badge-demo">DEMO DATA</span></c:if></div>
    <div class="card-body">
      <c:choose>
        <c:when test="${not empty weather}">
          <div class="d-flex align-items-center gap-3">
            <div class="weather-temp"><fmt:formatNumber value="${weather.temperatureC}" maxFractionDigits="0"/>&deg;</div>
            <div><div class="fw-semibold"><c:out value="${weather.conditions}"/></div>
              <small class="text-muted"><c:out value="${weather.campusLocation}"/> &middot; humidity ${weather.humidity}%</small></div>
          </div>
          <hr>
          <div class="small">Rain expected next 24h: <strong><fmt:formatNumber value="${weather.rainMmForecast24h}" maxFractionDigits="1"/> mm</strong></div>
          <c:if test="${weather.rainMmForecast24h + weather.rainMmCurrent >= 5}"><div class="alert alert-warning py-2 mt-2 mb-0 small"><i class="bi bi-cloud-rain me-1"></i>Heavy rain forecast - consider postponing irrigation.</div></c:if>
          <div class="small text-muted mt-2">Updated <fmt:formatDate value="${weather.retrievedAt}" pattern="dd MMM, hh:mm a"/></div>
        </c:when>
        <c:otherwise><div class="empty-state"><i class="bi bi-cloud-slash"></i>No weather data yet. <a href="${ctx}/weather">Open the weather page</a>.</div></c:otherwise>
      </c:choose>
    </div></div></div>

  <div class="col-lg-4"><div class="card-soft h-100"><div class="card-header d-flex justify-content-between">Upcoming irrigation <a href="${ctx}/irrigation" class="small">View all</a></div>
    <div class="list-group list-group-flush">
      <c:forEach var="s" items="${upcomingList}">
        <div class="list-group-item d-flex justify-content-between align-items-center">
          <div><div class="fw-semibold"><c:out value="${s.gardenName}"/></div>
            <small class="text-muted"><fmt:formatDate value="${s.scheduledAt}" pattern="dd MMM, hh:mm a"/> &middot; <c:out value="${empty s.assignedUserName ? 'Unassigned' : s.assignedUserName}"/></small></div>
          <span class="badge badge-${fn:toLowerCase(s.status)}">${s.status}</span>
        </div>
      </c:forEach>
      <c:if test="${empty upcomingList}"><div class="empty-state"><i class="bi bi-droplet"></i>No upcoming irrigation.</div></c:if>
    </div></div></div>

  <div class="col-lg-4"><div class="card-soft h-100"><div class="card-header d-flex justify-content-between">Notifications &amp; alerts <a href="${ctx}/notifications" class="small">View all</a></div>
    <div class="list-group list-group-flush">
      <c:forEach var="n" items="${unreadList}">
        <div class="list-group-item small"><i class="bi bi-dot text-success fs-4 align-middle"></i><c:out value="${n.message}"/></div>
      </c:forEach>
      <c:if test="${empty unreadList}"><div class="empty-state"><i class="bi bi-bell-slash"></i>You're all caught up.</div></c:if>
    </div></div></div>
</div>

<!-- ============ Activity / tasks ============ -->
<div class="row g-3">
  <div class="col-lg-7"><div class="card-soft h-100"><div class="card-header">Recent gardening activities</div>
    <div class="list-group list-group-flush">
      <c:forEach var="a" items="${recentActivities}">
        <div class="list-group-item d-flex gap-3 align-items-start">
          <span class="badge badge-active mt-1">${a.activityType}</span>
          <div class="flex-grow-1"><c:out value="${a.description}"/><br>
            <small class="text-muted"><c:out value="${empty a.userName ? 'System' : a.userName}"/> &middot; <fmt:formatDate value="${a.createdAt}" pattern="dd MMM yyyy, hh:mm a"/></small></div>
        </div>
      </c:forEach>
      <c:if test="${empty recentActivities}"><div class="empty-state">No activity yet.</div></c:if>
    </div></div></div>
  <div class="col-lg-5"><div class="card-soft h-100"><div class="card-header d-flex justify-content-between">
      <c:out value="${isAdmin ? 'Open maintenance tasks' : 'My open tasks'}"/> <a href="${ctx}/maintenance" class="small">View all</a></div>
    <div class="list-group list-group-flush">
      <c:forEach var="t" items="${openTasks}">
        <div class="list-group-item d-flex justify-content-between align-items-center">
          <div><div class="fw-semibold"><c:out value="${t.title}"/></div>
            <small class="text-muted"><c:out value="${t.gardenName}"/> &middot; due <fmt:formatDate value="${t.dueDate}" pattern="dd MMM"/></small></div>
          <c:choose><c:when test="${t.overdue}"><span class="badge badge-overdue">OVERDUE</span></c:when>
            <c:otherwise><span class="badge badge-${fn:toLowerCase(t.priority)}">${t.priority}</span></c:otherwise></c:choose>
        </div>
      </c:forEach>
      <c:if test="${empty openTasks}"><div class="empty-state"><i class="bi bi-clipboard-check"></i>No open tasks.</div></c:if>
    </div></div></div>
</div>

<script src="https://cdn.jsdelivr.net/npm/chart.js@4.4.3/dist/chart.umd.min.js"></script>
<script>
  makeChart('monthlyChart', 'bar', ${monthlyLabels}, ${monthlyValues}, 'Estimated litres');
  makeChart('irrigationChart', 'doughnut', ${irrigationLabels}, ${irrigationValues}, 'Schedules');
  makeChart('taskChart', 'doughnut', ${taskLabels}, ${taskValues}, 'Tasks');
</script>
<%@ include file="/WEB-INF/views/layout/footer.jspf" %>
