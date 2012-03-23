package org.karatachi.portus.admin.web.file.component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.karatachi.portus.core.AssemblyInfo;
import org.karatachi.portus.core.entity.LocalFile;
import org.karatachi.portus.core.logic.LocalFileAccessLogic;
import org.seasar.framework.container.annotation.tiger.Binding;

public class LocalDirChoice extends DropDownChoice<LocalFile> {
    private static final long serialVersionUID = 1L;

    @Binding
    private LocalFileAccessLogic localFileAccessLogic;

    public LocalDirChoice(String id) {
        super(id);
        setOutputMarkupId(true);

        setChoices(new AbstractReadOnlyModel<List<LocalFile>>() {
            private static final long serialVersionUID = 1L;

            @Override
            public List<LocalFile> getObject() {
                Set<LocalFile> dirs = new TreeSet<LocalFile>();
                LocalFile dir = getModelObject();
                while (dir != null) {
                    dirs.add(dir);
                    dir = localFileAccessLogic.getParentDir(dir);
                }
                return new ArrayList<LocalFile>(dirs);
            }
        });

        setChoiceRenderer(new ChoiceRenderer<LocalFile>() {
            private static final long serialVersionUID = 1L;

            @Override
            public Object getDisplayValue(LocalFile object) {
                return object.file.getPath().substring(
                        AssemblyInfo.PATH_RAW_DATA.length());
            }
        });
    }
}
