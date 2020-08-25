package com.example.demo.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.example.demo.service.DocumentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/document")
public class DocumentController {

    private static final Logger log = LoggerFactory.getLogger(DocumentController.class);

    @Autowired
    private DocumentService doc;

    /**
     * @param params json格式 params="{"Project":"/TestProject","Type":"Stakeholder Requirement Document"}"
     * @return list<文档信息>
     */
    @RequestMapping(value = "/find", method = RequestMethod.GET)
    public List<JSONObject> FindDocument(String params) {
        log.info("params:{}", params);
        return doc.FindDocument(JSON.parseObject(params));
    }
}
