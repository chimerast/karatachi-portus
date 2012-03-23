package org.karatachi.portus.webbase.web.utils;

import org.apache.wicket.ResourceReference;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

public class TrueFalseImage extends Label {
    private static final long serialVersionUID = 1L;

    private static final ResourceReference IMAGE_TICK =
            new ResourceReference(TrueFalseImage.class, "tick.png");
    private static final ResourceReference IMAGE_CROSS =
            new ResourceReference(TrueFalseImage.class, "cross.png");
    private static final ResourceReference IMAGE_STOP =
            new ResourceReference(TrueFalseImage.class, "stop.png");

    public TrueFalseImage(String id, IModel<Boolean> model) {
        super(id, model);
    }

    public TrueFalseImage(String id, boolean value) {
        super(id, new Model<Boolean>(value));
    }

    public TrueFalseImage(String id) {
        super(id);
    }

    @Override
    protected void onComponentTag(ComponentTag tag) {
        if (getDefaultModelObject() != null) {
            tag.put("src", (Boolean) getDefaultModelObject()
                    ? urlFor(IMAGE_TICK) : urlFor(IMAGE_CROSS));
        } else {
            tag.put("src", urlFor(IMAGE_STOP));
        }
    }
}
