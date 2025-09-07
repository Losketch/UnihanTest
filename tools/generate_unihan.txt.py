import os
import re
from dataclasses import dataclass
from typing import List, Tuple, Union, Dict

@dataclass
class UnicodeRange:
    name: str
    codes: List[Union[int, Tuple[int, int]]]

def parse_code_range(range_str: str) -> Union[int, Tuple[int, int]]:
    """Parse a Unicode code point range string like '4E00..9FFF' or '3007'."""
    if '..' in range_str:
        start_str, end_str = range_str.split('..')
        return (int(start_str, 16), int(end_str, 16))
    else:
        return int(range_str, 16)

def load_blocks(blocks_file: str) -> Dict[int, str]:
    """Load Unicode blocks and return a mapping from start code point to block name."""
    blocks = {}
    if not os.path.exists(blocks_file):
        print(f"Warning: {blocks_file} not found, using default block names")
        return blocks
    
    with open(blocks_file, 'r', encoding='utf-8') as f:
        for line in f:
            line = line.split('#')[0].strip()
            if not line:
                continue
            
            try:
                range_part, name_part = line.split(';', 1)
                range_str = range_part.strip()
                block_name = name_part.strip()
                
                if '..' in range_str:
                    start_str, _ = range_str.split('..')
                    start_code = int(start_str, 16)
                    blocks[start_code] = block_name
            except ValueError:
                continue
    
    return blocks

def get_block_name(code_point: int, blocks: Dict[int, str]) -> str:
    """Get the block name for a given code point."""
    # Find the largest start code that is <= code_point
    valid_starts = [start for start in blocks.keys() if start <= code_point]
    if valid_starts:
        start = max(valid_starts)
        return blocks[start]
    return "Unknown Block"

def load_han_ranges(scripts_file: str, script_extensions_file: str) -> List[Tuple[int, int]]:
    """Load Han character ranges from Scripts.txt and ScriptExtensions.txt."""
    ranges = []
    
    # Load from Scripts.txt (Script = Han)
    if os.path.exists(scripts_file):
        with open(scripts_file, 'r', encoding='utf-8') as f:
            for line in f:
                line = line.split('#')[0].strip()
                if not line:
                    continue
                
                try:
                    range_part, script_part = line.split(';', 1)
                    script = script_part.strip()
                    
                    if script == 'Han':
                        range_str = range_part.strip()
                        code_range = parse_code_range(range_str)
                        if isinstance(code_range, tuple):
                            ranges.append(code_range)
                        else:
                            ranges.append((code_range, code_range))
                except ValueError:
                    continue
    
    # Load from ScriptExtensions.txt (contains Hani)
    if os.path.exists(script_extensions_file):
        with open(script_extensions_file, 'r', encoding='utf-8') as f:
            for line in f:
                line = line.split('#')[0].strip()
                if not line:
                    continue
                
                try:
                    range_part, scripts_part = line.split(';', 1)
                    scripts = scripts_part.strip().split()
                    
                    if 'Hani' in scripts:
                        range_str = range_part.strip()
                        code_range = parse_code_range(range_str)
                        if isinstance(code_range, tuple):
                            ranges.append(code_range)
                        else:
                            ranges.append((code_range, code_range))
                except ValueError:
                    continue
    
    return ranges

def merge_adjacent_ranges(ranges: List[Tuple[int, int]]) -> List[Tuple[int, int]]:
    """Merge adjacent or overlapping ranges."""
    if not ranges:
        return []
    
    # Sort ranges by start point
    sorted_ranges = sorted(ranges)
    merged = [sorted_ranges[0]]
    
    for current_start, current_end in sorted_ranges[1:]:
        last_start, last_end = merged[-1]
        
        # If current range overlaps or is adjacent to the last range, merge them
        if current_start <= last_end + 1:
            merged[-1] = (last_start, max(last_end, current_end))
        else:
            merged.append((current_start, current_end))
    
    return merged

def group_ranges_by_block(ranges: List[Tuple[int, int]], blocks: Dict[int, str]) -> List[UnicodeRange]:
    """Group ranges by Unicode blocks."""
    unicode_ranges = []
    
    for start, end in ranges:
        current_start = start
        
        while current_start <= end:
            # Find the block for current_start
            block_name = get_block_name(current_start, blocks)
            
            # Find the end of this block or the end of our range
            next_block_starts = [b for b in blocks.keys() if b > current_start]
            if next_block_starts:
                next_block_start = min(next_block_starts)
                current_end = min(end, next_block_start - 1)
            else:
                current_end = end
            
            # Create a UnicodeRange for this block segment
            if current_end > current_start:
                codes = [(current_start, current_end)]
            else:
                codes = [current_start]
            
            # Check if we already have a range for this block
            existing_range = next((r for r in unicode_ranges if r.name == block_name), None)
            if existing_range:
                existing_range.codes.extend(codes)
            else:
                unicode_ranges.append(UnicodeRange(block_name, codes))
            
            current_start = current_end + 1
    
    return unicode_ranges

def write_unicode_ranges(filename: str, ranges: List[UnicodeRange]) -> int:
    """Write Unicode ranges to a file with proper formatting and comments."""
    count = 0
    with open(filename, "w", encoding="utf-8") as f:
        for range_group in ranges:
            f.write(f"#BLOCK:{range_group.name}\n")
            for code in range_group.codes:
                if isinstance(code, tuple):
                    start, end = code
                    for code_point in range(start, end + 1):
                        f.write(f"U+{code_point:04X}\n")
                        count += 1
                else:
                    f.write(f"U+{code:04X}\n")
                    count += 1
    return count

def main():
    # File paths - adjust these to your actual file locations
    scripts_file = "Scripts.txt"
    script_extensions_file = "ScriptExtensions.txt"
    blocks_file = "Blocks.txt"
    output_file = os.path.join(os.getcwd(), "Unihan.txt")
    
    # Load Unicode blocks for better naming
    blocks = load_blocks(blocks_file)
    
    # Load Han character ranges
    han_ranges = load_han_ranges(scripts_file, script_extensions_file)
    
    if not han_ranges:
        print("No Han character ranges found. Please check your Scripts.txt and ScriptExtensions.txt files.")
        return
    
    # Merge adjacent ranges
    merged_ranges = merge_adjacent_ranges(han_ranges)
    
    # Group by Unicode blocks
    unicode_ranges = group_ranges_by_block(merged_ranges, blocks)
    
    # Sort by the first code point in each range
    unicode_ranges.sort(key=lambda r: r.codes[0] if isinstance(r.codes[0], int) else r.codes[0][0])
    
    # Write to file
    count = write_unicode_ranges(output_file, unicode_ranges)
    print(f"Generated {len(unicode_ranges)} Unicode ranges")
    print(f"Total {count} rows of UniHan characters")
    
    # Print summary
    print("\nGenerated ranges:")
    for range_group in unicode_ranges:
        total_chars = sum(
            1 if isinstance(code, int) else (code[1] - code[0] + 1)
            for code in range_group.codes
        )
        print(f"  {range_group.name}: {total_chars} characters")

if __name__ == "__main__":
    main()