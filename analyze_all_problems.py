#!/usr/bin/env python3
# -*- coding: utf-8 -*-

"""
Phân tích chi tiết tình trạng hiện tại của toàn bộ 4 bài toán VRP
Bao gồm: PDPTW, VRPTW, VRPSPDTW_Wang_Chen, VRPSPDTW_Liu_Tang_Yao
"""

import os
import glob
import re
import json
from datetime import datetime
from collections import defaultdict, Counter

def parse_solution_file(file_path):
    """Phân tích chi tiết một file solution"""
    try:
        with open(file_path, 'r', encoding='utf-8') as f:
            content = f.read().strip()
        
        if not content:
            return {
                'status': 'empty',
                'routes': 0,
                'customers_served': 0,
                'total_distance': 0,
                'vehicles_used': 0,
                'route_details': []
            }
        
        lines = content.split('\n')
        routes = []
        total_distance = 0
        customers_served = 0
        
        # Tìm thông tin distance nếu có
        distance_pattern = r'(?:Total\s+)?[Dd]istance[:\s]*([0-9]+\.?[0-9]*)'
        cost_pattern = r'(?:Total\s+)?[Cc]ost[:\s]*([0-9]+\.?[0-9]*)'
        
        for line in lines:
            if re.search(distance_pattern, line):
                match = re.search(distance_pattern, line)
                if match:
                    total_distance = float(match.group(1))
            elif re.search(cost_pattern, line):
                match = re.search(cost_pattern, line)
                if match:
                    total_distance = float(match.group(1))
        
        # Phân tích routes
        for line in lines:
            if line.strip() and ('Route' in line or 'route' in line):
                if ':' in line:
                    route_part = line.split(':')[1].strip()
                    nodes = route_part.split()
                    
                    # Loại bỏ depot (node 0) để đếm customers
                    customers_in_route = [n for n in nodes if n != '0' and n.isdigit()]
                    route_info = {
                        'nodes': nodes,
                        'customers': len(customers_in_route),
                        'customer_ids': customers_in_route
                    }
                    routes.append(route_info)
                    customers_served += len(customers_in_route)
        
        return {
            'status': 'success' if customers_served > 0 else 'no_customers',
            'routes': len(routes),
            'customers_served': customers_served,
            'total_distance': total_distance,
            'vehicles_used': len(routes),
            'route_details': routes
        }
        
    except Exception as e:
        return {
            'status': 'error',
            'error': str(e),
            'routes': 0,
            'customers_served': 0,
            'total_distance': 0,
            'vehicles_used': 0,
            'route_details': []
        }

