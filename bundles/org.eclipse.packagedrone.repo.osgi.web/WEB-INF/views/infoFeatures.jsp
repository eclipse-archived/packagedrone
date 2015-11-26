<%@ page language="java" contentType="text/html; charset=UTF-8" trimDirectiveWhitespaces="true"
    pageEncoding="UTF-8"%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://eclipse.org/packagedrone/repo/channel" prefix="pm" %>

<%@ taglib tagdir="/WEB-INF/tags/main" prefix="h" %>
<%@ taglib tagdir="/WEB-INF/tags/org.eclipse.packagedrone.repo.osgi.web" prefix="osgi" %>

<h:main title="Eclipse Features" subtitle="${pm:channel(channel) }">

<h:buttonbar menu="${menuManager.getActions(channel) }"/>

<h:nav menu="${menuManager.getViews(channel) }"/>

<table class="table table-striped">

<thead>
    <tr>
        <th>Symbolic Name</th>
        <th>Version</th>
        <th>Name</th>
        <th>Links</th>
    <tr>
</thead>

<tbody>
    <c:forEach var="feature" items="${features }">
    <tr>
        <td>
            <a href="<c:url value="/osgi.info/channel/${fn:escapeXml(channel.id)}/artifact/${feature.artifactId }/viewFeature"/>">${fn:escapeXml(feature.id) }</a>
        </td>
        <td>${feature.version }</td>
        
        <td>
        <c:choose>
            <c:when test="${not empty feature.description }">
                <a tabindex="0" href="#" data-toggle="popover" data-trigger="hover" data-placement="left" title="${fn:escapeXml(feature.translate(feature.label)) }" data-content="${fn:escapeXml(feature.translate(feature.description)) }">${fn:escapeXml(feature.translate(feature.label)) }</a><osgi:translatedLabels data="${feature }" property="label" />
            </c:when>
            <c:otherwise>
                <osgi:translated data="${feature }" property="label" />
            </c:otherwise>
        </c:choose>
        </td>
        
        <td>
            <ul class="link-list">
                <h:artifactLinkListItem channelId="${feature.channelId }" artifactId="${feature.artifactId }" url="${feature.translate(feature.licenseUrl) }">License</h:artifactLinkListItem>
                <h:artifactLinkListItem channelId="${feature.channelId }" artifactId="${feature.artifactId }" url="${feature.translate(feature.copyrightUrl) }">Copyright</h:artifactLinkListItem>
                <h:artifactLinkListItem channelId="${feature.channelId }" artifactId="${feature.artifactId }" url="${feature.translate(feature.descriptionUrl) }">Description</h:artifactLinkListItem>
            </ul>
        </td>
        
    </tr>
    </c:forEach>
</tbody>

</table>

<script type="text/javascript">
$(function () {
      $('[data-toggle="popover"]').popover()
    })
</script>

</h:main>