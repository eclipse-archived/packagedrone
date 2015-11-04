<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" trimDirectiveWhitespaces="true"%>
    
<%@ taglib tagdir="/WEB-INF/tags/main" prefix="h" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://dentrassi.de/pm" prefix="pm" %>
<%@ taglib uri="http://dentrassi.de/osgi/web/form" prefix="form"%>

<h:main title="Site Properties">

<div class="container-fluid form-padding">

<div class="row">

    <div class="col-md-8">

            <form:form action="" method="POST" cssClass="form-horizontal">
            
                <fieldset>
                
                    <legend>Global</legend>
            
	                <h:formEntry label="Site Prefix" path="prefix" command="command">
	                    <form:input path="prefix" cssClass="form-control" placeholder="Optional site prefix"/>
	                    <span class="help-block">
	                    Enter a Site prefix (like <code>http://myserver.com</code>), which will be used instead of
	                    the automatically detected prefix (default: <code>${fn:escapeXml(defaultSitePrefix)}</code>).
	                    </span>
	                </h:formEntry>
                
                </fieldset>
                
                <fieldset>
                    <legend>User registration</legend>
                
	                <h:formCheckbox label="Allow self registration" path="allowSelfRegistration" command="command">
	                    <span class="help-block">
	                    Enabling self registration will allow others to create a new user on their own. Including
	                    validation of the e-mail address.
	                    </span>
	                </h:formCheckbox>
                
                </fieldset>
                
                <fieldset>
                    <legend>reCAPTCHA</legend>
                    
                    <h:formEntry label="Site Key" path="recaptchaSiteKey" command="command">
                       <form:input path="recaptchaSiteKey" cssClass="form-control"/>
                       <span class="help-block">
                           Optionally enter the <a href="http://www.google.com/recaptcha/admin" target="_blank">reCAPTCHA site key</a>.
                       </span>
                    </h:formEntry>
                    
                    <h:formEntry label="Secret Key" path="recaptchaSecretKey" command="command">
                       <form:input path="recaptchaSecretKey" cssClass="form-control"/>
                       <span class="help-block">
                           The secret key is required if the site key is set.
                       </span>
                    </h:formEntry>

                </fieldset>
                
                <div class="form-group">
                    <div class="col-sm-offset-2 col-sm-10">
                        <button type="submit" class="btn btn-primary">Update</button>
                        <button type="reset" class="btn btn-default">Reset</button>
                    </div>
                </div>
            </form:form>
        
    </div><%-- form col --%>
    
</div><%-- outer row --%>

</div><%-- outer container --%>

</h:main>