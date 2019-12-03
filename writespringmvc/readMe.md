##手写SpringMVC
###整体思路

####1）配置阶段
> 配置web.xml
 - <\servlet>
 - XDispatchServlet
 - 设定init-param:contextConfigLocation=applicationContext.properties
 - <\servlet-mapping>
 - 设定url-pattern:/*
> 配置Annotation：
- @XController 
- @XService
- @Autowired
- @XRequestMapping

####2）、初始化阶段
- IOC
    - 调用init()方法：加载配置文件
    - IOC容器初始化：Map<String,Object>
    - 扫描相关的类：scan-package = "com.cmbc"
    - 创建实例化并保存到容器：通过反射机制将类实例化放入IOC容器中
- DI
    - 进行DI操作：扫描IOC容器中的实例，给没有赋值的属性自动赋值
    
- MVC
    - 初始化HandleMapping：将一个URL和一个Method进行一对一关联映射Map<String，Method>
    
####4）、运行阶段
- 调用doGet()/doPost()方法：web容器调用doGet()/doPost()方法，获得request/response对象
- 匹配HandleMapping：从request对象中获得用户输入的url，找到其对应的方法Method
- 反射调用：method.invoker():利用反射调用方法并返回结果
- response.getWrite().write():将返回结果输出到浏览器     


