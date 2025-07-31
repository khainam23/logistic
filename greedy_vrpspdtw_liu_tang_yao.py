"""
Thuật toán Greedy cho bài toán VRPSPDTW - Dataset Liu_Tang_Yao
Cải tiến với các chiến lược tối ưu hóa cho dataset lớn
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
        # Sử dụng tên file làm tên instance
        self.name = os.path.splitext(os.path.basename(filepath))[0]
        
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
        """Kiểm tra xe có thể phục vụ khách hàng không"""
        customer = self.instance.customers[customer_id]
        
        # Kiểm tra sức chứa - cải tiến: xem xét tải trọng tối đa trong suốt hành trình
        new_delivery_load = vehicle.current_load_delivery + customer.delivery_demand
        new_pickup_load = vehicle.current_load_pickup + customer.pickup_demand
        
        # Tải trọng tối đa có thể đạt được trong hành trình
        max_load = max(new_delivery_load, new_pickup_load)
        
        if max_load > vehicle.capacity:
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
        """Tính toán chi phí chèn khách hàng - cải tiến với nhiều yếu tố"""
        customer = self.instance.customers[customer_id]
        
        # Chi phí khoảng cách
        distance_cost = (self.instance.get_distance(vehicle.current_location, customer_id) + 
                        self.instance.get_distance(customer_id, self.instance.depot_id) -
                        self.instance.get_distance(vehicle.current_location, self.instance.depot_id))
        
        # Chi phí thời gian
        travel_time = self.instance.get_travel_time(vehicle.current_location, customer_id)
        arrival_time = vehicle.current_time + travel_time
        waiting_time = max(0, customer.ready_time - arrival_time)
        
        # Chi phí urgency - ưu tiên khách hàng có due_date sớm
        urgency_penalty = 1.0 / max(1, customer.due_date - arrival_time)
        
        # Chi phí tải trọng - ưu tiên khách hàng có demand phù hợp
        load_utilization = (customer.delivery_demand + customer.pickup_demand) / vehicle.capacity
        load_bonus = load_utilization * 0.1  # Bonus cho việc sử dụng tải trọng hiệu quả
        
        # Tổng chi phí
        total_cost = (distance_cost * self.instance.unit_cost + 
                     waiting_time * 0.1 + 
                     urgency_penalty * 10 - 
                     load_bonus)
        
        return total_cost
    
    def find_best_insertion(self) -> Tuple[Optional[Vehicle], Optional[int], float]:
        """Tìm vị trí chèn tốt nhất với chiến lược cải tiến"""
        best_vehicle = None
        best_customer = None
        best_cost = float('inf')
        
        # Sắp xếp khách hàng theo độ ưu tiên (due_date sớm trước)
        sorted_customers = sorted(self.unvisited_customers, 
                                key=lambda x: self.instance.customers[x].due_date)
        
        # Bước 1: Tìm khách hàng có thể phục vụ bằng xe hiện có
        for customer_id in sorted_customers:
            for vehicle in self.vehicles:
                if self.can_serve_customer(vehicle, customer_id):
                    cost = self.calculate_insertion_cost(vehicle, customer_id)
                    if cost < best_cost:
                        best_cost = cost
                        best_vehicle = vehicle
                        best_customer = customer_id
        
        # Bước 2: Nếu không tìm được, thử thêm xe mới
        if best_vehicle is None and self.next_vehicle_id < self.instance.num_vehicles:
            new_vehicle = self.add_new_vehicle()
            if new_vehicle is not None:
                # Tìm khách hàng tốt nhất cho xe mới (ưu tiên khách hàng urgent)
                for customer_id in sorted_customers:
                    if self.can_serve_customer(new_vehicle, customer_id):
                        cost = self.calculate_insertion_cost(new_vehicle, customer_id)
                        # Thêm penalty cho việc sử dụng xe mới
                        cost += self.instance.dispatching_cost
                        if cost < best_cost:
                            best_cost = cost
                            best_vehicle = new_vehicle
                            best_customer = customer_id
        
        return best_vehicle, best_customer, best_cost
    
    def assign_customer_to_vehicle(self, vehicle: Vehicle, customer_id: int):
        """Gán khách hàng cho xe"""
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
    
    def calculate_total_cost(self) -> float:
        """Tính tổng chi phí của giải pháp"""
        total_distance = 0
        for route in self.solution_routes:
            for i in range(len(route) - 1):
                total_distance += self.instance.get_distance(route[i], route[i + 1])
        
        total_cost = (self.get_vehicles_used() * self.instance.dispatching_cost + 
                     total_distance * self.instance.unit_cost)
        return total_cost
    
    def solve(self) -> List[List[int]]:
        """Giải bài toán với thuật toán Greedy cải tiến"""
        print(f"Bắt đầu giải {self.instance.name}...")
        start_time = time.time()
        
        self.initialize_vehicles()
        self.solution_routes = []
        
        iteration = 0
        while self.unvisited_customers:
            iteration += 1
            if iteration % 50 == 0:
                print(f"  Đã xử lý {iteration} lần lặp, còn {len(self.unvisited_customers)} khách hàng")
            
            best_vehicle, best_customer, best_cost = self.find_best_insertion()
            
            if best_vehicle is None or best_customer is None:
                print(f"  Không thể phục vụ thêm khách hàng. Còn lại: {len(self.unvisited_customers)}")
                break
            
            self.assign_customer_to_vehicle(best_vehicle, best_customer)
        
        self.finalize_routes()
        
        solve_time = time.time() - start_time
        print(f"  Hoàn thành trong {solve_time:.2f} giây")
        
        return self.solution_routes

def solve_single_instance(filepath: str, output_folder: str = None):
    """Giải một instance và in kết quả theo format yêu cầu"""
    instance = VRPSPDTWInstance()
    instance.load_from_file(filepath)
    
    solver = GreedyVRPSPDTWSolver(instance)
    routes = solver.solve()
    
    # Tạo nội dung output
    output_lines = []
    
    for i, route in enumerate(routes):
        if len(route) > 2:  # Chỉ in route có khách hàng
            route_str = " ".join(map(str, route))
            # Tính chi phí route
            route_distance = 0
            for j in range(len(route) - 1):
                route_distance += instance.get_distance(route[j], route[j + 1])
            route_cost = instance.dispatching_cost + route_distance * instance.unit_cost
            output_lines.append(f"Route {i+1}: {route_str}")
    
    # In ra console
    for line in output_lines:
        print(line)
    print()  # Dòng trống sau mỗi instance
    
    # Lưu vào file riêng nếu có output_folder
    if output_folder:
        try:
            # Đảm bảo thư mục tồn tại
            os.makedirs(output_folder, exist_ok=True)
            
            output_filename = f"{instance.name}.txt"
            output_filepath = os.path.join(output_folder, output_filename)
            
            with open(output_filepath, 'w', encoding='utf-8') as f:
                for line in output_lines:
                    f.write(line + '\n')
            
            print(f"  ✓ Đã lưu kết quả vào: {output_filepath}")
            
        except Exception as e:
            print(f"  ✗ Lỗi khi lưu file: {str(e)}")
            print(f"  Đường dẫn: {output_filepath if 'output_filepath' in locals() else 'N/A'}")

def solve_liu_tang_yao_dataset():
    """Giải tất cả instances trong dataset Liu_Tang_Yao"""
    data_folder = r"d:\Logistic\vrpspdtw\VRPenstein\data"
    liu_tang_yao_folder = os.path.join(data_folder, "Liu_Tang_Yao")
    solution_folder = os.path.join(liu_tang_yao_folder, "solution")
    
    if not os.path.exists(liu_tang_yao_folder):
        print(f"Không tìm thấy thư mục: {liu_tang_yao_folder}")
        return
    
    # Tạo thư mục solution nếu chưa có
    if not os.path.exists(solution_folder):
        os.makedirs(solution_folder)
        print(f"Đã tạo thư mục: {solution_folder}")
    
    # Lấy danh sách file và sắp xếp
    vrpsdptw_files = [f for f in os.listdir(liu_tang_yao_folder) if f.endswith('.vrpsdptw')]
    vrpsdptw_files.sort()
    
    print(f"Bắt đầu giải {len(vrpsdptw_files)} instances Liu Tang Yao...")
    print(f"Kết quả sẽ được lưu vào thư mục: {solution_folder}")
    print("="*80)
    
    total_start_time = time.time()
    
    # Giải từng file
    for i, filename in enumerate(vrpsdptw_files, 1):
        filepath = os.path.join(liu_tang_yao_folder, filename)
        print(f"[{i}/{len(vrpsdptw_files)}] Đang giải {filename}...")
        
        try:
            solve_single_instance(filepath, solution_folder)
        except Exception as e:
            print(f"  ✗ Lỗi khi giải {filename}: {str(e)}")
            import traceback
            traceback.print_exc()
            continue
    
    total_time = time.time() - total_start_time
    print("="*80)
    print(f"Hoàn thành tất cả {len(vrpsdptw_files)} instances trong {total_time:.2f} giây")
    print(f"Thời gian trung bình: {total_time/len(vrpsdptw_files):.2f} giây/instance")

if __name__ == "__main__":
    # Chạy giải tất cả instances
    solve_liu_tang_yao_dataset()
    
    # Hoặc giải một instance cụ thể
    # solve_single_instance(r"d:\Logistic\vrpspdtw\VRPenstein\data\Liu_Tang_Yao\200_1.vrpsdptw")