def analyze_problem_results():
    """Phân tích chi tiết kết quả của tất cả bài toán"""
    
    problems = {
        'PDPTW': {
            'solution_dir': 'exports/pdptw/solution',
            'description': 'Pickup and Delivery Problem with Time Windows'
        },
        'VRPTW': {
            'solution_dir': 'exports/vrptw/solution',
            'description': 'Vehicle Routing Problem with Time Windows'
        },
        'VRPSPDTW_Wang_Chen': {
            'solution_dir': 'exports/vrpspdtw_Wang_Chen/solution',
            'description': 'VRP with Simultaneous Pickup-Delivery and Time Windows (Wang-Chen)'
        },
        'VRPSPDTW_Liu_Tang_Yao': {
            'solution_dir': 'exports/vrpspdtw_Liu_Tang_Yao/solution',
            'description': 'VRP with Simultaneous Pickup-Delivery and Time Windows (Liu-Tang-Yao)'
        }
    }
    
    print("=" * 100)
    print("PHÂN TÍCH CHI TIẾT TÌNH TRẠNG HIỆN TẠI CỦA TẤT CẢ BÀI TOÁN VRP")
    print("=" * 100)
    print(f"📅 Thời gian phân tích: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}")
    
    overall_stats = {
        'total_files': 0,
        'successful_files': 0,
        'empty_files': 0,
        'error_files': 0,
        'total_customers_served': 0,
        'total_routes': 0,
        'total_distance': 0
    }
    
    problem_details = {}
    
    for problem_name, problem_info in problems.items():
        solution_dir = problem_info['solution_dir']
        description = problem_info['description']
        
        print(f"\n{'='*60}")
        print(f"🔍 {problem_name}")
        print(f"📝 {description}")
        print(f"📁 Thư mục: {solution_dir}")
        print(f"{'='*60}")
        
        if not os.path.exists(solution_dir):
            print(f"   ❌ Thư mục không tồn tại")
            problem_details[problem_name] = {'status': 'directory_not_found'}
            continue
            
        # Lấy tất cả file solution
        solution_files = glob.glob(os.path.join(solution_dir, "*.txt"))
        print(f"📊 Tổng số file solution: {len(solution_files)}")
        
        if len(solution_files) == 0:
            print(f"   ⚠️ Không có file solution nào")
            problem_details[problem_name] = {'status': 'no_files'}
            continue
        
        # Phân tích từng file
        file_stats = {
            'successful': 0,
            'empty': 0,
            'error': 0,
            'no_customers': 0
        }
        
        route_stats = []
        customer_stats = []
        distance_stats = []
        file_details = []
        
        print(f"\n📋 PHÂN TÍCH CHI TIẾT CÁC FILE:")
        print("-" * 80)
        
        for file_path in solution_files:
            filename = os.path.basename(file_path)
            file_size = os.path.getsize(file_path)
            
            result = parse_solution_file(file_path)
            
            # Cập nhật thống kê
            if result['status'] == 'success':
                file_stats['successful'] += 1
                route_stats.append(result['routes'])
                customer_stats.append(result['customers_served'])
                if result['total_distance'] > 0:
                    distance_stats.append(result['total_distance'])
                    
                print(f"✅ {filename:<25} | {result['routes']:>2} routes | {result['customers_served']:>3} customers | Distance: {result['total_distance']:>8.1f} | Size: {file_size:>6} bytes")
                
            elif result['status'] == 'empty':
                file_stats['empty'] += 1
                print(f"⚪ {filename:<25} | EMPTY FILE | Size: {file_size:>6} bytes")
                
            elif result['status'] == 'no_customers':
                file_stats['no_customers'] += 1
                print(f"⚠️  {filename:<25} | NO CUSTOMERS SERVED | Size: {file_size:>6} bytes")
                
            else:  # error
                file_stats['error'] += 1
                print(f"❌ {filename:<25} | ERROR: {result.get('error', 'Unknown')} | Size: {file_size:>6} bytes")
            
            file_details.append({
                'filename': filename,
                'size': file_size,
                'result': result
            })
        
        # Tính toán thống kê tổng hợp
        print(f"\n📈 THỐNG KÊ TỔNG HỢP:")
        print("-" * 50)
        print(f"📁 Tổng số file:           {len(solution_files)}")
        print(f"✅ File thành công:        {file_stats['successful']} ({file_stats['successful']/len(solution_files)*100:.1f}%)")
        print(f"⚪ File trống:             {file_stats['empty']} ({file_stats['empty']/len(solution_files)*100:.1f}%)")
        print(f"⚠️  File không có khách:    {file_stats['no_customers']} ({file_stats['no_customers']/len(solution_files)*100:.1f}%)")
        print(f"❌ File lỗi:              {file_stats['error']} ({file_stats['error']/len(solution_files)*100:.1f}%)")
        
        if route_stats:
            print(f"\n🚛 THỐNG KÊ ROUTES:")
            print(f"   • Trung bình:          {sum(route_stats)/len(route_stats):.1f} routes/file")
            print(f"   • Min - Max:           {min(route_stats)} - {max(route_stats)} routes")
            print(f"   • Tổng routes:         {sum(route_stats)}")
            
        if customer_stats:
            print(f"\n👥 THỐNG KÊ KHÁCH HÀNG:")
            print(f"   • Trung bình:          {sum(customer_stats)/len(customer_stats):.1f} customers/file")
            print(f"   • Min - Max:           {min(customer_stats)} - {max(customer_stats)} customers")
            print(f"   • Tổng khách hàng:     {sum(customer_stats)}")
            
        if distance_stats:
            print(f"\n📏 THỐNG KÊ KHOẢNG CÁCH:")
            print(f"   • Trung bình:          {sum(distance_stats)/len(distance_stats):.1f}")
            print(f"   • Min - Max:           {min(distance_stats):.1f} - {max(distance_stats):.1f}")
            print(f"   • Tổng khoảng cách:    {sum(distance_stats):.1f}")
        
        # Lưu thông tin chi tiết
        problem_details[problem_name] = {
            'status': 'analyzed',
            'total_files': len(solution_files),
            'file_stats': file_stats,
            'route_stats': route_stats,
            'customer_stats': customer_stats,
            'distance_stats': distance_stats,
            'file_details': file_details[:10]  # Chỉ lưu 10 file đầu để tiết kiệm bộ nhớ
        }
        
        # Cập nhật thống kê tổng
        overall_stats['total_files'] += len(solution_files)
        overall_stats['successful_files'] += file_stats['successful']
        overall_stats['empty_files'] += file_stats['empty']
        overall_stats['error_files'] += file_stats['error']
        overall_stats['total_customers_served'] += sum(customer_stats) if customer_stats else 0
        overall_stats['total_routes'] += sum(route_stats) if route_stats else 0
        overall_stats['total_distance'] += sum(distance_stats) if distance_stats else 0
    
    # Hiển thị tổng kết cuối cùng
    print(f"\n" + "=" * 100)
    print(f"📊 TỔNG KẾT TOÀN BỘ DỰ ÁN")
    print("=" * 100)
    print(f"📁 Tổng số file solution:     {overall_stats['total_files']}")
    print(f"✅ File thành công:           {overall_stats['successful_files']} ({overall_stats['successful_files']/overall_stats['total_files']*100:.1f}%)")
    print(f"⚪ File trống:                {overall_stats['empty_files']} ({overall_stats['empty_files']/overall_stats['total_files']*100:.1f}%)")
    print(f"❌ File lỗi:                 {overall_stats['error_files']} ({overall_stats['error_files']/overall_stats['total_files']*100:.1f}%)")
    print(f"🚛 Tổng số routes:            {overall_stats['total_routes']}")
    print(f"👥 Tổng khách hàng phục vụ:   {overall_stats['total_customers_served']}")
    print(f"📏 Tổng khoảng cách:          {overall_stats['total_distance']:.1f}")
    print("=" * 100)
    
    return problem_details, overall_stats

