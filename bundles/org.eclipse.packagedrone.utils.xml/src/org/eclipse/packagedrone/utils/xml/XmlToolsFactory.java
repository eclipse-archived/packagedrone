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
package org.eclipse.packagedrone.utils.xml;

import java.util.function.Consumer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.xpath.XPathFactory;

/**
 * A factory of XML tools
 * <p>
 * This interfaces tries to the clean up the mess of OSGi and JAXP. JAXP highly
 * depends of the ServiceLoader framework of Java which performs badly in an
 * OSGi environment. For example might the framework locate a different provider
 * than it might be able to instantiate.
 * </p>
 * <p>
 * This interface is intended to clean up this mess by hiding all those details.
 * Each XML should register an instance of XmlToolsFactory as OSGi service and
 * each user should track services implementing XmlToolsFactory when needed.
 * </p>
 * <p>
 * In order to get an instanceof of XmlToolsFactory it is required to either use
 * a mechanism like OSGi DS or Blueprint. Or use a more direct approach using
 * {@link ServiceTracker}s or {@link BundleContext#getServiceReference(Class)}.
 * </p>
 */
public interface XmlToolsFactory
{

    /*
     * Core factory methods
     */

    public DocumentBuilderFactory newDocumentBuilderFactory ();

    public SAXParserFactory newParserFactory ();

    public TransformerFactory newTransformerFactory ();

    public XPathFactory newXPathFactory ();

    public XMLOutputFactory newXMLOutputFactory ();

    /*
     * Default helper methods
     */

    public default DocumentBuilder newDocumentBuilder () throws ParserConfigurationException
    {
        return newDocumentBuilder ( null );
    }

    public default DocumentBuilder newDocumentBuilder ( final Consumer<DocumentBuilderFactory> customizer ) throws ParserConfigurationException
    {
        final DocumentBuilderFactory dbf = newDocumentBuilderFactory ();

        if ( customizer != null )
        {
            customizer.accept ( dbf );
        }

        return dbf.newDocumentBuilder ();
    }

    public default Transformer newTransformer () throws TransformerConfigurationException
    {
        return newTransformer ( null );
    }

    public default Transformer newTransformer ( final Consumer<TransformerFactory> customizer ) throws TransformerConfigurationException
    {
        final TransformerFactory factory = newTransformerFactory ();

        if ( customizer != null )
        {
            customizer.accept ( factory );
        }

        return factory.newTransformer ();
    }

}
