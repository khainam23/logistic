import os
import math

class Customer:
    def __init__(self, idx, x, y, ready_time, due_time, demand):
        self.idx = idx
        self.x = x
        self.y = y
        self.ready_time = ready_time
        self.due_time = due_time
        self.demand = demand
        self.visited = False

    def distance_to(self, other):
        return math.hypot(self.x - other.x, self.y - other.y)

def parse_instance(file_path):
    print(f"  📖 Đọc dữ liệu từ file...")
    with open(file_path, 'r') as f:
        lines = f.readlines()

    capacity = 0
    max_vehicles = 0
    customers = []
    read_nodes = False

    for line in lines:
        if line.startswith("CAPACITY"):
            capacity = float(line.split(":")[1].strip())
        elif line.startswith("VEHICLES"):
            max_vehicles = int(line.split(":")[1].strip())
        elif line.startswith("NODE_SECTION"):
            read_nodes = True
            continue
        elif read_nodes:
            if line.strip() == "DEPOT_SECTION":
                break
            parts = line.strip().split(',')
            if len(parts) < 6:
                continue
            idx = int(parts[0])
            x = float(parts[1])
            y = float(parts[2])
            ready = int(parts[3])
            due = int(parts[4])
            demand = float(parts[5])
            customers.append(Customer(idx, x, y, ready, due, demand))

    # Nếu không có thông tin về số xe tối đa, ước tính dựa trên số khách hàng
    if max_vehicles == 0:
        max_vehicles = len(customers) - 1  # Tối đa bằng số khách hàng
    
    print(f"  ✅ Đã đọc {len(customers)} điểm (bao gồm depot)")
    print(f"  🚛 Sức chứa xe: {capacity}")
    print(f"  🚗 Số xe tối đa: {max_vehicles}")
    print(f"  👥 Số khách hàng cần phục vụ: {len(customers) - 1}")
    
    return capacity, max_vehicles, customers

def greedy_vrpspdtw(capacity, max_vehicles, customers):
    print(f"  🔄 Bắt đầu thuật toán Greedy VRPSPDTW...")
    depot = customers[0]
    routes = []
    current_time = 0
    route_count = 0
    
    # Reset trạng thái visited cho tất cả khách hàng
    for c in customers:
        if c.idx != 0:
            c.visited = False

    while any(not c.visited and c.idx != 0 for c in customers) and route_count < max_vehicles:
        route_count += 1
        print(f"    🚛 Tạo tuyến đường {route_count}/{max_vehicles}...")
        
        route = [0]
        load = 0
        current = depot
        current_time = 0
        customers_in_route = 0
        route_added_customer = False

        while True:
            feasible = []
            for c in customers:
                if c.visited or c.idx == 0:
                    continue
                dist = current.distance_to(c)
                arrival = current_time + dist
                if arrival <= c.due_time and load + c.demand <= capacity:
                    feasible.append((arrival, dist, c))

            if not feasible:
                if customers_in_route == 0:
                    print(f"      ❌ Không có khách hàng nào khả thi cho tuyến {route_count}")
                else:
                    print(f"      ❌ Không còn khách hàng khả thi cho tuyến {route_count}")
                break

            feasible.sort()
            _, travel, next_customer = feasible[0]
            current_time += travel
            if current_time < next_customer.ready_time:
                wait_time = next_customer.ready_time - current_time
                print(f"      ⏰ Chờ đợi {wait_time:.1f} đơn vị thời gian tại khách hàng {next_customer.idx}")
                current_time = next_customer.ready_time
            
            load += next_customer.demand
            next_customer.visited = True
            route.append(next_customer.idx)
            current = next_customer
            customers_in_route += 1
            route_added_customer = True
            
            print(f"      ✅ Thêm khách hàng {next_customer.idx} (tải trọng: {load:.1f}/{capacity}, thời gian: {current_time:.1f})")

        # Chỉ thêm route nếu có ít nhất 1 khách hàng
        if route_added_customer:
            route.append(0)
            routes.append(route)
            print(f"    📋 Tuyến {route_count} hoàn thành với {customers_in_route} khách hàng")
        else:
            print(f"    ⚠️  Tuyến {route_count} bị hủy vì không có khách hàng nào")
            break

    # Kiểm tra khách hàng chưa được phục vụ
    unserved = [c.idx for c in customers if not c.visited and c.idx != 0]
    if unserved:
        print(f"  ⚠️  CẢNH BÁO: {len(unserved)} khách hàng chưa được phục vụ: {unserved}")
        print(f"  🚫 Lý do: Vượt quá số xe tối đa ({max_vehicles}) hoặc không thỏa mãn ràng buộc")
    
    print(f"  ✅ Hoàn thành thuật toán với {len(routes)} tuyến đường")
    return routes

