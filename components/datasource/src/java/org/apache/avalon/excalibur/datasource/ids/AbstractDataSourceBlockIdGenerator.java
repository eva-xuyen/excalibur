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

package org.apache.avalon.excalibur.datasource.ids;

import java.math.BigDecimal;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;

/**
 * The AbstractDataSourceBlockIdGenerator allocates blocks of ids from a DataSource
 *  and then provides them as needed.  This is useful in reducing communication with
 *  the DataSource.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version CVS $Revision: 1.4 $ $Date: 2004/02/28 11:47:17 $
 * @since 4.1
 */
public abstract class AbstractDataSourceBlockIdGenerator
    extends AbstractDataSourceIdGenerator
{
    /**
     * The first id in a batch of Ids loaded in from the DataSource.
     */
    private BigDecimal m_firstBigDecimal;

    /**
     * The first id in a batch of Ids loaded in from the DataSource.
     */
    private long m_firstLong;

    /**
     * The number of ids loaded in each block.
     */
    private int m_blockSize;

    /**
     * The number of ids which have been allocated from the current block.
     */
    private int m_allocated;

    /*---------------------------------------------------------------
     * Constructors
     *-------------------------------------------------------------*/
    public AbstractDataSourceBlockIdGenerator()
    {
    }

    /*---------------------------------------------------------------
     * Methods
     *-------------------------------------------------------------*/
    /**
     * Allocates a block, of the given size, of ids from the database.
     *
     * @param blockSize number of Ids which are to be allocated.
     *
     * @return The first id in the allocated block.
     *
     * @throws IdException if there it was not possible to allocate a block of ids.
     */
    protected abstract BigDecimal allocateBigDecimalIdBlock( int blockSize )
        throws IdException;

    /**
     * Allocates a block, of the given size, of ids from the database.
     *
     * @param blockSize number of Ids which are to be allocated.
     *
     * @return The first id in the allocated block.
     *
     * @throws IdException if there it was not possible to allocate a block of ids.
     */
    protected abstract long allocateLongIdBlock( int blockSize )
        throws IdException;

    /*---------------------------------------------------------------
     * AbstractIdGenerator Methods
     *-------------------------------------------------------------*/
    /**
     * Gets the next id as a Big Decimal.  This method will only be called
     *  when synchronized and when the data type is configured to be BigDecimal.
     *
     * @return the next id as a BigDecimal.
     *
     * @throws IdException if an Id could not be allocated for any reason.
     */
    protected BigDecimal getNextBigDecimalIdInner()
        throws IdException
    {
        if( m_allocated >= m_blockSize )
        {
            // Need to allocate a new batch of ids
            try
            {
                m_firstBigDecimal = allocateBigDecimalIdBlock( m_blockSize );

                // Reset the allocated count
                m_allocated = 0;
            }
            catch( IdException e )
            {
                // Set the allocated count to signal that there are not any ids available.
                m_allocated = Integer.MAX_VALUE;
                throw e;
            }
        }

        // We know that at least one id is available.
        // Get an id out of the currently allocated block.
        BigDecimal id = m_firstBigDecimal.add( new BigDecimal( m_allocated ) );
        m_allocated++;

        return id;
    }

    /**
     * Gets the next id as a long.  This method will only be called
     *  when synchronized and when the data type is configured to be long.
     *
     * @return the next id as a long.
     *
     * @throws IdException if an Id could not be allocated for any reason.
     */
    protected long getNextLongIdInner()
        throws IdException
    {
        if( m_allocated >= m_blockSize )
        {
            // Need to allocate a new batch of ids
            try
            {
                m_firstLong = allocateLongIdBlock( m_blockSize );

                // Reset the allocated count
                m_allocated = 0;
            }
            catch( IdException e )
            {
                // Set the allocated count to signal that there are not any ids available.
                m_allocated = Integer.MAX_VALUE;
                throw e;
            }
        }

        // We know that at least one id is available.
        // Get an id out of the currently allocated block.
        long id = m_firstLong + m_allocated;
        if( id < 0 )
        {
            // The value wrapped
            String msg = "No more Ids are available, the maximum long value has been reached.";
            getLogger().error( msg );
            throw new IdException( msg );
        }
        m_allocated++;

        return id;
    }

    /*---------------------------------------------------------------
     * Configurable Methods
     *-------------------------------------------------------------*/
    /**
     * Called by the Container to configure the component.
     *
     * @param configuration configuration info used to setup the component.
     *
     * @throws ConfigurationException if there are any problems with the configuration.
     */
    public void configure( Configuration configuration )
        throws ConfigurationException
    {
        super.configure( configuration );

        // Obtain the block size.
        m_blockSize = configuration.getAttributeAsInteger( "block-size", 10 );
    }

    /*---------------------------------------------------------------
     * Initializable Methods
     *-------------------------------------------------------------*/
    /**
     * Called by the Container to initialize the component.
     *
     * @throws Exception if there were any problems durring initialization.
     */
    public void initialize()
        throws Exception
    {
        super.initialize();

        // Set the state so that the first request for an id will load in a block of ids.
        m_allocated = Integer.MAX_VALUE;
    }
}

