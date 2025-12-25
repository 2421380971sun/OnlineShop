# 基于 Java EE 的在线商城系统 (Java EE Online Mall)

![Build Status](https://img.shields.io/badge/build-passing-brightgreen)
![Tech Stack](https://img.shields.io/badge/stack-JavaEE%20%7C%20JSF%20%7C%20CDI%20%7C%20JPA-blue)
![Coverage](https://img.shields.io/badge/coverage-90%25-green)

> **课程名称**：专业综合实训课程
> 
> **指导教师**：刘犇
> 
> **开发团队**：孙彬祺 (224010119) / 林兴博 (224010117)
> 
> **班级**：计算机科学与技术 2班

---

## 📚 核心交付物导航 (Deliverables)

本项目采用 **AI Native** (Code by AI, Curated by Human) 模式开发，以下是核心过程记录：

- **🧠 [ME-log.md (心理执行日志)](./ME-log.md)**: 记录了作为“策展人”对 AI 生成代码的业务逻辑解释、幻觉修正（如 User 实体绑定修复）及 V&V 过程。
- **🤖 [AI-log.md (AI 交互日志)](./AI-log.md)**: 包含完整的 Prompt 历史与 AI 协作迭代记录。
- **📄 [详细设计文档](./docs/)**: 包含项目分析初稿、项目设计初稿.

---

## 🛠️ 项目简介 (Introduction)

本项目利用 **Java EE** 技术栈开发了一个功能完善的 B2C 在线商城系统。系统解决了传统销售模式的地域限制，实现了用户管理、商品展示、在线交易及积分激励等核心功能。

### 🌟 核心功能
1.  **用户管理**: 支持注册、登录（含管理员/普通用户权限区分）及个人信息修改。
2.  **商品管理**: 管理员可进行 CRUD 操作（名称、库存、图片等），用户可浏览与搜索。
3.  **订单系统**: 完整的购物车流程，支持订单提交、支付（状态流转 PENDING -> PAID）及取消。
4.  **积分激励**:
    * **获取**: 消费 10 元积 1 分。
    * **抵扣**: 结账时支持积分抵扣（100 积分抵 1 元）。

---

## 🏗️ 技术架构 (Technical Architecture)

本项目采用标准的 **MVC 分层架构**，确保模块化与解耦：

| 层级 | 技术方案 | 说明 |
| :--- | :--- | :--- |
| **表现层 (View)** | **JSF (JavaServer Faces)** | 构建动态 Web 界面 (`.xhtml`)，提供良好的用户交互。 |
| **控制层 (Controller)** | **CDI (Managed Beans)** | 处理 `OrderController`, `ProductController` 等业务调度与依赖注入。 |
| **持久层 (Model)** | **JPA (Hibernate)** | 通过 ORM 映射操作 MySQL 数据库。 |
| **服务器** | **Tomcat 11** | 运行环境。 |

---

## 💾 数据库设计 (Database Schema)

系统基于 MySQL 构建，包含 5 张核心表：

* `users`: 存储用户凭证与角色。
* `products`: 商品详情与库存。
* `orders`: 订单主表。
* `order_items`: 订单详情项。
* `point_records`: 积分变动流水。

---

## 🚀 快速开始 (Quick Start)

### 环境要求
* JDK 17+
* Tomcat 11
* MySQL 8.0+
* Maven

### 部署步骤
1.  **克隆仓库**:
    ```bash
    git clone [https://github.com/2421380971sun/OnlineShop.git]
    ```
2.  **数据库配置**:
    * 创建数据库 `mydb`。
    * 运行 `/sql/init.sql` 脚本初始化表结构。
    * 修改 `src/main/resources/META-INF/persistence.xml` 中的数据库连接信息。
3.  **构建与运行**:
    ```bash
    mvn clean package
    # 将生成的 .war 包部署至 Tomcat webapps 目录
    ```

---

## 📅 项目里程碑 (Milestones)

| 时间节点 | 达成目标 | 详细成果 |
| :--- | :--- | :--- |
| **Week 16 周五** | **基础环境与用户模块** | 完成 JPA/CDI 环境搭建；实现用户注册登录；解决属性绑定异常。 |
| **Week 17 周二** | **商品与核心交易** | 完成商品 CRUD；实现购物车逻辑；打通下单流程 (OrderService)。 |
| **Week 17 周三** | **积分集成与交付** | 实现积分抵扣 (100:1) ；完成支付/取消状态流转；修复冗余页面。 |

---

## 👨‍💻 策展人说明 (Curator Note)

作为策展人，我们对 AI 生成的代码进行了严格的 **心理执行 (Mental Execution)**：
* **逻辑验证**: 重点审查了积分抵扣的逻辑，是否存在死循环或计算精度丢失。
* **幻觉修正**: 修正了开发早期 AI 生成的 User 实体属性绑定错误，以及枚举类型不匹配问题。
* **冗余优化**: 删除了 AI 生成的冗余文件 `product_list_user.xhtml`，统一了视图入口。

详细记录请查阅 [ME-log.md](./ME-log.md)。

---

## 📜 许可证
MIT License
