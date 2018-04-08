package org.codehaus.mojo.wagon;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.settings.Settings;
import org.apache.maven.wagon.Wagon;
import org.codehaus.mojo.wagon.shared.WagonFactory;
import org.codehaus.mojo.wagon.shared.WagonFileSet;
import org.codehaus.mojo.wagon.shared.WagonUtils;
import org.codehaus.plexus.PlexusContainer;

/**
 * Provides base functionality for dealing with I/O using wagon.
 */
public abstract class AbstractWagonMojo
    extends AbstractMojo
{

    @Component
    protected WagonFactory wagonFactory;

    /**
     * The current user system settings for use in Maven.
     */
    @Parameter( defaultValue = "${settings}", readonly = true )
    protected Settings settings;

    /**
     * Internal Maven's project.
     */
    @Parameter( defaultValue = "${project}", readonly = true )
    protected MavenProject project;

    /**
     * When <code>true</code>, skip the execution.
     *
     * @parameter property="wagon.skip" default-value="false"
     */
    protected boolean skip = false;

    /////////////////////////////////////////////////////////////////////////

    /**
     * Convenient method to create a wagon
     *
     * @param id
     * @param url
     * @return
     * @throws MojoExecutionException
     */
    protected Wagon createWagon( String id, String url )
        throws MojoExecutionException
    {
        try
        {
            return wagonFactory.create( url, id, settings );
        }
        catch ( Exception e )
        {
            throw new MojoExecutionException( "Unable to create a Wagon instance for " + url, e );
        }

    }

    protected WagonFileSet getWagonFileSet( String fromDir, String includes, String excludes, boolean caseSensitive,
                                            String toDir )
    {
        return WagonUtils.getWagonFileSet( fromDir, includes, excludes, caseSensitive, toDir );
    }
}