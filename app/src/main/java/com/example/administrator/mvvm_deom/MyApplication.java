package com.example.administrator.mvvm_deom;

import android.app.Application;
import android.content.pm.PackageInfo;

import com.facebook.drawee.backends.pipeline.Fresco;

/**
 * Created by Administrator on 2018-02-28.
 */

public class MyApplication extends Application {

    public static String sPackageName;  //包名

    @Override
    public void onCreate() {
        super.onCreate();
        Fresco.initialize(this);
        initPackageName();
    }

    private void initPackageName() {
        PackageInfo info;
        try {
            info = getApplicationContext().getPackageManager().getPackageInfo(this.getPackageName(),0);
            sPackageName = info.packageName;
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
