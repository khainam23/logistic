
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

class DistanceMatrix:
    def __init__(self):
        self.distances = {}
        self.times = {}
    
    def add_distance(self, from_node, to_node, distance, time):
        self.distances[(from_node, to_node)] = distance
        self.times[(from_node, to_node)] = time
    
    def get_distance(self, from_node, to_node):
        return self.distances.get((from_node, to_node), float('inf'))
    
    def get_time(self, from_node, to_node):
        return self.times.get((from_node, to_node), float('inf'))

class Vehicle:
    def __init__(self, id, capacity, current_location, distance_matrix=None):
        self.id = id
        self.capacity = capacity
        self.remaining = capacity
        self.route = [0]  # depot
        self.current_location = current_location  # depot
        self.time = 0
        self.distance_matrix = distance_matrix

    def get_travel_time(self, from_customer, to_customer):
        """Lấy thời gian di chuyển giữa hai khách hàng"""
        if self.distance_matrix:
            return self.distance_matrix.get_time(from_customer.cid, to_customer.cid)
        else:
            # Fallback to Euclidean distance
            return from_customer.distance_to(to_customer)

    def can_serve(self, customer):
        travel_time = self.get_travel_time(self.current_location, customer)
        arrival_time = self.time + travel_time
        
        # Kiểm tra dung lượng
        if self.remaining < customer.d_demand:
            return False
        
        # Kiểm tra thời gian - xe có thể đến sau due time
        if arrival_time > customer.tw_end:
            return False
            
        return True

    def assign(self, customer):
        travel_time = self.get_travel_time(self.current_location, customer)
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
                    # Sử dụng travel time thay vì distance
                    travel_time = vehicle.get_travel_time(vehicle.current_location, customer)
                    if travel_time < best_distance:
                        best_distance = travel_time
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
    # Kiểm tra Liu Tang Yao format (VRPSDPTW) - có NAME, TYPE : VRPSDPTW, NODE_SECTION
    has_name = False
    has_vrpsdptw_type = False
    has_node_section = False
    
    for i, line in enumerate(lines):
        if line.startswith('NAME'):
            has_name = True
        elif 'TYPE : VRPSDPTW' in line:
            has_vrpsdptw_type = True
        elif line.startswith('NODE_SECTION'):
            has_node_section = True
            
    if has_name and has_vrpsdptw_type and has_node_section:
        return 'LIU_TANG_YAO'
    
    # Kiểm tra TSPLIB-style format khác
    for i, line in enumerate(lines):
        if 'NAME' in line and 'TYPE' in lines[i+1] if i+1 < len(lines) else False:
            return 'TSPLIB'
        # Kiểm tra định dạng explicit Wang Chen
        if 'TYPE : VRPSDPTW' in line and 'DIMENSION' in lines[i+1] if i+1 < len(lines) else False:
            return 'EXPLICIT_WANG_CHEN'
        if 'CUSTOMER' in line and 'VEHICLE' in line and i < 10:
            # Kiểm tra thêm có DDEMAND và PDEMAND không (đặc trưng của VRPSPDTW)
            for j in range(i, min(i+10, len(lines))):
                if 'DDEMAND' in lines[j] and 'PDEMAND' in lines[j]:
                    return 'VRPSPDTW'
            return 'VRPTW'
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
            parts = vehicle_info_line.split()
            if len(parts) == 2:
                # Định dạng VRPTW chuẩn: num_vehicles, capacity
                num_vehicles, capacity = map(int, parts)
            elif len(parts) >= 3:
                # Định dạng Wang Chang: num_customers, num_vehicles, capacity
                num_vehicles, capacity = int(parts[1]), int(parts[2])
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
        vehicles.append(Vehicle(i, capacity, customers[0], None))
    
    customers.pop(0)  # Loại bỏ depot khỏi danh sách khách hàng
    return customers, vehicles

def read_vrpspdtw_format(lines):
    """Đọc định dạng VRPSPDTW (cdp101.txt style)"""
    customers = []
    vehicles = []
    
    # Tìm thông tin xe
    for i, line in enumerate(lines):
        if 'NUMBER' in line and 'CAPACITY' in line:
            vehicle_info_line = lines[i + 1].strip()
            # Xử lý tab và nhiều dấu cách
            parts = vehicle_info_line.replace('\t', ' ').split()
            if len(parts) >= 3:
                num_customers = int(parts[0])
                num_vehicles = int(parts[1])
                capacity = int(parts[2])
                break
    
    # Tìm dữ liệu khách hàng
    customer_data_start = None
    for i, line in enumerate(lines):
        if 'CUST NO.' in line and 'DDEMAND' in line:
            customer_data_start = i + 1  # Tìm dòng tiếp theo
            # Bỏ qua các dòng trống
            while customer_data_start < len(lines) and not lines[customer_data_start].strip():
                customer_data_start += 1
            break
    
    if customer_data_start:
        for line in lines[customer_data_start:]:
            # Xử lý dấu cách không đều và tab
            line = line.strip()
            if not line:
                continue
            parts = line.replace('\t', ' ').split()
            if len(parts) >= 8:
                try:
                    cid = int(parts[0])
                    x = float(parts[1])
                    y = float(parts[2])
                    d_demand = float(parts[3])
                    p_demand = float(parts[4])
                    ready = float(parts[5])
                    due = float(parts[6])
                    service = float(parts[7])
                    
                    customers.append(Customer(cid, x, y, d_demand, p_demand, ready, due, service))
                except (ValueError, IndexError):
                    # Bỏ qua dòng không hợp lệ
                    continue
    
    # Tạo xe
    for i in range(1, num_vehicles + 1):
        vehicles.append(Vehicle(i, capacity, customers[0], None))
    
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
        vehicles.append(Vehicle(i, capacity, customers[0], None))
    
    customers.pop(0)  # Loại bỏ depot khỏi danh sách khách hàng
    return customers, vehicles

