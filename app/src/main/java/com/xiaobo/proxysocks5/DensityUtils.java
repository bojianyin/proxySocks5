package com.xiaobo.proxysocks5;

import android.content.Context;
import android.util.TypedValue;

public class DensityUtils {

    /**
     * 将 dp 转换为 px
     *
     * @param context 上下文，用于获取屏幕密度
     * @param dpValue 需要转换的 dp 值
     * @return 转换后的 px 值
     */
    public static int dpToPx(Context context, float dpValue) {
        float density = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * density + 0.5f);
    }

    /**
     * 将 px 转换为 dp
     *
     * @param context 上下文，用于获取屏幕密度
     * @param pxValue 需要转换的 px 值
     * @return 转换后的 dp 值
     */
    public static int pxToDp(Context context, float pxValue) {
        float density = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / density + 0.5f);
    }

    /**
     * 将 sp 转换为 px
     *
     * @param context 上下文，用于获取屏幕缩放密度
     * @param spValue 需要转换的 sp 值
     * @return 转换后的 px 值
     */
    public static int spToPx(Context context, float spValue) {
        float scaledDensity = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * scaledDensity + 0.5f);
    }

    /**
     * 将 px 转换为 sp
     *
     * @param context 上下文，用于获取屏幕缩放密度
     * @param pxValue 需要转换的 px 值
     * @return 转换后的 sp 值
     */
    public static int pxToSp(Context context, float pxValue) {
        float scaledDensity = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (pxValue / scaledDensity + 0.5f);
    }
}