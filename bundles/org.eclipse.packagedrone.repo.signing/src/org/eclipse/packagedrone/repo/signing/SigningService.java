/*******************************************************************************
 * Copyright (c) 2015 IBH SYSTEMS GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBH SYSTEMS GmbH - initial API and implementation
 *     Walker Funk - Trident Systems Inc. - rpm signing components
 *******************************************************************************/
package org.eclipse.packagedrone.repo.signing;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import org.eclipse.packagedrone.utils.rpm.parse.RpmParserStream;

public interface SigningService
{
    public void sign ( InputStream in, OutputStream out, boolean inline ) throws Exception;

    public void signRpm ( Path path, RpmParserStream in ) throws Exception;

    public void printPublicKey ( OutputStream out ) throws IOException;

    public OutputStream signingStream ( OutputStream out, boolean inline );
}
