<%@ page language="java" pageEncoding="UTF-8" contentType="text/html;charset=utf-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<html>
<head>
    <title>Hello, World!</title>
</head>
<body>
<h3>local에서 실행중인 job</h3>
<table border="1">
    <tr>
        <th>job name</th>
        <th>job ID</th>
        <th>parameter</th>
    </tr>
    <c:forEach var="runningJob" items="${runningJobNames}">
        <tr>
            <c:set var="jobToken" value="${fn:split(runningJob.key,'::')}" />
            <td>${jobToken[0]}</td>
            <td>${runningJob.value}</td>
            <td>${jobToken[1]}&nbsp;</td>
        </tr>
    </c:forEach>
</table>

<h3>등록된 job</h3>
<table border="1">
    <tr>
        <th>job name</th>
    </tr>
    <c:forEach var="job" items="${jobNames}">
        <tr>
            <td>${job}</td>
        </tr>
    </c:forEach>
</table>
</body>
</html>
