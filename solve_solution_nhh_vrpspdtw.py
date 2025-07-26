#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Giải thuật tối ưu hóa cho bài toán VRPSPDTW (Vehicle Routing Problem with Simultaneous Pickup and Delivery and Time Windows)
Sử dụng thuật toán Greedy với các ràng buộc nghiêm ngặt
Định dạng file mới với header và sections
Nhập đường dẫn thư mục từ người dùng

Định dạng file được hỗ trợ:
--- HEADER ---
NAME, TYPE, DIMENSION, VEHICLES, CAPACITY, COST,...
--- NODE_SECTION ---
<ID>, x, y, earliestTime, latestTime, demand
--- DISTANCETIME_SECTION ---
<From>, <To>, <Distance>, <Time>
--- DEPOT_SECTION ---
ID Depot (thường là 0)
"""

import math
import os
import time

class Customer:
    def __init__(self, cid, x, y, d_demand, p_demand, ready, due, service):
        self.cid = cid
        self.x = x
        self.y = y
        self.d_demand = d_demand  # Delivery demand
        self.p_demand = p_demand  # Pickup demand
        self.tw_start = ready     # Thời gian sớm nhất có thể phục vụ
        self.tw_end = due         # Thời gian muộn nhất có thể phục vụ
        self.service = service    # Thời gian phục vụ
        self.assigned = False     # Đã được gán cho xe chưa

    def distance_to(self, other):
        """Tính khoảng cách Euclidean đến khách hàng khác"""
        return math.hypot(self.x - other.x, self.y - other.y)

class Vehicle:
    def __init__(self, id, capacity, current_location, distance_matrix=None):
        self.id = id
        self.capacity = capacity
        self.remaining = capacity
        self.route = [0]  # Bắt đầu từ depot (ID = 0)
        self.current_location = current_location  # Vị trí hiện tại (depot ban đầu)
        self.time = 0  # Thời gian hiện tại
        self.distance_matrix = distance_matrix  # Ma trận khoảng cách tùy chỉnh

    def get_travel_time(self, customer):
        """Tính thời gian di chuyển đến khách hàng"""
        if self.distance_matrix and (self.current_location.cid, customer.cid) in self.distance_matrix:
            # Sử dụng ma trận khoảng cách nếu có
            return self.distance_matrix[(self.current_location.cid, customer.cid)]['time']
        else:
            # Sử dụng khoảng cách Euclidean
            return self.current_location.distance_to(customer)

    def can_serve(self, customer):
        """Kiểm tra xe có thể phục vụ khách hàng này không"""
        # Kiểm tra khách hàng đã được phục vụ chưa
        if customer.assigned:
            return False
            
        travel_time = self.get_travel_time(customer)
        arrival_time = self.time + travel_time
        
        # Kiểm tra cửa sổ thời gian nghiêm ngặt
        if arrival_time > customer.tw_end:
            return False
            
        # Kiểm tra dung lượng xe cho cả delivery và pickup
        # Đối với VRPSPDTW, xe phải có đủ chỗ cho cả hai loại dịch vụ
        capacity_needed = max(customer.d_demand, customer.p_demand)
        if self.remaining < capacity_needed:
            return False
            
        return True

    def assign(self, customer):
        """Gán khách hàng cho xe"""
        # Kiểm tra lại trước khi gán để đảm bảo an toàn
        if customer.assigned or not self.can_serve(customer):
            return False
            
        travel_time = self.get_travel_time(customer)
        self.time += travel_time
        self.time = max(self.time, customer.tw_start)  # Đảm bảo xe đến sau thời gian sẵn sàng
        self.time += customer.service  # Thời gian phục vụ khách hàng
        
        # Cập nhật dung lượng xe dựa trên loại dịch vụ
        # Trong VRPSPDTW, cả delivery và pickup có thể xảy ra đồng thời
        if customer.d_demand > 0:  # Delivery - giao hàng, tăng chỗ trống
            self.remaining += customer.d_demand
        if customer.p_demand > 0:  # Pickup - nhận hàng, giảm chỗ trống
            self.remaining -= customer.p_demand
            
        self.route.append(customer.cid)  # Thêm khách hàng vào tuyến đường
        self.current_location = customer  # Cập nhật vị trí của xe
        customer.assigned = True  # Đánh dấu khách hàng đã được phục vụ
        return True

    def return_to_depot(self):
        """Quay về depot"""
        self.route.append(0)  # Quay lại điểm xuất phát (depot)

def assign_customers_to_vehicles(customers, vehicles):
    """
    Thuật toán Greedy tối ưu hóa số xe sử dụng
    Ưu tiên sử dụng ít xe nhất có thể
    """
    # Reset trạng thái assigned cho tất cả khách hàng
    for customer in customers:
        customer.assigned = False
    
    # Sắp xếp khách hàng theo độ ưu tiên:
    # 1. Thời gian kết thúc sớm nhất (tw_end)
    # 2. Cửa sổ thời gian hẹp nhất (tw_end - tw_start)
    # 3. Demand lớn nhất (ưu tiên khách hàng khó phục vụ)
    customers_sorted = sorted(customers, key=lambda c: (
        c.tw_end, 
        c.tw_end - c.tw_start, 
        -max(c.d_demand, c.p_demand)
    ))
    
    # Chỉ số xe hiện tại đang được sử dụng
    current_vehicle_index = 0
    max_attempts_per_customer = 3
    
    for customer in customers_sorted:
        if customer.assigned:
            continue
            
        assigned = False
        attempts = 0
        
        # BƯỚC 1: Thử gán cho xe đang hoạt động trước (từ xe đầu tiên)
        while not assigned and attempts < max_attempts_per_customer and current_vehicle_index < len(vehicles):
            for i in range(current_vehicle_index + 1):  # Thử từ xe đầu tiên đến xe hiện tại
                if i >= len(vehicles):
                    break
                    
                vehicle = vehicles[i]
                
                # Chỉ xét xe đang hoạt động hoặc xe đầu tiên chưa dùng
                if (len(vehicle.route) > 1 and vehicle.route[-1] != 0) or (len(vehicle.route) == 1 and i == current_vehicle_index):
                    if vehicle.can_serve(customer):
                        if vehicle.assign(customer):
                            assigned = True
                            # Nếu đây là xe mới được kích hoạt, tăng chỉ số
                            if len(vehicle.route) == 2:  # Vừa gán khách hàng đầu tiên
                                current_vehicle_index = max(current_vehicle_index, i)
                            break
            
            attempts += 1
            
            # BƯỚC 2: Nếu không gán được cho xe hiện tại, thử xe mới (chỉ khi thực sự cần)
            if not assigned and current_vehicle_index + 1 < len(vehicles):
                next_vehicle = vehicles[current_vehicle_index + 1]
                if len(next_vehicle.route) == 1 and next_vehicle.can_serve(customer):
                    if next_vehicle.assign(customer):
                        current_vehicle_index += 1
                        assigned = True
                        break
        
        # BƯỚC 3: Thử lần cuối với tất cả xe có thể (backup plan)
        if not assigned:
            for vehicle in vehicles:
                if (len(vehicle.route) == 1 or vehicle.route[-1] != 0) and vehicle.can_serve(customer):
                    if vehicle.assign(customer):
                        assigned = True
                        break
    
    # Thống kê và tối ưu hóa thêm
    unassigned_count = sum(1 for c in customers if not c.assigned)
    if unassigned_count > 0:
        print(f"⚠️  Còn {unassigned_count} khách hàng chưa được phục vụ")
        
        # Thử một lần nữa với chiến lược khác
        remaining_customers = [c for c in customers if not c.assigned]
        remaining_customers.sort(key=lambda c: (c.tw_start, c.tw_end))  # Sắp xếp theo thời gian bắt đầu
        
        for customer in remaining_customers:
            for vehicle in vehicles:
                if (len(vehicle.route) == 1 or vehicle.route[-1] != 0) and vehicle.can_serve(customer):
                    if vehicle.assign(customer):
                        break

    # Quay về depot cho tất cả xe đang hoạt động
    for vehicle in vehicles:
        if len(vehicle.route) > 1 and vehicle.route[-1] != 0:
            vehicle.return_to_depot()

    return vehicles

def read_vrpspdtw_format(lines):
    """Đọc định dạng VRPSPDTW mới với header và sections"""
    customers = []
    vehicles = []
    
    # Biến để lưu thông tin
    capacity = 0
    num_vehicles = 50  # Mặc định, có thể điều chỉnh
    dimension = 0
    cost = 0
    distance_matrix = {}
    problem_name = "Unknown"
    problem_type = "VRPSDPTW"
    depot_id = 0  # ID của depot
    
    # Phân tích header để lấy thông tin cơ bản
    header_section = True
    node_section = False
    distance_section = False
    depot_section = False
    
    nodes_data = []
    
    for line in lines:
        line = line.strip()
        if not line:
            continue
            
        # Xử lý header section
        if header_section:
            if line.startswith('NAME'):
                problem_name = line.split(':', 1)[1].strip() if ':' in line else line.split(',')[0].strip()
            elif line.startswith('TYPE'):
                problem_type = line.split(':', 1)[1].strip() if ':' in line else line.split(',')[1].strip()
            elif line.startswith('DIMENSION'):
                dimension = int(line.split(':', 1)[1].strip() if ':' in line else line.split(',')[2].strip())
            elif line.startswith('VEHICLES'):
                num_vehicles = int(line.split(':', 1)[1].strip() if ':' in line else line.split(',')[3].strip())
            elif line.startswith('CAPACITY'):
                capacity = int(float(line.split(':', 1)[1].strip() if ':' in line else line.split(',')[4].strip()))
            elif line.startswith('COST'):
                cost = float(line.split(':', 1)[1].strip() if ':' in line else line.split(',')[5].strip())
            elif line.startswith('NODE_SECTION'):
                header_section = False
                node_section = True
                continue
        
        # Xử lý node section
        elif node_section:
            if line.startswith('DISTANCETIME_SECTION'):
                node_section = False
                distance_section = True
                continue
            elif line.startswith('DEPOT_SECTION'):
                node_section = False
                depot_section = True
                continue
            else:
                # Đọc dữ liệu node: <ID>, x, y, earliestTime, latestTime, demand
                parts = [p.strip() for p in line.split(',')]
                if len(parts) >= 6:
                    try:
                        node_id = int(parts[0])
                        x = float(parts[1])
                        y = float(parts[2])
                        ready = float(parts[3])  # earliest time
                        due = float(parts[4])    # latest time
                        demand = float(parts[5]) # demand (có thể âm hoặc dương)
                        service = 0  # Mặc định thời gian phục vụ
                        
                        nodes_data.append({
                            'id': node_id,
                            'x': x,
                            'y': y,
                            'ready': ready,
                            'due': due,
                            'demand': demand,
                            'service': service
                        })
                    except ValueError:
                        continue
        
        # Xử lý distance section (tùy chọn - có thể bỏ qua nếu tính toán Euclidean)
        elif distance_section:
            if line.startswith('DEPOT_SECTION'):
                distance_section = False
                depot_section = True
                continue
            else:
                # Đọc ma trận khoảng cách: <From>, <To>, <Distance>, <Time>
                parts = [p.strip() for p in line.split(',')]
                if len(parts) >= 4:
                    try:
                        from_node = int(parts[0])
                        to_node = int(parts[1])
                        distance = float(parts[2])
                        travel_time = float(parts[3])
                        
                        # Lưu vào ma trận khoảng cách
                        distance_matrix[(from_node, to_node)] = {
                            'distance': distance,
                            'time': travel_time
                        }
                    except ValueError:
                        continue
        
        # Xử lý depot section
        elif depot_section:
            # Đọc ID depot
            try:
                depot_id = int(line.strip())
            except ValueError:
                # Nếu không đọc được, giữ mặc định là 0
                depot_id = 0
    
    # Tạo đối tượng Customer từ nodes_data
    if not nodes_data:
        print("❌ Không tìm thấy dữ liệu node trong file VRPSPDTW")
        return [], []
    
    # Sắp xếp nodes theo ID
    nodes_data.sort(key=lambda x: x['id'])
    
    # Tìm depot dựa trên depot_id
    depot = None
    for node in nodes_data:
        # Xử lý demand: âm = delivery, dương = pickup, 0 = depot
        if node['demand'] < 0:
            d_demand = abs(node['demand'])
            p_demand = 0
        elif node['demand'] > 0:
            d_demand = 0
            p_demand = node['demand']
        else:
            d_demand = 0
            p_demand = 0
        
        customer = Customer(
            cid=node['id'],
            x=node['x'],
            y=node['y'],
            d_demand=d_demand,
            p_demand=p_demand,
            ready=node['ready'],
            due=node['due'],
            service=node['service']
        )
        
        # Kiểm tra xem có phải depot không
        if node['id'] == depot_id:
            depot = customer
        
        customers.append(customer)
    
    # Kiểm tra capacity và vehicles
    if capacity == 0:
        capacity = 100  # Giá trị mặc định
        print(f"⚠️  Không tìm thấy CAPACITY, sử dụng giá trị mặc định: {capacity}")
    
    if num_vehicles == 0:
        num_vehicles = 50  # Giá trị mặc định
        print(f"⚠️  Không tìm thấy VEHICLES, sử dụng giá trị mặc định: {num_vehicles}")
    
    # Tạo xe với depot được xác định
    if depot is None:
        # Nếu không tìm thấy depot theo ID, sử dụng node đầu tiên
        depot = customers[0] if customers else None
        print(f"⚠️  Không tìm thấy depot với ID {depot_id}, sử dụng node đầu tiên làm depot")
    
    if depot:
        for i in range(1, num_vehicles + 1):
            vehicles.append(Vehicle(i, capacity, depot, distance_matrix))
        
        # Loại bỏ depot khỏi danh sách khách hàng
        customers = [c for c in customers if c.cid != depot_id]
    
    print(f"✅ Đọc thành công định dạng VRPSPDTW:")
    print(f"   - Tên bài toán: {problem_name}")
    print(f"   - Loại bài toán: {problem_type}")
    print(f"   - Số chiều (DIMENSION): {dimension}")
    print(f"   - Số xe (VEHICLES): {num_vehicles}")
    print(f"   - Dung lượng xe (CAPACITY): {capacity}")
    print(f"   - Chi phí (COST): {cost}")
    print(f"   - ID Depot: {depot_id}")
    print(f"   - Số khách hàng: {len(customers)}")
    print(f"   - Ma trận khoảng cách: {len(distance_matrix)} cặp")
    
    return customers, vehicles

def read_data(filepath):
    """Hàm đọc dữ liệu cho định dạng VRPSPDTW"""
    try:
        with open(filepath, 'r', encoding='utf-8') as f:
            lines = [line.strip() for line in f if line.strip()]
        
        if not lines:
            print(f"File {filepath} trống hoặc không đọc được")
            return [], []
        
        # Kiểm tra định dạng VRPSPDTW
        is_vrpspdtw = False
        
        # Nếu file có đuôi .vrpsdptw thì mặc định là VRPSPDTW
        if filepath.endswith('.vrpsdptw'):
            is_vrpspdtw = True
        else:
            # Kiểm tra nội dung file
            for line in lines:
                if 'TYPE' in line and 'VRPSDPTW' in line:
                    is_vrpspdtw = True
                    break
                if 'NODE_SECTION' in line or 'DISTANCETIME_SECTION' in line or 'DEPOT_SECTION' in line:
                    is_vrpspdtw = True
                    break
        
        if not is_vrpspdtw:
            print(f"❌ File {filepath} không phải định dạng VRPSPDTW")
            return [], []
        
        print(f"Phát hiện định dạng file: VRPSPDTW")
        return read_vrpspdtw_format(lines)
                
    except Exception as e:
        print(f"Lỗi khi đọc file {filepath}: {str(e)}")
        return [], []

def write_solution(filename, vehicles):
    """Ghi kết quả giải thuật ra file"""
    with open(filename, 'w', encoding='utf-8') as f:
        for vehicle in vehicles:
            if len(vehicle.route) > 2:  # Chỉ ghi xe có phục vụ khách hàng
                route_str = ' '.join(map(str, vehicle.route))
                f.write(f"Route {vehicle.id}: {route_str}\n")

def print_file_info(customers, vehicles, filename):
    """In thông tin về file vừa xử lý"""
    print(f"  📁 File: {os.path.basename(filename)}")
    print(f"  👥 Khách hàng: {len(customers)}")
    print(f"  🚛 Xe có sẵn: {len(vehicles)}")
    if vehicles:
        depot = vehicles[0].current_location
        print(f"  📍 Depot: ({depot.x}, {depot.y})")
        print(f"  💰 Dung lượng xe: {vehicles[0].capacity}")
    print(f"  ⏰ Thời gian xử lý: ", end="")

def process_directory(src_dir, solution_dir):
    """Xử lý tất cả file VRPSPDTW trong một thư mục"""
    if not os.path.exists(src_dir):
        print(f"❌ Thư mục {src_dir} không tồn tại")
        return
    
    # Tạo thư mục solution nếu chưa có
    os.makedirs(solution_dir, exist_ok=True)
    
    processed_count = 0
    error_count = 0
    
    # Tìm tất cả file có phần mở rộng phù hợp
    supported_extensions = ['.txt', '.vrpsdptw']
    all_files = []
    
    for ext in supported_extensions:
        files_with_ext = [f for f in os.listdir(src_dir) if f.endswith(ext) and os.path.isfile(os.path.join(src_dir, f))]
        all_files.extend(files_with_ext)
    
    if not all_files:
        print(f"❌ Không tìm thấy file VRPSPDTW nào (.txt, .vrpsdptw) trong thư mục {src_dir}")
        return
    
    print(f"📂 Tìm thấy {len(all_files)} file VRPSPDTW")
    
    for i, fname in enumerate(all_files, 1):
        src_path = os.path.join(src_dir, fname)
        
        print(f"\n[{i}/{len(all_files)}] 🔄 Đang xử lý...")
        
        start_time = time.time()
        
        customers, vehicles = read_data(src_path)
        
        if not customers or not vehicles:
            print(f"❌ Không thể đọc dữ liệu từ file {fname}")
            error_count += 1
            continue
        
        # In thông tin file
        print_file_info(customers, vehicles, fname)
        
        # Giải thuật
        vehicles_result = assign_customers_to_vehicles(customers, vehicles)
        
        # Lưu kết quả
        solution_path = os.path.join(solution_dir, fname)
        write_solution(solution_path, vehicles_result)
        
        # Tính thời gian xử lý
        processing_time = time.time() - start_time
        print(f"{processing_time:.2f}s")
        
        # Thống kê kết quả chi tiết
        used_vehicles = sum(1 for v in vehicles_result if len(v.route) > 2)
        assigned_customers = sum(1 for c in customers if c.assigned)
        unassigned_customers = len(customers) - assigned_customers
        
        # Thống kê theo loại khách hàng
        delivery_customers = sum(1 for c in customers if c.d_demand > 0 and c.assigned)
        pickup_customers = sum(1 for c in customers if c.p_demand > 0 and c.assigned)
        
        # Tính các chỉ số tối ưu hóa
        customers_per_vehicle = assigned_customers / max(used_vehicles, 1)
        vehicle_efficiency = (used_vehicles / len(vehicles)) * 100
        service_rate = (assigned_customers / len(customers)) * 100
        
        print(f"  ✅ Kết quả tối ưu hóa:")
        print(f"     - 🚛 Xe sử dụng: {used_vehicles}/{len(vehicles)} ({vehicle_efficiency:.1f}%) - MỤC TIÊU: ÍT NHẤT")
        print(f"     - 👥 Khách hàng phục vụ: {assigned_customers}/{len(customers)} ({service_rate:.1f}%)")
        if unassigned_customers > 0:
            print(f"     - ⚠️  Chưa phục vụ: {unassigned_customers} khách hàng")
        print(f"     - 📊 Hiệu quả: {customers_per_vehicle:.1f} khách hàng/xe")
        print(f"     - 📦 Phân loại: Giao hàng={delivery_customers}, Nhận hàng={pickup_customers}")
        print(f"     - 💾 File: {os.path.basename(solution_path)}")
        
        # Hiển thị chi tiết từng xe
        if used_vehicles > 0:
            print(f"     - 🔍 Chi tiết từng xe:")
            total_capacity_used = 0
            total_capacity_available = 0
            
            for v in vehicles_result:
                if len(v.route) > 2:
                    customers_in_route = len(v.route) - 2  # Trừ depot đầu và cuối
                    capacity_used = v.capacity - v.remaining
                    capacity_efficiency = (capacity_used / v.capacity) * 100
                    total_capacity_used += capacity_used
                    total_capacity_available += v.capacity
                    
                    print(f"       * Xe {v.id}: {customers_in_route} KH, dung lượng {capacity_used}/{v.capacity} ({capacity_efficiency:.0f}%)")
            
            # Tổng hiệu quả sử dụng dung lượng
            overall_capacity_efficiency = (total_capacity_used / max(total_capacity_available, 1)) * 100
            print(f"     - 📈 Tổng hiệu quả dung lượng: {overall_capacity_efficiency:.1f}%")
        
        processed_count += 1
    
    print(f"\n{'='*50}")
    print(f"🎯 TỔNG KẾT THƯ MỤC")
    print(f"✅ Thành công: {processed_count} file")
    print(f"❌ Lỗi: {error_count} file")
    if processed_count > 0:
        print(f"📁 Kết quả lưu tại: {solution_dir}")
    print(f"{'='*50}")

# Định nghĩa các thư mục dữ liệu VRPSPDTW
data_directories = [
    # {
    #     'name': 'VRPSPDTW Wang_Chen',
    #     'src': r"D:\Logistic\excute_data\logistic\data\vrpspdtw_Wang_Chen\src",
    #     'solution': r"D:\Logistic\excute_data\logistic\data\vrpspdtw_Wang_Chen\solution",
    #     'enabled': True  # Bật/tắt xử lý thư mục này
    # },
    {
        'name': 'VRPSPDTW Liu_Tang_Yao',
        'src': r"D:\Logistic\excute_data\logistic\data\vrpspdtw_Liu_Tang_Yao\src",
        'solution': r"D:\Logistic\excute_data\logistic\data\vrpspdtw_Liu_Tang_Yao\solution",
        'enabled': True
    },
]

if __name__ == "__main__":
    print("🚛 GIẢI THUẬT VRPSPDTW - Vehicle Routing Problem with Simultaneous Pickup and Delivery and Time Windows")
    print("=" * 80)
    
    # Lọc và hiển thị danh sách thư mục sẽ xử lý
    enabled_directories = [d for d in data_directories if d.get('enabled', True)]
    disabled_directories = [d for d in data_directories if not d.get('enabled', True)]
    
    print(f"\n📋 DANH SÁCH THƯ MỤC SẼ XỬ LÝ: {len(enabled_directories)}")
    for i, directory in enumerate(enabled_directories, 1):
        print(f"  {i}. {directory['name']}")
    
    if disabled_directories:
        print(f"\n⏸️  THƯ MỤC TẠM THỜI TẮT: {len(disabled_directories)}")
        for i, directory in enumerate(disabled_directories, 1):
            print(f"  {i}. {directory['name']} (disabled)")
    
    # Xử lý các thư mục được bật
    for directory in enabled_directories:
        print(f"\n{'='*50}")
        print(f"XỬ LÝ THƯ MỤC: {directory['name']}")
        print(f"{'='*50}")
        
        process_directory(directory['src'], directory['solution'])

    print(f"\n{'='*80}")
    print(f"🎯 TỔNG KẾT TOÀN BỘ QUÁ TRÌNH")
    print(f"✅ Đã xử lý: {len(enabled_directories)} thư mục")
    if disabled_directories:
        print(f"⏸️  Bỏ qua: {len(disabled_directories)} thư mục (disabled)")
    print(f"🎉 HOÀN THÀNH XỬ LÝ TẤT CẢ THƯ MỤC VRPSPDTW!")
    print(f"{'='*80}")