def read_liu_tang_yao_format(lines):
    """Đọc định dạng Liu Tang Yao (VRPSDPTW)"""
    customers = []
    vehicles = []
    distance_matrix = DistanceMatrix()
    
    # Đọc thông tin header
    dimension = 0
    num_vehicles = 0
    capacity = 0
    
    for line in lines:
        if line.startswith('DIMENSION'):
            dimension = int(line.split(':')[1].strip())
        elif line.startswith('VEHICLES'):
            num_vehicles = int(line.split(':')[1].strip())
        elif line.startswith('CAPACITY'):
            capacity = float(line.split(':')[1].strip())
        elif line.startswith('NODE_SECTION'):
            break
    
    # Tìm vị trí bắt đầu dữ liệu node
    node_start = None
    distance_start = None
    
    for i, line in enumerate(lines):
        if line.startswith('NODE_SECTION'):
            node_start = i + 1
        elif line.startswith('DISTANCETIME_SECTION'):
            distance_start = i + 1
            break
    
    # Đọc dữ liệu node
    if node_start:
        for i in range(node_start, len(lines)):
            line = lines[i]
            if not line or line.startswith('EOF') or line.startswith('DISTANCETIME_SECTION'):
                break
            
            parts = line.split(',')
            if len(parts) >= 6:
                cid = int(parts[0])
                x = float(parts[1])
                y = float(parts[2])
                d_demand = float(parts[3])  # delivery demand
                due = float(parts[4])       # due time
                service = float(parts[5])   # service time
                ready = 0                   # ready time - mặc định là 0
                p_demand = 0                # pickup demand - Liu Tang Yao thường chỉ có delivery
                
                customers.append(Customer(cid, x, y, d_demand, p_demand, ready, due, service))
    
    # Đọc ma trận khoảng cách và thời gian
    if distance_start:
        for i in range(distance_start, len(lines)):
            line = lines[i]
            if not line or line.startswith('EOF'):
                break
            
            parts = line.split(',')
            if len(parts) >= 4:
                from_node = int(parts[0])
                to_node = int(parts[1])
                distance = float(parts[2])
                time = float(parts[3])
                
                distance_matrix.add_distance(from_node, to_node, distance, time)
    
    # Tạo xe với capacity đã đọc được, giới hạn số lượng xe hợp lý
    actual_vehicles = min(num_vehicles, 50, len(customers))
    for i in range(1, actual_vehicles + 1):
        vehicles.append(Vehicle(i, capacity, customers[0], distance_matrix))
    
    customers.pop(0)  # Loại bỏ depot khỏi danh sách khách hàng
    return customers, vehicles

def read_tsplib_format(lines):
    """Đọc định dạng TSPLIB-style khác"""
    customers = []
    vehicles = []
    
    # Đọc thông tin header
    dimension = 0
    num_vehicles = 0
    capacity = 0
    
    for line in lines:
        if line.startswith('DIMENSION'):
            dimension = int(line.split(':')[1].strip())
        elif line.startswith('VEHICLES'):
            num_vehicles = int(line.split(':')[1].strip())
        elif line.startswith('CAPACITY'):
            capacity = float(line.split(':')[1].strip())
        elif line.startswith('NODE_SECTION'):
            break
    
    # Tìm vị trí bắt đầu dữ liệu node
    node_start = None
    for i, line in enumerate(lines):
        if line.startswith('NODE_SECTION'):
            node_start = i + 1
            break
    
    if node_start:
        for i in range(node_start, len(lines)):
            line = lines[i]
            if not line or line.startswith('EOF'):
                break
            
            parts = line.split(',')
            if len(parts) >= 6:
                cid = int(parts[0])
                x = float(parts[1])
                y = float(parts[2])
                d_demand = float(parts[3])  # delivery demand
                p_demand = 0  # pickup demand - tạm thời để 0
                ready = float(parts[4])
                due = float(parts[5])
                service = 30 if cid > 0 else 0  # default service time
                
                customers.append(Customer(cid, x, y, d_demand, p_demand, ready, due, service))
    
    # Tạo xe với capacity đã đọc được
    for i in range(1, min(num_vehicles, 50) + 1):  # Giới hạn số xe để tránh quá nhiều
        vehicles.append(Vehicle(i, capacity, customers[0], None))
    
    customers.pop(0)  # Loại bỏ depot khỏi danh sách khách hàng
    return customers, vehicles

