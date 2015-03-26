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

import org.apache.avalon.framework.configuration.ConfigurationException;

/**
 * A series of methods which are useful when working with InstrumentSamples.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version CVS $Revision: 1.4 $ $Date: 2004/02/28 11:47:19 $
 * @since 4.1
 */
public class InstrumentSampleUtils
{
    /**
     * Resolves an instrument sample type based on a name.
     *
     * @param type Type of the InstrumentSample to resolve.  Accepted values are:
     *              "max", "maximum", "min", "minimum", "mean", 
     *              "ctr", and "counter".
     *
     * @throws ConfigurationException if the specified sample type is unknown.
     */
    public static int resolveInstrumentSampleType( String type )
        throws ConfigurationException {
        
        if ( type.equalsIgnoreCase( "max" ) || type.equalsIgnoreCase( "maximum" ) )
        {
            return InstrumentSampleElementData.INSTRUMENT_SAMPLE_TYPE_MAXIMUM;
        }
        else if ( type.equalsIgnoreCase( "min" ) || type.equalsIgnoreCase( "minimum" ) )
        {
            return InstrumentSampleElementData.INSTRUMENT_SAMPLE_TYPE_MINIMUM;
        }
        else if ( type.equalsIgnoreCase( "mean" ) )
        {
            return InstrumentSampleElementData.INSTRUMENT_SAMPLE_TYPE_MEAN;
        }
        else if ( type.equalsIgnoreCase( "ctr" ) || type.equalsIgnoreCase( "counter" ) )
        {
            return InstrumentSampleElementData.INSTRUMENT_SAMPLE_TYPE_COUNTER;
        }
        else
        {
            throw new ConfigurationException( "'" + type + "' is not a valid sample type." );
        }
    }
    
    public static String getInstrumentSampleTypeName( int type )
    {
        switch ( type )
        {
        case InstrumentSampleElementData.INSTRUMENT_SAMPLE_TYPE_MAXIMUM:
            return "maximum";
            
        case InstrumentSampleElementData.INSTRUMENT_SAMPLE_TYPE_MINIMUM:
            return "minimum";
        
        case InstrumentSampleElementData.INSTRUMENT_SAMPLE_TYPE_MEAN:
            return "mean";
            
        case InstrumentSampleElementData.INSTRUMENT_SAMPLE_TYPE_COUNTER:
            return "counter";
            
        default:
            return "unknown-" + type;
        }
    }
    
    /**
     * Generates a sample name given its parameters.
     *
     * @param sampleType Type of the sample.
     * @param sampleInterval Interval of the sample.
     * @param sampleSize Size of the sample.
     *
     * @return A sample name.
     */
    public static String generateInstrumentSampleName( int sampleType,
                                                       long sampleInterval,
                                                       int sampleSize )
    {
        return getInstrumentSampleTypeName( sampleType ) + "_" + 
            sampleInterval + "_" + sampleSize;
    }
    
    /**
     * Generates a fully qualified sample name given its parameters.
     *
     * @param instrumentName Name of the instrument which owns the sample.
     * @param sampleType Type of the sample.
     * @param sampleInterval Interval of the sample.
     * @param sampleSize Size of the sample.
     *
     * @return A fully qualified sample name.
     */
    public static String generateFullInstrumentSampleName( String instrumentName,
                                                           int sampleType,
                                                           long sampleInterval,
                                                           int sampleSize )
    {
        return instrumentName + "." +
            generateInstrumentSampleName( sampleType, sampleInterval, sampleSize );
    }
    
    /**
     * Returns the default description for a given type.
     *
     * @param type Whose description is being requested.
     * @param interval Interval of the Sample.
     *
     * @return The description.
     */
    public static String getDefaultDescriptionForType( int type, long interval )
    {
        StringBuffer sb = new StringBuffer();
        switch ( type )
        {
        case InstrumentSampleElementData.INSTRUMENT_SAMPLE_TYPE_COUNTER:
            sb.append( "Count each " );
            break;
            
        case InstrumentSampleElementData.INSTRUMENT_SAMPLE_TYPE_MAXIMUM:
            sb.append( "Max Value each " );
            break;
            
        case InstrumentSampleElementData.INSTRUMENT_SAMPLE_TYPE_MEAN:
            sb.append( "Mean Value each " );
            break;
            
        case InstrumentSampleElementData.INSTRUMENT_SAMPLE_TYPE_MINIMUM:
            sb.append( "Min Value each " );
            break;
            
        default:
            return "Unknown Type " + type;
        }
        
        if ( interval == 1000 )
        {
            sb.append( "Second" );
        }
        else if ( interval == 60000 )
        {
            sb.append( "Minute" );
        }
        else if ( interval == 3600000 )
        {
            sb.append( "Hour" );
        }
        else if ( interval == 86400000 )
        {
            sb.append( "Day" );
        }
        else if ( interval % 86400000 == 0 )
        {
            sb.append( interval / 86400000 );
            sb.append( " Days" );
        }
        else if ( interval % 3600000 == 0 )
        {
            sb.append( interval / 3600000 );
            sb.append( " Hours" );
        }
        else if ( interval % 60000 == 0 )
        {
            sb.append( interval / 60000 );
            sb.append( " Minutes" );
        }
        else if ( interval % 1000 == 0 )
        {
            sb.append( interval / 1000 );
            sb.append( " Seconds" );
        }
        else
        {
            sb.append( interval );
            sb.append( " Milliseconds" );
        }
        
        return sb.toString();
    }
}
