<%--
  Copyright (c) 2016 IBH SYSTEMS GmbH.
  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
   
  Contributors:
      IBH SYSTEMS GmbH - initial API and implementation
--%>

<%@ page
  language="java"
  contentType="text/html; charset=UTF-8"
  pageEncoding="UTF-8"
  trimDirectiveWhitespaces="true"
  %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<%@ taglib prefix="storage" uri="http://eclipse.org/packagedrone/repo/channel" %>
<%@ taglib prefix="form" uri="http://eclipse.org/packagedrone/web/form" %>
<%@ taglib prefix="web" uri="http://eclipse.org/packagedrone/web" %>

<%@ taglib prefix="h" tagdir="/WEB-INF/tags/main" %>
<%@ taglib prefix="s" tagdir="/WEB-INF/tags/storage" %>

<h:main title="Configure P2 Unzipper" subtitle="${storage:channel(channel) }">

<jsp:attribute name="subtitleHtml"><s:channelSubtitle channel="${channel }" /></jsp:attribute>

<jsp:body>

  <h:breadcrumbs/>

  <div class="container form-padding">

        <form:form action="" method="POST" cssClass="form-horizontal">
    	    
          <fieldset>
            <legend>Foo bar</legend>
                 
            <h:formCheckbox label="Extract P2 meta data" path="extractMetadata" command="command">
                <span class="help-block">
                This will also extract the P2 meta data files from the zipped P2 archive. The meta data will be re-used by the P2 repository aspect.
                </span>
                <span class="help-block">
                <strong>Note:</strong> The P2 metadata cannot be used to create an OBR/OSGi R5 repository index and there must be no
                other aspect generate P2 meta data in addition (like the P2 Metadata aspect).
                </span>
            </h:formCheckbox>
          </fieldset>
          
          <h:formButtons>
                <input type="submit" value="Update" class="btn btn-primary" />
                <input type="reset" value="Reset" class="btn btn-default" />
          </h:formButtons>
        </form:form>
          
  </div><%-- container --%>

</jsp:body>

</h:main>