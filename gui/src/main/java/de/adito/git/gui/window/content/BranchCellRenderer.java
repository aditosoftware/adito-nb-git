package de.adito.git.gui.window.content;

import de.adito.git.api.data.IBranch;
import de.adito.git.gui.TableLayoutUtil;
import de.adito.git.gui.icon.SwingIconLoaderImpl;
import info.clearthought.layout.TableLayout;

import javax.swing.*;
import java.awt.*;

import static de.adito.git.gui.Constants.ARROW_RIGHT;

/**
 * This renderer is for the popup window.
 * Every row gets an arrow, to show that other options are possible
 * There are overridden functions because performance reasons which are implemented from the {@link DefaultListCellRenderer}.
 *
 * @author a.arnold, 21.11.2018
 */
class BranchCellRenderer extends JPanel implements ListCellRenderer<IBranch> {
    private JLabel rightLabel = new JLabel();
    private JLabel leftLabel = new JLabel();

    BranchCellRenderer() {
        double[] cols = {TableLayout.FILL, 10};
        double[] rows = {TableLayout.PREFERRED};

        setLayout(new TableLayout(cols, rows));
        TableLayoutUtil tlu = new TableLayoutUtil(this);
        tlu.add(0, 0, leftLabel);
        tlu.add(1, 0, rightLabel);
    }

    @Override
    public Component getListCellRendererComponent(JList<? extends IBranch> list, IBranch branch, int index, boolean isSelected, boolean cellHasFocus) {
        setComponentOrientation(list.getComponentOrientation());

        if (isSelected) {
            setBackground(list.getSelectionBackground());
            setForeground(list.getSelectionForeground());
        } else {
            setBackground(list.getBackground());
            setForeground(list.getForeground());
        }
        setEnabled(list.isEnabled());


        SwingIconLoaderImpl swingIconLoader = new SwingIconLoaderImpl();
        rightLabel.setIcon(swingIconLoader.getIcon(ARROW_RIGHT));
        leftLabel.setText(branch.getName());
        return this;
    }

    /**
     * see {@link }
     * <p>
     * Overridden for performance reasons.
     * See the <a href="#override">Implementation Note</a>
     * for more information.
     *
     * @return <code>true</code> if the background is completely opaque
     * and differs from the JList's background;
     * <code>false</code> otherwise
     * @since 1.5
     */
    @Override
    public boolean isOpaque() {
        Color back = getBackground();
        Component p = getParent();
        if (p != null) {
            p = p.getParent();
        }
        // p should now be the JList.
        boolean colorMatch = (back != null) && (p != null) &&
                back.equals(p.getBackground()) &&
                p.isOpaque();
        return !colorMatch && super.isOpaque();
    }

    /**
     * Overridden for performance reasons.
     * See the <a href="#override">Implementation Note</a>
     * for more information.
     *
     * @since 1.5
     */
    @Override
    public void repaint() {
    }

    /**
     * Overridden for performance reasons.
     * See the <a href="#override">Implementation Note</a>
     * for more information.
     */
    @Override
    public void repaint(long tm, int x, int y, int width, int height) {
    }

    /**
     * Overridden for performance reasons.
     * See the <a href="#override">Implementation Note</a>
     * for more information.
     */
    @Override
    public void repaint(Rectangle r) {
    }

    /**
     * Overridden for performance reasons.
     * See the <a href="#override">Implementation Note</a>
     * for more information.
     */
    @Override
    protected void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
        // Strings get interned...
        if (propertyName.equals("text")
                || ((propertyName.equals("font") || propertyName.equals("foreground"))
                && oldValue != newValue
                && getClientProperty(javax.swing.plaf.basic.BasicHTML.propertyKey) != null)) {

            super.firePropertyChange(propertyName, oldValue, newValue);
        }
    }

    /**
     * Overridden for performance reasons.
     * See the <a href="#override">Implementation Note</a>
     * for more information.
     */
    @Override
    public void firePropertyChange(String propertyName, byte oldValue, byte newValue) {
    }

    /**
     * Overridden for performance reasons.
     * See the <a href="#override">Implementation Note</a>
     * for more information.
     */
    @Override
    public void firePropertyChange(String propertyName, char oldValue, char newValue) {
    }

    /**
     * Overridden for performance reasons.
     * See the <a href="#override">Implementation Note</a>
     * for more information.
     */
    @Override
    public void firePropertyChange(String propertyName, short oldValue, short newValue) {
    }

    /**
     * Overridden for performance reasons.
     * See the <a href="#override">Implementation Note</a>
     * for more information.
     */
    @Override
    public void firePropertyChange(String propertyName, int oldValue, int newValue) {
    }

    /**
     * Overridden for performance reasons.
     * See the <a href="#override">Implementation Note</a>
     * for more information.
     */
    @Override
    public void firePropertyChange(String propertyName, long oldValue, long newValue) {
    }

    /**
     * Overridden for performance reasons.
     * See the <a href="#override">Implementation Note</a>
     * for more information.
     */
    @Override
    public void firePropertyChange(String propertyName, float oldValue, float newValue) {
    }

    /**
     * Overridden for performance reasons.
     * See the <a href="#override">Implementation Note</a>
     * for more information.
     */
    @Override
    public void firePropertyChange(String propertyName, double oldValue, double newValue) {
    }

    /**
     * Overridden for performance reasons.
     * See the <a href="#override">Implementation Note</a>
     * for more information.
     */
    @Override
    public void firePropertyChange(String propertyName, boolean oldValue, boolean newValue) {
    }
}