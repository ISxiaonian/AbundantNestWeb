package com.example.demo.service;

import com.alibaba.fastjson.JSONObject;

public interface ProjectService {
    /**
     * @param params {"parentProjectName":"","description":"","groups":"","projectName":"","administrators":""}
     * @return 返回成功或错误信息
     */
    JSONObject UpdateProject(JSONObject params);
}
