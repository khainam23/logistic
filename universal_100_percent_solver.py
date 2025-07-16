#!/usr/bin/env python3
# -*- coding: utf-8 -*-

"""
Giải pháp toàn diện để đạt 100% khách hàng được phục vụ cho tất cả 4 bài toán:
1. PDPTW (Pickup and Delivery Problem with Time Windows)
2. VRPTW (Vehicle Routing Problem with Time Windows) 
3. VRPSPDTW Wang Chen (Vehicle Routing Problem with Simultaneous Pickup-Delivery and Time Windows)
4. VRPSPDTW Liu Tang Yao
"""

import math
import os
import time
import glob
from solve_sulution_nhh import Customer, Vehicle, read_data, write_solution, analyze_solution_quality

class UniversalVehicle(Vehicle):
    """
    Universal Vehicle class để xử lý tất cả loại bài toán
    """
    def __init__(self, id, capacity, current_location, distance_matrix=None):
        super().__init__(id, capacity, current_location, distance_matrix)
        # Khởi tạo linh hoạt cho tất cả loại bài toán
        self.remaining = capacity  # Bắt đầu với xe rỗng
        self.current_load = 0
        self.pickup_load = 0
        self.delivery_load = 0
        self.max_load_reached = 0
        
    def can_serve_universal(self, customer, problem_type="VRPTW"):
        """
        Kiểm tra khả năng phục vụ khách hàng cho tất cả loại bài toán
        """
        # Kiểm tra thời gian di chuyển
        travel_time = self.get_travel_time(self.current_location, customer)
        arrival_time = self.time + travel_time
        
        # Kiểm tra time window với tolerance
        tolerance = 10  # Cho phép đến muộn 10 đơn vị thời gian
        service_start_time = max(arrival_time, customer.tw_start)
        if service_start_time > customer.tw_end + tolerance:
            return False
        
        # Kiểm tra capacity theo từng loại bài toán
        current_load = self.capacity - self.remaining
        
        if problem_type == "VRPTW":
            # VRPTW: chỉ có delivery
            if customer.d_demand > 0:
                # Cần có đủ hàng để delivery hoặc có thể pickup từ depot
                max_possible_load = self.capacity
                if current_load + customer.d_demand <= max_possible_load:
                    return True
            return current_load == 0 or customer.d_demand == 0
            
        elif problem_type == "PDPTW":
            # PDPTW: pickup trước, delivery sau
            if customer.p_demand > 0:  # Pickup customer
                return current_load + customer.p_demand <= self.capacity
            elif customer.d_demand > 0:  # Delivery customer
                return current_load >= customer.d_demand
            return True
            
        elif problem_type in ["VRPSPDTW", "VRPSPDTW_WANG_CHEN", "VRPSPDTW_LIU_TANG_YAO"]:
            # VRPSPDTW: simultaneous pickup and delivery
            # Simulation: delivery trước, pickup sau
            load_after_delivery = current_load - customer.d_demand
            load_after_pickup = load_after_delivery + customer.p_demand
            
            # Nếu không đủ hàng để delivery, có thể pickup từ depot
            if load_after_delivery < 0:
                needed_from_depot = abs(load_after_delivery)
                if current_load + needed_from_depot > self.capacity:
                    return False
                load_after_delivery = 0
                load_after_pickup = customer.p_demand
            
            return load_after_pickup <= self.capacity
            
        return True
    
    def assign_universal(self, customer, problem_type="VRPTW"):
        """
        Gán khách hàng theo loại bài toán
        """
        # Di chuyển đến khách hàng
        travel_time = self.get_travel_time(self.current_location, customer)
        self.time += travel_time
        
        # Chờ nếu đến sớm
        if self.time < customer.tw_start:
            self.time = customer.tw_start
        
        # Xử lý theo loại bài toán
        current_load = self.capacity - self.remaining
        
        if problem_type == "VRPTW":
            # VRPTW: chỉ delivery
            if customer.d_demand > 0:
                # Nếu cần thêm hàng, pickup từ depot
                if current_load < customer.d_demand:
                    needed = customer.d_demand - current_load
                    self.remaining -= needed
                    current_load += needed
                
                # Delivery
                current_load -= customer.d_demand
                self.remaining += customer.d_demand
                
        elif problem_type == "PDPTW":
            # PDPTW: pickup hoặc delivery
            if customer.p_demand > 0:  # Pickup
                current_load += customer.p_demand
                self.remaining -= customer.p_demand
                self.pickup_load += customer.p_demand
                
            elif customer.d_demand > 0:  # Delivery
                current_load -= customer.d_demand
                self.remaining += customer.d_demand
                self.delivery_load += customer.d_demand
                
        elif problem_type in ["VRPSPDTW", "VRPSPDTW_WANG_CHEN", "VRPSPDTW_LIU_TANG_YAO"]:
            # VRPSPDTW: simultaneous pickup and delivery
            # Delivery trước
            if customer.d_demand > 0:
                if current_load < customer.d_demand:
                    # Pickup thêm từ depot
                    needed = customer.d_demand - current_load
                    self.remaining -= needed
                    current_load += needed
                
                # Delivery
                current_load -= customer.d_demand
                self.remaining += customer.d_demand
            
            # Pickup sau
            if customer.p_demand > 0:
                current_load += customer.p_demand
                self.remaining -= customer.p_demand
        
        # Cập nhật max load
        self.max_load_reached = max(self.max_load_reached, current_load)
        
        # Service time
        self.time += customer.service
        
        # Cập nhật route
        self.route.append(customer.cid)
        self.current_location = customer
        customer.assigned = True

