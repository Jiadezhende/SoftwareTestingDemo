#!/usr/bin/env python3
"""
normalize_docs.py — 提交前文档规范检查
被 .githooks/pre-commit 自动调用，也可手动运行：
    python scripts/normalize_docs.py
返回码：0 = 全部通过，1 = 有错误（pre-commit 会阻止提交）
"""

import re
import sys
from pathlib import Path
from collections import defaultdict

# Windows 终端强制 UTF-8 输出
if sys.stdout.encoding and sys.stdout.encoding.lower() != "utf-8":
    sys.stdout.reconfigure(encoding="utf-8", errors="replace")

ROOT = Path(__file__).parent.parent

# ── 规则常量 ──────────────────────────────────────────────

VALID_CONCLUSIONS = {"正确", "错误", "待测"}

# 各文件允许的编号前缀
PREFIX_RULES = {
    "TC_UT_US.md": r"^UT-US-\d{3}$",
    "TC_UT_OR.md": r"^UT-OR-\d{3}$",
    "TC_UT_VN.md": r"^UT-VN-\d{3}$",
    "TC_UT_NW.md": r"^UT-NW-\d{3}$",
    "TC_UT_MG.md": r"^UT-MG-\d{3}$",
    "TC_IT.md":    r"^IT-(LG|OR|VN|MG)-\d{3}$",
}

# 单元测试必填列
UT_REQUIRED_FIELDS = {"用例编号", "用例描述", "预期结果", "结论"}
# 集成测试必填列（多功能点列表）
IT_REQUIRED_FIELDS = {"功能点列表", "用例编号", "用例描述", "预期结果", "结论"}

IT_FILES = {"TC_IT.md"}

# ANSI 颜色
GREEN  = "\033[92m"
RED    = "\033[91m"
YELLOW = "\033[93m"
BOLD   = "\033[1m"
RESET  = "\033[0m"

# ── 解析器 ────────────────────────────────────────────────

def parse_tables(md_text: str) -> list[tuple[list[str], list[dict]]]:
    """
    返回文件中所有表格的 (headers, rows) 列表。
    只处理含"用例编号"的表格（测试用例表）。
    """
    tables = []
    lines = md_text.splitlines()
    headers = []
    rows = []
    in_table = False

    for line in lines:
        stripped = line.strip()
        if not stripped.startswith("|"):
            if in_table and rows:
                tables.append((headers, rows))
            in_table = False
            headers = []
            rows = []
            continue

        cells = [c.strip() for c in stripped.strip("|").split("|")]

        # 分隔行
        if all(re.match(r"^:?-+:?$", c) for c in cells if c):
            continue

        if "用例编号" in cells:
            if in_table and rows:
                tables.append((headers, rows))
            headers = cells
            rows = []
            in_table = True
            continue

        if in_table and headers:
            if len(cells) >= len(headers):
                rows.append(dict(zip(headers, cells[:len(headers)])))
            else:
                # 补全缺失列
                padded = cells + [""] * (len(headers) - len(cells))
                rows.append(dict(zip(headers, padded)))

    if in_table and rows:
        tables.append((headers, rows))

    return tables


# ── 检查器 ────────────────────────────────────────────────

def check_file(filepath: Path) -> list[str]:
    """检查单个 TC_*.md，返回问题列表"""
    errors = []
    fname = filepath.name
    text = filepath.read_text(encoding="utf-8")
    tables = parse_tables(text)

    if not tables:
        errors.append(f'[{fname}] 未找到测试用例表格（需包含"用例编号"列）')
        return errors

    prefix_pattern = PREFIX_RULES.get(fname)
    required_fields = IT_REQUIRED_FIELDS if fname in IT_FILES else UT_REQUIRED_FIELDS
    seen_ids = set()

    for headers, rows in tables:
        # 检查必要列是否存在
        missing_cols = required_fields - set(headers)
        if missing_cols:
            errors.append(f"[{fname}] 表格缺少必要列：{missing_cols}")

        for i, row in enumerate(rows, start=1):
            case_id  = row.get("用例编号", "").strip()
            desc     = row.get("用例描述", "").strip()
            expected = row.get("预期结果", "").strip()
            conc     = row.get("结论", "").strip()
            owner    = row.get("负责人", "").strip()

            # 跳过纯占位行（描述和预期结果均为空）
            if not desc and not expected:
                continue

            line_ref = f"[{fname}] 行#{i} ({case_id or '无编号'})"

            # 1. 用例编号不能为空（允许整数或 UT-XX-NNN 两种格式）
            if not case_id:
                errors.append(f"{line_ref} 用例编号为空")

            # 2. 编号重复
            if case_id:
                if case_id in seen_ids:
                    errors.append(f"{line_ref} 用例编号重复：{case_id}")
                seen_ids.add(case_id)

            # 3. 用例描述不能为空
            if not desc or desc in ("—", "-"):
                errors.append(f"{line_ref} 用例描述为空")

            # 4. 预期结果不能为空
            if not expected or expected in ("—", "-"):
                errors.append(f"{line_ref} 预期结果为空")

            # 5. 结论字段合法性
            if conc not in VALID_CONCLUSIONS:
                errors.append(
                    f"{line_ref} 结论字段非法（当前='{conc}'，允许：{VALID_CONCLUSIONS}）"
                )

            # 6. 结论为"错误"时检查缺陷日志关联
            if conc == "错误":
                defect_log = ROOT / "docs" / "defects" / "DefectLog.md"
                if defect_log.exists():
                    defect_text = defect_log.read_text(encoding="utf-8")
                    if case_id and case_id not in defect_text:
                        errors.append(
                            f'{line_ref} 结论为"错误"但 DefectLog.md 中未找到关联用例编号 {case_id}'
                        )

    return errors


