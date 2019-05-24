package net.bither.net;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

import net.bither.util.NetworkUtil;

import java.io.IOException;

import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by xusong on 2016/5/23.
 */
public abstract class RequestCallback<T> extends BaseCallback<T> {

    private Context context;



    public RequestCallback(Context context) {
        this.context = context;

    }

    private Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 0x11:
                    ToastUtils.show(context,"请检查网络");
                    break;
                case 0x12:
                    ToastUtils.show(context,"找不到指定服务");
                    break;
                case 0x13:
                    ToastUtils.show(context,"404找不到指定接口");
                    break;
                case 0x14:
                    ToastUtils.show(context,"数据格式异常");
                    break;
            }


        }
    };
    @Override
    public void onRequestBefore(Request request, boolean isShow) {
        if (isShow) {

        }
    }

    @Override
    public void onFailure(Request request, IOException e) {

       if (NetworkUtil.isConnected()){
           handler.sendEmptyMessage(0x12);
       }else{
           handler.sendEmptyMessage(0x11);
       }

    }

    @Override
    public void onResponse(Response response) {


    }

    @Override
    public void onError(Response response, int code, Exception e) {
        if (code!=200){
               handler.sendEmptyMessage(0x13);
        }else {
            if (e!=null){
                handler.sendEmptyMessage(0x14);
            }
        }
    }



}
