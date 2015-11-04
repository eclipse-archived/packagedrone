<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
    
<%@ taglib tagdir="/WEB-INF/tags/main" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://dentrassi.de/osgi/web/form" prefix="form"%>
<%@ taglib uri="http://dentrassi.de/osgi/job" prefix="job"%>
<%@ taglib tagdir="/WEB-INF/tags/main" prefix="h" %>

<h:main title="Import" subtitle="Test">

<div class="container-fluid form-padding">

<form action="testComplete" id="form" method="POST">
    <input type="hidden" name="jobId" value="${job.id }"/>
</form>

<div class="row">
    <div class="col-md-6 col-md-offset-3">
        <job:monitor job="${job }" oncomplete="$('#form').submit();"/>
    </div>
</div>

</div>

</h:main>