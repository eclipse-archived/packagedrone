<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
    
<%@ taglib tagdir="/WEB-INF/tags/main" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://eclipse.org/packagedrone/repo/channel" prefix="storage" %>
<%@ taglib uri="http://dentrassi.de/osgi/web/form" prefix="form" %>
<%@ taglib uri="http://eclipse.org/package-drone/web" prefix="web" %>


<%
pageContext.setAttribute ( "manager", request.isUserInRole ( "MANAGER" ) );
%>

<h:main title="Channel" subtitle="${storage:channel(channel) }">

<h:buttonbar menu="${menuManager.getActions(channel) }" />

<h:nav menu="${menuManager.getViews(channel) }"/>

<div class="container-fluid form-padding">

    <div class="row">
        <div class="col-md-6">
            <form:form action="" method="POST" cssClass="form-horizontal">
	        
	            <h:formEntry label="Group ID" path="groupId">
	                <form:input path="groupId" cssClass="form-control" placeholder="The maven group ID which will be used for creating POM files"/>
	            </h:formEntry>
	        
	            <h:formButtons>
	                <input type="submit" value="Update" class="btn btn-primary" />
	                <input type="reset" value="Reset" class="btn btn-default" />
	            </h:formButtons>
	        </form:form>
        </div>
    </div>

</div>



</h:main>