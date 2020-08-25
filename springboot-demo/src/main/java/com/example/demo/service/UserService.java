package com.example.demo.service;


import java.util.List;

public interface UserService {

    /**
     * 获取单个用户的名称
     *
     * @return 名称
     */
    String getUserName(String id);

    /**
     * 获取多个用户的名称
     *
     * @return 名称List
     */
    List<String> findUsersName(List<String> ids);
}
