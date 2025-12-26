# AI Interaction Log (AI-log)

## Interaction 1: 技术栈与架构选型
**Context:** 项目启动阶段，确定 Jakarta EE 技术栈。

**Prompt:** "我正在开发在线商城，使用 JSF+CDI+JPA+MySQL+Tomcat。请评估技术栈并给出分层建议。"

**AI Response Summary:**
AI 认可该组合，但指出 Tomcat 仅支持 Web Profile，建议使用 Payara。同时给出了标准的 `Controller -> Service -> Repository` 分层架构。

**Curator Evaluation:**
- **Valid:** 分层架构建议非常清晰，直接采纳。
- **Critique:** 更换服务器（Payara）需要重新配置环境，且 Tomcat 11 对 Jakarta EE 10 支持已足够满足本项目需求。
- 
  **Action:** 采纳分层结构设计，保留 Tomcat，手动在 `pom.xml` 配置必要的 Jakarta 依赖。

---

## Interaction 2: 支付与积分扣减的事务风险
**Context:** `OrderService` 开发，担心高并发下积分扣减导致数据不一致。

**Prompt:** "下单时既要扣积分又要扣库存，如果数据库挂了怎么办？请分析 3 个风险并给出 Jakarta EE 解决方案。"

**AI Response Summary:**
AI 识别了 1. 重复支付、2. 部分失败（积分扣了订单没成）、3. 竞态条件。推荐了三种方案：CMT+乐观锁、编程式事务、Saga 模式。

**Curator Evaluation (The Pivot Point):**
这是最有价值的一次交互。AI 正确识别了风险，但给出的方案对于 2 周的课程项目来说过于“重型”。
- **Selection:** 我提取了 **"容器管理事务 (CMT)"** 的核心概念。
- **Modification:** 我没有引入 `@Version` 乐观锁字段（避免复杂化前端异常处理），而是选择在 `@Transactional` 方法内部进行严格的逻辑检查（`if (pointsUsed > user.getPoints()) throw Exception`）。
- 
  **Action:** 编写了 `OrderService.java` ，利用简单的 `@Transactional` 注解包裹整个下单过程，确保原子性。

---

## Interaction 3: 数据库实体模型设计
**Context:** 设计数据库，特别是 `User` 和 `PointRecord`。

**Prompt:** "请帮我分析 User, Product, Order, PointRecord 的实体设计。User 和 Order 应该用 @OneToMany 吗？"

**AI Response Summary:**
AI 建议 `PointRecord` 必须存在以用于审计。**强烈建议避免在 User 中使用 `@OneToMany List<Order>`**，因为这会导致性能问题（N+1查询）。

**Curator Evaluation:**
- **Insight:** 这一点非常关键。如果我在 User 里放一个 Order 列表，查询用户时可能会拖慢整个系统。
- **Action:** 1. 采纳建议：在 `User` 实体中删除了订单列表引用。
              2. 采纳建议：建立了 `PointRecord` 表 ，用于记录每一次积分变动（来源、数值、时间），实现了系统的可追溯性。