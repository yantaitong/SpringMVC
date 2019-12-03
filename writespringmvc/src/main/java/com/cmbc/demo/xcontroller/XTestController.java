package com.cmbc.demo.xcontroller;

import com.cmbc.demo.xservice.XTestService;
import com.cmbc.demo.xservice.impl.XTestServiceImpl;
import com.cmbc.xspring.annotation.XAutowired;
import com.cmbc.xspring.annotation.XController;
import com.cmbc.xspring.annotation.XRequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@XController
@XRequestMapping(value = "/test")
public class XTestController {

    @XAutowired
    private XTestService xTestService;

    /**
     * 测试方法 /test/query
     * @param req
     * @param resp
     */
    @XRequestMapping(value = "/query")
    public void query(HttpServletRequest req, HttpServletResponse resp){
        if (req.getParameter("userName")==null || req.getParameter("userName")==""){
            try {
                resp.getWriter().write("param userName is null");
                return;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        String userName = req.getParameter("userName");
        try {
            resp.setHeader("content-type","text/html;charset=utf-8");
            resp.getWriter().write("para userName is "+userName);
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("[INFO-req] New request param userName -->"+userName);
    }


    @XRequestMapping("/listClassName")
    public void listClassName(HttpServletRequest req,HttpServletResponse resp){

        String s = xTestService.listClassName();
        System.out.println("xTestService---->"+s);
        try{
            resp.setHeader("content-type","text/html;charset=utf-8");
            resp.getWriter().write(s);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

}
