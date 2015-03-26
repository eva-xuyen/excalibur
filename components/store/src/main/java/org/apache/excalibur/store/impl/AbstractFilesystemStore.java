/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.excalibur.store.impl;

import java.io.*;
import java.util.BitSet;
import java.util.Enumeration;

import EDU.oswego.cs.dl.util.concurrent.Sync;

/**
 * Stores objects on the filesystem: String objects as text files,
 * all other objects are serialized. This class must be subclassed
 * in order to set the directory the store should work on.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version CVS $Id: AbstractFilesystemStore.java,v 1.4 2004/02/28 11:47:31 cziegeler Exp $
 */
public abstract class AbstractFilesystemStore
extends AbstractReadWriteStore {

    /** The directory repository */
    protected File m_directoryFile;
    protected volatile String m_directoryPath;

    /**
     * Sets the repository's location
     */
    public void setDirectory(final String directory)
    throws IOException 
    {
        this.setDirectory(new File(directory));
    }

    /**
     * Sets the repository's location
     */
    public void setDirectory(final File directory)
    throws IOException 
    {
        this.m_directoryFile = directory;

        /* Save directory path prefix */
        this.m_directoryPath = this.getFullFilename(this.m_directoryFile);
        this.m_directoryPath += File.separator;

        /* Does directory exist? */
        if (!this.m_directoryFile.exists()) 
        {
            /* Create it anew */
            if (!this.m_directoryFile.mkdir()) 
            {
                throw new IOException(
                "Error creating store directory '" + this.m_directoryPath + "': ");
            }
        }

        /* Is given file actually a directory? */
        if (!this.m_directoryFile.isDirectory()) 
        {
            throw new IOException("'" + this.m_directoryPath + "' is not a directory");
        }

        /* Is directory readable and writable? */
        if (!(this.m_directoryFile.canRead() && this.m_directoryFile.canWrite())) 
        {
            throw new IOException(
                "Directory '" + this.m_directoryPath + "' is not readable/writable"
            );
        }
    }

    /**
     * Returns the repository's full pathname
     */
    public String getDirectoryPath() 
    {
        return this.m_directoryPath;
    }

    /**
     * Get the File object associated with the given unique key name.
     */
    protected Object doGet(final Object key) 
    {
        final File file = fileFromKey(key);

        if (file != null && file.exists()) 
        {
            if (getLogger().isDebugEnabled()) 
            {
                getLogger().debug("Found file: " + key);
            }
            try 
            {
                return this.deserializeObject(file);
            } 
            catch (Exception any) {
                getLogger().error("Error during deseralization.", any);
            }
        } 
        else 
        {
            if (getLogger().isDebugEnabled()) 
            {
                getLogger().debug("NOT Found file: " + key);
            }
        }

        return null;
    }

    /**
     * Store the given object in a persistent state.
     * 1) Null values generate empty directories.
     * 2) String values are dumped to text files
     * 3) Object values are serialized
     */
    protected void doStore(final Object key, final Object value)
    throws IOException
     {
        final File file = fileFromKey(key);

        /* Create subdirectories as needed */
        final File parent = file.getParentFile();
        if (parent != null) 
        {
            parent.mkdirs();
        }

        /* Store object as file */
        if (value == null) 
        { /* Directory */
            if (file.exists()) 
            {
                if (!file.delete()) 
                { /* FAILURE */
                    getLogger().error("File cannot be deleted: " + file.toString());
                    return;
                }
            }

            file.mkdir();
        } 
        else if (value instanceof String) 
        {
            /* Text file */
            this.serializeString(file, (String) value);
        } 
        else 
        {
            /* Serialized Object */
            this.serializeObject(file, value);
        }
    }

    /**
     * Remove the object associated to the given key.
     */
    protected void doRemove(final Object key) 
    {
        final File file = fileFromKey(key);
        if (file != null) 
        {
            file.delete();
        }
    }

    /**
     * Clear the Store of all elements 
     */
    protected void doClear() 
    {
        Enumeration enumer = this.keys();
        while (enumer.hasMoreElements()) 
        {
            Object key = enumer.nextElement();
            if (key == null) 
            {
                continue;
            }
            this.remove(key);
        }
    }

    /**
     * Indicates if the given key is associated to a contained object.
     */
    protected boolean doContainsKey(final Object key) 
    {
        final File file = fileFromKey(key);
        if (file == null) 
        {
            return false;
        }
        return file.exists();
    }

    /**
     * Returns the list of stored files as an Enumeration of Files
     */
    protected Enumeration doGetKeys() 
    {
        final FSEnumeration enumer = new FSEnumeration();
        this.addKeys(enumer, this.m_directoryFile);
        return enumer;
    }

    /**
     * Returns count of the objects in the store, or -1 if could not be
     * obtained.
     */
    protected int doGetSize() 
    {
        return countKeys(this.m_directoryFile);
    }

    protected void addKeys(FSEnumeration enumer, File directory) 
    {
        final int subStringBegin = this.m_directoryFile.getAbsolutePath().length() + 1;
        final File[] files = directory.listFiles();
        for (int i=0; i<files.length; i++)
         {
            if (files[i].isDirectory()) 
            {
                this.addKeys(enumer, files[i]);
            } 
            else 
            {
                enumer.add(this.decode(files[i].getAbsolutePath().substring(subStringBegin)));
            }
        }
    }

    protected int countKeys(File directory) 
    {
        int count = 0;
        final File[] files = directory.listFiles();
        for (int i=0; i<files.length; i++) 
        {
            if (files[i].isDirectory()) 
            {
                count += this.countKeys(files[i]);
            } 
            else 
            {
                count ++;
            }
        }
        return count;
    }

    final class FSEnumeration implements Enumeration 
    {
        private String[] array;
        private int      index;
        private int      length;

        FSEnumeration() 
        {
            this.array = new String[16];
            this.length = 0;
            this.index = 0;
        }

        public void add(String key) 
        {
            if (this.length == array.length) 
            {
                String[] newarray = new String[this.length + 16];
                System.arraycopy(this.array, 0, newarray, 0, this.array.length);
                this.array = newarray;
            }
            this.array[this.length] = key;
            this.length++;
        }

        public boolean hasMoreElements() 
        {
            return (this.index < this.length);
        }

        public Object nextElement() 
        {
            if (this.hasMoreElements()) 
            {
                this.index++;
                return this.array[index-1];
            }
            return null;
        }
    }

    /* Utility Methods*/
    protected File fileFromKey(final Object key) 
    {
        File file = new File(this.m_directoryFile, this.encode(key.toString()));
        File parent = file.getParentFile();
        if (parent != null) parent.mkdirs();
        return file;
    }

    public String getString(final Object key)
    throws IOException 
    {
        final File file = this.fileFromKey(key);
        if (file != null) 
        {
            return this.deserializeString(file);
        }

        return null;
    }

    public void free() 
    {
        // if we ever implement this, we should implement doFree()
    }

    /* (non-Javadoc)
     * @see org.apache.excalibur.store.impl.AbstractReadWriteStore#doFree()
     */
    protected void doFree() 
    {
    }

    public synchronized Object getObject(final Object key)
    throws IOException, ClassNotFoundException
    {
        Sync sync = this.lock.writeLock();
        try
        {
            sync.acquire();
            try 
            {
                final File file = this.fileFromKey(key);
                if (file != null) {
                    return this.deserializeObject(file);
                }
            }
            finally 
            {
                sync.release();
            }
        }
        catch (InterruptedException ignore)
        {
        } 
        
        return null;
    }

    /**
     * Inverse of encode exept it do not use path.
     * So decode(encode(s) - m_path) = s.
     * In other words it returns a String that can be used as key to retive
     * the record contained in the 'filename' file.
     */
    protected String decode( String filename )
    {
        // if the key is longer than 127 bytes a File.separator
        // is added each 127 bytes
        if (filename.length() > 127) 
        {
            int c = filename.length() / 127;
            int pos = c * 127;
            StringBuffer out = new StringBuffer(filename);
            while (pos > 0) {
                out.delete(pos,pos+1);
                pos -= 127;
            }
            filename = out.toString();
        }
        // In JDK 1.4 this is deprecated, the new format is below
        return java.net.URLDecoder.decode( filename );
        // return java.net.URLDecoder.decode( filename, "UTF-8" );
    }

    /** A BitSet defining the characters which don't need encoding */
    static BitSet charactersDontNeedingEncoding;
    static final int characterCaseDiff = ('a' - 'A');

    /** Initialize the BitSet */
    static
    {
        charactersDontNeedingEncoding = new BitSet(256);
        int i;
        for (i = 'a'; i <= 'z'; i++)
        {
            charactersDontNeedingEncoding.set(i);
        }
        for (i = 'A'; i <= 'Z'; i++)
        {
            charactersDontNeedingEncoding.set(i);
        }
        for (i = '0'; i <= '9'; i++)
        {
            charactersDontNeedingEncoding.set(i);
        }
        charactersDontNeedingEncoding.set('-');
        charactersDontNeedingEncoding.set('_');
        charactersDontNeedingEncoding.set('(');
        charactersDontNeedingEncoding.set(')');
    }

    /**
     * Returns a String that uniquely identifies the object.
     * <b>Note:</b> since this method uses the Object.toString()
     * method, it's up to the caller to make sure that this method
     * doesn't change between different JVM executions (like
     * it may normally happen). For this reason, it's highly recommended
     * (even if not mandated) that Strings be used as keys.
     */
    protected String encode(String s) 
    {
        final StringBuffer out = new StringBuffer( s.length() );
        final ByteArrayOutputStream buf = new ByteArrayOutputStream( 32 );
        final OutputStreamWriter writer = new OutputStreamWriter( buf );
        for (int i = 0; i < s.length(); i++)
        {
            int c = s.charAt(i);
            if (charactersDontNeedingEncoding.get(c))
            {
                out.append((char)c);
            }
            else
            {
                try
                {
                    writer.write(c);
                    writer.flush();
                }
                catch(IOException e)
                {
                    buf.reset();
                    continue;
                }
                byte[] ba = buf.toByteArray();
                for (int j = 0; j < ba.length; j++)
                {
                    out.append('%');
                    char ch = Character.forDigit((ba[j] >> 4) & 0xF, 16);
                    // converting to use uppercase letter as part of
                    // the hex value if ch is a letter.
                    if (Character.isLetter(ch))
                    {
                        ch -= characterCaseDiff;
                    }
                    out.append(ch);
                    ch = Character.forDigit(ba[j] & 0xF, 16);
                    if (Character.isLetter(ch))
                    {
                        ch -= characterCaseDiff;
                    }
                    out.append(ch);
                }
                buf.reset();
            }
        }

        // if the key is longer than 127 bytes add a File.separator
        // each 127 bytes
        int pos = 127;
        while (out.length() > pos) {
            out.insert(pos, File.separatorChar);
            pos += 127;
        }
        return out.toString();
    }

    /**
     * Dump a <code>String</code> to a text file.
     *
     * @param file The output file
     * @param string The string to be dumped
     * @exception IOException IO Error
     */
    public void serializeString(File file, String string)
    throws IOException 
    {
        final Writer fw = new FileWriter(file);
        try 
        {
            fw.write(string);
            fw.flush();
        } 
        finally 
        {
            if (fw != null) fw.close();
        }
    }

    /**
     * Load a text file contents as a <code>String<code>.
     * This method does not perform enconding conversions
     *
     * @param file The input file
     * @return The file contents as a <code>String</code>
     * @exception IOException IO Error
     */
    public String deserializeString(File file)
    throws IOException 
    {
        int len;
        char[] chr = new char[4096];
        final StringBuffer buffer = new StringBuffer();
        final FileReader reader = new FileReader(file);
        try 
        {
            while ((len = reader.read(chr)) > 0) 
            {
                buffer.append(chr, 0, len);
            }
        } 
        finally 
        {
            if (reader != null) reader.close();
        }
        return buffer.toString();
    }

    /**
     * This method serializes an object to an output stream.
     *
     * @param file The output file
     * @param object The object to be serialized
     * @exception IOException IOError
     */

    public void serializeObject(File file, Object object)
    throws IOException 
    {
        FileOutputStream fos = new FileOutputStream(file);
        try 
        {
            ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(fos));
            oos.writeObject(object);
            oos.flush();
        } 
        finally 
        {
            if (fos != null) fos.close();
        }
    }

    /**
     * This method deserializes an object from an input stream.
     *
     * @param file The input file
     * @return The deserialized object
     * @exception IOException IOError
     */
    public Object deserializeObject(File file)
    throws IOException, ClassNotFoundException 
    {
        FileInputStream fis = new FileInputStream(file);
        Object object = null;
        try 
        {
            ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(fis));
            object = ois.readObject();
        } 
        finally 
        {
            if (fis != null) fis.close();
        }
        return object;
    }

    /**
     * Get the complete filename corresponding to a (typically relative)
     * <code>File</code>.
     * This method accounts for the possibility of an error in getting
     * the filename's <i>canonical</i> path, returning the io/error-safe
     * <i>absolute</i> form instead
     *
     * @param file The file
     * @return The file's absolute filename
     */
    public String getFullFilename(File file)
    {
        try
        {
            return file.getCanonicalPath();
        }
        catch (Exception e)
        {
            return file.getAbsolutePath();
        }
    }

}
