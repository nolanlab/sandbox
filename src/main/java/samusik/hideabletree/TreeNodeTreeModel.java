/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package samusik.hideabletree;

/**
 *
 * @author Kola
 */
// *** TreeNodeTreeModel ***
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;
 
/**
 * <code>TreeNodeTreeModel</code> is an <code>AbstractTreeModel</code> 
 * implementation for <code>javax.swing.tree.TreeNode</code> objects.
 */
public class TreeNodeTreeModel extends AbstractTreeModel {
	/**
	 * Creates a new <code>TreeNodeTreeModel</code> object.
	 * 
	 * @param  root  the root node
	 */
	public TreeNodeTreeModel(TreeNode root) {
		super(root);
		setRoot(root);
	}
 
	/**
	 * Returns the parent of the child node.
	 * 
	 * @param  node  the child node
	 * @return  the parent or null if root
	 */
	public Object getParent(Object node) {
		if(node != getRoot() && (node instanceof TreeNode)) {
			return ((TreeNode)node).getParent();
		}
		return null;
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
			return ((TreeNode)parent).getChildAt(index);
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
		if(parent instanceof TreeNode) {
			return ((TreeNode)parent).getChildCount();
		}
		return 0;
	}
 
	/**
	 * Returns the index of child in parent.
	 * 
	 * @param  parent  the parent node
	 * @param  child   the child node
	 * @return  the index of the child node in the parent
	 */
	public int getIndexOfChild(Object parent, Object child) {
		if(parent instanceof TreeNode && child instanceof TreeNode) {
			return ((TreeNode)parent).getIndex((TreeNode)child);
		}
		return -1;
	}
 
	/**
	 * Returns true if node is a leaf.
	 * 
	 * @param  node  the node
	 * @return  true if the node is a leaf
	 */
	public boolean isLeaf(Object node) {
		if(node instanceof TreeNode) {
			return ((TreeNode)node).isLeaf();
		}
		return true;
	}
 

}