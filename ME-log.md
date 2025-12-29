# Mental Execution Log (ME-log)

---

## 1. 模块：用户注册逻辑 (User Registration)

**Date:** 2025-12-23

**Spec:** 用户注册需绑定属性到 Bean 并持久化。

**AI Generation Analysis:**
AI 生成了 `UserController`，直接调用 `userService.register(user)`。

**Mental Execution (V&V):**
在脑海中模拟 JSF 生命周期：当页面加载 `value="#{userController.user.username}"` 时，如果 Controller 中的 `user` 对象为 `null`，EL 表达式会抛出 `PropertyNotFoundException`。AI 经常忽略 Bean 的初始化。
此外，AI 生成的代码中 `user.setRole("USER")` 使用了字符串，而我的实体定义了 `Enum`，这会导致类型安全问题。

**Correction:**

1. 在 `UserController` 构造或声明时手动 `new User()`。
2. 修正类型匹配：`user.setRole(User.Role.USER)`。

---

## 2. 模块：积分与订单的一致性 (Transaction Safety)

**Date:** 2025-12-24

**Spec:** 下单时扣除积分、扣库存、生成订单需原子操作。

**AI Generation Analysis:**
AI 建议使用 **Saga 模式** 或 **编程式事务（UserTransaction）** 来处理潜在的分布式风险。

**Mental Execution (Curator Decision):**

* **Saga:** 对于一个两周的单体应用（Monolithic）来说，引入 Saga 过于复杂，增加了维护成本。
* **UserTransaction:** 代码侵入性太强，干扰业务逻辑阅读。
* **Risk Analysis:** 当前架构是单数据库、单服务器。

**Decision:**
拒绝 AI 的复杂方案，采用 **容器管理事务（`@Transactional`）**。

**Correction:**
在 `OrderService.createOrder` 方法上添加 `@Transactional`，并确保在方法内部先检查 `user.getPoints()` 余额，再执行扣减。利用数据库的 ACID 特性保证一致性，代码行数减少了 50%。

---

## 3. 模块：积分审计表设计 (Database Design)

**Date:** 2025-12-25

**Spec:** 需要记录积分变动流水。

**AI Generation Analysis:**
AI 建议在 `PointRecord` 表中增加 `businessId`（String）做幂等性校验，并增加 `version` 做乐观锁。

**Mental Execution (Simplification):**
虽然 `businessId` 是好主意，但在当前需求中，`orderId` 实际上已经充当了幂等 Key 的角色（一个订单只能抵扣一次）。增加过多字段会增加开发时间。

**Decision:**
采纳 AI 关于“独立流水表”的建议，但简化字段。

**Correction:**
保留 `userId`、`points`、`source`、`createdAt` 核心字段，确保每一笔积分变动都有据可查，便于后续审计。

---

## 4. 模块：项目骨架与配置 (Project Skeleton & Configuration)

**Date:** 2025-12-25

**Spec:** 搭建基于 Maven 的 Jakarta EE 10 项目结构，适配 Tomcat 11。

**AI Generation Analysis:**
AI 提供了一个标准的 `pom.xml`，正确引入了 Jakarta EE API（`provided`）、Hibernate 6（`compile`）、Weld（`compile`）等关键依赖。同时提供了适配 Tomcat 的 `persistence.xml` 配置，特别是 `transaction-type="RESOURCE_LOCAL"`。

**Mental Execution (Validation):**
+1

**Tomcat Limitation:**
Tomcat 不是完整的 Application Server，不支持 JTA（Java Transaction API）。AI 建议的 `RESOURCE_LOCAL` 是正确的，但需要确认是否需要手动管理 `EntityManager`。

**Solution:**
AI 提供了 `JpaConfig` 类，通过 CDI 生产 `EntityManager`，这是一个很好的模式，避免了每次手动创建 `EntityManagerFactory`。

**Correction:**
采纳 AI 的 `pom.xml` 和 `persistence.xml` 配置，并集成了 `JpaConfig` 和 `TransactionManager` 工具类，以在 Tomcat 中实现**类似容器管理事务的体验**。

---

## 5. 模块：实体类设计 (Entity Design) - 类型安全

**Date:** 2025-12-26

**Spec:** 定义系统核心实体，包括用户角色的存储。

**AI Generation Analysis:**
在 Prompt 中，为了方便描述，要求 `role` 字段使用 `String` 类型（"ADMIN" / "USER"）。AI 忠实地执行了指令，在生成的 `User` 类中定义了 `private String role;`。

**Mental Execution (V&V):**

* **Risk:** 使用 `String` 存储角色极其危险。如果后续代码手误写成 "Admin"（大小写不同）或 "USRE"（拼写错误），编译器不会报错，但权限拦截等关键业务逻辑会直接失效，形成严重的安全隐患。

**Correction:**

1. 定义 `public enum Role { USER, ADMIN }`。
2. 将 `User` 实体中的字段类型修改为 `Role`，并添加 `@Enumerated(EnumType.STRING)` 注解。
3. 所有涉及角色的判断均转为**编译期检查（Compile-time Check）**，从源头消除该类错误。

---

## 6. 模块：持久层基础设施 (Persistence Infrastructure)

**Date:** 2025-12-27

**Spec:** 管理 `EntityManagerFactory` 和 `EntityManager` 的生命周期。

**AI Generation Analysis:**
AI 推荐了一个基于 CDI 的 `JpaConfig` 方案，通过 `@Produces` / `@Disposes` 管理 `EntityManager`，在标准 Jakarta EE 环境下非常优雅。

**Mental Execution (Curator Decision):**

* **Complexity:** 在 Tomcat 环境中启用完整的 CDI `@RequestScoped` 上下文需要额外配置 Filter 与 Weld，调试成本较高。
* **Project Context:** 当前是一个两周交付的敏捷项目，可维护性优先于架构“纯洁性”。

**Decision:**
放弃 CDI 方案，采用更直观、更可控的**静态工厂 / 单例工具类模式**。

**Correction:**
实现 `PersistenceUtil` 工具类，通过 `Persistence.createEntityManagerFactory` 显式管理 `EntityManager` 的创建与关闭。虽然牺牲了部分依赖注入的优雅性，但显著降低了环境相关问题的排查难度，避免了 CDI 上下文未激活导致的隐性错误。
