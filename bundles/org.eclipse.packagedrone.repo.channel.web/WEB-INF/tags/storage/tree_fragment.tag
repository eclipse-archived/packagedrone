<%@ tag
	language="java"
	pageEncoding="UTF-8"
	trimDirectiveWhitespaces="true"
	body-content="empty"
	%>
	
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<%@ taglib prefix="web" uri="http://eclipse.org/packagedrone/web" %>
<%@ taglib prefix="common" uri="http://eclipse.org/packagedrone/web/common" %>
<%@ taglib prefix="storage" uri="http://eclipse.org/packagedrone/repo/channel"%>
<%@ taglib prefix="table" uri="http://eclipse.org/packagedrone/web/common/table"  %>

<%@ taglib prefix="s" tagdir="/WEB-INF/tags/storage" %>
	
<%@ attribute name="map" type="java.util.Map" %>
<%@ attribute name="manager"  required="true"%>
<%@ attribute name="parentId" %>
<%@ attribute name="parents" %>
<%@ attribute name="artifacts" type="java.util.Collection" %>
<%@ attribute name="level" %>

<c:forEach var="artifact" items="${ artifacts }">
	<tr style="display: none;" data-parent="${parentId }" data-parents="${ parents }" data-level="${level }"
	      class="${storage:severityWithDefault(treeSeverityTester.getState(artifact),'') }"
	      data-severity="${artifact.getOverallValidationState() }">
	    <td style="padding-left: ${1+(level*2)}em;">
	      <c:choose>
	          <c:when test="${not empty map.get(artifact.id) }">
	              <a data-artifact="${artifact.id }" class="expander" href="#"><i class="fa fa-plus-square-o"></i></a>
	          </c:when>
	          <c:otherwise>
                     <i style="visibility: hidden;" class="fa fa-square-o"></i>
                 </c:otherwise>
	      </c:choose>
	      
	      ${fn:escapeXml(artifact.name) }
	    </td>
	    
	    <td>
			<c:forEach var="value" items="${common:metadata(artifact.metaData, null, 'artifactLabel') }" >
				<span class="label label-info">${fn:escapeXml(value) }</span>
			</c:forEach>
	    </td>
	    
	    <td class="text-right"><web:bytes amount="${artifact.size }"/></td>
	    
	    <td style="white-space: nowrap;"><time datetime="<fmt:formatDate value="${artifact.creationTimestamp}" pattern="yyyy-MM-dd'T'HH:mm:ss'Z'" timeZone="UTC" />"><fmt:formatDate value="${artifact.creationTimestamp }" type="both" /></time> </td>
	    
	    <%-- extension columns - before 0 --%>
	    
	    <table:row end="0" item="${artifact }">
        	<td><table:extension/></td>
        </table:row>
        
        <%-- command columns --%>
	    
	    <td><a href="<c:url value="/channel/${ fn:escapeXml(channel.id) }/artifacts/${ fn:escapeXml(artifact.id) }/get"/>">Download</a></td>
        <td>
          <c:if test='${artifact.is("stored") and manager}'><a href="<c:url value="/channel/${ fn:escapeXml(channel.id) }/artifacts/${artifact.id}/delete"/>">Delete</a></c:if>
        </td>
        <td><a href="<c:url value="/channel/${ fn:escapeXml(channel.id) }/artifacts/${ fn:escapeXml(artifact.id) }/view"/>">Details</a></td>
        <td><a href="<c:url value="/channel/${ fn:escapeXml(channel.id) }/artifacts/${ fn:escapeXml(artifact.id) }/dump"/>">View</a></td>
        
        <%-- extension columns - after 0 --%>
        
        <table:row start="0" item="${artifact }">
        	<td><table:extension/></td>
        </table:row>
	</tr>
	
	<s:tree_fragment map="${map }" manager="${manager}" parentId="${artifact.id }" parents="${parents } ${artifact.id }" artifacts="${map.get(artifact.id) }" level="${level+1 }"/>
</c:forEach>
