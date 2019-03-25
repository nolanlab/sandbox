/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package samusik.hideabletree;

/**
 *
 * @author Kola
 */
// *** HideableTreeModel ***
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;
 
/**
 * <code>HideableTreeModel</code> is an <code>TreeNodeTreeModel</code> 
 * implementation for <code>HideableMutableTreeNode</code> objects.  The 
 * model can also take any other <code>javax.swing.tree.TreeNode</code> 
 * objects.  
 */
public class HideableTreeModel extends TreeNodeTreeModel {
    private boolean forceVisible;
    
    
    /**
	 * Returns if hidden hodes are shown
	 * 
	 * @param  true if hidden nodes are shown 
	 */
    
    public boolean isForceVisible() {
        return forceVisible;
    }
    
    /**
	 * Defines if hidden hodes are shown
	 * 
	 * @param  forceVisible 
	 */
    
    public void setForceVisible(boolean forceVisible) {
        this.forceVisible = forceVisible;
        this.nodeStructureChanged(root);
    }
    
    
	/**
	 * Creates a new <code>HideableTreeModel</code> object.
	 * 
	 * @param  root  the root node
	 */
	public HideableTreeModel(TreeNode root) {
		super(root);
	}
 
	/**
	 * Checks if the specified node is visible.  A node can only be 
	 * hidden if the node is an instance of <code>HideableMutableTreeNode</code>.  <br />
	 * <br />
	 * Note that this only test the visibility of the specified node, not 
	 * whether a parent node is visible.  Use <code>isPathToNodeVisible(Object)</code> 
	 * to check if the full path is visible.  
	 * 
	 * @param  node  the node
	 * @param  true if the node is visible, else false
	 */
	public boolean isNodeVisible(Object node) {
		if(node != getRoot()) {
			if(node instanceof HideableMutableTreeNode) {
				return ((HideableMutableTreeNode)node).isVisible() || forceVisible;
			}
		}
		return true;
	}
 
	/**
	 * Sets the specified node to be hidden.  A node can only be made hidden 
	 * if the node is an instance of <code>HideableMutableTreeNode</code>.  <br />
	 * <br />
	 * Note that this method will notify the tree to reflect any changes to 
	 * node visibility.  <br />
	 * <br />
	 * Note that this will not alter the visibility of any nodes in the 
	 * specified node's path to the root node.  Use 
	 * <code>ensurePathToNodeVisible(Object)</code> instead to make sure the 
	 * full path down to that node is visible.  <br />
	 * <br />
	 * Note that this method will notify the tree to reflect any changes to 
	 * node visibility.  
	 * 
	 * @param  node  the node
	 * @param  v     true for visible, false for hidden
	 * @param  true if the node's visibility could actually change, else false
	 */
	public boolean setNodeVisible(Object node, boolean v) {
		// can't hide root
		if(node != super.getRoot()) {
			if(node instanceof HideableMutableTreeNode) {
				HideableMutableTreeNode n = (HideableMutableTreeNode)node;
				// don't fix what ain't broke...
				if(v != n.isVisible()) {
					TreeNode parent = n.getParent();
					if(v) {
						// need to get index after showing...
						n.setVisible(v);
						int index = getIndexOfChild(parent, n);
						super.nodeInserted(parent, n, index);
					} else {
						// need to get index before hiding...
						int index = getIndexOfChild(parent, n);
						n.setVisible(v);
						super.nodeRemoved(parent, n, index);
					}
				}
				return true;
			}
		}
		return false;
	}
 
	/**
	 * Checks if the specified node is visible and all nodes above it are 
	 * visible.  
	 * 
	 * @param  node  the node
	 * @param  true if the path is visible, else false
	 */
	public boolean isPathToNodeVisible(Object node) {
		Object[] path = getPathToRoot(node);
		for(int i = 0; i < path.length; i++) {
			if(!isNodeVisible(path[i])) {
				return false;
			}
		}
		return true;
	}
 
	/**
	 * Sets the specified node and all nodes above it to be visible.  <br />
	 * <br />
	 * Note that this method will notify the tree to reflect any changes to 
	 * node visibility.  
	 * 
	 * @param  node  the node
	 */
	public void ensurePathToNodeVisible(Object node) {
		Object[] path = getPathToRoot(node);
		for(int i = 0; i < path.length && !forceVisible; i++) {
			setNodeVisible(path[i], true);
		}
	}
 
	/**
	 * Returns the child of parent at index index in the parent's child array.
	 * 
	 * @param  parent  the parent node
	 * @param  index   the index
	 * @return  the child or null if no children
	 */
	public Object getChild(Object parent, int index) {
		if(parent instanceof TreeNode) {
			TreeNode p = (TreeNode)parent;
			for(int i = 0, j = -1; i < p.getChildCount(); i++) {
				TreeNode pc = (TreeNode)p.getChildAt(i);
				if(isNodeVisible(pc)||forceVisible) {
					j++;
				}
				if(j == index) {
					return pc;
				}
			}
		}
		return null;
	}
 
	/**
	 * Returns the number of children of parent.
	 * 
	 * @param  parent  the parent node
	 * @return  the child count
	 */
	public int getChildCount(Object parent) {
		int count = 0;
		if(parent instanceof TreeNode) {
			TreeNode p = (TreeNode)parent;
			for(int i = 0; i < p.getChildCount(); i++) {
				TreeNode pc = (TreeNode)p.getChildAt(i);
				if(isNodeVisible(pc)||forceVisible) {
					count++;
				}
			}
		}
		return count;
	}
 
	/**
	 * Returns the index of child in parent.
	 * 
	 * @param  parent  the parent node
	 * @param  child   the child node
	 * @return  the index of the child node in the parent
	 */
	public int getIndexOfChild(Object parent, Object child) {
		int index = -1;
		if(parent instanceof TreeNode && child instanceof TreeNode) {
			TreeNode p = (TreeNode)parent;
			TreeNode c = (TreeNode)child;
			if(isNodeVisible(c)||forceVisible) {
				index = 0;
				for(int i = 0; i < p.getChildCount(); i++) {
					TreeNode pc = (TreeNode)p.getChildAt(i);
					if(pc.equals(c)) {
						return index;
					}
					if(isNodeVisible(pc)||forceVisible) {
						index++;
					}
				}
			}
		}
		return index;
	}
}
