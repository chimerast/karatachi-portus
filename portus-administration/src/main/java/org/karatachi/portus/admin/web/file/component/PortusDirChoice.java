package org.karatachi.portus.admin.web.file.component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.karatachi.portus.core.entity.File;
import org.karatachi.portus.core.logic.FileAccessLogic;
import org.seasar.framework.container.annotation.tiger.Binding;

public class PortusDirChoice extends DropDownChoice<File> {
    private static final long serialVersionUID = 1L;

    @Binding
    private FileAccessLogic fileAccessLogic;

    public PortusDirChoice(String id) {
        super(id);
        setOutputMarkupId(true);

        setChoices(new AbstractReadOnlyModel<List<File>>() {
            private static final long serialVersionUID = 1L;

            @Override
            public List<File> getObject() {
                Set<File> dirs = new TreeSet<File>();
                dirs.addAll(fileAccessLogic.getDomainRootDirs());
                File dir = getModelObject();
                while (dir != null) {
                    dirs.add(dir);
                    dir = fileAccessLogic.getParentDir(dir);
                }
                return new ArrayList<File>(dirs);
            }
        });

        setChoiceRenderer(new ChoiceRenderer<File>() {
            private static final long serialVersionUID = 1L;

            @Override
            public Object getDisplayValue(File object) {
                return "http://" + object.fullPath + "/";
            }
        });
    }
}
