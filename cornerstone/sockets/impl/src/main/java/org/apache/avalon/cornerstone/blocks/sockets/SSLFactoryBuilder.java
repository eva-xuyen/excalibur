/* 
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 * 
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.avalon.cornerstone.blocks.sockets;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.util.Arrays;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocketFactory;
import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.logger.AbstractLogEnabled;

/**
 * Builds SSLContexts with desired properties. Hides all the gory
 * details of SSLContext productions behind nice Avalon
 * interfaces. Married to Sun JCA implementation.
 * <p>
 * Configuration looks like:
 * <pre>
 * &lt;ssl-factory&gt;
 *    &lt;keystore&gt;
 *      &lt;file&gt;conf/keystore&lt;/file&gt; &lt;!-- keystore file location --&gt;
 *      &lt;password&gt;&lt;/password&gt; &lt;!-- Key Store file password, only used to check keystore integrity --&gt;
 *      &lt;key-password&gt;&lt;/key-password&gt; &lt;!-- Only required when you need to decrypt a private key --&gt;
 *     &lt;type&gt;JKS&lt;/type&gt; &lt;!-- Key Store file format, defaults to JKS --&gt;
 *     &lt;algorithm&gt;SunX509&lt;/algorithm&gt; &lt;!-- Cryptography provider ID, defaults to SunX509 --&gt;
 *   &lt;/keystore&gt;
 *   &lt;!-- SSL protocol to use, defaults to TLS, another possible value is SSL --&gt;
 *   &lt;protocol&gt;TLS&lt;/protocol&gt;
 * &lt;/ssl-factory&gt;
 * </pre>
 * </p>
 * <p>
 * Notes on keystore files. Absolute paths are supported. Relative
 * paths are interpreted relative to .sar base directory. Defaults to
 * conf/keystore. Since keystore usually contains sensitive keys it
 * maybe beneficial to <b>not</b> include the keystores into the .sar
 * files.
 * </p>
 * @author <a href="mailto:greg-avalon-apps at nest.cx">Greg Steuck</a>
 */
