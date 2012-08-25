<%@ taglib uri='http://java.sun.com/jsp/jstl/core' prefix='c'%>
<%@ page session='false'%>
<c:set var="contextPath" value="${pageContext.request.contextPath}" />
<html>
<head>
<title>Debug</title>
</head>
<body>
	<p>Upload directory has:</p>
	<c:forEach items="${fileList}" var="item">
		<p>Name: ${item.getName()}<br />
		Size: ${item.length()}</p>
	</c:forEach>
</body>
</html>
