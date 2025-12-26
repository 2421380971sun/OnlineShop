# Mental Execution Log (ME-log)

## 1. 模块：用户注册逻辑 (User Registration)
**Date:** 2025-12-22

**Spec:** 用户注册需绑定属性到 Bean 并持久化。

**AI Generation Analysis:**
AI 生成了 `UserController`，直接调用 `userService.register(user)`。

**Mental Execution (V&V):**
在脑海中模拟 JSF 生命周期：当页面加载 `value="#{userController.user.username}"` 时，如果 Controller 中的 `user` 对象为 null，EL 表达式会抛出 `PropertyNotFoundException`。AI 经常忽略 Bean 的初始化。
此外，AI 生成的代码中 `user.setRole("USER")` 使用了字符串，而我的实体定义了 `Enum`，这会导致类型安全问题。

**Correction:**
1. 在 `UserController` 构造或声明时手动 `new User()`。
2. 修正类型匹配：`user.setRole(User.Role.USER)`。
   

---

## 2. 模块：积分与订单的一致性 (Transaction Safety)

**Date:** 2025-12-22

**Spec:** 下单时扣除积分、扣库存、生成订单需原子操作。

**AI Generation Analysis:**
AI 建议使用 **Saga 模式** 或 **编程式事务 (UserTransaction)** 来处理潜在的分布式风险。

**Mental Execution (Curator Decision):**
- **Saga:** 对于一个两周的单体应用（Monolithic）来说，引入 Saga 过于复杂，增加了维护成本。
- **UserTransaction:** 代码侵入性太强，干扰业务逻辑阅读。
- **Risk Analysis:** 我现在的架构是单数据库、单服务器。


  **Decision:**
  我决定拒绝 AI 的复杂方案，采用 **容器管理事务 (`@Transactional`)**。

**Correction:**
  我在 `OrderService.createOrder` 方法上添加 `@Transactional`，并确保在方法内部先检查 `user.getPoints()` 余额，再执行扣减。利用数据库的 ACID 特性保证一致性，代码行数减少了 50%。

---

## 3. 模块：积分审计表设计 (Database Design)

**Date:** 2025-12-22

**Spec:** 需要记录积分变动流水。

**AI Generation Analysis:**
AI 建议在 `PointRecord` 表中增加 `businessId` (String) 做幂等性校验，并增加 `version` 做乐观锁。

**Mental Execution (Simplification):**
虽然 `businessId` 是好主意，但在当前需求中，`orderId` 实际上已经充当了幂等 Key 的角色（一个订单只能抵扣一次）。增加过多的字段会增加开发时间。

**Decision:**
采纳 AI 关于“独立流水表”的建议，但简化字段。

**Correction:**
保留 `userId`, `points`, `source`, `createdAt` 核心字段，确保每一笔积分变动都有据可查，便于后续审计。