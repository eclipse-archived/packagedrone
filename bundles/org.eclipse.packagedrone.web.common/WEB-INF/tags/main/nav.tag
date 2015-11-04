<%@ tag language="java" pageEncoding="UTF-8" trimDirectiveWhitespaces="true"%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://eclipse.org/package-drone/web" prefix="web" %>

<%@ taglib tagdir="/WEB-INF/tags/main" prefix="h" %>

<%@attribute name="menu" type="org.eclipse.packagedrone.web.common.menu.Menu"%>

<c:if test="${not empty menu }">

<ul class="nav nav-tabs">
  <c:forEach items="${menu.nodes }" var="entry">
  
    <c:choose>
        <c:when test="${entry.getClass().simpleName eq 'Entry'}">
            <c:set var="url" value="${entry.target.renderFull(pageContext)}" />
            <li role="presentation" class='${web:active(pageContext.request, url)}'><h:menuLink entry="${entry }" /></li>
        </c:when>
        
        <c:when test="${entry.getClass().simpleName eq 'SubMenu' }">
             <li role="presentation" class="dropdown">
                 <a href="#" class="dropdown-toggle" data-toggle="dropdown" role="button">${fn:escapeXml(entry.label)} <span class="caret"></span></a>
                 <ul class="dropdown-menu" role="menu">
                      <c:forEach items="${entry.nodes }" var="subEntry">
                         <c:choose>
                             <c:when test="${subEntry.getClass().simpleName eq 'Entry'}">
                                 <c:set var="url" value="${subEntry.target.renderFull(pageContext)}" />
                                 <li class="${web:active(pageContext.request, url)}"><h:menuLink entry="${subEntry }" /></li>
                             </c:when>
                         </c:choose>
                      </c:forEach>
                 </ul>
             </li>
         </c:when>
        
    </c:choose>
  
  </c:forEach>
</ul>

</c:if>
