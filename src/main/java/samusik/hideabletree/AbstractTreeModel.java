/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package samusik.hideabletree;

/**
 *
 * @author Kola
 */
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.text.*;
import java.util.*;
 
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;
 
public abstract class AbstractTreeModel extends DefaultTreeModel {
	/**
	 * The list of tree model listeners.
	 */
	private Vector modelListeners = new Vector();
 
	/**
	 * The root object of the tree.
	 */
	
 
	/**
	 * Basic no-op constructor.  
	 */
	public AbstractTreeModel(TreeNode n) {
            super(n);
	}
 

	/**
	 * Gets the path to the root node for the specified object.  
	 * 
	 * @param  node  the root node
	 * @return  the path to the object or <CODE>null</CODE>
	 */
	public Object[] getPathToRoot(Object node) {
		return getPathToRoot(node, 0);
	}
 
	/**
	 * Gets the path to the root node for the specified object.  
	 * 
	 * @param  node  the root node
	 * @param  i     the current index
	 * @return  the path to the object or <CODE>null</CODE>
	 */
	private Object[] getPathToRoot(Object node, int i) {
		Object anode[];
		if(node == null) {
			if(i == 0) {
				return null;
			}
			anode = new Object[i];
		} else {
			i++;
			if(node == getRoot()) {
				anode = new Object[i];
			} else {
				anode = getPathToRoot(getParent(node), i);
			}
			anode[anode.length - i] = node;
		}
		return anode;
	}
 
	/**
	 * Gets the parent object of the specified object.  This method is not 
	 * part of the <code>javax.swing.tree.TreeModel</code> interface, but is 
	 * required to support the <code>getPathToRoot(Object)</code> method, 
	 * which is widely used in this class.  Therefore, it is important to 
	 * correctly implement this method.  
	 * 
	 * @param  obj  the object
	 * @parma  the parent object or null if no parent or invalid object
	 */
	protected abstract Object getParent(Object obj);
 
	/**
	 * Adds a listener for the <CODE>TreeModelEvent</CODE> posted after the 
	 * tree changes.
	 * 
	 * @param  l  the tree model listener
	 */
	
 
	/**
	 * Forces the tree to reload.  This is useful when many changes occur 
	 * under the root node in the tree structure.  
	 * <b>NOTE:</b> This will cause the tree to be collapsed.  To maintain 
	 * the expanded nodes, see the <code>getExpandedPaths(JTree)</code> 
	 * and <code>expandPaths(JTree, ArrayList)</code> methods.
	 * 
	 * @see  #getExpandedPaths(JTree)
	 * @see  #expandPaths(JTree, ArrayList)
	 */
	
 
	/**
	 * Forces the tree to repaint.  This is useful when many changes occur 
	 * under a specific node in the tree structure.  
	 * <b>NOTE:</b> This will cause the tree to be collapsed below the 
	 * updated node.  
	 * 
	 * @param  node  the node that changed
	 */
	public void reload(Object node) {
		if(node != null) {
			TreePath tp = new TreePath(getPathToRoot(node));
			fireTreeStructureChanged(new TreeModelEvent(this, tp));
		}
	}
 
 
	/**
	 * Notifies the tree that nodes were inserted.  The index is looked up 
	 * automatically.  
	 * 
	 * @param  node   the parent node
	 * @param  child  the inserted child node
	 */
	public void nodeInserted(Object node, Object child) {
		nodeInserted(node, child, -1);
	}
 
	/**
	 * Notifies the tree that nodes were inserted.  
	 * 
	 * @param  node   the parent node
	 * @param  child  the inserted child node
	 * @param  index  the index of the child
	 */
	public void nodeInserted(Object node, Object child, int index) {
		if(index < 0) {
			index = getIndexOfChild(node, child);
		}
		if(node != null && child != null && index >= 0) {
			TreePath tp = new TreePath(getPathToRoot(node));
			int[] ai = { index };
			Object[] ac = { child };
			fireTreeNodesInserted(new TreeModelEvent(this, tp, ai, ac));
		}
	}
 
