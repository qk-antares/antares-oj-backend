## 基于Docker代码沙箱的在线判题系统

### 1. 项目简介

该项目是基于Docker代码沙箱的算法题在线评测系统（Online Judge，OJ），分为`网关`、`用户`、`判题`和`代码沙箱`4个微服务。在系统前端，管理员可以创建、管理题目，用户可以查看题目，编写代码并进行在线自测和提交；在系统后端，自主实现了代码沙箱，能够对代码进行编译，在用户设置的测试用例上运行并输出结果信息（输出流、运行耗时、内存占用等），且代码沙箱作为独立服务，可以提供给其他开发者使用。

GitHub仓库：

- 后端：[qk-antares/antares-oj-backend (github.com)](https://github.com/qk-antares/antares-oj-backend)
- 前端：[qk-antares/antares-oj-frontend (github.com)](https://github.com/qk-antares/antares-oj-frontend)

---

#### 1.1 项目预览

##### 1.1.1 用户界面

![image-20250324193733643](https://s2.loli.net/2025/03/24/fKX52ZpoUGhaudL.png)

![image-20250324194211192](https://s2.loli.net/2025/03/24/Nc3pEzuZveQYC6X.png)

![image-20250324194332182](https://s2.loli.net/2025/03/24/TJN798mpMVjbHc2.png)

![image-20250324194445385](https://s2.loli.net/2025/03/24/fovX1imbWSlLTIg.png)

![image-20250324194530713](https://s2.loli.net/2025/03/24/1afXUkyTCo8hIx9.png)

##### 1.1.2 管理员界面

![image-20250324194823470](https://s2.loli.net/2025/03/24/XOh89AsDMbWT6t3.png)

![image-20250324194907814](https://s2.loli.net/2025/03/24/mzLGoutyFCKhkaB.png)

![image-20250324194951238](https://s2.loli.net/2025/03/24/YQEC12OFGLRjNKl.png)

----

#### 1.2 后端技术栈

- [Spring Cloud Gateway](https://github.com/qk-antares/antares-oj-backend/blob/master/doc/2.1_Gateway/Index.md)
  1. [请求路由，根据请求的URL将请求转发到不同的服务](https://github.com/qk-antares/antares-oj-backend/blob/master/doc/2.1_Gateway/2.1.1_Route.md)
  2. [统一处理CORS（跨域资源共享）](https://github.com/qk-antares/antares-oj-backend/blob/master/doc/2.1_Gateway/2.1.3_CORS.md)
- [Nacos](https://github.com/qk-antares/antares-oj-backend/blob/master/doc/2.2_Nacos/Index.md)
  1. [服务注册与发现](https://github.com/qk-antares/antares-oj-backend/blob/master/doc/2.2_Nacos/2.2.1_Registry.md)
  2. [动态配置管理](https://github.com/qk-antares/antares-oj-backend/blob/master/doc/2.2_Nacos/2.2.2_Config.md)
- [Spring&Java](https://github.com/qk-antares/antares-oj-backend/blob/master/doc/2.3_Spring&Java/Index.md)
  1. [自定义Bean的注入与使用（Spring IoC）](https://github.com/qk-antares/antares-oj-backend/blob/master/doc/2.3_Spring&Java/2.3.1_IoC.md)
  2. [泛型响应结构类](https://github.com/qk-antares/antares-oj-backend/blob/master/doc/2.3_Spring&Java/2.3.2_R.md)+[自定义异常类](https://github.com/qk-antares/antares-oj-backend/blob/master/doc/2.3_Spring&Java/2.3.3_BusinessException.md)+[`hibernate-validator`参数校验](https://github.com/qk-antares/antares-oj-backend/blob/master/doc/2.3_Spring&Java/2.3.4_Validator.md)+[`@ControllerAdvice`统一异常处理器类](https://github.com/qk-antares/antares-oj-backend/blob/master/doc/2.3_Spring&Java/2.3.5_ControllerAdvice.md)，实现对业务中异常的统一处理和响应
  3. AOP实现接口权限校验以及系统日志
  4. `@Async`实现异步发送验证码，提升响应效率
  5. CompletableFuture异步编程
  6. 编写sdk供其他开发者调用代码沙箱
- Redis:
  1. 自定义序列化器（Serializer），并缓存热点数据（题目信息）。基于`spring-boot-starter-data-redis`实现旁路缓存模式
  2. 缓存验证码，实现接口防刷
  3. JWT实现单点登录，并在Redis中缓存用户状态
  4. 基于位图实现每日一题签到功能
- MySQL：
  1. 优化索引设计，提高查询速度
- Docker：
  1. 通过docker-java远程连接docker，创建docker容器，编译执行代码，统计代码执行的时间和内存占用
- Jenkins：
  1. 自己部署服务器
- 设计模式：
  1. 模板方法模式
  2. 策略模式
- Nginx：
  1. 反向代理服务器
  2. lua脚本
- 其他：
  1. API签名认证，防止恶意调用代码沙箱



---

#### 1.3 项目结构

```bash
├── antares-api    # 各个微服务提供的接口，供Dubbo远程调用
│   └── antares-user-api  
├── antares-code-sandbox    # 代码沙箱服务 
├── antares-code-sandbox-sdk    # 调用代码沙箱的sdk
├── antares-common  # 各服务的公共依赖
│   ├── antares-common-auth
│   ├── antares-common-core
│   ├── antares-common-mybatis
│   └── antares-common-redis
├── antares-gateway # 网关服务
├── antares-judge   # 判题服务
├── antares-user    # 用户服务
├── script  # 内存监控脚本
└── sql # 数据库脚本
```

---

#### 1.4 运行教程







----

#### 1.5 下一步计划

- [x] ⭐完成今日一题签到功能（~~+积分功能~~）
- [x] ⭐完成用户的提交统计
- [x] ⭐做运行与提交接口的限流
- [ ] ⭐完善项目文档
- [x] ⭐项目的部署上线
- [x] 提供项目的Dockerfile方便一键部署（提供了Jenkinsfile替代）
- [ ] 接入第三方的代码沙箱实现
- [ ] 做运行时间与内存占用的统计功能（打败了xx%用户）
- [ ] 完善用户中心

---






#### 2.3 API签名认证

API签名认证是一种用于保护Web API的身份验证机制。它用于验证发送API请求的客户端或用户的身份，并确保请求数据在传输过程中没有被篡改。API签名认证通常涉及以下步骤：

1. 请求创建： 客户端创建API请求，并包括必要的请求参数、数据和请求头
2. 签名生成： 客户端使用密钥和一些请求信息（如时间戳等）来生成一个唯一的数字签名sign。签名生成通常使用哈希算法（如HMAC-SHA256）
3. 签名附加： 客户端将生成的数字签名附加到API请求中，通常是在请求头或请求参数中
4. 服务器验证： 服务端收到API请求后，会使用相同的密钥和接收到的请求信息来生成签名。然后，它将生成的签名与请求中的签名sign进行比较。

> ⚠️如果生成签名的过程中没有使用某个请求数据，那么对该请求数据的篡改将是不可知的

在日常使用软件中，我们可能会注意到有两种API认证方式：

- 方式一：只有一个密钥

  在注册SM.MS图床后，网站会提供一个Secret Token（可以随时查看），把Secret Token填写到PicGo这类软件中后，就可以方便地将图片上传到SM.MS

  ![image-20250412164038994](https://s2.loli.net/2025/04/12/p4NXAD9LvSJePW3.png)

- 方式二：有两个密钥

  在注册腾讯云后，网站会提供给我们一对密钥，分别叫SecretId和SecretKey，且SecretKey只能在创建时告诉用户一次，之后无法查看，只能重新创建。有了这两个密钥，我们就可以通过ddns-go动态修改所购买域名的域名解析。 

  ![image-20250412164357645](https://s2.loli.net/2025/04/12/CpUgPGrxm4Z5kh9.png)

这两种认证方式是如何工作的？有什么区别？

---

##### 2.3.1 单密钥

网站给你一个 `SecretToken`，你在软件中配置这个 Token，它会被加到 HTTP 请求头中：

```http
POST /upload
Authorization: Bearer sk_123456789abcdef
```

在这种场景下，`SecretToken`就是用户的身份标识。但是和JWT还不同，`SecretToken`中并不包含状态信息（Payload），它只是一个静态的、用户唯一的标识。当网站接受到你的请求时，他会在自己的数据库中查询有没有这个Token并确定你是谁。

单密钥的整个过程不涉及加密/签名，轻量简单。

---

##### 2.3.2 SecretId+SecretKey

在这种场景下，`SecretId` 相当于你的用户名，`SecretKey` 则是你的密码，用于签名（不直接参与网络传输）。每次请求都要用这两个参数进行**签名计算**，类似这样：

```http
POST /ddns/update
Action: UpdateDomainRecord
Timestamp: 1682341234
SecretId: sid_123456789
Signature: sha256_hmac(secretKey, 所有参数)

{
	"domain": "a.com",
	"ip": "1.2.3.4"
}
```

当服务端接收到这个请求时，首先根据`SecretId`去数据库中查询对应的`SecretKey`，然后对请求参数（Timestamp、SecretId、domain，ip）进行同样的签名操作，然后与Signature进行比较。总的来说，`SecretId`+`SecretKey`的方案依然是一种**对称加密（哈希消息认证码，HMAC）**。

###### 防篡改

签名计算的目的是为了防止请求参数的篡改。例如，你的请求被某个中间人截获，如果他修改某个请求参数（他无法修改Signature），那么服务端进行签名操作得到的结果就会和Signature不同，进而拒绝请求。

###### 防重放

1. Timestamp

   携带Timestamp是为了防止重放攻击。想象某个中间人截获了你的转账请求，他不进行数据篡改，而是反复重放该请求，那么也会造成攻击效果。为此可以将请求的时间戳作为参数的一部分，也进行签名（这意味着攻击者无法篡改时间戳），后端在执行业务逻辑之前首先判断该时间戳是否在范围之内。

2. nonce（随机串）+缓存机制

   单纯携带Timestamp并不能解决“**短时重放攻击**”的问题，为此，可以再加上一招：nonce（随机串）+缓存机制。客户端请求时，加入 `timestamp + nonce` 参数，服务端校验时，判断 `timestamp` 在时间窗口内（如1分钟），且**在该时间窗口内 nonce 是否首次出现**。如果 `nonce` 在时间窗口内已经出现过一次，直接拒绝请求，否则将 `nonce` 加入缓存（如 Redis），设置过期时间 1分钟

   ```markdown
   接收到请求
       ↓
   检查 timestamp 是否过期
       ↓
   检查 nonce 是否在缓存中
       ├─ 是 → 拒绝（重放）
       └─ 否 → 验证签名 → 缓存 nonce（5分钟） → 通过
   ```

---

##### 2.3.3 非对称加密

上面提到了`SecretId`+`SecretKey`的方案本质是一种对称加密，那么为什么需要非对称加密，以及它是如何工作的？

分析`SecretId`+`SecretKey`的工作流程，客户端需要使用`SecretKey`对请求数据进行签名，尽管普通的业务不会发生`SecretKey`的传输，但用户获取自己`SecretKey`的过程依然会发生`SecretKey`的传输（这也是为什么腾讯云只给你看一次`SecretKey`，再想查看只能重建）

以HTTPS来说明非对称加密的流程：

1. 服务器把公钥发给客户端
2. 客户端生成一个对称加密用的 AES 密钥
3. 客户端用服务器公钥加密 AES 密钥，发回给服务器
4. 服务器用私钥解密得到 AES 密钥
5. 后续通信用 AES（对称加密）进行加密传输

非对称加密的目的是为了传输一个对称加密通信使用的密钥AES（相当于上面的SecretKey，但避免了直接传输），一旦完成了AES的传输，后续的通信是通过对称加密实现的。用公钥加密的数据只有对应的私钥才能解密，客户端从始至终都不知道私钥，也从未发生AES密钥的直接传输。

---

#### 2.1 API签名认证

在我的项目中，API签名认证是在调用代码沙箱的这个过程，核心代码片段如下：

```Java
public static final String GATEWAY_HOST = "http://oj.antares.cool";
private String accessKey;
private String secretKey;

public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest) {
    String requestBodyJson = JSONUtil.toJsonStr(executeCodeRequest);
    try (HttpResponse response = HttpRequest.post(GATEWAY_HOST + "/api/sandbox/execute")
            .header("Content-Type", "application/json")
            .header("accessKey", accessKey)
            .header("sign", genSign(requestBodyJson, secretKey))
            .body(requestBodyJson)
            .execute()) {
        String responseBody = response.body();
        log.info("响应：{}", response);
        return JSONUtil.toBean(responseBody, ExecuteCodeResponse.class);
    } catch (Exception e) {
        log.info("请求沙箱失败：", e);
        return null;
    }
}
```

可以看到，除了普通的请求体，请求头中还多了两个参数accessKey和sign，其中后者是根据请求体根据secretKey加密生成的，这就保证了整个请求的过程中secretKey不会泄露（因为它根本就不参与传输）。

当一个执行代码的请求到达代码沙箱时，代码沙箱服务会拿出请求头中的accessKey和sign，去数据库中查询accessKey对应的secretKey，之后验证加密的结果是否和sign一致。如果一致，证明这个请求是合法的，否则是非法的。

```Java
@PostMapping("/execute")
public ExecuteCodeResponse execute(@RequestBody ExecuteCodeRequest executeCodeRequest, HttpServletRequest request){
    //首先进行校验
    String accessKey = request.getHeader("accessKey");
    String sign = request.getHeader("sign");
    String secretKey = userFeignService.getSecretKey(accessKey);
    String body = JSONUtil.toJsonStr(executeCodeRequest);
    //使用同样的算法生成签名
    String verify = genSign(body, secretKey);
    if(verify.equals(sign)){
        log.info("校验通过");
        return sandboxService.execute(executeCodeRequest);
    } else {
        return ExecuteCodeResponse.builder()
                .code(ExecuteCodeStatusEnum.NO_AUTH.getValue())
                .msg(ExecuteCodeStatusEnum.NO_AUTH.getMsg())
                .build();
    }
}
```

该实现还有很大的优化空间：首先，API签名的校验可以提到网关层来实现；其次，我这里获取secretKey的方法是远程调用，即使在网关层限制这个接口只能被内部调用，secretKey在整个过程依然以明文的形式传输了，应该直接查询数据库比较好。

----

#### 2.2 本地代码沙箱

##### 2.2.1 字典树校验违禁词

代码有违禁词，例如Files，exec等，防止用户写入木马文件并执行。使用字典树可以**减少存储空间，而且提高检索效率**

```
字典树（Trie树，也称为前缀树）是一种树形数据结构，用于有效地存储和检索一组字符串，通常是字符串集合或关键字集合。字典树的主要特点是将共享相同前缀的字符串存储在同一层次结构中，从而实现了高效的字符串查找和前缀匹配。

字典树的关键特点包括：
①根节点： 字典树通常有一个根节点，表示空字符串或空前缀。
②节点结构： 每个节点包含多个子节点，每个子节点代表一个字符。通常，字典树中的每个节点都有固定数量的子节点，通常是字符集的大小（例如，26个字母）。
③路径： 从根节点到任何一个节点，沿着路径的字符连接在一起，构成一个字符串。这个路径上的字符序列即表示存储在字典树中的字符串。
④叶节点： 叶节点是表示一个完整字符串的节点，不再有子节点。当我们从根节点到叶节点沿着路径移动时，就可以得到存储在字典树中的一个字符串。
⑤前缀匹配： 字典树非常适合前缀匹配操作。要查找以某个字符串为前缀的所有字符串，只需从根节点开始，沿着路径移动到表示该前缀的节点，然后遍历该节点的所有子节点，即可获取所有匹配的字符串。
⑥高效性能： 字典树在存储和检索字符串集合方面具有高效性能，特别适合于自动完成、单词搜索、拼写检查等应用。

尽管字典树在存储和前缀匹配方面非常高效，但它也有一些缺点。其中主要的问题是空间开销，因为字典树需要存储大量的节点，特别是对于大型字符串集合。为了解决这个问题，可以使用压缩的字典树变体，如压缩字典树（Compressed Trie）或基数树（Radix Tree），以减少存储空间的开销。

总之，字典树是一种用于高效存储和检索字符串集合的数据结构，特别适用于需要进行前缀匹配的应用。它在搜索引擎、自动完成、拼写检查和路由表等领域都有广泛的应用
```

一个字典树的实现

```Java
class TrieNode {
    private TrieNode[] children;
    private boolean isEndOfWord;

    public TrieNode() {
        children = new TrieNode[26]; // 假设只包含小写字母
        isEndOfWord = false;
    }

    public void insert(String word) {
        TrieNode current = this;
        for (char c : word.toCharArray()) {
            int index = c - 'a';
            if (current.children[index] == null) {
                current.children[index] = new TrieNode();
            }
            current = current.children[index];
        }
        current.isEndOfWord = true;
    }

    public boolean search(String word) {
        TrieNode node = searchPrefix(word);
        return node != null && node.isEndOfWord;
    }

    public boolean startsWith(String prefix) {
        return searchPrefix(prefix) != null;
    }

    private TrieNode searchPrefix(String prefix) {
        TrieNode current = this;
        for (char c : prefix.toCharArray()) {
            int index = c - 'a';
            if (current.children[index] == null) {
                return null; // 找不到前缀
            }
            current = current.children[index];
        }
        return current;
    }
}

public class Trie {
    private TrieNode root;

    public Trie() {
        root = new TrieNode();
    }

    public void insert(String word) {
        root.insert(word);
    }

    public boolean search(String word) {
        return root.search(word);
    }

    public boolean startsWith(String prefix) {
        return root.startsWith(prefix);
    }

    public static void main(String[] args) {
        Trie trie = new Trie();
        trie.insert("apple");
        System.out.println(trie.search("apple"));   // 输出 true
        System.out.println(trie.search("app"));     // 输出 false
        System.out.println(trie.startsWith("app")); // 输出 true
        trie.insert("app");
        System.out.println(trie.search("app"));     // 输出 true
    }
}
```

---

##### 2.2.2 Runtime

```
Runtime 类是 Java 中的一个系统类，用于与运行时环境进行交互和管理。它的主要作用是提供了一种在 Java 程序中执行系统级操作的方式，例如：
执行外部进程： 使用 Runtime 类，您可以在 Java 程序中启动和执行外部系统进程。这对于调用其他可执行程序或系统命令非常有用。
获取系统信息： Runtime 类允许您获取关于运行时环境的信息，如可用处理器数量、可用内存、操作系统信息等。
垃圾回收操作： 您可以使用 Runtime 类的 gc() 方法强制运行垃圾回收器来尝试释放不再使用的内存。
关闭虚拟机： Runtime 类的 exit() 方法允许您终止 Java 虚拟机的运行。这可以用于正常退出程序或在出现严重错误时终止程序。

请注意，从 Java 9 开始，一些 Runtime 的方法已经被标记为废弃，而且在某些情况下，更推荐使用 ProcessBuilder 类来执行外部进程。

总之，Runtime 类提供了一些与运行时环境交互的基本功能，使您能够执行一些系统级操作，但在使用时需要谨慎，以免造成不必要的系统干扰。
```

Runtime如何执行外部进程

`Runtime` 类可以用于执行外部进程。它提供了一个名为 `exec(String command)` 的方法，该方法用于执行指定的命令或可执行文件。下面是一个简单的示例，展示如何使用 `Runtime` 执行外部进程：

```
javaCopy codepublic class ExecuteExternalProcess {
    public static void main(String[] args) {
        try {
            // 使用Runtime.getRuntime()获取Runtime对象
            Runtime runtime = Runtime.getRuntime();
            
            // 要执行的外部命令或可执行文件
            String command = "ls -l"; // 在Unix/Linux系统上列出当前目录的文件列表
            
            // 使用exec()方法执行外部进程
            Process process = runtime.exec(command);
            
            // 处理进程的输出流
            InputStream inputStream = process.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            
            // 读取并输出进程的输出
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
            
            // 等待进程执行完成
            int exitCode = process.waitFor();
            System.out.println("进程执行完毕，退出代码：" + exitCode);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
```

在上述示例中，我们使用 `Runtime.getRuntime().exec(command)` 执行了一个外部命令，并获取了该进程的输出流，以便读取其输出。然后，我们使用 `process.waitFor()` 等待进程执行完成，并获取其退出代码。

请注意，这只是一个基本示例，实际应用中可能需要更多的错误处理和参数设置，具体取决于要执行的外部进程和您的需求。

---

#### 2.3 Docker代码沙箱

##### 2.3.1 对比

使用Docker实现的代码沙箱和通过Java的`Runtime`实现的代码沙箱各有其优缺点，具体取决于您的需求和用例：

**Docker 实现的代码沙箱：**

优点：

1. **强大的隔离性：** Docker容器提供了强大的隔离性，可以将用户代码隔离到独立的容器中，防止其访问主机系统资源。
2. **资源限制：** Docker允许您精确地限制容器的资源使用，包括CPU、内存和磁盘空间。
3. **容易部署和管理：** Docker容器可以轻松部署和管理，可以快速启动和停止，也支持自动化部署和扩展。
4. **安全性：** 使用容器化技术，容易实现安全配置，可以控制容器的访问权限和网络连接，增强了安全性。

缺点：

1. **资源开销：** Docker容器相对较重，需要一定的系统资源和存储空间。
2. **启动时间：** 相比于`Runtime`方式，启动一个Docker容器需要更多时间。
3. **复杂性：** Docker容器的设置和管理相对较复杂，需要一定的学习曲线。

**通过Java的 `Runtime` 实现的代码沙箱：**

优点：

1. **轻量级：** 与Docker容器相比，使用`Runtime`执行外部进程的方式更加轻量级，不需要额外的容器化资源。
2. **启动快速：** 启动外部进程通常比启动Docker容器更快。
3. **简单：** 相对于Docker，使用`Runtime`的方式相对简单，无需熟悉Docker的配置和管理。

缺点：

1. **有限的隔离性：** 使用`Runtime`方式执行外部进程时，无法获得与Docker容器相同的隔离性和安全性。用户代码可能能够访问主机系统资源。
2. **资源限制：** 限制资源（如CPU、内存）的精确度较低，较难实现。
3. **安全性风险：** 由于较低的隔离性，可能存在安全性风险，尤其是当运行不受信任的代码时。

---

##### 2.3.2 模板方法模式

由于原生和Docker代码沙箱的实现流程完全一致（保存为文件、编译、执行、获取输出、清理），使用模板方法模式定义了一套标准的流程并允许子类自行拓展，大幅减少冗余代码

---

##### 2.3.3 Spring Boot Starter

什么是Spring Boot Starter

```
Spring Boot Starter 是 Spring Boot 框架中的一个核心概念，用于简化和加速应用程序的开发和配置。

具体来说，Spring Boot Starter 是一种用于自动化配置 Spring Boot 应用程序的组件。它通常是一个 Maven 或 Gradle 依赖，其中包含了一组默认的配置、依赖和代码，以便您可以更轻松地集成各种功能和扩展。

Spring Boot Starter 的主要目标有以下几点：
简化配置： Starter 提供了默认配置，使得您无需手动配置复杂的框架和库，减少了样板代码的编写。
提供依赖管理： Starter 自动引入了应用程序所需的依赖项，确保它们的版本兼容性，这有助于避免版本冲突。
快速集成功能： Starter 允许您通过简单地引入相关依赖，快速集成常见功能，如数据库连接、安全性、消息队列、Web开发等。
模块化和可重用： Starter 可以按功能模块进行划分，允许您按需引入，提高了代码的可重用性和模块化。

例如，如果您想要创建一个使用 Spring Boot 的Web应用程序，可以使用 spring-boot-starter-web Starter，它包含了与Web开发相关的默认配置和依赖项。如果您需要访问数据库，可以添加 spring-boot-starter-data-jpa Starter，它将引入JPA（Java Persistence API）相关的依赖项和配置。

总之，Spring Boot Starter 是一种使 Spring Boot 应用程序更易于开发和配置的机制，通过它，您可以更快速地集成各种功能和扩展，减少了繁琐的配置工作，加速了应用程序的开发过程。
```

要开发自己的Spring Boot Starter，您可以按照以下流程进行：

1. **项目设置：** 创建一个新的Maven或Gradle项目作为您的Starter项目。确保项目使用Spring Boot的依赖管理，并在项目的构建文件中引入Spring Boot的父项目。

2. **项目结构：** 组织项目结构，通常按照以下方式创建：

   ```
   cssCopy codemy-starter/
   ├── src/
   │   ├── main/
   │   │   ├── java/
   │   │   │   └── com/
   │   │   │       └── example/
   │   │   │           └── mystarter/
   │   │   │               └── MyStarterAutoConfiguration.java
   │   │   └── resources/
   │   │       └── META-INF/
   │   │           └── spring.factories
   │   └── test/
   │       └── ...
   ├── pom.xml
   └── ...
   ```

    - `MyStarterAutoConfiguration.java`：这是自动配置类，用于配置您的Starter所提供的功能。
    - `spring.factories`：在`META-INF`目录下创建，用于告诉Spring Boot框架您的自动配置类的位置。

3. **编写自动配置类：** 在`MyStarterAutoConfiguration.java`中编写自动配置类，通过Java配置方式定义您的Starter提供的功能和Bean。确保在类上使用`@Configuration`注解，以标识这是一个配置类。

4. **编写Starter代码：** 开发Starter所提供的功能代码，包括服务、组件、配置等。

5. **编写Starter属性：** 如果您的Starter需要配置属性，可以创建一个`application.properties`或`application.yml`文件，定义属性的默认值，或者创建一个专门的属性类。

6. **测试：** 创建适当的单元测试和集成测试，确保您的Starter工作如预期。

7. **打包：** 使用Maven或Gradle将您的Starter项目打包为JAR文件。

8. **发布：** 如果希望其他开发人员可以使用您的Starter，可以将其发布到Maven中央仓库或私有仓库，以便其他项目可以依赖它。

9. **文档：** 编写文档，清晰地说明如何使用您的Starter，包括配置选项和示例代码。

10. **示例项目：** 创建一个示例项目，演示如何在Spring Boot应用程序中使用您的Starter。

11. **使用：** 您可以将您的Starter添加到其他Spring Boot项目的依赖中，并在应用程序配置文件中配置任何自定义属性。

总之，开发一个自己的Spring Boot Starter需要编写自动配置类、提供功能代码、定义属性、测试、文档和发布等一系列步骤。一旦开发完成，其他开发人员可以轻松地使用您的Starter来扩展他们的Spring Boot应用程序。

----

#### 2.4 判题服务

##### 2.4.1 AOP实现权限校验



##### 2.4.2 Redis缓存模式





##### 2.4.3 工厂模式+策略模式消除if-else