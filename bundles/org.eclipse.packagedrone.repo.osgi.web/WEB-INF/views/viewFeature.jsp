<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%@ page import="org.eclipse.packagedrone.repo.utils.osgi.feature.FeatureInformation"%>
<%@ page import="org.eclipse.packagedrone.repo.utils.osgi.feature.FeatureInformation.Requirement"%>
<%@ page import="org.eclipse.packagedrone.repo.utils.osgi.feature.FeatureInformation.FeatureInclude"%>
<%@ page import="org.eclipse.packagedrone.repo.utils.osgi.feature.FeatureInformation.PluginInclude"%>
<%@ page import="java.util.Collections"%>
<%@ page import="java.util.ArrayList"%>
<%@ page import="java.util.List"%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://eclipse.org/packagedrone/repo/channel" prefix="pm" %>

<%@ taglib tagdir="/WEB-INF/tags/main" prefix="h" %>
<%@ taglib tagdir="/WEB-INF/tags/org.eclipse.packagedrone.repo.osgi.web" prefix="osgi" %>

<%

FeatureInformation fi = (FeatureInformation)request.getAttribute ( "feature" );

List<PluginInclude> includedPlugins = new ArrayList<> ( fi.getIncludedPlugins ()) ;
Collections.sort ( includedPlugins );
pageContext.setAttribute ( "includedPlugins", includedPlugins );

List<FeatureInclude> includedFeatures = new ArrayList<> ( fi.getIncludedFeatures ()) ;
Collections.sort ( includedFeatures );
pageContext.setAttribute ( "includedFeatures", includedFeatures );


List<Requirement> requirements = new ArrayList<> ( fi.getRequirements ()) ;
Collections.sort ( requirements );
pageContext.setAttribute ( "requirements", requirements );

%>
<h:main title="${fn:escapeXml(feature.id)}" subtitle="${fn:escapeXml(feature.version) }">

<h:breadcrumbs/>

<ul class="nav nav-tabs" role="tablist">
    <li role="presentation" class="active"><a href="#home" aria-controls="home" role="tab" data-toggle="tab">Core information</a></li>
    <li role="presentation"><a href="#plugins" aria-controls="plugins" role="tab" data-toggle="tab">Included Plugins</a></li>
    <li role="presentation"><a href="#features" aria-controls="features" role="tab" data-toggle="tab">Included Features</a></li>
    <li role="presentation"><a href="#requirements" aria-controls="requirements" role="tab" data-toggle="tab">Requirements</a></li>
    <li role="presentation"><a href="#description" aria-controls="description" role="tab" data-toggle="tab">Description</a></li>
    <li role="presentation"><a href="#copyright" aria-controls="copyright" role="tab" data-toggle="tab">Copyright</a></li>
    <li role="presentation"><a href="#license" aria-controls="license" role="tab" data-toggle="tab">License</a></li>
</ul>

<div class="tab-content form-padding">

<%-- CORE --%>

<div role="tabpanel" class="tab-pane active" id="home">
    
    <div class="container-fluid">
	    <div class="row">
	    
		    <div class="col-md-6">
		    
			    <h3 class="details-heading">Core Attributes</h3>
			    
			    <dl class="dl-horizontal details">
			        <dt>Id</dt>
			        <dd>
			        ${fn:escapeXml(feature.id) }
			        </dd>
			        
			        <dt>Version</dt>
			        <dd>${fn:escapeXml(feature.version) }</dd>
			        
			        <dt>Label</dt>
			        <dd><osgi:translated data="${feature }" property="label" /></dd>
			        
                    <dt>Vendor</dt>
                    <dd><osgi:translated data="${feature }" property="provider" /></dd>
			        
			        <dt>Branding Plugin</dt>
                    <dd>${fn:escapeXml(feature.plugin) }</dd>
                    

			    </dl>
		    
		    </div>
	    
	    
		    <div class="col-md-6">
	        
	            <h3 class="details-heading">Qualifiers</h3>
	            
	            <dl class="dl-horizontal details">
	            
	                <dt>Operating Systems</dt>
	                <dd>
	                   <ul>
	                       <c:forEach var="i" items="${feature.qualifiers.operatingSystems }"><li>${fn:escapeXml(i) }</li></c:forEach>
	                   </ul>
	                </dd>
	                
	                <dt>Window Systems</dt>
                    <dd>
                       <ul>
                           <c:forEach var="i" items="${feature.qualifiers.windowSystems }"><li>${fn:escapeXml(i) }</li></c:forEach>
                       </ul>
                    </dd>
                    
                    <dt>Architectures</dt>
                    <dd>
                       <ul>
                           <c:forEach var="i" items="${feature.qualifiers.architectures }"><li>${fn:escapeXml(i) }</li></c:forEach>
                       </ul>
                    </dd>
                    
                    <dt>Languages</dt>
                    <dd>
                       <ul>
                           <c:forEach var="i" items="${feature.qualifiers.languages }"><li>${fn:escapeXml(i) }</li></c:forEach>
                       </ul>
                    </dd>
	                
	            </dl>
	            
	            <code>${fn:escapeXml(feature.qualifiers.toFilterString()) }</code>
	            
	        </div>
        </div>
        
    </div>
    
</div>

<%-- Included plugins --%>

