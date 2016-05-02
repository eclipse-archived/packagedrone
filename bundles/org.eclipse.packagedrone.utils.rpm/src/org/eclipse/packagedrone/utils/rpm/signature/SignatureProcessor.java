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
package org.eclipse.packagedrone.utils.rpm.signature;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.eclipse.packagedrone.utils.rpm.RpmSignatureTag;
import org.eclipse.packagedrone.utils.rpm.build.PayloadProvider;
import org.eclipse.packagedrone.utils.rpm.header.Header;

public interface SignatureProcessor
{
    public void sign ( ByteBuffer header, PayloadProvider payloadProvider, Header<RpmSignatureTag> signature ) throws IOException;
}
