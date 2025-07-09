#!/usr/bin/env python3
"""
Script xử lý các file epoch:
- Giữ lại chỉ các file epoch3
- Đổi tên file epoch3 thành tên không có "rl_epoch" và "epochx"
- Xóa tất cả các file epoch khác

Cách sử dụng:
    python process_epochs.py          # Chạy với xác nhận
    python process_epochs.py --auto   # Chạy tự động không xác nhận
"""

import os
import re
import sys
import shutil
from pathlib import Path

def process_epoch_files(directory_path, auto_mode=False):
    """
    Xử lý các file epoch trong thư mục được chỉ định
    
    Args:
        directory_path (str): Đường dẫn đến thư mục chứa các file epoch
        auto_mode (bool): Nếu True, chạy tự động không cần xác nhận
    """
    directory = Path(directory_path)
    
    if not directory.exists():
        print(f"Thư mục không tồn tại: {directory_path}")
        return False
    
    # Pattern để tìm các file epoch
    epoch_pattern = re.compile(r'rl_epoch_(.+)_epoch(\d+)\.txt$')
    
    # Danh sách các file cần xử lý
    files_to_rename = []
    files_to_delete = []
    
    # Quét tất cả file trong thư mục
    for file_path in directory.glob('*.txt'):
        match = epoch_pattern.match(file_path.name)
        if match:
            dataset_name = match.group(1)  # Ví dụ: lc101, lr102, etc.
            epoch_number = int(match.group(2))  # Số epoch
            
            if epoch_number == 3:
                # File epoch3 sẽ được đổi tên
                new_name = f"{dataset_name}.txt"
                files_to_rename.append((file_path, new_name))
            else:
                # Các file epoch khác sẽ bị xóa
                files_to_delete.append(file_path)
    
    # Hiển thị thông tin
    print(f"Tìm thấy {len(files_to_rename)} file epoch3 để đổi tên")
    print(f"Tìm thấy {len(files_to_delete)} file epoch khác để xóa")
    
    if not files_to_rename and not files_to_delete:
        print("Không tìm thấy file epoch nào để xử lý")
        return False
    
    # Hiển thị ví dụ
    if files_to_rename:
        print("\nVí dụ các file sẽ được đổi tên:")
        for old_path, new_name in files_to_rename[:5]:
            print(f"  {old_path.name} -> {new_name}")
        if len(files_to_rename) > 5:
            print(f"  ... và {len(files_to_rename) - 5} file khác")
    
    # Xác nhận từ người dùng (nếu không phải auto mode)
    if not auto_mode:
        response = input("\nBạn có muốn tiếp tục? (y/N): ").strip().lower()
        if response not in ['y', 'yes']:
            print("Hủy bỏ thao tác")
            return False
    else:
        print("\nChế độ tự động - Bắt đầu xử lý...")
    
    # Đổi tên các file epoch3
    renamed_count = 0
    if files_to_rename:
        print("\n--- ĐỔING TÊN FILE EPOCH3 ---")
        for old_path, new_name in files_to_rename:
            new_path = directory / new_name
            
            try:
                # Nếu file đích đã tồn tại, xóa nó trước
                if new_path.exists():
                    if not auto_mode:
                        print(f"  Cảnh báo: File {new_name} đã tồn tại, sẽ ghi đè")
                    new_path.unlink()
                
                shutil.move(str(old_path), str(new_path))
                print(f"✓ {old_path.name} -> {new_name}")
                renamed_count += 1
            except Exception as e:
                print(f"✗ Lỗi khi đổi tên {old_path.name}: {e}")
    
    # Xóa các file epoch khác
    deleted_count = 0
    if files_to_delete:
        print("\n--- XÓA FILE EPOCH KHÁC ---")
        for file_path in files_to_delete:
            try:
                file_path.unlink()
                print(f"✓ Đã xóa: {file_path.name}")
                deleted_count += 1
            except Exception as e:
                print(f"✗ Lỗi khi xóa {file_path.name}: {e}")
    
    print(f"\n=== KẾT QUẢ ===")
    print(f"✓ Đã đổi tên: {renamed_count} file")
    print(f"✓ Đã xóa: {deleted_count} file")
    print("Hoàn thành!")
    
    return True

def main():
    """Hàm main"""
    # Kiểm tra tham số dòng lệnh
    auto_mode = '--auto' in sys.argv or '-a' in sys.argv
    
    # Thư mục chứa các file epoch
    target_dir = "rl_10_30/pdptw/solution-use"
    
    print("=== Script xử lý file epoch ===")
    print(f"Thư mục đích: {target_dir}")
    print(f"Chế độ: {'Tự động' if auto_mode else 'Có xác nhận'}")
    
    # Kiểm tra thư mục tồn tại
    if not os.path.exists(target_dir):
        print(f"Lỗi: Thư mục {target_dir} không tồn tại!")
        print("Hãy chạy script từ thư mục gốc của project")
        return
    
    # Hiển thị thống kê file
    directory = Path(target_dir)
    all_epoch_files = list(directory.glob('rl_epoch_*.txt'))
    
    if not all_epoch_files:
        print("Không tìm thấy file epoch nào!")
        return
    
    print(f"Tổng số file epoch: {len(all_epoch_files)}")
    
    # Xử lý file
    success = process_epoch_files(target_dir, auto_mode)
    
    if success:
        print("\nScript đã chạy thành công!")
    else:
        print("\nScript đã dừng hoặc gặp lỗi!")

if __name__ == "__main__":
    main()