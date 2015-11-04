<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
    
<%@ taglib tagdir="/WEB-INF/tags/main" prefix="h" %>
<%@ taglib tagdir="/WEB-INF/tags/storage" prefix="s" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://dentrassi.de/pm" prefix="pm" %>

<h:main title="Artifact" subtitle="${fn:escapeXml(artifact.name) } (${fn:escapeXml(artifact.id) })">

<h:buttonbar menu="${menuManager.getActions(artifact) }" />

<ul class="nav nav-tabs nav" role="tablist">
    <li role="presentation" class="active"><a href="#home" aria-controls="home" role="tab" data-toggle="tab">Information</a></li>
    <li role="presentation"><a href="#md" aria-controls="md" role="tab" data-toggle="tab">Meta Data</a></li>
    <li role="presentation"><a href="#val" aria-controls="val" role="tab" data-toggle="tab">Validation</a></li>
    <li role="presentation"><a href="#relations" aria-controls="relations" role="tab" data-toggle="tab">Relations</a></li>
</ul>

<div class="tab-content">

<%-- INFO --%>

<div role="tabpanel" class="tab-pane active" id="home">

    <div class="container-fluid">
		<div class="row">
		    <div class="col-xs-6">
		        <h3 class="details-heading">
		           ${fn:escapeXml(artifact.name) }
	                    <c:forEach var="value" items="${pm:metadata(artifact.metaData,null,'artifactLabel') }">
	                        <small><span class="label label-info">${fn:escapeXml(value) }</span></small>
	                    </c:forEach>
		        </h3>
		        
		        <dl class="dl-horizontal details">
		            <dt>ID</dt>
		            <dd>${fn:escapeXml(artifact.id) }
		            
		            <dt>Facets</dt>
		            <dd>
		                <c:forEach var="i" items="${artifact.facets }"><span class="label label-default">${fn:escapeXml(i) }</span> </c:forEach>
		            </dd>
		            
		        </dl>
		    </div>
		</div>
	</div>

</div>

<%-- META DATA --%>

<div role="tabpanel" class="tab-pane" id="md">
<h:metaDataTable metaData="${artifact.metaData }"/>
</div>

<%-- VALIDATION --%>

<div role="tabpanel" class="tab-pane table-responsive" id="val">
    <s:valTable messages="${artifact.validationMessages }"/>
</div>

<%-- RELATIONS --%>

<p>

<div role="tabpanel" class="tab-pane" id="relations">
<dl class="dl-horizontal">

<c:if test="${not empty artifact.parentId }">
<dt>Parent<dt><dd><a href="<c:url value="/artifact/${artifact.parentId }/view"/>">${ artifact.parentId }</a></dd>
</c:if>

<c:if test="${not empty artifact.childIds }">
<dt>Children<dt><dd>
    <ul>
        <c:forEach var="child" items="${artifact.childIds }">
          <li><a href="<c:url value="/artifact/${child }/view"/>">${ child }</a></li>
        </c:forEach>
    </ul>
</dd>
</c:if>
</dl>
</div>

<%-- end of tab --%>
</div><%-- tabpanel --%>

</h:main>