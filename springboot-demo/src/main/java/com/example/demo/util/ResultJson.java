package com.example.demo.util;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Component;

@Component
public class ResultJson {

    public JSONObject success(String message) {
        JSONObject json = new JSONObject();
        json.put("code", 200);
        json.put("message", message);
        return json;
    }

    public JSONObject error(String message) {
        JSONObject json = new JSONObject();
        json.put("code", 400);
        json.put("message", message);
        return json;
    }

    public boolean isJSONValid(String jsonStr) {
        try {
            JSONObject.parseObject(jsonStr);
        } catch (JSONException ex) {
            try {
                JSONObject.parseArray(jsonStr);
            } catch (JSONException ex1) {
                return false;
            }
        }
        return true;
    }
}
