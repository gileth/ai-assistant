package com.ganlansi.chat.util.okhttp;


import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.ganlansi.chat.exception.BusinessException;
import okhttp3.*;
import org.springframework.util.StringUtils;

/**
 * Created by fighting on 2017/4/7.
 */

class RequestUtil {
    private String mMetyodType;//请求方式，目前只支持get和post
    private String mUrl;//接口
    private Map<String, String> mParamsMap;//键值对类型的参数，只有这一种情况下区分post和get。
    private String mJsonStr;//json类型的参数，post方式
    private File mFile;//文件的参数，post方式,只有一个文件
    private  List<File> mfileList;//文件集合，这个集合对应一个key，即mfileKey
    private  String mfileKey;//上传服务器的文件对应的key
    private  Map<String, File> mfileMap;//文件集合，每个文件对应一个key
    private String mFileType;//文件类型的参数，与file同时存在
    private Map<String, String> mHeaderMap;//头参数
    private CallBackUtil mCallBack;//回调接口
    private OkHttpClient mOkHttpClient;//OKhttpClient对象
    private Request mOkHttpRequest;//请求对象
    private Request.Builder mRequestBuilder;//请求对象的构建者


    private long callTimeout = 60;//超时时间
    private long  readTimeout = 60;//读取超时时间

    private long  writeTimeout = 60;//写入超时时间

    private long connectTimeout = 60;//连接超时时间



    RequestUtil(String methodType, String url, Map<String, String> paramsMap, Map<String, String> headerMap, CallBackUtil callBack) throws Exception {
        this(methodType,url,null,null,null,null,null,null,paramsMap,headerMap,callBack);
    }

    RequestUtil(String methodType, String url, String jsonStr, Map<String, String> headerMap, CallBackUtil callBack) throws Exception {
        this(methodType,url,jsonStr,null,null,null,null,null,null,headerMap,callBack);
    }

    RequestUtil(String methodType, String url, Map<String, String> paramsMap, File file, String fileKey, String fileType, Map<String, String> headerMap, CallBackUtil callBack) throws Exception {
        this(methodType,url,null,file,null,fileKey,null,fileType,paramsMap,headerMap,callBack);
    }
    RequestUtil(String methodType, String url, Map<String, String> paramsMap, List<File> fileList, String fileKey, String fileType, Map<String, String> headerMap, CallBackUtil callBack) throws Exception {
        this(methodType,url,null,null,fileList,fileKey,null,fileType,paramsMap,headerMap,callBack);
    }
    RequestUtil(String methodType, String url, Map<String, String> paramsMap, Map<String, File> fileMap, String fileType, Map<String, String> headerMap, CallBackUtil callBack) throws Exception {
        this(methodType,url,null,null,null,null,fileMap,fileType,paramsMap,headerMap,callBack);
    }

    private RequestUtil(String methodType, String url, String jsonStr ,File file ,List<File> fileList,String fileKey , Map<String,File> fileMap,String fileType,Map<String, String> paramsMap,Map<String, String> headerMap, CallBackUtil callBack) throws Exception {
        mMetyodType = methodType;
        mUrl = url;
        mJsonStr = jsonStr;
        mFile =file;
        mfileList =fileList;
        mfileKey =fileKey;
        mfileMap =fileMap;
        mFileType =fileType;
        mParamsMap = paramsMap;
        mHeaderMap = headerMap;
        mCallBack = callBack;
        getInstance();
    }


    /**
     * 创建OKhttpClient实例。
     */
    private void getInstance() throws Exception {
        OkHttpClient.Builder builder = new OkHttpClient.Builder().callTimeout(callTimeout, TimeUnit.SECONDS).connectTimeout(connectTimeout, TimeUnit.SECONDS)
                .readTimeout(readTimeout, TimeUnit.SECONDS).writeTimeout(writeTimeout, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true);
        mOkHttpClient = builder.build();
        mRequestBuilder = new Request.Builder();
        if(mFile != null || mfileList != null || mfileMap != null){//先判断是否有文件，
            throw new BusinessException("当前不支持文件处理");
        }else {
            //设置参数
            switch (mMetyodType){
                case OkhttpUtil.METHOD_GET:
                    setGetParams();
                    break;
                case OkhttpUtil.METHOD_POST:
                    mRequestBuilder.post(getRequestBody());
                    break;
                case OkhttpUtil.METHOD_PUT:
                    mRequestBuilder.put(getRequestBody());
                    break;
                case OkhttpUtil.METHOD_DELETE:
                    mRequestBuilder.delete(getRequestBody());
                    break;
            }
        }
        mRequestBuilder.url(mUrl);
        if(mHeaderMap != null){
            setHeader();
        }
        //mRequestBuilder.addHeader("Authorization","Bearer "+"token");可以把token添加到这儿
        mOkHttpRequest = mRequestBuilder.build();
    }

    /**
     * 得到body对象
     */
    private RequestBody getRequestBody() {
        /**
         * 首先判断mJsonStr是否为空，由于mJsonStr与mParamsMap不可能同时存在，所以先判断mJsonStr
         */
        if(!StringUtils.isEmpty(mJsonStr)){
            MediaType JSON = MediaType.parse("application/json; charset=utf-8");//数据类型为json格式，
            return RequestBody.create(JSON, mJsonStr);//json数据，
        }

        /**
         * post,put,delete都需要body，但也都有body等于空的情况，此时也应该有body对象，但body中的内容为空
         */
        FormBody.Builder formBody = new FormBody.Builder();
        if(mParamsMap != null) {
            for (String key : mParamsMap.keySet()) {
                formBody.add(key, mParamsMap.get(key));
            }
        }
        return formBody.build();
    }



    /**
     * get请求，只有键值对参数
     */
    private void setGetParams() {
        if(mParamsMap != null){
            mUrl = mUrl+"?";
            for (String key: mParamsMap.keySet()){
                mUrl = mUrl + key+"="+mParamsMap.get(key)+"&";
            }
            mUrl = mUrl.substring(0,mUrl.length()-1);
        }
    }


    /**
     * 设置头参数
     */
    private void setHeader() {
        if(mHeaderMap != null){
            for (String key: mHeaderMap.keySet()){
                mRequestBuilder.addHeader(key,mHeaderMap.get(key));
            }
        }
    }


    void execute(){
        mOkHttpClient.newCall(mOkHttpRequest).enqueue(new Callback() {
            @Override
            public void onFailure(final Call call, final IOException e) {
                if(mCallBack != null){
                    mCallBack.onError(call,e);
                }
            }
            @Override
            public void onResponse(final Call call, final Response response) throws IOException {
                if(mCallBack != null){
                    mCallBack.onSeccess(call,response);
                }
            }

        });
    }

    Response executeSyn() throws IOException {
        return mOkHttpClient.newCall(mOkHttpRequest).execute();
    }

}
