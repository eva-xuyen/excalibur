/* 
 * Copyright 2002-2004 The Apache Software Foundation
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at 
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
package org.apache.excalibur.store.impl;

import java.io.File;
import java.io.IOException;

import com.coyotegulch.jisp.BTreeIndex;
import com.coyotegulch.jisp.IndexedObjectDatabase;
import com.coyotegulch.jisp.KeyNotFound;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.parameters.ParameterException;
import org.apache.avalon.framework.parameters.Parameterizable;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.excalibur.store.Store;

/**
 * This store is based on the Jisp library
 * (http://www.coyotegulch.com/jisp/index.html). This store uses B-Tree indexes
 * to access variable-length serialized data stored in files.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version CVS $Id: JispFilesystemStore.java,v 1.4 2004/02/28 11:47:31 cziegeler Exp $
 */
public class JispFilesystemStore extends AbstractJispFilesystemStore
    implements Store,
               ThreadSafe,
               Parameterizable,
               Disposable {

    /**
     *  Configure the Component.<br>
     *  A few options can be used
     *  <UL>
     *    <LI> directory - The directory to store the two files belowe
     *    </LI>
     *    <LI> data-file = the name of the data file (Default: store.dat)
     *    </LI>
     *    <LI> index-file = the name of the index file (Default: store.idx)
     *    </LI>
     *    <LI> order = The page size of the B-Tree</LI>
     *  </UL>
     *
     * @param params the configuration paramters
     * @exception  ParameterException
     */
     public void parameterize(Parameters params) throws ParameterException
     {
        // get the directory to use
        try 
        {
            final String dir = params.getParameter("directory");
            this.setDirectory(new File(dir));
        } 
        catch (IOException e) 
        {
            throw new ParameterException("Unable to set directory", e);
        }

        final String databaseName = params.getParameter("data-file", "store.dat");
        final String indexName = params.getParameter("index-file", "store.idx");
        final int order = params.getParameterAsInteger("order", 301);
        if (getLogger().isDebugEnabled()) 
        {
            getLogger().debug("Database file name = " + databaseName);
            getLogger().debug("Index file name = " + indexName);
            getLogger().debug("Order=" + order);
        }

        final File databaseFile = new File(m_directoryFile, databaseName);
        final File indexFile = new File(m_directoryFile, indexName);

        if (getLogger().isDebugEnabled()) 
        {
            getLogger().debug("Initializing JispFilesystemStore");
        }

        try
        {
            final boolean isOld = databaseFile.exists();
            if (getLogger().isDebugEnabled()) 
            {
                getLogger().debug("initialize(): Datafile exists: " + isOld);
            }

            if (!isOld) {
                m_Index = new BTreeIndex(indexFile.toString(),
                                         order, super.getNullKey(), false);
            } else {
                m_Index = new BTreeIndex(indexFile.toString());
            }
            m_Database = new IndexedObjectDatabase(databaseFile.toString(), !isOld);
            m_Database.attachIndex(m_Index);
        } 
        catch (KeyNotFound ignore) 
        {
        } 
        catch (Exception e) 
        {
            getLogger().error("initialize(..) Exception", e);
        }
    }

    public void dispose()
    {
        try
        {
            getLogger().debug("Disposing");

            if (m_Index != null)
            {
                m_Index.close();
            }

            if (m_Database != null)
            {
                m_Database.close();
            }
        }
        catch (Exception e) 
        {
            getLogger().error("dispose(..) Exception", e);
        }
    }
}