def analyze_data_file(file_path):
    """Phân tích chi tiết một file dữ liệu đầu vào"""
    try:
        with open(file_path, 'r', encoding='utf-8') as f:
            lines = f.readlines()
        
        # Đếm số dòng và ước tính số customers
        total_lines = len(lines)
        non_empty_lines = len([line for line in lines if line.strip()])
        
        # Tìm thông tin về số customers (thường ở dòng đầu hoặc trong comment)
        customers_count = 0
        vehicles_count = 0
        capacity = 0
        
        for line in lines[:10]:  # Chỉ check 10 dòng đầu
            line = line.strip()
            if line and not line.startswith('#') and not line.startswith('//'):
                # Thử parse số customers từ dòng đầu tiên có số
                numbers = re.findall(r'\d+', line)
                if len(numbers) >= 2:
                    customers_count = int(numbers[0]) if customers_count == 0 else customers_count
                    vehicles_count = int(numbers[1]) if vehicles_count == 0 else vehicles_count
                    if len(numbers) >= 3:
                        capacity = int(numbers[2]) if capacity == 0 else capacity
                    break
        
        return {
            'status': 'success',
            'total_lines': total_lines,
            'non_empty_lines': non_empty_lines,
            'estimated_customers': customers_count,
            'estimated_vehicles': vehicles_count,
            'capacity': capacity,
            'file_size': os.path.getsize(file_path)
        }
        
    except Exception as e:
        return {
            'status': 'error',
            'error': str(e),
            'file_size': os.path.getsize(file_path) if os.path.exists(file_path) else 0
        }

