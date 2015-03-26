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
 
package org.apache.avalon.fortress.impl.interceptor.test.examples;

/**
 * @author <a href="mailto:dev@excalibur.apache.org">Excalibur Development Team</a>
 */
public class FakeTransactionManager
{
    private static final FakeTransactionManager m_instance = new FakeTransactionManager();
    
    private int m_transactionsStarted;
    private int m_transactionsFinished;
    
    public static FakeTransactionManager instance()
    {
        return m_instance;
    }
    
    public void clear()
    {
        m_transactionsStarted = m_transactionsFinished = 0;
    }
    
    public void startTransaction()
    {
        m_transactionsStarted ++;
    }

    public void endTransaction()
    {
        m_transactionsFinished ++;
    }
    
    /**
     * @return
     */
    public int getTransactionsFinished()
    {
        return m_transactionsFinished;
    }

    /**
     * @return
     */
    public int getTransactionsStarted()
    {
        return m_transactionsStarted;
    }

}