public class SSLFactoryBuilder extends AbstractLogEnabled
    implements Configurable, Contextualizable, Disposable, Initializable
{
    private File m_baseDirectory;
    private File m_keystoreFile;

    private String m_keystorePassword;
    private String m_keyPassword;
    private String m_protocol;
    private String m_provider;
    private String m_keystoreFormat;

    private SSLContext m_ctx;

    static
    {
        // Registers Sun's providers
        java.security.Security.addProvider( new sun.security.provider.Sun() );
        java.security.Security.addProvider( new com.sun.net.ssl.internal.ssl.Provider() );
    }

    /**
     * Requires a BlockContext. We'll see how we end up expressing
     * these dependencies.
     * @avalon.entry key="urn:avalon:home"
     */
    public void contextualize( final Context context ) throws ContextException
    {
        try
        {
            m_baseDirectory = (File) context.get( "urn:avalon:home" );
        }
        catch( ContextException ce )
        {
            m_baseDirectory = (File)context.get( "app.home" );
        }
    }

    public void configure( final Configuration configuration )
        throws ConfigurationException
    {
        final Configuration storeConfig = configuration.getChild( "keystore" );
        final String fileName = storeConfig.getChild( "file" ).getValue( "conf/keystore" );
        final File configuredFile = new File( fileName );
        if( !configuredFile.isAbsolute() )
        {
            m_keystoreFile = new File( m_baseDirectory, fileName );
        }
        else
        {
            m_keystoreFile = configuredFile;
        }

        m_keystorePassword = storeConfig.getChild( "password" ).getValue( null );
        m_keyPassword = storeConfig.getChild( "key-password" ).getValue( null );
        // key is named incorrectly, left as is for compatibility
        m_provider = storeConfig.getChild( "algorithm" ).getValue( "SunX509" );
        // key is named incorrectly, left as is for compatibility
        m_keystoreFormat = storeConfig.getChild( "type" ).getValue( "JKS" );
        // ugly compatibility workaround follows
        m_protocol = configuration.getChild( "protocol" ).
            getValue( storeConfig.getChild( "protocol" ).getValue( "TLS" ) );
    }

    /**
     * Produces a fresh ssl socket factory with configured parameters.
     */
    public SSLSocketFactory buildSocketFactory()
    {
        return m_ctx.getSocketFactory();
    }

    /**
     * Produces a fresh ssl server socket factory with configured
     * parameters.
     */
    public SSLServerSocketFactory buildServerSocketFactory()
    {
        return m_ctx.getServerSocketFactory();
    }

    public void initialize()
        throws IOException, GeneralSecurityException
    {
        final FileInputStream keyStream = new FileInputStream( m_keystoreFile );
        try
        {
            m_ctx = makeContext( keyStream, m_keystorePassword,
                                 m_keyPassword, m_protocol,
                                 m_provider, m_keystoreFormat );
        }
        finally
        {
            try
            {
                keyStream.close();
            }
            catch( IOException e )
            {
                // avoids hiding exceptions from makeContext
                // by catching this IOException
                getLogger().error( "Error keyStream.close failed", e );
            }
        }
    }

    public void dispose()
    {
        m_keystorePassword = null;
        m_keyPassword = null;
    }

    /**
     * Creates an SSL context which uses the keys and certificates
     * provided by the given <tt>keyStream</tt>.  For simplicity the
     * same key stream (keystore) is used for both key and trust
     * factory.
     *
     * @param keyStream to read the keys from
     * @param keystorePassword password for the keystore, can be null
     *                      if integrity verification is not desired
     * @param keyPassword passphrase which unlocks the keys in the key file
     *        (should really be a char[] so that it can be cleaned after use)
     * @param protocol the standard name of the requested protocol
     * @param provider the standard name of the requested algorithm
     * @param keystoreFormat the type of keystore
     *
     * @return context configured with these keys and certificates
     * @throws IOException if files can't be read
     * @throws GeneralSecurityException is something goes wrong inside
     *                                  cryptography framework
     */
    private static SSLContext makeContext( InputStream keyStream,
                                           String keystorePassword,
                                           String keyPassword,
                                           String protocol,
                                           String provider,
                                           String keystoreFormat )
        throws IOException, GeneralSecurityException
    {
        final KeyStore keystore = loadKeystore( keyStream,
                                                keystorePassword,
                                                keystoreFormat );
        final KeyManagerFactory kmf = KeyManagerFactory.getInstance( provider );
        // even though undocumented Sun's implementation doesn't allow
        // null passphrases, but zero sized arrays are OK
        final char[] passChars = ( keyPassword != null ) ?
            keyPassword.toCharArray() : new char[ 0 ];
        try
        {
            kmf.init( keystore, passChars );
        }
        finally
        {
            Arrays.fill( passChars, (char)0 );
        }

        final TrustManagerFactory tmf =
            TrustManagerFactory.getInstance( provider );
        tmf.init( keystore );

        final SSLContext result = SSLContext.getInstance( protocol );
        result.init( kmf.getKeyManagers(),
                     tmf.getTrustManagers(),
                     new java.security.SecureRandom() );
        return result;
    }

    /**
     * Builds a keystore loaded from the given stream. The passphrase
     * is used to verify the keystore file integrity.
     * @param keyStream to load from
     * @param passphrase for the store integrity verification (or null if
     *                   integrity check is not wanted)
     * @param keystoreFormat the type of keystore
     * @return loaded key store
     * @throws IOException if file can not be read
     * @throws GeneralSecurityException if key store can't be built
     */
    private static KeyStore loadKeystore( InputStream keyStream,
                                          String passphrase,
                                          String keystoreFormat )
        throws GeneralSecurityException, IOException
    {
        final KeyStore ks = KeyStore.getInstance( keystoreFormat );

        if( passphrase != null )
        {
            final char[] passChars = passphrase.toCharArray();
            try
            {
                ks.load( keyStream, passChars );
            }
            finally
            {
                Arrays.fill( passChars, (char)0 );
            }
        }
        else
        {
            ks.load( keyStream, null );
        }

        return ks;
    }
}
