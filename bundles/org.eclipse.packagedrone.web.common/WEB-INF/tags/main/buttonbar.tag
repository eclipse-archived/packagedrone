<%@ tag language="java" pageEncoding="UTF-8" trimDirectiveWhitespaces="true"%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://dentrassi.de/pm" prefix="pm" %>
<%@ taglib tagdir="/WEB-INF/tags/main" prefix="h" %>

<%@attribute name="menu" type="org.eclipse.packagedrone.web.common.menu.Menu"%>
<%@attribute name="before" fragment="true"%>
<%@attribute name="after" fragment="true"%>

<div class="button-bar btn-toolbar" role="toolbar">

    <jsp:invoke fragment="before" />

<c:if test="${not empty menu }">
    
    <c:forEach items="${menu.nodes }" var="entry">
        <c:choose>
            <c:when test="${entry.getClass().simpleName eq 'Entry'}">
                <c:set var="url" value="${entry.target.renderFull(pageContext)}" />
                <div class="btn-group" role="group">
                    <h:menuLink role="button" cssClass="btn ${pm:modifier('btn-', entry.modifier) }" entry="${entry }"/>
                </div>
            </c:when>

			<c:when test="${entry.getClass().simpleName eq 'SubMenu' }">
			
                <div class="btn-group" role="group">
                    
                    <a role="button" class="btn ${pm:modifier('btn-', entry.modifier) } dropdown-toggle" data-toggle="dropdown" aria-expanded="false">
                        <h:menuEntry entry="${entry }" />&nbsp;
                        <span class="caret"></span>
                        <span class="sr-only">Toggle Dropdown</span>
                    </a>
                    <ul class="dropdown-menu" role="menu">
                    <c:forEach items="${entry.nodes }" var="subEntry">
                            <c:choose>
                                <c:when test="${subEntry.getClass().simpleName eq 'Entry'}">
                                    <c:set var="url"
                                        value="${subEntry.target.renderFull(pageContext)}" />
                                    <li ><h:menuLink entry="${subEntry }"/></li>
                                </c:when>
                            </c:choose>
                        </c:forEach>
                    </ul>
                </div>
			</c:when>

			</c:choose>
    </c:forEach>

</c:if>

    <jsp:invoke fragment="after" />
</div>