package org.karatachi.portus.admin.web.file.component;

import org.apache.wicket.markup.html.form.ListMultipleChoice;
import org.apache.wicket.util.string.AppendingStringBuffer;
import org.apache.wicket.util.string.Strings;

public abstract class AbstractFileListChoice<T> extends ListMultipleChoice<T> {
    private static final long serialVersionUID = 1L;

    public AbstractFileListChoice(String id) {
        super(id);
    }

    @Override
    protected void appendOptionHtml(AppendingStringBuffer buffer, T choice,
            int index, String selected) {
        String displayValue =
                (String) getChoiceRenderer().getDisplayValue(choice);

        buffer.append("\n<option ");
        if (isSelected(choice, index, selected)) {
            buffer.append("selected=\"selected\" ");
        }
        if (isDisabled(choice, index, selected)) {
            buffer.append("disabled=\"disabled\" ");
        }

        if (displayValue.endsWith("/")) {
            buffer.append("class=\"folder\" style=\"color: #003366;\" ");
        } else {
            buffer.append("class=\"file\" ");
        }

        buffer.append("value=\"");
        buffer.append(Strings.escapeMarkup(getChoiceRenderer().getIdValue(
                choice, index)));
        buffer.append("\">");

        CharSequence escaped = displayValue;
        if (getEscapeModelStrings()) {
            escaped = escapeOptionHtml(displayValue);
        }
        buffer.append(escaped);
        buffer.append("</option>");
    }
}
