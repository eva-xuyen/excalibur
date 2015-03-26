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

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import com.coyotegulch.jisp.KeyObject;

/**
 * Wrapper class for Keys to be compatible with the
 * Jisp KeyObject.
 * 
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version CVS $Id: JispKey.java,v 1.4 2004/02/28 11:47:31 cziegeler Exp $
 */
public final class JispKey extends KeyObject 
{
    final static long serialVersionUID = -1216913992804571313L;

    protected Object m_Key;

    static protected JispKey NULL_KEY = new JispKey("");
    
    public JispKey() {
        this("");
    }
    
    /**
     *  Constructor for the JispKey object
     *
     * @param keyValue the key
     */
    public JispKey(Object keyValue) 
    {
        m_Key = keyValue;
    }

    /**
     * Compares two Keys
     *
     * @param key the KeyObject to be compared
     * @return 0 if equal, 1 if greater, -1 if less
     */

    public int compareTo(KeyObject key) 
    {
        if (key instanceof JispKey) 
        {
            final JispKey other = (JispKey)key;
            if ( other.m_Key.hashCode() == m_Key.hashCode() ) 
            {
                if ( m_Key == other.m_Key || m_Key.equals(other.m_Key) ) 
                {
                    return KEY_EQUAL;
                }
                // we have the same hashcode, but different keys
                // this is usually an error condition, but we deal
                // with it anway
                // if they would have the same classname, they
                // can only have the same hashCode if they are equal:
                int comp = m_Key.getClass().getName().compareTo(other.m_Key.getClass().getName());
                if ( comp < 0 ) 
                {
                    return KEY_LESS;
                }
                return KEY_MORE;
            } 
            else 
            {
                if ( m_Key.hashCode() < other.m_Key.hashCode() ) 
                {
                    return KEY_LESS;
                }
                return KEY_MORE;
            }
        } 
        else 
        {
            return KEY_ERROR;
        }
    }

    /**
     *  Composes a null Kewy
     *
     * @return a null Key
     */
    public KeyObject makeNullKey() 
    {
        return NULL_KEY;
    }

    /**
     * The object implements the writeExternal method to save its contents
     * by calling the methods of DataOutput for its primitive values or
     * calling the writeObject method of ObjectOutput for objects, strings,
     * and arrays.
     *
     * @param out the stream to write the object to
     * @exception IOException
     */
    public void writeExternal(ObjectOutput out)
    throws IOException 
    {
        out.writeObject(m_Key);
    }

    /**
     * The object implements the readExternal method to restore its contents
     * by calling the methods of DataInput for primitive types and readObject
     * for objects, strings and arrays. The readExternal method must read the
     * values in the same sequence and with the same types as were written by writeExternal.
     *
     * @param in the stream to read data from in order to restore the object
     * @exception IOException
     * @exception ClassNotFoundException
     */
    public void readExternal(ObjectInput in)
    throws IOException, ClassNotFoundException 
    {
        m_Key = in.readObject();
    }

    /**
     * Return the real key
     */
    public Object getKey() 
    {
        return m_Key;
    }
}
