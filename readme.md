## aValidate
利用简单的aop逻辑，将校验逻辑分离。

### 例子说起
假如你为一个书店老板写一个图书网站。考虑会员购买图书的场景。在购书前，必须确保该图书有货，必须确保该书能够购买(不是限定或纪念版)，必须确保该用户是缴过费会员，必须确保会员有足够多的书币，必须校验该用户已经登录，必须校验会员有足够的信用，必须校验会员已绑定手机号。。。。。。我们可能这样写。

```java
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

业务场景往往都是复杂的，能单独把校验校验逻辑分离开来，会使业务线更加清晰，增加校验的复用性，还能让你心情愉快。

你可以这样写：
```java
@BeforeProcess(advice = {VIPCheck.class, BindingPhoneCheck.class, BuyBookCheck.class})
@RequestMapping(value = "/buyBook",method = RequestMethod.POST)
public void buyBookById(HttpServletRequest request,HttpServletResponse response,@RequestParam("id")String id){
    bookservice.buybook(user,book);
}
```
通过`BeforeProcess`注解将一系列校验逻辑通过注解形式顺序列出。

+ 校验类：
```java
@Component
public class VIPCheck implements HttpAdvice{
    
    @Autowired
    private UserManagerService userManagerService;
    
    @Override
    public void doAdvice(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Map<String, Object> map) throws InvokeException{
        User user = (User) httpServletRequest.getSession().getAttribute("user");
        boolean vip = userManagerService.isVip(user);
        if(!vip){
            throw new InvokeException(ResultBean.failResult("必须是会员"));
        }
    }
}
```
### BeforeProcess注解使用

 `BeforeProcess`注解包含两个属性：advice和validate。advice需要的值为一系列公共校验逻辑，其传参为基本的request, response，以及封装方法参数的map；
 validate为一些有特性的方法一般为方法上特有的校验，其传参为所校验方法上的参数(可部分)。

在校验逻辑中，通过抛出异常的方式通知校验失败；稍后可以定义校验失败的逻辑以返回正常的视图和数据。

点击[这里](https://github.com/zk-123/aValidate-demo)查看demo
## 引入

如：

```xml
<dependency>
   <groupId>com.zkdcloud</groupId>
   <artifactId>aValidate</artifactId>
   <version>1.0.1</version>
</dependency>
```
