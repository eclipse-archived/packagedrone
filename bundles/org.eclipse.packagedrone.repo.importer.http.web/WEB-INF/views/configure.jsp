<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
    
<%@ taglib tagdir="/WEB-INF/tags/main" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://eclipse.org/packagedrone/web/form" prefix="form"%>

<h:main title="Import" subtitle="From HTTP source">

<script type="text/javascript">
function doAction(action) {
	var form = $('#command');
	form.attr("action", action);
	form.submit();
	return false;
} 
</script>

<div class="container-fluid form-padding">

<div class="row">
    <div class="col-md-8">
    
        <form:form action="start" method="POST" cssClass="form-horizontal">
		
		    <h:formEntry label="URL" path="url" command="command">
		        <form:input path="url" cssClass="form-control"/>
		    </h:formEntry>
		    
            <h:formEntry label="Alternate Name" path="alternateName" command="command">
                <form:input path="alternateName" cssClass="form-control"/>
                <span class="help-block">
                If set, this name will be used instead of the last segment of the URL for the artifact name.
                </span>
            </h:formEntry>
		    
		    <div class="form-group">
		        <div class="col-sm-offset-2 col-sm-10">
		            <button type="submit" class="btn ${ ok ? 'btn-default' : 'btn-primary'}">Check</button>
		            <c:if test="${ok }">
                        <button type="button" id="test" class="btn btn-primary" onclick="doAction('<c:url value="test"/>');">Proceed</button>
		            </c:if>
		            <button type="reset" class="btn btn-default">Reset</button>
		        </div>
		    </div>
		</form:form>
	
    </div>
    
    <div class="col-md-4">
        <div class="panel panel-info">
            <div class="panel-heading">
                <h4 class="panel-title"><span class="glyphicon glyphicon-info-sign"></span> Import from HTTP source</h4>
            </div>
            <div class="panel-body">
            <p>
            This importer downloads the artifact content directly from an HTTP source.
            </p>
            <p>
            For this to work, the Package Drone server instance must have network access
            to this target resource.
            </p>
            </div>
        </div>
    </div>
    
</div>

</div>

</h:main>