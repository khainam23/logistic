import matplotlib.pyplot as plt
import numpy as np
import random
from matplotlib.patches import Circle
import matplotlib.patches as mpatches

def read_solution_file(filename):
    """Đọc file solution và trả về danh sách các tuyến đường"""
    routes = []
    
    with open(filename, 'r', encoding='utf-8') as file:
        for line in file:
            line = line.strip()
            if line.startswith('Route') and ':' in line:
                # Tách phần sau dấu ':'
                route_part = line.split(':', 1)[1].strip()
                if route_part and route_part != '0':
                    # Chuyển đổi chuỗi số thành list integers
                    nodes = [int(x) for x in route_part.split()]
                    if len(nodes) > 2:  # Chỉ lấy routes có điểm trung gian (không chỉ 0-0)
                        routes.append(nodes)
    
    return routes

def generate_coordinates(max_node, seed=42):
    """Tạo tọa độ ngẫu nhiên cho các điểm"""
    random.seed(seed)
    np.random.seed(seed)
    
    coordinates = {}
    # Depot tại tâm
    coordinates[0] = (50, 50)
    
    # Tạo tọa độ cho các điểm khác
    for i in range(1, max_node + 1):
        x = random.uniform(10, 90)
        y = random.uniform(10, 90)
        coordinates[i] = (x, y)
    
    return coordinates

def visualize_routes(routes, coordinates, filename_prefix="route_visualization"):
    """Tạo visualization cho các tuyến đường"""
    
    # Tạo figure với kích thước lớn
    fig, ax = plt.subplots(figsize=(15, 12))
    
    # Màu sắc cho các routes
    colors = plt.cm.Set3(np.linspace(0, 1, len(routes)))
    
    # Vẽ tất cả các điểm trước
    all_nodes = set()
    for route in routes:
        all_nodes.update(route)
    
    for node in all_nodes:
        if node in coordinates:
            x, y = coordinates[node]
            if node == 0:
                # Depot - màu đỏ, kích thước lớn
                ax.scatter(x, y, c='red', s=200, marker='s', 
                          label='Depot' if node == 0 else '', zorder=3)
                ax.annotate(f'Depot({node})', (x, y), xytext=(5, 5), 
                           textcoords='offset points', fontsize=10, fontweight='bold')
            else:
                # Các điểm khác - màu xanh
                ax.scatter(x, y, c='lightblue', s=100, marker='o', 
                          edgecolors='darkblue', zorder=2)
                ax.annotate(str(node), (x, y), xytext=(5, 5), 
                           textcoords='offset points', fontsize=8)
    
    # Vẽ các tuyến đường
    route_patches = []
    for i, route in enumerate(routes):
        color = colors[i]
        route_coords = []
        
        for node in route:
            if node in coordinates:
                route_coords.append(coordinates[node])
        
        if len(route_coords) >= 2:
            # Vẽ đường nối
            xs, ys = zip(*route_coords)
            ax.plot(xs, ys, color=color, linewidth=2, alpha=0.7, zorder=1)
            
            # Vẽ mũi tên để chỉ hướng
            for j in range(len(route_coords) - 1):
                x1, y1 = route_coords[j]
                x2, y2 = route_coords[j + 1]
                
                # Tính vector hướng
                dx = x2 - x1
                dy = y2 - y1
                
                # Vẽ mũi tên nhỏ ở giữa đoạn
                mid_x = (x1 + x2) / 2
                mid_y = (y1 + y2) / 2
                
                ax.annotate('', xy=(mid_x + dx*0.1, mid_y + dy*0.1), 
                           xytext=(mid_x - dx*0.1, mid_y - dy*0.1),
                           arrowprops=dict(arrowstyle='->', color=color, lw=1.5))
        
        # Tạo patch cho legend
        route_patch = mpatches.Patch(color=color, label=f'Route {i+1}')
        route_patches.append(route_patch)
    
    # Cài đặt đồ thị
    ax.set_xlim(0, 100)
    ax.set_ylim(0, 100)
    ax.set_xlabel('X Coordinate', fontsize=12)
    ax.set_ylabel('Y Coordinate', fontsize=12)
    ax.set_title('Vehicle Routing Visualization', fontsize=16, fontweight='bold')
    ax.grid(True, alpha=0.3)
    
    # Thêm legend
    depot_patch = mpatches.Patch(color='red', label='Depot')
    customer_patch = mpatches.Patch(color='lightblue', label='Customer')
    
    # Chia legend thành 2 cột nếu có nhiều routes
    if len(route_patches) > 10:
        legend1 = ax.legend(handles=[depot_patch, customer_patch], 
                           loc='upper left', bbox_to_anchor=(1.02, 1))
        legend2 = ax.legend(handles=route_patches, 
                           loc='upper left', bbox_to_anchor=(1.02, 0.8), ncol=1)
        ax.add_artist(legend1)
    else:
        all_patches = [depot_patch, customer_patch] + route_patches
        ax.legend(handles=all_patches, loc='upper left', bbox_to_anchor=(1.02, 1))
    
    plt.tight_layout()
    
    # Lưu file
    plt.savefig(f'{filename_prefix}.png', dpi=300, bbox_inches='tight')
    plt.savefig(f'{filename_prefix}.pdf', bbox_inches='tight')
    
    return fig, ax

