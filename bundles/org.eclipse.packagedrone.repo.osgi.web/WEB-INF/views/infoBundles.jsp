<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" trimDirectiveWhitespaces="true"%>
    
<%@ taglib tagdir="/WEB-INF/tags/main" prefix="h" %>
<%@ taglib tagdir="/WEB-INF/tags/org.eclipse.packagedrone.repo.osgi.web" prefix="osgi" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<%@ taglib uri="http://eclipse.org/packagedrone/repo/channel" prefix="pm" %>

<h:main title="OSGi Bundles" subtitle="${pm:channel(channel) }">

<h:buttonbar menu="${menuManager.getActions(channel) }"/>

<h:nav menu="${menuManager.getViews(channel) }"/>

<div class="table-responsive">

<table class="table table-striped table-hover">

<thead>
    <tr>
        <th>Symbolic Name</th>
        <th>Version</th>
        <th>Name</th>
        <th>EE</th>
        <th>Links</th>
    <tr>
</thead>

<tbody>
    <c:forEach var="bundle" items="${bundles }">
    <tr>
        <td><a href="<c:url value="/osgi.info/channel/${fn:escapeXml(channel.id)}/artifact/${bundle.artifactId }/viewBundle"/>">${fn:escapeXml(bundle.id) }</a></td>
        <td>${bundle.version }</td>
        
        <td>
        <c:choose>
            <c:when test="${not empty bundle.description }">
                <a tabindex="0" href="#" data-toggle="popover" data-trigger="hover" data-placement="left" title="${fn:escapeXml(bundle.translate(bundle.name)) }" data-content="${fn:escapeXml(bundle.translate(bundle.description)) }">${fn:escapeXml(bundle.translate(bundle.name)) }</a><osgi:translatedLabels data="${bundle }" property="name" />
            </c:when>
            <c:otherwise>
                <osgi:translated data="${bundle }" property="name" />            
            </c:otherwise>
        </c:choose>
        </td>
        
        <td>
        <c:forEach var="ee" items="${bundle.requiredExecutionEnvironments }">
            <span class="label label-default">${fn:escapeXml(ee) }</span>
        </c:forEach>
        </td>
        
        <td>
            <ul class="link-list">
                <h:artifactLinkListItem channelId="${bundle.channelId }" artifactId="${bundle.artifactId }" url="${bundle.translate(bundle.docUrl) }">Documentation</h:artifactLinkListItem>
                <h:artifactLinkListItem channelId="${bundle.channelId }" artifactId="${bundle.artifactId }" url="${bundle.translate(bundle.license) }">License</h:artifactLinkListItem>
            </ul>
        </td>
    </tr>
    </c:forEach>
</tbody>

</table>

</div>

<script type="text/javascript">
$(function () {
	  $('[data-toggle="popover"]').popover()
	})
</script>

</h:main>

