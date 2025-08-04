import os
import re

# Danh sách các thư mục cần xử lý
folder_paths = [
    r"D:\Logistic\new\logistic\src\main\resources\data\Wang_Chen\solution",
]

for folder_path in folder_paths:
    print(f"📂 Đang xử lý thư mục: {folder_path}")

    # Duyệt qua tất cả thư mục con và file trong từng folder_path
    for root, dirs, files in os.walk(folder_path):
        for filename in files:
            file_path = os.path.join(root, filename)
            
            # Chỉ xử lý file .txt
            if filename.lower().endswith(".txt"):
                try:
                    with open(file_path, "r", encoding="utf-8") as file:
                        content = file.read()
                    
                    # Loại bỏ số 0 đứng một mình
                    new_content = re.sub(r'\b0\b', '', content)
                    
                    # Ghi đè lại file
                    with open(file_path, "w", encoding="utf-8") as file:
                        file.write(new_content)
                    
                    print(f"✅ Đã xử lý: {file_path}")
                
                except Exception as e:
                    print(f"⚠️ Lỗi khi xử lý {file_path}: {e}")

print("🎯 Hoàn tất! Đã loại bỏ số 0 đứng một mình trong tất cả file .txt")
