#!/usr/bin/env python3
# -*- coding: utf-8 -*-

"""
Cải tiến thuật toán VRPSPDTW Wang Chen để đạt 100% khách hàng được phục vụ
"""

import math
import os
from solve_sulution_nhh import Customer, Vehicle, read_data, write_solution

class ImprovedVehicle(Vehicle):
    def __init__(self, id, capacity, current_location, distance_matrix=None):
        super().__init__(id, capacity, current_location, distance_matrix)
        # Khởi tạo xe với trạng thái phù hợp cho VRPSPDTW
        # Xe bắt đầu rỗng, sẽ pickup hàng từ depot trước khi delivery
        self.remaining = capacity  # Xe rỗng
        self.pickup_load = 0  # Hàng đã pickup
        self.delivery_load = 0  # Hàng cần delivery
        
    def can_serve_improved(self, customer):
        """
        Kiểm tra cải tiến cho VRPSPDTW Wang Chen
        Logic: Xe có thể pickup từ depot trước, sau đó delivery và pickup tại khách hàng
        """
        travel_time = self.get_travel_time(self.current_location, customer)
        arrival_time = self.time + travel_time
        
        # Kiểm tra ràng buộc thời gian
        service_start_time = max(arrival_time, customer.tw_start)
        if service_start_time > customer.tw_end:
            return False
        
        # Tính toán payload sau khi phục vụ khách hàng này
        current_load = self.capacity - self.remaining
        
        # Giả sử xe có thể pickup từ depot trước khi đến khách hàng
        # để có đủ hàng delivery
        required_delivery = customer.d_demand
        additional_pickup = customer.p_demand
        
        # Kiểm tra xem xe có thể chứa được không
        max_load_during_service = max(
            current_load,  # Load hiện tại
            current_load + required_delivery,  # Nếu cần pickup thêm từ depot
            current_load + required_delivery - customer.d_demand + additional_pickup  # Sau khi delivery và pickup
        )
        
        return max_load_during_service <= self.capacity
    
    def assign_improved(self, customer):
        """
        Gán khách hàng với logic cải tiến
        """
        travel_time = self.get_travel_time(self.current_location, customer)
        self.time += travel_time
        
        # Chờ nếu đến sớm
        if self.time < customer.tw_start:
            self.time = customer.tw_start
        
        # Xử lý delivery và pickup
        if customer.d_demand > 0:
            # Nếu cần delivery mà không có đủ hàng, pickup từ depot
            current_load = self.capacity - self.remaining
            if current_load < customer.d_demand:
                needed_pickup = customer.d_demand - current_load
                self.remaining -= needed_pickup  # Pickup từ depot
                self.delivery_load += needed_pickup
            
            # Thực hiện delivery
            self.remaining += customer.d_demand
            self.delivery_load -= customer.d_demand
        
        if customer.p_demand > 0:
            # Thực hiện pickup
            self.remaining -= customer.p_demand
            self.pickup_load += customer.p_demand
        
        # Thêm service time
        self.time += customer.service
        
        # Cập nhật route
        self.route.append(customer.cid)
        self.current_location = customer
        customer.assigned = True

