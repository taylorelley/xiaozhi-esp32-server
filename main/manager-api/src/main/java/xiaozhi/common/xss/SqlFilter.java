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
     * SQL注入filter
     *
     * @param str pendingverification string
     */
    public static String sqlInject(String str) {
        if (StringUtils.isBlank(str)) {
            return null;
        }
        // 去掉'|"|;|\字符
        str = StringUtils.replace(str, "'", "");
        str = StringUtils.replace(str, "\"", "");
        str = StringUtils.replace(str, ";", "");
        str = StringUtils.replace(str, "\\", "");

        // convert成smallwrite
        str = str.toLowerCase();

        // non-法字符
        String[] keywords = { "master", "truncate", "insert", "select", "delete", "update", "declare", "alter",
                "drop" };

        // determineYesNocontainnon-法字符
        for (String keyword : keywords) {
            if (str.contains(keyword)) {
                throw new RenException(ErrorCode.INVALID_SYMBOL);
            }
        }

        return str;
    }
}
