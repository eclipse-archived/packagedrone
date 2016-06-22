<%@ page 
  language="java"
  contentType="text/html; charset=UTF-8"
  pageEncoding="UTF-8"
  trimDirectiveWhitespaces="true"
  %>

<!--
Copyright (c) 2016 IBH SYSTEMS GmbH.
All rights reserved. This program and the accompanying materials
are made available under the terms of the Eclipse Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/epl-v10.html

Contributors:
   IBH SYSTEMS GmbH - initial API and implementation
-->  

<%@ page import="org.eclipse.packagedrone.sec.web.ui.AccessTokenController"%>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="web" uri="http://eclipse.org/packagedrone/web" %>
<%@ taglib prefix="form" uri="http://eclipse.org/packagedrone/web/form" %>

<%@ taglib prefix="h" tagdir="/WEB-INF/tags/main" %>

<h:main title="Edit token" subtitle="${fn:escapeXml(token.id) }">

<h:breadcrumbs/>

<div class="container">

  <form:form action="" method="POST"  cssClass="form-horizontal">

    <h:formEntry label="Token">
      <pre>${fn:escapeXml(token.token) }</pre>
    </h:formEntry>
      
    <h:formEntry label="Description" path="description">
      <form:input path="description" cssClass="form-control" type="text" placeholder="Optional plain text description"/>
    </h:formEntry>
      
    <form:errors path="" var="error">
      <div class="alert alert-danger">${fn:escapeXml(error.message) }</div>
    </form:errors>
    
    <h:formButtons>
      <input type="submit" value="Update" class="btn btn-primary">
      <a href="<c:url value="/accessToken"/>" class="btn btn-default">Cancel</a>
      </h:formButtons>
      
  </form:form>
    
</div>
  
</h:main>