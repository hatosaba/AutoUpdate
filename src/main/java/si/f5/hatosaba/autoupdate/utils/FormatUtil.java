package si.f5.hatosaba.autoupdate.utils;

import org.apache.commons.lang.Validate;
import org.apache.commons.lang.WordUtils;
import org.bukkit.ChatColor;

import java.lang.reflect.Method;
import java.text.MessageFormat;

public class FormatUtil {
    private FormatUtil() { }

    public static String format(String format, Object... objects)
    {
        Validate.notNull(format, "format cannot be null!");

        try
        {
            format = MessageFormat.format(format, objects);
        } catch (Throwable ignored) { }

        return replaceColors(format);
    }

    private static final String[] rainbowColors = new String[]
            {
                    "c", "6", "e", "a", "b", "d", "5"
            };

    public static String replaceColors(String message)
    {
        Validate.notNull(message, "message cannot be null!");
        message = message.replaceAll("(&([zZ]))", "&z");
        if (message.contains("&z"))
        {
            StringBuilder ret = new StringBuilder();
            String[] ss = message.split("&z");
            ret.append(ss[0]);
            ss[0] = null;

            for (String s : ss)
            {
                if (s != null)
                {
                    int index = 0;
                    while (index < s.length() && s.charAt(index) != '&')
                    {
                        ret.append("&").append(rainbowColors[index % rainbowColors.length]);
                        ret.append(s.charAt(index));
                        index++;
                    }

                    if (index < s.length())
                    {
                        ret.append(s.substring(index));
                    }
                }
            }

            message = ret.toString();
        }

        // Format the colors
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public static String getFriendlyName(Object obj)
    {
        Validate.notNull(obj, "obj cannot be null!");

        try
        {
            // Clever little method to check if the method isn't declared by a class other than Object.
            Method method = obj.getClass().getMethod("toString");
            if (method.getDeclaringClass().getSuperclass() == null)
                return obj.getClass().getSimpleName();
        } catch (Throwable ignored) { }
        return getFriendlyName(obj.toString());
    }

    public static String getFriendlyName(String string)
    {
        Validate.notNull(string, "string cannot be null!");

        return WordUtils.capitalize(string.toLowerCase().replaceAll("_", " "));
    }

    private static final String VOWELS = "aeiou";

    public static String getArticle(String string)
    {
        Validate.notEmpty(string, "string cannot be null or empty!");

        return VOWELS.indexOf(Character.toLowerCase(string.charAt(0))) != -1 ? "an" : "a";
    }

    public static String getPlural(String string, int amount)
    {
        Validate.notEmpty(string, "string cannot be null or empty!");

        amount = Math.abs(amount);
        if (amount != 1)
        {
            char end = string.charAt(string.length() - 1);
            if (end != 's')
                return Character.isUpperCase(end) ? string + "S" : string + "s";
        }

        return string;
    }

    public static String capitalizeFirst(String string)
    {
        Validate.notEmpty(string, "string cannot be null or empty!");
        return Character.toUpperCase(string.charAt(0)) + string.substring(1);
    }
}

