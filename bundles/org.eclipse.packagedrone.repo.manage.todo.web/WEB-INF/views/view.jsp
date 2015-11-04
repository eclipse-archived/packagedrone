<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
    
<%@ taglib tagdir="/WEB-INF/tags/main" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://dentrassi.de/pm" prefix="pm" %>

<h:main title="Maintance tasks" subtitle="Open maintancen tasks">

<div class="container-fluid">

    <div class="row">
    
        <div class="col-md-12">
        
            <c:choose>
            
                <c:when test="${empty openTasks }">
                    <div class="well well-lg">
                        <h2>No open tasks</h2>
                        <p>Looks like you cleaned it all up! ;-)</p>
                    </div>
                </c:when>
            
                <c:otherwise>
                
                    <c:forEach var="task" items="${ openTasks }">
                        <div class="panel panel-default">
                            <div class="panel-heading">
                                <h3 class="panel-title">${fn:escapeXml(task.title) }</h3>
                            </div>
                            <div class="panel-body">
	                            <div class="pull-right" style="padding-left: 1em; padding-bottom: 1em;">
	                                <form action="<c:url value="${task.target.render(pageContext.request) }"/>" method="${task.targetRequestMethod }">
	                                    <h:button button="${task.button }" type="submit"/>
	                                </form>
	                            </div>
                                ${task.description } <%-- we allow HTML here --%>
                            </div>
                        </div>
                    </c:forEach>
                
	            
	            </c:otherwise>
            
            </c:choose>
        
        </div>
    
    </div>

</div>

</h:main>