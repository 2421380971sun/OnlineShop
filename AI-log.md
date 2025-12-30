# AI Interaction Log (AI-log)

---
## Interaction 1: 技术栈与架构选型
**Context:** 项目启动阶段，确定 Jakarta EE 技术栈。

**Prompt:** "我正在开发在线商城，使用 JSF+CDI+JPA+MySQL+Tomcat。请评估技术栈并给出分层建议。"

**AI Response Summary:**  
AI 认可该组合，但指出 Tomcat 仅支持 Web Profile，建议使用 Payara。同时给出了标准的 `Controller -> Service -> Repository` 分层架构。

**Curator Evaluation:**
- **Valid:** 分层架构建议非常清晰，直接采纳。
- **Critique:** 更换服务器（Payara）需要重新配置环境，且 Tomcat 11 对 Jakarta EE 10 支持已足够满足本项目需求。

**Action:** 采纳分层结构设计，保留 Tomcat，手动在 `pom.xml` 配置必要的 Jakarta 依赖。

---
## Interaction 2: 支付与积分扣减的事务风险
**Context:** `OrderService` 开发，担心高并发下积分扣减导致数据不一致。

**Prompt:** "下单时既要扣积分又要扣库存，如果数据库挂了怎么办？请分析 3 个风险并给出 Jakarta EE 解决方案。"

**AI Response Summary:**  
AI 识别了 1. 重复支付、2. 部分失败（积分扣了订单没成）、3. 竞态条件。推荐了三种方案：CMT+乐观锁、编程式事务、Saga 模式。

**Curator Evaluation (The Pivot Point):**  
这是最有价值的一次交互。AI 正确识别了风险，但给出的方案对于 2 周的课程项目来说过于“重型”。
- **Selection:** 我提取了 **容器管理事务（CMT）** 的核心概念。
- **Modification:** 我没有引入 `@Version` 乐观锁字段（避免复杂化前端异常处理），而是选择在 `@Transactional` 方法内部进行严格的逻辑检查（`if (pointsUsed > user.getPoints()) throw Exception`）。

**Action:** 编写了 `OrderService.java`，利用简单的 `@Transactional` 注解包裹整个下单过程，确保原子性。

---
## Interaction 3: 数据库实体模型设计
**Context:** 设计数据库，特别是 `User` 和 `PointRecord`。

**Prompt:** "请帮我分析 User, Product, Order, PointRecord 的实体设计。User 和 Order 应该用 @OneToMany 吗？"

**AI Response Summary:**  
AI 建议 `PointRecord` 必须存在以用于审计，并**强烈建议避免在 User 中使用 `@OneToMany List<Order>`**，因为这会导致性能问题（N+1 查询）。

**Curator Evaluation:**
- **Insight:** 这一点非常关键。如果我在 User 里放一个 Order 列表，查询用户时可能会拖慢整个系统。

**Action:**
1. 采纳建议：在 `User` 实体中删除了订单列表引用。
2. 采纳建议：建立了 `PointRecord` 表，用于记录每一次积分变动（来源、数值、时间），实现了系统的可追溯性。
---
## Interaction 4: 项目构建配置
**Context:** 生成 Maven 配置和 Jakarta EE 核心配置文件。

**Prompt:** "请为我生成一份标准的 pom.xml 文件... 关键依赖要求（必须兼容 Jakarta EE 10）... 随后生成 JPA 的核心配置文件 `src/main/resources/META-INF/persistence.xml`... 最后生成 Web 应用必须的三个配置文件（`web.xml`, `beans.xml`, `faces-config.xml`）。"

**AI Response Summary:**  
AI 提供了完整的 `pom.xml`，正确处理了 Tomcat 环境下的 CDI（Weld）和 JPA（Hibernate）依赖。接着生成了 `persistence.xml`，重点配置了 `RESOURCE_LOCAL` 事务类型以适配 Tomcat。最后提供了符合 **Servlet 6.0 / CDI 3.0 / Faces 4.0** 标准的 XML 配置文件。

**Curator Evaluation:**
- **Accuracy:** AI 准确识别了 Tomcat 环境的特殊性（不支持 JTA），并给出了正确的 Hibernate 配置：`hibernate.transaction.coordinator_class=jdbc`。
- **Completeness:** 提供的 `web.xml` 包含了必要的 JSF 和 CDI 监听器配置，具备可直接运行的完整度。

**Action:** 直接使用生成的 XML 文件，但移除了 `persistence.xml` 中关于二级缓存的配置，以简化开发与调试阶段的复杂度。

---
## Interaction 5: 后端实体类生成 (Backend Entities)
**Context:** 项目骨架搭建完毕，开始编写业务实体。

**Prompt:** "请利用 JPA 注解生成 User, Product, Order, OrderItem, PointRecord 实体类。要求：User.role 暂时用 String 类型；实现 Serializable；不使用 Lombok。"

**AI Response Summary:**  
AI 生成了标准的 JavaBean 代码，包含了所有请求的字段、Getter/Setter 和 JPA 注解。`User` 类中 `role` 被定义为 `String`。

**Curator Evaluation:**
- **Compliance:** AI 完美遵守了 "不使用 Lombok" 和 "使用 String" 的指令。
- **Critique:** 虽然 AI 听话，但作为架构师（Curator），我意识到 Prompt 中的设计有缺陷。String 类型的角色字段是“坏味道（Bad Smell）”。

