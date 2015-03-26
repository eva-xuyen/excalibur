/* 
 * Copyright 2003-2004 The Apache Software Foundation
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

package org.apache.avalon.fortress.util;

import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.parameters.Parameterizable;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.lifecycle.AbstractCreator;
import org.apache.commons.beanutils.BeanUtils;

/**
 * A creator that configures a component based on reflection.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version CVS $Revision: 1.1 $ $Date: 2004/03/08 18:50:25 $
 */
public final class ReflectiveConfigurator
    extends AbstractCreator
{

	/* (non-Javadoc)
	 * @see org.apache.avalon.lifecycle.Creator#create(java.lang.Object, org.apache.avalon.framework.context.Context)
	 */
	public void create(Object component, Context context) 
    throws Exception 
    {
        super.create( component, context );
        
        if ( !(component instanceof Parameterizable)
              && !(component instanceof Configurable) ) 
        {
            final Configuration conf = (Configuration) context.get("component.configuration");
            if ( conf != null && conf.getChildren().length > 0) 
            {
                final Parameters p = Parameters.fromConfiguration( conf );
                String[] names = p.getNames();
                for( int i = 0; i < names.length; i++ ) 
                {
                    try 
                    {
                        BeanUtils.setProperty( component, names[i], p.getParameter(names[i]));
                    } 
                    catch (Exception ignore)                    
                    {
                        if ( this.getLogger() != null && this.getLogger().isWarnEnabled() ) 
                        {
                            this.getLogger().warn("Error while trying to configure " + component 
                                     + " with parameter: " + names[i], ignore);   
                        }
                    }
                }
            }
        }
	}
    
}
