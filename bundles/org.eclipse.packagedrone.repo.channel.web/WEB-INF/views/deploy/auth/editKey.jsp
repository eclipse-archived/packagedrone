<%@ page
  language="java"
  contentType="text/html; charset=UTF-8"
  pageEncoding="UTF-8"
  trimDirectiveWhitespaces="true"
  %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://eclipse.org/packagedrone/web/form" prefix="form" %>

<%@ taglib tagdir="/WEB-INF/tags/main" prefix="h" %>

<h:main title="Edit key" subtitle="${fn:escapeXml( (empty command.name) ? id : command.name ) }">

<h:breadcrumbs/>

<div class="container-fluid">

<div class="row">

    <div class="col-md-10 col-lg-9">

        <form:form action="" method="POST"  cssClass="form-horizontal">
        
            <h:formEntry label="ID" command="command">
                <input id="id" class="form-control" readonly="readonly" value="${fn:escapeXml(id) }" />
            </h:formEntry>
        
            <h:formEntry label="Name" command="command" path="name">
                <form:input path="name" cssClass="form-control"/>
            </h:formEntry>
            
            <div class="form-group">
                <div class="col-sm-offset-2 col-sm-10">
                    <button type="submit" class="btn btn-primary">Update</button>
                    <a href="<c:url value="/deploy/auth/group/${groupId}/view"/>" class="btn btn-default">Cancel</a>
                </div>
            </div>
            
        </form:form>
        
    </div>
    
</div></div>



</h:main>