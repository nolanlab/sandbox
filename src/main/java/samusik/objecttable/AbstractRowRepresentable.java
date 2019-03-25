/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package samusik.objecttable;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.Serializable;
import java.util.ArrayList;

/**
 *
 * @author Nikolay
 */
public abstract class AbstractRowRepresentable implements RowRepresentable, Serializable {


    @Override
    public abstract boolean[] getEditibilityMask();

    @Override
    public abstract Object[] toRow();

    protected Object[] row;
    
    @Override
    public abstract String[] getHeaderRow();

    protected ArrayList<PropertyChangeListener> changeListeners = new ArrayList<>();

    protected void fireRowChanged(){
       for(PropertyChangeListener p :changeListeners){
           p.propertyChange(new PropertyChangeEvent(this, "Row", null, this.toRow()));
       }
    }

    protected void fireHeaderChanged(){
       for(PropertyChangeListener p :changeListeners){
           p.propertyChange(new PropertyChangeEvent(this, "Header", null, this.toRow()));
       }
    }

    protected abstract void clearHeaderAndData();

    @Override
    public void invalidate() {
    clearHeaderAndData();
    hashCode();
    fireRowChanged();
    fireHeaderChanged();
    }

     @Override
    public void addPropertyChangeListener(PropertyChangeListener p) {
        changeListeners.add(p);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener p) {
        changeListeners.remove(p);
    }



}
