<%@ page language="java"
    contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"
    trimDirectiveWhitespaces="true"
    %>

<%@ taglib tagdir="/WEB-INF/tags/main" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://dentrassi.de/osgi/web/form" prefix="form" %>

<h:main title="Setup">

<div class="container-fluid ">

	<div class="row">
	
		<div class="col-sm-8">
		
		<c:if test="${not empty error }">
            <div class="alert alert-danger" role="alert"><strong>Error!</strong> ${fn:escapeXml(error) }</div>
		</c:if>
		
		<c:if test="${not empty sysProp }">
		
		  <div class="panel panel-info">
		      <div class="panel-heading"><h3 class="panel-title">System property</h3></div>
		      <div class="panel-body">
		          The storage manager of this instance is already configured using the system property
		          <code>drone.storage.base</code>.
		      </div>
		  </div>
		
		</c:if>
		
		<c:if test="${empty sysProp }">
        
			<form:form action="" method="POST" cssClass="form-horizontal">
			
			    <h:formEntry label="Base Path" command="command" path="basePath">
			        <form:input path="basePath" cssClass="form-control"/>
			        
			        <span class="help-block">
			        Enter a path on the server where the Package Drone instance should store all its data.
			        </span>
			        
			        <span class="help-block">
	                The Package Drone instance needs read and write permissions in this directory. If the directory does not yet
	                exists it will be created, if possible.
	                </span>
			        
			    </h:formEntry>
			    
			    <h:formButtons>
	                <input type="submit" value="Update" class="btn btn-primary"/>
	                <input type="reset" value="Reset" class="btn btn-default"/>
			    </h:formButtons>
			    
			</form:form>
        </c:if>
        		
		</div>
		
		<div class="col-sm-4">
		
            <c:if test="${not empty sysProp or  not empty command.basePath }">
		
            <div class="panel panel-default">
                <div class="panel-heading">
                    <h3 class="panel-title">Overview</h3>
                </div>
                
                <div class="panel-body">
	                
	                <b>
	                    Location
	                </b>
	                
	                <p>
	                <code>${not empty sysProp ? sysProp : command.basePath }</code>
	                </p>
	                
	                <b>
	                    Free space:
	                </b>
	                
	                   <div class="progress">
                            <fmt:formatNumber var="p" value="${ freeSpacePercent * 100.0}" pattern="#.#"/>
                            <div class="progress-bar" role="progressbar" aria-valuenow="${p}" aria-valuemin="0" aria-valuemax="100" style="width: ${p}%;">
	                          ${p }%
                            </div>
                        </div> <%-- progress --%>
                </div>
            </div>
            
            </c:if>
            
        </div> <%-- col --%>
	
	</div> <%-- row --%>

</div> <%-- container --%>


</h:main>