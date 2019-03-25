/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package samusik.hideabletree;

/**
 *
 * @author Kola
 */
// *** HideableMutableTreeNode ***
import javax.swing.*;
import javax.swing.tree.*;
 
/**
 * <code>HideableMutableTreeNode</code> is a <code>DefaultMutableTreeNode</code> 
 * implementation that works with <code>HideableTreeModel</code>.  
 */
public class HideableMutableTreeNode<T extends Object> extends DefaultMutableTreeNode {
	/**
	 * The node is visible flag. 
	 */
	public boolean visible = true;
 
	/**
	 * Creates a tree node that has no parent and no children, but which 
	 * allows children.
	 */
	public HideableMutableTreeNode() {
		super();
	}
 
	/**
	 * Creates a tree node with no parent, no children, but which allows 
	 * children, and initializes it with the specified user object.
	 * 
	 * @param  userObject - an Object provided by the user that 
	 *                      constitutes the node's data
	 */
	public HideableMutableTreeNode(T userObject) {
		super(userObject);
	}
 
	/**
	 * Creates a tree node with no parent, no children, initialized with the 
	 * specified user object, and that allows children only if specified.
	 * 
	 * @param  userObject     - an Object provided by the user that 
	 *                          constitutes the node's data
	 * @param  allowsChildren - if true, the node is allowed to have child 
	 *                          nodes -- otherwise, it is always a leaf node
	 */
	public HideableMutableTreeNode(T userObject, boolean allowsChildren) {
		super(userObject, allowsChildren);
	}

    @Override
    public void setUserObject(Object userObject) {
        super.setUserObject(userObject);
    }

    @Override
    public T getUserObject() {
        return (T)super.getUserObject();
    }


	/**
	 * Checks if the node is visible. 
	 * 
	 * @return  true if the node is visible, else false
	 */
	public boolean isVisible() {
		return this.visible;
	}
 
	/**
	 * Sets if the node is visible. 
	 * 
	 * @param  v  true if the node is visible, else false
	 */
	public void setVisible(boolean v) {
		this.visible = v;
	}
}