def check_defect_log() -> list[str]:
    """检查缺陷日志格式"""
    errors = []
    defect_log = ROOT / "docs" / "defects" / "DefectLog.md"
    if not defect_log.exists():
        return []

    text = defect_log.read_text(encoding="utf-8")
    tables = parse_tables(text)
    seen_ids = set()
    bug_pattern = re.compile(r"^BUG-\d{3}$")

    for headers, rows in tables:
        for i, row in enumerate(rows, start=1):
            bug_id = row.get("缺陷编号", "").strip()
            desc   = row.get("缺陷描述", "").strip()
            sev    = row.get("严重程度", "").strip()
            status = row.get("状态", "").strip()

            line_ref = f"[DefectLog.md] 行#{i} ({bug_id or '无编号'})"

            if not bug_id:
                errors.append(f"{line_ref} 缺陷编号为空")
            elif not bug_pattern.match(bug_id):
                errors.append(f"{line_ref} 缺陷编号格式不符（应为 BUG-xxx）")

            if bug_id in seen_ids:
                errors.append(f"{line_ref} 缺陷编号重复：{bug_id}")
            if bug_id:
                seen_ids.add(bug_id)

            if not desc or desc in ("—", "-"):
                errors.append(f"{line_ref} 缺陷描述为空")

            if sev not in ("高", "中", "低", ""):
                errors.append(f"{line_ref} 严重程度非法（当前='{sev}'，允许：高/中/低）")

    return errors


def check_test_methods_exist(tc_files: list[Path]) -> list[str]:
    """检查测试用例表中填写的测试函数名是否在 src/test/java/ 中真实存在"""
    errors = []
    test_src = ROOT / "src" / "test" / "java"

    if not test_src.exists():
        return []  # 测试代码目录尚未创建，跳过

    # 收集所有 Java 测试文件的文本（合并后搜索，避免逐文件重复读）
    java_files = list(test_src.rglob("*.java"))
    if not java_files:
        return []  # 还没写测试代码，跳过

    java_sources = ""
    for java_file in java_files:
        java_sources += java_file.read_text(encoding="utf-8", errors="ignore")

    for filepath in tc_files:
        text = filepath.read_text(encoding="utf-8")
        # 从 **测试函数** 或 **测试脚本** 行提取方法名
        for match in re.finditer(r"\*\*测试(?:函数|脚本)\*\*[：:]\s*`?([^`\n]+)`?", text):
            raw = match.group(1).strip()
            # 只有包含 Class:method 格式时才检查方法存在性
            if ":" not in raw:
                continue
            method_part = raw.rsplit(":", 1)[-1]
            method_name = re.sub(r"\(.*\)$", "", method_part).strip()
            if not method_name:
                continue
            pattern = rf"\bvoid\s+{re.escape(method_name)}\s*\("
            if not re.search(pattern, java_sources):
                errors.append(
                    f"[{filepath.name}] 测试函数 '{method_name}' 在 src/test/java/ 中未找到"
                )

    return errors


def check_document_info(filepath: Path) -> list[str]:
    """检查文档信息表中的必填字段（最后更新日期）"""
    errors = []
    text = filepath.read_text(encoding="utf-8")
    # 简单检查"最后更新"字段是否已填写
    if "最后更新" in text:
        match = re.search(r"\|\s*最后更新\s*\|\s*(\S+)\s*\|", text)
        if match:
            val = match.group(1)
            if val in ("（填写", "YYYY-MM-DD", "—", "-", ""):
                errors.append(f'[{filepath.name}] "最后更新"日期未填写')
    return errors


# ── 主函数 ────────────────────────────────────────────────

def main() -> int:
    tc_dir = ROOT / "docs" / "test-cases"
    tc_files = list(tc_dir.rglob("TC_*.md"))

    all_errors = []

    for f in sorted(tc_files):
        all_errors.extend(check_file(f))
        all_errors.extend(check_document_info(f))

    all_errors.extend(check_defect_log())
    all_errors.extend(check_test_methods_exist(tc_files))

    if all_errors:
        print(f"\n{BOLD}{RED}✗ 文档规范检查失败，发现 {len(all_errors)} 个问题：{RESET}\n")
        for err in all_errors:
            print(f"  {RED}•{RESET} {err}")
        print(f"\n{YELLOW}请修复以上问题后再提交。{RESET}\n")
        return 1
    else:
        print(f"\n{BOLD}{GREEN}✓ 文档规范检查通过，共检查 {len(tc_files)} 个文件。{RESET}\n")
        return 0


if __name__ == "__main__":
    sys.exit(main())
