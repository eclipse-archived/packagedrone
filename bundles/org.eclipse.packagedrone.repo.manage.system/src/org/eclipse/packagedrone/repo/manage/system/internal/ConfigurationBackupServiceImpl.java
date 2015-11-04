/*******************************************************************************
 * Copyright (c) 2015 IBH SYSTEMS GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBH SYSTEMS GmbH - initial API and implementation
 *******************************************************************************/
package org.eclipse.packagedrone.repo.manage.system.internal;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.eclipse.packagedrone.repo.XmlHelper;
import org.eclipse.packagedrone.repo.manage.system.ConfigurationBackupService;
import org.eclipse.packagedrone.utils.converter.ConverterManager;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class ConfigurationBackupServiceImpl implements ConfigurationBackupService
{
    private final static Logger logger = LoggerFactory.getLogger ( ConfigurationBackupServiceImpl.class );

    private static final String BACKUP_VERSION = "1";

    private static final String CFG_FILE_NAME = "configurations.xml";

    private ConfigurationAdmin configAdmin;

    private final Bundle bundle = FrameworkUtil.getBundle ( ConfigurationBackupServiceImpl.class );

    private final ConverterManager cvt = new ConverterManager ();

    private final Set<String> ignoredFactories = new HashSet<> ( Arrays.asList ( "gemini.jpa.punit" ) );

    public void setConfigAdmin ( final ConfigurationAdmin configAdmin )
    {
        this.configAdmin = configAdmin;
    }

    @Override
    public void createConfigurationBackup ( final OutputStream stream )
    {
        try
        {
            processBackup ( stream );
        }
        catch ( final Exception e )
        {
            throw new RuntimeException ( "Failed to export backup", e );
        }
    }

    private void processBackup ( final OutputStream stream ) throws Exception
    {
        final ZipOutputStream zos = new ZipOutputStream ( stream );

        storeConfigurations ( zos, getAllConfigurations () );

        zos.close ();
    }

    protected List<Configuration> getAllConfigurations () throws IOException, InvalidSyntaxException
    {
        final Configuration[] result = this.configAdmin.listConfigurations ( null );

        if ( result == null )
        {
            return Collections.emptyList ();
        }

        final List<Configuration> list = new LinkedList<> ( Arrays.asList ( result ) );

        // remove ignored factories

        final Iterator<Configuration> i = list.iterator ();
        while ( i.hasNext () )
        {
            final Configuration cfg = i.next ();
            if ( this.ignoredFactories.contains ( cfg.getFactoryPid () ) )
            {
                i.remove ();
            }
        }

        return list;
    }

    private void storeConfigurations ( final ZipOutputStream zos, final List<Configuration> cfgs ) throws Exception
    {
        zos.putNextEntry ( new ZipEntry ( CFG_FILE_NAME ) );

        final XmlHelper xml = new XmlHelper ();

        final Document doc = xml.create ();
        final Element root = doc.createElement ( "configuration" );
        doc.appendChild ( root );
        root.setAttribute ( "version", BACKUP_VERSION );

        for ( final Configuration cfg : cfgs )
        {
            final Element entry = XmlHelper.addElement ( root, "entry" );

            if ( cfg.getFactoryPid () != null )
            {
                entry.setAttribute ( "factoryPid", cfg.getFactoryPid () );
            }
            else if ( cfg.getPid () != null )
            {
                entry.setAttribute ( "pid", cfg.getPid () );
            }

            for ( final String key : Collections.list ( cfg.getProperties ().keys () ) )
            {
                final Object value = cfg.getProperties ().get ( key );
                final Element prop = XmlHelper.addElement ( entry, "property" );
                prop.setAttribute ( "key", key );
                if ( value != null )
                {
                    prop.setAttribute ( "type", value.getClass ().getName () );
                    prop.setTextContent ( value.toString () );
                }
            }
        }

        xml.write ( doc, zos );
        zos.closeEntry ();
    }

    @Override
    public void restoreConfiguration ( final InputStream stream ) throws Exception
    {
        final ZipInputStream zis = new ZipInputStream ( stream );

        Configurations cfg = null;
        ZipEntry entry;
        while ( ( entry = zis.getNextEntry () ) != null )
        {
            if ( CFG_FILE_NAME.equals ( entry.getName () ) )
            {
                cfg = parse ( zis );
                break;
            }
        }

        if ( cfg == null )
        {
            throw new IllegalStateException ( String.format ( "Unable to find '%s' in the configuration archive", CFG_FILE_NAME ) );
        }

        apply ( cfg );
    }

    @Override
    public void provisionConfiguration ( final InputStream stream ) throws Exception
    {
        apply ( parse ( stream ) );
    }

    private Configurations parse ( final InputStream stream ) throws Exception
    {
        final XmlHelper xml = new XmlHelper ();
        final Document doc = xml.parse ( stream );

        final Element root = doc.getDocumentElement ();

        if ( !BACKUP_VERSION.equals ( root.getAttribute ( "version" ) ) )
        {
            throw new IllegalArgumentException ( String.format ( "Backup version %s is unsupported", root.getAttribute ( "version" ) ) );
        }

        final Configurations cfg = new Configurations ();

        for ( final Element entry : XmlHelper.iterElement ( root, "entry" ) )
        {
            final Entry cfgEntry = new Entry ();
            if ( !entry.getAttribute ( "factoryPid" ).isEmpty () )
            {
                final String factoryPid = entry.getAttribute ( "factoryPid" );
                List<Entry> list = cfg.factoryEntries.get ( factoryPid );
                if ( list == null )
                {
                    list = new LinkedList<> ();
                    cfg.factoryEntries.put ( factoryPid, list );
                }
                list.add ( cfgEntry );
            }
            else if ( !entry.getAttribute ( "pid" ).isEmpty () )
            {
                final String pid = entry.getAttribute ( "pid" );
                cfg.directEntries.put ( pid, cfgEntry );
            }
            else
            {
                // just ignore
                continue;
            }

            for ( final Element p : XmlHelper.iterElement ( entry, "property" ) )
            {
                final String key = p.getAttribute ( "key" );
                final String type = p.getAttribute ( "type" );
                final String value = p.getTextContent ();

                cfgEntry.data.put ( key, makeValue ( type, value ) );
            }
        }

        return cfg;
    }

    private Object makeValue ( final String type, final String value ) throws Exception
    {
        final Class<?> clazz = this.bundle.loadClass ( type );
        return this.cvt.convertTo ( value, clazz );
    }

    private void apply ( final Configurations cfg ) throws Exception
    {
        logger.debug ( "Apply configuration" );

        final List<Configuration> cfgs = getAllConfigurations ();
        for ( final Configuration cc : cfgs )
        {
            if ( cc.getFactoryPid () == null )
            {
                // direct entry

                final Entry ne = cfg.directEntries.remove ( cc.getPid () );
                if ( ne == null )
                {
                    logger.debug ( "Delete direct: {}", cc.getPid () );
                    cc.delete ();
                }
                else
                {
                    logger.debug ( "Update direct: {} = {}", cc.getPid (), ne.data );
                    cc.update ( ne.data );
                }
            }
            else
            {
                // delete all factory elements right now
                logger.debug ( "Update factory: {} / {}", cc.getFactoryPid (), cc.getPid () );
                cc.delete ();
            }
        }

        // process direct additions

        for ( final Map.Entry<String, Entry> entry : cfg.directEntries.entrySet () )
        {
            logger.debug ( "Add direct: {} = {}", entry.getKey (), entry.getValue ().data );
            final Configuration conf = this.configAdmin.getConfiguration ( entry.getKey (), null );
            conf.update ( entry.getValue ().data );
        }

        // process factories

        for ( final Map.Entry<String, List<Entry>> entry : cfg.factoryEntries.entrySet () )
        {
            for ( final Entry ce : entry.getValue () )
            {
                logger.debug ( "Add factory: {} = {}", entry.getKey (), ce.data );
                final Configuration conf = this.configAdmin.createFactoryConfiguration ( entry.getKey (), null );
                conf.update ( ce.data );
            }
        }
    }

    private static class Configurations
    {
        private final Map<String, Entry> directEntries = new HashMap<> ();

        private final Map<String, List<Entry>> factoryEntries = new HashMap<> ();
    }

    private static class Entry
    {
        private final Hashtable<String, Object> data = new Hashtable<> ();
    }
}
