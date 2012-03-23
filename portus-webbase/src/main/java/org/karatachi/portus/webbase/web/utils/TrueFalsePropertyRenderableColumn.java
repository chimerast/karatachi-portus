package org.karatachi.portus.webbase.web.utils;

import javax.swing.tree.TreeNode;

import org.apache.wicket.ResourceReference;
import org.apache.wicket.extensions.markup.html.tree.table.AbstractRenderableColumn;
import org.apache.wicket.extensions.markup.html.tree.table.ColumnLocation;
import org.apache.wicket.util.lang.PropertyResolver;

public class TrueFalsePropertyRenderableColumn extends AbstractRenderableColumn {
    private static final long serialVersionUID = 1L;

    private static final ResourceReference IMAGE_TICK =
            new ResourceReference(TrueFalseImage.class, "tick.png");
    private static final ResourceReference IMAGE_CROSS =
            new ResourceReference(TrueFalseImage.class, "cross.png");
    private static final ResourceReference IMAGE_STOP =
            new ResourceReference(TrueFalseImage.class, "stop.png");

    private final String propertyExpression;

    public TrueFalsePropertyRenderableColumn(ColumnLocation location,
            String header, String propertyExpression) {
        super(location, header);
        this.propertyExpression = propertyExpression;
        this.setEscapeContent(false);
    }

    @Override
    public String getNodeValue(TreeNode node) {
        Object result = PropertyResolver.getValue(propertyExpression, node);
        if (result == null || result.getClass() != Boolean.class) {
            return "<img src=\"/icons/stop.png\" />";
        } else if ((Boolean) result) {
            return "<img src=\"/icons/tick.png\" />";
        } else {
            return "<img src=\"/icons/cross.png\" />";
        }
    }
}
