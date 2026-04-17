package xiaozhi.common.xss;

import org.apache.commons.lang3.StringUtils;

import xiaozhi.common.exception.ErrorCode;
import xiaozhi.common.exception.RenException;

/**
 * SQLfilter
 * Copyright (c) Renren Opensource All rights reserved.
 * Website: https://www.renren.io
 */
public class SqlFilter {

    /**
     * SQLnoteinfilter
     *
     * @param str pendingverification string
     */
    public static String sqlInject(String str) {
        if (StringUtils.isBlank(str)) {
            return null;
        }
        // remove'|"|;|\charactersymbol
        str = StringUtils.replace(str, "'", "");
        str = StringUtils.replace(str, "\"", "");
        str = StringUtils.replace(str, ";", "");
        str = StringUtils.replace(str, "\\", "");

        // convertsmallwrite
        str = str.toLowerCase();

        // non-charactersymbol
        String[] keywords = { "master", "truncate", "insert", "select", "delete", "update", "declare", "alter",
                "drop" };

        // determineYesNocontainnon-charactersymbol
        for (String keyword : keywords) {
            if (str.contains(keyword)) {
                throw new RenException(ErrorCode.INVALID_SYMBOL);
            }
        }

        return str;
    }
}
