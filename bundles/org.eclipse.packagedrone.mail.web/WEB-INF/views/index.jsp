<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>

<%@page import="org.eclipse.packagedrone.sec.DatabaseDetails"%>
<%@page import="org.eclipse.packagedrone.sec.UserInformationPrincipal"%>
<%@page import="java.security.Principal"%>
<%@page import="javax.servlet.http.HttpServletRequest"%>
    
<%@ taglib tagdir="/WEB-INF/tags/main" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://eclipse.org/packagedrone/web/common" prefix="pm" %>
<%@ taglib uri="http://eclipse.org/packagedrone/web/form" prefix="form"%>

<%
Principal principal = request.getUserPrincipal ();
if ( principal instanceof UserInformationPrincipal )
{
    DatabaseDetails db = ((UserInformationPrincipal)principal).getUserInformation().getDetails ( DatabaseDetails.class  );
    if ( db != null )
    {
        pageContext.setAttribute ( "email", db.getEmail () );
    }
}
%>
<h:main title="Default Mail Sender" subtitle="Setup">

<div class="container-fluid form-padding">

<div class="row">

    <div class="col-md-8">

		    <form:form action="" method="POST" cssClass="form-horizontal">
		    
		        <fieldset>
                    <legend>Authentication</legend>
		        
			        <h:formEntry label="User" path="username" command="command" optional="true">
	                    <form:input path="username" cssClass="form-control" placeholder="Username for the mail server"/>
	                </h:formEntry>
	                
	                <h:formEntry label="Password" path="password" command="command" optional="true">
	                    <form:input path="password" cssClass="form-control" type="password" placeholder="Password for the mail server"/>
	                </h:formEntry>
		        
		        </fieldset>
		    
		        <fieldset>
		        
                    <legend>Connectivity</legend>
		        
			        <h:formEntry label="Host" path="host" command="command">
			            <form:input path="host" cssClass="form-control"  placeholder="Hostname or IP of the SMTP server"/>
			        </h:formEntry>
			        
			        <h:formEntry label="Port" path="port" command="command" optional="true">
			            <form:input path="port" cssClass="form-control" type="number"  placeholder="Optional port number of the SMTP server"/>
			        </h:formEntry>
			        
			        <h:formCheckbox label="Enable TLS" path="enableStartTls" command="command">
                        <span class="help-block">
                        This will enable the <code>STARTTLS</code> command and allow for starting an encrypted connection to the mail server.
                        If you mail server does not support TLS, then sending of mails will fail. 
                        </span>
			        </h:formCheckbox>
		        
		        </fieldset>
		        
		        <fieldset>
                    <legend>Mail Settings</legend>
                    
                    <h:formEntry label="From" path="from" command="command" optional="true">
	                    <form:input path="from" cssClass="form-control"  placeholder="Optional sender e-mail" type="email"/>
	                </h:formEntry>
	                
	                <h:formEntry label="Prefix" path="prefix" command="command" optional="true">
	                    <form:input path="prefix" cssClass="form-control"  placeholder="Optional subject prefix"/>
	                </h:formEntry>
                
		        </fieldset>
		        
		        <h:formButtons>
	                <button type="submit" class="btn btn-primary">Update</button>
	                <button type="reset" class="btn btn-default">Reset</button>
		        </h:formButtons>
		        
		    </form:form>
		
	</div><%-- form col --%>
	
	<div class="col-md-4">
	
	   <div class="panel panel-${servicePresent ? 'success' : 'default' }">
	       <div class="panel-heading"><h3 class="panel-title">Mail Service</h3></div>
	       
	       <%-- info about mail service --%>
	       
	       <table class="table">
	           <tbody>
	               <tr><th>Service Present</th><td id="servicePresent">${servicePresent }</td></tr>
	           </tbody>
	       </table>
	       
         <c:if test="${servicePresent }">
	         <div class="panel-body">
             <form class="form-inline" action="<c:url value="/default.mail/config/sendTest"/>" method="post">
                 <div class="form-group">
                    <p class="form-control-static">Test E-Mail</p>
                 </div>
                 <div class="form-group">
                     <label class="sr-only" for="testEmailReceiver">Receiver Email address</label>
                     <input type="email" class="form-control"  id="testEmailReceiver" name="testEmailReceiver" required="required" placeholder="Receiver of test e-mail" value="${fn:escapeXml(email) }"/>
                 </div>
                 <button type="submit" class="btn btn-default">Send</button>
             </form>
             </div>
          </c:if>
	       
	   </div>
	   
	   <div class="panel panel-info">
	       <div class="panel-heading"><h3 class="panel-title">Relay server</h3></div>
	       <div class="panel-body">
	       <p>
	       This system needs a connection to an SMTP mail server which allows relaying of e-mails.
	       </p>
	       <p>
	       The setup might be different based an how your SMTP server is set up. Some ISP servers
	       enforce that the sender mail address (<q>From</q>) is really your e-mail account.
	       </p>
	       </div>
	   </div>
	
	</div> <%-- info col --%>

</div><%-- outer row --%>

</div><%-- outer container --%>

</h:main>