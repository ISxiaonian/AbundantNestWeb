package com.example.demo.service;

import com.alibaba.fastjson.JSONObject;

import java.util.List;

public interface DocumentService {
    /**
     * @param params json格式的参数
     * @return document文件信息
     */
    List<JSONObject> FindDocument(JSONObject params);
}
