<%@ include file="/WEB-INF/views/layout/header.jspf" %>

<div class="page-title">
  <div><h1><i class="bi bi-cloud-sun text-warning me-2"></i>Weather</h1>
    <p>Campus location: <strong><c:out value="${campusLocation}"/></strong></p></div>
  <form method="post" action="${ctx}/weather"><input type="hidden" name="_csrf" value="${sessionScope.csrfToken}"><input type="hidden" name="action" value="refresh">
    <button class="btn btn-outline-green"><i class="bi bi-arrow-repeat me-1"></i>Refresh now</button></form>
</div>

<c:if test="${not empty weatherNotice}">
  <div class="alert alert-warning"><i class="bi bi-wifi-off me-1"></i><strong>Weather service unavailable.</strong> <c:out value="${weatherNotice}"/></div>
</c:if>
<c:if test="${weather.demo}">
  <div class="alert alert-warning"><span class="badge badge-demo me-2">DEMO MODE</span>
    No OpenWeather API key is configured, so the figures below are <strong>sample data - not live weather</strong>.
    See the README ("Weather API key") to switch on live data.</div>
</c:if>

<c:choose>
<c:when test="${not empty weather}">
  <div class="row g-3 mb-4">
    <div class="col-lg-5"><div class="card-soft h-100"><div class="card-body">
      <div class="d-flex align-items-center gap-4">
        <div class="weather-temp"><fmt:formatNumber value="${weather.temperatureC}" maxFractionDigits="1"/>&deg;C</div>
        <div><h5 class="mb-1"><c:out value="${weather.conditions}"/></h5>
          <div class="text-muted small">Humidity: ${weather.humidity}%</div>
          <div class="text-muted small">Rain now: <fmt:formatNumber value="${weather.rainMmCurrent}" maxFractionDigits="1"/> mm</div>
          <div class="text-muted small">Rain next 24h: <fmt:formatNumber value="${weather.rainMmForecast24h}" maxFractionDigits="1"/> mm</div></div>
      </div>
      <hr>
      <div class="small text-muted">Source: <c:out value="${weather.demo ? 'Demo sample data' : 'OpenWeather API (live)'}"/> &middot;
        retrieved <fmt:formatDate value="${weather.retrievedAt}" pattern="dd MMM yyyy, hh:mm a"/></div>
    </div></div></div>

    <div class="col-lg-7"><div class="card-soft h-100"><div class="card-header">Irrigation advice</div><div class="card-body">
      <c:choose>
        <c:when test="${postponeAdvice}">
          <div class="alert alert-warning mb-0"><i class="bi bi-cloud-rain-heavy me-1"></i><strong>Postpone irrigation.</strong>
            About <fmt:formatNumber value="${rainTotal24h}" maxFractionDigits="1"/> mm of rain is forecast for the next 24 hours, which should be enough for most plants.
            <c:if test="${weather.demo}"><br><small>(Based on demo data.)</small></c:if></div>
        </c:when>
        <c:otherwise>
          <div class="alert alert-success mb-0"><i class="bi bi-droplet me-1"></i><strong>No rain-based postponement needed.</strong>
            Only <fmt:formatNumber value="${rainTotal24h}" maxFractionDigits="1"/> mm of rain is expected in the next 24 hours.
            <c:if test="${weather.demo}"><br><small>(Based on demo data.)</small></c:if></div>
        </c:otherwise>
      </c:choose>
      <p class="small text-muted mt-3 mb-0">Postponement is suggested when rain (now + next 24h) reaches 5 mm. Garden-by-garden advice is on the
        <a href="${ctx}/irrigation">Irrigation</a> page.</p>
    </div></div></div>
  </div>

  <div class="card-soft mb-4"><div class="card-header">Forecast (next hours)</div>
    <div class="table-responsive"><table class="table align-middle mb-0">
      <thead><tr><th>Time</th><th>Conditions</th><th>Temp</th><th>Chance of rain</th><th>Rain (mm)</th></tr></thead>
      <tbody>
        <c:forEach var="f" items="${forecast}">
          <tr><td><c:out value="${f.time}"/></td><td><c:out value="${f.conditions}"/></td>
            <td><fmt:formatNumber value="${f.temp}" maxFractionDigits="1"/>&deg;C</td>
            <td><fmt:formatNumber value="${f.pop}" maxFractionDigits="0"/>%</td>
            <td><fmt:formatNumber value="${f.rainMm}" maxFractionDigits="1"/></td></tr>
        </c:forEach>
        <c:if test="${empty forecast}"><tr><td colspan="5" class="text-center text-muted">No forecast data.</td></tr></c:if>
      </tbody></table></div></div>
</c:when>
<c:otherwise>
  <div class="card-soft"><div class="empty-state"><i class="bi bi-cloud-slash"></i>No weather data is available yet. Use "Refresh now" to try again.</div></div>
</c:otherwise>
</c:choose>

<c:if test="${isAdmin}">
  <div class="card-soft" style="max-width:640px"><div class="card-header">Campus location (admin)</div><div class="card-body">
    <form method="post" action="${ctx}/weather" class="row g-2 align-items-end">
      <input type="hidden" name="_csrf" value="${sessionScope.csrfToken}"><input type="hidden" name="action" value="location">
      <div class="col-sm-8"><label class="form-label">City, country code</label>
        <input class="form-control" name="location" value="<c:out value='${campusLocation}'/>" required minlength="2" maxlength="100" placeholder="e.g. Chennai,IN"></div>
      <div class="col-sm-4"><button class="btn btn-green w-100">Save &amp; refresh</button></div>
    </form>
    <div class="form-text mt-2">API key status: <c:choose>
      <c:when test="${apiKeyConfigured}"><span class="text-success fw-semibold">configured (live mode)</span></c:when>
      <c:otherwise><span class="text-warning fw-semibold">not configured (demo mode)</span></c:otherwise></c:choose>. The key is never stored in the project files or database.</div>
  </div></div>
</c:if>
<%@ include file="/WEB-INF/views/layout/footer.jspf" %>
