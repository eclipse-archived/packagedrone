<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8" trimDirectiveWhitespaces="true"%>
    
<%@ taglib tagdir="/WEB-INF/tags/main" prefix="h" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<%@ taglib uri="http://eclipse.org/packagedrone/web" prefix="web" %>

<h:main title="System" subtitle="OSGi Bundles">

<web:define name="state">
<c:choose>
    <c:when test="${bundle.state == 1 }">UNINSTALLED</c:when>
    <c:when test="${bundle.state == 2 }">INSTALLED</c:when>
    <c:when test="${bundle.state == 4 }">RESOLVED</c:when>
    <c:when test="${bundle.state == 8 }">STARTING</c:when>
    <c:when test="${bundle.state == 16 }">STOPPING</c:when>
    <c:when test="${bundle.state == 32 }">ACTIVE</c:when>
    <c:otherwise>unkown</c:otherwise>
</c:choose>
</web:define>

<div class="table-responsive">

<table class="table table-condensed table-hover">
    <thead>
        <tr>
            <th>Symbolic Name</th>
            <th>Name</th>
            <th>Version</th>
            <th>State</th>
            <th>ID</th>
            <th></th>
        </tr>
    </thead>
    
    <tbody>
        <c:forEach var="bundle" items="${bundles }">
            <tr class="${bundle.state == 32 ? 'success' : '' } ">
                <td>
                    ${fn:escapeXml(bundle.symbolicName) }
                    <c:if test="${bundle.fragment }">
                        <span class="label label-default">Fragment</span>
                    </c:if>
                </td>
                <td>${fn:escapeXml(bundle.name) }</td>
                <td>${fn:escapeXml(bundle.version) }</td>
                <td><web:call name="state" bundle="${bundle }"/></td>
                <td>${fn:escapeXml(bundle.bundleId) }</td>
                <td>
                    <c:if test="${!bundle.fragment }">
	                    <c:if test="${bundle.state == 32 }">
	                    <form action="<c:url value="/system/info/framework/bundles/${bundle.bundleId }/stop"/>" method="post" id="stop_${bundle.bundleId }"></form><a href="#" onclick="$('#stop_${bundle.bundleId}').submit(); return false;">Stop</a>
	                    </c:if>
	                    <c:if test="${bundle.state < 32 }">
	                    <form action="<c:url value="/system/info/framework/bundles/${bundle.bundleId }/start"/>" method="post" id="start_${bundle.bundleId }"></form><a href="#" onclick="$('#start_${bundle.bundleId}').submit(); return false;">Start</a>
	                    </c:if>
                    </c:if>
                </td>
            </tr>
        </c:forEach>
    </tbody>
    
</table>
    
</div>

</h:main>