package org.primer.net;

import com.google.gson.internal.$Gson$Types;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by xusong on 2016/5/20.
 */
public abstract class BaseCallback<T> {

    public Type mType;

    public BaseCallback() {
        mType = getSuperclassTypeParameter(getClass());
    }

    static Type getSuperclassTypeParameter(Class<?> subclass) {
        Type superclass = subclass.getGenericSuperclass();
        if (superclass instanceof Class) {
            throw new RuntimeException("Missing type parameter.");
        }
        ParameterizedType parameterized = (ParameterizedType) superclass;
        return $Gson$Types.canonicalize(parameterized.getActualTypeArguments()[0]);
    }


    /*抽象方法*/

    //请求前
    public abstract void onRequestBefore(Request request, boolean isShow);

    //请求发送失败（连接失败）
    public abstract void onFailure(Request request, IOException e);

    //整合连接成功操作（隐藏dialog）
    public abstract void onResponse(Response response);

    //请求成功
    public abstract void onSuccess(Response response, T t);

    //获取数据失败
    public abstract void onError(Response response, int code, Exception e);


}
