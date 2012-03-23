package org.karatachi.portus.admin.web.file.component;

import java.util.List;

import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.karatachi.portus.core.entity.LocalFile;
import org.karatachi.portus.core.logic.LocalFileAccessLogic;
import org.seasar.framework.container.annotation.tiger.Binding;

public class LocalFileListChoice extends AbstractFileListChoice<LocalFile> {
    private static final long serialVersionUID = 1L;

    @Binding
    private LocalFileAccessLogic localFileAccessLogic;

    public LocalFileListChoice(String id, final IModel<LocalFile> dir) {
        super(id);
        setOutputMarkupId(true);

        setChoices(new AbstractReadOnlyModel<List<LocalFile>>() {
            private static final long serialVersionUID = 1L;

            @Override
            public List<LocalFile> getObject() {
                return localFileAccessLogic.getFiles(dir.getObject());
            }
        });

        setChoiceRenderer(new ChoiceRenderer<LocalFile>() {
            private static final long serialVersionUID = 1L;

            @Override
            public Object getDisplayValue(LocalFile object) {
                if (object.file.isDirectory()) {
                    return object.file.getName() + "/";
                } else {
                    return object.file.getName();
                }
            }
        });
    }
}
