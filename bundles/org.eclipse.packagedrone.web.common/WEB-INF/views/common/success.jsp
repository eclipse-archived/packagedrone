<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" trimDirectiveWhitespaces="true"%>

<%@ taglib tagdir="/WEB-INF/tags/main" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://dentrassi.de/osgi/web/form" prefix="form" %>

<h:main title="${title }" subtitle="${subtitle}">

	<div class="container">
	    <div class="row">
	        <div class="col-md-offset-2 col-md-8">
	            <div class="alert alert-success">${message }</div>
	        </div>
	    </div>
	</div>

</h:main>