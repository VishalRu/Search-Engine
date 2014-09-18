package edu.asu.irs13;

import java.awt.BorderLayout;
import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;

public class MyCellRenderer extends DefaultListCellRenderer {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	final JPanel p = new JPanel(new BorderLayout());
    final JPanel IconPanel = new JPanel(new BorderLayout());
    final JLabel l = new JLabel(""); //<-- this will be an icon instead of a text
    final JLabel lt = new JLabel();
    String pre = "<html><body style='width: 800px;'  >";

    MyCellRenderer() {
        //icon
        IconPanel.add(l, BorderLayout.NORTH);
        p.add(IconPanel, BorderLayout.WEST);

        p.add(lt, BorderLayout.CENTER);
 
    }

    @Override
    public Component getListCellRendererComponent(final JList list, final Object value, final int index, final boolean isSelected, final boolean hasFocus)
    {
        final String text = value.toString();
        lt.setText(pre + text);

        return p;
    }
}
