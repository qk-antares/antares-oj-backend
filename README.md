## 基于Docker代码沙箱的在线判题系统

### 1. 项目简介

该项目是基于微服务的算法题在线评测系统，分为`网关`、`用户`、`判题`和`代码沙箱`4个微服务。在系统前台，管理员可以创建、管理题目，用户可以搜索、查看题目，编写代码并进行在线自测和提交。在系统后端，自主实现了代码沙箱，能够根据接收的测试用例对代码进行编译、运行并给出输出结果。代码沙箱作为独立服务，可以提供给其他开发者使用。

- 前端基于 **Vue3 + Arco Design**（高仿leetcode）。
- 后端实现了**ACM模式的本地代码沙箱和Docker沙箱**，支持**在线测试，运行时间和内存占用统计**；使用了 API签名认证（AK/SK）
- 为了方便开发者用户调用我实现的代码沙箱，写了一个SDK。

github仓库：

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

#### 1.2 技术栈







---

#### 1.3 项目结构

```bash
├── antares-code-sandbox	# 代码沙箱服务 
├── antares-code-sandbox-sdk	# 调用代码沙箱的sdk
├── antares-common	# 各服务的公共依赖
├── antares-gateway	# 网关服务
├── antares-judge	# 判题服务
├── antares-user	# 用户服务
├── script	# 内存监控脚本
└── sql	# 数据库脚本
```

---

#### 1.4 运行教程





----

#### 1.5 下一步计划

- [ ] ⭐完成今日一题签到功能（+积分功能）
- [ ] ⭐完成用户的提交统计
- [ ] ⭐做运行与提交接口的限流
- [ ] ⭐完善项目文档
- [ ] ⭐项目的部署上线
- [ ] 提供项目的Dockerfile方便一键部署
- [ ] 接入第三方的代码沙箱实现
- [ ] 做运行时间与内存占用的统计功能（打败了xx%用户）
- [ ] 完善用户中心

---

### 2. 后端技术点

#### 2.1 API签名认证

什么是API签名认证

```
API签名认证（API Signature Authentication）是一种用于保护Web API（Application Programming Interface，应用程序编程接口）的身份验证机制。它用于验证发送API请求的客户端或用户的身份，并确保请求数据在传输过程中没有被篡改。API签名认证通常涉及以下步骤：
①请求创建： 客户端创建API请求，并包括必要的请求参数、数据和请求头。
②签名生成： 客户端使用预共享的密钥（API密钥或令牌）和一些请求信息（如HTTP方法、请求URL、时间戳等）来生成一个唯一的数字签名。签名生成通常使用哈希算法（如HMAC-SHA256）来加密这些信息。
③签名附加： 客户端将生成的数字签名附加到API请求中，通常是在请求头、请求参数或特定的请求字段中。
④服务器验证： 服务端收到API请求后，会使用相同的密钥和接收到的请求信息来生成签名。然后，它将生成的签名与请求中的签名进行比较。
⑤比较签名： 服务器将客户端提供的签名与自己生成的签名进行比较。如果两者匹配，说明请求是有效的，客户端身份已验证。如果签名不匹配，请求将被视为无效或潜在的恶意请求。

API签名认证的优点包括：
①身份验证： 确保请求的发送方是已授权的客户端或用户。
②数据完整性： 通过签名验证，可以确保请求数据在传输过程中没有被篡改。
③安全性： API密钥等敏感信息不会在请求中明文传输，从而增加了安全性。
④防止重放攻击： 通过时间戳或一次性令牌，API签名可以有效防止恶意重复使用请求。

API签名认证是保护API安全的一种重要方式，特别是在公共互联网上运行的API服务中。许多云服务提供商和Web应用程序框架都支持API签名认证，开发人员可以利用这些工具来轻松实现API身份验证和数据保护。
```

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