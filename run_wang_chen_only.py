#!/usr/bin/env python3
# -*- coding: utf-8 -*-

import os
import time
from solve_sulution_nhh import read_data, assign_customers_to_vehicles, write_solution, analyze_solution_quality

def run_wang_chen_only():
    """Chạy chỉ Wang Chen dataset"""
    
    input_dir = 'data/vrpspdtw_Wang_Chen/src'
    output_dir = 'exports/vrpspdtw_Wang_Chen/solution'
    
    # Tạo thư mục output nếu chưa có
    if not os.path.exists(output_dir):
        os.makedirs(output_dir)
    
    print("=" * 50)
    print("XỬ LÝ BÀI TOÁN: VRPSPDTW Wang Chen")
    print(f"📂 Input: {os.path.abspath(input_dir)}")
    print(f"📁 Export: {os.path.abspath(output_dir)}")
    print("=" * 50)
    
    # Lấy danh sách file
    files = [f for f in os.listdir(input_dir) if f.endswith('.txt')]
    files.sort()
    
    print(f"📂 Tìm thấy {len(files)} file dữ liệu:")
    print(f"   - {len(files)} file .txt")
    
    success_count = 0
    error_count = 0
    
    # Chỉ test với một vài file đầu tiên
    test_files = files[:5]  # Chỉ test 5 file đầu
    
    for i, filename in enumerate(test_files, 1):
        filepath = os.path.join(input_dir, filename)
        output_file = os.path.join(output_dir, filename)
        
        print(f"\n[{i}/{len(test_files)}] 🔄 Đang xử lý {filename}...")
        
        try:
            start_time = time.time()
            
            # Đọc dữ liệu
            customers, vehicles = read_data(filepath)
            
            if not customers or not vehicles:
                print(f"  ❌ Không thể đọc dữ liệu từ {filename}")
                error_count += 1
                continue
            
            print(f"  📁 File: {filename}")
            print(f"  👥 Khách hàng: {len(customers)}")
            print(f"  🚛 Xe có sẵn: {len(vehicles)}")
            print(f"  📍 Depot: ({vehicles[0].current_location.x}, {vehicles[0].current_location.y})")
            print(f"  💰 Dung lượng xe: {vehicles[0].capacity}")
            print(f"  ⏰ Thời gian xử lý: ", end="", flush=True)
            
            # Chạy thuật toán
            vehicles_result = assign_customers_to_vehicles(customers, vehicles)
            
            # Lưu kết quả
            write_solution(output_file, vehicles_result)
            
            # Phân tích chất lượng
            analyze_solution_quality(customers, vehicles_result)
            
            processing_time = time.time() - start_time
            print(f"⏱️ Thời gian xử lý: {processing_time:.2f}s")
            
            # Tính toán kết quả
            assigned_customers = sum(1 for v in vehicles_result for _ in v.route[1:-1])
            used_vehicles = sum(1 for v in vehicles_result if len(v.route) > 2)
            
            print(f"  ✅ Kết quả tổng quan:")
            print(f"     - Assigned rate: {assigned_customers/len(customers)*100:.1f}%")
            print(f"     - Vehicle utilization: {used_vehicles/len(vehicles)*100:.1f}%")
            print(f"     - File kết quả: {filename}")
            
            success_count += 1
            
        except Exception as e:
            print(f"  ❌ Lỗi xử lý {filename}: {e}")
            error_count += 1
    
    print("\n" + "=" * 50)
    print("🎯 TỔNG KẾT")
    print(f"✅ Thành công: {success_count} file")
    print(f"❌ Lỗi: {error_count} file")
    print(f"📁 Kết quả lưu tại: {os.path.abspath(output_dir)}")
    print("=" * 50)

if __name__ == "__main__":
    run_wang_chen_only()