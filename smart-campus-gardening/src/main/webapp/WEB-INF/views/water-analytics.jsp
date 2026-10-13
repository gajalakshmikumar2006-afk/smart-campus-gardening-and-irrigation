<%@ include file="/WEB-INF/views/layout/header.jspf" %>

<div class="page-title">
  <div><h1><i class="bi bi-bar-chart-line text-success me-2"></i>Water Usage &amp; Analytics</h1>
    <p>Estimated water consumption from recorded irrigation activities.</p></div>
</div>
<div class="notice-estimate mb-3"><i class="bi bi-info-circle me-1"></i><strong>All water figures are estimates.</strong>
  They come from scheduled/recorded quantities - this project has no physical water-flow sensors.</div>

<form method="get" action="${ctx}/water-analytics" class="card-soft p-3 mb-4">
  <div class="row g-2 align-items-end">
    <div class="col-6 col-md-3"><label class="form-label small mb-1">From</label><input type="date" class="form-control" name="from" value="${from}"></div>
    <div class="col-6 col-md-3"><label class="form-label small mb-1">To</label><input type="date" class="form-control" name="to" value="${to}"></div>
    <div class="col-md-3"><label class="form-label small mb-1">Garden</label>
      <select class="form-select" name="gardenId"><option value="0">All gardens</option>
        <c:forEach var="g" items="${gardens}"><option value="${g.gardenId}" ${filterGardenId == g.gardenId ? 'selected' : ''}><c:out value="${g.gardenName}"/></option></c:forEach></select></div>
    <div class="col-md-3 d-flex gap-2"><button class="btn btn-green flex-fill"><i class="bi bi-funnel me-1"></i>Apply</button><a class="btn btn-light" href="${ctx}/water-analytics">Reset</a></div>
  </div>
</form>

<div class="row g-3 mb-4">
  <div class="col-6 col-xl-3"><div class="card-soft stat-card"><div class="stat-icon dark"><i class="bi bi-droplet-fill"></i></div>
    <div><div class="stat-value"><fmt:formatNumber value="${totalLiters}" maxFractionDigits="1"/> L</div><div class="stat-label">Total estimated usage</div></div></div></div>
  <div class="col-6 col-xl-3"><div class="card-soft stat-card"><div class="stat-icon green"><i class="bi bi-check2-circle"></i></div>
    <div><div class="stat-value">${activityCount}</div><div class="stat-label">Irrigation activities</div></div></div></div>
  <div class="col-6 col-xl-3"><div class="card-soft stat-card"><div class="stat-icon blue"><i class="bi bi-calculator"></i></div>
    <div><div class="stat-value"><fmt:formatNumber value="${avgPerActivity}" maxFractionDigits="1"/> L</div><div class="stat-label">Average per activity</div></div></div></div>
  <div class="col-6 col-xl-3"><div class="card-soft stat-card"><div class="stat-icon beige"><i class="bi bi-calendar3"></i></div>
    <div><div class="stat-value"><fmt:formatNumber value="${avgPerDay}" maxFractionDigits="1"/> L</div><div class="stat-label">Average per day</div></div></div></div>
</div>

<div class="row g-3 mb-4">
  <div class="col-lg-8"><div class="card-soft h-100"><div class="card-header">Daily estimated usage (litres)</div><div class="card-body"><div class="chart-box"><canvas id="dailyChart"></canvas></div></div></div></div>
  <div class="col-lg-4"><div class="card-soft h-100"><div class="card-header">Share by garden</div><div class="card-body"><div class="chart-box"><canvas id="shareChart"></canvas></div></div></div></div>
  <div class="col-lg-6"><div class="card-soft h-100"><div class="card-header">Weekly (week starting Monday)</div><div class="card-body"><div class="chart-box"><canvas id="weeklyChart"></canvas></div></div></div></div>
  <div class="col-lg-6"><div class="card-soft h-100"><div class="card-header">Monthly</div><div class="card-body"><div class="chart-box"><canvas id="monthlyChart"></canvas></div></div></div></div>
</div>

<div class="card-soft"><div class="card-header">Garden-wise comparison</div>
  <div class="table-responsive"><table class="table align-middle mb-0">
    <thead><tr><th>#</th><th>Garden</th><th>Estimated usage (L)</th><th style="width:40%">Share of total</th></tr></thead>
    <tbody>
      <c:forEach var="g" items="${byGarden}" varStatus="st">
        <c:set var="pct" value="${totalLiters > 0 ? g.value * 100 / totalLiters : 0}"/>
        <tr><td>${st.index + 1}</td><td class="fw-semibold"><c:out value="${g.label}"/></td>
          <td><fmt:formatNumber value="${g.value}" maxFractionDigits="1"/></td>
          <td><div class="progress" style="height:14px"><div class="progress-bar" style="width:${pct}%;background:#52b788"></div></div>
            <small class="text-muted"><fmt:formatNumber value="${pct}" maxFractionDigits="1"/>%</small></td></tr>
      </c:forEach>
      <c:if test="${empty byGarden}"><tr><td colspan="4"><div class="empty-state"><i class="bi bi-droplet"></i>No irrigation records in this period.</div></td></tr></c:if>
    </tbody></table></div></div>

<script src="https://cdn.jsdelivr.net/npm/chart.js@4.4.3/dist/chart.umd.min.js"></script>
<script>
  makeChart('dailyChart', 'line', ${dailyLabels}, ${dailyValues}, 'Estimated litres');
  makeChart('shareChart', 'doughnut', ${gardenLabels}, ${gardenValues}, 'Estimated litres');
  makeChart('weeklyChart', 'bar', ${weeklyLabels}, ${weeklyValues}, 'Estimated litres');
  makeChart('monthlyChart', 'bar', ${monthlyLabels}, ${monthlyValues}, 'Estimated litres');
</script>
<%@ include file="/WEB-INF/views/layout/footer.jspf" %>
