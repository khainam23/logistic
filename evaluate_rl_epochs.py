#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Script đánh giá các RL epoch cho bài toán PDPTW
Đánh giá: số lượng điểm, số lượng vehicle, khoảng cách di chuyển
"""

import os
import re
import pandas as pd
import numpy as np
from pathlib import Path
import matplotlib.pyplot as plt
import seaborn as sns

class RLEpochEvaluator:
    def __init__(self, solution_dir):
        self.solution_dir = Path(solution_dir)
        self.results = []
        
    def parse_solution_file(self, file_path):
        """
        Parse một file solution và trích xuất thông tin
        """
        with open(file_path, 'r', encoding='utf-8') as f:
            lines = f.readlines()
        
        routes = []
        for line in lines:
            line = line.strip()
            if line.startswith('Route'):
                # Tách route number và customer list
                parts = line.split(':')
                if len(parts) == 2:
                    route_num = int(parts[0].split()[1])
                    customers = []
                    if parts[1].strip():  # Nếu route không rỗng
                        customers = [int(x) for x in parts[1].strip().split()]
                    routes.append({
                        'route_num': route_num,
                        'customers': customers
                    })
        
        return routes
    
    def calculate_basic_metrics(self, routes):
        """
        Tính các metric cơ bản từ routes
        """
        # Số lượng vehicle được sử dụng (routes không rỗng)
        num_vehicles = len([r for r in routes if len(r['customers']) > 0])
        
        # Tổng số điểm được phục vụ
        all_customers = []
        for route in routes:
            all_customers.extend(route['customers'])
        num_customers = len(all_customers)
        
        # Số điểm unique (tránh trùng lặp nếu có)
        unique_customers = len(set(all_customers))
        
        # Tính số điểm trung bình mỗi route
        non_empty_routes = [r for r in routes if len(r['customers']) > 0]
        avg_customers_per_route = np.mean([len(r['customers']) for r in non_empty_routes]) if non_empty_routes else 0
        
        return {
            'num_vehicles': num_vehicles,
            'num_customers': num_customers,
            'unique_customers': unique_customers,
            'avg_customers_per_route': avg_customers_per_route,
            'total_routes': len(routes)
        }
    
    def calculate_distance_estimate(self, routes):
        """
        Ước tính khoảng cách dựa trên số lượng điểm
        (Sẽ được cải thiện khi có dữ liệu tọa độ thực tế)
        """
        total_distance = 0
        
        for route in routes:
            if len(route['customers']) > 0:
                # Ước tính: depot -> first customer + inter-customer + last customer -> depot
                # Giả sử khoảng cách trung bình giữa các điểm là 10 đơn vị
                estimated_distance = (len(route['customers']) + 1) * 10
                total_distance += estimated_distance
        
        return total_distance
    
    def extract_file_info(self, filename):
        """
        Trích xuất thông tin từ tên file
        """
        # Pattern: rl_epoch_lc101_epoch1.txt
        pattern = r'rl_epoch_([a-z]+\d+)_epoch(\d+)\.txt'
        match = re.match(pattern, filename)
        
        if match:
            instance = match.group(1)
            epoch = int(match.group(2))
            return instance, epoch
        return None, None
    
    def evaluate_all_files(self):
        """
        Đánh giá tất cả các file solution
        """
        solution_files = list(self.solution_dir.glob('*.txt'))
        
        for file_path in solution_files:
            instance, epoch = self.extract_file_info(file_path.name)
            
            if instance and epoch:
                try:
                    routes = self.parse_solution_file(file_path)
                    metrics = self.calculate_basic_metrics(routes)
                    estimated_distance = self.calculate_distance_estimate(routes)
                    
                    result = {
                        'filename': file_path.name,
                        'instance': instance,
                        'epoch': epoch,
                        'num_vehicles': metrics['num_vehicles'],
                        'num_customers': metrics['num_customers'],
                        'unique_customers': metrics['unique_customers'],
                        'avg_customers_per_route': metrics['avg_customers_per_route'],
                        'total_routes': metrics['total_routes'],
                        'estimated_distance': estimated_distance
                    }
                    
                    self.results.append(result)
                    
                except Exception as e:
                    print(f"Lỗi khi xử lý file {file_path.name}: {e}")
        
        return pd.DataFrame(self.results)
    
    def generate_summary_report(self, df):
        """
        Tạo báo cáo tổng hợp
        """
        print("=" * 80)
        print("BÁO CÁO ĐÁNH GIÁ RL EPOCHS")
        print("=" * 80)
        
        print(f"\nTổng số file được phân tích: {len(df)}")
        print(f"Số instance khác nhau: {df['instance'].nunique()}")
        print(f"Số epoch khác nhau: {df['epoch'].nunique()}")
        
        print("\n" + "=" * 50)
        print("THỐNG KÊ TỔNG QUAN")
        print("=" * 50)
        
        print(f"Số vehicles trung bình: {df['num_vehicles'].mean():.2f}")
        print(f"Số customers trung bình: {df['num_customers'].mean():.2f}")
        print(f"Khoảng cách ước tính trung bình: {df['estimated_distance'].mean():.2f}")
        
        print("\n" + "=" * 50)
        print("TOP 10 SOLUTION TỐT NHẤT (ít vehicles nhất)")
        print("=" * 50)
        
        best_solutions = df.nsmallest(10, 'num_vehicles')[
            ['filename', 'instance', 'epoch', 'num_vehicles', 'num_customers', 'estimated_distance']
        ]
        print(best_solutions.to_string(index=False))
        
        print("\n" + "=" * 50)
        print("THỐNG KÊ THEO INSTANCE")
        print("=" * 50)
        
        instance_stats = df.groupby('instance').agg({
            'num_vehicles': ['mean', 'min', 'max'],
            'num_customers': ['mean', 'min', 'max'],
            'estimated_distance': ['mean', 'min', 'max']
        }).round(2)
        
        print(instance_stats)
        
        return best_solutions, instance_stats
    
    def create_visualizations(self, df):
        """
        Tạo các biểu đồ trực quan
        """
        plt.style.use('default')
        fig, axes = plt.subplots(2, 2, figsize=(15, 12))
        fig.suptitle('Đánh giá RL Epochs - PDPTW', fontsize=16, fontweight='bold')
        
        # 1. Số vehicles theo epoch
        epoch_stats = df.groupby('epoch')['num_vehicles'].mean()
        axes[0, 0].plot(epoch_stats.index, epoch_stats.values, marker='o', linewidth=2, markersize=6)
        axes[0, 0].set_title('Số Vehicles Trung Bình Theo Epoch')
        axes[0, 0].set_xlabel('Epoch')
        axes[0, 0].set_ylabel('Số Vehicles')
        axes[0, 0].grid(True, alpha=0.3)
        
        # 2. Phân bố số vehicles
        axes[0, 1].hist(df['num_vehicles'], bins=20, alpha=0.7, color='skyblue', edgecolor='black')
        axes[0, 1].set_title('Phân Bố Số Vehicles')
        axes[0, 1].set_xlabel('Số Vehicles')
        axes[0, 1].set_ylabel('Tần Suất')
        axes[0, 1].grid(True, alpha=0.3)
        
        # 3. Số customers theo epoch
        customer_stats = df.groupby('epoch')['num_customers'].mean()
        axes[1, 0].plot(customer_stats.index, customer_stats.values, marker='s', linewidth=2, markersize=6, color='orange')
        axes[1, 0].set_title('Số Customers Trung Bình Theo Epoch')
        axes[1, 0].set_xlabel('Epoch')
        axes[1, 0].set_ylabel('Số Customers')
        axes[1, 0].grid(True, alpha=0.3)
        
        # 4. Khoảng cách ước tính theo epoch
        distance_stats = df.groupby('epoch')['estimated_distance'].mean()
        axes[1, 1].plot(distance_stats.index, distance_stats.values, marker='^', linewidth=2, markersize=6, color='green')
        axes[1, 1].set_title('Khoảng Cách Ước Tính Trung Bình Theo Epoch')
        axes[1, 1].set_xlabel('Epoch')
        axes[1, 1].set_ylabel('Khoảng Cách Ước Tính')
        axes[1, 1].grid(True, alpha=0.3)
        
        plt.tight_layout()
        plt.savefig('rl_epochs_evaluation.png', dpi=300, bbox_inches='tight')
        plt.show()
        
        # Biểu đồ heatmap cho từng instance
        if df['instance'].nunique() > 1:
            plt.figure(figsize=(12, 8))
            pivot_data = df.pivot_table(values='num_vehicles', index='instance', columns='epoch', aggfunc='mean')
            sns.heatmap(pivot_data, annot=True, fmt='.1f', cmap='YlOrRd', cbar_kws={'label': 'Số Vehicles'})
            plt.title('Heatmap: Số Vehicles Theo Instance và Epoch')
            plt.xlabel('Epoch')
            plt.ylabel('Instance')
            plt.tight_layout()
            plt.savefig('rl_epochs_heatmap.png', dpi=300, bbox_inches='tight')
            plt.show()
    
    def save_results_to_csv(self, df, filename='rl_epochs_results.csv'):
        """
        Lưu kết quả vào file CSV
        """
        df.to_csv(filename, index=False, encoding='utf-8')
        print(f"\nKết quả đã được lưu vào file: {filename}")

def main():
    # Đường dẫn đến thư mục chứa solution files
    solution_dir = "d:/Logistic/excute_data/logistic/rl_10_30/pdptw/solution"
    
    # Tạo evaluator
    evaluator = RLEpochEvaluator(solution_dir)
    
    print("Đang phân tích các file solution...")
    
    # Đánh giá tất cả files
    df = evaluator.evaluate_all_files()
    
    if df.empty:
        print("Không tìm thấy file solution nào để phân tích!")
        return
    
    # Tạo báo cáo
    best_solutions, instance_stats = evaluator.generate_summary_report(df)
    
    # Tạo biểu đồ
    print("\nĐang tạo biểu đồ...")
    evaluator.create_visualizations(df)
    
    # Lưu kết quả
    evaluator.save_results_to_csv(df)
    
    print("\nHoàn thành đánh giá!")
    print("Các file đã được tạo:")
    print("- rl_epochs_results.csv: Kết quả chi tiết")
    print("- rl_epochs_evaluation.png: Biểu đồ tổng quan")
    print("- rl_epochs_heatmap.png: Heatmap theo instance và epoch")

if __name__ == "__main__":
    main()