
<%@ page
  language="java"
  contentType="text/html; charset=UTF-8"
  pageEncoding="UTF-8"
  trimDirectiveWhitespaces="true"
  %>
  
<%! @SuppressWarnings("unchecked") %>

<%@ page import="org.eclipse.packagedrone.utils.io.IOConsumer"%>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<%@ taglib prefix="web" uri="http://eclipse.org/packagedrone/web" %>
<%@ taglib prefix="form" uri="http://eclipse.org/packagedrone/web/form" %>

<%@ taglib prefix="storage" uri="http://eclipse.org/packagedrone/repo/channel" %>

<%@ taglib tagdir="/WEB-INF/tags/main" prefix="h" %>

<%
pageContext.setAttribute ( "manager", request.isUserInRole ( "MANAGER" ) );
%>

<h:main title="Test Cleanup" subtitle="${storage:channel(channel) }">

<div class="container-fluid">
<div class="table-responsive">
<table class="table table-bordered table-condensed table-hover">

    <thead>
        <tr>
            <c:forEach var="field" items="${cleaner.aggregator.fields }">
                <th>
                    ${fn:escapeXml(field) }
                </th>
            </c:forEach>
            <c:forEach var="field" items="${cleaner.sorter.fields }">
                <th>
                    <c:choose>
                        <c:when test="${field.order == 'ASCENDING' }">
                            <span class="glyphicon glyphicon-sort-by-attributes"></span>
                        </c:when>
                        <c:when test="${field.order == 'DESCENDING' }">
                            <span class="glyphicon glyphicon-sort-by-attributes-alt"></span>
                        </c:when>
                    </c:choose>
                    ${fn:escapeXml(field.key) }
                </th>
            </c:forEach>
            <th>ID</th>
            <th>Name</th>
        </tr>
    </thead>
    
    <tbody>
    
        <c:forEach var="entry" items="${result.entries }">
            <c:set var="span" value="${entry.value.size() }"/>
            <tr>
                <c:forEach var="i" items="${entry.key.keys }">
                    <td rowspan="${span +1}">${fn:escapeXml(i) }</td>
                </c:forEach>
            </tr>
            <c:forEach var="art" items="${entry.value }">
                <c:set var="rowClass" value=""/>
                <c:if test="${ art.action == 'DELETE' }"><c:set var="rowClass" value="danger" /></c:if>
	            <tr class="${rowClass }">
	                <c:forEach var="field" items="${cleaner.sorter.fields }">
	                   <td>${fn:escapeXml(art.artifact.metaData[field.key]) }</td>
	                </c:forEach>
	                <td>${fn:escapeXml(art.artifact.id) }</td>
	                <td>${fn:escapeXml(art.artifact.name) }</td>
	            </tr>
            </c:forEach>
        </c:forEach>
    
    </tbody>

</table>
</div>

</div>

<c:if test="${ customizer != null }">${ customizer.accept(pageContext.out) }</c:if>

</h:main>