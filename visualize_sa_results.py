"""
Trực quan hóa kết quả thuật toán Simulated Annealing (SA)
Tạo các biểu đồ và báo cáo chi tiết về các giải pháp được tìm thấy
"""

import re
import matplotlib.pyplot as plt
import seaborn as sns
import pandas as pd
import numpy as np
from datetime import datetime
import os
from collections import Counter
import matplotlib.patches as mpatches

# Thiết lập font tiếng Việt
plt.rcParams['font.family'] = ['DejaVu Sans', 'Arial Unicode MS', 'Tahoma']
plt.rcParams['axes.unicode_minus'] = False

class SAResultsVisualizer:
    def __init__(self, results_file):
        """
        Khởi tạo class trực quan hóa kết quả SA
        
        Args:
            results_file (str): Đường dẫn đến file kết quả SA
        """
        self.results_file = results_file
        self.data = {}
        self.solutions = []
        self.parse_results()
    
    def parse_results(self):
        """Phân tích file kết quả SA"""
        try:
            with open(self.results_file, 'r', encoding='utf-8') as f:
                content = f.read()
            
            # Trích xuất thông tin chung
            self.data['test_name'] = re.search(r'Test Name: (.+)', content).group(1)
            self.data['problem_type'] = re.search(r'Problem Type: (.+)', content).group(1)
            self.data['timestamp'] = re.search(r'Timestamp: (.+)', content).group(1)
            self.data['run_time'] = float(re.search(r'Run Time: ([\d.]+) seconds', content).group(1))
            self.data['num_locations'] = int(re.search(r'Number of Locations: (\d+)', content).group(1))
            self.data['initial_fitness'] = float(re.search(r'Initial Fitness: ([\d.]+)', content).group(1))
            self.data['population_size'] = int(re.search(r'Population Size: (\d+)', content).group(1))
            
            # Trích xuất thống kê quần thể
            self.data['best_fitness'] = float(re.search(r'Best Fitness: ([\d.]+)', content).group(1))
            self.data['worst_fitness'] = float(re.search(r'Worst Fitness: ([\d.]+)', content).group(1))
            self.data['avg_fitness'] = float(re.search(r'Average Fitness: ([\d.]+)', content).group(1))
            self.data['improvement'] = float(re.search(r'Improvement: ([\d.]+)', content).group(1))
            self.data['improvement_percent'] = float(re.search(r'Improvement %: ([\d.]+)%', content).group(1))
            
            # Trích xuất các giải pháp chi tiết
            solution_pattern = r'Solution (\d+) \(Fitness: ([\d.]+)\):\s*((?:\s*Route \d+: \[[\d, ]+\]\s*)+)'
            solutions = re.findall(solution_pattern, content)
            
            for sol_num, fitness, routes_text in solutions:
                route_pattern = r'Route (\d+): \[([\d, ]+)\]'
                routes = re.findall(route_pattern, routes_text)
                
                solution = {
                    'solution_id': int(sol_num),
                    'fitness': float(fitness),
                    'routes': []
                }
                
                for route_num, nodes_str in routes:
                    nodes = [int(x.strip()) for x in nodes_str.split(',') if x.strip()]
                    solution['routes'].append({
                        'route_id': int(route_num),
                        'nodes': nodes,
                        'num_customers': len(nodes)
                    })
                
                solution['num_routes'] = len(solution['routes'])
                solution['total_customers'] = sum(route['num_customers'] for route in solution['routes'])
                
                self.solutions.append(solution)
                
        except Exception as e:
            print(f"Lỗi khi phân tích file: {e}")
            raise
    
    def create_summary_report(self):
        """Tạo báo cáo tổng quan"""
        print("=" * 60)
        print("BÁO CÁO TỔNG QUAN KẾT QUẢ SIMULATED ANNEALING")
        print("=" * 60)
        print(f"Tên bài kiểm tra: {self.data['test_name']}")
        print(f"Loại bài toán: {self.data['problem_type']}")
        print(f"Thời gian chạy: {self.data['timestamp']}")
        print(f"Thời gian thực thi: {self.data['run_time']:.2f} giây")
        print(f"Số địa điểm: {self.data['num_locations']}")
        print(f"Kích thước quần thể: {self.data['population_size']}")
        print()
        print("THỐNG KÊ HIỆU SUẤT:")
        print(f"Fitness ban đầu: {self.data['initial_fitness']:,.2f}")
        print(f"Fitness tốt nhất: {self.data['best_fitness']:,.2f}")
        print(f"Fitness tệ nhất: {self.data['worst_fitness']:,.2f}")
        print(f"Fitness trung bình: {self.data['avg_fitness']:,.2f}")
        print(f"Cải thiện: {self.data['improvement']:,.2f}")
        print(f"Tỷ lệ cải thiện: {self.data['improvement_percent']:.2f}%")
        print()
        print("THỐNG KÊ GIẢI PHÁP:")
        print(f"Tổng số giải pháp: {len(self.solutions)}")
        
        if self.solutions:
            route_counts = [sol['num_routes'] for sol in self.solutions]
            customer_counts = [sol['total_customers'] for sol in self.solutions]
            
            print(f"Số tuyến đường trung bình: {np.mean(route_counts):.1f}")
            print(f"Số khách hàng trung bình: {np.mean(customer_counts):.1f}")
            print(f"Tuyến đường ít nhất: {min(route_counts)}")
            print(f"Tuyến đường nhiều nhất: {max(route_counts)}")
        print("=" * 60)
    
    def plot_fitness_distribution(self):
        """Vẽ biểu đồ phân phối fitness"""
        if not self.solutions:
            return
            
        fitness_values = [sol['fitness'] for sol in self.solutions]
        
        plt.figure(figsize=(12, 6))
        
        # Biểu đồ histogram
        plt.subplot(1, 2, 1)
        plt.hist(fitness_values, bins=20, alpha=0.7, color='skyblue', edgecolor='black')
        plt.title('Phân Phối Giá Trị Fitness', fontsize=14, fontweight='bold')
        plt.xlabel('Giá Trị Fitness')
        plt.ylabel('Tần Suất')
        plt.grid(True, alpha=0.3)
        
        # Biểu đồ boxplot
        plt.subplot(1, 2, 2)
        plt.boxplot(fitness_values, vert=True)
        plt.title('Boxplot Giá Trị Fitness', fontsize=14, fontweight='bold')
        plt.ylabel('Giá Trị Fitness')
        plt.grid(True, alpha=0.3)
        
        plt.tight_layout()
        plt.savefig(f'SA_fitness_distribution_{self.data["test_name"]}.png', dpi=300, bbox_inches='tight')
        plt.show()
    
    def plot_routes_analysis(self):
        """Phân tích số lượng tuyến đường"""
        if not self.solutions:
            return
            
        route_counts = [sol['num_routes'] for sol in self.solutions]
        route_counter = Counter(route_counts)
        
        plt.figure(figsize=(15, 10))
        
        # Biểu đồ số lượng tuyến đường
        plt.subplot(2, 2, 1)
        routes, counts = zip(*sorted(route_counter.items()))
        plt.bar(routes, counts, color='lightcoral', alpha=0.7, edgecolor='black')
        plt.title('Phân Phối Số Lượng Tuyến Đường', fontsize=14, fontweight='bold')
        plt.xlabel('Số Tuyến Đường')
        plt.ylabel('Số Giải Pháp')
        plt.grid(True, alpha=0.3)
        
        # Biểu đồ khách hàng trên mỗi tuyến
        plt.subplot(2, 2, 2)
        customers_per_route = []
        for sol in self.solutions:
            for route in sol['routes']:
                customers_per_route.append(route['num_customers'])
        
        plt.hist(customers_per_route, bins=20, color='lightgreen', alpha=0.7, edgecolor='black')
        plt.title('Phân Phối Số Khách Hàng Trên Mỗi Tuyến', fontsize=14, fontweight='bold')
        plt.xlabel('Số Khách Hàng')
        plt.ylabel('Tần Suất')
        plt.grid(True, alpha=0.3)
        
        # Biểu đồ tổng số khách hàng
        plt.subplot(2, 2, 3)
        total_customers = [sol['total_customers'] for sol in self.solutions]
        plt.hist(total_customers, bins=15, color='gold', alpha=0.7, edgecolor='black')
        plt.title('Phân Phối Tổng Số Khách Hàng', fontsize=14, fontweight='bold')
        plt.xlabel('Tổng Số Khách Hàng')
        plt.ylabel('Tần Suất')
        plt.grid(True, alpha=0.3)
        
        # Biểu đồ scatter: Số tuyến vs Fitness
        plt.subplot(2, 2, 4)
        fitness_values = [sol['fitness'] for sol in self.solutions]
        plt.scatter(route_counts, fitness_values, alpha=0.6, color='purple')
        plt.title('Mối Quan Hệ: Số Tuyến Đường vs Fitness', fontsize=14, fontweight='bold')
        plt.xlabel('Số Tuyến Đường')
        plt.ylabel('Giá Trị Fitness')
        plt.grid(True, alpha=0.3)
        
        plt.tight_layout()
        plt.savefig(f'SA_routes_analysis_{self.data["test_name"]}.png', dpi=300, bbox_inches='tight')
        plt.show()
    
    def plot_solution_evolution_analysis(self):
        """Phân tích sự tiến hóa và biến đổi của các giải pháp"""
        if not self.solutions:
            return
            
        # Sắp xếp theo fitness để thấy sự cải thiện
        sorted_solutions = sorted(self.solutions, key=lambda x: x['fitness'])
        
        plt.figure(figsize=(16, 12))
        
        # 1. Biểu đồ tiến hóa fitness
        plt.subplot(3, 3, 1)
        fitness_values = [sol['fitness'] for sol in self.solutions]
        solution_order = range(len(self.solutions))
        plt.plot(solution_order, fitness_values, 'o-', color='blue', alpha=0.7, markersize=4)
        plt.axhline(y=self.data['initial_fitness'], color='red', linestyle='--', 
                   label=f'Fitness Ban Đầu: {self.data["initial_fitness"]:,.0f}')
        plt.axhline(y=min(fitness_values), color='green', linestyle='--', 
                   label=f'Fitness Tốt Nhất: {min(fitness_values):,.0f}')
        plt.title('Tiến Hóa Fitness Qua Các Giải Pháp', fontweight='bold')
        plt.xlabel('Thứ Tự Giải Pháp')
        plt.ylabel('Giá Trị Fitness')
        plt.legend()
        plt.grid(True, alpha=0.3)
        
        # 2. Phân tích độ cải thiện
        plt.subplot(3, 3, 2)
        improvements = [(self.data['initial_fitness'] - sol['fitness'])/self.data['initial_fitness']*100 
                       for sol in self.solutions]
        plt.hist(improvements, bins=20, color='lightgreen', alpha=0.7, edgecolor='black')
        plt.title('Phân Phối Tỷ Lệ Cải Thiện (%)', fontweight='bold')
        plt.xlabel('Tỷ Lệ Cải Thiện (%)')
        plt.ylabel('Số Giải Pháp')
        plt.axvline(x=np.mean(improvements), color='red', linestyle='--', 
                   label=f'Trung Bình: {np.mean(improvements):.1f}%')
        plt.legend()
        plt.grid(True, alpha=0.3)
        
        # 3. Mối quan hệ Fitness vs Số Tuyến
        plt.subplot(3, 3, 3)
        route_counts = [sol['num_routes'] for sol in self.solutions]
        colors = plt.cm.viridis(np.linspace(0, 1, len(self.solutions)))
        scatter = plt.scatter(route_counts, fitness_values, c=colors, alpha=0.6, s=50)
        plt.title('Mối Quan Hệ: Số Tuyến vs Fitness', fontweight='bold')
        plt.xlabel('Số Tuyến Đường')
        plt.ylabel('Giá Trị Fitness')
        plt.colorbar(scatter, label='Thứ Tự Giải Pháp')
        plt.grid(True, alpha=0.3)
        
        # 4. Phân tích hiệu quả tuyến đường
        plt.subplot(3, 3, 4)
        efficiency_values = [sol['total_customers']/sol['num_routes'] if sol['num_routes'] > 0 else 0 
                           for sol in self.solutions]
        plt.scatter(efficiency_values, fitness_values, alpha=0.6, color='orange', s=50)
        plt.title('Hiệu Quả Tuyến vs Fitness', fontweight='bold')
        plt.xlabel('Hiệu Quả (Khách Hàng/Tuyến)')
        plt.ylabel('Giá Trị Fitness')
        plt.grid(True, alpha=0.3)
        
        # 5. Top 5 giải pháp tốt nhất
        plt.subplot(3, 3, 5)
        top_5 = sorted_solutions[:5]
        top_fitness = [sol['fitness'] for sol in top_5]
        top_ids = [f"Sol {sol['solution_id']}" for sol in top_5]
        bars = plt.bar(range(len(top_5)), top_fitness, color='gold', alpha=0.8, edgecolor='black')
        plt.title('Top 5 Giải Pháp Tốt Nhất', fontweight='bold')
        plt.xlabel('Giải Pháp')
        plt.ylabel('Fitness')
        plt.xticks(range(len(top_5)), top_ids, rotation=45)
        
        # Thêm giá trị lên cột
        for i, bar in enumerate(bars):
            height = bar.get_height()
            plt.text(bar.get_x() + bar.get_width()/2., height,
                    f'{height:,.0f}', ha='center', va='bottom', fontsize=9)
        plt.grid(True, alpha=0.3)
        
        # 6. Phân tích độ biến thiên
        plt.subplot(3, 3, 6)
        fitness_std = np.std(fitness_values)
        fitness_mean = np.mean(fitness_values)
        cv = fitness_std / fitness_mean * 100  # Coefficient of Variation
        
        plt.text(0.1, 0.8, f'Độ Lệch Chuẩn: {fitness_std:,.2f}', transform=plt.gca().transAxes, fontsize=12)
        plt.text(0.1, 0.7, f'Giá Trị Trung Bình: {fitness_mean:,.2f}', transform=plt.gca().transAxes, fontsize=12)
        plt.text(0.1, 0.6, f'Hệ Số Biến Thiên: {cv:.2f}%', transform=plt.gca().transAxes, fontsize=12)
        plt.text(0.1, 0.5, f'Khoảng Cách: {max(fitness_values) - min(fitness_values):,.2f}', 
                transform=plt.gca().transAxes, fontsize=12)
        plt.text(0.1, 0.4, f'Tỷ Lệ Cải Thiện Tốt Nhất: {self.data["improvement_percent"]:.2f}%', 
                transform=plt.gca().transAxes, fontsize=12, color='green', weight='bold')
        
        plt.title('Thống Kê Độ Biến Thiên', fontweight='bold')
        plt.axis('off')
        
        # 7. Phân phối số tuyến đường
        plt.subplot(3, 3, 7)
        route_counter = Counter(route_counts)
        routes, counts = zip(*sorted(route_counter.items()))
        plt.pie(counts, labels=[f'{r} tuyến' for r in routes], autopct='%1.1f%%', startangle=90)
        plt.title('Phân Phối Số Tuyến Đường', fontweight='bold')
        
        # 8. Biểu đồ violin cho fitness
        plt.subplot(3, 3, 8)
        plt.violinplot([fitness_values], positions=[1], showmeans=True, showmedians=True)
        plt.title('Phân Phối Fitness (Violin Plot)', fontweight='bold')
        plt.ylabel('Giá Trị Fitness')
        plt.xticks([1], ['Tất Cả Giải Pháp'])
        plt.grid(True, alpha=0.3)
        
        # 9. Heatmap correlation
        plt.subplot(3, 3, 9)
        correlation_data = pd.DataFrame({
            'Fitness': fitness_values,
            'Số Tuyến': route_counts,
            'Tổng KH': [sol['total_customers'] for sol in self.solutions],
            'Hiệu Quả': efficiency_values
        })
        
        corr_matrix = correlation_data.corr()
        sns.heatmap(corr_matrix, annot=True, cmap='coolwarm', center=0, 
                   square=True, fmt='.2f', cbar_kws={'shrink': 0.8})
        plt.title('Ma Trận Tương Quan', fontweight='bold')
        
        plt.tight_layout()
        plt.savefig(f'SA_evolution_analysis_{self.data["test_name"]}.png', dpi=300, bbox_inches='tight')
        plt.show()
    
    def analyze_solution_diversity(self):
        """Phân tích sự đa dạng và khác biệt giữa các giải pháp"""
        if not self.solutions:
            return
            
        plt.figure(figsize=(16, 10))
        
        # 1. Phân tích độ tương đồng giữa các giải pháp
        plt.subplot(2, 3, 1)
        fitness_values = [sol['fitness'] for sol in self.solutions]
        route_counts = [sol['num_routes'] for sol in self.solutions]
        
        # Tính độ tương đồng dựa trên cấu trúc tuyến
        similarity_matrix = np.zeros((len(self.solutions), len(self.solutions)))
        for i, sol1 in enumerate(self.solutions):
            for j, sol2 in enumerate(self.solutions):
                if i != j:
                    # Tính độ tương đồng dựa trên số tuyến và phân phối khách hàng
                    route_diff = abs(sol1['num_routes'] - sol2['num_routes'])
                    customer_diff = abs(sol1['total_customers'] - sol2['total_customers'])
                    similarity = 1 / (1 + route_diff + customer_diff/10)
                    similarity_matrix[i][j] = similarity
        
        sns.heatmap(similarity_matrix, cmap='Blues', square=True, cbar_kws={'shrink': 0.8})
        plt.title('Ma Trận Độ Tương Đồng Giải Pháp', fontweight='bold')
        plt.xlabel('Chỉ Số Giải Pháp')
        plt.ylabel('Chỉ Số Giải Pháp')
        
        # 2. Phân cụm giải pháp theo hiệu suất
        plt.subplot(2, 3, 2)
        efficiency_values = [sol['total_customers']/sol['num_routes'] if sol['num_routes'] > 0 else 0 
                           for sol in self.solutions]
        
        # Phân loại giải pháp
        high_performance = [f for f in fitness_values if f <= np.percentile(fitness_values, 25)]
        medium_performance = [f for f in fitness_values if np.percentile(fitness_values, 25) < f <= np.percentile(fitness_values, 75)]
        low_performance = [f for f in fitness_values if f > np.percentile(fitness_values, 75)]
        
        categories = ['Xuất Sắc\n(Top 25%)', 'Trung Bình\n(25-75%)', 'Kém\n(Bottom 25%)']
        counts = [len(high_performance), len(medium_performance), len(low_performance)]
        colors = ['green', 'orange', 'red']
        
        bars = plt.bar(categories, counts, color=colors, alpha=0.7, edgecolor='black')
        plt.title('Phân Loại Hiệu Suất Giải Pháp', fontweight='bold')
        plt.ylabel('Số Lượng Giải Pháp')
        
        # Thêm giá trị lên cột
        for bar, count in zip(bars, counts):
            plt.text(bar.get_x() + bar.get_width()/2., bar.get_height(),
                    f'{count}', ha='center', va='bottom', fontsize=12, fontweight='bold')
        
        # 3. Phân tích biến thiên cấu trúc tuyến
        plt.subplot(2, 3, 3)
        route_structures = {}
        for sol in self.solutions:
            structure_key = f"{sol['num_routes']}_routes"
            if structure_key not in route_structures:
                route_structures[structure_key] = []
            route_structures[structure_key].append(sol['fitness'])
        
        structure_names = list(route_structures.keys())
        structure_fitness = [route_structures[key] for key in structure_names]
        
        plt.boxplot(structure_fitness, labels=structure_names)
        plt.title('Phân Phối Fitness Theo Cấu Trúc Tuyến', fontweight='bold')
        plt.xlabel('Cấu Trúc Tuyến')
        plt.ylabel('Fitness')
        plt.xticks(rotation=45)
        plt.grid(True, alpha=0.3)
        
        # 4. Phân tích độ ổn định
        plt.subplot(2, 3, 4)
        fitness_std = np.std(fitness_values)
        fitness_mean = np.mean(fitness_values)
        cv = fitness_std / fitness_mean * 100
        
        stability_metrics = {
            'Hệ Số Biến Thiên (CV)': cv,
            'Độ Lệch Chuẩn': fitness_std,
            'Khoảng Biến Thiên': max(fitness_values) - min(fitness_values),
            'Tỷ Lệ Cải Thiện': self.data['improvement_percent']
        }
        
        metrics = list(stability_metrics.keys())
        values = list(stability_metrics.values())
        
        # Chuẩn hóa giá trị để hiển thị
        normalized_values = [(v - min(values)) / (max(values) - min(values)) * 100 for v in values]
        
        bars = plt.barh(metrics, normalized_values, color='skyblue', alpha=0.7, edgecolor='black')
        plt.title('Chỉ Số Ổn Định Thuật Toán', fontweight='bold')
        plt.xlabel('Giá Trị Chuẩn Hóa (%)')
        
        # 5. Phân tích xu hướng cải thiện
        plt.subplot(2, 3, 5)
        improvements = [(self.data['initial_fitness'] - sol['fitness'])/self.data['initial_fitness']*100 
                       for sol in self.solutions]
        
        # Chia thành các nhóm cải thiện
        excellent_improvement = sum(1 for imp in improvements if imp >= 15)
        good_improvement = sum(1 for imp in improvements if 5 <= imp < 15)
        moderate_improvement = sum(1 for imp in improvements if 0 <= imp < 5)
        no_improvement = sum(1 for imp in improvements if imp < 0)
        
        improvement_categories = ['Xuất Sắc\n(≥15%)', 'Tốt\n(5-15%)', 'Vừa Phải\n(0-5%)', 'Không\n(<0%)']
        improvement_counts = [excellent_improvement, good_improvement, moderate_improvement, no_improvement]
        improvement_colors = ['darkgreen', 'green', 'yellow', 'red']
        
        plt.pie(improvement_counts, labels=improvement_categories, colors=improvement_colors, 
                autopct='%1.1f%%', startangle=90)
        plt.title('Phân Phối Mức Độ Cải Thiện', fontweight='bold')
        
        # 6. Tóm tắt thống kê quan trọng
        plt.subplot(2, 3, 6)
        best_solution = min(self.solutions, key=lambda x: x['fitness'])
        worst_solution = max(self.solutions, key=lambda x: x['fitness'])
        
        summary_text = f"""
TÓNG TẮT PHÂN TÍCH GIẢI PHÁP SA

Tổng số giải pháp: {len(self.solutions)}
Thời gian thực thi: {self.data['run_time']:.2f}s

HIỆU SUẤT:
• Fitness tốt nhất: {best_solution['fitness']:,.0f}
• Fitness tệ nhất: {worst_solution['fitness']:,.0f}
• Cải thiện trung bình: {np.mean(improvements):.1f}%
• Độ ổn định (CV): {cv:.1f}%

CẤU TRÚC TUYẾN:
• Số tuyến trung bình: {np.mean(route_counts):.1f}
• Hiệu quả trung bình: {np.mean(efficiency_values):.1f} KH/tuyến
• Đa dạng cấu trúc: {len(set(route_counts))} loại

ĐÁNH GIÁ CHUNG:
• Thuật toán {'ổn định' if cv < 10 else 'không ổn định'}
• Cải thiện {'đáng kể' if self.data['improvement_percent'] > 10 else 'vừa phải'}
• Đa dạng giải pháp {'cao' if len(set(route_counts)) > 3 else 'thấp'}
        """
        
        plt.text(0.05, 0.95, summary_text, transform=plt.gca().transAxes, 
                fontsize=10, verticalalignment='top', fontfamily='monospace',
                bbox=dict(boxstyle='round', facecolor='lightblue', alpha=0.8))
        plt.axis('off')
        
        plt.tight_layout()
        plt.savefig(f'SA_diversity_analysis_{self.data["test_name"]}.png', dpi=300, bbox_inches='tight')
        plt.show()
    
    def create_comprehensive_summary_table(self):
        """Tạo bảng tóm tắt toàn diện về các giải pháp"""
        if not self.solutions:
            return
            
        # Sắp xếp theo fitness
        sorted_solutions = sorted(self.solutions, key=lambda x: x['fitness'])
        
        # Tạo DataFrame tổng hợp
        summary_data = []
        
        # Thống kê theo nhóm hiệu suất
        fitness_values = [sol['fitness'] for sol in self.solutions]
        q25 = np.percentile(fitness_values, 25)
        q75 = np.percentile(fitness_values, 75)
        
        groups = {
            'Xuất Sắc (Top 25%)': [sol for sol in self.solutions if sol['fitness'] <= q25],
            'Trung Bình (25-75%)': [sol for sol in self.solutions if q25 < sol['fitness'] <= q75],
            'Kém (Bottom 25%)': [sol for sol in self.solutions if sol['fitness'] > q75]
        }
        
        for group_name, group_solutions in groups.items():
            if group_solutions:
                group_fitness = [sol['fitness'] for sol in group_solutions]
                group_routes = [sol['num_routes'] for sol in group_solutions]
                group_customers = [sol['total_customers'] for sol in group_solutions]
                group_efficiency = [sol['total_customers']/sol['num_routes'] if sol['num_routes'] > 0 else 0 
                                  for sol in group_solutions]
                
                summary_data.append({
                    'Nhóm Hiệu Suất': group_name,
                    'Số Giải Pháp': len(group_solutions),
                    'Fitness TB': f"{np.mean(group_fitness):,.0f}",
                    'Fitness Min': f"{min(group_fitness):,.0f}",
                    'Fitness Max': f"{max(group_fitness):,.0f}",
                    'Số Tuyến TB': f"{np.mean(group_routes):.1f}",
                    'Tổng KH TB': f"{np.mean(group_customers):.0f}",
                    'Hiệu Quả TB': f"{np.mean(group_efficiency):.2f}",
                    'Độ Lệch Chuẩn': f"{np.std(group_fitness):,.0f}"
                })
        
        df_summary = pd.DataFrame(summary_data)
        
        # Hiển thị bảng tóm tắt
        print(f"\nBẢNG TÓM TẮT PHÂN TÍCH GIẢI PHÁP SA - {self.data['test_name']}")
        print("=" * 140)
        print(df_summary.to_string(index=False))
        print("=" * 140)
        
        # Tạo bảng chi tiết top giải pháp
        top_solutions = sorted_solutions[:10]
        detail_data = []
        
        for i, sol in enumerate(top_solutions, 1):
            efficiency = sol['total_customers'] / sol['num_routes'] if sol['num_routes'] > 0 else 0
            improvement = (self.data['initial_fitness'] - sol['fitness'])/self.data['initial_fitness']*100
            
            # Phân tích cấu trúc tuyến
            route_sizes = [route['num_customers'] for route in sol['routes']]
            route_structure = f"Min:{min(route_sizes)}, Max:{max(route_sizes)}, TB:{np.mean(route_sizes):.1f}"
            
            detail_data.append({
                'Hạng': i,
                'ID Giải Pháp': sol['solution_id'],
                'Fitness': f"{sol['fitness']:,.0f}",
                'Cải Thiện (%)': f"{improvement:.2f}%",
                'Số Tuyến': sol['num_routes'],
                'Tổng KH': sol['total_customers'],
                'Hiệu Quả': f"{efficiency:.2f}",
                'Cấu Trúc Tuyến': route_structure,
                'Đánh Giá': 'Xuất sắc' if improvement >= 15 else 'Tốt' if improvement >= 5 else 'Trung bình'
            })
        
        df_detail = pd.DataFrame(detail_data)
        
        print(f"\nBẢNG CHI TIẾT TOP 10 GIẢI PHÁP TỐT NHẤT:")
        print("=" * 160)
        print(df_detail.to_string(index=False))
        print("=" * 160)
        
        # Lưu vào file Excel với nhiều sheet
        excel_filename = f'SA_comprehensive_analysis_{self.data["test_name"]}.xlsx'
        with pd.ExcelWriter(excel_filename, engine='openpyxl') as writer:
            df_summary.to_excel(writer, sheet_name='Tóm Tắt Nhóm', index=False)
            df_detail.to_excel(writer, sheet_name='Top 10 Giải Pháp', index=False)
            
            # Thêm sheet thống kê tổng quan
            general_stats = pd.DataFrame([{
                'Chỉ Số': 'Tổng Số Giải Pháp',
                'Giá Trị': len(self.solutions)
            }, {
                'Chỉ Số': 'Thời Gian Thực Thi (s)',
                'Giá Trị': self.data['run_time']
            }, {
                'Chỉ Số': 'Fitness Ban Đầu',
                'Giá Trị': f"{self.data['initial_fitness']:,.0f}"
            }, {
                'Chỉ Số': 'Fitness Tốt Nhất',
                'Giá Trị': f"{min(fitness_values):,.0f}"
            }, {
                'Chỉ Số': 'Cải Thiện Tốt Nhất (%)',
                'Giá Trị': f"{self.data['improvement_percent']:.2f}%"
            }, {
                'Chỉ Số': 'Độ Ổn Định (CV %)',
                'Giá Trị': f"{np.std(fitness_values)/np.mean(fitness_values)*100:.2f}%"
            }])
            
            general_stats.to_excel(writer, sheet_name='Thống Kê Tổng Quan', index=False)
        
        print(f"Đã lưu phân tích toàn diện vào file: {excel_filename}")
        
        return df_summary, df_detail
    
    def plot_route_length_distribution(self):
        """Phân tích phân phối độ dài tuyến đường"""
        if not self.solutions:
            return
            
        route_lengths = []
        for sol in self.solutions:
            for route in sol['routes']:
                route_lengths.append(route['num_customers'])
        
        plt.figure(figsize=(12, 8))
        
        # Histogram
        plt.subplot(2, 2, 1)
        plt.hist(route_lengths, bins=20, color='lightblue', alpha=0.7, edgecolor='black')
        plt.title('Phân Phối Độ Dài Tuyến Đường', fontsize=14, fontweight='bold')
        plt.xlabel('Số Khách Hàng Trên Tuyến')
        plt.ylabel('Tần Suất')
        plt.grid(True, alpha=0.3)
        
        # Box plot
        plt.subplot(2, 2, 2)
        plt.boxplot(route_lengths, vert=True)
        plt.title('Boxplot Độ Dài Tuyến Đường', fontsize=14, fontweight='bold')
        plt.ylabel('Số Khách Hàng Trên Tuyến')
        plt.grid(True, alpha=0.3)
        
        # Thống kê theo giải pháp
        plt.subplot(2, 2, 3)
        avg_route_lengths = []
        for sol in self.solutions:
            if sol['routes']:
                avg_length = np.mean([route['num_customers'] for route in sol['routes']])
                avg_route_lengths.append(avg_length)
        
        plt.hist(avg_route_lengths, bins=15, color='lightcoral', alpha=0.7, edgecolor='black')
        plt.title('Phân Phối Độ Dài Tuyến Trung Bình Theo Giải Pháp', fontsize=14, fontweight='bold')
        plt.xlabel('Độ Dài Tuyến Trung Bình')
        plt.ylabel('Số Giải Pháp')
        plt.grid(True, alpha=0.3)
        
        # Pie chart cho phân loại tuyến
        plt.subplot(2, 2, 4)
        short_routes = sum(1 for length in route_lengths if length <= 5)
        medium_routes = sum(1 for length in route_lengths if 5 < length <= 15)
        long_routes = sum(1 for length in route_lengths if length > 15)
        
        sizes = [short_routes, medium_routes, long_routes]
        labels = ['Tuyến Ngắn (≤5 KH)', 'Tuyến Trung Bình (6-15 KH)', 'Tuyến Dài (>15 KH)']
        colors = ['lightgreen', 'gold', 'lightcoral']
        
        plt.pie(sizes, labels=labels, colors=colors, autopct='%1.1f%%', startangle=90)
        plt.title('Phân Loại Tuyến Đường Theo Độ Dài', fontsize=14, fontweight='bold')
        
        plt.tight_layout()
        plt.savefig(f'SA_route_length_analysis_{self.data["test_name"]}.png', dpi=300, bbox_inches='tight')
        plt.show()
    
    def generate_complete_report(self):
        """Tạo báo cáo hoàn chỉnh và hiệu quả về các giải pháp SA"""
        print("=" * 80)
        print("ĐANG TẠO BÁO CÁO TRỰC QUAN TOÀN DIỆN CHO THUẬT TOÁN SIMULATED ANNEALING")
        print("=" * 80)
        
        # 1. Báo cáo tổng quan cơ bản
        print("\n1. Tạo báo cáo tổng quan...")
        self.create_summary_report()
        
        # 2. Phân tích phân phối fitness
        print("\n2. Phân tích phân phối fitness...")
        self.plot_fitness_distribution()
        
        # 3. Phân tích tiến hóa và biến đổi giải pháp (thay thế solution_comparison cũ)
        print("\n3. Phân tích tiến hóa và biến đổi giải pháp...")
        self.plot_solution_evolution_analysis()
        
        # 4. Phân tích sự đa dạng và khác biệt giữa các giải pháp
        print("\n4. Phân tích sự đa dạng giải pháp...")
        self.analyze_solution_diversity()
        
        # 5. Phân tích cấu trúc tuyến đường
        print("\n5. Phân tích cấu trúc tuyến đường...")
        self.plot_routes_analysis()
        
        # 6. Phân tích độ dài tuyến
        print("\n6. Phân tích phân phối độ dài tuyến...")
        self.plot_route_length_distribution()
        
        # 7. Tạo bảng tóm tắt toàn diện
        print("\n7. Tạo bảng tóm tắt toàn diện...")
        self.create_comprehensive_summary_table()
        
        # Tóm tắt kết quả
        print("\n" + "=" * 80)
        print("ĐÃ HOÀN THÀNH TẠO BÁO CÁO TRỰC QUAN TOÀN DIỆN")
        print("=" * 80)
        print(f"Bài kiểm tra: {self.data['test_name']}")
        print(f"Tổng số giải pháp được phân tích: {len(self.solutions)}")
        print(f"Thời gian thực thi: {self.data['run_time']:.2f} giây")
        print(f"Cải thiện tốt nhất: {self.data['improvement_percent']:.2f}%")
        
        print("\nCÁC FILE ĐÃ ĐƯỢC TẠO:")
        files_created = [
            f"SA_fitness_distribution_{self.data['test_name']}.png",
            f"SA_evolution_analysis_{self.data['test_name']}.png", 
            f"SA_diversity_analysis_{self.data['test_name']}.png",
            f"SA_routes_analysis_{self.data['test_name']}.png",
            f"SA_route_length_analysis_{self.data['test_name']}.png",
            f"SA_executive_dashboard_{self.data['test_name']}.png",
            f"SA_comprehensive_analysis_{self.data['test_name']}.xlsx"
        ]
        
        for i, file in enumerate(files_created, 1):
            print(f"{i:2d}. {file}")
        
        print("\nMÔ TẢ CÁC BIỂU ĐỒ:")
        descriptions = [
            "Phân phối fitness: Histogram và boxplot của các giá trị fitness",
            "Tiến hóa giải pháp: 9 biểu đồ phân tích sự tiến hóa và mối quan hệ",
            "Đa dạng giải pháp: Ma trận tương đồng và phân cụm hiệu suất", 
            "Phân tích tuyến: Phân phối số tuyến và mối quan hệ với fitness",
            "Độ dài tuyến: Phân tích cấu trúc và phân loại tuyến đường",
            "Dashboard tổng hợp: Trang tổng quan với 9 biểu đồ KPI chính",
            "Báo cáo Excel: 3 sheet với tóm tắt nhóm, top 10 và thống kê tổng quan"
        ]
        
        for i, desc in enumerate(descriptions, 1):
            print(f"{i:2d}. {desc}")
        
        print("\n" + "=" * 80)
    
    def create_executive_dashboard(self):
        """Tạo dashboard tổng hợp cho báo cáo điều hành"""
        if not self.solutions:
            return
            
        fig = plt.figure(figsize=(20, 12))
        gs = fig.add_gridspec(3, 4, hspace=0.3, wspace=0.3)
        
        fitness_values = [sol['fitness'] for sol in self.solutions]
        route_counts = [sol['num_routes'] for sol in self.solutions]
        efficiency_values = [sol['total_customers']/sol['num_routes'] if sol['num_routes'] > 0 else 0 
                           for sol in self.solutions]
        improvements = [(self.data['initial_fitness'] - sol['fitness'])/self.data['initial_fitness']*100 
                       for sol in self.solutions]
        
        # 1. KPI chính (góc trên trái)
        ax1 = fig.add_subplot(gs[0, 0])
        kpi_text = f"""
CHỈ SỐ HIỆU SUẤT CHÍNH

🎯 Fitness Tốt Nhất: {min(fitness_values):,.0f}
📈 Cải Thiện: {self.data['improvement_percent']:.1f}%
⏱️ Thời Gian: {self.data['run_time']:.1f}s
🔢 Số Giải Pháp: {len(self.solutions)}

📊 Độ Ổn Định: {np.std(fitness_values)/np.mean(fitness_values)*100:.1f}%
🚛 Tuyến TB: {np.mean(route_counts):.1f}
👥 Hiệu Quả TB: {np.mean(efficiency_values):.1f} KH/tuyến
        """
        ax1.text(0.05, 0.95, kpi_text, transform=ax1.transAxes, fontsize=12,
                verticalalignment='top', fontfamily='monospace',
                bbox=dict(boxstyle='round', facecolor='lightgreen', alpha=0.8))
        ax1.set_title('DASHBOARD TỔNG QUAN SA', fontsize=16, fontweight='bold')
        ax1.axis('off')
        
        # 2. Biểu đồ tiến hóa fitness
        ax2 = fig.add_subplot(gs[0, 1:3])
        ax2.plot(range(len(fitness_values)), fitness_values, 'o-', color='blue', alpha=0.7, markersize=3)
        ax2.axhline(y=self.data['initial_fitness'], color='red', linestyle='--', alpha=0.7,
                   label=f'Ban Đầu: {self.data["initial_fitness"]:,.0f}')
        ax2.axhline(y=min(fitness_values), color='green', linestyle='--', alpha=0.7,
                   label=f'Tốt Nhất: {min(fitness_values):,.0f}')
        ax2.set_title('TIẾN HÓA FITNESS', fontweight='bold')
        ax2.set_xlabel('Thứ Tự Giải Pháp')
        ax2.set_ylabel('Fitness')
        ax2.legend()
        ax2.grid(True, alpha=0.3)
        
        # 3. Phân phối hiệu suất
        ax3 = fig.add_subplot(gs[0, 3])
        high_perf = sum(1 for f in fitness_values if f <= np.percentile(fitness_values, 25))
        med_perf = sum(1 for f in fitness_values if np.percentile(fitness_values, 25) < f <= np.percentile(fitness_values, 75))
        low_perf = sum(1 for f in fitness_values if f > np.percentile(fitness_values, 75))
        
        sizes = [high_perf, med_perf, low_perf]
        labels = ['Xuất Sắc', 'Trung Bình', 'Kém']
        colors = ['green', 'orange', 'red']
        ax3.pie(sizes, labels=labels, colors=colors, autopct='%1.1f%%', startangle=90)
        ax3.set_title('PHÂN LOẠI HIỆU SUẤT', fontweight='bold')
        
        # 4. Top 5 giải pháp
        ax4 = fig.add_subplot(gs[1, 0:2])
        sorted_solutions = sorted(self.solutions, key=lambda x: x['fitness'])[:5]
        top_fitness = [sol['fitness'] for sol in sorted_solutions]
        top_labels = [f"Sol {sol['solution_id']}" for sol in sorted_solutions]
        
        bars = ax4.bar(range(len(top_fitness)), top_fitness, color='gold', alpha=0.8, edgecolor='black')
        ax4.set_title('TOP 5 GIẢI PHÁP TỐT NHẤT', fontweight='bold')
        ax4.set_xlabel('Giải Pháp')
        ax4.set_ylabel('Fitness')
        ax4.set_xticks(range(len(top_labels)))
        ax4.set_xticklabels(top_labels, rotation=45)
        
        for i, bar in enumerate(bars):
            height = bar.get_height()
            ax4.text(bar.get_x() + bar.get_width()/2., height,
                    f'{height:,.0f}', ha='center', va='bottom', fontsize=9)
        ax4.grid(True, alpha=0.3)
        
        # 5. Mối quan hệ Tuyến vs Fitness
        ax5 = fig.add_subplot(gs[1, 2:4])
        scatter = ax5.scatter(route_counts, fitness_values, c=efficiency_values, 
                             cmap='viridis', alpha=0.6, s=50)
        ax5.set_title('MỐI QUAN HỆ: SỐ TUYẾN vs FITNESS', fontweight='bold')
        ax5.set_xlabel('Số Tuyến Đường')
        ax5.set_ylabel('Fitness')
        plt.colorbar(scatter, ax=ax5, label='Hiệu Quả (KH/Tuyến)')
        ax5.grid(True, alpha=0.3)
        
        # 6. Phân phối cải thiện
        ax6 = fig.add_subplot(gs[2, 0])
        ax6.hist(improvements, bins=15, color='lightblue', alpha=0.7, edgecolor='black')
        ax6.axvline(x=np.mean(improvements), color='red', linestyle='--',
                   label=f'TB: {np.mean(improvements):.1f}%')
        ax6.set_title('PHÂN PHỐI CẢI THIỆN', fontweight='bold')
        ax6.set_xlabel('Tỷ Lệ Cải Thiện (%)')
        ax6.set_ylabel('Tần Suất')
        ax6.legend()
        ax6.grid(True, alpha=0.3)
        
        # 7. Phân phối số tuyến
        ax7 = fig.add_subplot(gs[2, 1])
        route_counter = Counter(route_counts)
        routes, counts = zip(*sorted(route_counter.items()))
        ax7.bar(routes, counts, color='lightcoral', alpha=0.7, edgecolor='black')
        ax7.set_title('PHÂN PHỐI SỐ TUYẾN', fontweight='bold')
        ax7.set_xlabel('Số Tuyến')
        ax7.set_ylabel('Số Giải Pháp')
        ax7.grid(True, alpha=0.3)
        
        # 8. Boxplot fitness
        ax8 = fig.add_subplot(gs[2, 2])
        ax8.boxplot(fitness_values, vert=True)
        ax8.set_title('PHÂN PHỐI FITNESS', fontweight='bold')
        ax8.set_ylabel('Fitness')
        ax8.grid(True, alpha=0.3)
        
        # 9. Thống kê tóm tắt
        ax9 = fig.add_subplot(gs[2, 3])
        summary_stats = f"""
THỐNG KÊ CHI TIẾT

Fitness:
• Min: {min(fitness_values):,.0f}
• Max: {max(fitness_values):,.0f}
• TB: {np.mean(fitness_values):,.0f}
• Std: {np.std(fitness_values):,.0f}

Cải thiện:
• Tốt nhất: {max(improvements):.1f}%
• TB: {np.mean(improvements):.1f}%
• Số GP cải thiện: {sum(1 for x in improvements if x > 0)}

Cấu trúc:
• Tuyến min: {min(route_counts)}
• Tuyến max: {max(route_counts)}
• Đa dạng: {len(set(route_counts))} loại
        """
        ax9.text(0.05, 0.95, summary_stats, transform=ax9.transAxes, fontsize=10,
                verticalalignment='top', fontfamily='monospace',
                bbox=dict(boxstyle='round', facecolor='lightyellow', alpha=0.8))
        ax9.set_title('THỐNG KÊ CHI TIẾT', fontweight='bold')
        ax9.axis('off')
        
        plt.suptitle(f'DASHBOARD TỔNG HỢP PHÂN TÍCH SA - {self.data["test_name"]}', 
                    fontsize=18, fontweight='bold', y=0.98)
        
        plt.savefig(f'SA_executive_dashboard_{self.data["test_name"]}.png', 
                   dpi=300, bbox_inches='tight')
        plt.show()


def main():
    """Hàm chính để chạy trực quan hóa"""
    # Đường dẫn đến file kết quả SA
    results_file = "d:/Logistic/excute_data/logistic/SA_results_c101_20250727_152912.txt"
    
    try:
        # Tạo đối tượng trực quan hóa
        visualizer = SAResultsVisualizer(results_file)
        
        # Tạo báo cáo hoàn chỉnh
        visualizer.generate_complete_report()
        
        # Tạo dashboard tổng hợp
        print("\n8. Tạo dashboard tổng hợp...")
        visualizer.create_executive_dashboard()
        
    except FileNotFoundError:
        print(f"Không tìm thấy file: {results_file}")
        print("Vui lòng kiểm tra đường dẫn file.")
    except Exception as e:
        print(f"Lỗi khi xử lý: {e}")


if __name__ == "__main__":
    main()