def check_data_availability():
    """Kiểm tra chi tiết tính sẵn sàng của dữ liệu đầu vào"""
    
    data_dirs = {
        'PDPTW': {
            'data_dir': 'data/pdptw/src',
            'description': 'Pickup and Delivery Problem with Time Windows - Input Data'
        },
        'VRPTW': {
            'data_dir': 'data/vrptw/src',
            'description': 'Vehicle Routing Problem with Time Windows - Input Data'
        },
        'VRPSPDTW_Wang_Chen': {
            'data_dir': 'data/vrpspdtw_Wang_Chen/src',
            'description': 'VRP with Simultaneous Pickup-Delivery (Wang-Chen) - Input Data'
        },
        'VRPSPDTW_Liu_Tang_Yao': {
            'data_dir': 'data/vrpspdtw_Liu_Tang_Yao/src',
            'description': 'VRP with Simultaneous Pickup-Delivery (Liu-Tang-Yao) - Input Data'
        }
    }
    
    print(f"\n" + "=" * 100)
    print("🔍 KIỂM TRA CHI TIẾT DỮ LIỆU ĐẦU VÀO")
    print("=" * 100)
    
    total_data_files = 0
    total_data_size = 0
    
    for problem_name, problem_info in data_dirs.items():
        data_dir = problem_info['data_dir']
        description = problem_info['description']
        
        print(f"\n{'='*60}")
        print(f"📂 {problem_name}")
        print(f"📝 {description}")
        print(f"📁 Thư mục: {data_dir}")
        print(f"{'='*60}")
        
        if not os.path.exists(data_dir):
            print(f"   ❌ Thư mục không tồn tại: {data_dir}")
            continue
            
        data_files = glob.glob(os.path.join(data_dir, "*.txt"))
        print(f"📊 Tổng số file dữ liệu: {len(data_files)}")
        
        if len(data_files) == 0:
            print(f"   ⚠️ Không có file dữ liệu nào")
            continue
        
        print(f"\n📋 PHÂN TÍCH CHI TIẾT CÁC FILE DỮ LIỆU:")
        print("-" * 80)
        
        total_customers = 0
        total_vehicles = 0
        file_sizes = []
        
        for file_path in data_files:
            filename = os.path.basename(file_path)
            result = analyze_data_file(file_path)
            
            if result['status'] == 'success':
                print(f"✅ {filename:<25} | {result['estimated_customers']:>3} customers | {result['estimated_vehicles']:>2} vehicles | Capacity: {result['capacity']:>4} | Size: {result['file_size']:>6} bytes | Lines: {result['non_empty_lines']:>3}")
                total_customers += result['estimated_customers']
                total_vehicles += result['estimated_vehicles']
                file_sizes.append(result['file_size'])
            else:
                print(f"❌ {filename:<25} | ERROR: {result.get('error', 'Unknown')} | Size: {result['file_size']:>6} bytes")
                file_sizes.append(result['file_size'])
        
        # Thống kê tổng hợp
        if file_sizes:
            total_size = sum(file_sizes)
            avg_size = total_size / len(file_sizes)
            
            print(f"\n📈 THỐNG KÊ TỔNG HỢP:")
            print("-" * 50)
            print(f"📁 Tổng số file:           {len(data_files)}")
            print(f"👥 Tổng customers:         {total_customers}")
            print(f"🚛 Tổng vehicles:          {total_vehicles}")
            print(f"💾 Tổng dung lượng:        {total_size:,} bytes ({total_size/1024:.1f} KB)")
            print(f"📊 Dung lượng trung bình:  {avg_size:.0f} bytes")
            print(f"📏 File nhỏ nhất:          {min(file_sizes):,} bytes")
            print(f"📏 File lớn nhất:          {max(file_sizes):,} bytes")
            
            total_data_files += len(data_files)
            total_data_size += total_size
    
    print(f"\n" + "=" * 100)
    print(f"📊 TỔNG KẾT DỮ LIỆU ĐẦU VÀO")
    print("=" * 100)
    print(f"📁 Tổng số file dữ liệu:      {total_data_files}")
    print(f"💾 Tổng dung lượng:           {total_data_size:,} bytes ({total_data_size/1024:.1f} KB)")
    print("=" * 100)

