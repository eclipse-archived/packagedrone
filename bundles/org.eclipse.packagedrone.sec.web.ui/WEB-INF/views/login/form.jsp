<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" trimDirectiveWhitespaces="true"%>

<%@page import="org.eclipse.packagedrone.sec.UserStorage"%>

<%@ taglib tagdir="/WEB-INF/tags/main" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://eclipse.org/packagedrone/web/form" prefix="form" %>

<h:main title="Login">

<style>
table.login-help-table {
    margin-top: 1em;
    margin-left: 2em;
}
table.login-help-table th {
    padding-right: 1em;
    vertical-align: baseline;
    text-align: right;
}
table.login-help-table td {
    vertical-align: baseline;
}
</style>

<div class="container-fluid">

<c:if test="${not empty errorTitle }">
    <div class="row">
        <div class="col-md-offset-1 col-md-7">
            <c:choose>
                <c:when test="${empty details }">
                    <div class="alert alert-danger">
                        ${fn:escapeXml(errorTitle) }
                    </div>
                </c:when>
                <c:otherwise>
					<div class="alert alert-danger">
						<strong>${fn:escapeXml(errorTitle) }</strong>
						${fn:escapeXml(details) }
    				</div>
                </c:otherwise>
            </c:choose>
        
		    
	    </div>
    </div>
</c:if>

<div class="row">

    <div class="col-md-offset-1 col-md-7">
    
	    <form:form action="" method="POST"  cssClass="form-horizontal">
	        <h:formEntry label="E-Mail"  command="command" path="email">
	            <%--
	              - Although we state that this is an e-mail field, we do allow other user names (e.g. 'admin') as well.
	              - So we cannot set the type to 'email' since some browser validate this on the client side and prevent
	              - the form to be submitted.
	              --%>
	            <form:input path="email" cssClass="form-control" type="text"/>
	        </h:formEntry>
	        
	        <h:formEntry label="Password"  command="command" path="password">
	            <form:input path="password" cssClass="form-control" type="password"/>
	            <span class="help-block pull-right">
	               <a href="<c:url value="/signup/reset"/>"><small>Forgot your password?</small></a>
	            </span>
	        </h:formEntry>
	        
		    <div class="form-group">
		            <div class="col-sm-offset-2 col-sm-10">
		            <div class="checkbox">
		                <label>
		                    <input name="rememberMe" id="rememberMe" type="checkbox"> Remember me on this computer
		                </label>
		            </div>
		        </div>
		    </div>
	        
			<div class="form-group">
			    <div class="col-sm-offset-2 col-sm-10">
			        <button type="submit" class="btn btn-primary">Sign in</button>
			    </div>
			</div>
	
	    </form:form>

    </div>
    
    <div class="col-md-4">
	    <c:if test="${failureCount gt 2 }">
	        <div class="panel panel-info">
	            <div class="panel-heading"><h3 class="panel-title">Forgot your password?</h3></div>
	            <div class="panel-body">
	            If you forgot your password, or your account was created without a password, then you can
	            <a href="/signup/reset">request a new</a> one.
	            </div>            
	        </div>
	    </c:if>
	    <c:if test="${showAdminMode }">
            <div class="panel panel-info">
                <div class="panel-heading"><h3 class="panel-title">Admin Mode</h3></div>
                <div class="panel-body">
                It looks like your system has no registered users. In order to log in use
                the following credentials:
                <table class="login-help-table">
                    <tr>
                        <th>E-Mail</th><td><code>admin</code></td>
                    </tr>
                    <tr>
                        <th>Password</th>
                        <td>
                            <a
                                href="#"
                                data-toggle="modal"
                                data-target="#dlg-admin-token"
                                >
                                Admin Token
                            </a>
                        </td>
                    </tr>
                </table>
                </div>
            </div>
	    </c:if>
    </div>
        
</div></div>

<div class="modal" id="dlg-admin-token" tabindex="-1" role="dialog" aria-hidden="true">
  <div class="modal-dialog">
    <div class="modal-content">
      <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
        <h4 class="modal-title">Admin Token</h4>
      </div>
      <div class="modal-body">
        <jsp:include page="adminToken.jsp"/>
      </div>
      <div class="modal-footer">
        <button type="button" class="btn btn-success" data-dismiss="modal">Got it!</button>
      </div>
    </div><%-- /.modal-content --%>
  </div><%-- /.modal-dialog --%>
</div><%-- /.modal --%>

</h:main>