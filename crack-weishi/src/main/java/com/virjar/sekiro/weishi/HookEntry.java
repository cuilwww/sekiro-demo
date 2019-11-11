package com.virjar.sekiro.weishi;

import android.util.Log;

import com.virjar.sekiro.api.SekiroClient;
import com.virjar.sekiro.api.SekiroResponse;
import com.virjar.sekiro.weishi.actions.GlobalSearchAllHandler;
import com.virjar.sekiro.weishi.actions.ScreenShotHandler;

import java.util.UUID;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import external.com.alibaba.fastjson.JSONObject;

public class HookEntry implements IXposedHookLoadPackage {
    private static final String TAG = "WS_HOOK";
    public static XC_LoadPackage.LoadPackageParam lpparam = null;

    @Override
    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {

        HookEntry.lpparam = lpparam;
        //com.tencent.oscar.utils.network.wns.f#a(com.tencent.oscar.utils.network.d)
        XposedHelpers.findAndHookMethod("com.tencent.oscar.utils.network.wns.f", lpparam.classLoader,
                "a", "com.tencent.oscar.utils.network.d", new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        Log.i(TAG, "send request: " + param.args[0].getClass(), new Throwable());
                    }
                });

        //com.tencent.oscar.module.discovery.ui.adapter.i#i
        XposedBridge.hookAllConstructors(XposedHelpers.findClass("com.tencent.oscar.module.discovery.ui.adapter.i", lpparam.classLoader), new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                Log.i(TAG, "stWSSearchAllReq request:" + JSONObject.toJSONString(param.args), new Throwable());
                //stWSSearchAllReq request:[3,"近5成大学生不能坚持吃早饭",0,0,""]
                //stWSSearchAllReq request:[4,"近5成大学生不能坚持吃早饭",0,3,"2|user:1523330441429779&1553593652898547&1524121336030552&1542871933289262&1547122753682267&1540351913490662&1529914890868040&1528557912956115&1534905094713706&1526282000882047|feed:78dvWDY5b1HHypaR5&75mtbewqO1IfWwakJ&7afbMEdaJ1Idq0Gwq&77JcDyl6N1I7a6E0A&754seO5231HZNqKxq&DhHi1IQDBWctCIL3&7903jPWGH1IfXI7yE&6YzWxcT5w1IjuR8eD&76Q4U9BUv1I6Lotxr&76PXjlU5s1I5v4TCS"]
            }
        });

        /**
         *         long a3 = com.tencent.weseevideo.common.utils.ar.a();
         *         com.tencent.oscar.base.service.TinListService.a()
         *                 .a(
         *                         new com.tencent.oscar.module.discovery.ui.adapter.i(a3, trim, 0, 0, "")
         *                         , com.tencent.oscar.base.service.TinListService.ERefreshPolicy.EnumGetNetworkOnly
         *                         , GLOBAL_SEARCH_ALL
         *                 );
         *
         */

        if (lpparam.packageName.equals(lpparam.processName)) {
            //在主进程里面启动服务
            final SekiroClient sekiroClient = SekiroClient.start("sekiro.virjar.com", UUID.randomUUID().toString(), "weishi-demo");
            sekiroClient.registerHandler("globalSearchAll", new GlobalSearchAllHandler());
            sekiroClient.registerHandler("screenShot", new ScreenShotHandler());
        }

        //数据响应的时候，拦截请求
        //com.tencent.oscar.utils.network.j#a(com.tencent.oscar.utils.network.d, com.tencent.oscar.utils.network.e)
        XposedHelpers.findAndHookMethod("com.tencent.oscar.utils.network.j", lpparam.classLoader,
                "a", "com.tencent.oscar.utils.network.d", "com.tencent.oscar.utils.network.e", new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        SekiroResponse sekiroResponse = Store.requestTaskMap.remove(param.args[0]);
                        if (sekiroResponse == null) {
                            return;
                        }

                        Object jceStructObj = XposedHelpers.callMethod(param.args[1], "d");
                        Object responseData = ForceFiledViewer.toView(jceStructObj);
                        sekiroResponse.success(responseData);
                        param.setResult(true);
                    }
                });

        //数据异常返回的时候，拦截请求
        //com.tencent.oscar.utils.network.j#a(com.tencent.oscar.utils.network.d, int, java.lang.String)
        XposedHelpers.findAndHookMethod("com.tencent.oscar.utils.network.j", lpparam.classLoader,
                "a", "com.tencent.oscar.utils.network.d", int.class, String.class, new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        SekiroResponse sekiroResponse = Store.requestTaskMap.remove(param.args[0]);
                        if (sekiroResponse == null) {
                            return;
                        }
                        sekiroResponse.failed("code: " + param.args[1] + "  message: " + param.args[2]);
                        param.setResult(true);
                    }
                });
    }

}