def generate_comparison_report():
    """Tạo báo cáo so sánh giữa các bài toán"""
    print(f"\n" + "=" * 100)
    print("📊 BÁO CÁO SO SÁNH HIỆU SUẤT GIỮA CÁC BÀI TOÁN")
    print("=" * 100)
    
    problem_details, overall_stats = analyze_problem_results()
    
    # So sánh tỷ lệ thành công
    print(f"\n🏆 XẾP HẠNG THEO TỶ LỆ THÀNH CÔNG:")
    print("-" * 60)
    
    success_rates = []
    for problem_name, details in problem_details.items():
        if details.get('status') == 'analyzed':
            total = details['total_files']
            successful = details['file_stats']['successful']
            rate = (successful / total * 100) if total > 0 else 0
            success_rates.append((problem_name, rate, successful, total))
    
    success_rates.sort(key=lambda x: x[1], reverse=True)
    
    for i, (problem_name, rate, successful, total) in enumerate(success_rates, 1):
        print(f"{i}. {problem_name:<25} | {rate:>5.1f}% ({successful}/{total})")
    
    # So sánh hiệu suất trung bình
    print(f"\n📈 SO SÁNH HIỆU SUẤT TRUNG BÌNH:")
    print("-" * 80)
    print(f"{'Problem':<25} | {'Avg Routes':<12} | {'Avg Customers':<15} | {'Avg Distance':<15}")
    print("-" * 80)
    
    for problem_name, details in problem_details.items():
        if details.get('status') == 'analyzed' and details['route_stats']:
            avg_routes = sum(details['route_stats']) / len(details['route_stats'])
            avg_customers = sum(details['customer_stats']) / len(details['customer_stats'])
            avg_distance = sum(details['distance_stats']) / len(details['distance_stats']) if details['distance_stats'] else 0
            
            print(f"{problem_name:<25} | {avg_routes:>10.1f} | {avg_customers:>13.1f} | {avg_distance:>13.1f}")

