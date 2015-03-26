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
package org.apache.excalibur.event.impl;

import org.apache.excalibur.event.EnqueuePredicate;
import org.apache.excalibur.event.Sink;

/**
 * This enqueue predicate implements input rate policing.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Revision: 1.5 $
 */
public class RateLimitingPredicate implements EnqueuePredicate
{
    /** The rate to which the enqueuing should be limited */
    private double m_targetRate;

    /** The depth of the token bucket */
    private int m_depth;

    private int m_tokenCount;
    private long m_lastTime;
    private double m_regenTimeMs;

    /** Number of milliseconds between regenerations */
    private static final long MIN_REGENERATION_TIME = 0;

    //------------------------- RateLimitingPredicate constructors
    /**
     * Create a new RateLimitingPredicate for the given sink,
     * bucket depth and no rate limit.
     * @since May 15, 2002
     *
     * @param depth
     *  The token bucket depth.
     */
    public RateLimitingPredicate(int depth)
    {
        this(-1.0, depth);
    }

    /**
     * Create a new RateLimitingPredicate for the given sink,
     * targetRate, and token bucket depth.
     * A rate of <m_code>-1.0</m_code> indicates no rate limit.
     * @since May 15, 2002
     *
     * @param targetRate
     *  The rate that is the target for this predicate
     * @param depth
     *  The token bucket depth.
     */
    public RateLimitingPredicate(double targetRate, int depth)
    {
        m_targetRate = targetRate;
        m_depth = depth;

        m_regenTimeMs = (1.0 / targetRate) * 1.0e3;
        if (m_regenTimeMs < 1)
        {
            m_regenTimeMs = 1;
        }

        m_tokenCount = depth;
        m_lastTime = System.currentTimeMillis();
    }

    //------------------------- EnqueuePredicate implementation
    /**
     * @see EnqueuePredicate#accept(Object, Sink)
     */
    public boolean accept(Object element, Sink sink)
    {
        if (m_targetRate == -1.0)
        {
            return true;
        }

        // First regenerate tokens
        long currentTime = System.currentTimeMillis();
        long delay = currentTime - m_lastTime;

        if (delay >= MIN_REGENERATION_TIME)
        {
            double numTokens = ((double) delay * 1.0) / (m_regenTimeMs * 1.0);
            m_tokenCount += numTokens;

            if (m_tokenCount > m_depth)
            {
                m_tokenCount = m_depth;
            }

            m_lastTime = currentTime;
        }

        if (m_tokenCount >= 1)
        {
            m_tokenCount--;
            return true;
        }
        else
        {
            return false;
        }
    }

    /**
     * @see EnqueuePredicate#accept(Object, Sink)
     */
    public boolean accept(Object[] elements, Sink sink)
    {
        if (m_targetRate == -1.0)
        {
            return true;
        }

        // First regenerate tokens
        long currentTime = System.currentTimeMillis();
        long delay = currentTime - m_lastTime;

        if (delay >= MIN_REGENERATION_TIME)
        {
            double numTokens = ((double) delay * 1.0) / (m_regenTimeMs * 1.0);
            m_tokenCount += numTokens;

            if (m_tokenCount > m_depth)
            {
                m_tokenCount = m_depth;
            }
            m_lastTime = currentTime;
        }

        if (m_tokenCount >= elements.length)
        {
            m_tokenCount -= elements.length;
            return true;
        }
        else
        {
            return false;
        }
    }

    //------------------------- RateLimitingPredicate specific implementation
    /**
     * Returns the current rate limit.
     * @since May 15, 2002
     *
     * @return double
     *  the current target rate
     */
    public double getTargetRate()
    {
        return m_targetRate;
    }

    /**
     * Returns the current depth.
     * @since May 15, 2002
     *
     * @return int
     *  The current bucket depth.
     */
    public int getDepth()
    {
        return m_depth;
    }

    /**
     * Returns the number of tokens currently in the bucket.
     * @since May 15, 2002
     *
     * @return int
     *  the number of tokens currently in the bucket.
     */
    public int getBucketSize()
    {
        return m_tokenCount;
    }

    /**
     * Allows to set the rate limit. A limit of <m_code>-1.0</m_code>
     * indicates no rate limit.
     * @since May 15, 2002
     *
     * @param targetRate
     *  the current rate limit.
     */
    public void setTargetRate(double targetRate)
    {
        m_targetRate = targetRate;

        m_regenTimeMs = (1.0 / targetRate) * 1.0e3;
        if (m_regenTimeMs < 1)
        {
            m_regenTimeMs = 1;
        }
    }

    /**
     * Allows to set the bucket depth.
     * @since May 15, 2002
     *
     * @param depth
     *  The bucket depth as an integer.
     */
    public void setDepth(int depth)
    {
        m_depth = depth;
    }

}
