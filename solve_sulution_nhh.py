
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
        # Kiểm tra khách hàng đã được phục vụ chưa
        if customer.assigned:
            return False
            
        travel_time = self.current_location.distance_to(customer)
        arrival_time = self.time + travel_time
        
        # Kiểm tra cửa sổ thời gian nghiêm ngặt
        if arrival_time > customer.tw_end:
            return False
            
        # Kiểm tra dung lượng xe cho cả delivery và pickup
        capacity_needed = max(customer.d_demand, customer.p_demand)
        if self.remaining < capacity_needed:
            return False
            
        return True

    def assign(self, customer):
        # Kiểm tra lại trước khi gán để đảm bảo an toàn
        if customer.assigned or not self.can_serve(customer):
            return False
            
        travel_time = self.current_location.distance_to(customer)
        self.time += travel_time
        self.time = max(self.time, customer.tw_start)  # Đảm bảo xe đến sau thời gian sẵn sàng
        self.time += customer.service  # Thời gian phục vụ khách hàng
        
        # Cập nhật dung lượng xe dựa trên loại dịch vụ
        if customer.d_demand > 0:  # Delivery - giao hàng, tăng chỗ trống
            self.remaining += customer.d_demand
        if customer.p_demand > 0:  # Pickup - nhận hàng, giảm chỗ trống
            self.remaining -= customer.p_demand
            
        self.route.append(customer.cid)  # Thêm khách hàng vào tuyến đường
        self.current_location = customer  # Cập nhật vị trí của xe
        customer.assigned = True  # Đánh dấu khách hàng đã được phục vụ
        return True

    def return_to_depot(self):
        self.route.append(0)  # Quay lại điểm xuất phát (depot)

# Thuật toán tối ưu hóa số xe: ưu tiên sử dụng ít xe nhất
def assign_customers_to_vehicles(customers, vehicles):
    # Reset trạng thái assigned cho tất cả khách hàng
    for customer in customers:
        customer.assigned = False
    
    # Sắp xếp khách hàng theo độ ưu tiên: thời gian kết thúc sớm nhất và cửa sổ thời gian hẹp nhất
    customers_sorted = sorted(customers, key=lambda c: (c.tw_end, c.tw_end - c.tw_start, -max(c.d_demand, c.p_demand)))
    
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

def read_vrptw_wang_chen_format(lines):
    """Đọc định dạng VRPTW của Wang Chen (rdp*.txt style)"""
    customers = []
    vehicles = []
    
    # Tìm thông tin xe
    for i, line in enumerate(lines):
        if 'NUMBER' in line and 'CAPACITY' in line:
            vehicle_info_line = lines[i + 1]
            parts = vehicle_info_line.split()
            if len(parts) == 3:  # Định dạng Wang Chen: số khách hàng, số xe, dung lượng
                num_customers, num_vehicles, capacity = map(int, parts)
            else:
                raise ValueError(f"Không thể đọc thông tin xe từ dòng: {vehicle_info_line}")
            break
    
    # Tìm dữ liệu khách hàng
    customer_data_start = None
    for i, line in enumerate(lines):
        if 'CUST NO.' in line:
            customer_data_start = i + 1  # Không bỏ qua dòng trống trong định dạng Wang Chen
            break
    
    if customer_data_start:
        for line in lines[customer_data_start:]:
            parts = line.split()
            if len(parts) >= 7:
                cid = int(parts[0])
                x = float(parts[1])
                y = float(parts[2])
                d_demand = float(parts[3])
                p_demand = float(parts[4])  # Wang Chen có cả pickup demand
                ready = float(parts[5])
                due = float(parts[6])
                service = float(parts[7])
                
                customers.append(Customer(cid, x, y, d_demand, p_demand, ready, due, service))
    
    # Tạo xe
    for i in range(1, num_vehicles + 1):
        vehicles.append(Vehicle(i, capacity, customers[0]))
    
    customers.pop(0)  # Loại bỏ depot khỏi danh sách khách hàng
    return customers, vehicles

def get_supported_formats():
    """Trả về danh sách các định dạng file được hỗ trợ"""
    return ['VRPTW', 'VRPTW_WANG_CHEN', 'PDPTW']

def validate_file_format(file_format):
    """Kiểm tra xem định dạng file có được hỗ trợ không"""
    if file_format is None:
        return True  # None có nghĩa là tự động phát hiện
    return file_format in get_supported_formats()

def detect_file_format(lines):
    """Phát hiện định dạng file dựa trên nội dung"""
    # Kiểm tra các định dạng file
    for i, line in enumerate(lines):
        if 'VEHICLE' in line and i < 10:
            # Kiểm tra xem có phải định dạng Wang Chen không
            if i > 0 and lines[i-1].startswith('Rdp'):
                return 'VRPTW_WANG_CHEN'
            return 'VRPTW'
    
    # Nếu dòng đầu chỉ có 3 số thì có thể là PDPTW
    first_line_parts = lines[0].split()
    if len(first_line_parts) >= 3 and first_line_parts[0].isdigit():
        return 'PDPTW'
    
    return 'UNKNOWN'

