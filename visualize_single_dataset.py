"""
Trực quan hóa dữ liệu solution cho từng dataset riêng biệt
Tập trung vào phân tích chi tiết một dataset cụ thể
"""

import os
import matplotlib.pyplot as plt
import seaborn as sns
import pandas as pd
import numpy as np
from collections import defaultdict
import glob

# Thiết lập font tiếng Việt
plt.rcParams['font.family'] = ['DejaVu Sans', 'Arial Unicode MS', 'SimHei']
plt.rcParams['axes.unicode_minus'] = False

class SingleDatasetVisualizer:
    def __init__(self, base_path="d:/Logistic/excute_data/logistic/data"):
        """
        Khởi tạo visualizer cho một dataset cụ thể
        
        Args:
            base_path: Đường dẫn gốc chứa các thư mục dữ liệu
        """
        self.base_path = base_path
        self.solution_folders = {
            'VRPTW': 'vrptw/solution',
            'PDPTW': 'pdptw/solution', 
            'VRPSPDTW_Wang_Chen': 'vrptw_Wang_Chen/solution',
            'VRPSPDTW_Liu_Tang_Yao': 'vrpspdtw_Liu_Tang_Yao/solution'
        }
        
    def parse_solution_file(self, file_path):
        """
        Phân tích file solution và trích xuất thông tin chi tiết
        
        Args:
            file_path: Đường dẫn đến file solution
            
        Returns:
            dict: Thông tin chi tiết về solution
        """
        try:
            with open(file_path, 'r', encoding='utf-8') as f:
                lines = f.readlines()
            
            routes = []
            total_customers = 0
            active_routes = 0
            route_lengths = []
            
            for line in lines:
                line = line.strip()
                if line.startswith('Route') and ':' in line:
                    # Tách phần route: "Route 1: 0 5 87 86 ..."
                    route_part = line.split(':', 1)[1].strip()
                    if route_part:
                        nodes = [int(x) for x in route_part.split() if x.isdigit()]
                        # Loại bỏ depot (node 0) để đếm customers
                        customers_in_route = [n for n in nodes if n != 0]
                        if customers_in_route:  # Route có customers
                            routes.append(customers_in_route)
                            total_customers += len(customers_in_route)
                            active_routes += 1
                            route_lengths.append(len(customers_in_route))
            
            return {
                'total_routes': active_routes,
                'total_customers': total_customers,
                'avg_customers_per_route': total_customers / active_routes if active_routes > 0 else 0,
                'route_lengths': route_lengths,
                'routes_data': routes,
                'min_route_length': min(route_lengths) if route_lengths else 0,
                'max_route_length': max(route_lengths) if route_lengths else 0,
                'std_route_length': np.std(route_lengths) if route_lengths else 0
            }
            
        except Exception as e:
            print(f"Lỗi khi đọc file {file_path}: {e}")
            return None
    
    def collect_dataset_data(self, dataset_name):
        """
        Thu thập dữ liệu từ một dataset cụ thể
        
        Args:
            dataset_name: Tên dataset (key trong solution_folders)
            
        Returns:
            dict: Dữ liệu tổng hợp từ dataset
        """
        if dataset_name not in self.solution_folders:
            print(f"Dataset không hỗ trợ: {dataset_name}")
            print(f"Các dataset có sẵn: {list(self.solution_folders.keys())}")
            return {}
        
        folder_path = os.path.join(self.base_path, self.solution_folders[dataset_name])
        
        if not os.path.exists(folder_path):
            print(f"Thư mục không tồn tại: {folder_path}")
            return {}
        
        data = {}
        txt_files = glob.glob(os.path.join(folder_path, "*.txt"))
        
        print(f"🔍 Đang xử lý {len(txt_files)} files trong dataset {dataset_name}...")
        
        for file_path in txt_files:
            filename = os.path.basename(file_path).replace('.txt', '')
            solution_info = self.parse_solution_file(file_path)
            
            if solution_info:
                data[filename] = solution_info
        
        print(f"✅ Đã xử lý thành công {len(data)} files")
        return data
    
    def create_dataset_summary(self, dataset_name, data):
        """
        Tạo thống kê tổng hợp cho dataset
        
        Args:
            dataset_name: Tên dataset
            data: Dữ liệu từ dataset
            
        Returns:
            dict: Thống kê tổng hợp
        """
        if not data:
            return {}
        
        routes_count = [info['total_routes'] for info in data.values()]
        customers_count = [info['total_customers'] for info in data.values()]
        avg_customers = [info['avg_customers_per_route'] for info in data.values()]
        all_route_lengths = []
        
        for info in data.values():
            all_route_lengths.extend(info['route_lengths'])
        
        summary = {
            'Dataset': dataset_name,
            'Tổng số bài toán': len(data),
            'Trung bình số routes': np.mean(routes_count),
            'Trung bình số customers': np.mean(customers_count),
            'Trung bình customers/route': np.mean(avg_customers),
            'Min routes': np.min(routes_count),
            'Max routes': np.max(routes_count),
            'Min customers': np.min(customers_count),
            'Max customers': np.max(customers_count),
            'Độ lệch chuẩn routes': np.std(routes_count),
            'Độ lệch chuẩn customers': np.std(customers_count),
            'Tổng số routes': np.sum(routes_count),
            'Tổng số customers': np.sum(customers_count),
            'Route length trung bình': np.mean(all_route_lengths) if all_route_lengths else 0,
            'Route length min': np.min(all_route_lengths) if all_route_lengths else 0,
            'Route length max': np.max(all_route_lengths) if all_route_lengths else 0
        }
        
        return summary
    
    def plot_routes_histogram(self, dataset_name, data, save_path=None):
        """
        Vẽ histogram phân phối số routes
        """
        if not data:
            print("Không có dữ liệu để vẽ biểu đồ")
            return
        
        routes_count = [info['total_routes'] for info in data.values()]
        
        plt.figure(figsize=(10, 6))
        plt.hist(routes_count, bins=min(20, len(set(routes_count))), 
                alpha=0.7, color='skyblue', edgecolor='black')
        
        plt.title(f'Phân phối số Routes - Dataset {dataset_name}', 
                 fontsize=14, fontweight='bold')
        plt.xlabel('Số Routes')
        plt.ylabel('Tần suất')
        plt.grid(True, alpha=0.3)
        
        # Thêm thống kê
        mean_routes = np.mean(routes_count)
        median_routes = np.median(routes_count)
        plt.axvline(mean_routes, color='red', linestyle='--', linewidth=2, 
                   label=f'Trung bình: {mean_routes:.1f}')
        plt.axvline(median_routes, color='green', linestyle='--', linewidth=2, 
                   label=f'Trung vị: {median_routes:.1f}')
        plt.legend()
        
        # Thêm text box với thống kê
        stats_text = f'Số bài toán: {len(data)}\nMin: {min(routes_count)}\nMax: {max(routes_count)}\nStd: {np.std(routes_count):.2f}'
        plt.text(0.02, 0.98, stats_text, transform=plt.gca().transAxes, 
                verticalalignment='top', bbox=dict(boxstyle='round', facecolor='wheat', alpha=0.8))
        
        plt.tight_layout()
        
        if save_path:
            plt.savefig(save_path, dpi=300, bbox_inches='tight')
            print(f"Đã lưu biểu đồ routes: {save_path}")
        
        plt.show()
    
    def plot_customers_histogram(self, dataset_name, data, save_path=None):
        """
        Vẽ histogram phân phối số customers
        """
        if not data:
            print("Không có dữ liệu để vẽ biểu đồ")
            return
        
        customers_count = [info['total_customers'] for info in data.values()]
        
        plt.figure(figsize=(10, 6))
        plt.hist(customers_count, bins=min(20, len(set(customers_count))), 
                alpha=0.7, color='lightcoral', edgecolor='black')
        
        plt.title(f'Phân phối số Customers - Dataset {dataset_name}', 
                 fontsize=14, fontweight='bold')
        plt.xlabel('Số Customers')
        plt.ylabel('Tần suất')
        plt.grid(True, alpha=0.3)
        
        # Thêm thống kê
        mean_customers = np.mean(customers_count)
        median_customers = np.median(customers_count)
        plt.axvline(mean_customers, color='red', linestyle='--', linewidth=2, 
                   label=f'Trung bình: {mean_customers:.1f}')
        plt.axvline(median_customers, color='green', linestyle='--', linewidth=2, 
                   label=f'Trung vị: {median_customers:.1f}')
        plt.legend()
        
        # Thêm text box với thống kê
        stats_text = f'Số bài toán: {len(data)}\nMin: {min(customers_count)}\nMax: {max(customers_count)}\nStd: {np.std(customers_count):.2f}'
        plt.text(0.02, 0.98, stats_text, transform=plt.gca().transAxes, 
                verticalalignment='top', bbox=dict(boxstyle='round', facecolor='wheat', alpha=0.8))
        
        plt.tight_layout()
        
        if save_path:
            plt.savefig(save_path, dpi=300, bbox_inches='tight')
            print(f"Đã lưu biểu đồ customers: {save_path}")
        
        plt.show()
    
    def plot_efficiency_scatter(self, dataset_name, data, save_path=None):
        """
        Vẽ scatter plot mối quan hệ customers vs routes
        """
        if not data:
            print("Không có dữ liệu để vẽ biểu đồ")
            return
        
        customers = [info['total_customers'] for info in data.values()]
        routes = [info['total_routes'] for info in data.values()]
        problem_names = list(data.keys())
        
        plt.figure(figsize=(12, 8))
        scatter = plt.scatter(customers, routes, alpha=0.6, s=60, c=routes, 
                            cmap='viridis', edgecolors='black', linewidth=0.5)
        
        plt.title(f'Mối quan hệ Customers vs Routes - Dataset {dataset_name}', 
                 fontsize=14, fontweight='bold')
        plt.xlabel('Số Customers')
        plt.ylabel('Số Routes')
        plt.grid(True, alpha=0.3)
        
        # Thêm colorbar
        cbar = plt.colorbar(scatter)
        cbar.set_label('Số Routes', rotation=270, labelpad=15)
        
        # Thêm trend line
        z = np.polyfit(customers, routes, 1)
        p = np.poly1d(z)
        plt.plot(customers, p(customers), "r--", alpha=0.8, linewidth=2, 
                label=f'Trend line: y = {z[0]:.3f}x + {z[1]:.2f}')
        plt.legend()
        
        # Thêm correlation coefficient
        correlation = np.corrcoef(customers, routes)[0, 1]
        plt.text(0.02, 0.98, f'Correlation: {correlation:.3f}', 
                transform=plt.gca().transAxes, verticalalignment='top',
                bbox=dict(boxstyle='round', facecolor='wheat', alpha=0.8))
        
        plt.tight_layout()
        
        if save_path:
            plt.savefig(save_path, dpi=300, bbox_inches='tight')
            print(f"Đã lưu biểu đồ scatter: {save_path}")
        
        plt.show()
    
    def plot_route_length_distribution(self, dataset_name, data, save_path=None):
        """
        Vẽ biểu đồ phân phối độ dài routes
        """
        if not data:
            print("Không có dữ liệu để vẽ biểu đồ")
            return
        
        all_route_lengths = []
        for info in data.values():
            all_route_lengths.extend(info['route_lengths'])
        
        if not all_route_lengths:
            print("Không có dữ liệu route lengths")
            return
        
        plt.figure(figsize=(12, 6))
        
        # Subplot 1: Histogram
        plt.subplot(1, 2, 1)
        plt.hist(all_route_lengths, bins=min(20, len(set(all_route_lengths))), 
                alpha=0.7, color='lightgreen', edgecolor='black')
        plt.title('Phân phối độ dài Routes', fontweight='bold')
        plt.xlabel('Số Customers trong Route')
        plt.ylabel('Tần suất')
        plt.grid(True, alpha=0.3)
        
        # Thêm thống kê
        mean_length = np.mean(all_route_lengths)
        plt.axvline(mean_length, color='red', linestyle='--', linewidth=2, 
                   label=f'Trung bình: {mean_length:.1f}')
        plt.legend()
        
        # Subplot 2: Box plot
        plt.subplot(1, 2, 2)
        plt.boxplot(all_route_lengths, vert=True)
        plt.title('Box Plot độ dài Routes', fontweight='bold')
        plt.ylabel('Số Customers trong Route')
        plt.grid(True, alpha=0.3)
        
        plt.suptitle(f'Phân tích độ dài Routes - Dataset {dataset_name}', 
                    fontsize=14, fontweight='bold')
        plt.tight_layout()
        
        if save_path:
            plt.savefig(save_path, dpi=300, bbox_inches='tight')
            print(f"Đã lưu biểu đồ route lengths: {save_path}")
        
        plt.show()
    
    def plot_problem_comparison(self, dataset_name, data, save_path=None):
        """
        Vẽ biểu đồ so sánh các bài toán trong dataset
        """
        if not data:
            print("Không có dữ liệu để vẽ biểu đồ")
            return
        
        # Sắp xếp theo tên bài toán
        sorted_problems = sorted(data.items())
        problem_names = [name for name, _ in sorted_problems]
        routes_data = [info['total_routes'] for _, info in sorted_problems]
        customers_data = [info['total_customers'] for _, info in sorted_problems]
        
        # Chỉ hiển thị tối đa 30 bài toán để tránh quá tải
        if len(problem_names) > 30:
            step = len(problem_names) // 30
            problem_names = problem_names[::step]
            routes_data = routes_data[::step]
            customers_data = customers_data[::step]
        
        fig, (ax1, ax2) = plt.subplots(2, 1, figsize=(15, 10))
        
        x = range(len(problem_names))
        
        # Biểu đồ 1: Số routes
        ax1.bar(x, routes_data, alpha=0.7, color='skyblue', edgecolor='black')
        ax1.set_title(f'Số Routes theo từng bài toán - Dataset {dataset_name}', fontweight='bold')
        ax1.set_ylabel('Số Routes')
        ax1.grid(True, alpha=0.3)
        ax1.set_xticks(x)
        ax1.set_xticklabels(problem_names, rotation=45, ha='right')
        
        # Biểu đồ 2: Số customers
        ax2.bar(x, customers_data, alpha=0.7, color='lightcoral', edgecolor='black')
        ax2.set_title(f'Số Customers theo từng bài toán - Dataset {dataset_name}', fontweight='bold')
        ax2.set_ylabel('Số Customers')
        ax2.set_xlabel('Bài toán')
        ax2.grid(True, alpha=0.3)
        ax2.set_xticks(x)
        ax2.set_xticklabels(problem_names, rotation=45, ha='right')
        
        plt.tight_layout()
        
        if save_path:
            plt.savefig(save_path, dpi=300, bbox_inches='tight')
            print(f"Đã lưu biểu đồ so sánh: {save_path}")
        
        plt.show()
    
    def visualize_dataset(self, dataset_name, save_plots=True, save_excel=True):
        """
        Trực quan hóa toàn bộ một dataset
        
        Args:
            dataset_name: Tên dataset cần trực quan hóa
            save_plots: Có lưu biểu đồ không
            save_excel: Có lưu báo cáo Excel không
        """
        print(f"🚀 Bắt đầu trực quan hóa dataset: {dataset_name}")
        print("=" * 60)
        
        # Thu thập dữ liệu
        data = self.collect_dataset_data(dataset_name)
        
        if not data:
            print("❌ Không tìm thấy dữ liệu để trực quan hóa!")
            return
        
        # Tạo thống kê tổng hợp
        summary = self.create_dataset_summary(dataset_name, data)
        
        print(f"\n📊 THỐNG KÊ TỔNG HỢP - {dataset_name}")
        print("-" * 40)
        for key, value in summary.items():
            if isinstance(value, float):
                print(f"{key}: {value:.2f}")
            else:
                print(f"{key}: {value}")
        
        # Tạo thư mục lưu plots
        plots_dir = f"{dataset_name.lower()}_analysis_plots"
        if save_plots and not os.path.exists(plots_dir):
            os.makedirs(plots_dir)
        
        # Vẽ các biểu đồ
        print(f"\n📈 Tạo biểu đồ phân phối routes...")
        self.plot_routes_histogram(
            dataset_name, data,
            save_path=os.path.join(plots_dir, f"{dataset_name.lower()}_routes_histogram.png") if save_plots else None
        )
        
        print(f"\n📊 Tạo biểu đồ phân phối customers...")
        self.plot_customers_histogram(
            dataset_name, data,
            save_path=os.path.join(plots_dir, f"{dataset_name.lower()}_customers_histogram.png") if save_plots else None
        )
        
        print(f"\n🔍 Tạo biểu đồ mối quan hệ customers vs routes...")
        self.plot_efficiency_scatter(
            dataset_name, data,
            save_path=os.path.join(plots_dir, f"{dataset_name.lower()}_efficiency_scatter.png") if save_plots else None
        )
        
        print(f"\n📏 Tạo biểu đồ phân phối độ dài routes...")
        self.plot_route_length_distribution(
            dataset_name, data,
            save_path=os.path.join(plots_dir, f"{dataset_name.lower()}_route_lengths.png") if save_plots else None
        )
        
        print(f"\n📋 Tạo biểu đồ so sánh các bài toán...")
        self.plot_problem_comparison(
            dataset_name, data,
            save_path=os.path.join(plots_dir, f"{dataset_name.lower()}_problems_comparison.png") if save_plots else None
        )
        
        # Lưu báo cáo Excel
        if save_excel:
            detailed_data = []
            for problem_name, info in data.items():
                detailed_data.append({
                    'Problem': problem_name,
                    'Total_Routes': info['total_routes'],
                    'Total_Customers': info['total_customers'],
                    'Avg_Customers_per_Route': info['avg_customers_per_route'],
                    'Min_Route_Length': info['min_route_length'],
                    'Max_Route_Length': info['max_route_length'],
                    'Std_Route_Length': info['std_route_length']
                })
            
            detailed_df = pd.DataFrame(detailed_data)
            summary_df = pd.DataFrame([summary])
            
            excel_path = f"{dataset_name.lower()}_analysis_report.xlsx"
            with pd.ExcelWriter(excel_path, engine='openpyxl') as writer:
                summary_df.to_excel(writer, sheet_name='Summary', index=False)
                detailed_df.to_excel(writer, sheet_name='Detailed', index=False)
            
            print(f"\n💾 Đã lưu báo cáo Excel: {excel_path}")
        
        print(f"\n✅ Hoàn thành trực quan hóa dataset {dataset_name}!")
        if save_plots:
            print(f"📁 Kiểm tra thư mục '{plots_dir}' để xem các biểu đồ")
        
        return data, summary