def improved_assign_customers_to_vehicles(customers, vehicles):
    """
    Thuật toán cải tiến với nhiều chiến lược
    """
    # Chuyển đổi vehicles thành ImprovedVehicle
    improved_vehicles = []
    for v in vehicles:
        improved_v = ImprovedVehicle(v.id, v.capacity, v.current_location, v.distance_matrix)
        improved_vehicles.append(improved_v)
    
    depot = improved_vehicles[0].current_location
    all_customers = [depot] + customers
    unassigned = [c for c in customers if not c.assigned]
    
    print(f"🔄 Bắt đầu thuật toán cải tiến: {len(unassigned)} khách hàng, {len(improved_vehicles)} xe")
    
    # Chiến lược 1: Ưu tiên khách hàng có time window chặt
    unassigned.sort(key=lambda c: c.tw_end - c.tw_start)
    
    iteration = 0
    max_iterations = 10
    
    while unassigned and iteration < max_iterations:
        iteration += 1
        progress = False
        
        print(f"  Vòng lặp {iteration}: {len(unassigned)} khách hàng còn lại")
        
        # Thử gán từng khách hàng
        for customer in unassigned[:]:
            best_vehicle = None
            best_cost = float('inf')
            
            for vehicle in improved_vehicles:
                if vehicle.can_serve_improved(customer):
                    # Tính cost (khoảng cách + penalty thời gian)
                    travel_time = vehicle.get_travel_time(vehicle.current_location, customer)
                    arrival_time = vehicle.time + travel_time
                    time_penalty = max(0, arrival_time - customer.tw_end) * 10
                    
                    cost = travel_time + time_penalty
                    
                    if cost < best_cost:
                        best_cost = cost
                        best_vehicle = vehicle
            
            if best_vehicle:
                best_vehicle.assign_improved(customer)
                unassigned.remove(customer)
                progress = True
        
        if not progress:
            print(f"  ⚠️ Không thể gán thêm khách hàng nào trong vòng lặp {iteration}")
            break
    
    # Quay về depot
    for vehicle in improved_vehicles:
        if vehicle.route[-1] != 0:
            vehicle.return_to_depot()
    
    # Chuyển đổi lại về Vehicle thông thường để tương thích
    result_vehicles = []
    for improved_v in improved_vehicles:
        original_v = Vehicle(improved_v.id, improved_v.capacity, improved_v.current_location)
        original_v.route = improved_v.route
        original_v.time = improved_v.time
        original_v.remaining = improved_v.remaining
        result_vehicles.append(original_v)
    
    assigned_count = sum(1 for c in customers if c.assigned)
    print(f"✅ Kết quả: {assigned_count}/{len(customers)} khách hàng được gán ({assigned_count/len(customers)*100:.1f}%)")
    
    return result_vehicles

def test_improved_algorithm():
    """Test thuật toán cải tiến với file rdp108"""
    filepath = 'data/vrpspdtw_Wang_Chen/src/rdp108.txt'
    output_file = 'exports/vrpspdtw_Wang_Chen/solution/improved_rdp108.txt'
    
    print("=" * 60)
    print("TEST THUẬT TOÁN CẢI TIẾN VRPSPDTW WANG CHEN")
    print("=" * 60)
    
    # Đọc dữ liệu
    customers, vehicles = read_data(filepath)
    
    if not customers or not vehicles:
        print("❌ Không thể đọc dữ liệu")
        return
    
    print(f"📊 Dữ liệu đầu vào:")
    print(f"   - Khách hàng: {len(customers)}")
    print(f"   - Xe: {len(vehicles)}")
    print(f"   - Capacity: {vehicles[0].capacity}")
    
    # Reset trạng thái assigned
    for customer in customers:
        customer.assigned = False
    
    # Chạy thuật toán cải tiến
    result_vehicles = improved_assign_customers_to_vehicles(customers, vehicles)
    
    # Phân tích kết quả
    assigned_customers = sum(1 for c in customers if c.assigned)
    used_vehicles = sum(1 for v in result_vehicles if len(v.route) > 2)
    
    print(f"\n📈 KẾT QUẢ:")
    print(f"   - Khách hàng được phục vụ: {assigned_customers}/{len(customers)} ({assigned_customers/len(customers)*100:.1f}%)")
    print(f"   - Xe được sử dụng: {used_vehicles}/{len(vehicles)}")
    
    # Lưu kết quả
    os.makedirs(os.path.dirname(output_file), exist_ok=True)
    write_solution(output_file, result_vehicles)
    print(f"   - Kết quả lưu tại: {output_file}")
    
    # So sánh với kết quả gốc
    original_file = 'exports/vrpspdtw_Wang_Chen/solution/rdp108.txt'
    if os.path.exists(original_file):
        with open(original_file, 'r') as f:
            original_routes = [line.strip() for line in f if line.strip() and 'Route' in line]
        
        original_customers = 0
        for route in original_routes:
            # Đếm số khách hàng (loại bỏ depot 0)
            nodes = route.split(':')[1].strip().split()
            original_customers += len([n for n in nodes if n != '0'])
        
        print(f"\n📊 SO SÁNH:")
        print(f"   - Thuật toán gốc: {original_customers} khách hàng")
        print(f"   - Thuật toán cải tiến: {assigned_customers} khách hàng")
        print(f"   - Cải thiện: +{assigned_customers - original_customers} khách hàng")

if __name__ == "__main__":
    test_improved_algorithm()