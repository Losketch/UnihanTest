#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
结合 UnicodeData.txt 和 Blocks.txt
- 去除“0000..007F; Basic Latin”区块（及其他指定过滤区块）
- 在每个区块开头插入 "#BLOCK:区块详细"
- 展开范围，输出 U+XXXX 格式的 Unicode 码点列表
"""

import argparse
from csv import reader

def load_blocks(blocks_path, exclude_names=None):
    """
    读取 Blocks.txt，返回一个排序的 (start, end, name) 列表，
    过滤掉 name 在 exclude_names 的区块。
    """
    if exclude_names is None:
        exclude_names = set()

    blocks = []
    with open(blocks_path, 'r', encoding='utf-8') as f:
        for line in f:
            line = line.strip()
            if not line or line.startswith('#'):
                continue
            parts = [p.strip() for p in line.split(';', 1)]
            if len(parts) != 2:
                continue
            rng, name = parts
            if name in exclude_names:
                continue
            if '..' in rng:
                start_str, end_str = rng.split('..', 1)
                start = int(start_str, 16)
                end = int(end_str, 16)
            else:
                start = end = int(rng, 16)
            blocks.append((start, end, name))
    blocks.sort(key=lambda x: x[0])
    return blocks

def parse_unicode_data(ud_path):
    """
    读取 UnicodeData.txt，过滤掉特定类别，展开范围，返回唯一排序的码点列表。
    """
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

def main():
    parser = argparse.ArgumentParser(
        description='结合 UnicodeData.txt 和 Blocks.txt 生成带区块注释的 Unicode 码点列表'
    )
    parser.add_argument('-u', '--unicode-data', required=True, help='UnicodeData.txt 文件路径')
    parser.add_argument('-b', '--blocks', required=True, help='Blocks.txt 文件路径')
    parser.add_argument('-o', '--output', required=True, help='输出文件路径（例如 Unihan.txt）')
    args = parser.parse_args()

    # 1. 载入区块，过滤掉指定区块
    exclude_blocks = {'High Surrogates','High Private Use Surrogates','Low Surrogates','Private Use Area'}
    blocks = load_blocks(args.blocks, exclude_names=exclude_blocks)

    # 2. 解析 UnicodeData，展开范围
    cps = parse_unicode_data(args.unicode_data)

    # 3. 写文件，按区块插入注释
    with open(args.output, 'w', encoding='utf-8') as fout:
        block_idx = 0
        n_blocks = len(blocks)
        last_block_name = None

        for cp in cps:
            # 移动到对应区块
            while block_idx < n_blocks and blocks[block_idx][1] < cp:
                block_idx += 1
            if block_idx >= n_blocks:
                # 超出所有区块范围
                break

            start, end, name = blocks[block_idx]
            # 只处理在当前区块范围内的码点
            if not (start <= cp <= end):
                continue

            # 当区块变更时，插入注释
            if name != last_block_name:
                start, end, name = blocks[block_idx]
                # fout.write(f"#BLOCK:{start:04X}..{end:04X}; {name}\n")
                fout.write(f"#BLOCK:{start:04X}; {name}\n")
                last_block_name = name

            fout.write(f"U+{cp:04X}\n")


if __name__ == '__main__':
    main()