def read_data(filepath, file_format=None):
    """Hàm đọc dữ liệu tổng quát - có thể tự động phát hiện hoặc chỉ định định dạng file"""
    try:
        # Kiểm tra định dạng file có hợp lệ không
        if not validate_file_format(file_format):
            supported_formats = get_supported_formats()
            print(f"❌ Định dạng file không được hỗ trợ: {file_format}")
            print(f"📋 Các định dạng được hỗ trợ: {', '.join(supported_formats)}")
            return [], []
        
        with open(filepath, 'r', encoding='utf-8') as f:
            lines = [line.strip() for line in f if line.strip()]
        
        if not lines:
            print(f"File {filepath} trống hoặc không đọc được")
            return [], []
        
        # Nếu không chỉ định định dạng, tự động phát hiện
        if file_format is None:
            file_format = detect_file_format(lines)
            print(f"🔍 Phát hiện định dạng file: {file_format}")
        else:
            print(f"🎯 Sử dụng định dạng được chỉ định: {file_format}")
        
        # Đọc dữ liệu theo định dạng phù hợp
        if file_format == 'VRPTW':
            return read_vrptw_format(lines)
        elif file_format == 'VRPTW_WANG_CHEN':
            return read_vrptw_wang_chen_format(lines)
        elif file_format == 'PDPTW':
            return read_pdptw_format(lines)
        else:
            print(f"❌ Định dạng file không được hỗ trợ: {file_format}")
            # Thử đọc như PDPTW format (định dạng đơn giản nhất)
            try:
                print("🔄 Thử đọc với định dạng PDPTW...")
                return read_pdptw_format(lines)
            except:
                print(f"❌ Không thể đọc file {filepath}")
                return [], []
                
    except Exception as e:
        print(f"❌ Lỗi khi đọc file {filepath}: {str(e)}")
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

def process_directory(src_dir, solution_dir, file_format=None):
    """Xử lý tất cả file trong một thư mục với định dạng được chỉ định hoặc tự động phát hiện"""
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
    if file_format:
        print(f"🎯 Định dạng được chỉ định: {file_format}")
    else:
        print(f"🔍 Sử dụng chế độ tự động phát hiện định dạng")
    
    for i, fname in enumerate(txt_files, 1):
        src_path = os.path.join(src_dir, fname)
        
        print(f"\n[{i}/{len(txt_files)}] 🔄 Đang xử lý...")
        
        import time
        start_time = time.time()
        
        customers, vehicles = read_data(src_path, file_format)
        
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
        
        # Thống kê kết quả chi tiết với focus vào tối ưu hóa số xe
        used_vehicles = sum(1 for v in vehicles_and_way if len(v.route) > 2)
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
        
        # Hiển thị chi tiết từng xe để phân tích hiệu quả
        if used_vehicles > 0:
            print(f"     - 🔍 Chi tiết từng xe:")
            total_capacity_used = 0
            total_capacity_available = 0
            
            for v in vehicles_and_way:
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
    print(f"🎯 TỔNG KẾT THƯMỤC")
    print(f"✅ Thành công: {processed_count} file")
    print(f"❌ Lỗi: {error_count} file")
    if processed_count > 0:
        print(f"📁 Kết quả lưu tại: {solution_dir}")
    print(f"{'='*50}")

# Định nghĩa các thư mục dữ liệu với định dạng file được chỉ định
# 
# Cách sử dụng:
# - 'format': Chỉ định định dạng file cụ thể ('VRPTW', 'VRPTW_WANG_CHEN', 'PDPTW')
# - Không có 'format' hoặc 'format': None: Tự động phát hiện định dạng
# - Các định dạng được hỗ trợ: VRPTW, VRPTW_WANG_CHEN, PDPTW
#
data_directories = [
    # {
    #     'name': 'VRPTW',
    #     'src': r"D:\Logistic\excute_data\logistic\data\vrptw\src",    
    #     'solution': r"D:\Logistic\excute_data\logistic\data\vrptw\solution",
    #     'format': 'VRPTW'  # Chỉ định định dạng VRPTW
    # },
    # {
    #     'name': 'PDPTW',
    #     'src': r"D:\Logistic\excute_data\logistic\data\pdptw\src",
    #     'solution': r"D:\Logistic\excute_data\logistic\data\pdptw\solution",
    #     'format': 'PDPTW'  # Chỉ định định dạng PDPTW
    # },
    {
        'name': 'VRPTW Wang Chen',
        'src': r"D:\Logistic\excute_data\logistic\data\vrptw_Wang_Chen\src",
        'solution': r"D:\Logistic\excute_data\logistic\data\vrptw_Wang_Chen\solution",
        'format': 'VRPTW_WANG_CHEN'  # Chỉ định định dạng VRPTW Wang Chen
    }
    # Ví dụ sử dụng tự động phát hiện:
    # {
    #     'name': 'Mixed Format Directory',
    #     'src': r"D:\path\to\mixed\files",
    #     'solution': r"D:\path\to\solutions"
    #     # Không có 'format' -> tự động phát hiện
    # }
]

# Xử lý tất cả các thư mục
for directory in data_directories:
    print(f"\n{'='*50}")
    print(f"XỬ LÝ THƯ MỤC: {directory['name']}")
    print(f"{'='*50}")
    
    # Sử dụng định dạng được chỉ định hoặc None để tự động phát hiện
    file_format = directory.get('format', None)
    process_directory(directory['src'], directory['solution'], file_format)