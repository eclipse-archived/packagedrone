<%@ tag
	language="java"
	pageEncoding="UTF-8"
	trimDirectiveWhitespaces="true"
	%>

<%--
  Copyright (c) 2015 IBH SYSTEMS GmbH.
  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
   
  Contributors:
      IBH SYSTEMS GmbH - initial API and implementation
--%>

<%@ attribute name="channel" type="org.eclipse.packagedrone.repo.channel.ChannelId" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<%@ taglib prefix="web" uri="http://eclipse.org/packagedrone/web" %>

<c:choose>
  <c:when test="${empty channel.names }">
    <span class="label label-primary">${fn:escapeXml(channel.id) }</span>
  </c:when>
  <c:otherwise>
    <c:forEach var="name" items="${web:sort(channel.names)}">
    <span class="label label-default">${fn:escapeXml(name) }</span>
    </c:forEach>
  </c:otherwise>
</c:choose>

<%-- append description --%>

${ ' ' }${fn:escapeXml(channel.description)}
