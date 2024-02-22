import os

# 获取当前目录
current_path = os.getcwd()

# 创建文件路径
file_path = os.path.join(current_path, "Unihan.txt")

# 打开文件
with open(file_path, "w", encoding="utf-8") as f:
    #f.write("#康熙部首\n")
    for i in range(0x2F00, 0x2FD5 + 1):
        f.write("U+{:04X}\n".format(i))
    
    #f.write("#表意文字描述字符\n")
    for i in range(0x2FF0, 0x2FFF + 1):
        f.write("U+{:04X}\n".format(i))
    f.write("U+31EF\n")
    
    #f.write("#中日韩部首补充\n")
    for i in range(0x2E80, 0x2E99 + 1):
        f.write("U+{:04X}\n".format(i))
    for i in range(0x2E9B, 0x2EF3 + 1):
        f.write("U+{:04X}\n".format(i))
    
    #f.write("#〇\n")
    f.write("U+3007\n")
    
    #f.write("#中日韩汉字笔画\n")
    for i in range(0x31C0, 0x31E5 + 1):
        f.write("U+{:04X}\n".format(i))
    
    #f.write("#基本区\n")
    for i in range(0x4E00, 0x9FFF + 1):
        f.write("U+{:04X}\n".format(i))
    
    #f.write("#扩展A\n")
    for i in range(0x3400, 0x4DBF + 1):
        f.write("U+{:04X}\n".format(i))
    
    #f.write("#兼容区\n")
    for i in range(0xF900, 0xFA6D + 1):
        f.write("U+{:04X}\n".format(i))
    for i in range(0xFA70, 0xFAD9 + 1):
        f.write("U+{:04X}\n".format(i))
    
    #f.write("#扩展B\n")
    for i in range(0x20000, 0x2A6DF + 1):
        f.write("U+{:04X}\n".format(i))
    
    #f.write("#扩展C\n")
    for i in range(0x2A700, 0x2B739 + 1):
        f.write("U+{:04X}\n".format(i))
    
    #f.write("#扩展D\n")
    for i in range(0x2B740, 0x2B81D + 1):
        f.write("U+{:04X}\n".format(i))
    
    #f.write("#扩展E\n")
    for i in range(0x2B820, 0x2CEA1 + 1):
        f.write("U+{:04X}\n".format(i))
    
    #f.write("#扩展F\n")
    for i in range(0x2CEB0, 0x2EBE0 + 1):
        f.write("U+{:04X}\n".format(i))
    
    #f.write("#兼容补充区\n")
    for i in range(0x2F800, 0x2FA1D + 1):
        f.write("U+{:04X}\n".format(i))
    
    #f.write("#扩展G\n")
    for i in range(0x30000, 0x3134A + 1):
        f.write("U+{:04X}\n".format(i))
    
    #f.write("#扩展H\n")
    for i in range(0x31350, 0x323AF + 1):
        f.write("U+{:04X}\n".format(i))
    
    #f.write("#扩展I\n")
    for i in range(0x2EBF0, 0x2EE5D + 1):
        f.write("U+{:04X}\n".format(i))
    
