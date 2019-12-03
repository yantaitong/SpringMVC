package com.cmbc.demo.xservice.impl;

import com.cmbc.demo.xservice.XTestService;
import com.cmbc.xspring.annotation.XService;

@XService
public class XTestServiceImpl implements XTestService {
    @Override
    public String listClassName() {

        return "XTestService 手写SpringMVC测试!";
    }
}
