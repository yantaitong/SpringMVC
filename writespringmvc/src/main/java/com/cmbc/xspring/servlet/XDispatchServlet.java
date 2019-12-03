package com.cmbc.xspring.servlet;

import com.cmbc.xspring.annotation.XAutowired;
import com.cmbc.xspring.annotation.XController;
import com.cmbc.xspring.annotation.XRequestMapping;
import com.cmbc.xspring.annotation.XService;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;

public class XDispatchServlet extends HttpServlet {


    /**
     * 属性配置文件
     */
    private Properties contextConfig = new Properties();


    private List<String> classNameList = new ArrayList<>();

    /**
     * IOC容器
     */
    Map<String,Object> iocMap = new HashMap<>();

    Map<String, Method> handleMapping = new HashMap<>();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        /**
         * HTTP status 405-Method not Allowed
         */
        //super.doPost(req, resp);//get请求返回405
        this.doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        //运行阶段
        try {
            doDispatch(req,resp);
        } catch (Exception e) {
            e.printStackTrace();
            resp.getWriter().write("500 Exception Detail:\n" + Arrays.toString(e.getStackTrace()));
        }
    }

    /**
     * 运行阶段 进行拦截、匹配
     * @param req 请求
     * @param resp 响应
     */
    private void doDispatch(HttpServletRequest req, HttpServletResponse resp) throws InvocationTargetException, IllegalAccessException {
        String url = req.getRequestURI();
        String contextPath = req.getContextPath();
        url = url.replaceAll(contextPath,"").replaceAll("/+","/");

        System.out.println("request url ---->"+url);

        if (!this.handleMapping.containsKey(url)){
            try {
                resp.getWriter().write("404 NOT FOUND");
                return;
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        Method method = this.handleMapping.get(url);
        System.out.println("method---->"+method);

        String beanName = toLowerFirstCase(method.getDeclaringClass().getSimpleName());
        System.out.println("iocMap.get(beanName)-----》"+iocMap.get(beanName));

        //第一个参数是获取方法，后面是参数，多个参数直接加，按顺序对应
        method.invoke(iocMap.get(beanName),req,resp);

        System.out.println("method.invoke put {"+iocMap.get(beanName)+"}.");
    }

    /**
     * 获取类的首字母小写的名称
     * @param className
     * @return
     */
    private String toLowerFirstCase(String className) {
        char[] chars = className.toCharArray();
        chars[0]+=32;
        return String.valueOf(chars);
    }

    @Override
    public void destroy() {
        super.destroy();
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        //1、加载配置文件
        doLoadConfig(config.getInitParameter("contextConfigLocation"));
        //2、扫描相关的类
        doScanner(contextConfig.getProperty("scan-package"));
        //3、初始化IOC容器，将所有的相关的类实例保存到IOC容器中
        doInstance();
        //4、依赖注入
        doAuowired();
        //5、初始化 HandleMapping
        initHandleMapping();
        System.out.println("XSpring Framework is init");
        //6、打印数据
        doTestPrintData();
    }



    /**
     * 打印数据
     */
    private void doTestPrintData() {
        System.out.println("----------------data---------------------");
        System.out.println("contextConfig.propertyNames()-->"+contextConfig.propertyNames());

        System.out.println("classNameList--->");
        for (String className : classNameList) {
            System.out.println(className);
        }

        System.out.println("iocMap--->");
        for (Map.Entry<String, Object> iocEntry : iocMap.entrySet()) {
            System.out.println(iocEntry);
        }

        System.out.println("handleMapping--->");
        for (Map.Entry<String, Method> methodEntry : handleMapping.entrySet()) {
            System.out.println(methodEntry);
        }

        System.out.println("---------------------done-----------------");
        System.out.println("====启动成功=====");


        System.out.println("测试地址：http://localhost:8080/test/query?userName=smile");
        System.out.println("测试地址：http://localhost:8080/test/listClassName");
    }

    /**
     * 5、初始化HandleMapping
     */
    private void initHandleMapping() {
        if (iocMap.isEmpty()){
            return;
        }

        for (Map.Entry<String, Object> iocEntry : iocMap.entrySet()) {
            Class<?> aClass = iocEntry.getValue().getClass();

            if (!aClass.isAnnotationPresent(XController.class)){
                continue;
            }

            String baseUrl="";

            if (aClass.isAnnotationPresent(XRequestMapping.class)){
                XRequestMapping xRequestMapping = aClass.getAnnotation(XRequestMapping.class);
                baseUrl = xRequestMapping.value();
            }

            for (Method method : aClass.getMethods()) {
                if (!method.isAnnotationPresent(XRequestMapping.class)){
                    continue;
                }
                XRequestMapping xRequestMapping = method.getAnnotation(XRequestMapping.class);
                String url = ("/"+baseUrl+xRequestMapping.value()).replaceAll("/+","/");

                handleMapping.put(url,method);
                System.out.println("[INFO-5] handleMapping put {"+url+"} - {"+method+"}.");
            }
        }
    }


    /**
     * 4、依赖注入
     */
    private void doAuowired() {
        if (iocMap.isEmpty()){
            return;
        }
        for (Map.Entry<String, Object> iocEntry : iocMap.entrySet()) {
            Field[] fields = iocEntry.getValue().getClass().getDeclaredFields();

            for (Field field : fields) {
                if (!field.isAnnotationPresent(XAutowired.class)){
                    continue;
                }
                System.out.println("[INFO-4] Existence XAutowired");
                //获取注解对应的类
                XAutowired xAutowired = field.getAnnotation(XAutowired.class);
                String beanName = xAutowired.value().trim();

                //获取XAutowired注解的值
                if ("".equals(beanName)){
                    System.out.println("[INFO-4] XAutowired.value is null");
                    beanName = field.getType().getName();
                }

                //只要加了注解，都要加载，不管是private还是protect
                field.setAccessible(true);

                try {
                    field.set(iocEntry.getValue(),iocMap.get(beanName));
                    System.out.println("[INFO-4] field set {"+iocEntry.getValue()+"} - {"+iocMap.get(beanName)+"}.");

                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }


            }
        }

    }

    /**
     * 3、初始化IOC容器，将所有相关的类实例保存到IOC容器中
     */
    private void doInstance() {
        if (classNameList.isEmpty()){
            return;
        }
       try{
           for (String className : classNameList) {
               Class<?> aClass = Class.forName(className);

               if (aClass.isAnnotationPresent(XController.class)){
                   String beanName = toLowerFirstCase(aClass.getSimpleName());
                   Object object = aClass.newInstance();

                   //保存到IOC容器中
                   iocMap.put(beanName,object);
                   System.out.println("[INFO-3] {" + beanName + "} has bean saved in iocMap");

               }else if(aClass.isAnnotationPresent(XService.class)){
                   String beanName  = toLowerFirstCase(aClass.getSimpleName());
                   //如果注解包含自定义名称
                   XService xService = aClass.getAnnotation(XService.class);
                   if (!"".equals(xService.value())){
                       beanName = xService.value();
                   }

                   Object object = aClass.newInstance();

                   //保存到IOC容器中
                   iocMap.put(beanName,object);
                   System.out.println("[INFO-3] {" + beanName + "} has bean saved in iocMap");

                    //找类的接口
                   for (Class<?> anInterface : aClass.getInterfaces()) {
                       if (iocMap.containsKey(anInterface.getName())){
                           throw new Exception("The Bean Name is Exist!");
                       }
                       iocMap.put(anInterface.getName(),object);
                       System.out.println("[INFO-3] {" + anInterface.getName() + "} has bean saved in iocMap");
                   }

               }
           }
       }catch (Exception e){
           e.printStackTrace();
       }
    }

    /**
     * 2、扫描相关的类
     * @param scanPackage
     */
    private void doScanner(String scanPackage) {
        URL resourcePath = this.getClass().getClassLoader().getResource("/"+scanPackage.replaceAll("\\.","/"));

        if (resourcePath==null){
            return;
        }
        File classPath = new File(resourcePath.getFile());

        for (File file : classPath.listFiles()) {

            if (file.isDirectory()){
                System.out.println("[INFO-2] {" + file.getName()+"} is a directory");

                //子目录递归
                doScanner(scanPackage+"."+file.getName());
            }else{
                if (!file.getName().endsWith(".class")){
                    System.out.println("[INFO-2] {"+file.getName()+"} is not a class file.");
                    continue;
                }
                String className =  (scanPackage+"."+file.getName().replace(".class",""));

                //保存在内容
                classNameList.add(className);
                System.out.println("[INFO-2] {"+className+"} has bean saved in classNameList.");

            }
        }
    }

    /**
     * 加载配置文件
     * @param contextConfigLocation
     */
    private void doLoadConfig(String contextConfigLocation) {
        System.out.println("contextConfigLocation----->"+contextConfigLocation);
        URL resource = this.getClass().getClassLoader().getResource(contextConfigLocation);
        System.out.println("urlPath=="+resource.getPath());
        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(contextConfigLocation);
        try{
            contextConfig.load(inputStream);
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            if (null!=inputStream){
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
