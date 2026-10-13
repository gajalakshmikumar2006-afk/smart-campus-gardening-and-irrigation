<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" isErrorPage="true" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<!DOCTYPE html>
<html lang="en"><head><meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1">
<title>Something went wrong | Smart Campus Gardening</title>
<link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
<link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css" rel="stylesheet">
<link href="${ctx}/assets/css/style.css" rel="stylesheet"></head>
<body class="login-page">
  <div class="login-card text-center">
    <div class="logo"><i class="bi bi-exclamation-triangle"></i></div>
    <h4>Oops - something went wrong</h4>
    <p class="text-muted">
      <c:choose>
        <c:when test="${not empty errorMessage}"><c:out value="${errorMessage}"/></c:when>
        <c:when test="${pageContext.response.status == 403}">You do not have permission to open that page.</c:when>
        <c:when test="${pageContext.response.status == 404}">That page could not be found.</c:when>
        <c:otherwise>An unexpected error occurred. Please try again. If it keeps happening, contact the administrator.</c:otherwise>
      </c:choose>
    </p>
    <a class="btn btn-green" href="${ctx}/dashboard"><i class="bi bi-house me-1"></i>Back to dashboard</a>
  </div>
</body></html>
