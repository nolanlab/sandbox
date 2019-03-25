
package samusik.glasscmp;

import java.io.Serializable;
import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.plaf.TabbedPaneUI;







/**
 * @author VeRaptor
 */
public class GlassTabPane extends javax.swing.JTabbedPane implements  Serializable {

    private AddButtonTabComponent btnAdd;

    public GlassTabPane() {

        btnAdd = new AddButtonTabComponent();

        btnAdd.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                btnAddActionPerformed();
            }

        });
       super.addTab("", new JPanel());
       setTabComponentAt(getTabCount()-1, btnAdd);
       super.setEnabledAt(0, false);
    }



    @Override
    public void removeTabAt(int index) {
        if(index < this.getTabCount()-1){
        super.removeTabAt(index);
        }
    }



    @Override
    public void addTab(String title, Component component) {
        super.removeTabAt(getTabCount()-1);
        super.addTab(title, component);
        setTabComponentAt(getTabCount()-1, new CloseButtonTabComponent("TabComponent 1", GlassTabPane.this));
        super.addTab("", new JPanel());
        setTabComponentAt(getTabCount()-1, btnAdd);
        super.setEnabledAt(getTabCount()-1, false);
    }

    @Override
    public void addTab(String title, Icon icon, Component component) {
        super.removeTabAt(getTabCount()-1);
        super.addTab(title, icon, component);
        setTabComponentAt(getTabCount()-1, new CloseButtonTabComponent("TabComponent 1", GlassTabPane.this));
        super.addTab("", new JPanel());
        setTabComponentAt(getTabCount()-1, btnAdd);
        super.setEnabledAt(getTabCount()-1, false);
    }

    @Override
    public void addTab(String title, Icon icon, Component component, String tip) {
         super.removeTabAt(getTabCount()-1);
       super.addTab(title, icon, component, tip);
        setTabComponentAt(getTabCount()-1, new CloseButtonTabComponent("TabComponent 1", GlassTabPane.this));
        super.addTab("", new JPanel());
        setTabComponentAt(getTabCount()-1, btnAdd);
        super.setEnabledAt(getTabCount()-1, false);
    }


    private void btnAddActionPerformed(){
    }


}
class AddButtonTabComponent extends javax.swing.JPanel {



    public AddButtonTabComponent() {
        //unset default FlowLayout' gaps
        super(new FlowLayout(FlowLayout.LEFT, 0, 0));
        setOpaque(false);

        //make JLabel read titles from JTabbedPane
        //tab button
        JButton button = new TabButton();
        add(button);
    }

    private class TabButton extends JButton {
    ImageIcon btnAdd = new javax.swing.ImageIcon(getClass().getResource("/btnAdd.png"));
    ImageIcon btnAddHL = new javax.swing.ImageIcon(getClass().getResource("/btnAddHL.png"));
         int size = 15;
        public TabButton() {
            super();
            setPreferredSize(new Dimension(size, size));
            setToolTipText("New Tab");
            //Make the button looks the same for all Laf's

            setUI(new javax.swing.plaf.basic.BasicButtonUI());
            //Make it transparent
            setContentAreaFilled(false);
            //No need to be focusable
            setFocusable(false);
            setBorder(new EmptyBorder(0,0,0,0));
            setBorderPainted(false);
            //Making nice rollover effect
            //we use the same listener for all buttons
            addMouseListener(buttonMouseListener);
            setRolloverEnabled(true);
        }

        //we don't want to update UI for this button
        public void updateUI() {
        }

        //paint the cross
        protected void paintComponent(Graphics g) {

            Graphics2D g2 = (Graphics2D) g;

            if (getModel().isPressed()) g2.translate(1, 1);

            if(isBorderPainted()){
                //g2.translate(1, 1);
                //BorderFactory.createEtchedBorder().paintBorder(this, g2, 1, 1, size, size);
                //g2.translate(-1, -1);
            }

            RenderingHints hints = new RenderingHints (null);

            hints.put(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            hints.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.addRenderingHints(hints);

            //shift the image for pressed buttons
            int delta = 0;

            //Background cross

            g2.setStroke(new BasicStroke(3.0f,java.awt.BasicStroke.CAP_BUTT,java.awt.BasicStroke.JOIN_MITER));



            if (getModel().isRollover()) {
                g2.setColor(new Color(147,220,95));
            }else{
                g2.setPaint(new Color(147,190,95));
                
            }

            int center = size/2 + delta/2;

           //g2.translate(-1, -1);
           //g2.drawLine(center, delta , center, size-delta);
           //g2.drawLine(delta , center , size-delta, center);

            //Foreground cross

           hints = new RenderingHints (null);

           g2.setRenderingHints(hints);


            if (getModel().isRollover()) {
                 g2.setColor(new Color(177,250,120));
            }else{
            g2.setPaint(new Color(177,220,135));
            }

            g2.setStroke(new BasicStroke(1.0f,java.awt.BasicStroke.CAP_BUTT,java.awt.BasicStroke.JOIN_MITER));

           //g2.drawLine(center, delta , center, size-delta);
           //g2.drawLine(delta , center , size-delta, center);

            if(getModel().isRollover()) {
                g2.setPaint(new RadialGradientPaint(new Point2D.Double(size/2.0,size/2.0),size/1.7,1, new Color(100,255,0,150), new Color(100,255,0,0)));
                g2.fillRect(0, 0, size, size);
                g2.drawImage(btnAdd.getImage(), 1,1,size-1,size-1, this);
            }else{
                 g2.drawImage(btnAdd.getImage(), 1,1,size-1,size-1, this);
            }




            //g2.translate(1, 1);

        }
    }

    private final static java.awt.event.MouseListener buttonMouseListener = new MouseAdapter() {
        public void mouseEntered(MouseEvent e) {
            Component component = e.getComponent();
            if (component instanceof AbstractButton) {
                AbstractButton button = (AbstractButton) component;
                button.setBorderPainted(true);
            }
        }

        public void mouseExited(MouseEvent e) {
            Component component = e.getComponent();
            if (component instanceof AbstractButton) {
                AbstractButton button = (AbstractButton) component;
                button.setBorderPainted(false);
            }
        }
    };

}