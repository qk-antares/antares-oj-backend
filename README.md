# 在线判题系统

用React + Ant Design Pro那一套搭建的前端，后端实现了ACM模式的本地代码沙箱和Args模式的Docker沙箱（线上使用的是本地代码沙箱），实现了提交统计，支持在线测试，使用了API签名认证（只用了AK/SK），最后，为了方便开发者用户调用我实现的代码沙箱，写了一个SDK。

项目的部署使用的是自己搭建的服务器，使用IPv6+DDNS实现公网访问。如果你无法访问，可能是由于你的网络环境不支持IPv6（通常是公司内网和校园网），你可以到[IPv6 测试 (test-ipv6.com)](http://test-ipv6.com/)这个网站测试你是否支持IPv6，如果不支持可以连接手机热点后访问，手机网络一般支持IPv6

在线访问：http://oj.antares.cool

## 项目结构

![image-20230903153456627](http://image.antares.cool/PicGo/Project/OJ/461b8c0b478c26f0698c478dd8e0609d8c460332.png)

gateway和member我直接复用之前的博客项目了，详情见[qk-antares/antares-blog-backend (github.com)](https://github.com/qk-antares/antares-blog-backend)

## 前端页面

### 首页

![image-20230904004237071](http://image.antares.cool/PicGo/Project/OJ/51bc61d027a6cd36adfae175b8eb1a557d833325.png)

支持根据状态、难度、标签、关键字检索（就是改写了下getQueryWrapper），就是题目状态的查询条件构造起来有点麻烦。

当前进度这里算是一个复杂查询了吧，根据用户id和题目难度，去查询用户通过的题目的数量

```sql
select COUNT(DISTINCT problem_id)
from problem_submit
where user_id = ? 
and status = 3
and problem_id in
(select id from problem where difficulty = ?)
```

### 题目详情页

![image-20230904005445502](http://image.antares.cool/PicGo/Project/OJ/41a6c9e7da20588dfd56bb296ecf5ed3d3d8d3db.png)

这里是高仿LeetCode，右下角支持打开控制台输入自定义的输入然后获取执行结果

![image-20230904010000773](http://image.antares.cool/PicGo/Project/OJ/c72e399f92091e2a0df8c89fc5b9b6608444bb72.png)

题解界面就不说了，简单地展示解析

提交记录页面可以查看历史提交的代码和通过信息：

![image-20230904010244838](http://image.antares.cool/PicGo/Project/OJ/d4405e299729d568a4ae72d3e823f834f21d5176.png)

![image-20230904010304298](http://image.antares.cool/PicGo/Project/OJ/6808bf0fb6b5de97acd58b0053c6e4da77da1eed.png)

### 管理页

![image-20230904010359859](http://image.antares.cool/PicGo/Project/OJ/13d07367014c503876e830c754afc792eaf7f398.png)

![image-20230904010432931](http://image.antares.cool/PicGo/Project/OJ/a8795a8594ad50e068e939b3e47aadcc75712cd7.png)



![image-20230904083454196](http://image.antares.cool/PicGo/Project/OJ/0e8e289500cd189e210619cee0de162538a86c5c.png)

---

## 后端技术点

### API签名认证



---

### 字典树校验违禁词

代码有违禁词，例如Files，exec等，防止用户写入木马文件并执行。使用字典树可以**减少存储空间，而且提高检索效率**



---

### SecurityManager





----

### 本地代码沙箱



---

### Docker代码沙箱





---



