package util;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.util.Locale;

/**
 * Created by Ahmad on 12/19/17.
 * All rights reserved.
 */

public class EnglishTypeFace {


    private static DecimalFormat persianDecimalFormat;

    public static String withEnglishAmountFormat(long number) {
        return withEnglishAmountFormat((double) number);
    }
    public static String withEnglishAmountFormat(double number)
    {
        if (persianDecimalFormat == null) {
            String pattern = "###,###,###.##";
            DecimalFormatSymbols symbols = new DecimalFormatSymbols(new Locale("en"));
            symbols.setGroupingSeparator(',');
            symbols.setDecimalSeparator('.');
            persianDecimalFormat = new DecimalFormat(pattern, symbols);
        }
        String result = persianDecimalFormat.format(number);
        return result;
    }

    public static Double fromEnglishAmountFormat(String formattedAmount) {
        if (formattedAmount == null)
            return null;
        formattedAmount = formattedAmount.replace(",", "");
        if (persianDecimalFormat == null) {
            String pattern = "###,###,###.##";
            DecimalFormatSymbols symbols = new DecimalFormatSymbols(new Locale("en"));
            symbols.setGroupingSeparator(',');
            symbols.setDecimalSeparator('.');
            persianDecimalFormat = new DecimalFormat(pattern, symbols);
        }
        try {
            double value = persianDecimalFormat.parse(formattedAmount).doubleValue();
            return value;
        } catch (ParseException e) {
            return null;
        }
    }
}
