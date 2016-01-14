/*******************************************************************************
 * Copyright (c) 2014, 2016 IBH SYSTEMS GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBH SYSTEMS GmbH - initial API and implementation
 *******************************************************************************/
package org.eclipse.packagedrone.repo.channel.web.channel;

import static com.google.common.net.UrlEscapers.urlPathSegmentEscaper;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.Optional.ofNullable;
import static javax.servlet.annotation.ServletSecurity.EmptyRoleSemantic.PERMIT;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import javax.servlet.ServletException;
import javax.servlet.annotation.HttpConstraint;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import javax.validation.Valid;
import javax.xml.ws.Holder;

import org.apache.http.client.utils.URIBuilder;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.packagedrone.repo.ChannelAspectInformation;
import org.eclipse.packagedrone.repo.MetaKey;
import org.eclipse.packagedrone.repo.aspect.ChannelAspectProcessor;
import org.eclipse.packagedrone.repo.aspect.group.GroupInformation;
import org.eclipse.packagedrone.repo.aspect.recipe.RecipeInformation;
import org.eclipse.packagedrone.repo.aspect.recipe.RecipeNotFoundException;
import org.eclipse.packagedrone.repo.channel.ArtifactInformation;
import org.eclipse.packagedrone.repo.channel.AspectableChannel;
import org.eclipse.packagedrone.repo.channel.ChannelArtifactInformation;
import org.eclipse.packagedrone.repo.channel.ChannelDetails;
import org.eclipse.packagedrone.repo.channel.ChannelId;
import org.eclipse.packagedrone.repo.channel.ChannelInformation;
import org.eclipse.packagedrone.repo.channel.ChannelNotFoundException;
import org.eclipse.packagedrone.repo.channel.ChannelService;
import org.eclipse.packagedrone.repo.channel.ChannelService.By;
import org.eclipse.packagedrone.repo.channel.ChannelService.ChannelOperation;
import org.eclipse.packagedrone.repo.channel.DeployKeysChannelAdapter;
import org.eclipse.packagedrone.repo.channel.DescriptorAdapter;
import org.eclipse.packagedrone.repo.channel.ModifiableChannel;
import org.eclipse.packagedrone.repo.channel.ReadableChannel;
import org.eclipse.packagedrone.repo.channel.deploy.DeployAuthService;
import org.eclipse.packagedrone.repo.channel.deploy.DeployGroup;
import org.eclipse.packagedrone.repo.channel.deploy.DeployKey;
import org.eclipse.packagedrone.repo.channel.util.DownloadHelper;
import org.eclipse.packagedrone.repo.channel.web.Tags;
import org.eclipse.packagedrone.repo.channel.web.breadcrumbs.Breadcrumbs;
import org.eclipse.packagedrone.repo.channel.web.breadcrumbs.Breadcrumbs.Entry;
import org.eclipse.packagedrone.repo.channel.web.internal.Activator;
import org.eclipse.packagedrone.repo.generator.GeneratorProcessor;
import org.eclipse.packagedrone.repo.manage.system.SitePrefixService;
import org.eclipse.packagedrone.repo.web.sitemap.ChangeFrequency;
import org.eclipse.packagedrone.repo.web.sitemap.SitemapExtender;
import org.eclipse.packagedrone.repo.web.sitemap.UrlSetContext;
import org.eclipse.packagedrone.repo.web.utils.Channels;
import org.eclipse.packagedrone.sec.web.controller.HttpContraintControllerInterceptor;
import org.eclipse.packagedrone.sec.web.controller.Secured;
import org.eclipse.packagedrone.sec.web.controller.SecuredControllerInterceptor;
import org.eclipse.packagedrone.web.Controller;
import org.eclipse.packagedrone.web.LinkTarget;
import org.eclipse.packagedrone.web.ModelAndView;
import org.eclipse.packagedrone.web.RequestMapping;
import org.eclipse.packagedrone.web.RequestMethod;
import org.eclipse.packagedrone.web.ViewResolver;
import org.eclipse.packagedrone.web.common.CommonController;
import org.eclipse.packagedrone.web.common.InterfaceExtender;
import org.eclipse.packagedrone.web.common.Modifier;
import org.eclipse.packagedrone.web.common.menu.MenuEntry;
import org.eclipse.packagedrone.web.common.page.Pagination;
import org.eclipse.packagedrone.web.controller.ControllerInterceptor;
import org.eclipse.packagedrone.web.controller.ProfilerControllerInterceptor;
import org.eclipse.packagedrone.web.controller.binding.BindingResult;
import org.eclipse.packagedrone.web.controller.binding.PathVariable;
import org.eclipse.packagedrone.web.controller.binding.RequestParameter;
import org.eclipse.packagedrone.web.controller.form.FormData;
import org.eclipse.packagedrone.web.controller.validator.ControllerValidator;
import org.eclipse.packagedrone.web.controller.validator.ValidationContext;
import org.eclipse.scada.utils.ExceptionHelper;
import org.osgi.framework.FrameworkUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.ByteStreams;
import com.google.common.net.UrlEscapers;
import com.google.gson.GsonBuilder;

@Secured
@Controller
@ViewResolver ( "/WEB-INF/views/%s.jsp" )
@ControllerInterceptor ( SecuredControllerInterceptor.class )
@HttpConstraint ( rolesAllowed = "MANAGER" )
@ControllerInterceptor ( HttpContraintControllerInterceptor.class )
@ControllerInterceptor ( ProfilerControllerInterceptor.class )
public class ChannelController implements InterfaceExtender, SitemapExtender
{

    private static final int DEFAULT_MAX_WEB_SIZE = 10_000;

    public static final String DRONE_WEB_MAX_LIST_SIZE = "drone.web.maxListSize";

    private static final String DEFAULT_EXAMPLE_KEY = "xxxxx";

    private final static Logger logger = LoggerFactory.getLogger ( ChannelController.class );

    private final static List<MenuEntry> menuEntries = Collections.singletonList ( new MenuEntry ( "Channels", 100, new LinkTarget ( "/channel" ), Modifier.DEFAULT, null ) );

    private DeployAuthService deployAuthService;

    private SitePrefixService sitePrefix;

