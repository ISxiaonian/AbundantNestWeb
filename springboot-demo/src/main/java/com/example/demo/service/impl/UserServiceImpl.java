package com.example.demo.service.impl;

import com.example.demo.service.UserService;
import com.example.demo.util.Connection;
import com.mks.api.Command;
import com.mks.api.Option;
import com.mks.api.response.Response;
import com.mks.api.response.WorkItem;
import com.mks.api.response.WorkItemIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

    @Autowired
    private Connection conn;

    @Override
    public String getUserName(String id) {
        try {
            log.info("用户Id:{}", id);
            Command cmd = new Command(Command.IM, "users");
            cmd.addOption(new Option("fields", "fullname,isActive"));
            cmd.addSelection(id);
            Response response = conn.execute(cmd);
            if (response != null) {
                WorkItemIterator workItems = response.getWorkItems();
                while (workItems.hasNext()) {
                    WorkItem item = workItems.next();
                    if (item.getField("isActive").getValueAsString().equalsIgnoreCase("true")) {
                        return item.getField("fullName").getValueAsString();
                    }
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return null;
    }

    @Override
    public List<String> findUsersName(List<String> ids) {
        return null;
    }
}
