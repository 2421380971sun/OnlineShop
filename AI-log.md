# AI 协作日志 (AI Collaboration Log)

## Phase 1: Architecture Sounding Board (架构鸣响板)

**日期**: 2025-12-23
**模型**: Gemini LLM (Gemini Large Language Model)
**角色设置**: Senior Java EE Architect (资深 Java EE 架构师)
**目标**: 针对当前技术栈 (JSF+JPA+CDI) 进行严厉的架构审查，挖掘潜在的“技术坑点”和性能隐患。

---

### 1. User Prompt (初始提示词)

> **角色设置**：你是一位资深的 Java EE 架构师。
> **背景**：我正在开发一个基于原生 Jakarta EE (Java EE) 的在线商城系统。
> **技术栈**：JSF (前端), JPA (Hibernate, 数据持久化), CDI (依赖注入), MySQL, Tomcat。
> **任务**：请严厉地审查我的架构设计。不要只说好话，请找出至少 3 个潜在的技术风险或“坑点”（例如事务管理、JSF生命周期与AJAX的冲突、JPA懒加载异常等），并给出解决方案。
> **项目简介**：本项目包含用户管理、商品管理、订单管理及积分系统。系统采用 JSF 实现前端界面，JPA 进行数据持久化，CDI 实现依赖注入。用户可通过 Web 界面浏览商品、购买、查看订单及使用积分抵扣。

---

### 2. AI Response (AI 审查意见)

你好。我是你的架构师。既然你要求“严厉审查”，那我就不客套了。
作为一个基于原生 Jakarta EE 的商城项目，你的技术栈选择（JSF + JPA + CDI）是非常标准的“官方”组合，但在 2024/2025 年的背景下，这个架构如果不加小心，写出来的就是**“性能黑洞”和“维护噩梦”**。

以下是我找出的三个最致命的“坑点”以及相应的架构修正方案。

#### 1. 基础设施陷阱：Tomcat 不是 Jakarta EE 容器
* **风险分析**：你声称使用“原生 Jakarta EE”，却打算部署在 Tomcat 上。Tomcat 本质上只是一个 Servlet 容器，它不包含 CDI (Weld), JSF (Mojarra/MyFaces) 和 JPA (Hibernate) 的实现。这会导致**版本地狱（JAR Hell）**和**启动失败**（CDI Bean Manager 无法找到）。
* **解决方案**：建议手动在 pom.xml 中严格锁定 Weld-Servlet、Hibernate-Core 和 Mojarra 的版本，并配置 `META-INF/context.xml` 以启用 CDI。

#### 2. 持久化层危机：JPA 的 LazyInitializationException 与 N+1 问题
* **风险分析**：在商城系统中，你肯定会写出类似 `user.getOrders().get(0).getItems()` 这样的代码。在 JSF 的渲染阶段（Render Response），Hibernate Session 早就关闭了。
    * **Lazy加载异常**：JSF 页面渲染时数据库连接已断开，抛出 `LazyInitializationException`。
    * **N+1 查询灾难**：如果你改为 `EAGER` 加载，查询 1 个用户会连带查询 N 个订单，瞬间压垮数据库。
* **解决方案**：绝对禁止在 Entity 中使用 `FetchType.EAGER`。必须在 Service 层使用 **"Join Fetch" (JPQL)** 显式加载关联数据。

#### 3. 业务逻辑漏洞：库存超卖与积分并发 (Race Condition)
* **风险分析**：商城系统最忌讳“库存只有1个，卖给了5个人”。仅靠 `@Transactional` 是不够的。
    * **库存超卖**：线程 A 和线程 B 同时读取库存为 1，都执行减扣，导致库存变 -1。
* **解决方案**：引入 **乐观锁 (Optimistic Locking)**。在 `Product` 实体中增加 `@Version` 字段，依赖 JPA 的版本检查机制防止并发覆盖。

---

### 3. Risk Assessment & Decision (策展人决策记录)

> *此处记录我作为“策展人”对 AI 建议的评估与采纳情况*

| 风险点 ID | 风险描述 (Risk) | 严重等级 | 决策 (Decision) | 实施方案 (Action Item) |
| :--- | :--- | :--- | :--- | :--- |
| **R-01** | **Tomcat 兼容性** (Tomcat 缺少 CDI/JSF 实现) | High | **采纳 (Adapt)** | 坚持使用 Tomcat 以符合实验环境要求，但会严格检查 `pom.xml` 依赖，确保引入了 Weld 和 Mojarra 的独立包。 |
| **R-02** | **Lazy 加载异常** (JPA Session 在 View 层关闭) | Critical | **采纳 (Fix)** | 拒绝使用 `OpenSessionInView` 模式（反模式）。决定在 Service 层使用 `JOIN FETCH` 语句预加载数据，确保 View 层拿到的是完整对象。 |
| **R-03** | **库存超卖并发** (缺少锁机制) | Critical | **采纳 (Fix)** | AI 发现了核心业务逻辑漏洞。将在 Phase 2 编码阶段重点修复此问题，在 `Product` 表增加 `@Version` 字段实现乐观锁。 |

---

