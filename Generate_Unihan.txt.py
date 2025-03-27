import os
from dataclasses import dataclass
from typing import List, Tuple, Union

@dataclass
class UnicodeRange:
    name: str
    codes: List[Union[int, Tuple[int, int]]]

def write_unicode_ranges(filename: str, ranges: List[UnicodeRange], add_comments: bool) -> int:
    """Write Unicode ranges to a file with proper formatting and comments."""
    count = 0
    with open(filename, "w", encoding="utf-8") as f:
        for range_group in ranges:
            if add_comments and range_group.name:
                f.write(f"#{range_group.name}\n")
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

# Define Unicode ranges with meaningful names
UNICODE_RANGES = [
    UnicodeRange("康熙部首", [(0x2F00, 0x2FD5)]),
    UnicodeRange("表意文字描述字符", [(0x2FF0, 0x2FFF), 0x31EF]),
    UnicodeRange("中日韩部首补充", [(0x2E80, 0x2E99), (0x2E9B, 0x2EF3)]),
    UnicodeRange("〇", [0x3007]),
    UnicodeRange("中日韩汉字笔画", [(0x31C0, 0x31E5)]),
    UnicodeRange("基本区", [(0x4E00, 0x9FFF)]),
    UnicodeRange("扩展A", [(0x3400, 0x4DBF)]),
    UnicodeRange("兼容区", [(0xF900, 0xFA6D), (0xFA70, 0xFAD9)]),

    UnicodeRange("扩展B", [(0x20000, 0x2A6DF)]),
    UnicodeRange("扩展C", [(0x2A700, 0x2B739)]),
    UnicodeRange("扩展D", [(0x2B740, 0x2B81D)]),
    UnicodeRange("扩展E", [(0x2B820, 0x2CEA1)]),
    UnicodeRange("扩展F", [(0x2CEB0, 0x2EBE0)]),
    UnicodeRange("兼容补充区", [(0x2F800, 0x2FA1D)]),

    UnicodeRange("扩展G", [(0x30000, 0x3134A)]),
    UnicodeRange("扩展H", [(0x31350, 0x323AF)]),
    UnicodeRange("扩展I", [(0x2EBF0, 0x2EE5D)]),
    UnicodeRange("扩展J", [(0x323B0, 0x33479)]),
]

def main():
    add_comments = False  # Whether to add comment names (True/False)
    file_path = os.path.join(os.getcwd(), "Unihan.txt")
    count = write_unicode_ranges(file_path, UNICODE_RANGES, add_comments)
    print(f"Total {count} rows of UniHan characters")

if __name__ == "__main__":
    main()
