#!/usr/bin/env python3
"""
check_progress.py — 一键查看测试进度看板
用法：python scripts/check_progress.py
"""

import re
import sys
from pathlib import Path
from collections import defaultdict
from datetime import datetime

# Windows 终端强制 UTF-8 输出
if sys.stdout.encoding and sys.stdout.encoding.lower() != "utf-8":
    sys.stdout.reconfigure(encoding="utf-8", errors="replace")

ROOT = Path(__file__).parent.parent

VALID_CONCLUSIONS = {"正确", "错误", "待测"}
DEFECT_LOG = ROOT / "docs" / "defects" / "DefectLog.md"

# ANSI 颜色
GREEN  = "\033[92m"
RED    = "\033[91m"
YELLOW = "\033[93m"
CYAN   = "\033[96m"
BOLD   = "\033[1m"
RESET  = "\033[0m"

def parse_table_rows(md_text: str) -> list[dict]:
    """解析 MD 文件中所有符合测试用例表格格式的行"""
    rows = []
    lines = md_text.splitlines()
    headers = []
    in_table = False

    for line in lines:
        line = line.strip()
        if not line.startswith("|"):
            in_table = False
            headers = []
            continue

        cells = [c.strip() for c in line.strip("|").split("|")]

        # 分隔行（含 ---）
        if all(re.match(r"^-+$", c.replace(":", "")) for c in cells if c):
            continue

        # 表头行（含"用例编号"字样）
        if "用例编号" in cells:
            headers = cells
            in_table = True
            continue

        if in_table and headers and len(cells) >= len(headers):
            row = dict(zip(headers, cells))
            rows.append(row)

    return rows


def collect_stats():
    """遍历所有 TC_*.md，统计进度"""
    tc_dir = ROOT / "docs" / "test-cases"
    tc_files = sorted(tc_dir.rglob("TC_*.md"))

    total_stats = defaultdict(int)
    # file_stats: {filename: {"total":n, "通过":n, ..., "owners": set}}
    file_stats = {}

    for filepath in tc_files:
        text = filepath.read_text(encoding="utf-8")
        rows = parse_table_rows(text)
        f_stats = defaultdict(int)
        owners = set()

        # 从文档信息表提取负责成员（排除表格分隔符）
        owner_match = re.search(r"\|\s*负责成员\s*\|\s*([^|\n]+?)\s*\|", text)
        if owner_match:
            val = owner_match.group(1).strip()
            if val:
                owners.add(val)

        for row in rows:
            conclusion = row.get("结论", "").strip()
            if conclusion not in VALID_CONCLUSIONS:
                conclusion = "待测"

            f_stats[conclusion] += 1
            f_stats["total"] += 1
            total_stats[conclusion] += 1
            total_stats["total"] += 1

        result = dict(f_stats)
        result["owners"] = owners
        file_stats[filepath.name] = result

    return total_stats, file_stats


def parse_defects():
    """解析缺陷日志，统计各严重程度数量"""
    if not DEFECT_LOG.exists():
        return {}
    text = DEFECT_LOG.read_text(encoding="utf-8")
    rows = parse_table_rows(text)
    severity_count = defaultdict(int)
    for row in rows:
        sev = row.get("严重程度", "").strip()
        if sev in ("高", "中", "低"):
            severity_count[sev] += 1
    return dict(severity_count)


def pct(done: int, total: int) -> str:
    if total == 0:
        return "N/A"
    return f"{done / total * 100:.1f}%"


def bar(done: int, total: int, width: int = 20) -> str:
    if total == 0:
        return "[" + " " * width + "]"
    filled = int(done / total * width)
    return "[" + "█" * filled + "░" * (width - filled) + "]"


def print_dashboard():
    total_stats, file_stats = collect_stats()
    defects = parse_defects()

    now = datetime.now().strftime("%Y-%m-%d %H:%M")
    total  = total_stats.get("total", 0)
    correct= total_stats.get("正确", 0)
    wrong  = total_stats.get("错误", 0)
    pending= total_stats.get("待测", 0)
    done   = correct + wrong

    # ── 标题 ──────────────────────────────────────────────
    print(f"\n{BOLD}{CYAN}{'═'*60}")
    print(f"  软件测试进度看板  |  {now}")
    print(f"{'═'*60}{RESET}\n")

    # ── 总体进度 ──────────────────────────────────────────
    print(f"{BOLD}▌ 总体进度{RESET}")
    print(f"  总用例：{BOLD}{total}{RESET}")
    print(f"  {bar(done, total)} {pct(done, total)}")
    print(f"  ✅ 正确 {GREEN}{correct}{RESET}  "
          f"❌ 错误 {RED}{wrong}{RESET}  "
          f"⏳ 待测 {pending}")

    # ── 按模块 ────────────────────────────────────────────
    print(f"\n{BOLD}▌ 各模块进度{RESET}")
    print(f"  {'文件':<24} {'负责人':<12} {'总':<5} {'正确':<6} {'错误':<6} {'待测':<6} 完成率")
    print("  " + "─" * 64)
    for fname, s in file_stats.items():
        t = s.get("total", 0)
        c = s.get("正确", 0)
        w = s.get("错误", 0)
        p = s.get("待测", 0)
        owners = "、".join(sorted(s.get("owners", set()))) or "—"
        completion = pct(c + w, t)
        print(f"  {fname:<24} {owners:<12} {t:<5} {GREEN}{c:<6}{RESET} "
              f"{RED}{w:<6}{RESET} {p:<6} {completion}")

    # ── 缺陷统计 ──────────────────────────────────────────
    total_bugs = sum(defects.values())
    print(f"\n{BOLD}▌ 缺陷统计{RESET}（共 {RED}{total_bugs}{RESET} 条）")
    for sev in ("高", "中", "低"):
        cnt = defects.get(sev, 0)
        color = RED if sev == "高" else (YELLOW if sev == "中" else "")
        print(f"  {color}{sev}：{cnt}{RESET}")

    print(f"\n{'─'*60}\n")


if __name__ == "__main__":
    print_dashboard()
