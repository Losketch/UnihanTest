import os

# 获取当前目录
current_path = os.getcwd()

# 创建文件路径
file_path = os.path.join(current_path, "Unihan.txt")

# 打开文件
with open(file_path, "w", encoding="utf-8") as f:
    # 写入注释
    f.write("#中日韩部首补充\n")
    
    # 循环写入U+2E80到U+2E99的所有Unicode字符
    for i in range(0x2E80, 0x2E99 + 1):
        f.write("U+{:04X}\n".format(i))
    # 循环写入U+2E9B到U+2EF3的所有Unicode字符
    for i in range(0x2E9B, 0x2EF3 + 1):
        f.write("U+{:04X}\n".format(i))
    
    # 写入注释
    f.write("#康熙部首\n")
    
    # 循环写入U+2F00到U+2FD5的所有Unicode字符
    for i in range(0x2F00, 0x2FD5 + 1):
        f.write("U+{:04X}\n".format(i))
    # 写入注释
    f.write("#基本区\n")
    
    # 循环写入U+4E00到U+9FFF的所有Unicode字符
    for i in range(0x4E00, 0x9FFF + 1):
        f.write("U+{:04X}\n".format(i))
    # 写入注释
    f.write("#扩展A\n")
    
    # 循环写入U+3400到U+4DBF的所有Unicode字符
    for i in range(0x3400, 0x4DBF + 1):
        f.write("U+{:04X}\n".format(i))
    # 写入注释
    f.write("#扩展B\n")
    
    # 循环写入U+20000到U+2A6DF的所有Unicode字符
    for i in range(0x20000, 0x2A6DF + 1):
        f.write("U+{:04X}\n".format(i))
    # 写入注释
    f.write("#扩展C\n")
    
    # 循环写入U+2A700到U+2B739的所有Unicode字符
    for i in range(0x2A700, 0x2B739 + 1):
        f.write("U+{:04X}\n".format(i))
    # 写入注释
    f.write("#扩展D\n")
    
    # 循环写入U+2B740到U+2B81D的所有Unicode字符
    for i in range(0x2B740, 0x2B81D + 1):
        f.write("U+{:04X}\n".format(i))
    # 写入注释
    f.write("#扩展E\n")
    
    # 循环写入U+2B820到U+2CEA1的所有Unicode字符
    for i in range(0x2B820, 0x2CEA1 + 1):
        f.write("U+{:04X}\n".format(i))
    # 写入注释
    f.write("#扩展F\n")
    
    # 循环写入U+2CEB0到U+2EBE0的所有Unicode字符
    for i in range(0x2CEB0, 0x2EBE0 + 1):
        f.write("U+{:04X}\n".format(i))
    # 写入注释
    f.write("#扩展G\n")
    
    # 循环写入U+30000到U+3134A的所有Unicode字符
    for i in range(0x30000, 0x3134A + 1):
        f.write("U+{:04X}\n".format(i))
    # 写入注释
    f.write("#扩展H\n")
    
    # 循环写入U+31350到U+323AF的所有Unicode字符
    for i in range(0x31350, 0x323AF + 1):
        f.write("U+{:04X}\n".format(i))
    # 写入注释
    f.write("#扩展I\n")
    
    # 循环写入U+2EBF0到U+2EE5D的所有Unicode字符
    for i in range(0x2EBF0, 0x2EE5D + 1):
        f.write("U+{:04X}\n".format(i))
    # 写入注释
    f.write("#兼容区\n")
    
    # 循环写入U+F900到U+FA6D的所有Unicode字符
    for i in range(0xF900, 0xFA6D + 1):
        f.write("U+{:04X}\n".format(i))
    # 循环写入U+FA70到U+FAD9的所有Unicode字符
    for i in range(0xFA70, 0xFAD9 + 1):
        f.write("U+{:04X}\n".format(i))
    
    # 写入注释
    f.write("#兼容补充区\n")
    
    # 循环写入U+2F800到U+2FA1D的所有Unicode字符
    for i in range(0x2F800, 0x2FA1D + 1):
        f.write("U+{:04X}\n".format(i))
