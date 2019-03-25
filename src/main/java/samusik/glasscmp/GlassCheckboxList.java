/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package samusik.glasscmp;

import java.awt.Component;
import java.awt.ItemSelectable;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.lang.reflect.Array;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.HashMap;
import javax.swing.DefaultListModel;
import javax.swing.JCheckBox;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;

/**
 *
 * @author Nikolay
 */
public class GlassCheckboxList extends JList implements ItemSelectable {
    private static final long serialVersionUID = 1L;
    
    private ListModel bgModel;
    
    private ArrayList selectedItems = new ArrayList();
    
    private boolean multipleCheckingAllowed = true;

    public boolean isMultipleCheckingAllowed() {
        return multipleCheckingAllowed;
    }

    public void setMultipleCheckingAllowed(boolean multipleCheckingAllowed) {
        this.multipleCheckingAllowed = multipleCheckingAllowed;
    }

    @Override
    public void setSelectionMode(int selectionMode) {
        super.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    }
    
    public GlassCheckboxList() {
    super();
    super.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        this.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                Object val =GlassCheckboxList.super.getSelectedValue();
                if(!multipleCheckingAllowed) selectedItems.clear();
                if(selectedItems.contains(val)){
                    selectedItems.remove(val);
                }else{
                    selectedItems.add(val);
                }
                fireSelectionChange(val, selectedItems.contains(val));
                GlassCheckboxList.this.repaint();                
            }
        
        });
    }

    @Override
    public void setModel(ListModel model) {
        selectedItems.clear();
        super.setModel(model);
        this.setCellRenderer(new ChekboxListRenderer());
    }

    private ArrayList<ItemListener> listeners = new ArrayList<ItemListener>();
    
    protected void fireSelectionChange(Object value, boolean  selected){
        for (ItemListener l : listeners) {
            l.itemStateChanged(new ItemEvent(this, ItemEvent.ITEM_STATE_CHANGED,value, selected?ItemEvent.SELECTED:ItemEvent.DESELECTED));
        }
    }
   
    @Override
    public void addItemListener(ItemListener l) {
        listeners.add(l);
    }

    @Override
    public void removeItemListener(ItemListener l) {
        listeners.remove(l);
    }
    
        
    public void setModel(ListModel model, boolean defaultChecked) {
        selectedItems.clear();
        for (int i = 0; i < model.getSize(); i++) {
            if(defaultChecked)selectedItems.add(model.getElementAt(i));
        }      
        super.setModel(model);
        this.setCellRenderer(new ChekboxListRenderer());
    }
    
    @Override
    public int getSelectionMode() {
        return super.getSelectionMode();
    }

    @Override
    public Object[] getSelectedObjects() {
        return getSelectedValues();
    }
    
    @Override
    public Object[] getSelectedValues() {
        if (!multipleCheckingAllowed) {
           if(selectedItems.size() >0){
               return new Object[]{selectedItems.get(0)};
           }else{
               return new Object[0];
           }
        } else {
            return selectedItems.toArray(new Object[selectedItems.size()]);
        }
    }

    @Override
    public Object getSelectedValue() {
       if(selectedItems.size() >0){
        return selectedItems.get(0);
        }
        return null;
    }

    private class ChekboxListRenderer implements ListCellRenderer {
        @Override
        public Component getListCellRendererComponent(final JList list, final Object value, int index, boolean isSelected, boolean cellHasFocus) {
            boolean selected = selectedItems.contains(value);
            final JCheckBox cb = new JCheckBox(value.toString(), selected);
            return cb;
        }
    }
}
