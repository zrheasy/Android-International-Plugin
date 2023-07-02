//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.zrh.international.utils;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.ui.ComponentWithBrowseButton;
import com.intellij.ui.components.JBLabel;
import com.intellij.uiDesigner.core.AbstractLayout;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.util.containers.Stack;
import com.intellij.util.ui.JBUI;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.util.Iterator;
import javax.swing.JComponent;
import javax.swing.JSlider;
import javax.swing.JTable;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

public class FormScalingUtil {
    private static final boolean DEBUG = false;
    private static final Logger LOG = Logger.getInstance(FormScalingUtil.class);
    private static final String SCALE_FACTOR_KEY = "hidpi.scale.factor";
    private final Stack<String> myStack;
    private final Class myClazz;
    private final float myScaleFactor;
    private int myScaledValueCount;

    public FormScalingUtil(Class clazz) {
        this(clazz, JBUI.scale(1.0F));
    }

    public FormScalingUtil(Class clazz, float scaleFactor) {
        this.myStack = new Stack();
        this.myScaledValueCount = 0;
        this.myClazz = clazz;
        this.myScaleFactor = scaleFactor;
    }

    public static void scaleComponentTree(Class clazz, JComponent root) {
        (new FormScalingUtil(clazz)).scaleComponentTree(root);
    }

    public void scaleComponentTree(JComponent root) {
        if (this.myScaleFactor != 1.0F) {
            this.scaleComponentTreeWorker(root);
            root.putClientProperty("hidpi.scale.factor", this.myScaleFactor);
        }
    }

    protected boolean skipComponent(Component c) {
        if (c instanceof JComponent) {
            JComponent jComponent = (JComponent)c;
            if (jComponent.getClientProperty("hidpi.scale.factor") != null) {
                return true;
            }
        }

        return c instanceof ComponentWithBrowseButton && c.isPreferredSizeSet();
    }

    private void scaleComponentTreeWorker(Component c) {
        try {
            if (!this.skipComponent(c)) {
                this.scaleComponent(c);
                if (c instanceof Container) {
                    Container container = (Container)c;
                    Component[] var3 = container.getComponents();
                    int var4 = var3.length;

                    for(int var5 = 0; var5 < var4; ++var5) {
                        Component child = var3[var5];
                        this.scaleComponentTreeWorker(child);
                    }
                }

            }
        } finally {
            ;
        }
    }

    private void scaleComponent(Component c) {
        if (c instanceof Container) {
            Container container = (Container)c;
            this.scaleLayoutManager(container.getLayout());
        }

        if (c instanceof JTable) {
            JTable table = (JTable)c;
            Dimension size = table.getPreferredScrollableViewportSize();
            if (size != null) {
                table.setPreferredScrollableViewportSize(this.scale(size, "preferredScrollableViewportSize"));
            }
        }

        if (c instanceof JSlider) {
            JSlider slider = (JSlider)c;
            if (!slider.isPreferredSizeSet()) {
                slider.setPreferredSize(slider.getPreferredSize());
            }
        }

        if (c instanceof JBLabel) {
            JBLabel label = (JBLabel)c;
            label.setIconTextGap(this.scale(label.getIconTextGap(), "IconTextGap"));
        }

        if (c instanceof JComponent) {
            JComponent component = (JComponent)c;
            Border scaledBorder = this.getScaledBorder(component, component.getBorder());
            if (scaledBorder != null) {
                component.setBorder(scaledBorder);
            }
        }

        if (c.isFontSet()) {
            float fontSize = c.getFont().getSize2D();
            float minFontSize = 9.0F * this.myScaleFactor;
            if (fontSize <= minFontSize) {
                c.setFont(c.getFont().deriveFont(this.scale(fontSize, "FontSize")));
            }
        }

        this.scaleMinimumSize(c);
        this.scaleMaximumSize(c);
        this.scalePreferredSize(c);
        if (c.getParent() != null && c.getParent().getLayout() != null && c.getParent().getLayout() instanceof AbstractLayout) {
            AbstractLayout abstractLayout = (AbstractLayout)c.getParent().getLayout();
            GridConstraints constraint = abstractLayout.getConstraintsForComponent(c);
            constraint.myPreferredSize.width = this.scale(constraint.myPreferredSize.width, "constraint.myPreferredSize.width");
            constraint.myPreferredSize.height = this.scale(constraint.myPreferredSize.height, "constraint.myPreferredSize.height");
            constraint.myMinimumSize.width = this.scale(constraint.myMinimumSize.width, "constraint.myMinimumSize.width");
            constraint.myMinimumSize.height = this.scale(constraint.myMinimumSize.height, "constraint.myMinimumSize.height");
            constraint.myMaximumSize.width = this.scale(constraint.myMaximumSize.width, "constraint.myMaximumSize.width");
            constraint.myMaximumSize.height = this.scale(constraint.myMaximumSize.height, "constraint.myMaximumSize.height");
        }

    }

