package com.hubidaauto.servmarket.module.order.service;

/**
 * @Classname OrderUtils
 * @Description TODO
 * @Author wang.zhidong
 * @Date 2021/10/17 17:35
 */
public interface OrderUtils {

    static String priceFormat(int format, char unit, boolean unitInHead, boolean splt) {
        StringBuilder stringBuilde = new StringBuilder(format);
        if (splt) {
            int times = Math.floorDiv(stringBuilde.length(), 3), offset = stringBuilde.length();
            for (int i = 1; i <= times; i++) {
                offset -= i * 3;
                stringBuilde.insert(offset, ',');
            }
        }
        stringBuilde.insert(unitInHead ? 0 : stringBuilde.length(), unit);
        return stringBuilde.toString();
    }
}
