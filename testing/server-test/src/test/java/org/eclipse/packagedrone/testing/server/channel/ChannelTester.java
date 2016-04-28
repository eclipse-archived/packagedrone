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
package org.eclipse.packagedrone.testing.server.channel;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.eclipse.packagedrone.testing.server.WebContext;
import org.junit.Assert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.google.common.base.Predicate;

public class ChannelTester
{
    private static final String ROW_ID_PREFIX = "row-";

    private final WebContext context;

    private final String id;

    public ChannelTester ( final WebContext context, final String id )
    {
        this.context = context;
        this.id = id;
    }

    /**
     * Get the channel id
     *
     * @return the channel id
     */
    public String getId ()
    {
        return this.id;
    }

    public static ChannelTester create ( final WebContext context, final String... names )
    {
        // get before list of channels

        final Set<String> before = getAllChannelIds ( context );

        // create channel

        context.getDriver ().get ( context.resolve ( "/channel/createWithRecipe" ) );
        final WebElement element = context.findElement ( By.id ( "names" ) );
        for ( final String name : names )
        {
            element.sendKeys ( name, "\n" );
        }
        element.submit ();

        // get after list of channels

        final Set<String> after = getAllChannelIds ( context );
        after.removeAll ( before );

        if ( after.isEmpty () )
        {
            throw new RuntimeException ( "Channel did not get created" );
        }

        if ( after.size () > 1 )
        {
            throw new RuntimeException ( "More than one channel was created when adding channel" );
        }

        // return new channel

        return new ChannelTester ( context, after.iterator ().next () );
    }

    public static Set<String> getAllChannelIds ( final WebContext context )
    {
        context.getDriver ().get ( context.resolve ( "/channel" ) );

        final Set<String> result = new HashSet<> ();

        for ( final WebElement ele : context.findElements ( By.cssSelector ( "#channels tr" ) ) )
        {
            if ( ele.getAttribute ( "data-channel-id" ) != null )
            {
                result.add ( ele.getAttribute ( "data-channel-id" ) );
            }
        }

        return result;
    }

    public Set<String> getAllArtifactIds ()
    {
        get ( String.format ( "/channel/%s/viewPlain", this.id ) );

        final Set<String> result = new HashSet<> ();

        for ( final WebElement ele : this.context.findElements ( By.tagName ( "tr" ) ) )
        {
            final String id = ele.getAttribute ( "id" );
            System.out.println ( "Entry: " + id );
            if ( id == null || !id.startsWith ( ROW_ID_PREFIX ) )
            {
                continue;
            }

            result.add ( id.substring ( ROW_ID_PREFIX.length () ) );
        }

        return result;
    }

    public void addAspect ( final String aspectId )
    {
        System.out.format ( "Adding aspect '%s' to channel '%s'%n", aspectId, this.id );

        get ( String.format ( "/channel/%s/aspects", this.id ) );

        // find the div panel

        final WebElement ele = this.context.findElement ( By.id ( aspectId ) );

        // ensure the group is selected

        final String groupDivId = ele.findElement ( By.xpath ( "parent::*" ) ).getAttribute ( "id" );
        System.out.println ( "Group ID: " + groupDivId );
        final String path = String.format ( "//a[@aria-controls='%s']", groupDivId );
        System.out.println ( "XPath: " + path );
        this.context.findElement ( By.xpath ( path ) ).click ();

        // get the add button

        final WebElement btn = ele.findElement ( By.className ( "btn-default" ) );

        // wait for the add button to be visible

        new WebDriverWait ( this.context.getDriver (), 5 ).pollingEvery ( 100, TimeUnit.MILLISECONDS ).until ( new Predicate<WebDriver> () {

            @Override
            public boolean apply ( final WebDriver input )
            {
                return btn.isDisplayed ();
            }
        } );

        // click the add button

        btn.click ();

        // maybe the add button did trigger the dependency dialog

        final WebElement mr = this.context.findElement ( By.id ( "modal-requires" ) );
        if ( mr.isDisplayed () )
        {
            mr.findElement ( By.id ( "modal-req-with" ) ).click ();
        }

        // wait for the click to be processed

        new WebDriverWait ( this.context.getDriver (), 5 ).pollingEvery ( 100, TimeUnit.MILLISECONDS ).until ( new Predicate<WebDriver> () {

            @Override
            public boolean apply ( final WebDriver input )
            {
                return internalGetAspects ( "aspect-assigned" ).contains ( aspectId );
            }
        } );

        // check if the aspect was really added

        /*
        final boolean added = internalGetAspects ( "aspect-assigned" ).contains ( aspectId );
        Assert.assertTrue ( String.format ( "Aspect '%s' was not added to the channel", aspectId ), added );
        */
    }

    public Set<String> getAssignedAspects ()
    {
        get ( String.format ( "/channel/%s/aspects", this.id ) );
        return internalGetAspects ( "aspect-assigned" );
    }

    protected Set<String> internalGetAspects ( final String className )
    {
        final List<WebElement> elements = this.context.findElements ( By.className ( className ) );

        final Set<String> result = new HashSet<> ( elements.size () );

        for ( final WebElement ele : elements )
        {
            final String id = ele.getAttribute ( "id" );
            if ( id != null )
            {
                result.add ( id );
            }
        }

        return result;
    }

    /**
     * Upload a file to the channel
     *
     * @param localFileName
     *            the file to upload
     * @return a set of artifact ids which got created by this operation. This
     *         list may be empty or contain one or more artifacts
     */
    public Set<String> upload ( final String localFileName )
    {
        final Set<String> before = getAllArtifactIds ();

        get ( String.format ( "/channel/%s/add", this.id ) );

        final WebElement link = this.context.findElement ( By.linkText ( "Upload" ) );

        Assert.assertTrue ( link.findElement ( By.xpath ( ".." ) ).getAttribute ( "class" ).contains ( "active" ) );;

        // upload file

        final File input = this.context.getTestFile ( localFileName );

        {
            final WebElement ele = this.context.findElement ( By.id ( "file" ) );
            Assert.assertNotNull ( ele );

            ele.sendKeys ( input.toString () );
            ele.submit ();
        }

        final Set<String> after = getAllArtifactIds ();

        after.removeAll ( before );

        return after;
    }

    protected void get ( final String url )
    {
        System.out.println ( "Getting: " + url );
        this.context.getResolved ( url );
    }

    public void assignDeployGroup ( final String deployGroupName )
    {
        get ( String.format ( "/channel/%s/deployKeys", this.id ) );
        final WebElement ele = this.context.findElement ( By.id ( "groupId" ) );
        new Select ( ele ).selectByVisibleText ( deployGroupName );
        ele.submit ();
    }

    public Set<String> getDeployKeys ()
    {
        get ( String.format ( "/channel/%s/deployKeys", this.id ) );

        final Set<String> result = new HashSet<> ();

        final List<WebElement> eles = this.context.findElements ( By.xpath ( "//code[@title='password']" ) );
        for ( final WebElement ele : eles )
        {
            final String key = ele.getText ();
            System.out.println ( "Deploy key: " + key );
            result.add ( key );
        }

        return result;
    }
}
