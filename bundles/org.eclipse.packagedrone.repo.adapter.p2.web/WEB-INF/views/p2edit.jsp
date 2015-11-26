<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8" trimDirectiveWhitespaces="true"%>
    
<%@ taglib tagdir="/WEB-INF/tags/main" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://eclipse.org/packagedrone/repo/channel" prefix="pm" %>
<%@ taglib uri="http://eclipse.org/packagedrone/web/form" prefix="form"%>

<h:main title="Edit P2 Channel Information" subtitle="${pm:channel(channel) }">

<h:breadcrumbs/>

<div class="container form-padding">

<form:form action="" method="POST" cssClass="form-horizontal">
    
    <h:formEntry label="Title" path="title" command="command">
        <form:input path="title" cssClass="form-control" placeholder="Optional title for the repository" required="false"/>
    </h:formEntry>
    
    <h:formEntry label="Mirrors URL" path="mirrorsUrl" command="command">
        <form:input path="mirrorsUrl" cssClass="form-control" placeholder="Optional URL to a P2 mirrors list"/>
    </h:formEntry>
    
    <h:formEntry label="Statistics URL" path="statisticsUrl" command="command">
        <form:input path="statisticsUrl" cssClass="form-control" placeholder="Optional URL to a P2 statistics collector"/>
        <span class="help-block">
        Also see <a href="https://wiki.eclipse.org/Equinox_p2_download_stats" target="_blank">Equinox p2 download stats</a> for more
        informations about P2 download statistics.
        </span>
    </h:formEntry>
    
    <h:formButtons>
        <button type="submit" class="btn btn-primary">Update</button>
        <button type="reset" class="btn btn-default">Reset</button>
    </h:formButtons>
    
</form:form>
</div>

</h:main>