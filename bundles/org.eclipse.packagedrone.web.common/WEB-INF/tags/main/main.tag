<%@ tag language="java" pageEncoding="UTF-8" trimDirectiveWhitespaces="true"%>

<%@ tag import="org.eclipse.packagedrone.web.common.Activator"%>
<%@ tag import="org.eclipse.packagedrone.sec.UserInformationPrincipal"%>
<%@ tag import="java.security.Principal"%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib tagdir="/WEB-INF/tags/main" prefix="h" %>

<%@ taglib uri="http://eclipse.org/packagedrone/web" prefix="web" %>

<%@ attribute name="title" required="true" %>
<%@ attribute name="subtitle" %>

<%@ attribute name="head" fragment="true" %>
<%@ attribute name="body" fragment="true" %>

<%
Principal p = request.getUserPrincipal ();
if ( p instanceof UserInformationPrincipal )
{
    jspContext.setAttribute ( "principal", ((UserInformationPrincipal)p).getUserInformation() ); 
}
%><!DOCTYPE html>
<html>

<c:set var="bootstrap" value="${pageContext.request.contextPath}/resources/bootstrap/3.3.2"/>
<c:set var="jquery" value="${pageContext.request.contextPath}/resources/jquery"/>
<c:set var="fontAwesome" value="${pageContext.request.contextPath}/resources/font-awesome/4.2.0"/>

<head>
    <title>${fn:escapeXml(title) } | <c:if test="${not empty subtitle }">${fn:escapeXml(subtitle) }${' '}|${' '}</c:if>Package Drone</title>
    
    <meta charset="UTF-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    
    <link rel="icon" href="${pageContext.request.contextPath}/resources/favicon.ico" sizes="16x16 32x32 48x48 64x64 128x128 256x256" type="image/vnd.microsoft.icon"/>

    <%-- jQuery (necessary for Bootstrap's JavaScript plugins) --%>
    <script src="${jquery}/jquery-1.11.2.min.js"></script>
    
    <%-- bootstrap --%>
    <link href="${bootstrap}/css/bootstrap.min.css" rel="stylesheet" />
    <script src="${bootstrap}/js/bootstrap.min.js"></script>
    
    <%-- it's awesome --%>
    
    <link rel="stylesheet" href="${fontAwesome}/css/font-awesome.min.css" />
    
    <%-- custom styles --%>
    
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/default.css" />
    
    <jsp:invoke fragment="head"/>
    
    <%
    Activator.extendHead ( request, out );
    %>
</head>

<body>

<c:set var="gravatar" value="${web:gravatar(principal.details.email) }"/>

<h:navbar menu="${menuManager.mainMenu }">
    <jsp:attribute name="brand">
        <a class="navbar-brand" href="<c:url value="/"/>"><img alt="Package Drone" src="<c:url value="/resources/pdrone.png" />"/></a>
    </jsp:attribute>
    <jsp:attribute name="after">
        <c:if test="${empty principal }">
            <p class="navbar-text navbar-right">
                <c:if test="${not empty siteInformation and siteInformation.allowSelfRegistration }">
                    <a href="<c:url value="/signup"/>">Register</a> or
                </c:if> 
                <a href="<c:url value="/login"/>">Sign in</a></p>
        </c:if>
        
        <c:if test="${not empty principal }">
            <p class="navbar-text navbar-right">
                <c:if test="${not empty gravatar }"><img class="gravatar" src="https://secure.gravatar.com/avatar/${gravatar }.jpg?s=24"  width="24" height="24" />&nbsp;</c:if>
                
                <a href="<c:url value="/user/${principal.id}/view"/>">
	                <c:choose>
	                    <c:when test="${not empty principal.details.name }">${fn:escapeXml(principal.details.name) }</c:when>
	                    <c:otherwise>Profile</c:otherwise>
	                </c:choose>
                </a>
                
                &mdash;
                
                &nbsp;<a href="<c:url value="/logout"/>">Sign out</a>
            </p>
        </c:if>
        
        <p class="navbar-text navbar-right">
            <c:choose>
                <c:when test="${not empty openTasks and not empty principal}">
                    <a href="/tasks" class="navbar-link">Tasks <span class="badge">${openTasks.size() }</span></a>
                </c:when>
                <c:when test="${not empty openTasks and empty principal}">
                    <a href="/tasks" class="navbar-link" data-toggle="tooltip" data-placement="bottom" title="Maintance required!">
                       <span class="glyphicon glyphicon-bell"></span>
                    </a>
                    <script type="text/javascript">
                    $(function () {
                          $('[data-toggle="tooltip"]').tooltip()
                        })
                    </script>
                </c:when>
            </c:choose>
        </p>
        
        
    </jsp:attribute>
</h:navbar>

<header class="page-header">
    <div class="container-fluid">
	    <div class="row">
	        <div class="col-md-12">
	            <h1>${fn:escapeXml(title) }<c:if test="${not empty subtitle }">&nbsp;<small>${fn:escapeXml(subtitle) }</small></c:if></h1>
	        </div>
	    </div>
    </div>
</header>

<section>
    <div id="content">
        <c:choose>
            <c:when test="${not empty body }">
                <jsp:invoke fragment="body"/>
            </c:when>
            <c:otherwise>
                <jsp:doBody/>
            </c:otherwise>
        </c:choose>
    </div>
</section>

<web:pop name="modal"/>

<footer>
    <div class="pull-right"><a href="http://packagedrone.org" target="_blank">Package Drone ${droneVersion }</a></div>
</footer>

</body>

</html>