<%@ page
  language="java"
  contentType="text/html; charset=UTF-8"
  pageEncoding="UTF-8"
  trimDirectiveWhitespaces="true"
  %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%@ taglib prefix="h" tagdir="/WEB-INF/tags/main" %>

<%
pageContext.setAttribute ( "manager", request.isUserInRole ( "MANAGER" ) );
%>

<h:main title="About" subtitle="Eclipse Package Drone™">

<div class="container">
  <div class="row">
    <div class="col-sm-6" style="margin-top: 3em;">
    
      <div class="panel panel-info">
      
        <div class="panel-heading">
          <h3 class="panel-title">Eclipse Package Drone™</h3>
        </div>
      
        <div class="panel-body">
        
          <dl class="dl-horizontal">
            
            <dt>Version</dt>
            <dd>${version }</dd>
            
            <dt>Build ID</dt>
            <dd>${buildId }</dd>
            
          </dl>
          
          <p>
          &copy; Copyright Eclipse contributors and others 2014, 2016.  
          All rights reserved. 
          Eclipse and the Eclipse logo are trademarks of the Eclipse Foundation, Inc., <a href="https://www.eclipse.org/" target="_blank">https://www.eclipse.org/</a>.
          The Eclipse logo cannot be altered without Eclipse's permission.
          Eclipse logos are provided for use under the Eclipse logo and trademark guidelines,
          <a href="https://www.eclipse.org/logotm/" target="_blank">https://www.eclipse.org/logotm/</a>.
          Oracle and Java are trademarks or registered trademarks of Oracle and/or its affiliates.
          Other names may be trademarks of their respective owners.
          </p>
          
          <p>
          This product includes software developed by other open source projects including the Apache Software Foundation, <a href="https://www.apache.org/" target="_blank">https://www.apache.org/</a>.
          </p>
        
        </div>
      
      </div>

    </div>
    <div class="col-sm-6">
      <img class="center-block img-responsive" src="<c:url value="/resources/images/logo.png"/>"/>
    </div>
  </div>
</div>

</h:main>