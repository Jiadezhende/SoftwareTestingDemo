# SoftwareTestingDemo — 软件测试课程项目

基于 Spring Boot 的场馆预约系统，作为软件测试课程的被测对象。本仓库同时包含完整的测试文档体系和自动化辅助脚本。

## 技术栈

- Java 8 / Spring Boot 2.2.2
- MySQL 8 + Spring Data JPA
- Thymeleaf / Selenium 3
- JUnit 5 + Hamcrest
- Maven

## 目录结构

```
SoftwareTestingDemo/
├── src/
│   ├── main/java/com/demo/      # 被测业务代码
│   └── test/java/com/demo/      # 测试代码
├── docs/
│   ├── CONTRIBUTING.md          # 协作规范（必读）
│   ├── test-plan/               # IEEE 829 测试计划
│   ├── test-cases/              # 各模块测试用例
│   ├── defects/                 # 缺陷日志
│   └── test-summary/            # 测试总结报告
├── scripts/
│   ├── normalize_docs.py        # 文档规范检查
│   └── check_progress.py        # 测试进度看板
└── .githooks/                   # Git 钩子
```

## 快速开始

### 1. 启动应用

配置 `src/main/resources/application.properties` 中的数据库连接，导入 `demo_db.sql`，然后：

```bash
mvn spring-boot:run
```

### 2. 启用 Git 钩子（每人克隆后执行一次）

```bash
git config core.hooksPath .githooks
```

启用后：
- `git commit` 时自动检查 `docs/` 文档规范
- `git push` 时自动运行 `mvn test`

### 3. 查看测试进度

```bash
python scripts/check_progress.py
```

### 4. 手动检查文档规范

```bash
python scripts/normalize_docs.py
```

## 测试模块分工

| 成员 | 负责模块 | 用例编号段 |
|------|---------|-----------|
| 成员A（组长） | UserServiceImpl                    | `UT-US-001~099`          |
| 成员B        | OrderServiceImpl                   | `UT-OR-001~099`          |
| 成员C        | VenueServiceImpl + NewsServiceImpl | `UT-VN/NW-001~099`       |
| 成员D        | MessageServiceImpl                 | `UT-MG-001~099`          |
| 成员E        | 集成测试                            | `IT-LG/OR/VN/MG-001~099` |

## 文档规范

详见 [docs/CONTRIBUTING.md](docs/CONTRIBUTING.md)，核心规则：

- 用例结论只允许 `待测` / `正确` / `错误`
- 结论为 `错误` 时必须在 `DefectLog.md` 追加缺陷记录
- `**测试函数**` 行在对应 Java 测试方法写好后再填写
