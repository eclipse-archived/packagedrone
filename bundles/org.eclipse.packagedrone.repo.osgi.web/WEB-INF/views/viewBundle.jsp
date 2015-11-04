<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://eclipse.org/packagedrone/repo/channel" prefix="pm" %>

<%@ taglib tagdir="/WEB-INF/tags/main" prefix="h" %>
<%@ taglib tagdir="/WEB-INF/tags/org.eclipse.packagedrone.repo.osgi.web" prefix="osgi" %>

<h:main title="${fn:escapeXml(bundle.id)}" subtitle="${fn:escapeXml(bundle.version) }">

<h:breadcrumbs/>

<ul class="nav nav-tabs" role="tablist">
    <li role="presentation" class="active"><a href="#home" aria-controls="home" role="tab" data-toggle="tab">Core information</a></li>
    <li role="presentation"><a href="#packageImports" aria-controls="packageImports" role="tab" data-toggle="tab">Package Imports</a></li>
    <li role="presentation"><a href="#packageExports" aria-controls="packageExports" role="tab" data-toggle="tab">Package Exports</a></li>
    <li role="presentation"><a href="#bundleReq" aria-controls="bundleReq" role="tab" data-toggle="tab">Bundle Requirements</a></li>
    <li role="presentation"><a href="#manifest" aria-controls="manifest" role="tab" data-toggle="tab">Manifest</a></li>
</ul>

<div class="tab-content form-padding">

<%-- CORE --%>

<div role="tabpanel" class="tab-pane active" id="home">
    
    <div class="container-fluid">
	    <div class="row">
	    
		    <div class="col-md-6">
		    
			    <h3 class="details-heading">Core Attributes</h3>
			    
			    <dl class="dl-horizontal details">
			        <dt>Symbolic Name</dt>
			        <dd>
			        ${fn:escapeXml(bundle.id) }
			        <c:if test="${bundle.singleton }"> <span class="label label-default">singleton</span></c:if>
			        </dd>
			        
			        <dt>Version</dt>
			        <dd>${fn:escapeXml(bundle.version) }</dd>
			        
			        <dt>Name</dt>
			        <dd><osgi:translated data="${bundle }" property="name" /></dd>
			        
			        <dt>
			          <span data-toggle="tooltip" title="Execution Environments">EE</span>
			        </dt>
			        <dd>
				        <ul>
	                        <c:forEach var="ee" items="${bundle.requiredExecutionEnvironments }">
	                            <li>${fn:escapeXml(ee) }</li>
	                        </c:forEach>
				        </ul>
			        </dd>
			         
			        <dt>Description</dt>
	                <dd><osgi:translated data="${bundle }" property="description" /></dd>
	                
	                <dt>Documentation</dt>
	                <dd>
		                <c:if test="${not empty bundle.docUrl }">
		                    <a href="${bundle.docUrl }" target="_blank">${fn:escapeXml(bundle.docUrl) }</a>
		                </c:if>
	                </dd>
			    </dl>
		    
		    </div>
	    
	    
		    <div class="col-md-6">
	        
	            <h3 class="details-heading">Legal</h3>
	            
	            <dl class="dl-horizontal details">
	            
	                <dt>Vendor</dt>
	                <dd><osgi:translated data="${bundle }" property="vendor" /></dd>
	                
	                <dt>License</dt>
	                <dd>
		                <c:choose>
		                    <c:when test="${not empty bundle.license and ( fn:startsWith(bundle.license, 'http://') or fn:startsWith(bundle.license, 'https://') )  }">
		                        <a href="${bundle.license}" target="_blank">${fn:escapeXml(bundle.license) }</a>
		                    </c:when>
		                    <c:otherwise>
		                        <osgi:translated data="${bundle }" property="license" />
		                    </c:otherwise>
		                </c:choose>
	                </dd>
	            
	            </dl>
	            
	        </div>
        </div>
        
    </div>
    
</div>

<%-- Package Imports --%>

<div role="tabpanel" class="tab-pane" id="packageImports">

	<table class="table table-condensed table-striped table-hover">
	
	    <thead>
	        <tr>
	            <th>Name</th>
	            <th>Version</th>
	            <th>Optional</th>
	        </tr>
	    </thead>
	    
	    <tbody>
	        <c:forEach var="pi" items="${bundle.packageImports }">
	            <tr>
	                <td>${fn:escapeXml(pi.name) }</td>
	                <td>${fn:escapeXml(pi.versionRange) }</td>
	                <td>
	                    <c:if test="${pi.optional }">optional</c:if>
	                </td>
	            </tr>
	        </c:forEach>
	    </tbody>
	
	</table>

</div>

<%-- Package Exports --%>

<div role="tabpanel" class="tab-pane" id="packageExports">

    <table class="table table-condensed table-striped table-hover">
    
        <thead>
            <tr>
                <th>Name</th>
                <th>Version</th>
            </tr>
        </thead>
        
        <tbody>
            <c:forEach var="pe" items="${bundle.packageExports }">
                <tr>
                    <td>${fn:escapeXml(pe.name) }</td>
                    <td>${fn:escapeXml(pe.version) }</td>
                </tr>
            </c:forEach>
        </tbody>
    
    </table>
</div>

<%-- Bundle Requirements --%>

<div role="tabpanel" class="tab-pane" id="bundleReq">

    <table class="table table-condensed table-striped table-hover">
    
        <thead>
            <tr>
                <th>Name</th>
                <th>Version</th>
                <th>Optional</th>
                <th>Re-Export</th>
            </tr>
        </thead>
        
        <tbody>
            <c:forEach var="br" items="${bundle.bundleRequirements }">
                <tr>
                    <td>${fn:escapeXml(br.id) }</td>
                    <td>${fn:escapeXml(br.versionRange) }</td>
                    <td><c:if test="${br.optional }">optional</c:if></td>
                    <td><c:if test="${br.reexport }">re-export</c:if></td>
                </tr>
            </c:forEach>
        </tbody>
    
    </table>
</div>

<%-- Manifest --%>

<div role="tabpanel" class="tab-pane" id="manifest">

    <div class="container-fluid"><div class="row"><div class="col-md-12">
        <pre>${fn:escapeXml(fullManifest) }</pre>
    </div></div></div>

    
</div>

<%-- end of tab --%>
</div><%-- tabpanel --%>



<script type="text/javascript">
$(function () {
	  $('[data-toggle="tooltip"]').tooltip()
	})
</script>

</h:main>