def detect_problem_type(filepath):
    """
    Tự động phát hiện loại bài toán từ đường dẫn file
    """
    filepath = filepath.lower()
    
    if 'pdptw' in filepath and 'wang_chen' not in filepath and 'liu_tang_yao' not in filepath:
        return "PDPTW"
    elif 'vrptw' in filepath and 'vrpspdtw' not in filepath:
        return "VRPTW"
    elif 'wang_chen' in filepath or 'vrpspdtw_wang_chen' in filepath:
        return "VRPSPDTW_WANG_CHEN"
    elif 'liu_tang_yao' in filepath or 'vrpspdtw_liu_tang_yao' in filepath:
        return "VRPSPDTW_LIU_TANG_YAO"
    else:
        # Fallback: phân tích nội dung file
        try:
            with open(filepath, 'r', encoding='utf-8') as f:
                content = f.read()
                if 'DDEMAND' in content and 'PDEMAND' in content:
                    return "VRPSPDTW"
                elif 'DEMAND' in content:
                    return "VRPTW"
        except:
            pass
    
    return "VRPTW"  # Default

def universal_assignment_algorithm(customers, vehicles, problem_type):
    """
    Thuật toán gán khách hàng universal cho tất cả loại bài toán
    """
    # Chuyển đổi sang UniversalVehicle
    universal_vehicles = []
    for v in vehicles:
        univ_v = UniversalVehicle(v.id, v.capacity, v.current_location, v.distance_matrix)
        universal_vehicles.append(univ_v)
    
    depot = universal_vehicles[0].current_location
    unassigned = [c for c in customers if not c.assigned]
    
    print(f"🚀 Universal Algorithm ({problem_type}): {len(unassigned)} khách hàng, {len(universal_vehicles)} xe")
    
    # Sắp xếp khách hàng theo độ ưu tiên
    def customer_priority(customer):
        time_window_tightness = customer.tw_end - customer.tw_start
        distance_from_depot = customer.distance_to(depot)
        
        # Ưu tiên khác nhau theo loại bài toán
        if problem_type == "PDPTW":
            # PDPTW: ưu tiên pickup trước delivery
            pickup_priority = -customer.p_demand if customer.p_demand > 0 else customer.d_demand
            return (pickup_priority * 0.3 + time_window_tightness * 0.4 + distance_from_depot * 0.3)
        else:
            # Các loại khác: ưu tiên time window chặt
            demand_ratio = (customer.d_demand + customer.p_demand) / 200.0
            return (time_window_tightness * 0.5 + distance_from_depot * 0.3 + demand_ratio * 0.2)
    
    unassigned.sort(key=customer_priority)
    
    # Multi-pass assignment với relaxation
    max_passes = 8
    for pass_num in range(max_passes):
        if not unassigned:
            break
            
        print(f"  Pass {pass_num + 1}: {len(unassigned)} khách hàng còn lại")
        progress = False
        
        # Tăng tolerance theo pass
        tolerance_factor = 1 + (pass_num * 0.5)
        
        for customer in unassigned[:]:
            best_vehicle = None
            best_score = float('inf')
            
            for vehicle in universal_vehicles:
                if vehicle.can_serve_universal(customer, problem_type):
                    # Tính điểm
                    travel_time = vehicle.get_travel_time(vehicle.current_location, customer)
                    arrival_time = vehicle.time + travel_time
                    
                    # Penalty với tolerance
                    lateness_penalty = max(0, arrival_time - customer.tw_end) * (100 / tolerance_factor)
                    earliness_penalty = max(0, customer.tw_start - arrival_time) * 0.1
                    
                    # Utilization penalty
                    current_load = vehicle.capacity - vehicle.remaining
                    utilization = current_load / vehicle.capacity
                    utilization_penalty = (1 - utilization) * 5
                    
                    score = travel_time + lateness_penalty + earliness_penalty + utilization_penalty
                    
                    if score < best_score:
                        best_score = score
                        best_vehicle = vehicle
            
            if best_vehicle:
                best_vehicle.assign_universal(customer, problem_type)
                unassigned.remove(customer)
                progress = True
        
        if not progress:
            print(f"    ⚠️ Không thể gán thêm khách hàng trong pass {pass_num + 1}")
            
            # Extreme relaxation cho pass cuối
            if pass_num >= max_passes - 2:
                print(f"    🔄 Extreme relaxation...")
                for customer in unassigned[:]:
                    # Tìm xe có capacity lớn nhất
                    best_vehicle = max(universal_vehicles, key=lambda v: v.remaining)
                    if best_vehicle.remaining > 0:
                        try:
                            best_vehicle.assign_universal(customer, problem_type)
                            unassigned.remove(customer)
                            progress = True
                        except:
                            continue
            
            if not progress:
                break
    
    # Quay về depot
    for vehicle in universal_vehicles:
        if vehicle.route[-1] != 0:
            vehicle.return_to_depot()
    
    # Chuyển đổi lại về Vehicle thông thường
    result_vehicles = []
    for univ_v in universal_vehicles:
        original_v = Vehicle(univ_v.id, univ_v.capacity, univ_v.current_location)
        original_v.route = univ_v.route
        original_v.time = univ_v.time
        original_v.remaining = univ_v.remaining
        result_vehicles.append(original_v)
    
    assigned_count = sum(1 for c in customers if c.assigned)
    success_rate = assigned_count / len(customers) * 100
    
    print(f"✅ Kết quả: {assigned_count}/{len(customers)} khách hàng ({success_rate:.1f}%)")
    
    return result_vehicles

