<%@ tag language="java" pageEncoding="UTF-8" trimDirectiveWhitespaces="true" body-content="scriptless"%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib tagdir="/WEB-INF/tags/main" prefix="h" %>

<%@attribute name="channelId" required="true" %>
<%@attribute name="artifactId" required="true" %>
<%@attribute name="url" required="true" %>

<c:if test="${not empty url }">
<li><h:artifactLink channelId="${channelId }" artifactId="${artifactId }" url="${url }"><jsp:doBody/></h:artifactLink>
</c:if>