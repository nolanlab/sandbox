/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package samusik.objecttable;

import java.beans.PropertyChangeListener;

/**
 *
 * @author Nikolay
 */
public interface RowRepresentable {

    public Object[] toRow();
    public String[] getHeaderRow();
    public void updateValue(int col, Object value);
    public boolean[] getEditibilityMask();
    public void addPropertyChangeListener(PropertyChangeListener p);
    public void removePropertyChangeListener(PropertyChangeListener p);
    public void invalidate();
}
