package com.example.demo.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.example.demo.service.ProjectService;
import com.example.demo.util.Connection;
import com.example.demo.util.ResultJson;
import com.mks.api.Command;
import com.mks.api.Option;
import com.mks.api.response.APIException;
import com.mks.api.response.Response;
import com.mks.api.response.WorkItem;
import com.mks.api.response.WorkItemIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ProjectServiceImpl implements ProjectService {

    private static final Logger log = LoggerFactory.getLogger(ProjectServiceImpl.class);

    @Autowired
    private Connection conn;
    @Autowired
    private ResultJson json;

    @Override
    public JSONObject UpdateProject(JSONObject params) {
        try {
            log.info("params:{}", params.toJSONString());
            //判断projectName是否存在
            String projectName = params.getString("projectName");
            if (projectName.isEmpty()) {
                return json.error("项目名称不能为空！");
            }
            boolean flag = checkProject(projectName);
            Command cmd = new Command("im", flag ? "createproject" : "editproject");
            cmd.addOption(new Option("description", params.getString("description")));

            String parentProjectName = params.getString("parentProjectName");
            if (!parentProjectName.isEmpty()) {
                cmd.addOption(new Option("parent", String.format("/%s", parentProjectName)));
            }
            String administrators = params.getString("administrators");
            if (!administrators.isEmpty()) {
                cmd.addOption(new Option("permittedAdministrators", String.format("u=%s", administrators)));
            }
            cmd.addOption(new Option("permittedGroups", params.getString("groups")));
            String text;
            if (flag) {
                text = "新增";
                cmd.addOption(new Option("name", projectName));
            } else {
                text = "修改";
                cmd.addSelection(String.format("/%s", projectName));
            }
            conn.execute(cmd);
            log.info(String.format("%s成功", text));
            return json.success(String.format("%s成功", text));
        } catch (APIException e) {
            log.error("错误：{}", e.getMessage());
            if (e.getMessage() == null) {
                return json.error("请检查传入的参数parentProjectName,administrators,groups是否存在错误！");
            }
            return json.error(e.getMessage());
        }
    }

    private boolean checkProject(String name) {
        log.info("判断项目名称：{}", name);
        try {
            Command cmd = new Command("im", "projects");
            Response response = conn.execute(cmd);
            if (response != null) {
                WorkItemIterator workItems = response.getWorkItems();
                while (workItems.hasNext()) {
                    WorkItem itm = workItems.next();
                    if (itm.getId().equals(String.format("/%s", name).trim())) {
                        return false;
                    }
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return true;
    }
}
