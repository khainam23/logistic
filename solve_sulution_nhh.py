
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
        # Kiểm tra nếu xe có đủ dung lượng để phục vụ khách hàng (chỉ kiểm tra delivery demand)
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

# greedy algorithm
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

def detect_file_format(lines):
    """Phát hiện định dạng file dựa trên nội dung"""
    # Kiểm tra các định dạng file
    for i, line in enumerate(lines):
        if 'VEHICLE' in line and i < 10:
            return 'VRPTW'
    
    # Nếu dòng đầu chỉ có 3 số thì có thể là PDPTW
    first_line_parts = lines[0].split()
    if len(first_line_parts) >= 3 and first_line_parts[0].isdigit():
        return 'PDPTW'
    
    return 'UNKNOWN'

def read_vrptw_format(lines):
    """Đọc định dạng VRPTW (c101.txt style)"""
    customers = []
    vehicles = []
    
    # Tìm thông tin xe
    for i, line in enumerate(lines):
        if 'NUMBER' in line and 'CAPACITY' in line:
            vehicle_info_line = lines[i + 1]
            num_vehicles, capacity = map(int, vehicle_info_line.split())
            break
    
    # Tìm dữ liệu khách hàng
    customer_data_start = None
    for i, line in enumerate(lines):
        if 'CUST NO.' in line:
            customer_data_start = i + 2  # Bỏ qua dòng trống
            break
    
    if customer_data_start:
        for line in lines[customer_data_start:]:
            parts = line.split()
            if len(parts) >= 7:
                cid = int(parts[0])
                x = float(parts[1])
                y = float(parts[2])
                d_demand = float(parts[3])
                p_demand = 0  # VRPTW không có pickup demand
                ready = float(parts[4])
                due = float(parts[5])
                service = float(parts[6])
                
                customers.append(Customer(cid, x, y, d_demand, p_demand, ready, due, service))
    
    # Tạo xe
    for i in range(1, num_vehicles + 1):
        vehicles.append(Vehicle(i, capacity, customers[0]))
    
    customers.pop(0)  # Loại bỏ depot khỏi danh sách khách hàng
    return customers, vehicles



def read_pdptw_format(lines):
    """Đọc định dạng PDPTW (lc101.txt style)"""
    customers = []
    vehicles = []
    
    # Dòng đầu tiên chứa thông tin cơ bản
    first_line = lines[0].split()
    num_vehicles = int(first_line[0])
    capacity = int(first_line[1])
    
    # Đọc dữ liệu khách hàng
    for line in lines[1:]:
        parts = line.split()
        if len(parts) >= 7:
            cid = int(parts[0])
            x = float(parts[1])
            y = float(parts[2])
            demand = float(parts[3])  # Có thể âm hoặc dương
            ready = float(parts[4])
            due = float(parts[5])
            service = float(parts[6])
            
            # Xử lý demand: nếu âm thì là delivery demand, nếu dương thì là pickup demand
            if demand < 0:
                d_demand = abs(demand)
                p_demand = 0
            else:
                d_demand = 0
                p_demand = demand
            
            customers.append(Customer(cid, x, y, d_demand, p_demand, ready, due, service))
    
    # Tạo xe
    for i in range(1, num_vehicles + 1):
        vehicles.append(Vehicle(i, capacity, customers[0]))
    
    customers.pop(0)  # Loại bỏ depot khỏi danh sách khách hàng
    return customers, vehicles

def read_data(filepath):
    """Hàm đọc dữ liệu tổng quát - tự động phát hiện định dạng file"""
    try:
        with open(filepath, 'r', encoding='utf-8') as f:
            lines = [line.strip() for line in f if line.strip()]
        
        if not lines:
            print(f"File {filepath} trống hoặc không đọc được")
            return [], []
        
        # Phát hiện định dạng file
        file_format = detect_file_format(lines)
        print(f"Phát hiện định dạng file: {file_format}")
        
        # Đọc dữ liệu theo định dạng phù hợp
        if file_format == 'VRPTW':
            return read_vrptw_format(lines)
        elif file_format == 'PDPTW':
            return read_pdptw_format(lines)
        else:
            print(f"Không nhận diện được định dạng file {filepath}")
            # Thử đọc như PDPTW format (định dạng đơn giản nhất)
            try:
                return read_pdptw_format(lines)
            except:
                print(f"Không thể đọc file {filepath}")
                return [], []
                
    except Exception as e:
        print(f"Lỗi khi đọc file {filepath}: {str(e)}")
        return [], []

