package org.eclipse.packagedrone.repo.channel.web.channel;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.annotation.HttpConstraint;
import javax.servlet.annotation.ServletSecurity.EmptyRoleSemantic;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import org.eclipse.packagedrone.repo.channel.ChannelId;
import org.eclipse.packagedrone.repo.channel.ChannelInformation;
import org.eclipse.packagedrone.repo.channel.ChannelService;
import org.eclipse.packagedrone.repo.channel.transfer.TransferService;
import org.eclipse.packagedrone.repo.channel.web.Tags;
import org.eclipse.packagedrone.sec.web.controller.HttpContraintControllerInterceptor;
import org.eclipse.packagedrone.sec.web.controller.Secured;
import org.eclipse.packagedrone.sec.web.controller.SecuredControllerInterceptor;
import org.eclipse.packagedrone.utils.io.IOConsumer;
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
import org.eclipse.packagedrone.web.controller.ControllerInterceptor;
import org.eclipse.packagedrone.web.controller.ProfilerControllerInterceptor;
import org.eclipse.packagedrone.web.controller.binding.PathVariable;
import org.eclipse.packagedrone.web.controller.binding.RequestParameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.ByteStreams;
import com.google.common.net.UrlEscapers;

@Secured
@Controller
@ViewResolver ( "/WEB-INF/views/%s.jsp" )
@ControllerInterceptor ( SecuredControllerInterceptor.class )
@HttpConstraint ( rolesAllowed = "MANAGER" )
@ControllerInterceptor ( HttpContraintControllerInterceptor.class )
@ControllerInterceptor ( ProfilerControllerInterceptor.class )
public class TransferController implements InterfaceExtender
{
    private final static Logger logger = LoggerFactory.getLogger ( TransferController.class );

    private static final MessageFormat EXPORT_PATTERN = new MessageFormat ( "export-channel-{0}-{1,date,yyyyMMdd-HHmm}.zip" );

    private static final MessageFormat EXPORT_ALL_PATTERN = new MessageFormat ( "export-all-{0,date,yyyyMMdd-HHmm}.zip" );

    private TransferService transferService;

    private ChannelService channelService;

    public void setTransferService ( final TransferService transferService )
    {
        this.transferService = transferService;
    }

    public void setChannelService ( final ChannelService channelService )
    {
        this.channelService = channelService;
    }

    protected ModelAndView performExport ( final HttpServletResponse response, final String filename, final IOConsumer<OutputStream> exporter )
    {
        try
        {
            final Path tmp = Files.createTempFile ( "export-", null );

            try
            {
                try ( OutputStream tmpStream = new BufferedOutputStream ( new FileOutputStream ( tmp.toFile () ) ) )
                {
                    // first we spool this out to  temp file, so that we don't block the channel for too long
                    exporter.accept ( tmpStream );
                }

                response.setContentLengthLong ( tmp.toFile ().length () );
                response.setContentType ( "application/zip" );
                response.setHeader ( "Content-Disposition", String.format ( "attachment; filename=%s", filename ) );

                try ( InputStream inStream = new BufferedInputStream ( new FileInputStream ( tmp.toFile () ) ) )
                {
                    ByteStreams.copy ( inStream, response.getOutputStream () );
                }

                return null;
            }
            finally
            {
                Files.deleteIfExists ( tmp );
            }
        }
        catch ( final IOException e )
        {
            return CommonController.createError ( "Failed to export", null, e );
        }
    }

    @RequestMapping ( value = "/channel/{channelId}/export", method = RequestMethod.GET )
    @HttpConstraint ( value = EmptyRoleSemantic.PERMIT )
    public ModelAndView exportChannel ( @PathVariable ( "channelId" ) final String channelId, final HttpServletResponse response)
    {
        return performExport ( response, makeExportFileName ( channelId ), ( stream ) -> this.transferService.exportChannel ( channelId, stream ) );
    }

    @RequestMapping ( value = "/channel/export", method = RequestMethod.GET )
    @HttpConstraint ( value = EmptyRoleSemantic.PERMIT )
    public ModelAndView exportAll ( final HttpServletResponse response )
    {
        return performExport ( response, makeExportFileName ( null ), this.transferService::exportAll );
    }

    @RequestMapping ( value = "/channel/import", method = RequestMethod.GET )
    public ModelAndView importChannel ()
    {
        return new ModelAndView ( "channel/importChannel" );
    }

    @RequestMapping ( value = "/channel/import", method = RequestMethod.POST )
    public ModelAndView importChannelPost ( @RequestParameter ( "file" ) final Part part, @RequestParameter (
            value = "useName", required = false ) final boolean useName)
    {
        try
        {
            final ChannelId channelId = this.transferService.importChannel ( part.getInputStream (), useName );
            return new ModelAndView ( "redirect:/channel/" + UrlEscapers.urlPathSegmentEscaper ().escape ( channelId.getId () ) + "/view" );
        }
        catch ( final Exception e )
        {
            logger.warn ( "Failed to import", e );
            return CommonController.createError ( "Import", "Channel", "Failed to import channel", e, null );
        }
    }

    @RequestMapping ( value = "/channel/importAll", method = RequestMethod.GET )
    public ModelAndView importAll ( final HttpServletResponse response )
    {
        return new ModelAndView ( "channel/importAll" );
    }

    @RequestMapping ( value = "/channel/importAll", method = RequestMethod.POST )
    public ModelAndView importAllPost ( @RequestParameter ( value = "useNames",
            required = false ) final boolean useNames, @RequestParameter ( value = "wipe",
                    required = false ) final boolean wipe, @RequestParameter ( "file" ) final Part part, @RequestParameter (
                            value = "location", required = false ) final String location)
    {
        try
        {
            if ( location != null && !location.isEmpty () )
            {
                try ( BufferedInputStream stream = new BufferedInputStream ( new FileInputStream ( new File ( location ) ) ) )
                {
                    this.transferService.importAll ( stream, useNames, wipe );
                }
            }
            else
            {
                this.transferService.importAll ( part.getInputStream (), useNames, wipe );
            }
            return new ModelAndView ( "redirect:/channel" );
        }
        catch ( final Exception e )
        {
            logger.warn ( "Failed to import", e );
            return CommonController.createError ( "Import", "Channel", "Failed to import channel", e, null );
        }
    }

    private String makeExportFileName ( final String channelId )
    {
        if ( channelId != null )
        {
            return EXPORT_PATTERN.format ( new Object[] { channelId, new Date () } );
        }
        else
        {
            return EXPORT_ALL_PATTERN.format ( new Object[] { new Date () } );
        }
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

            if ( request.getRemoteUser () != null )
            {
                result.add ( new MenuEntry ( "Maintenance", 160, "Export channel", 200, LinkTarget.createFromController ( TransferController.class, "exportChannel" ).expand ( model ), Modifier.DEFAULT, "export" ) );
            }

            return result;
        }
        else if ( Tags.ACTION_TAG_CHANNELS.equals ( object ) )
        {
            final List<MenuEntry> result = new LinkedList<> ();

            if ( request.isUserInRole ( "MANAGER" ) )
            {
                result.add ( new MenuEntry ( "Maintenance", 160, "Import channel", 200, LinkTarget.createFromController ( TransferController.class, "importChannel" ), Modifier.DEFAULT, "import" ) );
                result.add ( new MenuEntry ( "Maintenance", 160, "Export all channels", 300, LinkTarget.createFromController ( TransferController.class, "exportAll" ), Modifier.DEFAULT, "export" ) );
            }

            return result;
        }
        return null;
    }
}
