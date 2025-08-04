"""
Chuyển đổi dữ liệu Wang Chen từ format .txt sang .vrpsdptw với tọa độ
Thêm tọa độ X, Y vào NODE_SECTION theo yêu cầu:
[ID],[X],[Y],[delivery],[pickup],[start_time],[end_time],[service_time]
"""

import os
import math
from typing import Dict, Tuple, List

class WangChenConverter:
    def __init__(self):
        self.customers = {}
        self.name = ""
        self.num_customers = 0
        self.num_vehicles = 0
        self.capacity = 0
        
    def read_txt_file(self, filepath: str):
        """Đọc file .txt của Wang Chen"""
        with open(filepath, 'r', encoding='utf-8') as f:
            lines = f.readlines()
        
        # Đọc tên bài toán
        self.name = lines[0].strip()
        
        # Đọc thông tin cơ bản
        info_line = lines[3].strip().split()
        self.num_customers = int(info_line[0])
        self.num_vehicles = int(info_line[1])
        self.capacity = float(info_line[2])
        
        # Đọc thông tin khách hàng
        self.customers = {}
        for i in range(6, 6 + self.num_customers + 1):  # +1 để bao gồm depot
            if i < len(lines):
                parts = lines[i].strip().split()
                if len(parts) >= 8:
                    customer_id = int(parts[0])
                    x_coord = float(parts[1])
                    y_coord = float(parts[2])
                    delivery_demand = float(parts[3])
                    pickup_demand = float(parts[4])
                    ready_time = int(parts[5])
                    due_date = int(parts[6])
                    service_time = int(parts[7])
                    
                    self.customers[customer_id] = {
                        'x': x_coord,
                        'y': y_coord,
                        'delivery': delivery_demand,
                        'pickup': pickup_demand,
                        'ready_time': ready_time,
                        'due_date': due_date,
                        'service_time': service_time
                    }
    
    def calculate_distance(self, x1: float, y1: float, x2: float, y2: float) -> float:
        """Tính khoảng cách Euclidean"""
        return math.sqrt((x2 - x1)**2 + (y2 - y1)**2)
    
    def generate_distance_matrix(self) -> Dict[Tuple[int, int], float]:
        """Tạo ma trận khoảng cách"""
        distance_matrix = {}
        
        for from_id in self.customers:
            for to_id in self.customers:
                if from_id != to_id:
                    from_customer = self.customers[from_id]
                    to_customer = self.customers[to_id]
                    
                    distance = self.calculate_distance(
                        from_customer['x'], from_customer['y'],
                        to_customer['x'], to_customer['y']
                    )
                    
                    distance_matrix[(from_id, to_id)] = distance
        
        return distance_matrix
    
    def write_vrpsdptw_file(self, output_filepath: str):
        """Ghi file .vrpsdptw với tọa độ"""
        distance_matrix = self.generate_distance_matrix()
        
        with open(output_filepath, 'w', encoding='utf-8') as f:
            # Header
            f.write(f"{self.name}\n")
            f.write("TYPE : VRPSDPTW\n")
            f.write(f"DIMENSION : {self.num_customers + 1}\n")  # +1 cho depot
            f.write(f"VEHICLES : {self.num_vehicles}\n")
            f.write("DISPATCHINGCOST : 2000\n")
            f.write("UNITCOST : 1.0\n")
            f.write(f"CAPACITY : {self.capacity}\n")
            f.write("EDGE_WEIGHT_TYPE : EXPLICIT\n")
            
            # NODE_SECTION với tọa độ
            f.write("NODE_SECTION\n")
            
            # Sắp xếp theo ID để đảm bảo thứ tự
            sorted_customers = sorted(self.customers.items())
            
            for customer_id, customer_data in sorted_customers:
                f.write(f"{customer_id},{customer_data['x']},{customer_data['y']},"
                       f"{customer_data['delivery']},{customer_data['pickup']},"
                       f"{customer_data['ready_time']},{customer_data['due_date']},"
                       f"{customer_data['service_time']}\n")
            
            # DISTANCETIME_SECTION
            f.write("DISTANCETIME_SECTION\n")
            
            for (from_id, to_id), distance in distance_matrix.items():
                # Giả sử thời gian di chuyển = khoảng cách
                travel_time = distance
                f.write(f"{from_id},{to_id},{distance:.6f},{travel_time:.6f}\n")
            
            # DEPOT_SECTION
            f.write("DEPOT_SECTION\n")
            f.write("0\n")  # Depot luôn có ID = 0
    
    def convert_file(self, input_filepath: str, output_filepath: str):
        """Chuyển đổi một file từ .txt sang .vrpsdptw với tọa độ"""
        print(f"Đang chuyển đổi: {input_filepath}")
        
        try:
            self.read_txt_file(input_filepath)
            self.write_vrpsdptw_file(output_filepath)
            print(f"✓ Đã tạo: {output_filepath}")
            print(f"  - Số khách hàng: {self.num_customers}")
            print(f"  - Số xe: {self.num_vehicles}")
            print(f"  - Sức chứa: {self.capacity}")
            
        except Exception as e:
            print(f"✗ Lỗi khi chuyển đổi {input_filepath}: {str(e)}")

