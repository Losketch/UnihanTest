#!/usr/bin/env python3
# -*- coding: utf-8 -*-

import argparse
import os
from csv import reader
from typing import List, Tuple, Set, Dict, Union, Optional

class UnicodeProcessor:
    def __init__(self, blocks_path: str, exclude_blocks: Optional[Set[str]] = None):
        self.blocks = self._load_blocks(blocks_path, exclude_blocks or set())

    def _load_blocks(self, blocks_path: str, exclude_names: Set[str]) -> List[Tuple[int, int, str]]:
        """加载 Unicode 区块信息"""
        blocks = []
        if not os.path.exists(blocks_path):
            print(f"Warning: {blocks_path} not found")
            return blocks

        with open(blocks_path, 'r', encoding='utf-8') as f:
            for line in f:
                line = line.split('#')[0].strip()
                if not line:
                    continue

                try:
                    range_part, name_part = line.split(';', 1)
                    name = name_part.strip()
                    if name in exclude_names:
                        continue

                    range_str = range_part.strip()
                    if '..' in range_str:
                        start_str, end_str = range_str.split('..', 1)
                        start = int(start_str, 16)
                        end = int(end_str, 16)
                    else:
                        start = end = int(range_str, 16)
                    blocks.append((start, end, name))
                except ValueError:
                    continue

        return sorted(blocks, key=lambda x: x[0])

    def get_block_for_codepoint(self, cp: int) -> Optional[Tuple[int, int, str]]:
        """获取码点所属的区块"""
        for start, end, name in self.blocks:
            if start <= cp <= end:
                return (start, end, name)
        return None

    def parse_unicode_data(self, ud_path: str) -> List[int]:
        """解析 UnicodeData.txt，提取有效码点"""
        filter_categories = {'Cc', 'Cf', 'Co', 'Zs', 'Zl', 'Zp', 'Mn', 'Cs'}
        cps = []
        start_cp = None

        with open(ud_path, 'r', encoding='utf-8') as f:
            rdr = reader(f, delimiter=';')
            for fields in rdr:
                if len(fields) < 3:
                    continue
                cp_hex, name, category = fields[0], fields[1], fields[2]
                if category in filter_categories:
                    continue
                cp = int(cp_hex, 16)

                if name.startswith('<') and name.endswith('First>'):
                    start_cp = cp
                elif name.startswith('<') and name.endswith('Last>') and start_cp is not None:
                    cps.extend(range(start_cp, cp + 1))
                    start_cp = None
                else:
                    cps.append(cp)

        return sorted(set(cps))

    def parse_han_scripts(self, scripts_path: str, script_extensions_path: str) -> List[int]:
        """解析 Scripts.txt 和 ScriptExtensions.txt，提取汉字码点"""
        ranges = []

        # Scripts.txt
        if os.path.exists(scripts_path):
            ranges.extend(self._parse_script_file(scripts_path, 'Han'))

        # ScriptExtensions.txt
        if os.path.exists(script_extensions_path):
            ranges.extend(self._parse_script_extensions_file(script_extensions_path, 'Hani'))

        merged_ranges = self._merge_ranges(ranges)
        cps = []
        for start, end in merged_ranges:
            cps.extend(range(start, end + 1))

        return sorted(set(cps))

    def _parse_script_file(self, file_path: str, target_script: str) -> List[Tuple[int, int]]:
        """解析脚本文件"""
        ranges = []
        with open(file_path, 'r', encoding='utf-8') as f:
            for line in f:
                line = line.split('#')[0].strip()
                if not line:
                    continue

                try:
                    range_part, script_part = line.split(';', 1)
                    script = script_part.strip()

                    if script == target_script:
                        range_str = range_part.strip()
                        ranges.append(self._parse_code_range(range_str))
                except ValueError:
                    continue
        return ranges

    def _parse_script_extensions_file(self, file_path: str, target_script: str) -> List[Tuple[int, int]]:
        """解析脚本扩展文件"""
        ranges = []
        with open(file_path, 'r', encoding='utf-8') as f:
            for line in f:
                line = line.split('#')[0].strip()
                if not line:
                    continue

                try:
                    range_part, scripts_part = line.split(';', 1)
                    scripts = scripts_part.strip().split()

                    if target_script in scripts:
                        range_str = range_part.strip()
                        ranges.append(self._parse_code_range(range_str))
                except ValueError:
                    continue
        return ranges

    def _parse_code_range(self, range_str: str) -> Tuple[int, int]:
        """解析码点范围字符串"""
        if '..' in range_str:
            start_str, end_str = range_str.split('..')
            return (int(start_str, 16), int(end_str, 16))
        else:
            code = int(range_str, 16)
            return (code, code)

    def _merge_ranges(self, ranges: List[Tuple[int, int]]) -> List[Tuple[int, int]]:
        """合并相邻或重叠的范围"""
        if not ranges:
            return []

        sorted_ranges = sorted(ranges)
        merged = [sorted_ranges[0]]

        for current_start, current_end in sorted_ranges[1:]:
            last_start, last_end = merged[-1]

            if current_start <= last_end + 1:
                merged[-1] = (last_start, max(last_end, current_end))
            else:
                merged.append((current_start, current_end))

        return merged

    def write_codepoints_with_blocks(self, codepoints: List[int], output_path: str):
        """写入带区块注释的码点列表"""
        with open(output_path, 'w', encoding='utf-8') as fout:
            last_block_name = None

            for cp in codepoints:
                block_info = self.get_block_for_codepoint(cp)
                if not block_info:
                    continue

                start, end, name = block_info
                if name != last_block_name:
                    fout.write(f"#BLOCK:{start:04X}; {name}\n")
                    last_block_name = name

                fout.write(f"U+{cp:04X}\n")

