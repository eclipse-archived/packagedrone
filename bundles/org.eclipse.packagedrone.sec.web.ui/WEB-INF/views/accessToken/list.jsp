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

<%@ taglib prefix="h" tagdir="/WEB-INF/tags/main" %>

<%
pageContext.setAttribute ( "TAG", AccessTokenController.TAG );
%>

<h:main title="Access Tokens">

<h:buttonbar menu="${menuManager.getActions(TAG) }" />

<div class="container-fluid">

<span id="copy" style="display: none;"></span>

<div class="table-responsive">
	
  <table class="table table-condensed table-striped">
	
	<thead>
      <tr>
        <th>ID <span class="glyphicon glyphicon-sort-by-attributes"></span></th>
        <th>Token</th>
        <th>Description</th>
        <th>Created</th>
        <th></th>
      </tr>
	</thead>
	
	<tbody>
      <c:forEach var="token" items="${tokens.data }">
        <tr>
          <td><a href="<c:url value="/accessToken/${fn:escapeXml(token.id) }/edit"/>">${fn:escapeXml(token.id) }</a></td>
          <td data-token="${fn:escapeXml(token.token) }" class="token">${fn:escapeXml(web:limit(token.token, 16, '…')) }</td>
          <td>${fn:escapeXml(token.description) }</td>
          <td><fmt:formatDate type="both" value="${web:toDate(token.creationTimestamp) }"/></td>
          <td>
            <a href="<c:url value="/accessToken/${fn:escapeXml(token.id)}/delete"/>" title="Delete token" class="btn btn-danger"><span class="glyphicon glyphicon-trash"></span></a>
          </td>
        </tr>
      </c:forEach>    
	</tbody>
	
	</table>
</div>

<h:pager value="${tokens }" />

</div>

<script>

function performCopy (token) {
	try {
		// clear first
		window.getSelection().removeAllRanges();  
		
		// fill, show, copy
		var node = document.getElementById("copy");
		$(node).text(token);
		$(node).show ();
		var range = document.createRange();
		range.selectNode(node);  
		window.getSelection().addRange(range);  
		  
		if ( !document.execCommand('copy') )
		{
			throw "Failed to copy";
		}
	}
	catch ( err ) {
		console.log ( err );
		window.alert( "Failed to copy to clipboard" );
	}
	
	// clear afterwards
	$(node).hide ();
	window.getSelection().removeAllRanges();  
}

if ( document.queryCommandSupported && document.queryCommandSupported('copy') ) {
	$('.token').append(" <button class='btn btn-default btn-xs' title='Copy token to clipboard'>Copy</button>");
	
	$('.token > button').on('click', function(){
		var token = $(this).parent().data("token");
		console.log ( "clicked", token );
		performCopy ( token );
	});
}
</script>

</h:main>