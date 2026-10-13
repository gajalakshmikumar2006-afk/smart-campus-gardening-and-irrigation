<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1">
  <title>Sign in | Smart Campus Gardening</title>
  <link href="https://fonts.googleapis.com/css2?family=Poppins:wght@400;500;600;700&display=swap" rel="stylesheet">
  <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
  <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css" rel="stylesheet">
  <link href="${ctx}/assets/css/style.css" rel="stylesheet">
</head>
<body class="login-page">
  <div class="login-card">
    <div class="logo"><i class="bi bi-flower2"></i></div>
    <h3 class="text-center mb-1">Smart Campus Gardening</h3>
    <p class="text-center text-muted mb-4">Gardening &amp; Irrigation Management System</p>

    <c:if test="${not empty error}"><div class="alert alert-danger py-2"><i class="bi bi-exclamation-circle me-1"></i><c:out value="${error}"/></div></c:if>
    <c:if test="${not empty info}"><div class="alert alert-info py-2"><c:out value="${info}"/></div></c:if>

    <form method="post" action="${ctx}/login" autocomplete="on">
      <div class="mb-3">
        <label class="form-label" for="email">Email</label>
        <div class="input-group">
          <span class="input-group-text bg-white"><i class="bi bi-envelope"></i></span>
          <input type="email" class="form-control" id="email" name="email" value="<c:out value='${email}'/>" required autofocus maxlength="120" placeholder="you@campus.edu">
        </div>
      </div>
      <div class="mb-3">
        <label class="form-label" for="password">Password</label>
        <div class="input-group">
          <span class="input-group-text bg-white"><i class="bi bi-lock"></i></span>
          <input type="password" class="form-control" id="password" name="password" required maxlength="64" placeholder="Your password">
          <button class="btn btn-outline-secondary" type="button" id="togglePw" aria-label="Show password"><i class="bi bi-eye"></i></button>
        </div>
      </div>
      <button type="submit" class="btn btn-green w-100 py-2"><i class="bi bi-box-arrow-in-right me-1"></i>Sign in</button>
    </form>

    <div class="demo-box mt-4">
      <strong><i class="bi bi-info-circle me-1"></i>Demo accounts</strong><br>
      Admin: <code>admin@campus.edu</code> / <code>Admin@123</code><br>
      Staff: <code>ravi@campus.edu</code> / <code>Staff@123</code>
    </div>
  </div>
  <script>
    document.getElementById('togglePw').addEventListener('click', function () {
      var p = document.getElementById('password');
      p.type = p.type === 'password' ? 'text' : 'password';
    });
  </script>
</body>
</html>
