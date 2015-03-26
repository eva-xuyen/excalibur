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

import java.awt.Component;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

/**
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 */
class InstrumentManagerTreeCellRenderer
    extends DefaultTreeCellRenderer
{
    /*---------------------------------------------------------------
     * Constructors
     *-------------------------------------------------------------*/
    InstrumentManagerTreeCellRenderer()
    {
    }
    
    /*---------------------------------------------------------------
     * DefaultTreeCellRenderer Methods
     *-------------------------------------------------------------*/
    public Component getTreeCellRendererComponent( JTree tree,
                                                   Object value,
                                                   boolean sel,
                                                   boolean expanded,
                                                   boolean leaf,
                                                   int row,
                                                   boolean hasFocus)
    {
        super.getTreeCellRendererComponent( tree, value, sel, expanded, leaf, row, hasFocus );
        
        /*
        System.out.println("InstrumentManagerTreeCellRenderer.getTreeCellRendererComponent(tree, " +
                            "value=" + value + ", sel=" + sel + ", expanded=" + expanded + ", leaf=" +
                            leaf + ", row=" + row + ", focus=" + hasFocus + ") " +
                            value.getClass().getName() );
        */
        if ( value instanceof DefaultMutableTreeNode )
        {
            DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode)value;
            Object userObject = treeNode.getUserObject();
            
            if ( userObject instanceof NodeData )
            {
                NodeData nodeData = (NodeData)userObject;
                setIcon( nodeData.getIcon() );
                setToolTipText( nodeData.getToolTipText() );
            }
        }
        
        return this;
    }
    
    /*---------------------------------------------------------------
     * Methods
     *-------------------------------------------------------------*/
}