    private ChannelService channelService;

    private final GeneratorProcessor generators = new GeneratorProcessor ( FrameworkUtil.getBundle ( ChannelController.class ).getBundleContext () );

    public void setChannelService ( final ChannelService channelService )
    {
        this.channelService = channelService;
    }

    public void setDeployAuthService ( final DeployAuthService deployAuthService )
    {
        this.deployAuthService = deployAuthService;
    }

    public void setSitePrefixService ( final SitePrefixService sitePrefix )
    {
        this.sitePrefix = sitePrefix;
    }

    public void start ()
    {
        this.generators.open ();
    }

    public void stop ()
    {
        this.generators.close ();
    }

    @Override
    public List<MenuEntry> getMainMenuEntries ( final HttpServletRequest request )
    {
        return menuEntries;
    }

    @Secured ( false )
    @RequestMapping ( value = "/channel", method = RequestMethod.GET )
    @HttpConstraint ( PERMIT )
    public ModelAndView list ( @RequestParameter ( value = "start", required = false ) final Integer startPage)
    {
        final ModelAndView result = new ModelAndView ( "channel/list" );

        final List<ChannelInformation> channels = new ArrayList<> ( this.channelService.list () );
        channels.sort ( Comparator.comparing ( ChannelInformation::getId ) );

        result.put ( "channels", Pagination.paginate ( startPage, 10, channels ) );

        return result;
    }

    @RequestMapping ( value = "/channel/create", method = RequestMethod.GET )
    public ModelAndView create ()
    {
        this.channelService.create ( "apm", new ChannelDetails (), Collections.emptyMap () );

        return new ModelAndView ( "redirect:/channel" );
    }

    @RequestMapping ( value = "/channel/createDetailed", method = RequestMethod.GET )
    public ModelAndView createDetailed ()
    {
        final Map<String, Object> model = new HashMap<> ( 1 );
        model.put ( "command", new CreateChannel () );
        return new ModelAndView ( "channel/create", model );
    }

    @RequestMapping ( value = "/channel/createDetailed", method = RequestMethod.POST )
    public ModelAndView createDetailedPost ( @Valid @FormData ( "command" ) final CreateChannel data, final BindingResult result)
    {
        if ( !result.hasErrors () )
        {
            final ChannelDetails desc = new ChannelDetails ();
            desc.setDescription ( data.getDescription () );

            final ChannelId channel = this.channelService.create ( "apm", desc, Collections.emptyMap () );
            setChannelNames ( channel, splitChannelNames ( data.getNames () ) );

            return new ModelAndView ( String.format ( "redirect:/channel/%s/view", urlPathSegmentEscaper ().escape ( channel.getId () ) ) );
        }

        return new ModelAndView ( "channel/create" );
    }

    @RequestMapping ( value = "/channel/createWithRecipe", method = RequestMethod.GET )
    public ModelAndView createWithRecipe ()
    {
        final Map<String, Object> model = new HashMap<> ( 2 );

        model.put ( "command", new CreateChannel () );
        model.put ( "recipes", Activator.getRecipes ().getSortedRecipes ( RecipeInformation::getLabel ) );

        return new ModelAndView ( "channel/createWithRecipe", model );
    }

    private static Set<String> splitChannelNames ( final String names )
    {
        final Set<String> result = new HashSet<> ();
        for ( String name : names.split ( "[\\n\\r]+" ) )
        {
            name = name.trim ();
            if ( !name.isEmpty () )
            {
                result.add ( name );
            }
        }
        return result;
    }

    private static String joinChannelNames ( final Collection<String> names )
    {
        return names.stream ().collect ( Collectors.joining ( "\n" ) );
    }

    @RequestMapping ( value = "/channel/createWithRecipe", method = RequestMethod.POST )
    public ModelAndView createWithRecipePost ( @Valid @FormData ( "command" ) final CreateChannel data, @RequestParameter (
            required = false,
            value = "recipe" ) final String recipeId, final BindingResult result) throws UnsupportedEncodingException, RecipeNotFoundException
    {
        if ( !result.hasErrors () )
        {

            final Holder<ChannelId> holder = new Holder<> ();
            final Holder<String> targetHolder = new Holder<> ();

            if ( recipeId == null || recipeId.isEmpty () )
            {
                // without recipe
                final ChannelDetails desc = new ChannelDetails ();
                desc.setDescription ( data.getDescription () );

                holder.value = this.channelService.create ( "apm", desc, Collections.emptyMap () );
                setChannelNames ( holder.value, splitChannelNames ( data.getNames () ) );
            }
            else
            {
                // with recipe
                Activator.getRecipes ().process ( recipeId, recipe -> {
                    final ChannelDetails desc = new ChannelDetails ();
                    desc.setDescription ( data.getDescription () );

                    final ChannelId channel = this.channelService.create ( "apm", desc, Collections.emptyMap () );

                    setChannelNames ( channel, splitChannelNames ( data.getNames () ) );

                    this.channelService.accessRun ( By.id ( channel.getId () ), AspectableChannel.class, aspChannel -> {

                        final LinkTarget target = recipe.setup ( channel.getId (), aspChannel );

                        if ( target != null )
                        {
                            final Map<String, String> model = new HashMap<> ( 1 );
                            model.put ( "channelId", channel.getId () );
                            targetHolder.value = target.expand ( model ).getUrl ();
                        }
                    } );

                    holder.value = channel;
                } );

                if ( targetHolder.value != null )
                {
                    return new ModelAndView ( "redirect:" + targetHolder.value );
                }
            }

            return new ModelAndView ( String.format ( "redirect:/channel/%s/view", URLEncoder.encode ( holder.value.getId (), "UTF-8" ) ) );
        }

        final Map<String, Object> model = new HashMap<> ( 1 );
        model.put ( "recipes", Activator.getRecipes ().getSortedRecipes ( RecipeInformation::getLabel ) );

        return new ModelAndView ( "channel/createWithRecipe", model );
    }

    protected void setChannelNames ( final ChannelId id, final Collection<String> name )
    {
        this.channelService.accessRun ( By.id ( id.getId () ), DescriptorAdapter.class, channel -> {
            channel.setNames ( name );
        } );
    }