def main():
    """
    Hàm chính để chạy trực quan hóa từng dataset riêng biệt
    """
    print("=" * 70)
    print("🎯 TRỰC QUAN HÓA DATASET RIÊNG BIỆT")
    print("=" * 70)
    
    # Khởi tạo visualizer
    visualizer = SingleDatasetVisualizer()
    
    # Menu lựa chọn
    print("\nCác dataset có sẵn:")
    datasets = list(visualizer.solution_folders.keys())
    for i, dataset in enumerate(datasets, 1):
        print(f"{i}. {dataset}")
    
    print("\nTùy chọn:")
    print("0. Thoát")
    
    while True:
        try:
            choice = input(f"\nNhập lựa chọn (0-{len(datasets)}): ").strip()
            
            if choice == '0':
                print("👋 Tạm biệt!")
                break
            
            choice_idx = int(choice) - 1
            if 0 <= choice_idx < len(datasets):
                dataset_name = datasets[choice_idx]
                print(f"\n🎯 Đã chọn dataset: {dataset_name}")
                
                # Tùy chọn lưu file
                save_plots = input("Lưu biểu đồ? (y/n, mặc định y): ").strip().lower()
                save_plots = save_plots != 'n'
                
                save_excel = input("Lưu báo cáo Excel? (y/n, mặc định y): ").strip().lower()
                save_excel = save_excel != 'n'
                
                # Thực hiện trực quan hóa
                data, summary = visualizer.visualize_dataset(
                    dataset_name, 
                    save_plots=save_plots, 
                    save_excel=save_excel
                )
                
                # Hỏi có muốn tiếp tục không
                continue_choice = input("\nTiếp tục với dataset khác? (y/n): ").strip().lower()
                if continue_choice == 'n':
                    print("👋 Tạm biệt!")
                    break
                    
            else:
                print("❌ Lựa chọn không hợp lệ!")
                
        except ValueError:
            print("❌ Vui lòng nhập số!")
        except KeyboardInterrupt:
            print("\n👋 Tạm biệt!")
            break


if __name__ == "__main__":
    main()