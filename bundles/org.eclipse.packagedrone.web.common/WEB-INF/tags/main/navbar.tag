<%@ tag language="java" pageEncoding="UTF-8" trimDirectiveWhitespaces="true"%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://eclipse.org/package-drone/web" prefix="web" %>

<%@ taglib tagdir="/WEB-INF/tags/main" prefix="h" %>

<%@attribute name="menu" required="true" type="org.eclipse.packagedrone.web.common.menu.Menu"%>

<%@attribute name="brand" fragment="true"%>
<%@attribute name="after" fragment="true"%>


<c:if test="${not empty menu }">

<c:set var="currentUrl" value="${pageContext.request.servletPath}" />

<nav class="navbar navbar-default">
	<div class="container-fluid">
	
	    <div class="navbar-header">
	      <button type="button" class="navbar-toggle collapsed" data-toggle="collapse" data-target="#main-menu-navbar-collapse">
	        <span class="sr-only">Toggle navigation</span>
	        <span class="icon-bar"></span>
	        <span class="icon-bar"></span>
	        <span class="icon-bar"></span>
	      </button>
	      <c:if test="${not empty brand }"><jsp:invoke fragment="brand"/></c:if>
	    </div>
	    
	    <div class="collapse navbar-collapse" id="main-menu-navbar-collapse">
		    <ul class="nav navbar-nav">
			    <c:forEach items="${menu.nodes }" var="entry">
			        <c:choose>
			            <c:when test="${entry.getClass().simpleName eq 'Entry'}">
			                <c:set var="url" value="${entry.target.renderFull(pageContext)}" />
			                <li class="${web:active(pageContext.request,url) }"><h:menuLink entry="${entry }"/></li>
			            </c:when>
			            <c:when test="${entry.getClass().simpleName eq 'SubMenu' }">
			                <li class="dropdown">
			                    <a href="#" class="dropdown-toggle" data-toggle="dropdown" role="button">${fn:escapeXml(entry.label)} <span class="caret"></span></a>
			                    <ul class="dropdown-menu" role="menu">
			                         <c:forEach items="${entry.nodes }" var="subEntry">
			                            <c:choose>
			                                <c:when test="${subEntry.getClass().simpleName eq 'Entry'}">
			                                    <c:set var="url" value="${subEntry.target.renderFull(pageContext)}" />
			                                    <li class="${web:active(pageContext.request,url) }"><h:menuLink entry="${subEntry }"/></li>
			                                </c:when>
			                            </c:choose>
			                         </c:forEach>
			                    </ul>
			                </li>
			            </c:when>
			        </c:choose>
			    </c:forEach>
		    </ul>
	    
	        <c:if test="${not empty after }"><jsp:invoke fragment="after"/></c:if>
	    
	    </div>
	
	</div>

</nav>

</c:if>
