<%@ tag language="java" pageEncoding="UTF-8" trimDirectiveWhitespaces="true" body-content="empty"%>

<%@attribute name="data" required="true" type="java.lang.Object"%>
<%@attribute name="property" required="true" type="java.lang.String"%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<%@ taglib tagdir="/WEB-INF/tags/org.eclipse.packagedrone.repo.osgi.web" prefix="osgi" %>

${fn:escapeXml(data.translate(data[property])) }
<osgi:translatedLabels data="${data }" property="${property }" />