<div role="tabpanel" class="tab-pane" id="plugins">

    <table class="table table-condensed table-striped table-hover">
    
        <thead>
            <tr>
                <th>Name</th>
                <th>Version</th>
                <th>Qualifier</th>
                <th>Unpack</th>
            </tr>
        </thead>
        
        <tbody>
            <c:forEach var="pi" items="${includedPlugins }">
                <tr>
                    <td>${fn:escapeXml(pi.id) }</td>
                    <td>${fn:escapeXml(pi.version) }</td>
                    <td>
                        <c:if test="${not empty pi.qualifiers and not pi.qualifiers['empty'] }">
                            <code>${fn:escapeXml(pi.qualifiers.toFilterString()) }</code>
                        </c:if>
                    </td>
                    <td>
                        <c:if test="${pi.unpack }">unpack</c:if>
                    </td>
                </tr>
            </c:forEach>
        </tbody>
    
    </table>

</div>

<%-- Included features --%>

<div role="tabpanel" class="tab-pane" id="features">

    <table class="table table-condensed table-striped table-hover">
    
        <thead>
            <tr>
                <th>Id</th>
                <th>Version</th>
                <th>Name</th>
                <th>Optional</th>
                <th>Qualifier</th>
            </tr>
        </thead>
        
        <tbody>
            <c:forEach var="fi" items="${includedFeatures }">
                <tr>
                    <td>${fn:escapeXml(fi.id) }</td>
                    <td>${fn:escapeXml(fi.version) }</td>
                    <td>${fn:escapeXml(fi.name) }</td>
                    <td>
                        <c:if test="${fi.optional }">optional</c:if>
                    </td>
                    <td>
                        <c:if test="${not empty fi.qualifiers and not fi.qualifiers['empty'] }">
                            <code>${fn:escapeXml(fi.qualifiers.toFilterString()) }</code>
                        </c:if>
                    </td>
                </tr>
            </c:forEach>
        </tbody>
    
    </table>

</div>

<%-- Requirements --%>

<div role="tabpanel" class="tab-pane" id="requirements">

    <table class="table table-condensed table-striped table-hover">
    
        <thead>
            <tr>
                <th>Id</th>
                <th>Type</th>
                <th>Version</th>
                <th>Rule</th>
                <th>Version Range</th>
            </tr>
        </thead>
        
        <tbody>
            <c:forEach var="req" items="${requirements }">
                <tr>
                    <td>${fn:escapeXml(req.id) }</td>
                    <td>${fn:escapeXml(req.type) }</td>
                    <td>${fn:escapeXml(req.version) }</td>
                    <td>${fn:escapeXml(req.matchRule) }</td>
                    <td>${fn:escapeXml(req.matchRule.makeRange(req.version)) }
                </tr>
            </c:forEach>
        </tbody>
    
    </table>

</div>

<%-- Description --%>

<div role="tabpanel" class="tab-pane" id="description">

    <div class="container-fluid">
        <div class="row">
            <div class="col-md-12">
                <c:if test="${not empty feature.descriptionUrl}">
                    <dl class="dl-horizontal details">
                        <dt>Link</dt>
                        <dd><h:artifactLink url="${feature.translate(feature.descriptionUrl) }" artifactId="${artifact.id}">${fn:escapeXml(feature.translate(feature.descriptionUrl)) }</h:artifactLink><osgi:translatedLabels data="${feature }" property="descriptionUrl" /></dd>
                    </dl>
                </c:if>
            </div>
        </div>
        <div class="row form-padding">
            <div class="col-md-12">
                <c:if test="${not empty feature.description }">
                    <pre>${fn:escapeXml(feature.translate(feature.description)) }</pre>
                </c:if>
            </div>
        </div>
    </div>

</div>

<%-- License --%>

<div role="tabpanel" class="tab-pane" id="license">

    <div class="container-fluid">
        <div class="row">
            <div class="col-md-12">
                <c:if test="${not empty feature.licenseUrl}">
                    <dl class="dl-horizontal details">
                        <dt>Link</dt>
                        <dd><h:artifactLink url="${feature.translate(feature.licenseUrl) }" artifactId="${artifact.id}">${fn:escapeXml(feature.translate(feature.licenseUrl)) }</h:artifactLink><osgi:translatedLabels data="${feature }" property="licenseUrl" /></dd>
                    </dl>
                </c:if>
            </div>
        </div>
        <div class="row form-padding">
            <div class="col-md-12">
                <c:if test="${not empty feature.license }">
                    <pre>${fn:escapeXml(feature.translate(feature.license)) }</pre>
                </c:if>
            </div>
        </div>
    </div>

</div>

<%-- Copyright --%>

<div role="tabpanel" class="tab-pane" id="copyright">

    <div class="container-fluid">
        <div class="row">
            <div class="col-md-12">
            
                <c:if test="${not empty feature.copyrightUrl}">
	                <dl class="dl-horizontal details">
	                    <dt>Link</dt>
	                    <dd><h:artifactLink url="${feature.translate(feature.copyrightUrl) }" artifactId="${artifact.id}">${fn:escapeXml(feature.translate(feature.copyrightUrl)) }</h:artifactLink><osgi:translatedLabels data="${feature }" property="copyrightUrl" /></dd>
	                </dl>
                </c:if>
                
            </div>
        </div>
        <div class="row form-padding">
            <div class="col-md-12">
                <c:if test="${not empty feature.copyright }">
                    <pre>${fn:escapeXml(feature.translate(feature.copyright)) }</pre>
                </c:if>
            </div>
        </div>
    </div>

</div>

<%-- end of tab --%>
</div><%-- tabpanel --%>



<script type="text/javascript">
$(function () {
	  $('[data-toggle="tooltip"]').tooltip()
	})
</script>

</h:main>
