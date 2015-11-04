<%@ page
    language="java"
    contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"
    trimDirectiveWhitespaces="true"
    %>

<%@ taglib tagdir="/WEB-INF/tags/main" prefix="h" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://eclipse.org/package-drone/web" prefix="web" %>

<%
pageContext.setAttribute ( "admin", request.isUserInRole ( "ADMIN" ) );
%>

<web:define name="entryBody">

    <div
    class="list-group-item 
    ${ ( task.state eq 'DONE' ) ? 'list-group-item-success' : '' }
    ${ ( task.state eq 'FAILED' ) ? 'list-group-item-danger' : '' }
    ">

	    <h4 class="list-group-item-heading">${fn:escapeXml(task.title) }
	    
		    <c:if test="${(not empty task.target) and (not ( task.state eq 'DONE' )) }"><div class="pull-right">
			    <a href="${task.target.render(pageContext.request) }">
                    <span class="glyphicon glyphicon-chevron-right" aria-hidden="true"></span>
			    </a>
		    </div></c:if>
		    
	    </h4>
	    
	    <p class="list-group-item-text">${task.description }</p>
	    
    </div>
    
</web:define>

<h:main title="Setup" subtitle="Prepare your system">

<p class="lead">
There are a few things you have to do in order to setup up Package Drone
</p>

<div class="container-fluid"><div class="row">

    <div class="col-md-4">

		<div class="list-group">
			<c:forEach var="task" items="${tasks }">
			     <web:call name="entryBody"/>
			</c:forEach>
		</div>
	</div>
	
	<div class="col-md-4">
	   <c:if test="${admin }">
	       <div class="alert alert-info">
	           <strong>Import Configuration!</strong> If you have an exported configuration archive, you can <a class="alert-link" href="<c:url value="/system/backup"/>">import it</a>!.
	       </div>
	   </c:if>
	</div>

</div></div>

</h:main>