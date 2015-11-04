<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8" trimDirectiveWhitespaces="true"%>
    
<%@ taglib tagdir="/WEB-INF/tags/main" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://eclipse.org/packagedrone/repo/channel" prefix="pm" %>

<h:main title="P2 Meta Data Generation Information" subtitle="${pm:channel(channel) }">

<h:buttonbar menu="${menuManager.getActions(channel) }"/>

<h:nav menu="${menuManager.getViews(channel) }"/>

<table class="table table-properties">

<tr>
    <th>System Bundle Alias</th>
    <td>
	     <c:choose>
	        <c:when test="${not empty channelInfo.systemBundleAlias }">
	            <code>${fn:escapeXml(channelInfo.systemBundleAlias)}</code>
	        </c:when>
	        <c:otherwise>
	            <i>empty</i> – defaulting to <code>org.eclipse.osgi</code>
	        </c:otherwise>
	    </c:choose>
    </td>
</tr>

</table>

</h:main>