/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package samusik.glasscmp;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTabbedPane;
import javax.swing.border.EmptyBorder;

/**
 *
 * @author Nikolay
 */
class CloseButtonTabComponent extends javax.swing.JPanel {
    private final javax.swing.JTabbedPane pane;

    public CloseButtonTabComponent(String title, final JTabbedPane pane) {
        //unset default FlowLayout' gaps
        super(new FlowLayout(FlowLayout.LEFT, 0, 0));
        if (pane == null) {
            throw new NullPointerException("TabbedPane is null");
        }
        this.pane = pane;
        setOpaque(false);

        //make JLabel read titles from JTabbedPane
        JLabel label = new JLabel(title) {
            public String getText() {
                int i = pane.indexOfTabComponent(CloseButtonTabComponent.this);
                if (i != -1) {
                    return pane.getTitleAt(i);
                }
                return null;
            }
        };

        add(label);
        //add more space between the label and the button
        label.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 2));
        //tab button
        JButton button = new TabButton();
        add(button);

        //add more space to the top of the component
        setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));


    }

    private class TabButton extends JButton implements ActionListener {
         int size = 13;
        public TabButton() {
            setPreferredSize(new Dimension(size, size));
            setToolTipText("Close Tab");
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
            //Close the proper tab by clicking the button
            addActionListener(this);
        }

        public void actionPerformed(ActionEvent e) {
            int i = pane.indexOfTabComponent(CloseButtonTabComponent.this);
            if (i != -1) {
                pane.remove(i);
            }
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
                BorderFactory.createEtchedBorder().paintBorder(this, g2, 1, 1, size, size);
                //g2.translate(-1, -1);
            }

            RenderingHints hints = new RenderingHints (null);

            hints.put(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            hints.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.addRenderingHints(hints);

            //shift the image for pressed buttons
            int delta = 3;

            //Background cross

            g2.setStroke(new BasicStroke(2.0f,java.awt.BasicStroke.CAP_BUTT,java.awt.BasicStroke.JOIN_MITER));



            if (getModel().isRollover()) {
                g2.setPaint(new Color(233,179,173));
            }else{
                g2.setColor(new Color(192,190,182));
            }



           //g2.translate(-1, -1);
            g2.drawLine(delta , delta , size-delta, size-delta);
           g2.drawLine(size-delta , delta , delta, size-delta);

            //Foreground cross

           hints = new RenderingHints (null);

           g2.setRenderingHints(hints);


            if (getModel().isRollover()) {
                g2.setPaint(new Color(172,57,28));
            }else{
            g2.setColor(Color.BLACK);
            }

            g2.setStroke(new BasicStroke(1.0f,java.awt.BasicStroke.CAP_BUTT,java.awt.BasicStroke.JOIN_MITER));


            g2.drawLine(delta , delta , size-delta, size-delta);
           g2.drawLine(size-delta , delta , delta, size-delta);

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
