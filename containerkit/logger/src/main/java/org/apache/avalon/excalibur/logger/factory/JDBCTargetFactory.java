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
package org.apache.avalon.excalibur.logger.factory;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import org.apache.avalon.excalibur.logger.LogTargetFactory;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.log.LogTarget;
import org.apache.log.output.db.ColumnInfo;
import org.apache.log.output.db.ColumnType;
import org.apache.log.output.db.DefaultJDBCTarget;
import org.apache.log.output.db.NormalizedJDBCTarget;

/**
 * Factory for JDBCLogTarget-s. The configuration looks like this:
 *
 * <pre>
 *  &lt;jdbc id="database"&gt;
 *      &lt;datasource&gt;java:/LogTargetDataSource&lt;/datasource&gt;
 *      &lt;normalized&gt;true&lt;/normalized&gt;
 *      &lt;table name="LOG"&gt;
 *          &lt;category&gt;CATEGORY&lt;/category&gt;
 *          &lt;priority&gt;PRIORITY&lt;/priority&gt;
 *          &lt;message&gt;MESSAGE&lt;/message&gt;
 *          &lt;time&gt;TIME&lt;/time&gt;
 *          &lt;rtime&gt;RTIME&lt;/rtime&gt;
 *          &lt;throwable&gt;THROWABLE&lt;/throwable&gt;
 *          &lt;hostname&gt;HOSTNAME&lt;/hostname&gt;
 *          &lt;static aux="-"&gt;STATIC&lt;/static&gt;
 *          &lt;context aux="principal"&gt;PRINCIPAL&lt;/context&gt;
 *          &lt;context aux="ipaddress"&gt;IPADDRESS&lt;/context&gt;
 *          &lt;context aux="username"&gt;USERNAME&lt;/context&gt;
 *      &lt;/table&gt;
 *  &lt;/jdbc&gt;
 * </pre>
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version CVS $Revision: 1.10 $ $Date: 2004/03/10 13:54:50 $
 */
public class JDBCTargetFactory implements LogTargetFactory
{
    public LogTarget createTarget( Configuration configuration )
        throws ConfigurationException
    {
        final String dataSourceName =
            configuration.getChild( "datasource", true ).getValue();

        final boolean normalized =
            configuration.getChild( "normalized", true ).getValueAsBoolean( false );

        final Configuration tableConfiguration =
            configuration.getChild( "table" );

        final String table = tableConfiguration.getAttribute( "name" );

        final Configuration[] conf = tableConfiguration.getChildren();
        final ColumnInfo[] columns = new ColumnInfo[ conf.length ];

        for( int i = 0; i < conf.length; i++ )
        {
            final String name = conf[ i ].getValue();
            final int type = ColumnType.getTypeIdFor( conf[ i ].getName() );
            final String aux = conf[ i ].getAttribute( "aux", null );

            columns[ i ] = new ColumnInfo( name, type, aux );
        }

        final DataSource dataSource;

        try
        {
            Context ctx = new InitialContext();
            dataSource = (DataSource)ctx.lookup( dataSourceName );
        }
        catch( final NamingException ne )
        {
            throw new ConfigurationException( "Cannot lookup data source", ne );
        }

        final LogTarget logTarget;
        if( normalized )
        {
            logTarget = new NormalizedJDBCTarget( dataSource, table, columns );
        }
        else
        {
            logTarget = new DefaultJDBCTarget( dataSource, table, columns );
        }

        return logTarget;
    }
}
