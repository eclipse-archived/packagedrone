<%@ tag language="java" pageEncoding="UTF-8" trimDirectiveWhitespaces="true"%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://eclipse.org/packagedrone/web" prefix="web" %>

<ul class="nav nav-stacked nav-pills">
   <li class="">Default</li>
    <c:url value="/channel/${channelId }/add" var="url" />
    
    <li class="${web:active(pageContext.request, url) }"><a href="${ url }">Upload</a></li>
    
    <li class="">Generated</li>
    <c:forEach items="${generators}" var="gen">
       <li
       class="${web:active(pageContext.request, gen.addTarget.renderFull(pageContext))}"
       ><a href="${gen.addTarget.renderFull(pageContext) }">${fn:escapeXml(gen.label) }</a></li>
    </c:forEach>
</ul>
