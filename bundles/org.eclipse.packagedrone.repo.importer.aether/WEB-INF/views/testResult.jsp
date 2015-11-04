<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
    
<%@ taglib tagdir="/WEB-INF/tags/main" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://dentrassi.de/osgi/web/form" prefix="form"%>
<%@ taglib uri="http://dentrassi.de/osgi/job" prefix="job"%>
<%@ taglib tagdir="/WEB-INF/tags/main" prefix="h" %>

<h:main title="Import Test" subtitle="Result">

<script type="text/javascript">
function doAction(action) {
    var form = $('#command');
    form.attr("action", action);
    form.submit();
    return false;
} 
</script>

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
            
            <dt>Coordinates</dt>
            <dd>${fn:escapeXml(configuration.coordinates) }</dd>
        </dl>
    </div>
    <div class="col-md-6">
        <h3 class="details-heading">Response</h3>
        <dl class="dl-horizontal details">
            <dt>Resolved?</dt>
            <dd>${result.resolved ? "yes" : "no" }</dd>
            
            <dt>Group ID</dt>
            <dd>${result.coordinates.groupId }</dd>
            
            <dt>Artifact ID</dt>
            <dd>${result.coordinates.artifactId }</dd>
            
            <dt>Version</dt>
            <dd>${result.coordinates.version }</dd>
            
            <dt>Classifier</dt>
            <dd>${result.coordinates.classifier }</dd>
            
            <dt>Extension</dt>
            <dd>${result.coordinates.extension }</dd>
            
            <dt>Repository</dt>
            <dd>${fn:escapeXml(result.url) }</dd>
        </dl>
    </div>
</div>

<div class="row">
    <div class="col-md-11 col-md-offset-1">
        <form class="form-inline" method="GET" action="" id="command">
            <input type="hidden" name=configuration value="${fn:escapeXml(cfgJson) }"/>
            <input type="hidden" name=request value="${fn:escapeXml(request) }"/>
            <input type="hidden" name="token" value="${fn:escapeXml(token) }"/>
            <button class="btn btn-primary" type="button" onclick="doAction('/import/perform');">Import</button>
            <button class="btn btn-default" type="button" onclick="doAction('start');">Edit</button>
        </form>
    </div>
</div>

</div>

</h:main>