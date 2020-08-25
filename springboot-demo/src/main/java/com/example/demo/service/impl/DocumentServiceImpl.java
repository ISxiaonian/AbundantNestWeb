package com.example.demo.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.example.demo.service.DocumentService;
import com.example.demo.service.UserService;
import com.example.demo.util.AnalysisXML;
import com.example.demo.util.Connection;
import com.example.demo.util.IntegrityFactory;
import com.mks.api.Command;
import com.mks.api.Option;
import com.mks.api.Session;
import com.mks.api.response.Response;
import com.mks.api.response.WorkItem;
import com.mks.api.response.WorkItemIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


@Service
public class DocumentServiceImpl implements DocumentService {

    private static final Logger log = LoggerFactory.getLogger(DocumentServiceImpl.class);

    @Autowired
    private Connection conn;

    @Autowired
    private IntegrityFactory integrity;

    @Autowired
    private AnalysisXML xml;

    @Autowired
    private UserService user;

    @Override
    public List<JSONObject> FindDocument(JSONObject params) {
        log.info("params:{}", params);
        try {
            List<JSONObject> jsonList = new ArrayList<>();
            List<String> list = xml.resultFile("documentFile");
            ArrayList<String> fileList = new ArrayList<>();
            list.forEach(i -> {
                //移除Url,修改人，创建人等字段
                if (!("Url".equals(i) || "Created By Name".equals(i) || "Modified By Name".equals(i))) {
                    fileList.add(i);
                }
            });
            Command cmd = new Command("im", "issues");
            cmd.addOption(new Option("queryDefinition", query(params)));
            cmd.addOption(new Option("fields", String.join(",", fileList)));
            Response response = conn.execute(cmd);
            if (response != null) {
                WorkItemIterator workItems = response.getWorkItems();
                while (workItems.hasNext()) {
                    WorkItem itm = workItems.next();
                    JSONObject jsonFile = new JSONObject();
                    list.forEach(file -> {
                        switch (file) {
                            case "Url":
                                jsonFile.put(file, String.format("integrity:%s:%s/im/viewissue?selection=%s", integrity.getHost(), integrity.getPort(), itm.getId()));
                                break;
                            case "Modified By Name":
                                break;
                            case "Created By Name":
                                break;
                            case "Modified By":
                            case "Created By":
                                jsonFile.put(String.format("%s Name", file), user.getUserName(itm.getField(file).getValueAsString()));
                            default:
                                jsonFile.put(file, itm.getField(file).getValueAsString());
                        }
                    });
                    jsonList.add(jsonFile);
                }
            }
            return jsonList;
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return null;
    }


    private String query(JSONObject params) {
        List<String> list = new ArrayList<>();
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            if (!String.valueOf(entry.getValue()).isEmpty()) {
                list.add(String.format("(field[%s]=%s)", entry.getKey(), entry.getValue()));
            }
        }
        if (list.size() > 0) {
            return String.format("(%s)", String.join(" and ", list));
        }
        return null;
    }
}
