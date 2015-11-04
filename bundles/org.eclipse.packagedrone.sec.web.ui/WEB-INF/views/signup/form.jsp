<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" trimDirectiveWhitespaces="true"%>

<%@ taglib tagdir="/WEB-INF/tags/main" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://dentrassi.de/osgi/web/form" prefix="form" %>

<h:main title="Register">

<jsp:attribute name="head">
    <c:if test="${not empty siteInformation.recaptchaSiteKey }">
        <script src="https://www.google.com/recaptcha/api.js" async defer></script>
    </c:if>
</jsp:attribute>

<jsp:attribute name="body">

<div class="container-fluid">

	<div class="row">
	
		<div class="col-md-10 col-lg-9">
	
	        <form:form action="" method="POST"  cssClass="form-horizontal">
		   
			    <h:formEntry label="E-Mail"  command="command" path="email">
                    <div class="input-group">
                        <span class="input-group-addon">@</span>
                        <form:input path="email" cssClass="form-control" type="email" placeholder="Valid e-mail address"/>
                    </div>
			    </h:formEntry>
			    
			    <h:formEntry label="Password"  command="command" path="password">
			        <form:input path="password" cssClass="form-control" type="password"/>
			    </h:formEntry>
			    
			    <h:formEntry label="Password (repeat)"  command="command" path="passwordRepeat">
			        <form:input path="passwordRepeat" cssClass="form-control" type="password"/>
			    </h:formEntry>
			    
			    <h:formEntry label="Real Name"  command="command" path="name" optional="true">
			        <form:input path="name" cssClass="form-control" placeholder="Optional real name"/>
			    </h:formEntry>
			    
			    <c:if test="${not empty siteInformation.recaptchaSiteKey }">
			        <h:formGroup additionalCssClass="${form:validationState(pageContext,'captcha',null,'','has-error') }">
                        <jsp:attribute name="body">
                            <div class="g-recaptcha" data-sitekey="${fn:escapeXml(siteInformation.recaptchaSiteKey) }"></div>
                        </jsp:attribute>
                        <jsp:attribute name="errors">
                            <ul class="help-block">
                            <c:forEach var="error" items="${form:errors(pageContext, 'captcha', null, false) }">
                                <li>${fn:escapeXml(error) }</li>
                            </c:forEach>
                            </ul>
                        </jsp:attribute>
			        </h:formGroup>
			    </c:if>
		
		        <h:formButtons>
                    <button type="submit" class="btn btn-primary">Register</button>
		        </h:formButtons>
		
			</form:form>
		</div>
		
		<div class="col-md-2 col-lg-3">
		   <c:if test="${ duplicateEmail }">
		   
		   <div class="panel panel-info" id="duplicateEmailBox">
		       <div class="panel-heading"><h3 class="panel-title">Re-Request E-Mail</h3></div>
		       <div class="panel-body">
		           If you already signed up, but did not get the verification e-mail, you can
		           <a href="<c:url value="/signup/requestEmail"/>">re-request</a> this e-mail.
		       </div>
		   </div>
		   </c:if>
		</div>
		
	</div>
</div>

</jsp:attribute>

</h:main>