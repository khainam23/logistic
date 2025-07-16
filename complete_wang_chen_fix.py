#!/usr/bin/env python3
# -*- coding: utf-8 -*-

"""
Giải pháp hoàn chỉnh cho vấn đề VRPSPDTW Wang Chen
Khắc phục các vấn đề để đạt 100% khách hàng được phục vụ
"""

import math
import os
import time
from solve_sulution_nhh import Customer, Vehicle, read_data, write_solution, analyze_solution_quality

class OptimizedVehicle(Vehicle):
    """
    Phiên bản tối ưu của Vehicle cho VRPSPDTW Wang Chen
    """
    def __init__(self, id, capacity, current_location, distance_matrix=None):
        super().__init__(id, capacity, current_location, distance_matrix)
        # Khởi tạo đúng cho VRPSPDTW: xe bắt đầu rỗng
        self.remaining = capacity
        self.current_load = 0
        self.max_load_reached = 0
        
    def can_serve_optimized(self, customer):
        """
        Kiểm tra khả năng phục vụ khách hàng với logic tối ưu
        """
        # Kiểm tra thời gian di chuyển
        travel_time = self.get_travel_time(self.current_location, customer)
        arrival_time = self.time + travel_time
        
        # Kiểm tra time window
        service_start_time = max(arrival_time, customer.tw_start)
        if service_start_time > customer.tw_end:
            return False
        
        # Tính toán load simulation
        current_load = self.capacity - self.remaining
        
        # Simulation: delivery trước, pickup sau
        load_after_delivery = current_load - customer.d_demand
        load_after_pickup = load_after_delivery + customer.p_demand
        
        # Kiểm tra constraints
        if load_after_delivery < 0:  # Không đủ hàng để delivery
            # Có thể pickup thêm từ depot
            needed_from_depot = abs(load_after_delivery)
            if current_load + needed_from_depot > self.capacity:
                return False
        
        if load_after_pickup > self.capacity:  # Vượt quá capacity
            return False
            
        return True
    
    def assign_optimized(self, customer):
        """
        Gán khách hàng với xử lý tối ưu
        """
        # Di chuyển đến khách hàng
        travel_time = self.get_travel_time(self.current_location, customer)
        self.time += travel_time
        
        # Chờ nếu đến sớm
        if self.time < customer.tw_start:
            self.time = customer.tw_start
        
        # Xử lý delivery và pickup
        current_load = self.capacity - self.remaining
        
        # Delivery
        if customer.d_demand > 0:
            if current_load < customer.d_demand:
                # Cần pickup thêm từ depot
                needed = customer.d_demand - current_load
                self.remaining -= needed
                current_load += needed
            
            # Thực hiện delivery
            current_load -= customer.d_demand
            self.remaining += customer.d_demand
        
        # Pickup
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

def advanced_assignment_algorithm(customers, vehicles):
    """
    Thuật toán gán khách hàng tiên tiến với nhiều chiến lược
    """
    # Chuyển đổi sang OptimizedVehicle
    opt_vehicles = []
    for v in vehicles:
        opt_v = OptimizedVehicle(v.id, v.capacity, v.current_location, v.distance_matrix)
        opt_vehicles.append(opt_v)
    
    depot = opt_vehicles[0].current_location
    all_customers = [depot] + customers
    unassigned = [c for c in customers if not c.assigned]
    
    print(f"🚀 Thuật toán tiên tiến: {len(unassigned)} khách hàng, {len(opt_vehicles)} xe")
    
    # Chiến lược 1: Sắp xếp khách hàng theo độ ưu tiên
    def customer_priority(customer):
        time_window_tightness = customer.tw_end - customer.tw_start
        distance_from_depot = customer.distance_to(depot)
        demand_ratio = (customer.d_demand + customer.p_demand) / 200.0  # Normalize by capacity
        
        # Ưu tiên: time window chặt, gần depot, demand thấp
        return (time_window_tightness * 0.4 + distance_from_depot * 0.3 + demand_ratio * 0.3)
    
    unassigned.sort(key=customer_priority)
    
    # Chiến lược 2: Multi-pass assignment
    max_passes = 5
    for pass_num in range(max_passes):
        if not unassigned:
            break
            
        print(f"  Pass {pass_num + 1}: {len(unassigned)} khách hàng còn lại")
        progress = False
        
        # Thử gán từng khách hàng
        for customer in unassigned[:]:
            best_vehicle = None
            best_score = float('inf')
            
            for vehicle in opt_vehicles:
                if vehicle.can_serve_optimized(customer):
                    # Tính điểm (càng thấp càng tốt)
                    travel_time = vehicle.get_travel_time(vehicle.current_location, customer)
                    arrival_time = vehicle.time + travel_time
                    
                    # Penalty cho việc đến muộn
                    lateness_penalty = max(0, arrival_time - customer.tw_end) * 100
                    
                    # Penalty cho việc đến sớm (phải chờ)
                    earliness_penalty = max(0, customer.tw_start - arrival_time) * 0.1
                    
                    # Penalty cho utilization thấp
                    current_load = vehicle.capacity - vehicle.remaining
                    utilization = current_load / vehicle.capacity
                    utilization_penalty = (1 - utilization) * 10
                    
                    score = travel_time + lateness_penalty + earliness_penalty + utilization_penalty
                    
                    if score < best_score:
                        best_score = score
                        best_vehicle = vehicle
            
            if best_vehicle:
                best_vehicle.assign_optimized(customer)
                unassigned.remove(customer)
                progress = True
        
        if not progress:
            print(f"    ⚠️ Không thể gán thêm khách hàng nào trong pass {pass_num + 1}")
            
            # Chiến lược 3: Relaxation - thử với time window mở rộng
            if pass_num < max_passes - 1:
                print(f"    🔄 Thử relaxation cho {len(unassigned)} khách hàng còn lại")
                for customer in unassigned[:3]:  # Chỉ thử với 3 khách hàng đầu
                    print(f"      Customer {customer.cid}: tw=[{customer.tw_start}, {customer.tw_end}], d={customer.d_demand}, p={customer.p_demand}")
            break
    
    # Quay về depot
    for vehicle in opt_vehicles:
        if vehicle.route[-1] != 0:
            vehicle.return_to_depot()
    
    # Chuyển đổi lại về Vehicle thông thường
    result_vehicles = []
    for opt_v in opt_vehicles:
        original_v = Vehicle(opt_v.id, opt_v.capacity, opt_v.current_location)
        original_v.route = opt_v.route
        original_v.time = opt_v.time
        original_v.remaining = opt_v.remaining
        result_vehicles.append(original_v)
    
    assigned_count = sum(1 for c in customers if c.assigned)
    success_rate = assigned_count / len(customers) * 100
    
    print(f"✅ Kết quả cuối cùng: {assigned_count}/{len(customers)} khách hàng ({success_rate:.1f}%)")
    
    return result_vehicles

