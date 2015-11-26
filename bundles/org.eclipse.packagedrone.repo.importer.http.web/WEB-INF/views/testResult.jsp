<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
    
<%@ taglib tagdir="/WEB-INF/tags/main" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://eclipse.org/packagedrone/web/form" prefix="form"%>
<%@ taglib uri="http://eclipse.org/packagedrone/job" prefix="job"%>
<%@ taglib uri="http://eclipse.org/packagedrone/web" prefix="web"%>
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
            <dt>URL</dt>
            <dd>${fn:escapeXml(configuration.url) }</dd>
        </dl>
    </div>
    <div class="col-md-6">
        <h3 class="details-heading">Response</h3>
        <dl class="dl-horizontal details">
            <dt>Return Code</dt>
            <dd>${result.returnCode }</dd>
            
            <dt>Name</dt>
            <dd>${result.name }</dd>
            
            <dt>Content Length</dt>
            <dd><web:bytes amount="${result.contentLength }"/></dd>
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