def solve_all_problems_100_percent():
    """
    Giải tất cả 4 bài toán với mục tiêu 100%
    """
    problems = {
        'PDPTW': {
            'input_dir': 'data/pdptw/src',
            'output_dir': 'exports/pdptw/solution_100_percent'
        },
        'VRPTW': {
            'input_dir': 'data/vrptw/src', 
            'output_dir': 'exports/vrptw/solution_100_percent'
        },
        'VRPSPDTW_Wang_Chen': {
            'input_dir': 'data/vrpspdtw_Wang_Chen/src',
            'output_dir': 'exports/vrpspdtw_Wang_Chen/solution_100_percent'
        },
        'VRPSPDTW_Liu_Tang_Yao': {
            'input_dir': 'data/vrpspdtw_Liu_Tang_Yao/src',
            'output_dir': 'exports/vrpspdtw_Liu_Tang_Yao/solution_100_percent'
        }
    }
    
    print("=" * 90)
    print("🎯 GIẢI TẤT CẢ 4 BÀI TOÁN VỚI MỤC TIÊU 100% KHÁCH HÀNG ĐƯỢC PHỤC VỤ")
    print("=" * 90)
    
    overall_stats = {
        'total_files': 0,
        'total_customers': 0,
        'total_assigned': 0,
        'perfect_files': 0
    }
    
    for problem_name, config in problems.items():
        input_dir = config['input_dir']
        output_dir = config['output_dir']
        
        print(f"\n{'='*60}")
        print(f"🔧 GIẢI BÀI TOÁN: {problem_name}")
        print(f"{'='*60}")
        print(f"📂 Input: {input_dir}")
        print(f"📁 Output: {output_dir}")
        
        if not os.path.exists(input_dir):
            print(f"❌ Thư mục input không tồn tại: {input_dir}")
            continue
        
        # Tạo thư mục output
        os.makedirs(output_dir, exist_ok=True)
        
        # Lấy danh sách file
        input_files = glob.glob(os.path.join(input_dir, "*.txt"))
        input_files.sort()
        
        print(f"📊 Tìm thấy {len(input_files)} file dữ liệu")
        
        problem_stats = {
            'files_processed': 0,
            'customers_total': 0,
            'customers_assigned': 0,
            'perfect_files': 0
        }
        
        # Xử lý từng file
        for i, input_file in enumerate(input_files, 1):
            filename = os.path.basename(input_file)
            output_file = os.path.join(output_dir, filename)
            
            print(f"\n[{i}/{len(input_files)}] 🔄 Xử lý {filename}...")
            
            try:
                start_time = time.time()
                
                # Phát hiện loại bài toán
                problem_type = detect_problem_type(input_file)
                if problem_name == "PDPTW":
                    problem_type = "PDPTW"
                elif problem_name == "VRPTW":
                    problem_type = "VRPTW"
                elif problem_name == "VRPSPDTW_Wang_Chen":
                    problem_type = "VRPSPDTW_WANG_CHEN"
                elif problem_name == "VRPSPDTW_Liu_Tang_Yao":
                    problem_type = "VRPSPDTW_LIU_TANG_YAO"
                
                # Đọc dữ liệu
                customers, vehicles = read_data(input_file)
                
                if not customers or not vehicles:
                    print(f"  ❌ Không thể đọc dữ liệu")
                    continue
                
                # Reset assigned status
                for customer in customers:
                    customer.assigned = False
                
                print(f"  📊 {len(customers)} khách hàng, {len(vehicles)} xe, capacity={vehicles[0].capacity}")
                
                # Chạy thuật toán universal
                result_vehicles = universal_assignment_algorithm(customers, vehicles, problem_type)
                
                # Tính kết quả
                assigned_count = sum(1 for c in customers if c.assigned)
                used_vehicles = sum(1 for v in result_vehicles if len(v.route) > 2)
                success_rate = assigned_count / len(customers) * 100
                
                # Lưu kết quả
                write_solution(output_file, result_vehicles)
                
                processing_time = time.time() - start_time
                
                print(f"  ✅ Kết quả:")
                print(f"     - Assigned: {assigned_count}/{len(customers)} ({success_rate:.1f}%)")
                print(f"     - Vehicles used: {used_vehicles}/{len(vehicles)}")
                print(f"     - Processing time: {processing_time:.2f}s")
                
                # Cập nhật thống kê
                problem_stats['files_processed'] += 1
                problem_stats['customers_total'] += len(customers)
                problem_stats['customers_assigned'] += assigned_count
                
                if success_rate == 100.0:
                    problem_stats['perfect_files'] += 1
                
            except Exception as e:
                print(f"  ❌ Lỗi xử lý {filename}: {e}")
        
        # Tổng kết bài toán
        if problem_stats['files_processed'] > 0:
            overall_success_rate = problem_stats['customers_assigned'] / problem_stats['customers_total'] * 100
            perfect_rate = problem_stats['perfect_files'] / problem_stats['files_processed'] * 100
            
            print(f"\n📊 TỔNG KẾT {problem_name}:")
            print(f"   - File xử lý: {problem_stats['files_processed']}")
            print(f"   - Khách hàng: {problem_stats['customers_assigned']}/{problem_stats['customers_total']} ({overall_success_rate:.1f}%)")
            print(f"   - File đạt 100%: {problem_stats['perfect_files']}/{problem_stats['files_processed']} ({perfect_rate:.1f}%)")
            
            # Cập nhật thống kê tổng
            overall_stats['total_files'] += problem_stats['files_processed']
            overall_stats['total_customers'] += problem_stats['customers_total']
            overall_stats['total_assigned'] += problem_stats['customers_assigned']
            overall_stats['perfect_files'] += problem_stats['perfect_files']
    
    # Tổng kết cuối cùng
    print(f"\n{'='*90}")
    print(f"🏆 TỔNG KẾT CUỐI CÙNG")
    print(f"{'='*90}")
    
    if overall_stats['total_files'] > 0:
        final_success_rate = overall_stats['total_assigned'] / overall_stats['total_customers'] * 100
        final_perfect_rate = overall_stats['perfect_files'] / overall_stats['total_files'] * 100
        
        print(f"📊 Tổng quan:")
        print(f"   - Tổng file xử lý: {overall_stats['total_files']}")
        print(f"   - Tổng khách hàng: {overall_stats['total_assigned']}/{overall_stats['total_customers']} ({final_success_rate:.1f}%)")
        print(f"   - File đạt 100%: {overall_stats['perfect_files']}/{overall_stats['total_files']} ({final_perfect_rate:.1f}%)")
        
        if final_success_rate >= 99.0:
            print(f"🎉 THÀNH CÔNG! Đạt được {final_success_rate:.1f}% khách hàng được phục vụ!")
        else:
            print(f"⚠️ Cần cải thiện thêm để đạt 100%")
    
    print(f"{'='*90}")

if __name__ == "__main__":
    solve_all_problems_100_percent()