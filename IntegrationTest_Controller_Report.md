# Controller 层集成测试汇报

## 1. 汇报信息

| 项目 | 内容 |
|---|---|
| 项目名称 | SoftwareTestingDemo |
| 测试类型 | 集成测试（Controller 层功能模块） |
| 负责成员 | 成员E |
| 汇报日期 | 2026-04-08 |
| 测试框架 | Spring Boot Test + MockMvc + JUnit 5 + Mockito |
| 测试脚本 | src/test/java/com/demo/integration/IntegrationFlowTest.java |

## 2. 测试目标

验证 Controller 层核心业务流程在接口入口层的行为正确性，重点检查：
- 正常流程是否返回预期结果
- 登录态相关逻辑是否生效
- 请求参数进入 Controller 后是否触发正确业务调用
- 页面跳转与 JSON 响应是否符合预期

## 3. 覆盖范围

| 模块 | 关键接口/功能 | 用例编号 |
|---|---|---|
| 登录流程 | `/loginCheck.do` | IT-LG-001, IT-LG-002 |
| 订单流程 | `/order_manage`, `/addOrder.do` | IT-OR-001, IT-OR-002 |
| 场馆流程 | `/venuelist/getVenueList`, `/venue` | IT-VN-001, IT-VN-002 |
| 留言流程 | `/sendMessage`, `/message/findUserList` | IT-MG-001, IT-MG-002 |

## 4. 用例与结果统计

| 测试场景 | 总用例 | 通过 | 失败 | 阻塞 |
|---|---:|---:|---:|---:|
| 登录流程 | 2 | 2 | 0 | 0 |
| 订单流程 | 2 | 2 | 0 | 0 |
| 场馆流程 | 2 | 2 | 0 | 0 |
| 留言流程 | 2 | 2 | 0 | 0 |
| 合计 | 8 | 8 | 0 | 0 |

## 5. 关键验证点

| 验证点 | 结果 |
|---|---|
| 合法用户登录后写入 Session 并返回 `/index` | 通过 |
| 错误密码登录返回 `false` | 通过 |
| 未登录访问订单管理触发登录异常逻辑 | 通过 |
| 登录后提交订单触发业务提交并重定向 | 通过 |
| 场馆分页接口返回结构化 JSON 列表 | 通过 |
| 场馆详情页能够渲染目标场馆信息 | 通过 |
| 留言提交后重定向到留言页 | 通过 |
| 未登录查询个人留言触发登录异常逻辑 | 通过 |

## 6. 执行说明

已在本地通过 Maven 执行该测试类并验证通过，示例命令：

```powershell
$env:MAVEN_HOME=Join-Path $env:LOCALAPPDATA 'Apache\maven\apache-maven-3.9.14'
$env:Path="$env:MAVEN_HOME\bin;$env:Path"
Set-Location 'd:\desktop\testpj\SoftwareTestingDemo'
mvn -Dtest=IntegrationFlowTest test
```

## 7. 结论

Controller 层集成测试已完成且可运行，当前覆盖登录、订单、场馆、留言四个核心功能模块。首轮执行结果为 8/8 全部通过，未发现新增缺陷。

## 8. 相关文档

- docs/test-cases/IT-Integration/TC_IT.md
- docs/test-summary/TestSummaryReport.md（第3章）
