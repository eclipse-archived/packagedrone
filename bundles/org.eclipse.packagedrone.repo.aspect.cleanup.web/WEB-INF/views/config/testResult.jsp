<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
    
<%@ taglib tagdir="/WEB-INF/tags/main" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://eclipse.org/packagedrone/repo/channel" prefix="storage" %>
<%@ taglib uri="http://dentrassi.de/osgi/web/form" prefix="form" %>
<%@ taglib uri="http://eclipse.org/package-drone/web" prefix="web" %>


<%
pageContext.setAttribute ( "manager", request.isUserInRole ( "MANAGER" ) );
%>

<h:main title="Test Cleanup" subtitle="${storage:channel(channel) }">

<h:buttonbar menu="${menuManager.getActions(channel) }" />

<h:nav menu="${menuManager.getViews(channel) }"/>

<div class="table-responsive">
<table class="table table-bordered table-condensed table-hover">

    <thead>
        <tr>
            <c:forEach var="field" items="${command.aggregator.fields }">
                <th>
                    ${fn:escapeXml(field) }
                </th>
            </c:forEach>
            <c:forEach var="field" items="${command.sorter.fields }">
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
    
        <c:forEach var="entry" items="${result }">
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
	                <c:forEach var="field" items="${command.sorter.fields }">
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

<div class="container-fluid">

<div class="row">
<div class="col-md-12">

<form action="edit" method="get">
    <input type="hidden" name="configuration" value="${fn:escapeXml(web:json(command)) }"/>
    <button class="btn btn-primary" type="submit">Edit</button>
</form>

</div></div>
</div>
</h:main>