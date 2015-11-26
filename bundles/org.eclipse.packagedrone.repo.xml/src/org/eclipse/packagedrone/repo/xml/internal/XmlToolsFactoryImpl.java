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
package org.eclipse.packagedrone.repo.xml.internal;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.transform.TransformerFactory;
import javax.xml.xpath.XPathFactory;

import org.eclipse.packagedrone.utils.xml.XmlToolsFactory;

/**
 * Implements XmlToolsFactory
 * <p>
 * Now this class tries to clean up the mess OSGi and JAXP produce in
 * combination. For XPathFactory and TransformerFactory the implementation
 * directly uses Xalan, since it can be added and already is added.
 * </p>
 * <p>
 * For DocumentBuilderFactory and SAXParserFactory is uses the direct JAXP
 * approach, since there should be no other implementations active at the
 * moment.
 * </p>
 */
public class XmlToolsFactoryImpl implements XmlToolsFactory
{
    public XmlToolsFactoryImpl ()
    {
    }

    @Override
    public DocumentBuilderFactory newDocumentBuilderFactory ()
    {
        return DocumentBuilderFactory.newInstance ();
    }

    @Override
    public TransformerFactory newTransformerFactory ()
    {
        return new org.apache.xalan.processor.TransformerFactoryImpl ();
    }

    @Override
    public XPathFactory newXPathFactory ()
    {
        return new org.apache.xpath.jaxp.XPathFactoryImpl ();
    }

    @Override
    public SAXParserFactory newParserFactory ()
    {
        return SAXParserFactory.newInstance ();
    }

    @Override
    public XMLInputFactory newXMLInputFactory ()
    {
        return XMLInputFactory.newFactory ();
    }

    @Override
    public XMLOutputFactory newXMLOutputFactory ()
    {
        return XMLOutputFactory.newFactory ();
    }
}
