package org.eclipse.packagedrone.repo.aspect.common.spool;

import java.io.IOException;
import java.io.OutputStream;

import org.eclipse.packagedrone.repo.utils.IOConsumer;

@FunctionalInterface
public interface SpoolOutTarget
{
    public void spoolOut ( final String fileName, final String mimeType, final IOConsumer<OutputStream> stream ) throws IOException;
}