def convert_all_wang_chen_files():
    """Chuyển đổi tất cả file Wang Chen"""
    input_dir = r"d:\Logistic\excute_data\logistic\data\Liu_Tang_Yao\src"
    output_dir = r"d:\Logistic\excute_data\logistic\data\Liu_Tang_Yao\src"
    
    converter = WangChenConverter()
    
    # Lấy danh sách tất cả file .txt
    txt_files = [f for f in os.listdir(input_dir) if f.endswith('.txt')]
    
    print(f"Tìm thấy {len(txt_files)} file .txt để chuyển đổi")
    print("=" * 60)
    
    converted_count = 0
    
    for txt_file in sorted(txt_files):
        input_path = os.path.join(input_dir, txt_file)
        
        # Tạo tên file output với prefix "coords_"
        base_name = txt_file.replace('.txt', '')
        output_filename = f"coords_{base_name}.vrpsdptw"
        output_path = os.path.join(output_dir, output_filename)
        
        converter.convert_file(input_path, output_path)
        converted_count += 1
        print()
    
    print("=" * 60)
    print(f"Hoàn thành! Đã chuyển đổi {converted_count} file")
    print(f"File output được lưu trong: {output_dir}")

def convert_single_file(input_file: str, output_file: str = None):
    """Chuyển đổi một file cụ thể"""
    converter = WangChenConverter()
    
    if output_file is None:
        # Tự động tạo tên file output
        base_name = os.path.splitext(os.path.basename(input_file))[0]
        output_dir = os.path.dirname(input_file)
        output_file = os.path.join(output_dir, f"coords_{base_name}.vrpsdptw")
    
    converter.convert_file(input_file, output_file)

def test_conversion():
    """Test chuyển đổi với một file mẫu"""
    input_file = r"d:\Logistic\excute_data\logistic\data-vrpspdtw\Wang_Chen\cdp101.txt"
    output_file = r"d:\Logistic\excute_data\logistic\data-vrpspdtw\Wang_Chen\coords_cdp101_test.vrpsdptw"
    
    print("=== TEST CHUYỂN ĐỔI ===")
    convert_single_file(input_file, output_file)
    
    # Kiểm tra kết quả
    if os.path.exists(output_file):
        print(f"\n=== KIỂM TRA KẾT QUẢ ===")
        with open(output_file, 'r', encoding='utf-8') as f:
            lines = f.readlines()
        
        print("10 dòng đầu của file output:")
        for i, line in enumerate(lines[:10]):
            print(f"{i+1:2d}: {line.rstrip()}")
        
        print(f"\nTổng số dòng: {len(lines)}")
        
        # Tìm NODE_SECTION
        node_section_start = -1
        for i, line in enumerate(lines):
            if line.strip() == "NODE_SECTION":
                node_section_start = i
                break
        
        if node_section_start >= 0:
            print(f"\nNODE_SECTION bắt đầu ở dòng {node_section_start + 1}")
            print("5 dòng đầu của NODE_SECTION:")
            for i in range(node_section_start + 1, min(node_section_start + 6, len(lines))):
                print(f"{i+1:2d}: {lines[i].rstrip()}")

if __name__ == "__main__":
    print("=== CHUYỂN ĐỔI DỮNG LIỆU WANG CHEN VỚI TỌA ĐỘ ===")
    print("1. Test chuyển đổi một file")
    print("2. Chuyển đổi tất cả file")
    print("3. Chuyển đổi file cụ thể")
    
    choice = input("\nChọn tùy chọn (1-3): ").strip()
    
    if choice == "1":
        test_conversion()
    elif choice == "2":
        convert_all_wang_chen_files()
    elif choice == "3":
        input_file = input("Nhập đường dẫn file input: ").strip()
        output_file = input("Nhập đường dẫn file output (để trống để tự động): ").strip()
        
        if not output_file:
            output_file = None
        
        convert_single_file(input_file, output_file)
    else:
        print("Lựa chọn không hợp lệ!")