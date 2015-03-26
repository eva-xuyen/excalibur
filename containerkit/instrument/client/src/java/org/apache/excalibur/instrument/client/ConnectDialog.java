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

import java.awt.Component;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.JOptionPane;
import javax.swing.JTextField;

/**
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version CVS $Revision: 1.4 $ $Date: 2004/02/28 11:47:23 $
 * @since 4.1
 */
class ConnectDialog
    extends AbstractTabularOptionDialog
{
    private JTextField m_urlField;
    private URL m_url;
    
    /*---------------------------------------------------------------
     * Constructors
     *-------------------------------------------------------------*/
    /**
     * Creates a new ConnectDialog.
     *
     * @param frame Frame which owns the dialog.
     */
    ConnectDialog( InstrumentClientFrame frame )
    {
        super( frame, "Connect to Remote Instrument Manager",
            AbstractOptionDialog.BUTTON_OK | AbstractOptionDialog.BUTTON_CANCEL );
    }
    
    /*---------------------------------------------------------------
     * AbstractOptionDialog Methods
     *-------------------------------------------------------------*/
    /**
     * Returns the message to show at the top of the dialog.
     *
     * @return The text of the message.
     */
    protected String getMessage()
    {
        return "Please enter the url of the InstrumentManager to connect to.";
    }
    
    /**
     * Goes through and validates the fields in the dialog.
     *
     * @return True if the fields were Ok.
     */
    protected boolean validateFields()
    {
        // Check the URL.
        URL url;
        try
        {
            url = new URL( m_urlField.getText().trim() );
        }
        catch ( MalformedURLException e )
        {
            JOptionPane.showMessageDialog( this, "Please enter a valid url: " + e.getMessage(),
                "Invalid URL", JOptionPane.ERROR_MESSAGE );
            return false;
        }
        m_url = url;
        
        return true;
    }
    
    /*---------------------------------------------------------------
     * AbstractTabularOptionDialog Methods
     *-------------------------------------------------------------*/
    /**
     * Returns an array of labels to use for the components returned from
     *  getMainPanelComponents().
     *
     * @returns An array of labels.
     */
    protected String[] getMainPanelLabels()
    {
        return new String[]
        {
            "URL:"
        };
    }
    
    /**
     * Returns an array of components to show in the main panel of the dialog.
     *
     * @returns An array of components.
     */
    protected Component[] getMainPanelComponents()
    {
        m_urlField = new JTextField();
        m_urlField.setColumns( 30 );
        
        return new Component[]
        {
            m_urlField
        };
    }
        
    /*---------------------------------------------------------------
     * Methods
     *-------------------------------------------------------------*/
    /**
     * Sets the initial URL to be shown in the URL TextField.
     *
     * @param url The initial URL.
     */
    void setURL( URL url )
    {
        m_url = url;
        m_urlField.setText( url.toExternalForm() );
    }
    
    /**
     * Returns the URL set in the dialog.
     *
     * @return The URL.
     */
    URL getURL()
    {
        return m_url;
    }
}

