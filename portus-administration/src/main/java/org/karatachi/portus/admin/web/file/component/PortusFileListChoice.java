package org.karatachi.portus.admin.web.file.component;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.karatachi.portus.core.entity.File;
import org.karatachi.portus.core.logic.FileAccessLogic;
import org.seasar.framework.container.annotation.tiger.Binding;

public class PortusFileListChoice extends AbstractFileListChoice<File> {
    private static final long serialVersionUID = 1L;

    @Binding
    private FileAccessLogic fileAccessLogic;

    public PortusFileListChoice(String id, final IModel<File> dir) {
        super(id);
        setOutputMarkupId(true);

        setChoices(new AbstractReadOnlyModel<List<File>>() {
            private static final long serialVersionUID = 1L;

            @Override
            public List<File> getObject() {
                if (dir.getObject() != null) {
                    return fileAccessLogic.getFiles(dir.getObject());
                } else {
                    return new ArrayList<File>();
                }
            }
        });

        setChoiceRenderer(new ChoiceRenderer<File>() {
            private static final long serialVersionUID = 1L;

            @Override
            public Object getDisplayValue(File object) {
                if (object.directory) {
                    return object.name + "/";
                } else {
                    return object.name;
                }
            }
        });
    }
}
