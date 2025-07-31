"""
Thuật toán Greedy cho bài toán VRPSPDTW - Dataset Wang_Chen
"""

import os
import time
from typing import List, Dict, Tuple, Optional
from dataclasses import dataclass

@dataclass
class Customer:
    id: int
    delivery_demand: float
    pickup_demand: float
    ready_time: int
    due_date: int
    service_time: int

@dataclass
class Vehicle:
    id: int
    capacity: float
    current_load_delivery: float = 0.0
    current_load_pickup: float = 0.0
    current_time: int = 0
    current_location: int = 0
    route: List[int] = None
    
    def __post_init__(self):
        if self.route is None:
            self.route = [0]

class VRPSPDTWInstance:
    def __init__(self):
        self.name = ""
        self.dimension = 0
        self.num_vehicles = 0
        self.dispatching_cost = 0
        self.unit_cost = 1.0
        self.capacity = 0.0
        self.customers: Dict[int, Customer] = {}
        self.distance_matrix: Dict[Tuple[int, int], float] = {}
        self.time_matrix: Dict[Tuple[int, int], float] = {}
        self.depot_id = 0
        
    def load_from_file(self, filepath: str):
        with open(filepath, 'r', encoding='utf-8') as f:
            lines = f.readlines()
        
        i = 0
        while i < len(lines):
            line = lines[i].strip()
            
            if line.startswith('DIMENSION'):
                self.dimension = int(line.split(':')[1].strip())
            elif line.startswith('VEHICLES'):
                self.num_vehicles = int(line.split(':')[1].strip())
            elif line.startswith('DISPATCHINGCOST'):
                self.dispatching_cost = float(line.split(':')[1].strip())
            elif line.startswith('UNITCOST'):
                self.unit_cost = float(line.split(':')[1].strip())
            elif line.startswith('CAPACITY'):
                self.capacity = float(line.split(':')[1].strip())
            elif line.startswith('NODE_SECTION'):
                i += 1
                while i < len(lines) and not lines[i].strip().startswith('DISTANCETIME_SECTION'):
                    node_line = lines[i].strip()
                    if node_line and ',' in node_line:
                        parts = node_line.split(',')
                        if len(parts) >= 6:
                            customer_id = int(parts[0])
                            delivery = float(parts[1])
                            pickup = float(parts[2])
                            ready_time = int(parts[3])
                            due_date = int(parts[4])
                            service_time = int(parts[5])
                            
                            self.customers[customer_id] = Customer(
                                id=customer_id,
                                delivery_demand=delivery,
                                pickup_demand=pickup,
                                ready_time=ready_time,
                                due_date=due_date,
                                service_time=service_time
                            )
                    i += 1
                continue
            elif line.startswith('DISTANCETIME_SECTION'):
                i += 1
                while i < len(lines):
                    dist_line = lines[i].strip()
                    if dist_line and ',' in dist_line:
                        parts = dist_line.split(',')
                        if len(parts) >= 4:
                            from_id = int(parts[0])
                            to_id = int(parts[1])
                            distance = float(parts[2])
                            travel_time = float(parts[3])
                            
                            self.distance_matrix[(from_id, to_id)] = distance
                            self.time_matrix[(from_id, to_id)] = travel_time
                    i += 1
                continue
            elif not line.startswith('EDGE_WEIGHT_TYPE') and line and not line.startswith('TYPE'):
                if not self.name:
                    self.name = line
            
            i += 1

    def get_distance(self, from_id: int, to_id: int) -> float:
        return self.distance_matrix.get((from_id, to_id), float('inf'))
    
    def get_travel_time(self, from_id: int, to_id: int) -> float:
        return self.time_matrix.get((from_id, to_id), float('inf'))

