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

package org.apache.excalibur.instrument.client;

import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import org.apache.avalon.framework.logger.AbstractLogEnabled;

/**
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 */
class InstrumentManagerTreeModel
    extends AbstractLogEnabled
    implements InstrumentManagerConnectionListener, TreeModel
{
    private final InstrumentManagerConnection m_connection;

    /** The state version of the last client. */
    private int m_lastClientStateVersion;
    
    private boolean m_lastConnected = false;

    private DefaultMutableTreeNode m_root;

    private ArrayList m_listeners = new ArrayList();
    private TreeModelListener[] m_listenerArray;

    private HashMap m_elementMap = new HashMap();
    private HashMap m_leasedSampleMap = new HashMap();
    private DefaultMutableTreeNode[] m_leasedSampleArray;

    /*---------------------------------------------------------------
     * Constructors
     *-------------------------------------------------------------*/
    InstrumentManagerTreeModel( InstrumentManagerConnection connection )
    {
        m_connection = connection;

        m_root = new DefaultMutableTreeNode( "Root" );
    }

    /*---------------------------------------------------------------
     * InstrumentManagerConnectionListener Methods
     *-------------------------------------------------------------*/
    /**
     * Called when the connection is opened.  May be called more than once if
     *  the connection to the InstrumentManager is reopened.
     *
     * @param connection Connection which was opened.
     */
    public void opened( InstrumentManagerConnection connection )
    {
        getLogger().debug( "InstrumentManagerTreeModel.opened(" + connection + ")" );
        refreshModel();
    }

    /**
     * Called when the connection is closed.  May be called more than once if
     *  the connection to the InstrumentManager is reopened.
     *
     * @param connection Connection which was closed.
     */
    public void closed( InstrumentManagerConnection connection )
    {
        getLogger().debug( "InstrumentManagerTreeModel.closed(" + connection + ")" );
        refreshModel();
    }

    /**
     * Called when the connection is deleted.  All references should be removed.
     *
     * @param connection Connection which was deleted.
     */
    public void deleted( InstrumentManagerConnection connection )
    {
        getLogger().debug( "InstrumentManagerTreeModel.deleted(" + connection + ")" );
        refreshModel();
    }

    /*---------------------------------------------------------------
     * TreeModel Methods
     *-------------------------------------------------------------*/
    /**
     * Returns the root of the tree.  Returns null only if the tree has
     * no nodes.
     *
     * @return  the root of the tree
     */
    public Object getRoot()
    {
        return m_root;
    }

    /**
     * Returns the child of <I>parent</I> at index <I>index</I> in the parent's
     * child array.  <I>parent</I> must be a node previously obtained from
     * this data source. This should not return null if <i>index</i>
     * is a valid index for <i>parent</i> (that is <i>index</i> >= 0 &&
     * <i>index</i> < getChildCount(<i>parent</i>)).
     *
     * @param   parent  a node in the tree, obtained from this data source
     * @return  the child of <I>parent</I> at index <I>index</I>
     */
    public Object getChild( Object parent, int index )
    {
        //getLogger.debug("InstrumentManagerTreeModel.getChild(" + parent + ", " + index + ")");
        if ( parent instanceof DefaultMutableTreeNode )
        {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode)parent;
            return node.getChildAt( index );
        }
        else
        {
            return "---";
        }
    }


    /**
     * Returns the number of children of <I>parent</I>.  Returns 0 if the node
     * is a leaf or if it has no children.  <I>parent</I> must be a node
     * previously obtained from this data source.
     *
     * @param   parent  a node in the tree, obtained from this data source
     * @return  the number of children of the node <I>parent</I>
     */
    public int getChildCount( Object parent )
    {
        //getLogger.debug("InstrumentManagerTreeModel.getChildCount(" + parent + ")");
        if ( parent instanceof DefaultMutableTreeNode )
        {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode)parent;
            return node.getChildCount();
        }
        else
        {
            return 0;
        }
    }

    /**
     * Returns true if <I>node</I> is a leaf.  It is possible for this method
     * to return false even if <I>node</I> has no children.  A directory in a
     * filesystem, for example, may contain no files; the node representing
     * the directory is not a leaf, but it also has no children.
     *
     * @param   node    a node in the tree, obtained from this data source
     * @return  true if <I>node</I> is a leaf
     */
    public boolean isLeaf( Object node )
    {
        //getLogger.debug("InstrumentManagerTreeModel.isLeaf(" + node + ")");
        if ( node == m_root )
        {
            // The root must always return false so that trees that do not
            //  show their root node will display correctly.
            return false;
        }
        else if ( node instanceof DefaultMutableTreeNode )
        {
            DefaultMutableTreeNode inode = (DefaultMutableTreeNode)node;
            return inode.isLeaf();
        }
        else
        {
            return true;
        }
    }

    /**
     * Messaged when the user has altered the value for the item identified
     * by <I>path</I> to <I>newValue</I>.  If <I>newValue</I> signifies
     * a truly new value the model should post a treeNodesChanged
     * event.
     *
     * @param path path to the node that the user has altered.
     * @param newValue the new value from the TreeCellEditor.
     */
    public void valueForPathChanged( TreePath path, Object newValue )
    {
        //getLogger.debug( "InstrumentManagerTreeModel.valueForPathChanged(" + path +
        //  ", " + newValue + ")" );
    }

    /**
     * Returns the index of child in parent.
     */
    public int getIndexOfChild( Object parent, Object child )
    {
        //getLogger.debug("InstrumentManagerTreeModel.getIndexOfChild(" + parent + ", " + child + ")");
        if ( parent instanceof DefaultMutableTreeNode )
        {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode)parent;
            return node.getIndex( (DefaultMutableTreeNode)child );
        }
        else
        {
            return 0;
        }
    }

    /**
     * Adds a listener for the TreeModelEvent posted after the tree changes.
     *
     * @param listener the listener to add
     */
    public void addTreeModelListener( TreeModelListener listener )
    {
        //getLogger.debug("InstrumentManagerTreeModel.addTreeModelListener(" + listener + ")");
        synchronized(this)
        {
            m_listeners.add( listener );
            m_listenerArray = null;
        }
    }

    /**
     * Removes a listener previously added with <B>addTreeModelListener()</B>.
     *
     * @param listener the listener to remove
     */
    public void removeTreeModelListener( TreeModelListener listener )
    {
        //getLogger.debug("InstrumentManagerTreeModel.removeTreeModelListener(" + listener + ")");
        synchronized(this)
        {
            m_listeners.remove( listener );
            m_listenerArray = null;
        }
    }


    /*---------------------------------------------------------------
     * Methods
     *-------------------------------------------------------------*/
    /**
     * Returns an optimized array of the registered TreeModelListeners.
     *
     * @return An array of the registered TreeModelListeners
     */
    private TreeModelListener[] getListeners()
    {
        TreeModelListener[] listeners = m_listenerArray;
        if ( listeners == null )
        {
            synchronized(this)
            {
                m_listenerArray = new TreeModelListener[ m_listeners.size() ];
                m_listeners.toArray( m_listenerArray );
                listeners = m_listenerArray;
            }
        }
        return listeners;
    }

    private void fireTreeNodesChanged( TreeModelEvent event )
    {
        TreeModelListener[] listeners = getListeners();
        for ( int i = 0; i < listeners.length; i++ )
        {
            listeners[i].treeNodesChanged( event );
        }
    }

    private void fireTreeNodesInserted( TreeModelEvent event )
    {
        TreeModelListener[] listeners = getListeners();
        for ( int i = 0; i < listeners.length; i++ )
        {
            listeners[i].treeNodesInserted( event );
        }
    }

    private void fireTreeNodesRemoved( TreeModelEvent event )
    {
        TreeModelListener[] listeners = getListeners();
        for ( int i = 0; i < listeners.length; i++ )
        {
            listeners[i].treeNodesRemoved( event );
        }
    }

    private void fireTreeStructureChanged( TreeModelEvent event )
    {
        TreeModelListener[] listeners = getListeners();
        for ( int i = 0; i < listeners.length; i++ )
        {
            listeners[i].treeStructureChanged( event );
        }
    }

    /**
     * Returns a TreeNode for an Instrumentable given its name.
     *
     * @param name Name of the Instrumentable.
     *
     * @return The named TreeNode.
     */
    public DefaultMutableTreeNode getInstrumentableTreeNode( String name )
    {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode)m_elementMap.get( name );
        if ( node != null )
        {
            Object element = node.getUserObject();
            if ( element instanceof InstrumentableNodeData )
            {
                return node;
            }
        }
        return null;
    }

    /**
     * Returns a TreeNode for an Instrument given its name.
     *
     * @param name Name of the Instrument.
     *
     * @return The named TreeNode.
     */
    public DefaultMutableTreeNode getInstrumentTreeNode( String name )
    {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode)m_elementMap.get( name );
        if ( node != null )
        {
            Object element = node.getUserObject();
            if ( element instanceof InstrumentNodeData )
            {
                return node;
            }
        }
        return null;
    }

    /**
     * Returns a TreeNode for an InstrumentSample given its name.
     *
     * @param name Name of the InstrumentSample.
     *
     * @return The named TreeNode.
     */
    public DefaultMutableTreeNode getInstrumentSampleTreeNode( String name )
    {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode)m_elementMap.get( name );
        if ( node != null )
        {
            Object element = node.getUserObject();
            if ( element instanceof InstrumentSampleNodeData )
            {
                return node;
            }
        }
        return null;
    }

    /**
     * Returns an optimized array of TreeNodes representing the leased
     *  instrument samples in this tree model.
     *
     * @return An array of TreeNodes for the leased instrument samples
     *          in the TreeModel.
     */
    private DefaultMutableTreeNode[] getLeasedSampleArray()
    {
        DefaultMutableTreeNode[] leasedSampleArray = m_leasedSampleArray;
        if ( leasedSampleArray == null )
        {
            synchronized(this)
            {
                m_leasedSampleArray = new DefaultMutableTreeNode[ m_leasedSampleMap.size() ];
                m_leasedSampleMap.values().toArray( m_leasedSampleArray );
                leasedSampleArray = m_leasedSampleArray;
            }
        }
        return leasedSampleArray;
    }

    /**
     * Once a minute, all of the leased samples should be updated.  This is
     *  necessary to get the latest expiration times in case other processes
     *  are also updating the leases.
     *  Called from InstrumentManagerConnection.handleLeasedSamples.
     */
    void renewAllSampleLeases()
    {
        DefaultMutableTreeNode[] leasedSampleArray = getLeasedSampleArray();

        for ( int i = 0; i < leasedSampleArray.length; i++ )
        {
            // Extract the NodeData from the TreeNode
            InstrumentSampleNodeData sampleNodeData =
                (InstrumentSampleNodeData)leasedSampleArray[i].getUserObject();
            InstrumentSampleData sample = sampleNodeData.getData();

            updateInstrumentSample( sample );
        }
    }

    /**
     * Remove any instrument samples whose current leases have expired.
     */
    void purgeExpiredSamples()
    {
        DefaultMutableTreeNode[] leasedSampleArray = getLeasedSampleArray();

        for ( int i = 0; i < leasedSampleArray.length; i++ )
        {
            // Extract the NodeData from the TreeNode
            InstrumentSampleNodeData sampleNodeData =
                (InstrumentSampleNodeData)leasedSampleArray[i].getUserObject();
            //System.out.println("    check: " + sampleNodeData + "  remaining " +
            //  ( sampleNodeData.getRemainingLeaseTime() / 1000 ) + " seconds." );
            if ( sampleNodeData.getRemainingLeaseTime() < 0 )
            {
                // Update the Instrument containing the sample.
                DefaultMutableTreeNode instrumentTreeNode =
                    (DefaultMutableTreeNode)leasedSampleArray[i].getParent();
                InstrumentData instrumentData =
                    ((InstrumentNodeData)instrumentTreeNode.getUserObject()).getData();
                updateInstrument( instrumentData, instrumentTreeNode, -1 /*Force update*/ );
            }
        }
    }

    /**
     * Refreshes the entire Tree Model with the latest information from the server.
     *  This should be called whenever a refresh is needed, or whenever the status
     *  of the connection to the server changes.
     */
    void refreshModel()
    {
        // Is the connection open or not?
        if ( m_connection.isConnected() != m_lastConnected )
        {
            m_root.removeAllChildren();
            m_elementMap.clear();
            m_leasedSampleMap.clear();
            m_leasedSampleArray = null;
            m_lastClientStateVersion = -1;
            fireTreeStructureChanged( new TreeModelEvent( this, m_root.getPath() ) );
        }
        
        if ( m_connection.isConnected() )
        {
            // Need to update the child nodes. (Root Instrumentables)
            updateInstrumentManagerData(
                m_connection.getInstrumentManager(), m_root, m_lastClientStateVersion );
        }
        
        m_lastConnected = m_connection.isConnected();
    }

    /**
     * Called to update the local view of the InstrumentManagerData in the TreeeModel.
     *
     * @param manager The InstrumentManagerData to use for the update.
     * @param roorTreeNode The TreeNode representing the client.
     * @param oldStateVersion The state version at the time of the last update.
     */
    private void updateInstrumentManagerData( InstrumentManagerData manager,
                                              DefaultMutableTreeNode rootTreeNode,
                                              int oldStateVersion )
    {
        int stateVersion = manager.getStateVersion();
        if ( stateVersion == oldStateVersion )
        {
            // Already up to date.
            return;
        }

        if ( getLogger().isDebugEnabled() )
        {
            getLogger().debug( "update manager(" + manager.getName() + ") "
                + "state new=" + stateVersion + ", old=" + oldStateVersion );
        }

        // The latest Instrumentables will be in the correct order.
        InstrumentableData[] instrumentables = manager.getInstrumentables();
        int i;
        for ( i = 0; i < instrumentables.length; i++ )
        {
            InstrumentableData instrumentable = instrumentables[i];
            int oldInstrumentableStateVersion = -1;
            DefaultMutableTreeNode newChild = null;
            int childCount = rootTreeNode.getChildCount();
            if ( i < childCount )
            {
                int cmp;
                do {
                    DefaultMutableTreeNode oldChild =
                        (DefaultMutableTreeNode)rootTreeNode.getChildAt( i );
                    cmp = ((InstrumentableNodeData)oldChild.getUserObject()).getDescription().
                        compareTo( instrumentable.getDescription() );
                    if ( cmp == 0 )
                    {
                        // This is the same object.
                        InstrumentableNodeData nodeData =
                            (InstrumentableNodeData)oldChild.getUserObject();
                        oldInstrumentableStateVersion = nodeData.getStateVersion();
                        if ( nodeData.update() )
                        {
                            // The contents of the node changed.
                            fireTreeNodesChanged( new TreeModelEvent( this,
                                rootTreeNode.getPath(), new int[] { i }, new Object[] { oldChild } ) );
                        }
                        newChild = oldChild;

                        // Node already in the elementMap
                    }
                    else if ( cmp > 0 )
                    {
                        // Need to insert a new node.
                        newChild = new DefaultMutableTreeNode(
                            new InstrumentableNodeData( instrumentable ), true );
                        rootTreeNode.insert( newChild, i );
                        fireTreeNodesInserted( new TreeModelEvent( this,
                            rootTreeNode.getPath(),new int[] { i }, new Object[] { newChild } ) );

                        // Add the new node to the elementMap
                        m_elementMap.put( ((InstrumentableNodeData)newChild.getUserObject()).
                            getName(), newChild );
                    }
                    else if ( cmp < 0 )
                    {
                        // Need to remove an old node.
                        rootTreeNode.remove( i );
                        fireTreeNodesRemoved( new TreeModelEvent( this,
                            rootTreeNode.getPath(), new int[] { i }, new Object[] { oldChild } ) );

                        // Remove the old node from the elementMap
                        m_elementMap.remove( ((InstrumentableNodeData)oldChild.getUserObject()).
                            getName() );
                    }
                } while ( cmp < 0 );
            }
            else
            {
                // Append the new descriptor
                newChild = new DefaultMutableTreeNode(
                    new InstrumentableNodeData( instrumentable ), true );
                rootTreeNode.insert( newChild, i );
                fireTreeNodesInserted( new TreeModelEvent( this, rootTreeNode.getPath(),
                    new int[] { i }, new Object[] { newChild } ) );

                // Add the new node to the elementMap
                m_elementMap.put( ((InstrumentableNodeData)newChild.getUserObject()).
                    getName(), newChild );
            }

            updateInstrumentable( instrumentable, newChild, oldInstrumentableStateVersion );
        }
        // Remove any remaining old nodes
        while ( i < rootTreeNode.getChildCount() )
        {
            // Need to remove an old node.
            DefaultMutableTreeNode oldChild = (DefaultMutableTreeNode)rootTreeNode.getChildAt( i );
            rootTreeNode.remove( i );
            fireTreeNodesRemoved( new TreeModelEvent(
                this, rootTreeNode.getPath(), new int[] { i }, new Object[] { oldChild } ) );

            // Remove the old node from the elementMap
            m_elementMap.remove( ((InstrumentableNodeData)oldChild.getUserObject()).
                getName() );
        }

        m_lastClientStateVersion = stateVersion;
    }

    /**
     * @param instrumentableData The Instrumentable to update.
     * @param instrumentableTreeNode The tree node of the Instrumentable to
     *                               update.
     * @param oldStateVersion The state version at the time of the last update.
     */
    private void updateInstrumentable( InstrumentableData instrumentableData,
                                       DefaultMutableTreeNode instrumentableTreeNode,
                                       int oldStateVersion )
    {
        int stateVersion = instrumentableData.getStateVersion();
        if ( stateVersion == oldStateVersion )
        {
            // Already up to date.
            return;
        }

        if ( getLogger().isDebugEnabled() )
        {
            getLogger().debug( "update instrumentable(" + instrumentableData.getName() + ") "
                + "state new=" + stateVersion + ", old=" + oldStateVersion );
        }

        // The latest Instrumentables will be in the correct order.
        InstrumentableData[] childInstrumentables = instrumentableData.getInstrumentables();
        //System.out.println("Model.updateInstumentable() " + instrumentableData.getName() + " " + childInstrumentables.length);
        int i;
        for ( i = 0; i < childInstrumentables.length; i++ )
        {
            InstrumentableData childInstrumentable = childInstrumentables[i];
            int oldInstrumentableStateVersion = -1;
            //System.out.println("  " + childInstrumentable.getName() );
            DefaultMutableTreeNode newChild = null;
            int childCount = instrumentableTreeNode.getChildCount();
            if ( i < childCount )
            {
                int cmp;
                do {
                    DefaultMutableTreeNode oldChild =
                        (DefaultMutableTreeNode)instrumentableTreeNode.getChildAt( i );
                    if ( oldChild.getUserObject() instanceof InstrumentableNodeData )
                    {
                        cmp = ((InstrumentableNodeData)oldChild.getUserObject()).getDescription().
                            compareTo( childInstrumentable.getDescription() );
                    }
                    else
                    {
                        // Always put Instrumentables before any other nodes.
                        cmp = 1;
                    }

                    if ( cmp == 0 )
                    {
                        // This is the same object.
                        InstrumentableNodeData nodeData =
                            (InstrumentableNodeData)oldChild.getUserObject();
                        oldInstrumentableStateVersion = nodeData.getStateVersion();
                        if ( nodeData.update() )
                        {
                            // The contents of the node changed.
                            fireTreeNodesChanged( new TreeModelEvent( this,
                                instrumentableTreeNode.getPath(), new int[] { i },
                                new Object[] { oldChild } ) );
                        }
                        newChild = oldChild;

                        // Node already in the elementMap
                    }
                    else if ( cmp > 0 )
                    {
                        // Need to insert a new node.
                        newChild = new DefaultMutableTreeNode(
                            new InstrumentableNodeData( childInstrumentable ), true );
                        instrumentableTreeNode.insert( newChild, i );
                        fireTreeNodesInserted( new TreeModelEvent( this,
                            instrumentableTreeNode.getPath(),new int[] { i },
                            new Object[] { newChild } ) );

                        // Add the new node to the elementMap
                        m_elementMap.put( ((InstrumentableNodeData)newChild.getUserObject()).
                            getName(), newChild );
                    }
                    else if ( cmp < 0 )
                    {
                        // Need to remove an old node.
                        instrumentableTreeNode.remove( i );
                        fireTreeNodesRemoved( new TreeModelEvent( this,
                            instrumentableTreeNode.getPath(), new int[] { i },
                            new Object[] { oldChild } ) );

                        // Remove the old node from the elementMap
                        m_elementMap.remove( ((InstrumentableNodeData)oldChild.getUserObject()).
                            getName() );
                    }
                } while ( cmp < 0 );
            }
            else
            {
                // Append the new descriptor
                newChild = new DefaultMutableTreeNode(
                    new InstrumentableNodeData( childInstrumentable ), true );
                instrumentableTreeNode.insert( newChild, i );
                fireTreeNodesInserted( new TreeModelEvent( this, instrumentableTreeNode.getPath(),
                    new int[] { i }, new Object[] { newChild } ) );

                // Add the new node to the elementMap
                m_elementMap.put( ((InstrumentableNodeData)newChild.getUserObject()).
                    getName(), newChild );
            }

            updateInstrumentable( childInstrumentable, newChild, oldInstrumentableStateVersion );
        }
        // Remove any remaining old Instrumentable nodes
        while ( i < instrumentableTreeNode.getChildCount() )
        {
            // Need to remove an old node.
            DefaultMutableTreeNode oldChild =
                (DefaultMutableTreeNode)instrumentableTreeNode.getChildAt( i );
            if ( !( oldChild.getUserObject() instanceof InstrumentableNodeData ) )
            {
                break;
            }

            instrumentableTreeNode.remove( i );
            fireTreeNodesRemoved( new TreeModelEvent(
                this, instrumentableTreeNode.getPath(), new int[] { i },
                new Object[] { oldChild } ) );

            // Remove the old node from the elementMap
            m_elementMap.remove( ((InstrumentableNodeData)oldChild.getUserObject()).
                getName() );
        }


        // The latest Instruments will be in the correct order.
        InstrumentData[] instruments =
            instrumentableData.getInstruments();
        for ( i = childInstrumentables.length; i < instruments.length + childInstrumentables.length; i++ )
        {
            InstrumentData instrument = instruments[i - childInstrumentables.length];
            int oldInstrumentStateVersion = -1;
            //System.out.println("  " + instrument.getName() );
            DefaultMutableTreeNode newChild = null;
            int childCount = instrumentableTreeNode.getChildCount();
            if ( i < childCount )
            {
                int cmp;
                do {
                    DefaultMutableTreeNode oldChild =
                        (DefaultMutableTreeNode)instrumentableTreeNode.getChildAt( i );
                    if ( oldChild.getUserObject() instanceof InstrumentNodeData )
                    {
                        cmp = ((InstrumentNodeData)oldChild.getUserObject()).getDescription().
                            compareTo( instrument.getDescription() );
                    }
                    else
                    {
                        // Always put Instrumentables before any other nodes.
                        cmp = 1;
                    }

                    if ( cmp == 0 )
                    {
                        // This is the same object.
                        InstrumentNodeData nodeData = (InstrumentNodeData)oldChild.getUserObject();
                        oldInstrumentStateVersion = nodeData.getStateVersion();
                        if ( nodeData.update() )
                        {
                            // The contents of the node changed.
                            fireTreeNodesChanged( new TreeModelEvent( this,
                                instrumentableTreeNode.getPath(), new int[] { i },
                                new Object[] { oldChild } ) );
                        }
                        newChild = oldChild;

                        // Node already in the elementMap
                    }
                    else if ( cmp > 0 )
                    {
                        // Need to insert a new node.
                        newChild = new DefaultMutableTreeNode(
                            new InstrumentNodeData( instrument, m_connection ), true );
                        instrumentableTreeNode.insert( newChild, i );
                        fireTreeNodesInserted( new TreeModelEvent( this,
                            instrumentableTreeNode.getPath(),new int[] { i },
                            new Object[] { newChild } ) );

                        // Add the new node to the elementMap
                        m_elementMap.put( ((InstrumentNodeData)newChild.getUserObject()).
                            getName(), newChild );
                    }
                    else if ( cmp < 0 )
                    {
                        // Need to remove an old node.
                        instrumentableTreeNode.remove( i );
                        fireTreeNodesRemoved( new TreeModelEvent( this,
                            instrumentableTreeNode.getPath(), new int[] { i },
                            new Object[] { oldChild } ) );

                        // Remove the old node from the elementMap
                        m_elementMap.remove( ((InstrumentNodeData)oldChild.getUserObject()).
                            getName() );
                    }
                } while ( cmp < 0 );
            }
            else
            {
                // Append the new descriptor
                newChild = new DefaultMutableTreeNode(
                    new InstrumentNodeData( instrument, m_connection ), true );
                instrumentableTreeNode.insert( newChild, i );
                fireTreeNodesInserted( new TreeModelEvent( this, instrumentableTreeNode.getPath(),
                    new int[] { i }, new Object[] { newChild } ) );

                // Add the new node to the elementMap
                m_elementMap.put( ((InstrumentNodeData)newChild.getUserObject()).
                    getName(), newChild );
            }

            updateInstrument( instrument, newChild, oldInstrumentStateVersion );
        }
        // Remove any remaining old Instrument nodes
        while ( i < instrumentableTreeNode.getChildCount() )
        {
            // Need to remove an old node.
            DefaultMutableTreeNode oldChild =
                (DefaultMutableTreeNode)instrumentableTreeNode.getChildAt( i );
            instrumentableTreeNode.remove( i );
            fireTreeNodesRemoved( new TreeModelEvent(
                this, instrumentableTreeNode.getPath(), new int[] { i },
                new Object[] { oldChild } ) );

            // Remove the old node from the elementMap
            m_elementMap.remove( ((InstrumentNodeData)oldChild.getUserObject()).
                getName() );
        }
    }

    /**
     * @param instrumentData The Instrument to update.
     */
    void updateInstrument( InstrumentData instrumentData )
    {
        // Find the tree node.
        DefaultMutableTreeNode instrumentTreeNode =
            getInstrumentTreeNode( instrumentData.getName() );
        if ( instrumentTreeNode != null )
        {
            updateInstrument( instrumentData, instrumentTreeNode, -1 /* Force update */ );
        }
    }

    /**
     * @param instrumentData The Instrument to update.
     * @param instrumentTreeNode The tree node of the Instrument to update.
     */
    void updateInstrument( InstrumentData instrumentData,
                           DefaultMutableTreeNode instrumentTreeNode,
                           int oldStateVersion )
    {
        int stateVersion = instrumentData.getStateVersion();
        if ( stateVersion == oldStateVersion )
        {
            // Already up to date.
            return;
        }

        if ( getLogger().isDebugEnabled() )
        {
            getLogger().debug( "update instrument(" + instrumentData.getName() + ") "
                + "state new=" + stateVersion + ", old=" + oldStateVersion );
        }

        // The latest Instrument Samples will be in the correct order.
        InstrumentSampleData[] samples =
            instrumentData.getInstrumentSamples();
        //System.out.println("Model.updateInstument() " + instrumentDescriptor.getName() + " " + descriptors.length);
        int i;
        for ( i = 0; i < samples.length; i++ )
        {
            InstrumentSampleData sample = samples[i];
            //System.out.println("  " + sample.getName() );
            DefaultMutableTreeNode newChild = null;
            int childCount = instrumentTreeNode.getChildCount();
            if ( i < childCount )
            {
                int cmp;
                do {
                    // Old child will always be a sample
                    DefaultMutableTreeNode oldChild =
                        (DefaultMutableTreeNode)instrumentTreeNode.getChildAt( i );
                    cmp = ((InstrumentSampleNodeData)oldChild.getUserObject()).
                        getDescription().compareTo( sample.getDescription() );

                    if ( cmp == 0 )
                    {
                        // This is the same object.
                        if ( ((InstrumentSampleNodeData)oldChild.getUserObject()).update() )
                        {
                            // The contents of the node changed.
                            fireTreeNodesChanged( new TreeModelEvent( this,
                                instrumentTreeNode.getPath(), new int[] { i },
                                new Object[] { oldChild } ) );
                        }
                        newChild = oldChild;

                        // Node already in the elementMap
                    }
                    else if ( cmp > 0 )
                    {
                        // Need to insert a new node.
                        newChild = new DefaultMutableTreeNode(
                            new InstrumentSampleNodeData( instrumentData.getName(),
                            sample, m_connection ), true );
                        instrumentTreeNode.insert( newChild, i );
                        fireTreeNodesInserted( new TreeModelEvent( this,
                            instrumentTreeNode.getPath(),new int[] { i },
                            new Object[] { newChild } ) );

                        // Add the new node to the elementMap
                        InstrumentSampleNodeData newNodeData =
                            (InstrumentSampleNodeData)newChild.getUserObject();
                        String sampleName = newNodeData.getName();
                        m_elementMap.put( sampleName, newChild );
                        if ( newNodeData.isLeased() )
                        {
                            m_leasedSampleMap.put( sampleName, newChild );
                            m_leasedSampleArray = null;
                        }

                        // Make sure that the maintained flag is set correctly
                        MaintainedSampleLease lease =
                            m_connection.getMaintainedSampleLease( sampleName );
                        if ( lease != null )
                        {
                            newNodeData.setLeaseDuration( lease.getLeaseDuration() );
                        }
                    }
                    else if ( cmp < 0 )
                    {
                        // Need to remove an old node.
                        instrumentTreeNode.remove( i );
                        fireTreeNodesRemoved( new TreeModelEvent( this,
                            instrumentTreeNode.getPath(), new int[] { i },
                            new Object[] { oldChild } ) );

                        // Remove the old node from the elementMap
                        InstrumentSampleNodeData oldNodeData =
                            (InstrumentSampleNodeData)oldChild.getUserObject();
                        String sampleName = oldNodeData.getName();
                        m_elementMap.remove( sampleName );
                        if ( oldNodeData.isLeased() )
                        {
                            m_leasedSampleMap.remove( sampleName );
                            m_leasedSampleArray = null;
                        }
                    }
                } while ( cmp < 0 );
            }
            else
            {
                // Append the new descriptor
                newChild = new DefaultMutableTreeNode(
                    new InstrumentSampleNodeData( instrumentData.getName(), sample, m_connection ),
                    true );
                instrumentTreeNode.insert( newChild, i );
                fireTreeNodesInserted( new TreeModelEvent( this, instrumentTreeNode.getPath(),
                    new int[] { i }, new Object[] { newChild } ) );

                // Add the new node to the elementMap
                InstrumentSampleNodeData newNodeData =
                    (InstrumentSampleNodeData)newChild.getUserObject();
                String sampleName = newNodeData.getName();
                m_elementMap.put( sampleName, newChild );
                if ( newNodeData.isLeased() )
                {
                    m_leasedSampleMap.put( sampleName, newChild );
                    m_leasedSampleArray = null;
                }

                // Make sure that the maintained flag is set correctly
                MaintainedSampleLease lease =
                    m_connection.getMaintainedSampleLease( sampleName );
                if ( lease != null )
                {
                    newNodeData.setLeaseDuration( lease.getLeaseDuration() );
                }
            }
        }
        // Remove any remaining old Instrument Sample nodes
        while ( i < instrumentTreeNode.getChildCount() )
        {
            // Need to remove an old node.
            DefaultMutableTreeNode oldChild =
                (DefaultMutableTreeNode)instrumentTreeNode.getChildAt( i );

            instrumentTreeNode.remove( i );
            fireTreeNodesRemoved( new TreeModelEvent(
                this, instrumentTreeNode.getPath(), new int[] { i },
                new Object[] { oldChild } ) );

            // Remove the old node from the elementMap
            InstrumentSampleNodeData oldNodeData =
                (InstrumentSampleNodeData)oldChild.getUserObject();
            String sampleName = oldNodeData.getName();
            m_elementMap.remove( sampleName );
            if ( oldNodeData.isLeased() )
            {
                m_leasedSampleMap.remove( sampleName );
                m_leasedSampleArray = null;
            }
        }
    }

    /**
     * @param sampleData The Instrument Sample to update.
     */
    void updateInstrumentSample( InstrumentSampleData sampleData )
    {
        // Find the tree node.
        DefaultMutableTreeNode sampleTreeNode =
            getInstrumentSampleTreeNode( sampleData.getName() );
        if ( sampleTreeNode != null )
        {
            updateInstrumentSample( sampleData, sampleTreeNode );
        }
    }

    /**
     * @param sampleData The Instrument Sample to update.
     * @param sampleTreeNode The tree node of the Instrument Sample to update.
     */
    void updateInstrumentSample( InstrumentSampleData sampleData,
                                 DefaultMutableTreeNode sampleTreeNode )
    {
        // An update here should always lead to an event being fired.
        ((InstrumentSampleNodeData)sampleTreeNode.getUserObject()).update();

        // The contents of the node changed.
        fireTreeNodesChanged( new TreeModelEvent( this,
            sampleTreeNode.getPath(), new int[ 0 ], new Object[ 0 ] ) );
    }
}