def read_explicit_wang_chen_format(lines):
    """Đọc định dạng explicit Wang Chen (explicit_cdp101.txt style)"""
    customers = []
    vehicles = []
    num_vehicles = 0
    capacity = 0
    
    # Đọc thông tin header
    for line in lines:
        if line.startswith('DIMENSION :'):
            num_customers = int(line.split(':')[1].strip())
        elif line.startswith('VEHICLES :'):
            num_vehicles = int(line.split(':')[1].strip())
        elif line.startswith('CAPACITY :'):
            capacity = float(line.split(':')[1].strip())
        elif line.startswith('NODE_SECTION'):
            break
    
    # Tìm vị trí bắt đầu NODE_SECTION
    node_start = None
    for i, line in enumerate(lines):
        if line.startswith('NODE_SECTION'):
            node_start = i + 1
            break
    
    # Đọc dữ liệu khách hàng từ NODE_SECTION
    if node_start:
        for i in range(node_start, len(lines)):
            line = lines[i]
            if not line or line.startswith('DISTANCETIME_SECTION'):
                break
            
            parts = line.split(',')
            if len(parts) >= 6:
                cid = int(parts[0])
                d_demand = float(parts[1])
                p_demand = float(parts[2])
                ready = float(parts[3])
                due = float(parts[4])
                service = float(parts[5])
                
                # Tạo tọa độ giả (vì explicit format có thể không có tọa độ)
                # Hoặc có thể lấy từ distance matrix nếu cần
                x = cid * 10  # Tạm thời tạo tọa độ giả
                y = 0
                
                customers.append(Customer(cid, x, y, d_demand, p_demand, ready, due, service))
    
    # Tạo xe
    for i in range(1, num_vehicles + 1):
        vehicles.append(Vehicle(i, capacity, customers[0], None))
    
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
        elif file_format == 'VRPSPDTW':
            return read_vrpspdtw_format(lines)
        elif file_format == 'PDPTW':
            return read_pdptw_format(lines)
        elif file_format == 'LIU_TANG_YAO':
            return read_liu_tang_yao_format(lines)
        elif file_format == 'TSPLIB':
            return read_tsplib_format(lines)
        elif file_format == 'EXPLICIT_WANG_CHEN':
            return read_explicit_wang_chen_format(lines)
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
    
    # Hỗ trợ cả file .txt và .vrpsdptw
    supported_extensions = ['.txt', '.vrpsdptw']
    data_files = [f for f in os.listdir(src_dir) 
                  if any(f.endswith(ext) for ext in supported_extensions) 
                  and os.path.isfile(os.path.join(src_dir, f))]
    
    if not data_files:
        print(f"❌ Không tìm thấy file .txt hoặc .vrpsdptw nào trong thư mục {src_dir}")
        return
    
    # Thống kê theo loại file
    txt_count = len([f for f in data_files if f.endswith('.txt')])
    vrpsdptw_count = len([f for f in data_files if f.endswith('.vrpsdptw')])
    
    print(f"📂 Tìm thấy {len(data_files)} file dữ liệu:")
    if txt_count > 0:
        print(f"   - {txt_count} file .txt")
    if vrpsdptw_count > 0:
        print(f"   - {vrpsdptw_count} file .vrpsdptw")
    
    for i, fname in enumerate(data_files, 1):
        src_path = os.path.join(src_dir, fname)
        
        print(f"\n[{i}/{len(data_files)}] 🔄 Đang xử lý {fname}...")
        
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
        
        # Lưu kết quả - đổi đuôi thành .txt
        base_name = os.path.splitext(fname)[0]  # Lấy tên file không có đuôi
        solution_filename = f"{base_name}.txt"  # Thêm đuôi .txt
        solution_path = os.path.join(solution_dir, solution_filename)
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
    },
    {
        'name': 'VRPSPDTW Wang Chen',
        'src': r"D:\Logistic\excute_data\logistic\data\vrpspdtw_Wang_Chen\src",
        'solution': r"D:\Logistic\excute_data\logistic\data\vrpspdtw_Wang_Chen\solution"
    },
    {
        'name': 'VRPSPDTW Liu Tang Yao',
        'src': r"D:\Logistic\excute_data\logistic\data\vrpspdtw_Liu_Tang_Yao\src",
        'solution': r"D:\Logistic\excute_data\logistic\data\vrpspdtw_Liu_Tang_Yao\solution"
    }
]

# Xử lý tất cả các thư mục
for directory in data_directories:
    print(f"\n{'='*50}")
    print(f"XỬ LÝ THƯ MỤC: {directory['name']}")
    print(f"{'='*50}")
    
    process_directory(directory['src'], directory['solution'])