class GreedyVRPSPDTWSolver:
    def __init__(self, instance: VRPSPDTWInstance):
        self.instance = instance
        self.vehicles: List[Vehicle] = []
        self.unvisited_customers = set()
        self.solution_routes = []
        self.next_vehicle_id = 0
        
    def initialize_vehicles(self):
        """Khởi tạo với một xe đầu tiên, các xe khác sẽ được thêm khi cần"""
        self.vehicles = []
        self.next_vehicle_id = 0
        
        # Bắt đầu với một xe đầu tiên
        if self.instance.num_vehicles > 0:
            vehicle = Vehicle(id=self.next_vehicle_id, capacity=self.instance.capacity)
            self.vehicles.append(vehicle)
            self.next_vehicle_id += 1
        
        self.unvisited_customers = set(self.instance.customers.keys())
        self.unvisited_customers.discard(self.instance.depot_id)
    
    def add_new_vehicle(self) -> Optional[Vehicle]:
        """Thêm xe mới nếu còn xe khả dụng"""
        if self.next_vehicle_id < self.instance.num_vehicles:
            vehicle = Vehicle(id=self.next_vehicle_id, capacity=self.instance.capacity)
            self.vehicles.append(vehicle)
            self.next_vehicle_id += 1
            return vehicle
        return None
    
    def can_serve_customer(self, vehicle: Vehicle, customer_id: int) -> bool:
        customer = self.instance.customers[customer_id]
        
        # Kiểm tra sức chứa
        new_delivery_load = vehicle.current_load_delivery + customer.delivery_demand
        new_pickup_load = vehicle.current_load_pickup + customer.pickup_demand
        
        if new_delivery_load > vehicle.capacity or new_pickup_load > vehicle.capacity:
            return False
        
        # Kiểm tra thời gian
        travel_time = self.instance.get_travel_time(vehicle.current_location, customer_id)
        arrival_time = vehicle.current_time + travel_time
        
        if arrival_time > customer.due_date:
            return False
        
        # Kiểm tra có thể về depot
        service_start_time = max(arrival_time, customer.ready_time)
        departure_time = service_start_time + customer.service_time
        return_time = departure_time + self.instance.get_travel_time(customer_id, self.instance.depot_id)
        
        depot = self.instance.customers[self.instance.depot_id]
        if return_time > depot.due_date:
            return False
        
        return True
    
    def calculate_insertion_cost(self, vehicle: Vehicle, customer_id: int) -> float:
        customer = self.instance.customers[customer_id]
        
        # Chi phí khoảng cách cơ bản
        distance_cost = (self.instance.get_distance(vehicle.current_location, customer_id) + 
                        self.instance.get_distance(customer_id, self.instance.depot_id) -
                        self.instance.get_distance(vehicle.current_location, self.instance.depot_id))
        
        # Chi phí thời gian
        travel_time = self.instance.get_travel_time(vehicle.current_location, customer_id)
        arrival_time = vehicle.current_time + travel_time
        waiting_time = max(0, customer.ready_time - arrival_time)
        
        # Tính toán urgency - ưu tiên khách hàng có thời gian chặt chẽ
        urgency_factor = 0
        if customer.due_date > 0:
            time_window = customer.due_date - customer.ready_time
            if time_window > 0:
                # Khách hàng có time window nhỏ sẽ được ưu tiên
                urgency_factor = 1000.0 / time_window
        
        # Tính toán capacity utilization - ưu tiên khách hàng có demand cao
        total_demand = customer.delivery_demand + customer.pickup_demand
        capacity_factor = 0
        if total_demand > 0:
            # Bonus cho khách hàng có demand cao (giảm chi phí)
            capacity_factor = -total_demand * 5
        
        # Tính toán time slack - ưu tiên khách hàng có ít thời gian dư
        time_slack_factor = 0
        if customer.due_date > arrival_time:
            time_slack = customer.due_date - arrival_time
            # Khách hàng có ít thời gian dư sẽ được ưu tiên
            time_slack_factor = 100.0 / (time_slack + 1)
        
        # Tổng chi phí
        total_cost = (distance_cost * self.instance.unit_cost + 
                     waiting_time * 0.1 + 
                     urgency_factor * 0.3 + 
                     capacity_factor + 
                     time_slack_factor * 0.2)
        
        return total_cost
    
    def find_best_insertion(self) -> Tuple[Optional[Vehicle], Optional[int], float]:
        """Tìm vị trí chèn tốt nhất với chiến lược cải tiến"""
        best_vehicle = None
        best_customer = None
        best_cost = float('inf')
        
        # Tạo danh sách tất cả các lựa chọn khả thi
        feasible_options = []
        
        # Bước 1: Đánh giá tất cả xe hiện có
        for customer_id in self.unvisited_customers:
            for vehicle in self.vehicles:
                if self.can_serve_customer(vehicle, customer_id):
                    cost = self.calculate_insertion_cost(vehicle, customer_id)
                    feasible_options.append((cost, vehicle, customer_id, False))  # False = xe hiện có
        
        # Bước 2: Đánh giá khả năng sử dụng xe mới
        if self.next_vehicle_id < self.instance.num_vehicles:
            # Tạo xe tạm thời để đánh giá
            temp_vehicle = Vehicle(id=self.next_vehicle_id, capacity=self.instance.capacity)
            
            for customer_id in self.unvisited_customers:
                if self.can_serve_customer(temp_vehicle, customer_id):
                    cost = self.calculate_insertion_cost(temp_vehicle, customer_id)
                    # Thêm chi phí dispatching cho xe mới
                    cost += self.instance.dispatching_cost
                    feasible_options.append((cost, temp_vehicle, customer_id, True))  # True = xe mới
        
        # Sắp xếp và chọn lựa chọn tốt nhất
        if feasible_options:
            feasible_options.sort(key=lambda x: x[0])
            best_cost, best_vehicle, best_customer, is_new_vehicle = feasible_options[0]
            
            # Nếu chọn xe mới, thực sự thêm vào danh sách
            if is_new_vehicle:
                best_vehicle = self.add_new_vehicle()
        
        return best_vehicle, best_customer, best_cost
    
    def assign_customer_to_vehicle(self, vehicle: Vehicle, customer_id: int):
        customer = self.instance.customers[customer_id]
        
        travel_time = self.instance.get_travel_time(vehicle.current_location, customer_id)
        arrival_time = vehicle.current_time + travel_time
        service_start_time = max(arrival_time, customer.ready_time)
        departure_time = service_start_time + customer.service_time
        
        vehicle.current_time = departure_time
        vehicle.current_location = customer_id
        vehicle.current_load_delivery += customer.delivery_demand
        vehicle.current_load_pickup += customer.pickup_demand
        vehicle.route.append(customer_id)
        
        self.unvisited_customers.remove(customer_id)
    
    def finalize_routes(self):
        """Hoàn thiện các route và chỉ giữ lại xe có phục vụ khách hàng"""
        self.solution_routes = []
        for vehicle in self.vehicles:
            if len(vehicle.route) > 1:  # Xe có phục vụ ít nhất 1 khách hàng
                return_time = self.instance.get_travel_time(vehicle.current_location, self.instance.depot_id)
                vehicle.route.append(self.instance.depot_id)
                vehicle.current_time += return_time
                vehicle.current_location = self.instance.depot_id
                self.solution_routes.append(vehicle.route.copy())
    
    def get_vehicles_used(self) -> int:
        """Trả về số xe thực sự được sử dụng"""
        return len(self.solution_routes)
    
    def get_customer_difficulty(self, customer_id: int) -> float:
        """Tính toán độ khó phục vụ của khách hàng"""
        customer = self.instance.customers[customer_id]
        
        # Time window tightness
        time_window = customer.due_date - customer.ready_time
        time_difficulty = 1000.0 / (time_window + 1) if time_window > 0 else 1000.0
        
        # Demand size
        total_demand = customer.delivery_demand + customer.pickup_demand
        demand_difficulty = total_demand
        
        # Distance from depot
        distance_from_depot = self.instance.get_distance(self.instance.depot_id, customer_id)
        distance_difficulty = distance_from_depot
        
        return time_difficulty + demand_difficulty * 10 + distance_difficulty * 0.1
    
    def solve_with_priority(self) -> List[List[int]]:
        """Giải bài toán với ưu tiên khách hàng khó phục vụ"""
        self.initialize_vehicles()
        self.solution_routes = []
        
        # Sắp xếp khách hàng theo độ khó phục vụ (khó nhất trước)
        customers_by_difficulty = list(self.unvisited_customers)
        customers_by_difficulty.sort(key=self.get_customer_difficulty, reverse=True)
        
        # Thử phục vụ theo thứ tự ưu tiên
        for customer_id in customers_by_difficulty:
            if customer_id not in self.unvisited_customers:
                continue
                
            best_vehicle = None
            best_cost = float('inf')
            
            # Tìm xe tốt nhất cho khách hàng này
            for vehicle in self.vehicles:
                if self.can_serve_customer(vehicle, customer_id):
                    cost = self.calculate_insertion_cost(vehicle, customer_id)
                    if cost < best_cost:
                        best_cost = cost
                        best_vehicle = vehicle
            
            # Nếu không tìm được xe phù hợp, thử thêm xe mới
            if best_vehicle is None and self.next_vehicle_id < self.instance.num_vehicles:
                new_vehicle = self.add_new_vehicle()
                if new_vehicle and self.can_serve_customer(new_vehicle, customer_id):
                    best_vehicle = new_vehicle
            
            # Gán khách hàng nếu tìm được xe
            if best_vehicle is not None:
                self.assign_customer_to_vehicle(best_vehicle, customer_id)
        
        # Sau khi xử lý khách hàng khó, tiếp tục với thuật toán greedy thông thường
        iteration = 0
        max_iterations = len(self.instance.customers) * 2
        
        while self.unvisited_customers and iteration < max_iterations:
            best_vehicle, best_customer, best_cost = self.find_best_insertion()
            
            if best_vehicle is None or best_customer is None:
                break
            
            self.assign_customer_to_vehicle(best_vehicle, best_customer)
            iteration += 1
        
        self.finalize_routes()
        return self.solution_routes
    
    def solve(self) -> List[List[int]]:
        # Thử cả hai phương pháp và chọn kết quả tốt hơn
        
        # Phương pháp 1: Greedy thông thường
        original_unvisited = self.unvisited_customers.copy()
        self.initialize_vehicles()
        self.solution_routes = []
        
        iteration = 0
        max_iterations = len(self.instance.customers) * 2
        
        while self.unvisited_customers and iteration < max_iterations:
            best_vehicle, best_customer, best_cost = self.find_best_insertion()
            
            if best_vehicle is None or best_customer is None:
                break
            
            self.assign_customer_to_vehicle(best_vehicle, best_customer)
            iteration += 1
        
        self.finalize_routes()
        greedy_routes = self.solution_routes.copy()
        greedy_served = sum(len(route) - 2 for route in greedy_routes)
        
        # Phương pháp 2: Priority-based
        self.unvisited_customers = original_unvisited.copy()
        priority_routes = self.solve_with_priority()
        priority_served = sum(len(route) - 2 for route in priority_routes)
        
        # Chọn kết quả tốt hơn (phục vụ được nhiều khách hàng hơn)
        if priority_served > greedy_served:
            self.solution_routes = priority_routes
            return priority_routes
        else:
            self.solution_routes = greedy_routes
            return greedy_routes

