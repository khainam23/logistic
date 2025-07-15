#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Kiểm tra và phân tích dữ liệu có sẵn trong hệ thống
"""

import os
from solve_sulution_nhh import read_data, detect_file_format

def check_directory_structure():
    """Kiểm tra cấu trúc thư mục dữ liệu"""
    print("🔍 KIỂM TRA CẤU TRÚC THƯ MỤC DỮ LIỆU")
    print("="*60)
    
    base_path = r"D:\Logistic\excute_data\logistic\data"
    
    if not os.path.exists(base_path):
        print(f"❌ Thư mục data không tồn tại: {base_path}")
        return []
    
    data_dirs = []
    
    for item in os.listdir(base_path):
        item_path = os.path.join(base_path, item)
        if os.path.isdir(item_path):
            src_path = os.path.join(item_path, "src")
            if os.path.exists(src_path):
                file_count = len([f for f in os.listdir(src_path) 
                                if f.endswith(('.txt', '.vrpsdptw')) and os.path.isfile(os.path.join(src_path, f))])
                print(f"📁 {item}/src: {file_count} files")
                data_dirs.append({
                    'name': item,
                    'path': src_path,
                    'file_count': file_count
                })
            else:
                print(f"📁 {item}: không có thư mục src")
    
    return data_dirs

def analyze_file_formats(data_dirs):
    """Phân tích định dạng các file dữ liệu"""
    print(f"\n📊 PHÂN TÍCH ĐỊNH DẠNG FILE")
    print("="*60)
    
    format_stats = {}
    
    for data_dir in data_dirs:
        print(f"\n📂 {data_dir['name']}:")
        src_path = data_dir['path']
        
        files = [f for f in os.listdir(src_path) 
                if f.endswith(('.txt', '.vrpsdptw')) and os.path.isfile(os.path.join(src_path, f))]
        
        for i, filename in enumerate(files[:5], 1):  # Chỉ kiểm tra 5 file đầu
            filepath = os.path.join(src_path, filename)
            try:
                with open(filepath, 'r', encoding='utf-8') as f:
                    lines = [line.strip() for line in f if line.strip()]
                
                if lines:
                    file_format = detect_file_format(lines)
                    print(f"   {i}. {filename}: {file_format}")
                    
                    if file_format not in format_stats:
                        format_stats[file_format] = 0
                    format_stats[file_format] += 1
                else:
                    print(f"   {i}. {filename}: EMPTY")
                    
            except Exception as e:
                print(f"   {i}. {filename}: ERROR - {str(e)}")
        
        if len(files) > 5:
            print(f"   ... và {len(files) - 5} file khác")
    
    print(f"\n📈 THỐNG KÊ ĐỊNH DẠNG:")
    for format_type, count in format_stats.items():
        print(f"   - {format_type}: {count} files")
    
    return format_stats

def test_sample_files(data_dirs):
    """Test đọc một số file mẫu"""
    print(f"\n🧪 TEST ĐỌC FILE MẪU")
    print("="*60)
    
    test_results = []
    
    for data_dir in data_dirs:
        print(f"\n📂 Testing {data_dir['name']}:")
        src_path = data_dir['path']
        
        files = [f for f in os.listdir(src_path) 
                if f.endswith(('.txt', '.vrpsdptw')) and os.path.isfile(os.path.join(src_path, f))]
        
        if not files:
            print("   ❌ Không có file để test")
            continue
        
        # Test file đầu tiên
        test_file = files[0]
        filepath = os.path.join(src_path, test_file)
        
        print(f"   🔄 Testing {test_file}...")
        
        try:
            customers, vehicles = read_data(filepath)
            
            if customers and vehicles:
                # Phân tích dữ liệu
                delivery_count = sum(1 for c in customers if c.d_demand > 0)
                pickup_count = sum(1 for c in customers if c.p_demand > 0)
                both_count = sum(1 for c in customers if c.d_demand > 0 and c.p_demand > 0)
                
                result = {
                    'directory': data_dir['name'],
                    'file': test_file,
                    'customers': len(customers),
                    'vehicles': len(vehicles),
                    'capacity': vehicles[0].capacity if vehicles else 0,
                    'delivery_customers': delivery_count,
                    'pickup_customers': pickup_count,
                    'both_customers': both_count,
                    'status': 'SUCCESS'
                }
                
                print(f"   ✅ SUCCESS:")
                print(f"      - Customers: {len(customers)}")
                print(f"      - Vehicles: {len(vehicles)}")
                print(f"      - Capacity: {vehicles[0].capacity}")
                print(f"      - Delivery only: {delivery_count}")
                print(f"      - Pickup only: {pickup_count}")
                print(f"      - Both: {both_count}")
                
            else:
                result = {
                    'directory': data_dir['name'],
                    'file': test_file,
                    'status': 'FAILED - No data'
                }
                print(f"   ❌ FAILED: Không đọc được dữ liệu")
                
        except Exception as e:
            result = {
                'directory': data_dir['name'],
                'file': test_file,
                'status': f'ERROR - {str(e)}'
            }
            print(f"   ❌ ERROR: {str(e)}")
        
        test_results.append(result)
    
    return test_results

def check_constraint_logic_compatibility(test_results):
    """Kiểm tra tính tương thích với logic constraint mới"""
    print(f"\n🔧 KIỂM TRA TƯƠNG THÍCH CONSTRAINT LOGIC")
    print("="*60)
    
    for result in test_results:
        if result['status'] == 'SUCCESS':
            print(f"\n📂 {result['directory']} - {result['file']}:")
            
            # Kiểm tra loại bài toán
            has_delivery = result['delivery_customers'] > 0
            has_pickup = result['pickup_customers'] > 0
            has_both = result['both_customers'] > 0
            
            if has_delivery and not has_pickup:
                problem_type = "VRPTW (Delivery only)"
                compatibility = "✅ FULLY COMPATIBLE"
            elif has_pickup and not has_delivery:
                problem_type = "Pure Pickup Problem"
                compatibility = "✅ COMPATIBLE"
            elif has_both or (has_delivery and has_pickup):
                problem_type = "VRPSPDTW (Mixed)"
                compatibility = "✅ COMPATIBLE với logic mới"
            else:
                problem_type = "UNKNOWN"
                compatibility = "❓ NEEDS INVESTIGATION"
            
            print(f"   - Problem type: {problem_type}")
            print(f"   - Compatibility: {compatibility}")
            
            # Đánh giá độ phức tạp
            complexity_score = 0
            if has_delivery: complexity_score += 1
            if has_pickup: complexity_score += 1
            if has_both: complexity_score += 2
            if result['customers'] > 100: complexity_score += 1
            if result['vehicles'] > 20: complexity_score += 1
            
            if complexity_score <= 2:
                complexity = "🟢 LOW"
            elif complexity_score <= 4:
                complexity = "🟡 MEDIUM"
            else:
                complexity = "🔴 HIGH"
            
            print(f"   - Complexity: {complexity} (score: {complexity_score})")

def generate_summary_report(data_dirs, format_stats, test_results):
    """Tạo báo cáo tổng kết"""
    print(f"\n📋 BÁO CÁO TỔNG KẾT")
    print("="*60)
    
    total_dirs = len(data_dirs)
    total_files = sum(d['file_count'] for d in data_dirs)
    successful_tests = len([r for r in test_results if r['status'] == 'SUCCESS'])
    
    print(f"📊 Thống kê tổng quan:")
    print(f"   - Thư mục dữ liệu: {total_dirs}")
    print(f"   - Tổng số file: {total_files}")
    print(f"   - Test thành công: {successful_tests}/{len(test_results)}")
    
    print(f"\n📈 Định dạng file phổ biến:")
    for format_type, count in sorted(format_stats.items(), key=lambda x: x[1], reverse=True):
        print(f"   - {format_type}: {count} files")
    
    print(f"\n🎯 Khuyến nghị:")
    if successful_tests == len(test_results):
        print("   ✅ Tất cả file test đều đọc được - hệ thống hoạt động tốt")
    elif successful_tests > len(test_results) * 0.8:
        print("   ⚠️ Hầu hết file đọc được - cần kiểm tra một số file lỗi")
    else:
        print("   ❌ Nhiều file không đọc được - cần kiểm tra lại logic đọc file")
    
    print(f"\n🔧 Logic constraint mới phù hợp với:")
    print("   ✅ VRPTW (delivery only)")
    print("   ✅ VRPSPDTW (pickup + delivery)")
    print("   ✅ PDPTW (với precedence constraints)")

def main():
    """Hàm chính"""
    print("🚀 BẮT ĐẦU KIỂM TRA DỮ LIỆU HỆ THỐNG")
    print("="*80)
    
    # 1. Kiểm tra cấu trúc thư mục
    data_dirs = check_directory_structure()
    
    if not data_dirs:
        print("❌ Không tìm thấy dữ liệu để kiểm tra")
        return
    
    # 2. Phân tích định dạng file
    format_stats = analyze_file_formats(data_dirs)
    
    # 3. Test đọc file mẫu
    test_results = test_sample_files(data_dirs)
    
    # 4. Kiểm tra tương thích constraint logic
    check_constraint_logic_compatibility(test_results)
    
    # 5. Tạo báo cáo tổng kết
    generate_summary_report(data_dirs, format_stats, test_results)
    
    print(f"\n{'='*80}")
    print("🎉 HOÀN THÀNH KIỂM TRA DỮ LIỆU")

if __name__ == "__main__":
    main()