def save_analysis_report(problem_details, overall_stats):
    """Lưu báo cáo phân tích ra file JSON và TXT"""
    timestamp = datetime.now().strftime('%Y%m%d_%H%M%S')
    
    # Lưu báo cáo JSON
    report = {
        'analysis_time': datetime.now().isoformat(),
        'overall_stats': overall_stats,
        'problem_details': problem_details,
        'summary': {
            'total_problems_analyzed': len(problem_details),
            'success_rate': (overall_stats['successful_files'] / overall_stats['total_files'] * 100) if overall_stats['total_files'] > 0 else 0
        }
    }
    
    json_file = f"analysis_report_{timestamp}.json"
    txt_file = f"analysis_report_{timestamp}.txt"
    
    try:
        # Lưu JSON
        with open(json_file, 'w', encoding='utf-8') as f:
            json.dump(report, f, indent=2, ensure_ascii=False)
        
        # Lưu TXT (dễ đọc hơn)
        with open(txt_file, 'w', encoding='utf-8') as f:
            f.write("=" * 100 + "\n")
            f.write("BÁO CÁO PHÂN TÍCH CHI TIẾT CÁC BÀI TOÁN VRP\n")
            f.write("=" * 100 + "\n")
            f.write(f"📅 Thời gian phân tích: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}\n\n")
            
            # Tổng kết chung
            f.write("📊 TỔNG KẾT TOÀN BỘ DỰ ÁN\n")
            f.write("=" * 50 + "\n")
            f.write(f"📁 Tổng số file solution:     {overall_stats['total_files']}\n")
            f.write(f"✅ File thành công:           {overall_stats['successful_files']} ({overall_stats['successful_files']/overall_stats['total_files']*100:.1f}%)\n")
            f.write(f"⚪ File trống:                {overall_stats['empty_files']} ({overall_stats['empty_files']/overall_stats['total_files']*100:.1f}%)\n")
            f.write(f"❌ File lỗi:                 {overall_stats['error_files']} ({overall_stats['error_files']/overall_stats['total_files']*100:.1f}%)\n")
            f.write(f"🚛 Tổng số routes:            {overall_stats['total_routes']}\n")
            f.write(f"👥 Tổng khách hàng phục vụ:   {overall_stats['total_customers_served']}\n")
            f.write(f"📏 Tổng khoảng cách:          {overall_stats['total_distance']:.1f}\n\n")
            
            # Chi tiết từng bài toán
            for problem_name, details in problem_details.items():
                f.write("=" * 60 + "\n")
                f.write(f"🔍 {problem_name}\n")
                f.write("=" * 60 + "\n")
                
                if details.get('status') == 'analyzed':
                    file_stats = details['file_stats']
                    f.write(f"📁 Tổng số file:           {details['total_files']}\n")
                    f.write(f"✅ File thành công:        {file_stats['successful']} ({file_stats['successful']/details['total_files']*100:.1f}%)\n")
                    f.write(f"⚪ File trống:             {file_stats['empty']} ({file_stats['empty']/details['total_files']*100:.1f}%)\n")
                    f.write(f"⚠️  File không có khách:    {file_stats['no_customers']} ({file_stats['no_customers']/details['total_files']*100:.1f}%)\n")
                    f.write(f"❌ File lỗi:              {file_stats['error']} ({file_stats['error']/details['total_files']*100:.1f}%)\n")
                    
                    if details['route_stats']:
                        route_stats = details['route_stats']
                        f.write(f"\n🚛 THỐNG KÊ ROUTES:\n")
                        f.write(f"   • Trung bình:          {sum(route_stats)/len(route_stats):.1f} routes/file\n")
                        f.write(f"   • Min - Max:           {min(route_stats)} - {max(route_stats)} routes\n")
                        f.write(f"   • Tổng routes:         {sum(route_stats)}\n")
                        
                    if details['customer_stats']:
                        customer_stats = details['customer_stats']
                        f.write(f"\n👥 THỐNG KÊ KHÁCH HÀNG:\n")
                        f.write(f"   • Trung bình:          {sum(customer_stats)/len(customer_stats):.1f} customers/file\n")
                        f.write(f"   • Min - Max:           {min(customer_stats)} - {max(customer_stats)} customers\n")
                        f.write(f"   • Tổng khách hàng:     {sum(customer_stats)}\n")
                        
                    if details['distance_stats']:
                        distance_stats = details['distance_stats']
                        f.write(f"\n📏 THỐNG KÊ KHOẢNG CÁCH:\n")
                        f.write(f"   • Trung bình:          {sum(distance_stats)/len(distance_stats):.1f}\n")
                        f.write(f"   • Min - Max:           {min(distance_stats):.1f} - {max(distance_stats):.1f}\n")
                        f.write(f"   • Tổng khoảng cách:    {sum(distance_stats):.1f}\n")
                        
                    # Chi tiết một số file mẫu
                    f.write(f"\n📋 CHI TIẾT MỘT SỐ FILE MẪU:\n")
                    f.write("-" * 80 + "\n")
                    for file_detail in details['file_details'][:10]:
                        result = file_detail['result']
                        if result['status'] == 'success':
                            f.write(f"✅ {file_detail['filename']:<25} | {result['routes']:>2} routes | {result['customers_served']:>3} customers | Distance: {result['total_distance']:>8.1f}\n")
                        elif result['status'] == 'empty':
                            f.write(f"⚪ {file_detail['filename']:<25} | EMPTY FILE\n")
                        elif result['status'] == 'no_customers':
                            f.write(f"⚠️  {file_detail['filename']:<25} | NO CUSTOMERS SERVED\n")
                        else:
                            f.write(f"❌ {file_detail['filename']:<25} | ERROR: {result.get('error', 'Unknown')}\n")
                            
                else:
                    f.write(f"❌ Trạng thái: {details.get('status', 'Unknown')}\n")
                    
                f.write("\n")
            
            f.write("=" * 100 + "\n")
            f.write("KẾT THÚC BÁO CÁO\n")
            f.write("=" * 100 + "\n")
        
        print(f"\n💾 Báo cáo đã được lưu vào:")
        print(f"   📄 File JSON: {json_file}")
        print(f"   📄 File TXT:  {txt_file}")
        
    except Exception as e:
        print(f"\n❌ Lỗi khi lưu báo cáo: {e}")

def save_detailed_text_report():
    """Tạo và lưu báo cáo chi tiết dạng text dễ đọc"""
    timestamp = datetime.now().strftime('%Y%m%d_%H%M%S')
    report_file = f"detailed_analysis_{timestamp}.txt"
    
    try:
        with open(report_file, 'w', encoding='utf-8') as f:
            # Redirect stdout để ghi vào file
            import sys
            original_stdout = sys.stdout
            sys.stdout = f
            
            # Chạy tất cả các phân tích
            print("🚀 Bắt đầu phân tích toàn diện...")
            problem_details, overall_stats = analyze_problem_results()
            check_data_availability()
            generate_comparison_report()
            show_problem_recommendations()
            
            # Restore stdout
            sys.stdout = original_stdout
            
        print(f"\n💾 Báo cáo chi tiết đã được lưu vào: {report_file}")
        print(f"📖 Bạn có thể mở file này bằng notepad hoặc text editor để xem chi tiết")
        
    except Exception as e:
        print(f"\n❌ Lỗi khi tạo báo cáo chi tiết: {e}")

