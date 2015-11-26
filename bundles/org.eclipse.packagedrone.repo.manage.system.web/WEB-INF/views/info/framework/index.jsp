<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8" trimDirectiveWhitespaces="true"%>
    
<%@ taglib tagdir="/WEB-INF/tags/main" prefix="h" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<%@ taglib uri="http://eclipse.org/packagedrone/web" prefix="web" %>

<h:main title="System" subtitle="OSGi Framework">

<div class="container-fluid">

    <div class="row">
    
        <div class="col-sm-6">
            <h3 class="details-heading">Framework</h3>
            <dl class="dl-horizontal details">
                <dt>Vendor</dt>
                <dd>${fn:escapeXml(vendor) }</dd>
                <dt>Version</dt>
                <dd>${fn:escapeXml(version) }</dd>
                <dt>Name</dt>
                <dd>${fn:escapeXml(sysName) }</dd>
                <dt>Symbolic Name</dt>
                <dd>${fn:escapeXml(sysSymbolicName) }</dd>
            </dl>
        </div>
        
        <div class="col-sm-6">
            <h3 class="details-heading">Details</h3>
            <ul>
                <li><a href="<c:url value="/system/info/framework/bundles"/>">List of bundles</a></li>
            </ul>
        </div>

    </div>
    
</div>

</h:main>