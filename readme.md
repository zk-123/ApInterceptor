## AspectInterceptor
该项目时一个临时站点，将于不久删除。

aspectInterceptor见名知意，是一个以aop开发为基础的interceptor(当然是基于Spring的)。主要用于解决controller层随着业务的复杂，输入的参数判断也过于多，过于复杂，从而导致不易看清逻辑的麻烦现状；如果任由这么多复杂判断逻辑存在，每次增加需求都是往controller层堆积代码，必然会导致不易阅读和不易维护；所以解决的方法就显而易见了，如果能把复杂的判断放到一处进行处理和管控，必然是更整洁和舒心的。

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