def solve_single_instance(filepath: str, output_folder: str = None):
    """Giải một instance và in kết quả theo format yêu cầu"""
    instance = VRPSPDTWInstance()
    instance.load_from_file(filepath)
    
    solver = GreedyVRPSPDTWSolver(instance)
    routes = solver.solve()
    
    # Thống kê
    vehicles_used = solver.get_vehicles_used()
    total_customers = len(instance.customers) - 1  # Trừ depot
    served_customers = sum(len(route) - 2 for route in routes)  # Trừ depot đầu và cuối
    
    # Tạo nội dung output
    output_lines = []
    output_lines.append(f"{instance.name}")
    output_lines.append(f"Vehicles used: {vehicles_used}/{instance.num_vehicles}")
    output_lines.append(f"Customers served: {served_customers}/{total_customers}")
    
    for i, route in enumerate(routes):
        if len(route) > 2:  # Chỉ in route có khách hàng
            route_str = " ".join(map(str, route))
            output_lines.append(f"Route {i+1}: {route_str}")
    
    # In ra console
    for line in output_lines:
        print(line)
    print()  # Dòng trống sau mỗi instance
    
    # Lưu vào file riêng nếu có output_folder
    if output_folder:
        output_filename = f"{instance.name}.txt"
        output_filepath = os.path.join(output_folder, output_filename)
        
        with open(output_filepath, 'w', encoding='utf-8') as f:
            for line in output_lines:
                f.write(line + '\n')

