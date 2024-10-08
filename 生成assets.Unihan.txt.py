import os

def write_unicode_range(file, start, end):
    """写入指定 Unicode 范围的值"""
    for i in range(start, end + 1):
        file.write("U+{:04X}\n".format(i))

# 获取当前目录
current_path = os.getcwd()
# 创建文件路径
file_path = os.path.join(current_path, "Unihan.txt")

# 打开文件
with open(file_path, "w", encoding="utf-8") as f:
    #f.write("#康熙部首\n")
    write_unicode_range(f, 0x2F00, 0x2FD5)

    #f.write("#表意文字描述字符\n")
    write_unicode_range(f, 0x2FF0, 0x2FFF)
    f.write("U+31EF\n")

    #f.write("#中日韩部首补充\n")
    write_unicode_range(f, 0x2E80, 0x2E99)
    write_unicode_range(f, 0x2E9B, 0x2EF3)

    #f.write("#〇\n")
    f.write("U+3007\n")

    #f.write("#中日韩汉字笔画\n")
    write_unicode_range(f, 0x31C0, 0x31E5)

    #f.write("#基本区\n")
    write_unicode_range(f, 0x4E00, 0x9FFF)
    #f.write("#扩展A\n")
    write_unicode_range(f, 0x3400, 0x4DBF)

    #f.write("#兼容区\n")
    write_unicode_range(f, 0xF900, 0xFA6D)
    write_unicode_range(f, 0xFA70, 0xFAD9)

    #f.write("#扩展B\n")
    write_unicode_range(f, 0x20000, 0x2A6DF)
    #f.write("#扩展C\n")
    write_unicode_range(f, 0x2A700, 0x2B739)
    #f.write("#扩展D\n")
    write_unicode_range(f, 0x2B740, 0x2B81D)
    #f.write("#扩展E\n")
    write_unicode_range(f, 0x2B820, 0x2CEA1)
    #f.write("#扩展F\n")
    write_unicode_range(f, 0x2CEB0, 0x2EBE0)

    #f.write("#兼容补充区\n")
    write_unicode_range(f, 0x2F800, 0x2FA1D)

    #f.write("#扩展G\n")
    write_unicode_range(f, 0x30000, 0x3134A)
    #f.write("#扩展H\n")
    write_unicode_range(f, 0x31350, 0x323AF)

    #f.write("#扩展I\n")
    write_unicode_range(f, 0x2EBF0, 0x2EE5D)

    #f.write("#扩展J\n")
    write_unicode_range(f, 0x323B0, 0x3347B)
