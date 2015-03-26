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

package org.apache.excalibur.instrument.manager.http;

import java.io.IOException;
import java.io.PrintWriter;

import org.apache.excalibur.instrument.manager.InstrumentableDescriptor;
import org.apache.excalibur.instrument.manager.InstrumentDescriptor;
import org.apache.excalibur.instrument.manager.DefaultInstrumentManager;
import org.apache.excalibur.instrument.manager.InstrumentSampleDescriptor;
import org.apache.excalibur.instrument.manager.InstrumentSampleSnapshot;

/**
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version CVS $Revision: 1.6 $ $Date: 2004/03/06 14:01:28 $
 * @since 4.1
 */
public abstract class AbstractXMLHandler
    extends AbstractHandler
{
    protected static final String INDENT = "  ";
    
    /*---------------------------------------------------------------
     * Constructors
     *-------------------------------------------------------------*/
    /**
     * Creates a new AbstractXMLHandler.
     *
     * @param path The path handled by this handler.
     * @param manager Reference to the instrument manager interface.
     */
    public AbstractXMLHandler( String path, DefaultInstrumentManager manager )
    {
        super( path, CONTENT_TYPE_TEXT_XML, manager );
    }
    
    /*---------------------------------------------------------------
     * Methods
     *-------------------------------------------------------------*/
    protected String makeSafeAttribute( String attribute )
    {
        // TODO:
        return attribute;
    }
    
    protected void outputLine( PrintWriter out, String indent, boolean packed, String line )
    {
        if ( !packed )
        {
            out.print( indent );
        }
        out.print( line );
        if ( !packed )
        {
            out.println();
        }
    }
    
    protected void outputInstrumentManager( PrintWriter out,
                                            DefaultInstrumentManager manager,
                                            String indent,
                                            boolean recurse,
                                            boolean packed,
                                            boolean readOnly )
        throws IOException
    {
        outputLine( out, indent, packed, "<instrument-manager "
            + "name=\"" + makeSafeAttribute( manager.getName() ) + "\" "
            + "description=\"" + makeSafeAttribute( manager.getDescription() ) + "\" "
            + "state-version=\"" + manager.getStateVersion() + "\" "
            + "batched-updates=\"true\" read-only=\"" + readOnly + "\">" );
        
        String childIndent = indent + INDENT;
        
        InstrumentableDescriptor[] instrumentables = manager.getInstrumentableDescriptors();
        for ( int i = 0; i < instrumentables.length; i++ )
        {
            InstrumentableDescriptor instrumentable = instrumentables[i];
            if ( recurse )
            {
                outputInstrumentable( out, instrumentable, childIndent, recurse, packed );
            }
            else
            {
                outputInstrumentableBrief( out, instrumentable, childIndent, packed );
            }
        }
        
        outputLine( out, indent, packed, "</instrument-manager>" );
    }
    
    protected void outputInstrumentableBrief( PrintWriter out,
                                              InstrumentableDescriptor instrumentable,
                                              String indent,
                                              boolean packed )
        throws IOException
    {
        outputLine( out, indent, packed, "<instrumentable "
            + "name=\"" + makeSafeAttribute( instrumentable.getName() ) + "\" "
            + "state-version=\"" + instrumentable.getStateVersion() + "\"/>" );
    }
    
    protected void outputInstrumentable( PrintWriter out,
                                         InstrumentableDescriptor instrumentable,
                                         String indent,
                                         boolean recurse,
                                         boolean packed )
        throws IOException
    {
        InstrumentableDescriptor[] instrumentables =
            instrumentable.getChildInstrumentableDescriptors();
        InstrumentDescriptor[] instruments = instrumentable.getInstrumentDescriptors();
        
        String terminator;
        if ( ( instrumentables.length > 0 ) || ( instruments.length > 0 ) )
        {
            terminator = ">";
        }
        else
        {
            terminator = "/>";
        }
                                            
        outputLine( out, indent, packed, "<instrumentable "
            + "name=\"" + makeSafeAttribute( instrumentable.getName() ) + "\" "
            + "description=\"" + makeSafeAttribute( instrumentable.getDescription() ) + "\" "
            + "state-version=\"" + instrumentable.getStateVersion() + "\" "
            + "registered=\"" + instrumentable.isRegistered() + "\" "
            + "configured=\"" + instrumentable.isConfigured() + "\"" + terminator );
        
        if ( ( instrumentables.length > 0 ) || ( instruments.length > 0 ) )
        {
            String childIndent = indent + INDENT;
            
            for ( int i = 0; i < instrumentables.length; i++ )
            {
                InstrumentableDescriptor child = instrumentables[i];
                if ( recurse )
                {
                    outputInstrumentable( out, child, childIndent, recurse, packed );
                }
                else
                {
                    outputInstrumentableBrief( out, child, childIndent, packed );
                }
            }
            
            for ( int i = 0; i < instruments.length; i++ )
            {
                InstrumentDescriptor instrument = instruments[i];
                if ( recurse )
                {
                    outputInstrument( out, instrument, childIndent, recurse, packed );
                }
                else
                {
                    outputInstrumentBrief( out, instrument, childIndent, packed );
                }
            }
            
            outputLine( out, indent, packed, "</instrumentable>" );
        }
    }
    
    protected void outputInstrumentBrief( PrintWriter out,
                                          InstrumentDescriptor instrument,
                                          String indent,
                                          boolean packed )
        throws IOException
    {
        outputLine( out, indent, packed, "<instrument "
            + "name=\"" + makeSafeAttribute( instrument.getName() ) + "\" "
            + "state-version=\"" + instrument.getStateVersion() + "\"/>" );
    }
    
    protected void outputInstrument( PrintWriter out,
                                     InstrumentDescriptor instrument,
                                     String indent,
                                     boolean recurse,
                                     boolean packed )
        throws IOException
    {
        InstrumentSampleDescriptor[] samples = instrument.getInstrumentSampleDescriptors();
        
        String terminator;
        if ( samples.length > 0 )
        {
            terminator = ">";
        }
        else
        {
            terminator = "/>";
        }
        
        outputLine( out, indent, packed, "<instrument "
            + "name=\"" + makeSafeAttribute( instrument.getName() ) + "\" "
            + "description=\"" + makeSafeAttribute( instrument.getDescription() ) + "\" "
            + "type=\"" + instrument.getType() + "\" "
            + "state-version=\"" + instrument.getStateVersion() + "\" "
            + "registered=\"" + instrument.isRegistered() + "\" "
            + "configured=\"" + instrument.isConfigured() + "\"" + terminator );
        
        if ( samples.length > 0 )
        {
            String childIndent = indent + INDENT;
            
            for ( int i = 0; i < samples.length; i++ )
            {
                InstrumentSampleDescriptor sample = samples[i];
                if ( recurse )
                {
                    outputSample( out, sample, childIndent, packed );
                }
                else
                {
                    outputSampleBrief( out, sample, childIndent, packed );
                }
            }
            
            outputLine( out, indent, packed, "</instrument>" );
        }
    }
    
    protected void outputSampleBrief( PrintWriter out,
                                      InstrumentSampleDescriptor sample,
                                      String indent,
                                      boolean packed )
        throws IOException
    {
        outputLine( out, indent, packed, "<sample "
            + "name=\"" + makeSafeAttribute( sample.getName() ) + "\" "
            + "state-version=\"" + sample.getStateVersion() + "\"/>" );
    }
    
    protected void outputSample( PrintWriter out,
                                 InstrumentSampleDescriptor sample,
                                 String indent,
                                 boolean packed )
        throws IOException
    {
        outputLine( out, indent, packed, "<sample "
            + "name=\"" + makeSafeAttribute( sample.getName() ) + "\" "
            + "description=\"" + makeSafeAttribute( sample.getDescription() ) + "\" "
            + "type=\"" + sample.getType() + "\" "
            + "interval=\"" + sample.getInterval() + "\" "
            + "size=\"" + sample.getSize() + "\" "
            + "value=\"" + sample.getValue() + "\" "
            + "time=\"" + sample.getTime() + "\" "
            + "expiration-time=\"" + sample.getLeaseExpirationTime() + "\" "
            + "state-version=\"" + sample.getStateVersion() + "\" "
            + "configured=\"" + sample.isConfigured() + "\"/>" );
    }
    
    protected void outputSampleHistory( PrintWriter out,
                                        InstrumentSampleDescriptor sample,
                                        String indent,
                                        long baseTime,
                                        boolean packed,
                                        boolean compact )
        throws IOException
    {
        InstrumentSampleSnapshot snapshot = sample.getSnapshot();
        int[] values = snapshot.getSamples();
        
        // Given the base time, decide on the first value index and this time which
        //  will be included.
        long firstTime = snapshot.getTime() - ( snapshot.getSize() - 1 ) * snapshot.getInterval();
        int firstIndex;
        if ( baseTime <= firstTime )
        {
            firstIndex = 0;
        }
        else if ( baseTime >= snapshot.getTime() )
        {
            firstTime = snapshot.getTime();
            firstIndex = values.length - 1;
        }
        else
        {
            int count = (int)Math.ceil(
                ( (double)snapshot.getTime() - baseTime ) / snapshot.getInterval() ) + 1;
            firstTime = snapshot.getTime() - ( count - 1 ) * snapshot.getInterval();
            firstIndex = values.length - count;
        }
        
        // Where possible, display values from the snapshot rather than the sample
        //  to avoid any synchronization issues.
        outputLine( out, indent, packed, "<sample "
            + "name=\"" + makeSafeAttribute( sample.getName() ) + "\" "
            + "description=\"" + makeSafeAttribute( sample.getDescription() ) + "\" "
            + "type=\"" + sample.getType() + "\" "
            + "interval=\"" + snapshot.getInterval() + "\" "
            + "size=\"" + snapshot.getSize() + "\" "
            + "value=\"" + values[values.length - 1] + "\" "
            + "time=\"" + snapshot.getTime() + "\" "
            + "first-time=\"" + firstTime + "\" "
            + "count=\"" + ( values.length - firstIndex ) + "\" "
            + "expiration-time=\"" + sample.getLeaseExpirationTime() + "\" "
            + "state-version=\"" + snapshot.getStateVersion() + "\" "
            + "configured=\"" + sample.isConfigured() + "\">" );
        
        String childIndent = indent + INDENT;
        
        if ( compact )
        {
            // Output the values as a comma separated list.
            StringBuffer sb = new StringBuffer();
            sb.append( "<values>" );
            for ( int i = firstIndex; i < values.length; i++ )
            {
                if ( i > firstIndex )
                {
                    sb.append( "," );
                }
                sb.append( values[i] );
            }
            sb.append( "</values>" );
            
            outputLine( out, childIndent, packed, sb.toString() );
        }
        else
        {
            // Output an element for each value.
            long interval = snapshot.getInterval();
            long time = firstTime;
            for ( int i = firstIndex; i < values.length; i++ )
            {
                outputLine( out, childIndent, packed,
                    "<value time=\"" + time + "\" value=\"" + values[i] + "\"/>" );
                time += interval;
            }
        }
        
        outputLine( out, indent, packed, "</sample>" );
    }
}