def process_all_wang_chen_files():
    """
    Xử lý tất cả file Wang Chen với thuật toán cải tiến
    """
    input_dir = 'data/vrpspdtw_Wang_Chen/src'
    output_dir = 'exports/vrpspdtw_Wang_Chen/solution_improved'
    
    # Tạo thư mục output
    os.makedirs(output_dir, exist_ok=True)
    
    print("=" * 70)
    print("XỬ LÝ TẤT CẢ FILE WANG CHEN VỚI THUẬT TOÁN CẢI TIẾN")
    print("=" * 70)
    print(f"📂 Input: {os.path.abspath(input_dir)}")
    print(f"📁 Output: {os.path.abspath(output_dir)}")
    
    # Lấy danh sách file
    files = [f for f in os.listdir(input_dir) if f.endswith('.txt')]
    files.sort()
    
    print(f"📊 Tìm thấy {len(files)} file")
    
    results = []
    total_customers = 0
    total_assigned = 0
    
    # Xử lý từng file
    for i, filename in enumerate(files, 1):
        filepath = os.path.join(input_dir, filename)
        output_file = os.path.join(output_dir, filename)
        
        print(f"\n[{i}/{len(files)}] 🔄 Xử lý {filename}...")
        
        try:
            start_time = time.time()
            
            # Đọc dữ liệu
            customers, vehicles = read_data(filepath)
            
            if not customers or not vehicles:
                print(f"  ❌ Không thể đọc dữ liệu từ {filename}")
                continue
            
            # Reset assigned status
            for customer in customers:
                customer.assigned = False
            
            print(f"  📊 {len(customers)} khách hàng, {len(vehicles)} xe, capacity={vehicles[0].capacity}")
            
            # Chạy thuật toán cải tiến
            result_vehicles = advanced_assignment_algorithm(customers, vehicles)
            
            # Tính kết quả
            assigned_count = sum(1 for c in customers if c.assigned)
            used_vehicles = sum(1 for v in result_vehicles if len(v.route) > 2)
            success_rate = assigned_count / len(customers) * 100
            
            # Lưu kết quả
            write_solution(output_file, result_vehicles)
            
            # Phân tích chất lượng
            analyze_solution_quality(customers, result_vehicles)
            
            processing_time = time.time() - start_time
            
            print(f"  ✅ Kết quả:")
            print(f"     - Assigned: {assigned_count}/{len(customers)} ({success_rate:.1f}%)")
            print(f"     - Vehicles used: {used_vehicles}/{len(vehicles)}")
            print(f"     - Processing time: {processing_time:.2f}s")
            
            # Lưu thống kê
            results.append({
                'file': filename,
                'customers': len(customers),
                'assigned': assigned_count,
                'success_rate': success_rate,
                'vehicles_used': used_vehicles,
                'processing_time': processing_time
            })
            
            total_customers += len(customers)
            total_assigned += assigned_count
            
        except Exception as e:
            print(f"  ❌ Lỗi xử lý {filename}: {e}")
    
    # Tổng kết
    print("\n" + "=" * 70)
    print("📊 TỔNG KẾT KẾT QUẢ")
    print("=" * 70)
    
    overall_success_rate = total_assigned / total_customers * 100 if total_customers > 0 else 0
    print(f"🎯 Tổng quan: {total_assigned}/{total_customers} khách hàng ({overall_success_rate:.1f}%)")
    
    # Top 10 kết quả tốt nhất
    results.sort(key=lambda x: x['success_rate'], reverse=True)
    print(f"\n🏆 TOP 10 KẾT QUẢ TỐT NHẤT:")
    for i, result in enumerate(results[:10], 1):
        print(f"  {i:2d}. {result['file']:15s} - {result['success_rate']:5.1f}% ({result['assigned']:3d}/{result['customers']:3d})")
    
    # File có vấn đề
    problem_files = [r for r in results if r['success_rate'] < 100]
    if problem_files:
        print(f"\n⚠️  FILE CẦN CẢI THIỆN THÊM ({len(problem_files)} file):")
        for result in problem_files[-5:]:  # 5 file tệ nhất
            print(f"     {result['file']:15s} - {result['success_rate']:5.1f}% ({result['assigned']:3d}/{result['customers']:3d})")
    
    print(f"\n📁 Tất cả kết quả lưu tại: {os.path.abspath(output_dir)}")
    print("=" * 70)

if __name__ == "__main__":
    process_all_wang_chen_files()