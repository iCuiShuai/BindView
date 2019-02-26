package com.csdroid.demo.apt.api;

import android.app.Activity;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class Bind {
    public static Map<Class, Method> binds = new HashMap<>();
    public static void bind(Activity activity) {
        if (activity == null || activity.isFinishing()) {
            return;
        }
        Method method = binds.get(activity.getClass());
        if (method == null) {
            Package pack = activity.getClass().getPackage();
            if (pack == null) {
                return;
            }
            String className = pack.getName() + "." + activity.getClass().getSimpleName() + "Bind";
            try {
                Class bindClass = Class.forName(className);
                method = bindClass.getMethod("bind", activity.getClass());
                binds.put(activity.getClass(), method);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        try {
            if (method != null) {
                method.invoke(null, activity);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
