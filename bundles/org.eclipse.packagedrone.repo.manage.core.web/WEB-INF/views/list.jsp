<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" trimDirectiveWhitespaces="true"%>
    
<%@ taglib tagdir="/WEB-INF/tags/main" prefix="h" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://dentrassi.de/pm" prefix="pm" %>

<h:main title="Global Properties">

<table class="table table-striped table-condensed table-hover">

<thead>
    <tr>
        <th>Namespace</th>
        <th>Name</th>
        <th>Value</th>
    <tr>
</thead>

<tbody>
    <c:forEach var="entry" items="${properties}">
    <tr>
        <td>${fn:escapeXml(entry.key.namespace) }</td>
        <td>${fn:escapeXml(entry.key.key) }</td>
        <td><code>${fn:escapeXml(entry.value) }</code></td>
    </c:forEach>
</tbody>

</table>

</h:main>