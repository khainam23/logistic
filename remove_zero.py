#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Xóa số 0 đứng một mình trong các file solution
Sử dụng: python remove_zero.py
"""

import os
import glob

def remove_standalone_zeros(input_file, output_file=None):
    """
    Xóa số 0 đứng một mình trong file solution
    
    Args:
        input_file (str): Đường dẫn file input
        output_file (str): Đường dẫn file output (None = ghi đè file gốc)
    
    Returns:
        bool: True nếu thành công
    """
    
    if not os.path.exists(input_file):
        print(f"❌ File không tồn tại: {input_file}")
        return False
    
    try:
        # Đọc file gốc
        with open(input_file, 'r', encoding='utf-8') as f:
            content = f.read()
        
        # Xử lý từng dòng
        lines = content.split('\n')
        cleaned_lines = []
        
        for line in lines:
            # Xử lý từng dòng
            line = line.strip()
            if line:
                # Tách các số trong dòng
                parts = line.split()
                if len(parts) > 1 and ':' in parts[0]:  # Dòng route
                    route_part = ' '.join(parts[1:])  # Phần sau dấu ":"
                    numbers = route_part.split()
                    
                    # Loại bỏ số 0 đứng một mình
                    filtered_numbers = [num for num in numbers if num != '0']
                    
                    if filtered_numbers:  # Nếu còn số sau khi lọc
                        new_route = parts[0] + ' ' + ' '.join(filtered_numbers)
                        cleaned_lines.append(new_route)
                else:
                    # Không phải dòng route, giữ nguyên
                    cleaned_lines.append(line)
        
        # Tạo nội dung mới
        new_content = '\n'.join(cleaned_lines)
        
        # Xác định file output
        if output_file is None:
            output_file = input_file
        
        # Ghi file
        with open(output_file, 'w', encoding='utf-8') as f:
            f.write(new_content)
        
        print(f"✅ Đã xử lý: {os.path.basename(input_file)}")
        return True
        
    except Exception as e:
        print(f"❌ Lỗi xử lý file {input_file}: {str(e)}")
        return False

def process_directory_recursive(directory_path):
    """
    Xử lý tất cả file .txt trong thư mục và thư mục con
    
    Args:
        directory_path (str): Đường dẫn thư mục
    
    Returns:
        int: Số file đã xử lý thành công
    """
    
    if not os.path.exists(directory_path):
        print(f"❌ Thư mục không tồn tại: {directory_path}")
        return 0
    
    success_count = 0
    total_files = 0
    
    print(f"🔍 Đang quét thư mục: {directory_path}")
    
    # Duyệt tất cả file .txt trong thư mục và thư mục con
    for root, dirs, files in os.walk(directory_path):
        txt_files = [f for f in files if f.endswith('.txt')]
        
        if txt_files:
            print(f"\n📂 Thư mục: {root}")
            print(f"   Tìm thấy {len(txt_files)} file .txt")
            
            for filename in txt_files:
                file_path = os.path.join(root, filename)
                total_files += 1
                
                print(f"   🔄 Đang xử lý: {filename}")
                if remove_standalone_zeros(file_path):
                    success_count += 1
    
    print(f"\n✅ Tổng kết: Đã xử lý thành công {success_count}/{total_files} file")
    return success_count

def main():
    """Hàm main - Xử lý thư mục data"""
    
    print("🛠️  CÔNG CỤ XÓA SỐ 0 ĐỨNG MỘT MÌNH")
    print("="*50)
    
    # Đường dẫn thư mục data
    data_dir = "d:/Logistic/excute_data/logistic/data"
    
    if not os.path.exists(data_dir):
        print(f"❌ Thư mục data không tồn tại: {data_dir}")
        return
    
    print(f"🚀 Bắt đầu xử lý thư mục data: {data_dir}")
    print("-" * 50)
    
    # Xử lý toàn bộ thư mục data
    success_count = process_directory_recursive(data_dir)
    
    print("\n" + "="*50)
    print("🎉 HOÀN THÀNH!")
    print(f"📊 Đã xử lý thành công {success_count} file")
    print("="*50)

if __name__ == "__main__":
    main()