package com.virjar.sekiro.weishi.actions;

import com.virjar.sekiro.api.SekiroRequest;
import com.virjar.sekiro.api.SekiroRequestHandler;
import com.virjar.sekiro.api.SekiroResponse;
import com.virjar.sekiro.api.databind.AutoBind;
import com.virjar.sekiro.weishi.HookEntry;
import com.virjar.sekiro.weishi.Store;

import de.robv.android.xposed.XposedHelpers;

public class GlobalSearchAllHandler implements SekiroRequestHandler {

    @AutoBind
    private int searchType = 0;

    @AutoBind
    private int dataType = 0;

    @Override
    public void handleRequest(SekiroRequest sekiroRequest, SekiroResponse sekiroResponse) {

        String key = sekiroRequest.getString("key");

        String attachInfo = sekiroRequest.getString("attachInfo");
        if (attachInfo == null) {
            attachInfo = "";
        }

        //请求id long a3 = com.tencent.weseevideo.common.utils.ar.a();
        Class<?> arClass = XposedHelpers.findClass("com.tencent.weseevideo.common.utils.ar", HookEntry.lpparam.classLoader);
        long a3 = (long) XposedHelpers.callStaticMethod(arClass, "a");

        //reqeust bean
        Class<?> seachBeanClass = XposedHelpers.findClass("com.tencent.oscar.module.discovery.ui.adapter.i", HookEntry.lpparam.classLoader);

        Object requestBean = XposedHelpers.newInstance(seachBeanClass, a3, key, searchType, dataType, attachInfo);

        //请求和响应绑定关系
        Store.requestTaskMap.put(requestBean, sekiroResponse);

        //请求发出去
        Class<?> tinListServiceClass = XposedHelpers.findClass("com.tencent.oscar.base.service.TinListService", HookEntry.lpparam.classLoader);
        Object tinListService = XposedHelpers.callStaticMethod(tinListServiceClass, "a");


        Class<?> ERefreshPolicyEnumClass = XposedHelpers.findClass("com.tencent.oscar.base.service.TinListService$ERefreshPolicy", HookEntry.lpparam.classLoader);
        Object EnumGetNetworkOnly = XposedHelpers.callStaticMethod(ERefreshPolicyEnumClass, "valueOf", "EnumGetNetworkOnly");

        XposedHelpers.callMethod(tinListService, "a", requestBean, EnumGetNetworkOnly, "GlobalSearchActivity_global_search_all");
    }
}