    private void scaleMinimumSize(Component c) {
        if (c.isMinimumSizeSet()) {
            c.setMinimumSize(this.scale(c.getMinimumSize(), "MinimumSize"));
        }

    }

    private void scaleMaximumSize(Component c) {
        if (c.isMaximumSizeSet()) {
            c.setMaximumSize(this.scale(c.getMaximumSize(), "MaximumSize"));
        }

    }

    private void scalePreferredSize(Component c) {
        if (c.isPreferredSizeSet()) {
            c.setPreferredSize(this.scale(c.getPreferredSize(), "PreferredSize"));
        }

    }

    private void scaleLayoutManager(LayoutManager layout) {
        if (layout instanceof AbstractLayout) {
            AbstractLayout abstractLayout = (AbstractLayout)layout;
            abstractLayout.setVGap(this.scale(abstractLayout.getVGap(), "VGap"));
            abstractLayout.setHGap(this.scale(abstractLayout.getHGap(), "VGap"));
            abstractLayout.setMargin(this.scale(abstractLayout.getMargin(), "Margin"));
        }

    }

    private Border getScaledBorder(Component c, Border border) {
        if (border == null) {
            return null;
        } else if (border.getClass() == EmptyBorder.class) {
            return new EmptyBorder(this.scale(border.getBorderInsets(c), "EmptyBorder"));
        } else {
            if (border instanceof TitledBorder) {
                TitledBorder titledBorder = (TitledBorder)border;

                try {
                    Border innerBorder = this.getScaledBorder(c, titledBorder.getBorder());
                    if (innerBorder != null) {
                        titledBorder.setBorder(innerBorder);
                    }
                } finally {
                    ;
                }
            }

            return border;
        }
    }

    private Dimension scale(Dimension dimension, String propertyName) {
        try {
            Dimension var3 = new Dimension(this.scale(dimension.width, "width"), this.scale(dimension.height, "height"));
            return var3;
        } finally {
            ;
        }
    }

    private Insets scale(Insets insets, String propertyName) {
        try {
            Insets var3 = new Insets(this.scale(insets.top, "top"), this.scale(insets.left, "left"), this.scale(insets.bottom, "bottom"), this.scale(insets.right, "right"));
            return var3;
        } finally {
            ;
        }
    }

    private int scale(int value, String propertyName) {
        try {
            int var3 = this.scale(value);
            return var3;
        } finally {
            ;
        }
    }

    private float scale(float value, String propertyName) {
        try {
            float var3 = this.scale(value);
            return var3;
        } finally {
            ;
        }
    }

    private float scale(float value) {
        return (float)this.scale((int)value);
    }

    private int scale(int value) {
        if (value <= 0) {
            return value;
        } else {
            long result = (long)(this.myScaleFactor * (float)value);
            if (result >= 2147483647L) {
                return Integer.MAX_VALUE;
            } else {
                this.logScale(value, (int)result);
                ++this.myScaledValueCount;
                return (int)result;
            }
        }
    }

    private void logScale(int value, int scaledValue) {
    }

    private static String getComponentName(Component c) {
        String name = c.getName();
        if (name == null) {
            name = c.getClass().getSimpleName();
        }

        return name;
    }

    private String buildStackString() {
        StringBuilder sb = new StringBuilder();

        String text;
        for(Iterator var2 = this.myStack.iterator(); var2.hasNext(); sb.append(text)) {
            text = (String)var2.next();
            if (sb.length() > 0) {
                sb.append(" > ");
            }
        }

        return sb.toString();
    }
}