def show_problem_recommendations():
    """Hiển thị khuyến nghị cải thiện cho từng bài toán"""
    print(f"\n" + "=" * 100)
    print("💡 KHUYẾN NGHỊ CẢI THIỆN")
    print("=" * 100)
    
    problem_details, _ = analyze_problem_results()
    
    for problem_name, details in problem_details.items():
        print(f"\n🔧 {problem_name}:")
        
        if details.get('status') == 'directory_not_found':
            print("   ❌ Cần tạo thư mục solution")
            print("   💡 Khuyến nghị: Chạy thuật toán để tạo solution files")
            
        elif details.get('status') == 'no_files':
            print("   ❌ Không có file solution")
            print("   💡 Khuyến nghị: Chạy thuật toán tối ưu hóa cho bài toán này")
            
        elif details.get('status') == 'analyzed':
            total = details['total_files']
            successful = details['file_stats']['successful']
            empty = details['file_stats']['empty']
            error = details['file_stats']['error']
            
            success_rate = (successful / total * 100) if total > 0 else 0
            
            if success_rate < 50:
                print("   ⚠️ Tỷ lệ thành công thấp")
                print("   💡 Khuyến nghị: Kiểm tra và cải thiện thuật toán")
                
            if empty > 0:
                print(f"   ⚠️ Có {empty} file trống")
                print("   💡 Khuyến nghị: Kiểm tra quá trình tạo solution")
                
            if error > 0:
                print(f"   ❌ Có {error} file lỗi")
                print("   💡 Khuyến nghị: Kiểm tra format và encoding của file")
                
            if success_rate >= 80:
                print("   ✅ Tình trạng tốt")
                if details['distance_stats']:
                    avg_distance = sum(details['distance_stats']) / len(details['distance_stats'])
                    print(f"   💡 Có thể tối ưu thêm để giảm khoảng cách trung bình ({avg_distance:.1f})")
    
    print(f"\n" + "=" * 100)

def main():
    """Chương trình chính với menu lựa chọn"""
    while True:
        print(f"\n" + "=" * 100)
        print("🔍 CÔNG CỤ PHÂN TÍCH CHI TIẾT CÁC BÀI TOÁN VRP")
        print("=" * 100)
        print("1. 📊 Phân tích chi tiết kết quả solution")
        print("2. 📂 Kiểm tra dữ liệu đầu vào")
        print("3. 📈 Báo cáo so sánh hiệu suất")
        print("4. 💡 Khuyến nghị cải thiện")
        print("5. 💾 Lưu báo cáo phân tích (JSON + TXT)")
        print("6. 📄 Tạo báo cáo chi tiết toàn diện (TXT)")
        print("7. 🔄 Chạy phân tích toàn diện")
        print("0. ❌ Thoát")
        print("=" * 100)
        
        try:
            choice = input("👉 Chọn chức năng (0-7): ").strip()
            
            if choice == '0':
                print("👋 Cảm ơn bạn đã sử dụng công cụ phân tích!")
                break
                
            elif choice == '1':
                problem_details, overall_stats = analyze_problem_results()
                
            elif choice == '2':
                check_data_availability()
                
            elif choice == '3':
                generate_comparison_report()
                
            elif choice == '4':
                show_problem_recommendations()
                
            elif choice == '5':
                problem_details, overall_stats = analyze_problem_results()
                save_analysis_report(problem_details, overall_stats)
                
            elif choice == '6':
                save_detailed_text_report()
                
            elif choice == '7':
                print("🚀 Bắt đầu phân tích toàn diện...")
                problem_details, overall_stats = analyze_problem_results()
                check_data_availability()
                generate_comparison_report()
                show_problem_recommendations()
                save_analysis_report(problem_details, overall_stats)
                print("✅ Hoàn thành phân tích toàn diện!")
                
            else:
                print("❌ Lựa chọn không hợp lệ. Vui lòng chọn từ 0-7.")
                
        except KeyboardInterrupt:
            print("\n👋 Tạm biệt!")
            break
        except Exception as e:
            print(f"❌ Lỗi: {e}")
            
        # Hỏi có muốn tiếp tục không
        if choice != '0':
            input("\n⏸️  Nhấn Enter để tiếp tục...")

if __name__ == "__main__":
    main()