def calculate_route_statistics(routes, customers, capacity):
    """Tính toán thống kê cho các tuyến đường"""
    total_distance = 0
    total_customers_served = 0
    
    for route in routes:
        route_distance = 0
        route_load = 0
        
        for i in range(len(route) - 1):
            current_idx = route[i]
            next_idx = route[i + 1]
            
            current_customer = next(c for c in customers if c.idx == current_idx)
            next_customer = next(c for c in customers if c.idx == next_idx)
            
            route_distance += current_customer.distance_to(next_customer)
            
            if next_idx != 0:  # Không tính depot
                route_load += next_customer.demand
                
        total_distance += route_distance
        total_customers_served += len(route) - 2  # Trừ depot đầu và cuối
    
    return total_distance, total_customers_served

def solve_all_instances(folder_path):
    print("🚀 BẮT ĐẦU XỬ LÝ TẤT CẢ CÁC FILE TRONG THƯ MỤC")
    print("=" * 60)
    
    total_files = len([f for f in os.listdir(folder_path) if f.endswith(".txt")])
    current_file = 0
    
    for filename in os.listdir(folder_path):
        if not filename.endswith(".txt"):
            continue
            
        current_file += 1
        file_path = os.path.join(folder_path, filename)
        
        print(f"\n📂 [{current_file}/{total_files}] Đang xử lý file: {filename}")
        print("-" * 50)
        
        # Đọc dữ liệu
        capacity, max_vehicles, customers = parse_instance(file_path)
        
        # Giải bài toán
        routes = greedy_vrpspdtw(capacity, max_vehicles, customers)
        
        # Tính toán thống kê
        total_distance, total_customers_served = calculate_route_statistics(routes, customers, capacity)
        unserved_customers = [c.idx for c in customers if not c.visited and c.idx != 0]
        
        # In kết quả
        print(f"\n  📊 KẾT QUẢ CHO FILE {filename}:")
        print(f"  🚛 Số xe sử dụng: {len(routes)}/{max_vehicles}")
        print(f"  👥 Tổng khách hàng phục vụ: {total_customers_served}/{len(customers)-1}")
        if unserved_customers:
            print(f"  ⚠️  Khách hàng chưa phục vụ: {len(unserved_customers)} ({unserved_customers})")
        print(f"  📏 Tổng khoảng cách: {total_distance:.2f}")
        if len(routes) > 0:
            print(f"  📈 Trung bình khách hàng/xe: {total_customers_served/len(routes):.1f}")
        else:
            print(f"  📈 Trung bình khách hàng/xe: 0")
        
        print(f"\n  🗺️  CHI TIẾT CÁC TUYẾN ĐƯỜNG:")
        for i, route in enumerate(routes):
            route_customers = len(route) - 2
            route_load = sum(next(c.demand for c in customers if c.idx == idx) 
                           for idx in route if idx != 0)
            print(f"    Route {i+1}: {' → '.join(map(str, route))} "
                  f"({route_customers} khách hàng, tải trọng: {route_load:.1f})")
        
        print("=" * 60)

if __name__ == "__main__":
    folder = r'D:\Logistic\excute_data\logistic\data\vrpspdtw_Liu_Tang_Yao\src'
    solve_all_instances(folder)
