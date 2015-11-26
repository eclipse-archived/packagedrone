<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8" trimDirectiveWhitespaces="true"%>
    
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://eclipse.org/packagedrone/web/form" prefix="form"%>

<%@ taglib tagdir="/WEB-INF/tags/main" prefix="h" %>

<h:main title="Import Test" subtitle="Result">

<style>
tr.optional {
    font-style: italic;
}
tr.requested {
    font-weight: bold;
}
</style>

<script type="text/javascript">
function doAction(action) {
    var form = $('#command');
    form.attr("action", action);
    form.submit();
    return false;
} 
</script>

<form class="form-inline" method="POST" action="" id="command">

<div class="container-fluid">
	<div class="row">
	    <div class="col-xs-12">
	        <dl class="dl-horizontal details">
	            <dt>Repository</dt>
	            <dd>
	                <c:choose>
	                    <c:when test="${empty configuration.repositoryUrl }"><em>Maven Central</em></c:when>
	                    <c:otherwise>${fn:escapeXml(configuration.repositoryUrl) }</c:otherwise>
	                </c:choose>
	            </dd>
	            
	            <dt>Coordinates</dt>
	            <dd>${fn:escapeXml(configuration.coordinates) }</dd>
	        </dl>
	    </div>
	</div>
</div>

<div class="table-responsive">
<table class="table table-condensed">
    <thead>
        <tr>
            <th></th>
            <th>Group ID</th>
            <th>Artifact ID</th>
            <th>Version</th>
            <th>Classifier</th>
            <th>Extension</th>
            <th></th>
        </tr>
    </thead>
    <tbody>
	    <c:forEach var="entry" items="${result.artifacts }">
	    
	        <c:set var="rowClass">
	            <c:choose>
	                <c:when test="${not entry.resolved }">danger</c:when>
	                <c:when test="${not empty entry.existingVersions }">success</c:when>
	                <c:when test="${entry.requested }">active requested</c:when>
	                <c:otherwise></c:otherwise>
	            </c:choose>
	            <c:if test="${entry.requested }">${' ' }active requested</c:if>
	            <c:if test="${entry.optional }">${' ' }optional</c:if>
	        </c:set>
            
	        <tr class="${rowClass }">
                <td>
                    <c:choose>
                        <c:when test="${ not entry.resolved }">
                            <input type="checkbox" name="${fn:escapeXml(entry.coordinates) }" readonly="readonly" disabled="disabled"/>
                        </c:when>
                        <c:when test="${ entry.optional or (not empty entry.existingVersions) }">
                            <input type="checkbox" name="${fn:escapeXml(entry.coordinates) }"/>
                        </c:when>
                        <c:otherwise>
                            <input type="checkbox" name="${fn:escapeXml(entry.coordinates) }" checked="checked" />
                        </c:otherwise>
                    </c:choose>
                </td>
	            <td>${fn:escapeXml(entry.coordinates.groupId) }</td>
	            <td>${fn:escapeXml(entry.coordinates.artifactId) }</td>
	            <td>${fn:escapeXml(entry.coordinates.version) }</td>
	            <td>${fn:escapeXml(entry.coordinates.classifier) }</td>
	            <td>${fn:escapeXml(entry.coordinates.extension) }</td>
	            <td>
	               <c:if test="${not empty entry.error }">
	                   <span data-toggle="tooltip" data-placement="left" title="${fn:escapeXml(entry.error) }" class="glyphicon glyphicon-alert"></span>
	               </c:if>
	               <c:if test="${not empty entry.existingVersions }">
	                   <span data-toggle="tooltip" data-placement="left" title="Already imported" class="glyphicon glyphicon-ok-sign"></span>
	               </c:if>
	            </td>
	        </tr>
	    </c:forEach>
    </tbody>
</table>
</div>

<%-- initialize tooltips --%>
<script type="text/javascript">
$(function () {
	  $('[data-toggle="tooltip"]').tooltip()
})
</script>

<div class="container-fluid">

	<div class="row">
	    <div class="col-md-11 col-md-offset-1">
            <input type="hidden" name=importConfig value="${fn:escapeXml(importConfig) }"/>
            <button class="btn btn-primary" type="button" onclick="doAction('perform');">Import</button>
            
            <c:if test="${not empty cfgJson }">
                <input type="hidden" name=configuration value="${fn:escapeXml(cfgJson) }"/>
                <button class="btn btn-default" type="button" onclick="doAction('edit');">Edit</button>
            </c:if>
	    </div>
	</div>

</div>

</form>

</h:main>