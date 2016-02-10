'use strict';

/*******************************************************************************
 * Copyright (c) 2016 IBH SYSTEMS GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBH SYSTEMS GmbH - initial API and implementation
 *******************************************************************************/

/* tags */

function droneTagDismissItem ( context ) {
	var item = context.closest ( ".drone-tag-list-item");
	if ( item ) {
		$(item).detach ();
	}
}

$( document ).ready(function() {
	$('[data-dismiss="drone-tag"]').click(function(){
		droneTagDismissItem ( this );
	});
});