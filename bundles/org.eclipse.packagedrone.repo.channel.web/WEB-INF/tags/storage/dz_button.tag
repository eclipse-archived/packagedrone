<%@ tag language="java" pageEncoding="UTF-8" body-content="empty"%>

<%@ taglib tagdir="/WEB-INF/tags/storage" prefix="s" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<div class="btn-group" role="group">
    <div class="btn">
        <span id="dropzone" title="Drop files here for uploading"><span class="glyphicon glyphicon-upload" aria-hidden="true"></span> Drop Artifacts</span>
    </div>
</div>

<div class="btn-group" role="group" id="upload-refresh" style="display: none;">
    <a class="btn btn-success" href="">Reload</a>
</div>