def write_solution(filename, vehicles):
    """Ghi kết quả giải thuật ra file"""
    with open(filename, 'w', encoding='utf-8') as f:
        for vehicle in vehicles:
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
    """Xử lý tất cả file trong một thư mục"""
    if not os.path.exists(src_dir):
        print(f"❌ Thư mục {src_dir} không tồn tại")
        return
    
    # Tạo thư mục solution nếu chưa có
    os.makedirs(solution_dir, exist_ok=True)
    
    processed_count = 0
    error_count = 0
    
    txt_files = [f for f in os.listdir(src_dir) if f.endswith('.txt') and os.path.isfile(os.path.join(src_dir, f))]
    
    if not txt_files:
        print(f"❌ Không tìm thấy file .txt nào trong thư mục {src_dir}")
        return
    
    print(f"📂 Tìm thấy {len(txt_files)} file .txt")
    
    for i, fname in enumerate(txt_files, 1):
        src_path = os.path.join(src_dir, fname)
        
        print(f"\n[{i}/{len(txt_files)}] 🔄 Đang xử lý...")
        
        import time
        start_time = time.time()
        
        customers, vehicles = read_data(src_path)
        
        if not customers or not vehicles:
            print(f"❌ Không thể đọc dữ liệu từ file {fname}")
            error_count += 1
            continue
        
        # In thông tin file
        print_file_info(customers, vehicles, fname)
        
        # Giải thuật
        vehicles_and_way = assign_customers_to_vehicles(customers, vehicles)
        
        # Lưu kết quả
        solution_path = os.path.join(solution_dir, fname)
        write_solution(solution_path, vehicles_and_way)
        
        # Tính thời gian xử lý
        processing_time = time.time() - start_time
        print(f"{processing_time:.2f}s")
        
        # Thống kê kết quả
        used_vehicles = sum(1 for v in vehicles_and_way if len(v.route) > 2)
        assigned_customers = sum(1 for c in customers if c.assigned)
        
        print(f"  ✅ Kết quả:")
        print(f"     - Xe sử dụng: {used_vehicles}/{len(vehicles)}")
        print(f"     - Khách hàng được phục vụ: {assigned_customers}/{len(customers)}")
        print(f"     - File kết quả: {os.path.basename(solution_path)}")
        
        processed_count += 1
    
    print(f"\n{'='*50}")
    print(f"🎯 TỔNG KẾT THƯMỤC")
    print(f"✅ Thành công: {processed_count} file")
    print(f"❌ Lỗi: {error_count} file")
    if processed_count > 0:
        print(f"📁 Kết quả lưu tại: {solution_dir}")
    print(f"{'='*50}")

# Định nghĩa các thư mục dữ liệu
data_directories = [
    {
        'name': 'VRPTW',
        'src': r"D:\Logistic\excute_data\logistic\data\vrptw\src",
        'solution': r"D:\Logistic\excute_data\logistic\data\vrptw\solution"
    },
    {
        'name': 'PDPTW',
        'src': r"D:\Logistic\excute_data\logistic\data\pdptw\src",
        'solution': r"D:\Logistic\excute_data\logistic\data\pdptw\solution"
    }
]

# Xử lý tất cả các thư mục
for directory in data_directories:
    print(f"\n{'='*50}")
    print(f"XỬ LÝ THƯ MỤC: {directory['name']}")
    print(f"{'='*50}")
    
    process_directory(directory['src'], directory['solution'])