def create_route_report(routes, output_file="route_report.txt"):
    """Tạo báo cáo chi tiết về các tuyến đường"""
    
    with open(output_file, 'w', encoding='utf-8') as f:
        f.write("=" * 60 + "\n")
        f.write("         BÁO CÁO CHI TIẾT CÁC TUYẾN ĐƯỜNG\n")
        f.write("=" * 60 + "\n\n")
        
        total_customers = 0
        non_empty_routes = 0
        
        for i, route in enumerate(routes, 1):
            f.write(f"TUYẾN {i}:\n")
            f.write("-" * 40 + "\n")
            
            # Loại bỏ depot (0) để đếm khách hàng
            customers = [node for node in route if node != 0]
            total_customers += len(customers)
            non_empty_routes += 1
            
            f.write(f"Trình tự di chuyển: {' → '.join(map(str, route))}\n")
            f.write(f"Số khách hàng phục vụ: {len(customers)}\n")
            
            if customers:
                f.write(f"Danh sách khách hàng: {', '.join(map(str, customers))}\n")
            
            f.write(f"Tổng số điểm dừng: {len(route)}\n")
            f.write("\n")
        
        # Thống kê tổng quan
        f.write("=" * 60 + "\n")
        f.write("                THỐNG KÊ TỔNG QUAN\n")
        f.write("=" * 60 + "\n")
        f.write(f"Tổng số tuyến có khách hàng: {non_empty_routes}\n")
        f.write(f"Tổng số khách hàng được phục vụ: {total_customers}\n")
        f.write(f"Trung bình khách hàng/tuyến: {total_customers/non_empty_routes:.1f}\n")
        
        # Phân tích độ dài tuyến
        route_lengths = [len([n for n in route if n != 0]) for route in routes]
        f.write(f"Tuyến ngắn nhất: {min(route_lengths)} khách hàng\n")
        f.write(f"Tuyến dài nhất: {max(route_lengths)} khách hàng\n")

def main():
    # Đường dẫn file solution
    solution_file = "d:/Logistic/excute_data/logistic/data/pdptw/solution/lc102.txt"
    
    try:
        print("Đang đọc file solution...")
        routes = read_solution_file(solution_file)
        
        if not routes:
            print("Không tìm thấy tuyến đường nào trong file!")
            return
        
        print(f"Đã tìm thấy {len(routes)} tuyến đường")
        
        # Tìm node có số lớn nhất
        max_node = 0
        for route in routes:
            max_node = max(max_node, max(route))
        
        print(f"Tạo tọa độ cho {max_node + 1} điểm...")
        coordinates = generate_coordinates(max_node)
        
        print("Đang tạo visualization...")
        fig, ax = visualize_routes(routes, coordinates, 
                                 filename_prefix="d:/Logistic/excute_data/logistic/lc102_routes")
        
        print("Đang tạo báo cáo...")
        create_route_report(routes, "d:/Logistic/excute_data/logistic/lc102_route_report.txt")
        
        print("Hoàn thành!")
        print("Các file đã được tạo:")
        print("- lc102_routes.png (hình ảnh)")
        print("- lc102_routes.pdf (PDF)")
        print("- lc102_route_report.txt (báo cáo chi tiết)")
        
        # Hiển thị đồ thị
        plt.show()
        
    except FileNotFoundError:
        print(f"Không tìm thấy file: {solution_file}")
    except Exception as e:
        print(f"Lỗi: {e}")

if __name__ == "__main__":
    main()