    @Secured ( false )
    @RequestMapping ( value = "/channel/{channelId}/view", method = RequestMethod.GET )
    @HttpConstraint ( PERMIT )
    public ModelAndView view ( @PathVariable ( "channelId" ) final String channelId, final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException
    {
        final Optional<ChannelInformation> channel = this.channelService.getState ( By.name ( channelId ) );
        if ( channel.isPresent () )
        {
            return new ModelAndView ( String.format ( "redirect:/channel/%s/view", channel.get ().getId () ) );
        }
        else
        {
            request.getRequestDispatcher ( "tree" ).forward ( request, response );
            return null;
        }
    }

    @Secured ( false )
    @RequestMapping ( value = "/channel/{channelId}/viewPlain", method = RequestMethod.GET )
    @HttpConstraint ( PERMIT )
    public ModelAndView viewPlain ( @PathVariable ( "channelId" ) final String channelId)
    {
        try
        {
            return this.channelService.accessCall ( By.id ( channelId ), ReadableChannel.class, ( channel ) -> {

                final Map<String, Object> model = new HashMap<> ();

                model.put ( "channel", channel.getInformation () );

                final Collection<ArtifactInformation> artifacts = channel.getContext ().getArtifacts ().values ();

                if ( artifacts.size () > maxWebListSize () )
                {
                    return viewTooMany ( channel );
                }

                // sort artifacts

                final List<ArtifactInformation> sortedArtifacts = new ArrayList<> ( artifacts );
                sortedArtifacts.sort ( Comparator.comparing ( ArtifactInformation::getName ) );
                model.put ( "sortedArtifacts", sortedArtifacts );

                return new ModelAndView ( "channel/view", model );
            } );
        }
        catch ( final ChannelNotFoundException e )
        {
            return CommonController.createNotFound ( "channel", channelId );
        }
    }

    private ModelAndView viewTooMany ( final ReadableChannel channel )
    {
        final Map<String, Object> model = new HashMap<> ();
        model.put ( "channel", channel.getInformation () );
        model.put ( "numberOfArtifacts", channel.getArtifacts ().size () );
        model.put ( "maxNumberOfArtifacts", maxWebListSize () );
        model.put ( "propertyName", DRONE_WEB_MAX_LIST_SIZE );
        return new ModelAndView ( "channel/viewTooMany", model );
    }

    @Secured ( false )
    @RequestMapping ( value = "/channel/{channelId}/tree", method = RequestMethod.GET )
    @HttpConstraint ( PERMIT )
    public ModelAndView tree ( @PathVariable ( "channelId" ) final String channelId)
    {
        try
        {
            return this.channelService.accessCall ( By.id ( channelId ), ReadableChannel.class, ( channel ) -> {

                if ( channel.getContext ().getArtifacts ().size () > maxWebListSize () )
                {
                    return viewTooMany ( channel );
                }

                final ModelAndView result = new ModelAndView ( "channel/tree" );

                final Map<String, List<ArtifactInformation>> tree = new HashMap<> ();

                for ( final ArtifactInformation entry : channel.getContext ().getArtifacts ().values () )
                {
                    List<ArtifactInformation> list = tree.get ( entry.getParentId () );
                    if ( list == null )
                    {
                        list = new LinkedList<> ();
                        tree.put ( entry.getParentId (), list );
                    }
                    list.add ( entry );
                }

                result.put ( "channel", channel.getInformation () );
                result.put ( "treeArtifacts", tree );
                result.put ( "treeSeverityTester", new TreeTesterImpl ( tree ) );

                return result;
            } );
        }
        catch ( final ChannelNotFoundException e )
        {
            return CommonController.createNotFound ( "channel", channelId );
        }
    }

    private Integer maxWebListSize ()
    {
        return Integer.getInteger ( DRONE_WEB_MAX_LIST_SIZE, DEFAULT_MAX_WEB_SIZE );
    }

    @Secured ( false )
    @RequestMapping ( value = "/channel/{channelId}/validation", method = RequestMethod.GET )
    @HttpConstraint ( PERMIT )
    public ModelAndView viewValidation ( @PathVariable ( "channelId" ) final String channelId)
    {
        try
        {
            return this.channelService.accessCall ( By.id ( channelId ), ReadableChannel.class, channel -> {
                final ModelAndView result = new ModelAndView ( "channel/validation" );

                result.put ( "channel", channel.getInformation () );
                result.put ( "messages", channel.getInformation ().getState ().getValidationMessages () );
                result.put ( "aspects", Activator.getAspects ().getAspectInformations () );

                return result;
            } );
        }
        catch ( final ChannelNotFoundException e )
        {
            return CommonController.createNotFound ( "channel", channelId );
        }
    }

    @Secured ( false )
    @RequestMapping ( value = "/channel/{channelId}/details", method = RequestMethod.GET )
    @HttpConstraint ( PERMIT )
    public ModelAndView details ( @PathVariable ( "channelId" ) final String channelId)
    {
        final ModelAndView result = new ModelAndView ( "channel/details" );

        try
        {
            this.channelService.accessRun ( By.id ( channelId ), ReadableChannel.class, ( channel ) -> {
                result.put ( "channel", channel.getInformation () );
            } );
        }
        catch ( final ChannelNotFoundException e )
        {
            return CommonController.createNotFound ( "channel", channelId );
        }

        return result;
    }

    @RequestMapping ( value = "/channel/{channelId}/delete", method = RequestMethod.GET )
    public ModelAndView delete ( @PathVariable ( "channelId" ) final String channelId)
    {
        final ModelAndView result = new ModelAndView ( "redirect:/channel" );

        if ( this.channelService.delete ( By.id ( channelId ) ) )
        {
            result.put ( "success", String.format ( "Deleted channel %s", channelId ) );
        }
        else
        {
            result.put ( "warning", String.format ( "Unable to delete channel %s. Was not found.", channelId ) );
        }

        return result;
    }

    @RequestMapping ( value = "/channel/{channelId}/artifacts/{artifactId}/delete", method = RequestMethod.GET )
    public ModelAndView deleteArtifact ( @PathVariable ( "channelId" ) final String channelId, @PathVariable ( "artifactId" ) final String artifactId)
    {
        return withChannel ( channelId, ModifiableChannel.class, channel -> {
            channel.getContext ().deleteArtifact ( artifactId );
            return redirectDefaultView ( channelId, true );
        } );
    }

    @RequestMapping ( value = "/channel/{channelId}/artifacts/{artifactId}/get", method = RequestMethod.GET )
    public void getArtifact ( @PathVariable ( "channelId" ) final String channelId, @PathVariable ( "artifactId" ) final String artifactId, final HttpServletResponse response) throws IOException
    {
        DownloadHelper.streamArtifact ( response, this.channelService, channelId, artifactId, null, true );
    }

    @RequestMapping ( value = "/channel/{channelId}/artifacts/{artifactId}/dump", method = RequestMethod.GET )
    public void dumpArtifact ( @PathVariable ( "channelId" ) final String channelId, @PathVariable ( "artifactId" ) final String artifactId, final HttpServletResponse response) throws IOException
    {
        DownloadHelper.streamArtifact ( response, this.channelService, channelId, artifactId, null, false );
    }

    @RequestMapping ( value = "/channel/{channelId}/artifacts/{artifactId}/view", method = RequestMethod.GET )
    public ModelAndView viewArtifact ( @PathVariable ( "channelId" ) final String channelId, @PathVariable ( "artifactId" ) final String artifactId)
    {
        return withChannel ( channelId, ReadableChannel.class, channel -> {

            final Optional<ChannelArtifactInformation> artifact = channel.getArtifact ( artifactId );
            if ( !artifact.isPresent () )
            {
                return CommonController.createNotFound ( "artifact", artifactId );
            }

            final Map<String, Object> model = new HashMap<String, Object> ( 1 );
            model.put ( "artifact", artifact.get () );
            model.put ( "sortedMetaData", new TreeMap<> ( artifact.get ().getMetaData () ) );

            return new ModelAndView ( "artifact/view", model );
        } );
    }

    @RequestMapping ( value = "/channel/{channelId}/add", method = RequestMethod.GET )
    public ModelAndView add ( @PathVariable ( "channelId" ) final String channelId)
    {
        final ModelAndView mav = new ModelAndView ( "/channel/add" );

        mav.put ( "generators", this.generators.getInformations ().values () );
        mav.put ( "channelId", channelId );

        return mav;
    }

    @RequestMapping ( value = "/channel/{channelId}/add", method = RequestMethod.POST )
    public ModelAndView addPost ( @PathVariable ( "channelId" ) final String channelId, @RequestParameter (
            required = false, value = "name" ) String name, final @RequestParameter ( "file" ) Part file)
    {
        try
        {
            if ( name == null || name.isEmpty () )
            {
                name = file.getSubmittedFileName ();
            }

            final String finalName = name;

            this.channelService.accessRun ( By.id ( channelId ), ModifiableChannel.class, channel -> {
                channel.getContext ().createArtifact ( file.getInputStream (), finalName, null );
            } );

            return redirectDefaultView ( channelId, true );
        }
        catch ( final Exception e )
        {
            return CommonController.createError ( "Upload", "Upload failed", e );
        }
    }

    @RequestMapping ( value = "/channel/{channelId}/drop", method = RequestMethod.POST )
    public void drop ( @PathVariable ( "channelId" ) final String channelId, @RequestParameter ( required = false,
            value = "name" ) String name, final @RequestParameter ( "file" ) Part file, final HttpServletResponse response) throws IOException
    {
        response.setContentType ( "text/plain" );

        try
        {
            if ( name == null || name.isEmpty () )
            {
                name = file.getSubmittedFileName ();
            }

            final String finalName = name;

            this.channelService.accessRun ( By.id ( channelId ), ModifiableChannel.class, channel -> {
                channel.getContext ().createArtifact ( file.getInputStream (), finalName, null );
            } );
        }
        catch ( final Throwable e )
        {
            logger.debug ( "Failed to drop file", e );
            response.setStatus ( HttpServletResponse.SC_INTERNAL_SERVER_ERROR );
            response.getWriter ().write ( "Internal error: " + ExceptionHelper.getMessage ( e ) );
            return;
        }

        response.setStatus ( HttpServletResponse.SC_OK );
        response.getWriter ().write ( "OK" );
    }

    @RequestMapping ( value = "/channel/{channelId}/clear", method = RequestMethod.GET )
    public ModelAndView clear ( @PathVariable ( "channelId" ) final String channelId)
    {
        return withChannel ( channelId, ModifiableChannel.class, channel -> {
            channel.getContext ().clear ();
            return redirectDefaultView ( channelId, true );
        } );
    }

    protected ModelAndView redirectDefaultView ( final String channelId, final boolean force )
    {
        return new ModelAndView ( ( force ? "redirect" : "referer" ) + ":/channel/" + channelId + "/view" );
    }

    @RequestMapping ( value = "/channel/{channelId}/deployKeys" )
    public ModelAndView deployKeys ( @PathVariable ( "channelId" ) final String channelId)
    {
        return withChannel ( channelId, DeployKeysChannelAdapter.class, deployChannel -> {
            return withChannel ( channelId, ReadableChannel.class, channel -> {
                final Map<String, Object> model = new HashMap<> ();

                final List<DeployGroup> channelDeployGroups = new ArrayList<> ( deployChannel.getDeployGroups () );
                Collections.sort ( channelDeployGroups, DeployGroup.NAME_COMPARATOR );

                model.put ( "channel", channel.getInformation () );
                model.put ( "channelDeployGroups", channelDeployGroups );
                model.put ( "deployGroups", getGroupsForChannel ( channelDeployGroups ) );

                model.put ( "sitePrefix", this.sitePrefix.getSitePrefix () );

                return new ModelAndView ( "channel/deployKeys", model );
            } );
        } );
    }

    protected List<DeployGroup> getGroupsForChannel ( final Collection<DeployGroup> channelDeployGroups )
    {
        final List<DeployGroup> groups = new ArrayList<> ( this.deployAuthService.listGroups ( 0, -1 ) );
        groups.removeAll ( channelDeployGroups );
        Collections.sort ( groups, DeployGroup.NAME_COMPARATOR );
        return groups;
    }

    protected <T> ModelAndView withChannel ( final String channelId, final Class<T> clazz, final ChannelOperation<ModelAndView, T> operation )
    {
        return Channels.withChannel ( this.channelService, channelId, clazz, operation );
    }

    @RequestMapping ( "/channel/{channelId}/help/p2" )
    @Secured ( false )
    @HttpConstraint ( PERMIT )
    public ModelAndView helpP2 ( @PathVariable ( "channelId" ) final String channelId)
    {
        return withChannel ( channelId, ReadableChannel.class, channel -> {
            final Map<String, Object> model = new HashMap<> ();

            model.put ( "channel", channel.getInformation () );
            model.put ( "sitePrefix", this.sitePrefix.getSitePrefix () );

            model.put ( "p2Active", channel.hasAspect ( "p2.repo" ) );

            return new ModelAndView ( "channel/help/p2", model );
        } );
    }

    @RequestMapping ( "/channel/{channelId}/help/api" )
    @Secured ( false )
    @HttpConstraint ( PERMIT )
    public ModelAndView helpApi ( @PathVariable ( "channelId" ) final String channelId, final HttpServletRequest request)
    {
        return withChannel ( channelId, ReadableChannel.class, channel -> {
            final Map<String, Object> model = new HashMap<> ();

            model.put ( "channel", channel.getInformation () );
            model.put ( "sitePrefix", this.sitePrefix.getSitePrefix () );

            final String exampleKey;
            if ( request.isUserInRole ( "MANAGER" ) )
            {
                @SuppressWarnings ( "null" )
                @NonNull
                final Collection<DeployKey> keys = (@NonNull Collection<DeployKey>)this.channelService.getChannelDeployKeys ( By.id ( channel.getId ().getId () ) ).orElse ( Collections.emptyList () );
                exampleKey = keys.stream ().map ( DeployKey::getKey ).findFirst ().orElse ( DEFAULT_EXAMPLE_KEY );
            }
            else
            {
                exampleKey = DEFAULT_EXAMPLE_KEY;
            }

            model.put ( "hasExampleKey", !DEFAULT_EXAMPLE_KEY.equals ( exampleKey ) );

            model.put ( "exampleKey", exampleKey );
            model.put ( "exampleSitePrefix", makeCredentialsPrefix ( this.sitePrefix.getSitePrefix (), "deploy", exampleKey ) );

            return new ModelAndView ( "channel/help/api", model );
        } );
    }

    private String makeCredentialsPrefix ( final String sitePrefix, final String name, final String password )
    {
        try
        {
            final URIBuilder builder = new URIBuilder ( sitePrefix );

            builder.setUserInfo ( name, password );

            return builder.build ().toString ();
        }
        catch ( final URISyntaxException e )
        {
            return sitePrefix;
        }
    }

    @RequestMapping ( value = "/channel/{channelId}/addDeployGroup", method = RequestMethod.POST )
    public ModelAndView addDeployGroup ( @PathVariable ( "channelId" ) final String channelId, @RequestParameter ( "groupId" ) final String groupId)
    {
        return modifyDeployGroup ( channelId, groupId, DeployKeysChannelAdapter::assignDeployGroup );
    }

    @RequestMapping ( value = "/channel/{channelId}/removeDeployGroup", method = RequestMethod.POST )
    public ModelAndView removeDeployGroup ( @PathVariable ( "channelId" ) final String channelId, @RequestParameter ( "groupId" ) final String groupId)
    {
        return modifyDeployGroup ( channelId, groupId, DeployKeysChannelAdapter::unassignDeployGroup );
    }

    protected ModelAndView modifyDeployGroup ( final String channelId, final String groupId, final BiConsumer<DeployKeysChannelAdapter, String> cons )
    {
        return withChannel ( channelId, DeployKeysChannelAdapter.class, channel -> {
            cons.accept ( channel, groupId );

            return new ModelAndView ( "redirect:/channel/" + channelId + "/deployKeys" );
        } );
    }

    @Secured ( false )
    @RequestMapping ( value = "/channel/{channelId}/aspects", method = RequestMethod.GET )
    @HttpConstraint ( PERMIT )
    public ModelAndView aspects ( @PathVariable ( "channelId" ) final String channelId)
    {
        return withChannel ( channelId, ReadableChannel.class, channel -> {

            final ModelAndView model = new ModelAndView ( "channel/aspects" );

            final ChannelAspectProcessor aspects = Activator.getAspects ();
            final Collection<GroupInformation> groups = aspects.getGroups ();

            model.put ( "channel", channel.getInformation () );

            final Set<String> assigned = channel.getInformation ().getAspectStates ().keySet ();

            final List<AspectInformation> allAspects = AspectInformation.resolve ( groups, aspects.getAspectInformations ().values () );

            final List<AspectInformation> assignedAspects = AspectInformation.filterIds ( allAspects, ( id ) -> assigned.contains ( id ) );
            model.put ( "assignedAspects", assignedAspects );

            model.put ( "groupedAssignedAspects", AspectInformation.group ( assignedAspects ) );

            model.put ( "addAspects", AspectInformation.group ( AspectInformation.filterIds ( allAspects, ( id ) -> !assigned.contains ( id ) ) ) );

            final Map<String, String> nameMap = new HashMap<> ();
            for ( final AspectInformation ai : allAspects )
            {
                nameMap.put ( ai.getFactoryId (), ai.getName () );
            }

            model.put ( "nameMapJson", new GsonBuilder ().create ().toJson ( nameMap ) );

            model.put ( "breadcrumbs", new Breadcrumbs ( new Entry ( "Home", "/" ), Breadcrumbs.create ( "Channel", ChannelController.class, "view", "channelId", channelId ), new Entry ( "Aspects" ) ) );

            return model;
        } );
    }

    @RequestMapping ( value = "/channel/{channelId}/viewAspectVersions", method = RequestMethod.GET )
    public ModelAndView viewAspectVersions ( @PathVariable ( "channelId" ) final String channelId)
    {
        return withChannel ( channelId, ReadableChannel.class, channel -> {
            final Map<String, String> states = channel.getInformation ().getAspectStates ();

            final List<ChannelAspectInformation> aspects = Activator.getAspects ().resolve ( states.keySet () );
            Collections.sort ( aspects, ChannelAspectInformation.NAME_COMPARATOR );

            final Map<String, Object> model = new HashMap<> ( 3 );

            model.put ( "channel", channel.getInformation () );
            model.put ( "states", states );
            model.put ( "aspects", aspects );

            return new ModelAndView ( "channel/viewAspectVersions", model );
        } );
    }

    @RequestMapping ( value = "/channel/{channelId}/lock", method = RequestMethod.GET )
    public ModelAndView lock ( @PathVariable ( "channelId" ) final String channelId)
    {
        try
        {
            this.channelService.accessRun ( By.id ( channelId ), ModifiableChannel.class, channel -> {
                channel.lock ();
            } );
        }
        catch ( final ChannelNotFoundException e )
        {
            return CommonController.createNotFound ( "channel", channelId );
        }

        return redirectDefaultView ( channelId, false );
    }

    @RequestMapping ( value = "/channel/{channelId}/unlock", method = RequestMethod.GET )
    public ModelAndView unlock ( @PathVariable ( "channelId" ) final String channelId)
    {
        try
        {
            this.channelService.accessRun ( By.id ( channelId ), ModifiableChannel.class, channel -> {
                channel.unlock ();
            } );
        }
        catch ( final ChannelNotFoundException e )
        {
            return CommonController.createNotFound ( "channel", channelId );
        }

        return redirectDefaultView ( channelId, false );
    }

    @RequestMapping ( value = "/channel/{channelId}/addAspect", method = RequestMethod.POST )
    public ModelAndView addAspect ( @PathVariable ( "channelId" ) final String channelId, @RequestParameter ( "aspect" ) final String aspectFactoryId)
    {
        return withChannel ( channelId, AspectableChannel.class, channel -> {
            channel.addAspects ( false, aspectFactoryId );
            return new ModelAndView ( String.format ( "redirect:aspects", channelId ) );
        } );
    }

    @RequestMapping ( value = "/channel/{channelId}/addAspectWithDependencies", method = RequestMethod.POST )
    public ModelAndView addAspectWithDependencies ( @PathVariable ( "channelId" ) final String channelId, @RequestParameter ( "aspect" ) final String aspectFactoryId)
    {
        return withChannel ( channelId, AspectableChannel.class, channel -> {
            channel.addAspects ( true, aspectFactoryId );
            return new ModelAndView ( String.format ( "redirect:aspects", channelId ) );
        } );
    }

    @RequestMapping ( value = "/channel/{channelId}/removeAspect", method = RequestMethod.POST )
    public ModelAndView removeAspect ( @PathVariable ( "channelId" ) final String channelId, @RequestParameter ( "aspect" ) final String aspectFactoryId)
    {
        return withChannel ( channelId, AspectableChannel.class, channel -> {
            channel.removeAspects ( aspectFactoryId );
            return new ModelAndView ( String.format ( "redirect:aspects", channelId ) );
        } );
    }

    @RequestMapping ( value = "/channel/{channelId}/refreshAspect", method = RequestMethod.POST )
    public ModelAndView refreshAspect ( @PathVariable ( "channelId" ) final String channelId, @RequestParameter ( "aspect" ) final String aspectFactoryId)
    {
        return withChannel ( channelId, AspectableChannel.class, channel -> {
            channel.refreshAspects ( aspectFactoryId );
            return new ModelAndView ( String.format ( "redirect:aspects", channelId ) );
        } );
    }

    @RequestMapping ( value = "/channel/{channelId}/refreshAllAspects", method = RequestMethod.GET )
    public ModelAndView refreshAllAspects ( @PathVariable ( "channelId" ) final String channelId, final HttpServletRequest request)
    {
        return withChannel ( channelId, AspectableChannel.class, channel -> {
            channel.refreshAspects ();
            return redirectDefaultView ( channelId, false );
        } );
    }

    @RequestMapping ( value = "/channel/{channelId}/edit", method = RequestMethod.GET )
    public ModelAndView edit ( @PathVariable ( "channelId" ) final String channelId)
    {
        final Map<String, Object> model = new HashMap<> ();

        final Optional<ChannelInformation> info = this.channelService.getState ( By.id ( channelId ) );
        if ( !info.isPresent () )
        {
            return CommonController.createNotFound ( "channel", channelId );
        }

        final EditChannel edit = new EditChannel ();

        final ChannelInformation channel = info.get ();

        edit.setId ( channel.getId () );
        edit.setNames ( joinChannelNames ( channel.getNames () ) );
        edit.setDescription ( channel.getDescription () );

        model.put ( "command", edit );
        model.put ( "breadcrumbs", new Breadcrumbs ( new Entry ( "Home", "/" ), Breadcrumbs.create ( "Channel", ChannelController.class, "view", "channelId", channelId ), new Entry ( "Edit" ) ) );

        return new ModelAndView ( "channel/edit", model );

    }

    @RequestMapping ( value = "/channel/{channelId}/edit", method = RequestMethod.POST )
    public ModelAndView editPost ( @PathVariable ( "channelId" ) final String channelId, @Valid @FormData ( "command" ) final EditChannel data, final BindingResult result)
    {
        if ( !result.hasErrors () )
        {
            this.channelService.accessRun ( By.id ( channelId ), DescriptorAdapter.class, channel -> {
                channel.setNames ( splitChannelNames ( data.getNames () ) );
                channel.setDescription ( data.getDescription () );
            } );

            return redirectDefaultView ( channelId, true );
        }
        else
        {
            final Map<String, Object> model = new HashMap<> ();
            model.put ( "command", data );
            model.put ( "breadcrumbs", new Breadcrumbs ( new Entry ( "Home", "/" ), Breadcrumbs.create ( "Channel", ChannelController.class, "view", "channelId", channelId ), new Entry ( "Edit" ) ) );
            return new ModelAndView ( "channel/edit", model );
        }
    }

    @RequestMapping ( value = "/channel/{channelId}/viewCache", method = RequestMethod.GET )
    @HttpConstraint ( rolesAllowed = { "MANAGER", "ADMIN" } )
    public ModelAndView viewCache ( @PathVariable ( "channelId" ) final String channelId)
    {
        return withChannel ( channelId, ReadableChannel.class, channel -> {
            final Map<String, Object> model = new HashMap<> ();

            model.put ( "channel", channel.getInformation () );
            model.put ( "cacheEntries", channel.getCacheEntries ().values () );

            return new ModelAndView ( "channel/viewCache", model );
        } );
    }

    @RequestMapping ( value = "/channel/{channelId}/viewCacheEntry", method = RequestMethod.GET )
    @HttpConstraint ( rolesAllowed = { "MANAGER", "ADMIN" } )
    public ModelAndView viewCacheEntry ( @PathVariable ( "channelId" ) final String channelId, @RequestParameter ( "namespace" ) final String namespace, @RequestParameter ( "key" ) final String key, final HttpServletResponse response)
    {
        return withChannel ( channelId, ReadableChannel.class, channel -> {

            if ( !channel.streamCacheEntry ( new MetaKey ( namespace, key ), entry -> {
                logger.trace ( "Length: {}, Mime: {}", entry.getSize (), entry.getMimeType () );

                response.setContentLengthLong ( entry.getSize () );
                response.setContentType ( entry.getMimeType () );
                response.setHeader ( "Content-Disposition", String.format ( "inline; filename=%s", URLEncoder.encode ( entry.getName (), "UTF-8" ) ) );
                // response.setHeader ( "Content-Disposition", String.format ( "attachment; filename=%s", entry.getName () ) );
                ByteStreams.copy ( entry.getStream (), response.getOutputStream () );
            } ) )
            {
                return CommonController.createNotFound ( "channel cache entry", String.format ( "%s:%s", namespace, key ) );
            }

            return null;
        } );
    }

    @Override
    public List<MenuEntry> getActions ( final HttpServletRequest request, final Object object )
    {
        if ( object instanceof ChannelInformation )
        {
            final ChannelInformation channel = (ChannelInformation)object;

            final Map<String, Object> model = new HashMap<> ( 1 );
            model.put ( "channelId", channel.getId () );

            final List<MenuEntry> result = new LinkedList<> ();

            if ( request.isUserInRole ( "MANAGER" ) )
            {
                if ( !channel.getState ().isLocked () )
                {
                    result.add ( new MenuEntry ( "Add Artifact", 100, LinkTarget.createFromController ( ChannelController.class, "add" ).expand ( model ), Modifier.PRIMARY, null ) );
                    result.add ( new MenuEntry ( "Delete Channel", 400, LinkTarget.createFromController ( ChannelController.class, "delete" ).expand ( model ), Modifier.DANGER, "trash" ).makeModalMessage ( "Delete channel", "Are you sure you want to delete the whole channel?" ) );
                    result.add ( new MenuEntry ( "Clear Channel", 500, LinkTarget.createFromController ( ChannelController.class, "clear" ).expand ( model ), Modifier.WARNING, null ).makeModalMessage ( "Clear channel", "Are you sure you want to delete all artifacts from this channel?" ) );

                    result.add ( new MenuEntry ( "Lock Channel", 600, LinkTarget.createFromController ( ChannelController.class, "lock" ).expand ( model ), Modifier.DEFAULT, null ) );
                }
                else
                {
                    result.add ( new MenuEntry ( "Unlock Channel", 600, LinkTarget.createFromController ( ChannelController.class, "unlock" ).expand ( model ), Modifier.DEFAULT, null ) );
                }

                result.add ( new MenuEntry ( "Edit", 150, "Edit Channel", 200, LinkTarget.createFromController ( ChannelController.class, "edit" ).expand ( model ), Modifier.DEFAULT, null ) );
                result.add ( new MenuEntry ( "Maintenance", 160, "Refresh aspects", 100, LinkTarget.createFromController ( ChannelController.class, "refreshAllAspects" ).expand ( model ), Modifier.SUCCESS, "refresh" ) );
            }

            if ( request.getRemoteUser () != null )
            {
                result.add ( new MenuEntry ( "Edit", 150, "Configure Aspects", 300, LinkTarget.createFromController ( ChannelController.class, "aspects" ).expand ( model ), Modifier.DEFAULT, null ) );
            }

            return result;
        }
        else if ( Tags.ACTION_TAG_CHANNELS.equals ( object ) )
        {
            final List<MenuEntry> result = new LinkedList<> ();

            if ( request.isUserInRole ( "MANAGER" ) )
            {
                // result.add ( new MenuEntry ( "Create Channel", 100, LinkTarget.createFromController ( ChannelController.class, "createDetailed" ), Modifier.PRIMARY, null ) );
                result.add ( new MenuEntry ( "Create Channel", 120, LinkTarget.createFromController ( ChannelController.class, "createWithRecipe" ), Modifier.PRIMARY, null ) );
            }

            return result;
        }
        else if ( object instanceof org.eclipse.packagedrone.repo.channel.ChannelArtifactInformation )
        {
            final ChannelArtifactInformation ai = (ChannelArtifactInformation)object;

            final List<MenuEntry> result = new LinkedList<> ();

            final Map<String, Object> model = new HashMap<> ( 2 );
            model.put ( "channelId", ai.getChannelId ().getId () );
            model.put ( "artifactId", ai.getId () );

            if ( request.isUserInRole ( "MANAGER" ) )
            {
                if ( ai.is ( "stored" ) )
                {
                    result.add ( new MenuEntry ( "Attach Artifact", 200, LinkTarget.createFromController ( ChannelController.class, "attachArtifact" ).expand ( model ), Modifier.PRIMARY, null ) );
                    result.add ( new MenuEntry ( "Delete", 1000, LinkTarget.createFromController ( ChannelController.class, "deleteArtifact" ).expand ( model ), Modifier.DANGER, "trash" ) );
                }
            }

            return result;
        }
        return null;
    }

    @Override
    public List<MenuEntry> getViews ( final HttpServletRequest request, final Object object )
    {
        if ( object instanceof ChannelInformation )
        {
            final ChannelInformation channel = (ChannelInformation)object;

            final Map<String, Object> model = new HashMap<> ( 1 );
            model.put ( "channelId", channel.getId () );

            final List<MenuEntry> result = new LinkedList<> ();

            result.add ( new MenuEntry ( "Content", 100, LinkTarget.createFromController ( ChannelController.class, "view" ).expand ( model ), Modifier.DEFAULT, null ) );
            result.add ( new MenuEntry ( "List", 120, LinkTarget.createFromController ( ChannelController.class, "viewPlain" ).expand ( model ), Modifier.DEFAULT, null ) );
            result.add ( new MenuEntry ( "Details", 200, LinkTarget.createFromController ( ChannelController.class, "details" ).expand ( model ), Modifier.DEFAULT, null ) );

            result.add ( new MenuEntry ( null, -1, "Validation", 210, LinkTarget.createFromController ( ChannelController.class, "viewValidation" ).expand ( model ), Modifier.DEFAULT, null ).setBadge ( channel.getState ().getValidationErrorCount () ) );

            if ( request.isUserInRole ( "MANAGER" ) )
            {
                result.add ( new MenuEntry ( "Deploy Keys", 1000, LinkTarget.createFromController ( ChannelController.class, "deployKeys" ).expand ( model ), Modifier.DEFAULT, null ) );
            }

            if ( request.isUserInRole ( "MANAGER" ) || request.isUserInRole ( "ADMIN" ) )
            {
                result.add ( new MenuEntry ( "Internal", 400, "View Cache", 100, LinkTarget.createFromController ( ChannelController.class, "viewCache" ).expand ( model ), Modifier.DEFAULT, null ) );
                result.add ( new MenuEntry ( "Internal", 400, "Aspect Versions", 100, LinkTarget.createFromController ( ChannelController.class, "viewAspectVersions" ).expand ( model ), Modifier.DEFAULT, null ) );
            }

            if ( channel.hasAspect ( "p2.repo" ) )
            {
                result.add ( new MenuEntry ( "Help", Integer.MAX_VALUE, "P2 Repository", 2_000, LinkTarget.createFromController ( ChannelController.class, "helpP2" ).expand ( model ), Modifier.DEFAULT, "info-sign" ) );
            }

            result.add ( new MenuEntry ( "Help", Integer.MAX_VALUE, "API Upload", 1_100, LinkTarget.createFromController ( ChannelController.class, "helpApi" ).expand ( model ), Modifier.DEFAULT, "upload" ) );

            return result;
        }
        return null;
    }

    @ControllerValidator ( formDataClass = CreateChannel.class )
    public void validateCreate ( final CreateChannel data, final ValidationContext ctx )
    {
        validateChannelNamesUnique ( null, splitChannelNames ( data.getNames () ), ctx );
    }

    @ControllerValidator ( formDataClass = EditChannel.class )
    public void validateEdit ( final EditChannel data, final ValidationContext ctx )
    {
        validateChannelNamesUnique ( data.getId (), splitChannelNames ( data.getNames () ), ctx );
    }

    private void validateChannelNamesUnique ( final String id, final Iterable<String> names, final ValidationContext ctx )
    {
        for ( final String name : names )
        {
            validateChannelNameUnique ( id, name, ctx );
        }
    }

    private void validateChannelNameUnique ( final String id, final String name, final ValidationContext ctx )
    {
        if ( name == null || name.isEmpty () )
        {
            return;
        }

        final ChannelInformation other = this.channelService.getState ( By.name ( name ) ).orElse ( null );

        if ( other == null )
        {
            // no one else has this name right now
            return;
        }

        if ( id != null && other.getId ().equals ( id ) )
        {
            // name was found, but it belongs to ourself -> rename
            return;
        }

        ctx.error ( "name", String.format ( "The channel name '%s' is already in use by channel '%s'", name, other.getId () ) );
    }

    @RequestMapping ( value = "/channel/{channelId}/artifact/{artifactId}/attach", method = RequestMethod.GET )
    public ModelAndView attachArtifact ( @PathVariable ( "channelId" ) final String channelId, @PathVariable ( "artifactId" ) final String artifactId)
    {
        return withChannel ( channelId, ReadableChannel.class, channel -> {
            final Optional<ChannelArtifactInformation> artifact = channel.getArtifact ( artifactId );
            if ( !artifact.isPresent () )
            {
                return CommonController.createNotFound ( "artifact", artifactId );
            }

            return new ModelAndView ( "/artifact/attach", "artifact", artifact.get () );
        } );
    }

    @RequestMapping ( value = "/channel/{channelId}/artifact/{artifactId}/attach", method = RequestMethod.POST )
    public ModelAndView attachArtifactPost ( @PathVariable ( "channelId" ) final String channelId, @PathVariable ( "artifactId" ) final String artifactId, @RequestParameter (
            required = false, value = "name" ) final String name, final @RequestParameter ( "file" ) Part file)
    {
        return withChannel ( channelId, ModifiableChannel.class, channel -> {

            String targetName = name;

            final Optional<ChannelArtifactInformation> parentArtifact = channel.getArtifact ( artifactId );
            if ( !parentArtifact.isPresent () )
            {
                return CommonController.createNotFound ( "artifact", artifactId );
            }

            try
            {
                if ( targetName == null || targetName.isEmpty () )
                {
                    targetName = file.getSubmittedFileName ();
                }

                channel.getContext ().createArtifact ( artifactId, file.getInputStream (), targetName, null );
            }
            catch ( final IOException e )
            {
                return new ModelAndView ( "/error/upload" );
            }

            return new ModelAndView ( "redirect:/channel/" + UrlEscapers.urlPathSegmentEscaper ().escape ( channelId ) + "/view" );
        } );
    }

    @Override
    public void extend ( final UrlSetContext context )
    {
        // add location of channels page

        context.addLocation ( "/channel", ofNullable ( calcLastMod () ), of ( ChangeFrequency.DAILY ), empty () );
    }

    private Instant calcLastMod ()
    {
        Instant globalLastMod = null;

        for ( final ChannelInformation ci : this.channelService.list () )
        {
            final Optional<Instant> lastMod = ofNullable ( ci.getState ().getModificationTimestamp () );

            if ( globalLastMod == null || lastMod.get ().isAfter ( globalLastMod ) )
            {
                globalLastMod = lastMod.get ();
            }
        }
        return globalLastMod;
    }

}
