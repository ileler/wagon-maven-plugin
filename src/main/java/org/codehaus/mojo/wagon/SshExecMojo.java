package org.codehaus.mojo.wagon;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.wagon.Streams;
import org.apache.maven.wagon.Wagon;
import org.apache.maven.wagon.WagonException;
import org.apache.maven.wagon.providers.ssh.jsch.ScpWagon;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.StringUtils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

/**
 * Executes a list of commands against a given server.
 *
 * @goal sshexec
 * @requiresProject true
 */
public class SshExecMojo
    extends AbstractSingleWagonMojo
{

    /**
     * The commands that we will execute.
     *
     * @parameter
     * @required
     */
    private String[] commands;

    /**
     * Allow option not to fail the build on error
     *
     * @parameter default-value = "true"
     */
    private boolean failOnError = true;

    /**
     * Option to display remote command's outputs
     *
     * @parameter default-value = "false"
     */
    private boolean displayCommandOutputs = true;

    /**
     * Option to `tail -f` remote command's outputs
     *
     * @parameter default-value = "true"
     */
    private boolean tailOut = true;

    protected void execute( final Wagon wagon )
        throws MojoExecutionException
    {
        if ( commands != null )
        {
            String basedir = wagon.getRepository().getBasedir();
            String cdBasedir = "";
            if ( StringUtils.isNotBlank(basedir) )
            {
                cdBasedir = "cd " + basedir + " && ";
            }
            for ( int i = 0; i < commands.length; i++ )
            {

                try
                {
                    this.getLog().info( "sshexec: " + commands[i] + " ..." );
                    if ( displayCommandOutputs )
                    {
                        if ( tailOut )
                        {
                            tailExecute((ScpWagon) wagon, cdBasedir + commands[i]);
                        }
                        else
                        {
                            Streams stream = ( (ScpWagon) wagon ).executeCommand( cdBasedir + commands[i], true, false );
                            System.out.println( stream.getOut() );
                            System.out.println( stream.getErr() );
                        }
                    }
                    else
                    {
                        ( (ScpWagon) wagon ).executeCommand( cdBasedir + commands[i], true, false );
                    }
                }
                catch ( final WagonException e )
                {
                    if ( this.failOnError )
                    {
                        throw new MojoExecutionException( "Unable to execute remote command", e );
                    }
                    else
                    {
                        this.getLog().warn( e );
                    }
                }

            }
        }
    }

    private void tailExecute (ScpWagon scpWagon, String command)
            throws MojoExecutionException
    {
        try
        {
            final PipedInputStream outInputStream = new PipedInputStream();
            final PipedInputStream errInputStream = new PipedInputStream();
            final PipedOutputStream outOutputStream = new PipedOutputStream();
            final PipedOutputStream errOutputStream = new PipedOutputStream();
            outInputStream.connect(outOutputStream);
            errInputStream.connect(errOutputStream);
            scpWagon.executeCommand( command, true, false, errOutputStream, outOutputStream );
            pipeStart( new BufferedReader( new InputStreamReader( outInputStream ) ), outInputStream, outOutputStream );
            pipeStart( new BufferedReader( new InputStreamReader( errInputStream ) ), errInputStream, errOutputStream );
        }
        catch (Exception e)
        {
            if ( this.failOnError )
            {
                throw new MojoExecutionException( "Unable to execute remote command", e );
            }
            else
            {
                this.getLog().warn( e );
            }
        }
    }

    private void pipeStart(final BufferedReader reader, final InputStream inputStream, final OutputStream outputStream) {
        try
        {
            while ( true )
            {
                String line = reader.readLine();

                if ( line == null )
                {
                    break;
                }

                System.out.println(line);
            }
        }
        catch ( Exception e )
        {
            e.printStackTrace();
        }
        finally
        {
            IOUtil.close( reader );
            IOUtil.close( inputStream );
            IOUtil.close( outputStream );
        }
    }
}
