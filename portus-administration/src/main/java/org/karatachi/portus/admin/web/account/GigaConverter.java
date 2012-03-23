package org.karatachi.portus.admin.web.account;

import java.util.Locale;

import org.apache.wicket.util.convert.ConversionException;
import org.apache.wicket.util.convert.IConverter;

public class GigaConverter implements IConverter {
    private static final long serialVersionUID = 1L;

    public static final double GIGA = 1024.0 * 1024.0 * 1024.0;

    @Override
    public String convertToString(Object value, Locale locale) {
        if (value == null || value.getClass() != Long.class) {
            return "";
        }
        return String.format("%.2f", (Long) value / GIGA);
    }

    @Override
    public Object convertToObject(String value, Locale locale) {
        try {
            return (long) (Double.parseDouble(value) * GIGA);
        } catch (NumberFormatException e) {
            throw new ConversionException("Cannot convert.", e);
        }
    }
}
