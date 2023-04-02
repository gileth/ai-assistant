package com.ganlansi.chat.controller;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.map.MapUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.ganlansi.chat.bean.ApiResult;
import com.ganlansi.chat.bean.airesp.ChatCompletion;
import com.ganlansi.chat.conf.MyProperties;
import com.ganlansi.chat.enums.ReturnCodeEnum;
import com.ganlansi.chat.exception.BusinessException;
import com.ganlansi.chat.util.okhttp.CallBackUtil;
import com.ganlansi.chat.util.okhttp.OkhttpUtil;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Call;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping(value = "/chat", method = RequestMethod.POST)
public class ChatController {

    @Autowired
    private MyProperties myProperties;

    @ResponseBody
    @RequestMapping(value = "/doChat", method = RequestMethod.POST, produces = "application/json")
    public ApiResult<String> chatWithChatGPT(@RequestBody Map<String, Object> taskParam)  {
        Map<String, Object> respMap = new HashMap<>();
        if (MapUtil.isEmpty(taskParam)) {
            log.error("参数为空");
            return ApiResult.error(ReturnCodeEnum.SYSTEM_REQ_PARAM_ILLEGAL);
        }
        String message = (String) taskParam.get("message");

        try {
            Map<String, String> headMap = new HashMap<>();
            headMap.put("Content-Type", "application/json;charset=utf-8");
            headMap.put("Authorization", "Bearer  " + myProperties.getOpenaiApiKey());

            JSONObject jsonObject = new JSONObject();
            jsonObject.put("model", myProperties.getOpenaiApiModel());
            JSONObject messageObj = new JSONObject();
            messageObj.put("role", "user");
            messageObj.put("content", message);
            jsonObject.put("messages",  new JSONArray().fluentAdd(messageObj));
            log.info("请求参数：{}", jsonObject);
            Response response = OkhttpUtil.okHttpPostJson(myProperties.getHttpsProxy(), jsonObject.toJSONString(), headMap);
            String chatGPTResp = response.body().string();
            log.info("chatGPT response : {}",chatGPTResp);

            if (response.isSuccessful()) {
                ChatCompletion resp = JSON.parseObject(chatGPTResp, ChatCompletion.class);
                if (resp.getChoices() != null && resp.getChoices().size() > 0) {
                    return ApiResult.success(resp.getChoices().get(0).getMessage().getContent());
                }

            } else {
                log.error("chatGPT 官方接口响应失败：{}", response.body().string());
                return ApiResult.error(ReturnCodeEnum.CHAT_GPT_API_REQUEST_FAIL);
            }
        } catch (Exception e) {
            log.error("chatWithChatGPT fail ", e);
        }

        return ApiResult.error(ReturnCodeEnum.SYSTEM_EXCEPTION);
    }


}
