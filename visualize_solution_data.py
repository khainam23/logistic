"""
Trực quan hóa dữ liệu solution cho các bài toán VRP khác nhau
Hỗ trợ nhiều loại dataset: VRPTW, PDPTW, VRPSPDTW
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

class SolutionDataVisualizer:
    def __init__(self, base_path="d:/Logistic/excute_data/logistic/data"):
        """
        Khởi tạo visualizer với đường dẫn gốc
        
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
        Phân tích file solution và trích xuất thông tin
        
        Args:
            file_path: Đường dẫn đến file solution
            
        Returns:
            dict: Thông tin về solution (số routes, số customers, tổng distance)
        """
        try:
            with open(file_path, 'r', encoding='utf-8') as f:
                lines = f.readlines()
            
            routes = []
            total_customers = 0
            active_routes = 0
            
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
            
            return {
                'total_routes': active_routes,
                'total_customers': total_customers,
                'avg_customers_per_route': total_customers / active_routes if active_routes > 0 else 0,
                'routes_data': routes
            }
            
        except Exception as e:
            print(f"Lỗi khi đọc file {file_path}: {e}")
            return None
    
    def collect_data_from_folder(self, folder_name):
        """
        Thu thập dữ liệu từ một thư mục solution
        
        Args:
            folder_name: Tên thư mục (key trong solution_folders)
            
        Returns:
            dict: Dữ liệu tổng hợp từ thư mục
        """
        folder_path = os.path.join(self.base_path, self.solution_folders[folder_name])
        
        if not os.path.exists(folder_path):
            print(f"Thư mục không tồn tại: {folder_path}")
            return {}
        
        data = {}
        txt_files = glob.glob(os.path.join(folder_path, "*.txt"))
        
        print(f"Đang xử lý {len(txt_files)} files trong {folder_name}...")
        
        for file_path in txt_files:
            filename = os.path.basename(file_path).replace('.txt', '')
            solution_info = self.parse_solution_file(file_path)
            
            if solution_info:
                data[filename] = solution_info
        
        return data
    
    def create_summary_statistics(self, all_data):
        """
        Tạo thống kê tổng hợp cho tất cả datasets
        
        Args:
            all_data: Dictionary chứa dữ liệu từ tất cả folders
            
        Returns:
            pandas.DataFrame: Bảng thống kê tổng hợp
        """
        summary_data = []
        
        for dataset_name, dataset_data in all_data.items():
            if not dataset_data:
                continue
                
            routes_count = [info['total_routes'] for info in dataset_data.values()]
            customers_count = [info['total_customers'] for info in dataset_data.values()]
            avg_customers = [info['avg_customers_per_route'] for info in dataset_data.values()]
            
            summary_data.append({
                'Dataset': dataset_name,
                'Số bài toán': len(dataset_data),
                'Trung bình số routes': np.mean(routes_count),
                'Trung bình số customers': np.mean(customers_count),
                'Trung bình customers/route': np.mean(avg_customers),
                'Min routes': np.min(routes_count) if routes_count else 0,
                'Max routes': np.max(routes_count) if routes_count else 0,
                'Min customers': np.min(customers_count) if customers_count else 0,
                'Max customers': np.max(customers_count) if customers_count else 0
            })
        
        return pd.DataFrame(summary_data)
    
    def plot_routes_distribution(self, all_data, save_path=None):
        """
        Vẽ biểu đồ phân phối số routes cho các datasets
        """
        fig, axes = plt.subplots(2, 2, figsize=(15, 12))
        fig.suptitle('Phân phối số Routes theo Dataset', fontsize=16, fontweight='bold')
        
        datasets = list(all_data.keys())
        colors = ['skyblue', 'lightcoral', 'lightgreen', 'gold']
        
        for i, (dataset_name, dataset_data) in enumerate(all_data.items()):
            if not dataset_data:
                continue
                
            row, col = i // 2, i % 2
            ax = axes[row, col]
            
            routes_count = [info['total_routes'] for info in dataset_data.values()]
            
            ax.hist(routes_count, bins=20, alpha=0.7, color=colors[i % len(colors)], edgecolor='black')
            ax.set_title(f'{dataset_name}\n({len(dataset_data)} bài toán)', fontweight='bold')
            ax.set_xlabel('Số Routes')
            ax.set_ylabel('Tần suất')
            ax.grid(True, alpha=0.3)
            
            # Thêm thống kê
            mean_routes = np.mean(routes_count)
            ax.axvline(mean_routes, color='red', linestyle='--', linewidth=2, 
                      label=f'Trung bình: {mean_routes:.1f}')
            ax.legend()
        
        plt.tight_layout()
        
        if save_path:
            plt.savefig(save_path, dpi=300, bbox_inches='tight')
            print(f"Đã lưu biểu đồ phân phối routes: {save_path}")
        
        plt.show()
    
    def plot_customers_analysis(self, all_data, save_path=None):
        """
        Vẽ biểu đồ phân tích số customers
        """
        fig, (ax1, ax2) = plt.subplots(1, 2, figsize=(16, 6))
        fig.suptitle('Phân tích số Customers theo Dataset', fontsize=16, fontweight='bold')
        
        # Biểu đồ 1: Boxplot số customers
        customers_data = []
        labels = []
        
        for dataset_name, dataset_data in all_data.items():
            if dataset_data:
                customers_count = [info['total_customers'] for info in dataset_data.values()]
                customers_data.append(customers_count)
                labels.append(f'{dataset_name}\n(n={len(customers_count)})')
        
        ax1.boxplot(customers_data, labels=labels)
        ax1.set_title('Phân phối số Customers', fontweight='bold')
        ax1.set_ylabel('Số Customers')
        ax1.grid(True, alpha=0.3)
        ax1.tick_params(axis='x', rotation=45)
        
        # Biểu đồ 2: Scatter plot customers vs routes
        colors = ['blue', 'red', 'green', 'orange']
        
        for i, (dataset_name, dataset_data) in enumerate(all_data.items()):
            if dataset_data:
                customers = [info['total_customers'] for info in dataset_data.values()]
                routes = [info['total_routes'] for info in dataset_data.values()]
                
                ax2.scatter(customers, routes, alpha=0.6, s=50, 
                           color=colors[i % len(colors)], label=dataset_name)
        
        ax2.set_title('Mối quan hệ Customers vs Routes', fontweight='bold')
        ax2.set_xlabel('Số Customers')
        ax2.set_ylabel('Số Routes')
        ax2.legend()
        ax2.grid(True, alpha=0.3)
        
        plt.tight_layout()
        
        if save_path:
            plt.savefig(save_path, dpi=300, bbox_inches='tight')
            print(f"Đã lưu biểu đồ phân tích customers: {save_path}")
        
        plt.show()
    
    def plot_efficiency_analysis(self, all_data, save_path=None):
        """
        Vẽ biểu đồ phân tích hiệu quả (customers per route)
        """
        fig, ax = plt.subplots(figsize=(12, 8))
        
        dataset_names = []
        efficiency_data = []
        
        for dataset_name, dataset_data in all_data.items():
            if dataset_data:
                avg_customers = [info['avg_customers_per_route'] for info in dataset_data.values()]
                efficiency_data.extend(avg_customers)
                dataset_names.extend([dataset_name] * len(avg_customers))
        
        # Tạo DataFrame cho seaborn
        df = pd.DataFrame({
            'Dataset': dataset_names,
            'Customers_per_Route': efficiency_data
        })
        
        # Violin plot
        sns.violinplot(data=df, x='Dataset', y='Customers_per_Route', ax=ax)
        ax.set_title('Phân phối hiệu quả Routes (Customers/Route)', fontsize=14, fontweight='bold')
        ax.set_ylabel('Số Customers trung bình mỗi Route')
        ax.set_xlabel('Dataset')
        ax.grid(True, alpha=0.3)
        plt.xticks(rotation=45)
        
        plt.tight_layout()
        
        if save_path:
            plt.savefig(save_path, dpi=300, bbox_inches='tight')
            print(f"Đã lưu biểu đồ phân tích hiệu quả: {save_path}")
        
        plt.show()
    
    def generate_detailed_report(self, folder_name=None, save_excel=None):
        """
        Tạo báo cáo chi tiết cho một hoặc tất cả datasets
        
        Args:
            folder_name: Tên folder cụ thể (None để xử lý tất cả)
            save_excel: Đường dẫn lưu file Excel
        """
        if folder_name:
            folders_to_process = {folder_name: self.solution_folders[folder_name]}
        else:
            folders_to_process = self.solution_folders
        
        all_data = {}
        detailed_data = []
        
        for name, folder in folders_to_process.items():
            print(f"\n=== Xử lý dataset: {name} ===")
            data = self.collect_data_from_folder(name)
            all_data[name] = data
            
            # Thu thập dữ liệu chi tiết
            for problem_name, info in data.items():
                detailed_data.append({
                    'Dataset': name,
                    'Problem': problem_name,
                    'Total_Routes': info['total_routes'],
                    'Total_Customers': info['total_customers'],
                    'Avg_Customers_per_Route': info['avg_customers_per_route']
                })
        
        # Tạo summary statistics
        summary_df = self.create_summary_statistics(all_data)
        print("\n=== THỐNG KÊ TỔNG HỢP ===")
        print(summary_df.to_string(index=False))
        
        # Lưu Excel nếu được yêu cầu
        if save_excel:
            detailed_df = pd.DataFrame(detailed_data)
            with pd.ExcelWriter(save_excel, engine='openpyxl') as writer:
                summary_df.to_excel(writer, sheet_name='Summary', index=False)
                detailed_df.to_excel(writer, sheet_name='Detailed', index=False)
            print(f"\nĐã lưu báo cáo Excel: {save_excel}")
        
        return all_data, summary_df
    
    def visualize_all(self, folder_name=None, save_plots=True):
        """
        Tạo tất cả các biểu đồ trực quan hóa
        
        Args:
            folder_name: Tên folder cụ thể (None để xử lý tất cả)
            save_plots: Có lưu biểu đồ không
        """
        print("🚀 Bắt đầu trực quan hóa dữ liệu solution...")
        
        # Thu thập dữ liệu
        all_data, summary_df = self.generate_detailed_report(folder_name)
        
        if not any(all_data.values()):
            print("❌ Không tìm thấy dữ liệu để trực quan hóa!")
            return
        
        # Tạo thư mục lưu plots
        plots_dir = "solution_analysis_plots"
        if save_plots and not os.path.exists(plots_dir):
            os.makedirs(plots_dir)
        
        # Vẽ các biểu đồ
        print("\n📊 Tạo biểu đồ phân phối routes...")
        self.plot_routes_distribution(
            all_data, 
            save_path=os.path.join(plots_dir, "routes_distribution.png") if save_plots else None
        )
        
        print("\n📈 Tạo biểu đồ phân tích customers...")
        self.plot_customers_analysis(
            all_data,
            save_path=os.path.join(plots_dir, "customers_analysis.png") if save_plots else None
        )
        
        print("\n📉 Tạo biểu đồ phân tích hiệu quả...")
        self.plot_efficiency_analysis(
            all_data,
            save_path=os.path.join(plots_dir, "efficiency_analysis.png") if save_plots else None
        )
        
        print("\n✅ Hoàn thành trực quan hóa dữ liệu!")
        
        return all_data, summary_df


def main():
    """
    Hàm chính để chạy trực quan hóa
    """
    print("=" * 60)
    print("🎯 TRỰC QUAN HÓA DỮ LIỆU SOLUTION VRP")
    print("=" * 60)
    
    # Khởi tạo visualizer
    visualizer = SolutionDataVisualizer()
    
    # Menu lựa chọn
    print("\nCác tùy chọn:")
    print("1. Trực quan hóa tất cả datasets")
    print("2. Trực quan hóa VRPTW")
    print("3. Trực quan hóa PDPTW") 
    print("4. Trực quan hóa VRPSPDTW Wang Chen")
    print("5. Trực quan hóa VRPSPDTW Liu Tang Yao")
    
    choice = input("\nNhập lựa chọn (1-5): ").strip()
    
    folder_mapping = {
        '2': 'VRPTW',
        '3': 'PDPTW',
        '4': 'VRPSPDTW_Wang_Chen', 
        '5': 'VRPSPDTW_Liu_Tang_Yao'
    }
    
    if choice == '1':
        # Trực quan hóa tất cả
        all_data, summary_df = visualizer.visualize_all()
        
        # Lưu báo cáo Excel
        visualizer.generate_detailed_report(save_excel="solution_analysis_report.xlsx")
        
    elif choice in folder_mapping:
        # Trực quan hóa dataset cụ thể
        folder_name = folder_mapping[choice]
        print(f"\n🎯 Trực quan hóa dataset: {folder_name}")
        
        all_data, summary_df = visualizer.visualize_all(folder_name=folder_name)
        
        # Lưu báo cáo Excel cho dataset cụ thể
        excel_name = f"solution_analysis_{folder_name.lower()}.xlsx"
        visualizer.generate_detailed_report(folder_name=folder_name, save_excel=excel_name)
        
    else:
        print("❌ Lựa chọn không hợp lệ!")
        return
    
    print(f"\n🎉 Hoàn thành! Kiểm tra thư mục 'solution_analysis_plots' để xem các biểu đồ.")


if __name__ == "__main__":
    main()