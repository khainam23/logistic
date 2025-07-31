"""
Trực quan hóa kết quả solution cho bài toán VRPSPDTW bằng biểu đồ
Dataset: Wang_Chen
"""

import os
import matplotlib.pyplot as plt
import matplotlib.patches as patches
import seaborn as sns
import pandas as pd
import numpy as np
from typing import Dict, List, Tuple
from dataclasses import dataclass
import statistics

# Cấu hình matplotlib để hiển thị tiếng Việt
plt.rcParams['font.family'] = ['DejaVu Sans', 'Arial Unicode MS', 'SimHei']
plt.rcParams['axes.unicode_minus'] = False

@dataclass
class SolutionStats:
    """Thống kê cho một solution"""
    instance_name: str
    num_routes: int
    total_customers: int
    served_customers: int
    unserved_customers: int
    avg_route_length: float
    min_route_length: int
    max_route_length: int
    route_lengths: List[int]
    routes: List[List[int]]

class SolutionVisualizer:
    """Trực quan hóa tập solution"""
    
    def __init__(self, solution_folder: str, output_folder: str):
        self.solution_folder = solution_folder
        self.output_folder = output_folder
        self.solutions: Dict[str, SolutionStats] = {}
        
        # Tạo thư mục output nếu chưa có
        if not os.path.exists(output_folder):
            os.makedirs(output_folder)
    
    def load_solutions(self):
        """Đọc tất cả file solution"""
        if not os.path.exists(self.solution_folder):
            print(f"Không tìm thấy thư mục solution: {self.solution_folder}")
            return
        
        solution_files = [f for f in os.listdir(self.solution_folder) if f.endswith('.txt')]
        solution_files.sort()
        
        print(f"Đang load {len(solution_files)} solution files...")
        
        for filename in solution_files:
            filepath = os.path.join(self.solution_folder, filename)
            try:
                stats = self._analyze_single_solution(filepath)
                if stats:
                    self.solutions[stats.instance_name] = stats
            except Exception as e:
                print(f"Lỗi khi phân tích {filename}: {str(e)}")
                continue
        
        print(f"Đã load thành công {len(self.solutions)} solutions!")
    
    def _analyze_single_solution(self, filepath: str) -> SolutionStats:
        """Phân tích một file solution"""
        with open(filepath, 'r', encoding='utf-8') as f:
            lines = f.readlines()
        
        if not lines:
            return None
        
        instance_name = lines[0].strip()
        routes = []
        
        # Đọc các route
        for line in lines[1:]:
            line = line.strip()
            if line.startswith('Route'):
                route_part = line.split(':', 1)[1].strip()
                route = [int(x) for x in route_part.split()]
                routes.append(route)
        
        # Tính toán thống kê
        num_routes = len(routes)
        route_lengths = [len(route) - 2 for route in routes]  # Trừ depot đầu và cuối
        
        # Đếm khách hàng được phục vụ
        served_customers_set = set()
        for route in routes:
            for customer in route[1:-1]:  # Bỏ depot đầu và cuối
                served_customers_set.add(customer)
        
        served_customers = len(served_customers_set)
        
        # Ước tính tổng số khách hàng
        if instance_name.lower().startswith('cdp'):
            if len(instance_name) > 3 and instance_name[3:].isdigit():
                total_customers = int(instance_name[3:])
            else:
                total_customers = served_customers
        elif instance_name.lower().startswith('rcdp') or instance_name.lower().startswith('rdp'):
            # Tìm số trong tên
            import re
            numbers = re.findall(r'\d+', instance_name)
            if numbers:
                total_customers = int(numbers[-1])  # Lấy số cuối
            else:
                total_customers = served_customers
        else:
            total_customers = served_customers
        
        unserved_customers = max(0, total_customers - served_customers)
        
        avg_route_length = statistics.mean(route_lengths) if route_lengths else 0
        min_route_length = min(route_lengths) if route_lengths else 0
        max_route_length = max(route_lengths) if route_lengths else 0
        
        return SolutionStats(
            instance_name=instance_name,
            num_routes=num_routes,
            total_customers=total_customers,
            served_customers=served_customers,
            unserved_customers=unserved_customers,
            avg_route_length=avg_route_length,
            min_route_length=min_route_length,
            max_route_length=max_route_length,
            route_lengths=route_lengths,
            routes=routes
        )
    
    def plot_overview_statistics(self):
        """Biểu đồ tổng quan thống kê"""
        if not self.solutions:
            return
        
        fig, axes = plt.subplots(2, 2, figsize=(15, 12))
        fig.suptitle('TỔNG QUAN THỐNG KÊ SOLUTION - THUẬT TOÁN GREEDY VRPSPDTW', 
                     fontsize=16, fontweight='bold')
        
        # 1. Số lượng routes theo instance
        instances = list(self.solutions.keys())
        num_routes = [self.solutions[inst].num_routes for inst in instances]
        
        axes[0, 0].bar(range(len(instances)), num_routes, color='skyblue', alpha=0.7)
        axes[0, 0].set_title('Số Routes theo Instance')
        axes[0, 0].set_xlabel('Instance')
        axes[0, 0].set_ylabel('Số Routes')
        axes[0, 0].tick_params(axis='x', rotation=45)
        if len(instances) <= 20:
            axes[0, 0].set_xticks(range(len(instances)))
            axes[0, 0].set_xticklabels(instances, rotation=45, ha='right')
        
        # 2. Tỷ lệ phục vụ khách hàng
        service_rates = []
        for inst in instances:
            stats = self.solutions[inst]
            if stats.total_customers > 0:
                rate = (stats.served_customers / stats.total_customers) * 100
            else:
                rate = 100
            service_rates.append(rate)
        
        colors = ['green' if rate == 100 else 'orange' if rate >= 80 else 'red' for rate in service_rates]
        axes[0, 1].bar(range(len(instances)), service_rates, color=colors, alpha=0.7)
        axes[0, 1].set_title('Tỷ Lệ Phục Vụ Khách Hàng (%)')
        axes[0, 1].set_xlabel('Instance')
        axes[0, 1].set_ylabel('Tỷ Lệ (%)')
        axes[0, 1].set_ylim(0, 105)
        axes[0, 1].tick_params(axis='x', rotation=45)
        if len(instances) <= 20:
            axes[0, 1].set_xticks(range(len(instances)))
            axes[0, 1].set_xticklabels(instances, rotation=45, ha='right')
        
        # 3. Phân phối độ dài route trung bình
        avg_lengths = [self.solutions[inst].avg_route_length for inst in instances]
        axes[1, 0].hist(avg_lengths, bins=20, color='lightcoral', alpha=0.7, edgecolor='black')
        axes[1, 0].set_title('Phân Phối Độ Dài Route Trung Bình')
        axes[1, 0].set_xlabel('Độ Dài Route Trung Bình')
        axes[1, 0].set_ylabel('Số Instance')
        
        # 4. Tổng số khách hàng được phục vụ vs chưa phục vụ
        total_served = sum(stats.served_customers for stats in self.solutions.values())
        total_unserved = sum(stats.unserved_customers for stats in self.solutions.values())
        
        labels = ['Đã Phục Vụ', 'Chưa Phục Vụ']
        sizes = [total_served, total_unserved]
        colors = ['lightgreen', 'lightcoral']
        
        if total_unserved > 0:
            axes[1, 1].pie(sizes, labels=labels, colors=colors, autopct='%1.1f%%', startangle=90)
        else:
            axes[1, 1].pie([100], labels=['Đã Phục Vụ 100%'], colors=['lightgreen'], 
                          autopct='%1.1f%%', startangle=90)
        
        axes[1, 1].set_title('Tỷ Lệ Khách Hàng Được Phục Vụ')
        
        plt.tight_layout()
        plt.savefig(os.path.join(self.output_folder, 'overview_statistics.png'), 
                   dpi=300, bbox_inches='tight')
        plt.close()
        
        print("✓ Đã tạo biểu đồ tổng quan: overview_statistics.png")
    
    def plot_route_length_analysis(self):
        """Phân tích chi tiết độ dài route"""
        if not self.solutions:
            return
        
        # Thu thập tất cả độ dài route
        all_route_lengths = []
        for stats in self.solutions.values():
            all_route_lengths.extend(stats.route_lengths)
        
        fig, axes = plt.subplots(2, 2, figsize=(15, 12))
        fig.suptitle('PHÂN TÍCH ĐỘ DÀI ROUTE', fontsize=16, fontweight='bold')
        
        # 1. Histogram độ dài route
        axes[0, 0].hist(all_route_lengths, bins=range(1, max(all_route_lengths)+2), 
                       color='skyblue', alpha=0.7, edgecolor='black')
        axes[0, 0].set_title('Phân Phối Độ Dài Route')
        axes[0, 0].set_xlabel('Độ Dài Route (số khách hàng)')
        axes[0, 0].set_ylabel('Số Lượng Route')
        
        # 2. Box plot độ dài route theo nhóm instance
        instance_groups = {}
        for inst_name, stats in self.solutions.items():
            if inst_name.lower().startswith('cdp'):
                group = 'CDP'
            elif inst_name.lower().startswith('rcdp'):
                group = 'RCDP'
            elif inst_name.lower().startswith('rdp'):
                group = 'RDP'
            else:
                group = 'Other'
            
            if group not in instance_groups:
                instance_groups[group] = []
            instance_groups[group].extend(stats.route_lengths)
        
        if len(instance_groups) > 1:
            data_for_boxplot = []
            labels_for_boxplot = []
            for group, lengths in instance_groups.items():
                data_for_boxplot.append(lengths)
                labels_for_boxplot.append(group)
            
            axes[0, 1].boxplot(data_for_boxplot, labels=labels_for_boxplot)
            axes[0, 1].set_title('Độ Dài Route theo Nhóm Instance')
            axes[0, 1].set_ylabel('Độ Dài Route')
        else:
            axes[0, 1].text(0.5, 0.5, 'Chỉ có một nhóm instance', 
                           ha='center', va='center', transform=axes[0, 1].transAxes)
            axes[0, 1].set_title('Độ Dài Route theo Nhóm Instance')
        
        # 3. Scatter plot: Số routes vs Độ dài trung bình
        num_routes = [stats.num_routes for stats in self.solutions.values()]
        avg_lengths = [stats.avg_route_length for stats in self.solutions.values()]
        
        axes[1, 0].scatter(num_routes, avg_lengths, alpha=0.6, s=50)
        axes[1, 0].set_title('Số Routes vs Độ Dài Route Trung Bình')
        axes[1, 0].set_xlabel('Số Routes')
        axes[1, 0].set_ylabel('Độ Dài Route Trung Bình')
        
        # Thêm trendline
        if len(num_routes) > 1:
            z = np.polyfit(num_routes, avg_lengths, 1)
            p = np.poly1d(z)
            axes[1, 0].plot(num_routes, p(num_routes), "r--", alpha=0.8)
        
        # 4. Top 10 instances có route dài nhất
        max_route_lengths = [(inst, stats.max_route_length) 
                           for inst, stats in self.solutions.items()]
        max_route_lengths.sort(key=lambda x: x[1], reverse=True)
        top_10 = max_route_lengths[:10]
        
        if top_10:
            instances_top = [x[0] for x in top_10]
            lengths_top = [x[1] for x in top_10]
            
            axes[1, 1].barh(range(len(instances_top)), lengths_top, color='lightcoral')
            axes[1, 1].set_title('Top 10 Route Dài Nhất')
            axes[1, 1].set_xlabel('Độ Dài Route Tối Đa')
            axes[1, 1].set_yticks(range(len(instances_top)))
            axes[1, 1].set_yticklabels(instances_top)
            axes[1, 1].invert_yaxis()
        
        plt.tight_layout()
        plt.savefig(os.path.join(self.output_folder, 'route_length_analysis.png'), 
                   dpi=300, bbox_inches='tight')
        plt.close()
        
        print("✓ Đã tạo biểu đồ phân tích độ dài route: route_length_analysis.png")
    
    def plot_performance_comparison(self):
        """So sánh hiệu suất giữa các instance"""
        if not self.solutions:
            return
        
        # Tạo DataFrame cho dễ xử lý
        data = []
        for inst_name, stats in self.solutions.items():
            service_rate = (stats.served_customers / max(stats.total_customers, 1)) * 100
            data.append({
                'Instance': inst_name,
                'Num_Routes': stats.num_routes,
                'Service_Rate': service_rate,
                'Avg_Route_Length': stats.avg_route_length,
                'Total_Customers': stats.total_customers,
                'Served_Customers': stats.served_customers,
                'Unserved_Customers': stats.unserved_customers
            })
        
        df = pd.DataFrame(data)
        
        fig, axes = plt.subplots(2, 2, figsize=(15, 12))
        fig.suptitle('SO SÁNH HIỆU SUẤT GIỮA CÁC INSTANCE', fontsize=16, fontweight='bold')
        
        # 1. Heatmap correlation
        numeric_cols = ['Num_Routes', 'Service_Rate', 'Avg_Route_Length', 'Total_Customers']
        corr_matrix = df[numeric_cols].corr()
        
        sns.heatmap(corr_matrix, annot=True, cmap='coolwarm', center=0, 
                   square=True, ax=axes[0, 0])
        axes[0, 0].set_title('Ma Trận Tương Quan')
        
        # 2. Scatter plot: Total customers vs Service rate
        scatter = axes[0, 1].scatter(df['Total_Customers'], df['Service_Rate'], 
                                   c=df['Num_Routes'], cmap='viridis', alpha=0.6, s=50)
        axes[0, 1].set_title('Tổng Khách Hàng vs Tỷ Lệ Phục Vụ')
        axes[0, 1].set_xlabel('Tổng Số Khách Hàng')
        axes[0, 1].set_ylabel('Tỷ Lệ Phục Vụ (%)')
        plt.colorbar(scatter, ax=axes[0, 1], label='Số Routes')
        
        # 3. Bar chart: Top 10 instances có nhiều khách hàng chưa phục vụ nhất
        unserved_data = df[df['Unserved_Customers'] > 0].nlargest(10, 'Unserved_Customers')
        
        if not unserved_data.empty:
            axes[1, 0].bar(range(len(unserved_data)), unserved_data['Unserved_Customers'], 
                          color='red', alpha=0.7)
            axes[1, 0].set_title('Top 10 Instance Có Nhiều Khách Hàng Chưa Phục Vụ')
            axes[1, 0].set_xlabel('Instance')
            axes[1, 0].set_ylabel('Số Khách Hàng Chưa Phục Vụ')
            axes[1, 0].set_xticks(range(len(unserved_data)))
            axes[1, 0].set_xticklabels(unserved_data['Instance'], rotation=45, ha='right')
        else:
            axes[1, 0].text(0.5, 0.5, 'Tất cả khách hàng\nđều được phục vụ!', 
                           ha='center', va='center', transform=axes[1, 0].transAxes,
                           fontsize=14, color='green', fontweight='bold')
            axes[1, 0].set_title('Khách Hàng Chưa Phục Vụ')
        
        # 4. Efficiency score (Routes per customer served)
        df['Efficiency'] = df['Served_Customers'] / df['Num_Routes']
        top_efficient = df.nlargest(10, 'Efficiency')
        
        axes[1, 1].bar(range(len(top_efficient)), top_efficient['Efficiency'], 
                      color='green', alpha=0.7)
        axes[1, 1].set_title('Top 10 Instance Hiệu Quả Nhất\n(Khách hàng/Route)')
        axes[1, 1].set_xlabel('Instance')
        axes[1, 1].set_ylabel('Khách Hàng/Route')
        axes[1, 1].set_xticks(range(len(top_efficient)))
        axes[1, 1].set_xticklabels(top_efficient['Instance'], rotation=45, ha='right')
        
        plt.tight_layout()
        plt.savefig(os.path.join(self.output_folder, 'performance_comparison.png'), 
                   dpi=300, bbox_inches='tight')
        plt.close()
        
        print("✓ Đã tạo biểu đồ so sánh hiệu suất: performance_comparison.png")
    
    def plot_instance_categories(self):
        """Phân tích theo loại instance"""
        if not self.solutions:
            return
        
        # Phân loại instances
        categories = {'CDP': [], 'RCDP': [], 'RDP': [], 'Other': []}
        
        for inst_name, stats in self.solutions.items():
            if inst_name.lower().startswith('cdp'):
                categories['CDP'].append(stats)
            elif inst_name.lower().startswith('rcdp'):
                categories['RCDP'].append(stats)
            elif inst_name.lower().startswith('rdp'):
                categories['RDP'].append(stats)
            else:
                categories['Other'].append(stats)
        
        # Loại bỏ categories rỗng
        categories = {k: v for k, v in categories.items() if v}
        
        if len(categories) <= 1:
            print("Chỉ có một loại instance, bỏ qua biểu đồ phân loại")
            return
        
        fig, axes = plt.subplots(2, 2, figsize=(15, 12))
        fig.suptitle('PHÂN TÍCH THEO LOẠI INSTANCE', fontsize=16, fontweight='bold')
        
        # 1. Số lượng instance theo loại
        cat_names = list(categories.keys())
        cat_counts = [len(categories[cat]) for cat in cat_names]
        
        axes[0, 0].pie(cat_counts, labels=cat_names, autopct='%1.1f%%', startangle=90)
        axes[0, 0].set_title('Phân Bố Số Lượng Instance')
        
        # 2. Trung bình số routes theo loại
        avg_routes = []
        for cat in cat_names:
            routes = [stats.num_routes for stats in categories[cat]]
            avg_routes.append(statistics.mean(routes))
        
        axes[0, 1].bar(cat_names, avg_routes, color=['skyblue', 'lightgreen', 'lightcoral', 'gold'][:len(cat_names)])
        axes[0, 1].set_title('Trung Bình Số Routes theo Loại')
        axes[0, 1].set_ylabel('Số Routes Trung Bình')
        
        # 3. Tỷ lệ phục vụ trung bình theo loại
        avg_service_rates = []
        for cat in cat_names:
            rates = []
            for stats in categories[cat]:
                if stats.total_customers > 0:
                    rate = (stats.served_customers / stats.total_customers) * 100
                else:
                    rate = 100
                rates.append(rate)
            avg_service_rates.append(statistics.mean(rates))
        
        colors = ['green' if rate >= 95 else 'orange' if rate >= 80 else 'red' for rate in avg_service_rates]
        axes[1, 0].bar(cat_names, avg_service_rates, color=colors)
        axes[1, 0].set_title('Tỷ Lệ Phục Vụ Trung Bình theo Loại (%)')
        axes[1, 0].set_ylabel('Tỷ Lệ Phục Vụ (%)')
        axes[1, 0].set_ylim(0, 105)
        
        # 4. Box plot độ dài route theo loại
        route_lengths_by_cat = []
        labels_by_cat = []
        
        for cat in cat_names:
            all_lengths = []
            for stats in categories[cat]:
                all_lengths.extend(stats.route_lengths)
            if all_lengths:
                route_lengths_by_cat.append(all_lengths)
                labels_by_cat.append(cat)
        
        if route_lengths_by_cat:
            axes[1, 1].boxplot(route_lengths_by_cat, labels=labels_by_cat)
            axes[1, 1].set_title('Phân Phối Độ Dài Route theo Loại')
            axes[1, 1].set_ylabel('Độ Dài Route')
        
        plt.tight_layout()
        plt.savefig(os.path.join(self.output_folder, 'instance_categories.png'), 
                   dpi=300, bbox_inches='tight')
        plt.close()
        
        print("✓ Đã tạo biểu đồ phân loại instance: instance_categories.png")
    
    def create_summary_dashboard(self):
        """Tạo dashboard tổng hợp"""
        if not self.solutions:
            return
        
        # Tính toán các thống kê tổng hợp
        total_instances = len(self.solutions)
        total_routes = sum(stats.num_routes for stats in self.solutions.values())
        total_served = sum(stats.served_customers for stats in self.solutions.values())
        total_unserved = sum(stats.unserved_customers for stats in self.solutions.values())
        total_customers = total_served + total_unserved
        
        all_route_lengths = []
        for stats in self.solutions.values():
            all_route_lengths.extend(stats.route_lengths)
        
        avg_route_length = statistics.mean(all_route_lengths) if all_route_lengths else 0
        service_rate = (total_served / max(total_customers, 1)) * 100
        
        # Tạo dashboard
        fig = plt.figure(figsize=(16, 10))
        gs = fig.add_gridspec(3, 4, hspace=0.3, wspace=0.3)
        
        # Title
        fig.suptitle('DASHBOARD TỔNG HỢP - THUẬT TOÁN GREEDY VRPSPDTW\nDataset: Wang_Chen', 
                     fontsize=18, fontweight='bold', y=0.95)
        
        # Thống kê chính (text boxes)
        ax_stats = fig.add_subplot(gs[0, :])
        ax_stats.axis('off')
        
        stats_text = f"""
        THỐNG KÊ TỔNG QUAN:
        • Tổng số instances: {total_instances}
        • Tổng số routes: {total_routes}
        • Tổng khách hàng: {total_customers}
        • Khách hàng được phục vụ: {total_served} ({service_rate:.1f}%)
        • Khách hàng chưa phục vụ: {total_unserved}
        • Độ dài route trung bình: {avg_route_length:.2f}
        • Routes trung bình/instance: {total_routes/total_instances:.1f}
        """
        
        ax_stats.text(0.5, 0.5, stats_text, ha='center', va='center', 
                     fontsize=12, bbox=dict(boxstyle="round,pad=0.5", facecolor="lightblue"))
        
        # Biểu đồ tròn tỷ lệ phục vụ
        ax_pie = fig.add_subplot(gs[1, 0])
        if total_unserved > 0:
            ax_pie.pie([total_served, total_unserved], 
                      labels=['Đã phục vụ', 'Chưa phục vụ'],
                      colors=['lightgreen', 'lightcoral'],
                      autopct='%1.1f%%', startangle=90)
        else:
            ax_pie.pie([100], labels=['Đã phục vụ 100%'], 
                      colors=['lightgreen'], autopct='%1.1f%%', startangle=90)
        ax_pie.set_title('Tỷ Lệ Phục Vụ')
        
        # Histogram độ dài route
        ax_hist = fig.add_subplot(gs[1, 1])
        ax_hist.hist(all_route_lengths, bins=min(20, max(all_route_lengths)), 
                    color='skyblue', alpha=0.7, edgecolor='black')
        ax_hist.set_title('Phân Phối Độ Dài Route')
        ax_hist.set_xlabel('Độ dài')
        ax_hist.set_ylabel('Số lượng')
        
        # Top 10 instances theo số routes
        ax_top_routes = fig.add_subplot(gs[1, 2])
        top_routes_data = sorted(self.solutions.items(), 
                               key=lambda x: x[1].num_routes, reverse=True)[:10]
        instances = [x[0] for x in top_routes_data]
        routes = [x[1].num_routes for x in top_routes_data]
        
        ax_top_routes.barh(range(len(instances)), routes, color='lightcoral')
        ax_top_routes.set_title('Top 10 Instances\n(Số Routes)')
        ax_top_routes.set_yticks(range(len(instances)))
        ax_top_routes.set_yticklabels(instances, fontsize=8)
        ax_top_routes.invert_yaxis()
        
        # Tỷ lệ phục vụ theo instance (sample)
        ax_service = fig.add_subplot(gs[1, 3])
        sample_instances = list(self.solutions.keys())[:15]  # Lấy 15 instance đầu
        sample_rates = []
        for inst in sample_instances:
            stats = self.solutions[inst]
            rate = (stats.served_customers / max(stats.total_customers, 1)) * 100
            sample_rates.append(rate)
        
        colors = ['green' if rate == 100 else 'orange' if rate >= 80 else 'red' 
                 for rate in sample_rates]
        ax_service.bar(range(len(sample_instances)), sample_rates, color=colors, alpha=0.7)
        ax_service.set_title('Tỷ Lệ Phục Vụ\n(15 instances đầu)')
        ax_service.set_ylabel('Tỷ lệ (%)')
        ax_service.set_ylim(0, 105)
        ax_service.tick_params(axis='x', rotation=45, labelsize=6)
        
        # Thống kê chi tiết (bảng)
        ax_table = fig.add_subplot(gs[2, :])
        ax_table.axis('off')
        
        # Tạo bảng thống kê top instances
        table_data = []
        for inst_name in sorted(list(self.solutions.keys())[:10]):  # Top 10
            stats = self.solutions[inst_name]
            service_rate = (stats.served_customers / max(stats.total_customers, 1)) * 100
            table_data.append([
                inst_name,
                stats.num_routes,
                f"{stats.served_customers}/{stats.total_customers}",
                f"{service_rate:.1f}%",
                f"{stats.avg_route_length:.1f}"
            ])
        
        table = ax_table.table(cellText=table_data,
                              colLabels=['Instance', 'Routes', 'Served/Total', 'Service Rate', 'Avg Length'],
                              cellLoc='center',
                              loc='center',
                              bbox=[0, 0, 1, 1])
        table.auto_set_font_size(False)
        table.set_fontsize(9)
        table.scale(1, 1.5)
        
        # Styling cho table
        for i in range(len(table_data) + 1):
            for j in range(5):
                cell = table[(i, j)]
                if i == 0:  # Header
                    cell.set_facecolor('#4CAF50')
                    cell.set_text_props(weight='bold', color='white')
                else:
                    cell.set_facecolor('#f0f0f0' if i % 2 == 0 else 'white')
        
        plt.savefig(os.path.join(self.output_folder, 'summary_dashboard.png'), 
                   dpi=300, bbox_inches='tight')
        plt.close()
        
        print("✓ Đã tạo dashboard tổng hợp: summary_dashboard.png")
    
    def generate_all_visualizations(self):
        """Tạo tất cả biểu đồ trực quan"""
        print("BẮT ĐẦU TẠO CÁC BIỂU ĐỒ TRỰC QUAN...")
        print("="*50)
        
        self.load_solutions()
        
        if not self.solutions:
            print("Không có solution nào để trực quan hóa!")
            return
        
        print(f"Đang tạo biểu đồ cho {len(self.solutions)} solutions...")
        
        # Tạo các biểu đồ
        self.create_summary_dashboard()
        self.plot_overview_statistics()
        self.plot_route_length_analysis()
        self.plot_performance_comparison()
        self.plot_instance_categories()
        
        print("="*50)
        print("HOÀN THÀNH! Các file biểu đồ đã được tạo:")
        print(f"📊 {self.output_folder}/summary_dashboard.png")
        print(f"📊 {self.output_folder}/overview_statistics.png")
        print(f"📊 {self.output_folder}/route_length_analysis.png")
        print(f"📊 {self.output_folder}/performance_comparison.png")
        print(f"📊 {self.output_folder}/instance_categories.png")

def visualize_wang_chen_solutions():
    """Trực quan hóa solutions của dataset Wang_Chen"""
    solution_folder = r"d:\Logistic\vrpspdtw\VRPenstein\data\Wang_Chen\solution"
    output_folder = r"d:\Logistic\vrpspdtw\VRPenstein\output\visualizations"
    
    visualizer = SolutionVisualizer(solution_folder, output_folder)
    visualizer.generate_all_visualizations()

if __name__ == "__main__":
    visualize_wang_chen_solutions()