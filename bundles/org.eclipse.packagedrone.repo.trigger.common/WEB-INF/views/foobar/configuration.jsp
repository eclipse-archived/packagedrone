<%@ page
  language="java"
  contentType="text/html; charset=UTF-8"
  pageEncoding="UTF-8"
  trimDirectiveWhitespaces="true"
  %>
  
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<%@ taglib prefix="form" uri="http://eclipse.org/packagedrone/web/form" %>

<%@ taglib prefix="h" tagdir="/WEB-INF/tags/main" %>

<h:main title="Trigger" subtitle="${triggerId }">

<div class="container">
  <div class="row">
    <form:form method="POST" cssClass="form-horizontal">
    
      <h:formEntry label="String #1" optional="true" path="string1" command="command">
        <form:input path="string1" cssClass="form-control"/>
      </h:formEntry>
      
      <h:formButtons>
        <button type="submit" class="btn btn-primary">Apply</button>
        <button type="reset" class="btn btn-default">Reset</button>
        <a href="/trigger/channel/${fn:escapeXml(channelId) }/list" class="btn btn-default">Cancel</a>
      </h:formButtons>
    </form:form>
  </div>
</div>

</h:main>
