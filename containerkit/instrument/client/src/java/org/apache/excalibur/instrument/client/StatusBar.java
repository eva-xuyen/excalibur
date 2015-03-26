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

package org.apache.excalibur.instrument.client;

import java.awt.BorderLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * This class defines the status bar at the bottom of the main frame.
 *  It is used to display information to the user.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version CVS $Revision: 1.4 $ $Date: 2004/02/28 11:47:23 $
 * @since 4.1
 */
class StatusBar extends JPanel
{
    private JLabel m_statusLabel;
    
    /*---------------------------------------------------------------
     * Constructors
     *-------------------------------------------------------------*/
    StatusBar()
    {
        setLayout( new BorderLayout() );
        m_statusLabel = new JLabel( " " );
        add( m_statusLabel, BorderLayout.CENTER );
    }
    
    /*---------------------------------------------------------------
     * Methods
     *-------------------------------------------------------------*/
    void setStatusMessage( String message )
    {
        // If the message is of 0 length, then the status bar will collapse.
        if ( ( message == null ) || ( message.length() < 1 ) )
        {
            message = " ";
        }
        
        if ( !message.equals( m_statusLabel.getText() ) )
        {
            m_statusLabel.setText( message );
            m_statusLabel.invalidate();
            validate();
        }
    }
}
