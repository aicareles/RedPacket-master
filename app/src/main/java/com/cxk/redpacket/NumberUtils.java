package com.cxk.redpacket;

/**
 * Created by jerry on 2018/6/27.
 */

public class NumberUtils {

    //金额验证
    public static boolean isNumber(String str)
    {
        java.util.regex.Pattern pattern=java.util.regex.Pattern.compile("^(([1-9]{1}\\d*)|([0]{1}))(\\.(\\d){0,2})?$"); // 判断小数点后2位的数字的正则表达式
        java.util.regex.Matcher match=pattern.matcher(str);
        if(!match.matches())
        {
            return false;
        }
        else
        {
            return true;
        }
    }
}