def main():
    parser = argparse.ArgumentParser(
        description='生成带区块注释的 Unicode 码点列表'
    )
    parser.add_argument('-b', '--blocks', required=True, help='Blocks.txt 文件路径')
    parser.add_argument('-o', '--output', required=True, help='输出文件路径')

    mode_group = parser.add_mutually_exclusive_group(required=True)
    mode_group.add_argument('-u', '--unicode-data', help='UnicodeData.txt 文件路径（生成所有有效字符）')
    mode_group.add_argument('--han-scripts', nargs=2, metavar=('SCRIPTS', 'SCRIPT_EXTENSIONS'), 
                           help='Scripts.txt 和 ScriptExtensions.txt 文件路径（生成汉字）')

    parser.add_argument('--exclude-blocks', nargs='*', default=[
        'High Surrogates', 'High Private Use Surrogates', 'Low Surrogates', 'Private Use Area'
    ], help='要排除的区块名称')

    args = parser.parse_args()

    output_dir = os.path.dirname(args.output)
    if output_dir and not os.path.exists(output_dir):
        print(f"创建输出目录: {output_dir}")
        os.makedirs(output_dir, exist_ok=True)

    exclude_blocks = set(args.exclude_blocks) if args.exclude_blocks else set()
    processor = UnicodeProcessor(args.blocks, exclude_blocks)

    if args.unicode_data:
        print("解析 UnicodeData.txt...")
        codepoints = processor.parse_unicode_data(args.unicode_data)
        print(f"找到 {len(codepoints)} 个有效码点")
    else:
        scripts_path, script_extensions_path = args.han_scripts
        print("解析汉字脚本文件...")
        codepoints = processor.parse_han_scripts(scripts_path, script_extensions_path)
        print(f"找到 {len(codepoints)} 个汉字码点")

    print(f"写入到 {args.output}...")
    processor.write_codepoints_with_blocks(codepoints, args.output)
    print("完成！")

if __name__ == '__main__':
    main()