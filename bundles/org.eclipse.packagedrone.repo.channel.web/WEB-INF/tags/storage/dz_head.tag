<%@ tag language="java" pageEncoding="UTF-8" body-content="empty"%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<script src="<c:url value="/resources/js/dropzone.min.js"/>"></script>

<style type="text/css">
.dz-progress {
    width: 100%;
    height: 18px;
    background: #FAFAFA;
}
.dz-upload {
    height: 18px;
    background: #EEEEEE;
}
.dz-filename {
    display: inline;
}
.dz-size {
    display: inline;
}

div.dz-error-message {
    display: none;
}

.dz-error div.dz-error-message {
    display: block;
}

.dz-error-message {
    padding: 1em;
}

#upload {
    margin-bottom: 1em;
}

#dropzone {
    border: 2pt dashed #BBB;
    border-radius: 3pt;
    
    background: #FAFAFA;
    
    padding: 6px 12px;
    
    text-align: center;
        
    cursor: crosshair;
    
     -webkit-touch-callout: none;
    -webkit-user-select: none;
    -khtml-user-select: none;
    -moz-user-select: none;
    -ms-user-select: none;
    user-select: none;
    
    color: rgba(0, 0, 0, 0.80);
}

#dropzone:HOVER, .dz-drag-hover {
    border-color: #888;
    background-color: #EEE;
}

</style>