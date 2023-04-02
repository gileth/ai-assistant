package com.ganlansi.chat.util.okhttp;



import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import lombok.extern.slf4j.Slf4j;
import okhttp3.Call;
import okhttp3.Response;

/**
 * Created by fighting on 2017/4/7.
 */
@Slf4j
public abstract class CallBackUtil<T> {


    public  void onProgress(float progress, long total ){};

    public  void onError(final Call call, final Exception e){
        log.error("error ", e);
    };
    public  void onSeccess(Call call, Response response){
        final T obj = onParseResponse(call, response);
        log.info(obj.toString());
    };


    /**
     * 解析response，执行在子线程
     */
    public abstract T onParseResponse(Call call, Response response);

    /**
     * 访问网络失败后被调用，执行在UI线程
     */
    public abstract void onFailure(Call call, Exception e);

    /**
     *
     * 访问网络成功后被调用，执行在UI线程
     */
    public abstract void onResponse(T response);


    public static abstract class CallBackDefault extends CallBackUtil<Response>{
        @Override
        public Response onParseResponse(Call call, Response response) {
            return response;
        }
    }

    public static abstract class CallBackString extends CallBackUtil<String>{
        @Override
        public String onParseResponse(Call call, Response response) {
            try {
                return response.body().string();
            } catch (IOException e) {
                new RuntimeException("failure");
                return "";
            }
        }
    }


    /**
     * 下载文件时的回调类
     */
    public static abstract class CallBackFile extends CallBackUtil<File>{

        private final String mDestFileDir;
        private final String mdestFileName;

        /**
         *
         * @param destFileDir:文件目录
         * @param destFileName：文件名
         */
        public CallBackFile(String destFileDir, String destFileName){
            mDestFileDir = destFileDir;
            mdestFileName = destFileName;
        }
        @Override
        public File onParseResponse(Call call, Response response) {

            InputStream is = null;
            byte[] buf = new byte[1024*8];
            int len = 0;
            FileOutputStream fos = null;
            try{
                is = response.body().byteStream();
                final long total = response.body().contentLength();

                long sum = 0;

                File dir = new File(mDestFileDir);
                if (!dir.exists()){
                    dir.mkdirs();
                }
                File file = new File(dir, mdestFileName);
                fos = new FileOutputStream(file);
                while ((len = is.read(buf)) != -1){
                    sum += len;
                    fos.write(buf, 0, len);
                    final long finalSum = sum;

                }
                fos.flush();

                return file;

            } catch (Exception e) {
                e.printStackTrace();
            } finally{
                try{
                    response.body().close();
                    if (is != null) is.close();
                } catch (IOException e){
                }
                try{
                    if (fos != null) fos.close();
                } catch (IOException e){
                }

            }
            return null;
        }
    }

}