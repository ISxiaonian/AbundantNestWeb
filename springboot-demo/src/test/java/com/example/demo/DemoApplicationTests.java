package com.example.demo;

import com.example.demo.service.UserService;
import com.example.demo.util.Connection;
import com.example.demo.util.IntegrityUtil;
import com.example.demo.util.SessionPool;
import com.mks.api.Command;
import com.mks.api.Option;
import com.mks.api.response.Response;
import com.mks.api.response.WorkItem;
import com.mks.api.response.WorkItemIterator;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class DemoApplicationTests {

    private static final Logger log = LoggerFactory.getLogger(DemoApplicationTests.class);

    @Autowired
    private Connection conn;

    @Autowired
    private UserService user;

    @Test
    void getUserName() {
        String userName = user.getUserName("admin");
        log.info("userName:{}", userName);
    }

    @Test
    void sessionTest() {
        try {
           /* log.info("-----------------------");
            Command cmd = new Command(Command.IM, "users");
            cmd.addOption(new Option("fields", "name,fullname,email,isActive"));
            cmd.addSelection("admin");
            Response response = conn.execute(cmd);
            String result = IntegrityUtil.getResult(response);
            log.info(result);
            //Response execute = cmdRunner.execute(cmd);
            if (execute != null) {
                WorkItemIterator workItems = execute.getWorkItems();
                while (workItems.hasNext()) {
                    WorkItem item = workItems.next();
                    if (item.getField("isActive").getValueAsString().equalsIgnoreCase("true")) {
                        log.warn(String.valueOf(item));
                    }
                }
            }*/
        } catch (Exception e) {
            log.error("错误：{}", e.getMessage());
        }
    }
}