def solve_wang_chen_dataset():
    """Giải tất cả instances trong dataset Wang_Chen"""
    data_folder = r"d:\Logistic\vrpspdtw\VRPenstein\data"
    wang_chen_folder = os.path.join(data_folder, "Wang_Chen")
    solution_folder = os.path.join(wang_chen_folder, "solution")
    
    if not os.path.exists(wang_chen_folder):
        print(f"Không tìm thấy thư mục: {wang_chen_folder}")
        return
    
    # Tạo thư mục solution nếu chưa có
    if not os.path.exists(solution_folder):
        os.makedirs(solution_folder)
        print(f"Đã tạo thư mục: {solution_folder}")
    
    # Lấy danh sách file và sắp xếp
    vrpsdptw_files = [f for f in os.listdir(wang_chen_folder) if f.endswith('.vrpsdptw')]
    vrpsdptw_files.sort()
    
    print(f"Bắt đầu giải {len(vrpsdptw_files)} instances...")
    print(f"Kết quả sẽ được lưu vào thư mục: {solution_folder}")
    print("="*60)
    
    # Giải từng file
    for filename in vrpsdptw_files:
        filepath = os.path.join(wang_chen_folder, filename)
        try:
            solve_single_instance(filepath, solution_folder)
        except Exception as e:
            print(f"Lỗi khi giải {filename}: {str(e)}")
            continue
    
    print("="*60)
    print(f"Hoàn thành! Tất cả kết quả đã được lưu vào: {solution_folder}")

if __name__ == "__main__":
    solve_wang_chen_dataset()