	/**
	 * Notifies the tree that nodes were removed.  The index is required 
	 * since by this point, the object will no longer be in the tree.  
	 * 
	 * @param  node   the parent node
	 * @param  child  the removed child node
	 * @param  index  the index of the child
	 */
	public void nodeRemoved(Object node, Object child, int index) {
		if(node != null && child != null && index >= 0) {
			TreePath tp = new TreePath(getPathToRoot(node));
			int[] ai = { index };
			Object[] ac = { child };
			fireTreeNodesRemoved(new TreeModelEvent(this, tp, ai, ac));
		}
	}
 
	/**
	 * Notifies the tree that a node was changed.  
	 * 
	 * @param  node  the changed node
	 */
	public void nodeChanged(Object node) {
		if(node != null) {
			TreePath tp = new TreePath(getPathToRoot(node));
			fireTreeNodesChanged(new TreeModelEvent(this, tp, null, null));
		}
	}
 
	/**
	 * Fires "tree nodes changed" events to all listeners.  
	 * 
	 * @param  event  the tree model event
	 */
	protected void fireTreeNodesChanged(TreeModelEvent event) {
		for(int i = 0; i < modelListeners.size(); i++) {
			((TreeModelListener)modelListeners.elementAt(i)).treeNodesChanged(event);
		}
	}
 
	/**
	 * Fires "tree nodes inserted" events to all listeners.  
	 * 
	 * @param  event  the tree model event
	 */
	protected void fireTreeNodesInserted(TreeModelEvent event) {
		for(int i = 0; i < modelListeners.size(); i++) {
			((TreeModelListener)modelListeners.elementAt(i)).treeNodesInserted(event);
		}
	}
 
	/**
	 * Fires "tree nodes removed" events to all listeners.  
	 * 
	 * @param  event  the tree model event
	 */
	protected void fireTreeNodesRemoved(TreeModelEvent event) {
		for(int i = 0; i < modelListeners.size(); i++) {
			((TreeModelListener)modelListeners.elementAt(i)).treeNodesRemoved(event);
		}
	}
 
	/**
	 * Fires "tree structure changed" events to all listeners.  
	 * 
	 * @param  event  the tree model event
	 */
	protected void fireTreeStructureChanged(TreeModelEvent event) {
		for(int i = 0; i < modelListeners.size(); i++) {
			((TreeModelListener)modelListeners.elementAt(i)).treeStructureChanged(event);
		}
	}
 
	/**
	 * Records the list of currently expanded paths in the specified tree.  
	 * This method is meant to be called before calling the 
	 * <code>reload()</code> methods to allow the tree to store the paths.  
	 * 
	 * @param  tree      the tree
	 * @param  pathlist  the list of expanded paths
	 */
	public ArrayList getExpandedPaths(JTree tree) {
		ArrayList expandedPaths = new ArrayList();
		addExpandedPaths(tree, tree.getPathForRow(0), expandedPaths);
		return expandedPaths;
	}
 
	/**
	 * Adds the expanded descendants of the specifed path in the specified 
	 * tree to the internal expanded list.
	 * 
	 * @param  tree      the tree
	 * @param  path      the path
	 * @param  pathlist  the list of expanded paths
	 */
	private void addExpandedPaths(JTree tree, TreePath path, ArrayList pathlist) {
		Enumeration enumer = tree.getExpandedDescendants(path);
		while(enumer.hasMoreElements()) {
			TreePath tp = (TreePath)enumer.nextElement();
			pathlist.add(tp);
			addExpandedPaths(tree, tp, pathlist);
		}
	}
 
	/**
	 * Re-expands the expanded paths in the specified tree.  This method is 
	 * meant to be called before calling the <code>reload()</code> methods 
	 * to allow the tree to store the paths.  
	 * 
	 * @param  tree      the tree
	 * @param  pathlist  the list of expanded paths
	 */
	public void expandPaths(JTree tree, ArrayList pathlist) {
		for(int i = 0; i < pathlist.size(); i++) {
			tree.expandPath((TreePath)pathlist.get(i));
		}
	}
} 
