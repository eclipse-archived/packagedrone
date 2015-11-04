<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
    
<%@ taglib tagdir="/WEB-INF/tags/main" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://dentrassi.de/osgi/web/form" prefix="form"%>
<%@ taglib uri="http://dentrassi.de/osgi/job" prefix="job"%>
<%@ taglib tagdir="/WEB-INF/tags/main" prefix="h" %>

<h:main title="Import Test" subtitle="Failed">

<div class="container-fluid form-padding">

<div class="row">
    <div class="col-md-6">
        <h3 class="details-heading">Request</h3>
        <dl class="dl-horizontal details">
            <dt>Repository</dt>
            <dd>
                <c:choose>
                    <c:when test="${empty configuration.url }"><em>Maven Central</em></c:when>
                    <c:otherwise>${fn:escapeXml(configuration.url) }</c:otherwise>
                </c:choose>
            </dd>
            
            <dt>Artifact ID</dt>
            <dd>${fn:escapeXml(configuration.coordinates) }</dd>
        </dl>
    </div>
    <div class="col-md-6">
        <h3 class="details-heading">Information</h3>
        <div class="alert alert-danger">
        <strong>Failed! </strong>${fn:escapeXml(error.message) }
        </div>
    </div>
</div>

<div class="row">
    <div class="col-md-11 col-md-offset-1">
        <form class="form-inline" method="GET" action="start" id="command">
            <input type="hidden" name=configuration value="${fn:escapeXml(cfgJson) }"/>
            <input type="hidden" name=request value="${fn:escapeXml(request) }"/>
            <input type="hidden" name="token" value="${fn:escapeXml(token) }"/>
            <button class="btn btn-primary" type="submit">Edit</button>
        </form>
    </div>
</div>

</div>

</h:main>