import math
import os

class Customer:
    def __init__(self, cid, x, y, d_demand, p_demand, ready, due, service):
        self.cid = cid
        self.x = x
        self.y = y
        self.d_demand = d_demand
        self.p_demand = p_demand
        self.tw_start = ready
        self.tw_end = due
        self.service = service
        self.assigned = False

    def distance_to(self, other):
        return math.hypot(self.x - other.x, self.y - other.y)

class Vehicle:
    def __init__(self, id, capacity, current_location):
        self.id = id
        self.capacity = capacity
        self.remaining = capacity
        self.route = [0]  # depot
        self.current_location = current_location  # depot
        self.time = 0

    def can_serve(self, customer):
        travel_time = self.current_location.distance_to(customer)
        arrival_time = self.time + travel_time
        # Kiểm tra nếu xe có thể đến khách hàng trong cửa sổ thời gian
        within_tw = customer.tw_start <= arrival_time <= customer.tw_end
        # Kiểm tra nếu xe có đủ dung lượng để phục vụ khách hàng
        can_serve = self.remaining >= customer.d_demand and within_tw
        return can_serve

    def assign(self, customer):
        travel_time = self.current_location.distance_to(customer)
        self.time += travel_time
        self.time = max(self.time, customer.tw_start)  # Đảm bảo xe đến sau thời gian sẵn sàng
        self.time += customer.service  # Thời gian phục vụ khách hàng
        self.remaining -= customer.d_demand  # Giảm dung lượng của xe
        self.route.append(customer.cid)  # Thêm khách hàng vào tuyến đường
        self.current_location = customer  # Cập nhật vị trí của xe
        customer.assigned = True

    def return_to_depot(self):
        self.route.append(0)  # Quay lại điểm xuất phát (depot)

def assign_customers_to_vehicles(customers, vehicles):
    unassigned = [c for c in customers if not c.assigned]

    while unassigned:
        progress = False
        for vehicle in vehicles:
            best_customer = None
            best_distance = float('inf')
            for customer in unassigned:
                if vehicle.can_serve(customer):
                    dist = vehicle.current_location.distance_to(customer)
                    if dist < best_distance:
                        best_distance = dist
                        best_customer = customer
            if best_customer:
                vehicle.assign(best_customer)
                unassigned.remove(best_customer)
                progress = True
        if not progress:
            break  # Nếu không có tiến bộ, thoát khỏi vòng lặp

    # Quay về depot khi đã hoàn thành các tuyến đường
    for vehicle in vehicles:
        if vehicle.route[-1] != 0:
            vehicle.return_to_depot()

    return vehicles

def read_data(filepath):
    customers = []
    vehicles = []

    with open(filepath, 'r') as f:
        lines = [line.strip() for line in f if line.strip()]

    # Tìm vị trí các section
    for i, line in enumerate(lines):
        if line.startswith('NUMBER') and 'CAPACITY' in line:
            vehicle_info_line = lines[i + 1]
            customer_section_start = i + 3
            break

    num_customers, num_vehicles, capacity = map(int, vehicle_info_line.split())
    # Đọc thông tin khách hàng
    for line in lines[customer_section_start:]:
        parts = line.split()
        if len(parts) != 8:
            continue  # bỏ qua dòng không hợp lệ

        cid = int(parts[0])
        x = float(parts[1])
        y = float(parts[2])
        d_demand = float(parts[3])
        p_demand = float(parts[4])
        ready = float(parts[5])
        due = float(parts[6])
        service = float(parts[7])

        customers.append(Customer(cid, x, y, d_demand, p_demand, ready, due, service))
        
    # Tạo xe
    for i in range(1, num_vehicles + 1):
        vehicles.append(Vehicle(i, capacity, customers[0]))
        
    customers.pop(0)

    return customers, vehicles

def write_solution(filename, vehicles):
    with open(filename, 'w') as f:
        for vehicle in vehicles:
            route_str = ' '.join(map(str, vehicle.route))
            f.write(f"Route {vehicle.id}: {route_str}\n")

# Danh sách khách hàng
src_dir = r"D:\Logistic\new\logistic\src\main\resources\data\spdptw\src"
solution_dir = r"D:\Logistic\new\logistic\src\main\resources\data\spdptw\solution"

for fname in os.listdir(src_dir):
    src_path = os.path.join(src_dir, fname)
    if not os.path.isfile(src_path):
        continue
    customers, vehicles = read_data(src_path)
    vehicles_and_way = assign_customers_to_vehicles(customers, vehicles)
    solution_path = os.path.join(solution_dir, fname)
    write_solution(solution_path, vehicles_and_way)
    print(f"Đã giải xong file {fname}, lưu kết quả vào {solution_path}")