# 协作规范 CONTRIBUTING

> 五人小组软件测试课程项目协作规范，所有成员必须遵守。

---

## 1. 人员分工

| 成员         | 负责测试模块        | 用例编号段             | 负责文档章节                           |
|--------------|--------------------|-----------------------|---------------------------------------|
| 成员A（组长） | UserServiceImpl    | `UT-US-001~099`       | 测试总结报告 第1章 + 第4章 + 汇总       |
| 成员B        | OrderServiceImpl   | `UT-OR-001~099`       | 测试总结报告 第2章（单元测试-Order）     |
| 成员C        | VenueServiceImpl   | `UT-VN-001~099`       | 测试总结报告 第2章（单元测试-Venue）     |
| 成员D        | NewsServiceImpl    | `UT-NW-001~099`       | 测试总结报告 第2章（单元测试-News）      |
| 成员D        | MessageServiceImpl | `UT-MG-001~099`       | 测试总结报告 第2章（单元测试-Message）   |
| 成员E        | 集成测试            | `IT-LG/OR/VN/MG-001~099` | 测试总结报告 第3章 + IEEE829计划        |

> 示例：张三负责 UserServiceImpl，在 `TC_UT_US.md` 中填写编号 `UT-US-001` 起的用例，并撰写测试总结报告第1章。

---

## 2. 目录结构

```
docs/
├── CONTRIBUTING.md              # 本文件，协作规范
├── test-plan/
│   └── IEEE829_TestPlan.md      # 成员E负责
├── test-cases/
│   ├── _TEMPLATE.md             # 用例表格模板（不要修改）
│   ├── UT-UserService/
│   │   └── TC_UT_US.md          # 成员A负责
│   ├── UT-OrderService/
│   │   └── TC_UT_OR.md          # 成员B负责
│   ├── UT-VenueService/
│   │   └── TC_UT_VN.md          # 成员C负责
│   ├── UT-NewsService/
│   │   └── TC_UT_NW.md          # 成员D负责
│   ├── UT-MessageService/
│   │   └── TC_UT_MG.md          # 成员D负责
│   └── IT-Integration/
│       └── TC_IT.md             # 成员E负责
├── test-summary/
│   └── TestSummaryReport.md     # 组长汇总，各成员按章节填写
└── defects/
    └── DefectLog.md             # 全组共同维护

scripts/
├── check_progress.py            # 终端输出进度看板
└── normalize_docs.py            # 文档规范检查（被 pre-commit 调用）

.githooks/
├── pre-commit                   # commit 前：文档规范检查
└── pre-push                     # push 前：mvn test
```

---

## 3. 用例编号规范

格式：`{类型}-{模块}-{三位序号}`

| 示例         | 含义                          |
|-------------|------------------------------|
| `UT-US-001` | 单元测试 / UserService / 第1条  |
| `UT-OR-001` | 单元测试 / OrderService / 第1条 |
| `UT-VN-001` | 单元测试 / VenueService / 第1条 |
| `UT-NW-001` | 单元测试 / NewsService / 第1条    |
| `UT-MG-001` | 单元测试 / MessageService / 第1条 |
| `IT-LG-001` | 集成测试 / 登录流程 / 第1条       |
| `IT-OR-001` | 集成测试 / 订单流程 / 第1条       |
| `IT-VN-001` | 集成测试 / 场馆流程 / 第1条       |
| `IT-MG-001` | 集成测试 / 消息流程 / 第1条       |

**禁止**跨成员使用对方编号段，防止合并冲突。

> 示例：成员A新增第2条用例，编号为 `UT-US-002`，不能使用 `UT-OR-xxx`。

---

## 4. 结论字段规范

结论列只允许以下三个值（`normalize_docs.py` 和 `check_progress.py` 均依赖此字段统计进度）：

| 值    | 含义                                              |
|------|--------------------------------------------------|
| `待测` | 尚未执行                                          |
| `正确` | 执行后符合预期                                    |
| `错误` | 不符合预期（**必须同时在 DefectLog.md 添加缺陷**）  |

> **注意**：`通过`、`失败`、`阻塞` 均为非法值，提交时将被 pre-commit 钩子拒绝。

> 示例：执行 `UT-US-001` 后返回值不符合预期，将结论改为 `错误`，并在 DefectLog.md 追加 `BUG-001`。

---

## 5. 缺陷登记规范

发现失败用例时，在 `docs/defects/DefectLog.md` 末尾追加一行。
缺陷编号：`BUG-{三位序号}`，全组按发现顺序统一递增。

> 示例：
> | BUG-001 | 张三 | 2026-04-01 | 单元测试 | UT-US-001 | checkLogin 传空密码时 NPE | 高 | 新建 | |

> **触发条件**：用例结论填写 `错误` 后，必须在此日志中追加对应缺陷记录，否则 pre-commit 将阻止提交。

---

## 6. 快速命令

```bash
# ① 克隆仓库后执行一次，启用 Git 钩子（每人只需运行一次）
git config core.hooksPath .githooks

# ② 查看当前测试进度（终端输出）
python scripts/check_progress.py

# ③ 手动运行文档规范检查（git commit 时会自动触发）
python scripts/normalize_docs.py
```

### Git 钩子说明

| 钩子         | 触发时机     | 做什么                                    |
|-------------|------------|-------------------------------------------|
| `pre-commit` | `git commit` | 检查 docs/ 文档规范（编号格式、必填字段、结论合法性、缺陷关联等） |
| `pre-push`   | `git push`   | 运行 `mvn test`，测试不通过则阻止推送      |

### pre-commit 检查项明细

`normalize_docs.py` 会对所有 `TC_*.md` 执行以下检查：

1. 表格必须包含 `用例编号` 列（单元测试还需 `用例描述`、`预期结果`、`结论`；集成测试另需 `功能点列表`）
2. 用例编号不能为空，不能重复，格式须符合对应文件规则（如 `UT-US-\d{3}`）
3. 用例描述与预期结果不能为空或占位符（`—`、`-`）
4. 结论只允许 `待测` / `正确` / `错误` 三个值
5. 结论为 `错误` 时，`DefectLog.md` 中必须存在该用例编号
6. 文档信息表的 `最后更新` 字段必须填写实际日期（不能留空或填占位符）
7. `**测试函数**` 若使用 `Class:method` 格式，对应方法须在 `src/test/java/` 中存在

---

## 7. 测试设计方法说明

| 方法       | 适用场景                      |
|-----------|------------------------------|
| 等价类划分  | 输入参数的合法 / 非法分类        |
| 边界值分析  | 数字、字符串长度等边界           |
| 决策表     | 多条件组合逻辑                  |
| 因果图     | 复杂输入输出关系                |
| 场景法     | 集成测试业务流程                |

> 示例：测试 `checkLogin` 的密码参数，用**等价类划分**分为「正确密码」「错误密码」「null」「空字符串」四类，每类取一个代表值。
