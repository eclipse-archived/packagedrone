<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" trimDirectiveWhitespaces="true"%>

<%@ taglib tagdir="/WEB-INF/tags/main" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://dentrassi.de/osgi/web/form" prefix="form" %>
<%@ taglib uri="http://eclipse.org/packagedrone/repo/channel" prefix="pm" %>

<h:main title="Import to channel" subtitle="${pm:channel(channel) }">

<div class="container-fluid">

<div class="row">

    <div class="col-md-4">

        <h2>Available Importers</h2>

        <div class="list-group">
            <c:forEach var="desc" items="${descriptions }">
                <a href="<c:url value="${desc.configurationTarget.render(pageContext.request) }"/>" class="list-group-item">
                    <h4 class="list-group-item-heading">${fn:escapeXml(desc.label) }</h4>
                    <p class="list-group-item-text">
                    ${fn:escapeXml(desc.description) }
                    </p>
                </a>
            </c:forEach>
        </div>
        
    </div>
    
    <div class="col-md-8">
    
        <div class="jumbotron">
            <h1>Import Artifacts</h1>
            <p>
                Start importing artifacts into Package Drone by selecting an importer on the left side.
            </p>
        </div>
    
    </div>
    
</div></div>

</h:main>