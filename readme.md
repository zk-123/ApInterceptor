## AspectInterceptor
该项目时一个临时站点，将于不久删除。

aspectInterceptor见名知意，是一个以aop开发为基础的interceptor(当然是基于Spring的)。主要用于解决controller层随着业务的复杂，输入的参数判断也过于多，过于复杂，从而导致不易看清逻辑的麻烦现状；如果任由这么多复杂判断逻辑存在，每次增加需求都是往controller层堆积代码，必然会导致不易阅读和不易维护；所以解决的方法就显而易见了，如果能把复杂的判断放到一处进行处理和管控，必然是更整洁和舒心的。

## 例子说起


### 场景一
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

随着业务更加复杂，要添加的内容还有更多；往往很多能在进入control前的校验，我们把它堆积到了一起；可能有人会说：我们也可以把这些校验放到controller的一个方法里进行调用，这样看起来不算是乱了吧。

这样导致：
+ 在controller中本来就是描述服务调用关系，存放这些的代码会造成一个结果---------易读性更为差的代码。
+ 进一步说，这些代码有些可以重用，比如：检查是否是会员，检查用户是否绑定手机号........等等等。

如果能把这些代码放到别的地方可能效果会更好，所以我们可以用这个插件这样写：
```java
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
1、校验类需要继承一个HttpAdvice（目前仅支持ajax返回json结果校验）,并实现其doAdvice()方法，如果校验失败，抛出`InvokeException`异常并传入一个ResultBean参数(返回结果形式详见：ResultBean)。

2、doAdvice参数：HttpServletRequest,httpServletResponse 为请求的request和response。最后一个 Map 是将被拦截的方法参数以key,value形式写入。如：该VIPCheck中map中包含三个成员：request -> request的值, response -> response的值,id-> id的值 

  
### 场景二
还是书店老板，还是你写的网站，这次要求写一个购买图书的请求。你可能会这样写
```java
@RequestMapping(value = "/buyBook",method = RequestMethod.POST)
public void buyBook(Order order){
    if(StringUtils.isEmpty(order.getBookName())){
        //处理为null的请求
    }
    if(StringUtils.isEmpty(order.getBookContent())){
        //处理为null的请求
    }
    ...
    bookService.buyBook(order);
}
```
按照上一个例子的思路，我们可以改成这样
```java
@BeforeProcess(advice = {BuyBook.class})
@RequestMapping(value = "/buyBook",method = RequestMethod.POST)
public void buyBook(Order order){
    bookService.buyBook(order);
}
```
后来，你寻思着，难道每次校验我都单独写一个校验类？如果有的校验只有两行代码，还要写一个类吗？很多时候，校验的场景会很多，但是代码量会很少；比如一个controller类中有好几处校验场景，而且这些校验往往都是特定的，重用的很少。

面对这样的场景，小插件也是有办法解决的~，废话不多说，直接上例子：

```java
@BeforeProcess(validate = {@Validate(value = BookControllerValidate.class,method = "buyBook")})
@RequestMapping(value = "/buyBook",method = RequestMethod.POST)
public void buyBook(Order order){
    bookService.buyBook(order);
}
```
通过这个例子我们可以看出：
+ 还是@BeforeProcess注解，只不过选项换成了`validate`,`validate`里存放多个校验**类.方法**,用`@Validate`注解包裹。其含义和advice选项差不多，只不过精确到方法了
+ 查看校验类`BookControllerValidate.class`中的内容：
```
@Component
public class BookControllerValidate {
    /**
     * 添加图书校验
     *
     * @param book book
     * @throws InvokeException
     */
    public void addBook(Book book) throws InvokeException {
        if(StringUtils.isEmpty(book.getBookName())){
            throw new InvokeException(ResultBean.failResult("书名不能为空"));
        }
        if(StringUtils.isEmpty(book.getBookContent())){
            throw new InvokeException(ResultBean.failResult("书内容不能为空"));
        }
    }

    /**
     * 购买图书校验
     *
     * @param order order
     * @throws InvokeException
     */
    public void buyBook(Order order) throws InvokeException {
        if(StringUtils.isEmpty(order.getBookName())){
            throw new InvokeException(ResultBean.failResult("图书必选"));
        }

        if(StringUtils.isEmpty(order.getNumber())){
            throw new InvokeException(ResultBean.failResult("数量必选"));
        }
    }
}
```
1、首先`BookControllerValidate`是一个Spring Bean，并没有继承任何接口或类。

2、然后其中的校验方法可以有多个(这一点应该早就料想到了)，校验方法和`advice`选项相似，都是通过抛出`InvokeException(ResultBean)`异常来终止校验

3、与`advie`选项不同的是，校验方法的**参数**可以随意多少。随意并不意味着可以随便写与业务无关的参数，参数必须是被调用方法(如：购买图书校验的参数，必须是`BookController`中`/buyBook`请求中的参数)，甚至可以加`HttpServletRequest`和`HttpSerlvetResponse`。但是这样灵活的参数，也往往牺牲了它的重用性。

4、方法参数除了3、介绍的这几种，还有一个与`advice`选项一样的`Map`参数,用来在校验过程中传输必要的数据，这里的`Map`和`advice`中的`Map`参数完全等价。

### 还有吗？
单单的校验可能满足我们绝大部分的需求了。不管是重用校验还是特殊校验，都提供了很好的支持。

但是还有。。。。还有进入Controller方法中的一些辅助操作，例子说起：
```java
@BeforeProcess(advice = {VIPCheck.class})
@RequestMapping(value = "/index",method = RequestMethod.GET)
public ResultBean getBookInfoById(){
    return ResultBean.successResult("hello world");
}
```
+ 最不一样的就是，在controller层支持返回`ResultBean`,并自动将返回的`ResultBean`转化成JSON(高版本的Spring都会支持)。
+ 除了返回，还支持以抛出`InvokeException(ResultBean) `异常的形式返回ResultBean，这意味着，可以把判断的逻辑放到`Service`层(不知道好不好)
+ 还有封装好的`ValidateUtils`工具类支持断言校验


### 最后
至此，这一个插件的介绍也接近尾声。它有哪些优点呢？

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

### Todo And Think
+ 借助于这样的思路，是否可以把复杂的权限校验，采用类似的方法进行封装和改进