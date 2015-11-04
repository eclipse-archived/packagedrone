<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" trimDirectiveWhitespaces="true"%>

<%@page import="org.eclipse.packagedrone.sec.UserStorage"%>

<%@ taglib tagdir="/WEB-INF/tags/main" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://dentrassi.de/osgi/web/form" prefix="form" %>

<h:main title="E-Mail sent">

<div class="container-fluid"><div class="row">

<div class="col-xs-offset-1 col-xs-10">

<div class="alert alert-success">
    <strong>E-mail sent!</strong> The system sent you another verification e-mail. Please <a class="alert-link" target="_blank" href="https://youtube.com">wait for it to
    arrive</a> and also check you spam folder.
</div>

<div class="alert alert-info">
    <strong>Please note</strong> that the verification link in older e-mail will no longer work. Only the latest
        verification link will work!
</div>

</div></div></div>

</h:main>