**Action:** 接受 AI 生成的大部分样板代码（Getter/Setter/JPA注解），但在合并到代码库时，**手动将 `role` 字段重构为枚举类型**，并修改了对应的数据库映射配置。  
*(Self-Correction: 下次 Prompt 应直接要求生成 Enum 类型)*

---
## Interaction 6: 业务服务层实现 (Service Layer)
**Context:** 有了实体后，需要实现业务逻辑。

**Prompt:** "请基于生成的实体，创建 UserService，包含登录认证和注册功能。"

**AI Response Summary:**  
AI 生成了 `UserService`，使用了标准的 JPQL 查询用户。

**Curator Evaluation:**
- **Integration:** AI 默认假设环境中有完整的 CDI 容器，代码中使用了 `@Inject EntityManager em;`。
- **Adaptation:** 由于我决定使用 `PersistenceUtil` 静态工厂，我需要修改 AI 生成的代码。
  **Action:** 将直接注入 `EntityManager` 修改为注入 `PersistenceUtil`，并在方法内部调用 `persistenceUtil.getEntityManager()` 来获取连接。这确保了代码在 Tomcat 环境下能稳定运行。
---
## Interaction 7: 核心业务逻辑实现 (Core Implementation)
**Context:** 实体类已就位，开始编写 Service 层。

**Prompt:** "请生成 OrderService.java... createOrder 需开启事务、库存检查、100积分抵1元... payOrder 需扣除积分、累积积分、记录流水..."

**AI Response Summary:**
AI 生成了包含 `createOrder` 和 `payOrder` 方法的类。代码逻辑清晰，能处理正常的积分计算和 `PointRecord` 生成。

**Curator Evaluation:**
- **Issue 1 (Injection):** AI 试图使用 `@Inject PersistenceUtil`，但我之前的架构决策是使用静态工厂方法 `PersistenceUtil.getEntityManager()`。
- **Issue 2 (Safety):** 代码缺乏防御性编程（资源泄漏风险、负数金额风险）。

**Action:** 采纳 AI 的业务流程（扣库存->算分->存订单），但**手动替换**了 `EntityManager` 的获取方式，并**重写**了事务控制代码结构。

---


## Interaction 8: 前端页面生成 (Frontend Pages)

**Context:**  
后端逻辑完成，开始写界面。

**Prompt:**  
"请使用 JSF 3.0 生成 register.xhtml... 绑定到 UserController... 添加 confirmPassword 输入框..." (基于 web4.txt)

**AI Response Summary:**  
AI 生成了标准的 XHTML 文件，使用了 PrimeFaces 或原生 h:form。

**Curator Evaluation:**
- **Usability:** 页面结构正确，但 AI 忘记在 `UserController` 中处理 `confirmPassword` 的比对逻辑（因为 User 实体里没这个字段）。

**Action:**  
采纳页面代码，但在编写后端 `UserController` 时，手动补全了密码确认的校验逻辑。

---

## Interaction 9: 商品模块 (Product Module)

**Context:**  
管理员管理商品。

**Prompt:**  
"请生成 ProductService... 注入 PersistenceUtil (使用之前重构的 CDI Bean 方式)..." (基于 web5.txt)

**AI Response Summary:**  
AI 听话地生成了带有 `@Inject` 的代码。

**Curator Evaluation:**
- **Critique:** AI 过于顺从。作为策展人，我意识到之前的 Prompt 写得有问题（或者是测试时故意留的坑）。

**Action:**  
采纳 CRUD 逻辑，但**重构**数据访问层代码，移除依赖注入，改为静态工厂调用。

---
## Interaction 10: 积分 Web Service (SOAP)

**Context:**  
第三方系统对接。

**Prompt:**  
"请创建一个 SOAP Web Service (PointWebService)... 提供查询积分功能... 生成 web.xml 配置..."

**AI Response Summary:**  
AI 生成了完美的 `@WebService` 类和测试用的 HTML 客户端。

**Curator Evaluation:**
- **Completeness:** 代码逻辑正确，但 AI 假设环境是完整的 Java EE 容器。在 Tomcat 上需要额外的 XML 配置。

**Action:**  
补全 `sun-jaxws.xml`，这是 AI 遗漏的关键部署描述符。

---

## Interaction 11: 订单列表页面 (Order List View)

**Context:**  
用户查看历史订单。

**Prompt:**  
"请生成 order_list.xhtml... 显示订单号、总价、状态和下单时间... 下单时间使用 yyyy-MM-dd HH:mm:ss 格式。"

**AI Response Summary:**  
AI 生成了 `<p:dataTable>` 或 `<h:dataTable>`，并在时间列使用了 `<f:convertDateTime type="localDateTime" ... />`。

**Curator Evaluation:**
- **Standard:** AI 的代码符合 Jakarta EE 规范。
- **Preference:** 考虑到我对 Tomcat 环境稳定性的极高要求，我拒绝使用标签内的转换配置。

**Action:**  
移除 `<f:convertDateTime>` 标签，改为在 `src/main/java` 下手动实现一个全局转换器，并在 XHTML 中引用它。
