package org.eclipse.packagedrone.repo.adapter.p2.internal.aspect;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.eclipse.packagedrone.repo.utils.IOConsumer;

public class FileSpoolOutHandler implements SpoolOutHandler
{
    private final File base;

    public FileSpoolOutHandler ( final File base )
    {
        this.base = base;
    }

    @Override
    public void spoolOut ( final String id, final String name, final String mimeType, final IOConsumer<OutputStream> stream ) throws IOException
    {
        final File file = new File ( this.base, name );
        file.getParentFile ().mkdirs ();

        try ( OutputStream out = new BufferedOutputStream ( new FileOutputStream ( file ) ) )
        {
            stream.accept ( null );
        }
    }

}
