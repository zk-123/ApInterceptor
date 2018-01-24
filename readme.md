## AspectInterceptor
该项目时一个临时站点，将于不久删除。

aspectInterceptor见名知意，是一个以aop开发为基础的interceptor(当然是基于Spring的)。主要用于解决controller层随着业务的复杂，输入的参数判断也过于多，过于复杂，从而导致不易看清逻辑的麻烦现状；如果任由这么多复杂判断逻辑存在，每次增加需求都是往controller层堆积代码，必然会导致不易阅读和不易维护；所以解决的方法就显而易见了，如果能把复杂的判断放到一处进行处理和管控，必然是更整洁和舒心的。

## 例子说起


### 场景一
假如你为一个书店老板写一个图书网站。考虑会员购买图书的场景。在购书前，必须确保该图书有货，必须确保该书能够购买(不是限定或纪念版)，必须确保该用户是缴过费会员，必须确保会员有足够多的书币，必须校验该用户已经登录，必须校验会员有足够的信用，必须校验会员已绑定手机号。。。。。。我们可能这样写。
```
@RequestMapping(value = "/buyBook",method = RequestMethod.POST)
public void buyBookById(HttpServletRequest request,HttpServletResponse response,@RequestParam("id")String id){
    if(校验该图书有货){

    }
    if(该书能够购买){

    }
    if(用户已经登录){

    }
    if(该用户是会员){

    }
    if(会员有足够多的书币){

    }
    if(会员有足够的信用){

    }
    if(已绑定手机号){

    }
    ....
    bookservice.buybook(user,book);
   
}
```

随着业务更加复杂，要添加的内容还有更多；往往很多能在进入control前的校验，我们把它堆积到了一起；可能有人会说：我们也可以把这些校验放到controller的一个方法里进行调用，这样看起来不算是乱了吧。

这样导致：
+ 在controller中本来就是描述服务调用关系，存放这些的代码会造成一个结果---------易读性更为差的代码。
+ 进一步说，这些代码有些可以重用，比如：检查是否是会员，检查用户是否绑定手机号........等等等。

如果能把这些代码放到别的地方可能效果会更好，所以我们可以用这个插件这样写：
```
@BeforeProcess(advice = {VIPCheck.class, BindingPhoneCheck.class, BuyBookCheck.class})
@RequestMapping(value = "/buyBook",method = RequestMethod.POST)
public void buyBookById(HttpServletRequest request,HttpServletResponse response,@RequestParam("id")String id){
    bookservice.buybook(user,book);
}
```
这样写是不是看起来更舒心点了，下面介绍下该用法：
+ 增加了BeforeProcess注解：该注解是声明在方法上的注解。其中的advice值是一个Class数组，该数组里存放指定的校验类，校验会按照类存放的顺序执行，如果校验失败则会进行相应的处理，不会进入该方法。
+ 如上述代码，该advice中包含了，VIP校验，用户绑定手机号校验，以及其他校验。这样做到了 VIP校验，用户绑定手机号校验可以在其他地方重用。
+ 校验类：
```
@Component
public class VIPCheck implements HttpAdvice{
    
    @Autowired
    private UserManagerService userManagerService;
    
    @Override
    public void doAdvice(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Map<String, Object> map) throws AdviceException {
        User user = (User) httpServletRequest.getSession().getAttribute("user");
        boolean vip = userManagerService.isVip(user);
        if(!vip){
            throw new AdviceException(ResultBean.failResult("必须是会员"));
        }
    }
}
```
1、校验类需要继承一个HttpAdvice（目前仅支持ajax返回json结果校验）,并实现其doAdvice()方法，如果校验失败，抛出`AdviceException`异常并传入一个ResultBean参数(返回结果形式详见：ResultBean)。

2、doAdvice参数：HttpServletRequest,httpServletResponse 为请求的request和response。最后一个 Map 是将被拦截的方法参数以key,value形式写入。如：该VIPCheck中map中包含三个成员：request -> request的值, response -> response的值,id-> id的值 

  
### 场景二
还是书店老板，还是你写的网站，这次要求写一个添加图书的请求。你可能会这样写


说了这么多，该项目(称之为插件比较好),该插件是如何简化上述说的这些的？

+ 基于注解：仅仅简单增加一个注解就能使复杂的业务参数判断消失的无影无踪
+ 不冲突：具体的不冲突是指不与之前写过的代码冲突，不是引入该插件，就必须把之前的代码重构或重写
+ 配置简单，易引用：这是一个独立的jar文件，仅仅通过maven地址引入即用


## 引入

如：暂时公司内部使用

```xml
<dependency>
   <groupId>com.xdja</groupId>
   <artifactId>aspectInterceptor</artifactId>
   <version>1.0-SNAPSHOT</version>
</dependency>
```

## 使用
在Controller中使用，加入`@BeforeProcess`注解
```
@Controller
public class IndexController {

    @BeforeProcess({@BeforeInterceptor(value = AspectTest.class,method = "doSomething"),
            @BeforeInterceptor(value = AspectTest.class,method = "doSomething"),
            @BeforeInterceptor(value = AspectTest.class,method = "doSomething")})
    @RequestMapping(value = {"/","/index"},method = RequestMethod.GET)
    @ResponseBody
    public String index(HttpServletRequest request, HttpServletResponse response, ModelMap modelMap){
        return "hello";
    }
}
```

+ `@BeforeProcess`注解必须在有`@RequestMapping`的地方使用，也是体现了它作为拦截器的体现(虽然调用的是aop，与interceptor毫不相关，但是对外表现的行为就是一个拦截器,所以这里用拦截器来称呼它)
+ `@BeforeProcess` 注解中包含一个`@BeforeInterceptor`数组，一个`@BeforeInterceptor`包含具体的拦截器实现类和方法
+ `@BeforeInterceptor`调用顺序和数组顺序一致
+ 被调用类为一个Bean(即Spring Bean `@Component`注解修饰)，除此之外，无其他要求

AspectTest 类

```
@Component
public class AspectTest {
    public void doSomething(HttpServletRequest request, ModelMap modelMap,Map<String, Object> transportData){
        System.out.println(request.getRequestURL().toString());
        System.out.println("do something");
    }
}

```
+ AspectTest 类无其他特点，需要注意的就是其被调用的方法。被调用的方法参数可以为一到多个，参数一般为被拦截的Controller的方法里的参数，可以随意填写多少，必须的是被调用方法的参数名必须和被拦截的Controller的方法参数名一致。
+ 大家应该注意到了还多出来一个`Map<String, Object> transportData`参数。这个参数是Controller的方法参数中没有的变量，它是主要用来存放所有Controller的方法参数(key)和值(value)的一个介质。除此之外，它还有一个作用就是可以在Interceptor中携带变量进行传输

## 至此
至此，简单的一个雏形已经完成了，但是也只是一个雏形，还有很多工作还未做。通过该雏形，可以看出一个面向aop思想的应用，也在考虑是否有必要继续做下去。
### TODO
+ `@AfterProcess`注解
+ 拦截器中断
+ 返回值的统一处理
+ 不仅仅基于简单的判断拦截器，权限以及其他的一些可以放到aop的切面工作