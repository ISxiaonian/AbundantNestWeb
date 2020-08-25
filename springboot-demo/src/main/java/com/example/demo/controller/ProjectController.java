package com.example.demo.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.example.demo.service.ProjectService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/project")

public class ProjectController {

    private static final Logger log = LoggerFactory.getLogger(ProjectController.class);

    @Autowired
    private ProjectService projectService;


    /**
     * @param params json格式 params="{"parentProjectName":"","description":"","groups":"","projectName":"","administrators":""}"
     * @return 成功或错误信息
     */
    @RequestMapping(value = "/update", method = RequestMethod.GET)
    public JSONObject UpdateProject(String params) {
        log.info("params:{}", params);
        return projectService.UpdateProject(JSON.parseObject(params));
    }
}
