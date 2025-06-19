import matplotlib.pyplot as plt
import matplotlib.animation as animation
import numpy as np
import random
from matplotlib.patches import Circle
import matplotlib.patches as mpatches
import time

class VehicleAnimation:
    def __init__(self):
        # Dữ liệu tuyến đường từ lr209.txt
        self.routes_data = [
            [0, 27, 95, 0],
            [0, 69, 0],
            [0, 59, 28, 0],
            [0, 92, 0],
            [0, 5, 0],
            [0, 83, 0],
            [0, 72, 0],
            [0, 33, 2, 52, 29, 19, 61, 101, 100, 84, 90, 77, 58, 102, 50, 0],
            [0, 42, 0],
            [0, 62, 44, 88, 76, 16, 40, 73, 22, 85, 81, 55, 80, 68, 34, 96, 43, 32, 48, 0],
            [0, 45, 0],
            [0, 15, 0],
            [0, 14, 0],
            [0, 11, 0],
            [0, 39, 31, 30, 0],
            [0, 47, 0],
            [0, 63, 0],
            [0, 23, 82, 38, 99, 41, 6, 8, 7, 86, 25, 56, 4, 74, 35, 54, 1, 89, 0],
            [0, 36, 67, 21, 75, 78, 9, 53, 93, 57, 17, 94, 13, 3, 66, 97, 24, 91, 0],
            [0, 64, 0],
            [0, 65, 98, 12, 71, 70, 79, 51, 18, 87, 49, 46, 10, 20, 26, 37, 60, 0]
        ]
        
        self.coordinates = {}
        self.vehicles = []
        self.served_customers = set()
        self.frame_count = 0
        self.speed_multiplier = 1.0
        
        # Màu sắc cho các xe
        self.colors = plt.cm.Set3(np.linspace(0, 1, len(self.routes_data)))
        
        # Khởi tạo
        self.generate_coordinates()
        self.initialize_vehicles()
        self.setup_plot()
        
    def generate_coordinates(self):
        """Tạo tọa độ cho các điểm"""
        max_node = max([max(route) for route in self.routes_data])
        
        # Depot ở trung tâm
        self.coordinates[0] = (50, 50)
        
        # Tạo tọa độ cho các điểm khác theo hình tròn
        for i in range(1, max_node + 1):
            angle = (i / max_node) * 2 * np.pi
            radius_variation = 0.7 + random.random() * 0.6
            radius = 35 * radius_variation
            
            self.coordinates[i] = (
                50 + np.cos(angle) * radius,
                50 + np.sin(angle) * radius
            )
    
    def initialize_vehicles(self):
        """Khởi tạo các xe"""
        self.vehicles = []
        for i, route in enumerate(self.routes_data):
            if len(route) > 2:  # Chỉ xử lý routes có khách hàng
                vehicle = {
                    'id': i + 1,
                    'route': route,
                    'current_segment': 0,
                    'progress': 0.0,
                    'position': self.coordinates[route[0]],
                    'is_moving': False,
                    'color': self.colors[i],
                    'wait_time': 0,
                    'service_time': random.randint(30, 60),  # 30-60 frames
                    'total_customers': len([x for x in route if x != 0])
                }
                self.vehicles.append(vehicle)
    
    def setup_plot(self):
        """Thiết lập plot"""
        self.fig, self.ax = plt.subplots(figsize=(16, 12))
        self.ax.set_xlim(0, 100)
        self.ax.set_ylim(0, 100)
        self.ax.set_aspect('equal')
        self.ax.grid(True, alpha=0.3)
        self.ax.set_title('🚛 Mô Phỏng Di Chuyển Logistics - Thời Gian Thực', 
                         fontsize=16, fontweight='bold', pad=20)
        
        # Thêm text để hiển thị thông tin
        self.time_text = self.ax.text(0.02, 0.98, '', transform=self.ax.transAxes, 
                                     fontsize=12, verticalalignment='top',
                                     bbox=dict(boxstyle='round', facecolor='white', alpha=0.8))
        
        self.stats_text = self.ax.text(0.98, 0.98, '', transform=self.ax.transAxes, 
                                      fontsize=10, verticalalignment='top', horizontalalignment='right',
                                      bbox=dict(boxstyle='round', facecolor='lightblue', alpha=0.8))
        
        # Vẽ tuyến đường (static)
        self.draw_routes()
        
        # Vẽ các điểm (sẽ được cập nhật)
        self.customer_points = []
        self.vehicle_points = []
        
        # Tạo legend
        self.create_legend()
        
    def draw_routes(self):
        """Vẽ các tuyến đường"""
        for i, route in enumerate(self.routes_data):
            if len(route) > 2:
                route_coords = [self.coordinates[node] for node in route if node in self.coordinates]
                if len(route_coords) >= 2:
                    xs, ys = zip(*route_coords)
                    self.ax.plot(xs, ys, color=self.colors[i], linewidth=1.5, 
                               alpha=0.3, linestyle='--')
    
    def create_legend(self):
        """Tạo legend"""
        legend_elements = [
            mpatches.Patch(color='red', label='🏢 Depot'),
            mpatches.Patch(color='lightblue', label='👥 Khách hàng chưa phục vụ'),
            mpatches.Patch(color='lightgreen', label='✅ Khách hàng đã phục vụ'),
            mpatches.Patch(color='orange', label='🚛 Xe đang di chuyển'),
            mpatches.Patch(color='gray', label='⏸️ Xe đang dừng')
        ]
        self.ax.legend(handles=legend_elements, loc='upper left', bbox_to_anchor=(1.02, 1))
    
    def update_vehicles(self):
        """Cập nhật vị trí các xe"""
        for vehicle in self.vehicles:
            if vehicle['current_segment'] >= len(vehicle['route']) - 1:
                continue  # Xe đã hoàn thành tuyến
                
            current_node = vehicle['route'][vehicle['current_segment']]
            next_node = vehicle['route'][vehicle['current_segment'] + 1]
            
            if not vehicle['is_moving']:
                # Xe đang dừng để phục vụ
                vehicle['wait_time'] += 1
                if vehicle['wait_time'] >= vehicle['service_time']:
                    vehicle['is_moving'] = True
                    vehicle['wait_time'] = 0
                    vehicle['service_time'] = random.randint(20, 40)
                    
                    # Đánh dấu khách hàng đã được phục vụ
                    if current_node != 0:
                        self.served_customers.add(current_node)
            else:
                # Xe đang di chuyển
                vehicle['progress'] += 0.02 * self.speed_multiplier
                
                if vehicle['progress'] >= 1.0:
                    # Đã đến điểm tiếp theo
                    vehicle['current_segment'] += 1
                    vehicle['progress'] = 0.0
                    vehicle['is_moving'] = False
                    
                    if vehicle['current_segment'] < len(vehicle['route']):
                        vehicle['position'] = self.coordinates[vehicle['route'][vehicle['current_segment']]]
                else:
                    # Interpolate vị trí
                    from_pos = self.coordinates[current_node]
                    to_pos = self.coordinates[next_node]
                    
                    x = from_pos[0] + (to_pos[0] - from_pos[0]) * vehicle['progress']
                    y = from_pos[1] + (to_pos[1] - from_pos[1]) * vehicle['progress']
                    vehicle['position'] = (x, y)
    
    def animate(self, frame):
        """Hàm animation chính"""
        self.frame_count = frame
        
        # Cập nhật vehicles
        self.update_vehicles()
        
        # Xóa các điểm cũ
        for point in self.customer_points + self.vehicle_points:
            point.remove()
        self.customer_points.clear()
        self.vehicle_points.clear()
        
        # Vẽ depot
        depot_pos = self.coordinates[0]
        depot_patch = Circle(depot_pos, 1.5, color='red', zorder=5)
        self.ax.add_patch(depot_patch)
        self.customer_points.append(depot_patch)
        
        # Vẽ text cho depot
        depot_text = self.ax.text(depot_pos[0], depot_pos[1]-3, '🏢 DEPOT', 
                                ha='center', va='top', fontsize=8, fontweight='bold')
        self.customer_points.append(depot_text)
        
        # Vẽ các điểm khách hàng
        all_customers = set()
        for route in self.routes_data:
            all_customers.update([node for node in route if node != 0])
        
        for customer in all_customers:
            if customer in self.coordinates:
                pos = self.coordinates[customer]
                is_served = customer in self.served_customers
                
                color = 'lightgreen' if is_served else 'lightblue'
                marker = '✅' if is_served else str(customer)
                
                customer_patch = Circle(pos, 0.8, color=color, zorder=3)
                self.ax.add_patch(customer_patch)
                self.customer_points.append(customer_patch)
                
                if is_served:
                    customer_text = self.ax.text(pos[0], pos[1], '✓', 
                                               ha='center', va='center', fontsize=8, 
                                               color='darkgreen', fontweight='bold')
                else:
                    customer_text = self.ax.text(pos[0], pos[1], str(customer), 
                                               ha='center', va='center', fontsize=6)
                self.customer_points.append(customer_text)
        
        # Vẽ các xe
        for vehicle in self.vehicles:
            pos = vehicle['position']
            color = 'orange' if vehicle['is_moving'] else 'gray'
            size = 1.2 if vehicle['is_moving'] else 1.0
            
            vehicle_patch = Circle(pos, size, color=color, zorder=10)
            self.ax.add_patch(vehicle_patch)
            self.vehicle_points.append(vehicle_patch)
            
            # Vẽ ID xe
            vehicle_text = self.ax.text(pos[0], pos[1], str(vehicle['id']), 
                                      ha='center', va='center', fontsize=8, 
                                      color='white', fontweight='bold')
            self.vehicle_points.append(vehicle_text)
            
            # Vẽ trail nếu xe đang di chuyển
            if vehicle['is_moving'] and vehicle['progress'] > 0.1:
                current_node = vehicle['route'][vehicle['current_segment']]
                from_pos = self.coordinates[current_node]
                
                trail_x = np.linspace(from_pos[0], pos[0], 5)
                trail_y = np.linspace(from_pos[1], pos[1], 5)
                
                for i, (tx, ty) in enumerate(zip(trail_x, trail_y)):
                    alpha = (i + 1) / 5 * 0.5
                    trail_patch = Circle((tx, ty), 0.3, color=vehicle['color'], 
                                       alpha=alpha, zorder=1)
                    self.ax.add_patch(trail_patch)
                    self.vehicle_points.append(trail_patch)
        
        # Cập nhật thông tin
        self.update_info_display()
        
        return self.customer_points + self.vehicle_points
    
    def update_info_display(self):
        """Cập nhật hiển thị thông tin"""
        # Thời gian mô phỏng
        minutes = self.frame_count // 60
        seconds = self.frame_count % 60
        time_str = f"⏰ Thời gian: {minutes:02d}:{seconds:02d}"
        
        # Thống kê
        moving_vehicles = sum(1 for v in self.vehicles if v['is_moving'])
        total_customers = len(set(node for route in self.routes_data for node in route if node != 0))
        served_count = len(self.served_customers)
        
        stats = f"""📊 THỐNG KÊ REALTIME
🚛 Xe đang di chuyển: {moving_vehicles}/{len(self.vehicles)}
👥 Khách hàng đã phục vụ: {served_count}/{total_customers}
⚡ Tốc độ: {self.speed_multiplier}x
🎯 Tiến độ: {served_count/total_customers*100:.1f}%"""
        
        self.time_text.set_text(time_str)
        self.stats_text.set_text(stats)
    
    def on_key_press(self, event):
        """Xử lý phím bấm"""
        if event.key == '+' or event.key == '=':
            self.speed_multiplier = min(5.0, self.speed_multiplier + 0.5)
        elif event.key == '-':
            self.speed_multiplier = max(0.1, self.speed_multiplier - 0.5)
        elif event.key == 'r':
            # Reset animation
            self.frame_count = 0
            self.served_customers.clear()
            self.initialize_vehicles()
        elif event.key == 'q':
            plt.close('all')
    
    def run_animation(self):
        """Chạy animation"""
        print("🚛 Mô Phỏng Di Chuyển Logistics")
        print("=" * 50)
        print("⌨️  Điều khiển:")
        print("   + / = : Tăng tốc độ")
        print("   - : Giảm tốc độ")
        print("   r : Reset mô phỏng")
        print("   q : Thoát")
        print("=" * 50)
        
        # Kết nối sự kiện bàn phím
        self.fig.canvas.mpl_connect('key_press_event', self.on_key_press)
        
        # Tạo animation
        self.anim = animation.FuncAnimation(
            self.fig, self.animate, frames=None, interval=100, 
            blit=False, repeat=True
        )
        
        plt.tight_layout()
        plt.show()
        
        return self.anim

def main():
    """Hàm main"""
    try:
        print("🔄 Đang khởi tạo mô phỏng...")
        
        # Tạo animation
        vehicle_animation = VehicleAnimation()
        
        print("✅ Khởi tạo thành công!")
        print("🎬 Bắt đầu mô phỏng...")
        
        # Chạy animation
        anim = vehicle_animation.run_animation()
        
        # Giữ animation chạy
        plt.show()
        
    except KeyboardInterrupt:
        print("\n⏹️ Dừng mô phỏng")
    except Exception as e:
        print(f"❌ Lỗi: {e}")
        import traceback
        traceback.print_exc()

if __name__ == "__main__":
    main()