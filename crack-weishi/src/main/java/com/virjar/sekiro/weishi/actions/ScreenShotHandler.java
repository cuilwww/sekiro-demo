package com.virjar.sekiro.weishi.actions;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

import com.virjar.sekiro.api.SekiroRequest;
import com.virjar.sekiro.api.SekiroRequestHandler;
import com.virjar.sekiro.api.SekiroResponse;
import com.virjar.sekiro.api.databind.AutoBind;

import java.io.ByteArrayOutputStream;
import java.util.Map;

import de.robv.android.xposed.XposedHelpers;

public class ScreenShotHandler implements SekiroRequestHandler {
    @AutoBind
    private int quality = 50;

    @Override
    public void handleRequest(SekiroRequest sekiroRequest, final SekiroResponse sekiroResponse) {

        Class<?> activityThreadClass = XposedHelpers.findClass("android.app.ActivityThread", ClassLoader.getSystemClassLoader());

        final Object mainThread = XposedHelpers.callStaticMethod(activityThreadClass, "currentActivityThread");


        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Activity topActivity = null;
                Map mActivities = (Map) XposedHelpers.getObjectField(mainThread, "mActivities");
                for (Object activityClientRecord : mActivities.values()) {
                    Activity tempActivity = (Activity) XposedHelpers.getObjectField(activityClientRecord, "activity");
                    if (tempActivity.hasWindowFocus()) {
                        topActivity = tempActivity;
                        break;
                    }
                }
                if (topActivity == null) {
                    sekiroResponse.failed("no data");
                    return;
                }
                View decorView = topActivity.getWindow().getDecorView();

                Bitmap bitmap = Bitmap.createBitmap(decorView.getWidth(), decorView.getHeight(), Bitmap.Config.RGB_565);
                decorView.draw(new Canvas(bitmap));

                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, quality, byteArrayOutputStream);

                sekiroResponse.send("image/jpeg", byteArrayOutputStream.toByteArray());
            }
        });


    }
}
