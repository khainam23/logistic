"""
Phân tích và trực quan hóa kết quả solution cho bài toán VRPSPDTW
Dataset: Wang_Chen
"""

import os
import json
from typing import Dict, List, Tuple
from dataclasses import dataclass
import statistics

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

class SolutionAnalyzer:
    """Phân tích tập solution"""
    
    def __init__(self, solution_folder: str):
        self.solution_folder = solution_folder
        self.solutions: Dict[str, SolutionStats] = {}
        
    def load_solutions(self):
        """Đọc tất cả file solution"""
        if not os.path.exists(self.solution_folder):
            print(f"Không tìm thấy thư mục solution: {self.solution_folder}")
            return
        
        solution_files = [f for f in os.listdir(self.solution_folder) if f.endswith('.txt')]
        solution_files.sort()
        
        print(f"Đang phân tích {len(solution_files)} solution files...")
        
        for filename in solution_files:
            filepath = os.path.join(self.solution_folder, filename)
            try:
                stats = self._analyze_single_solution(filepath)
                if stats:
                    self.solutions[stats.instance_name] = stats
            except Exception as e:
                print(f"Lỗi khi phân tích {filename}: {str(e)}")
                continue
    
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
        
        # Ước tính tổng số khách hàng từ tên file (giả sử format như Cdp101 = 100 customers)
        if instance_name.lower().startswith('cdp'):
            total_customers = int(instance_name[3:]) if instance_name[3:].isdigit() else served_customers
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
    
    def generate_summary_report(self, output_file: str):
        """Tạo báo cáo tổng hợp"""
        if not self.solutions:
            print("Không có solution nào để phân tích!")
            return
        
        with open(output_file, 'w', encoding='utf-8') as f:
            f.write("="*80 + "\n")
            f.write("BÁO CÁO PHÂN TÍCH SOLUTION - THUẬT TOÁN GREEDY VRPSPDTW\n")
            f.write("Dataset: Wang_Chen\n")
            f.write("="*80 + "\n\n")
            
            # Thống kê tổng quan
            f.write("1. THỐNG KÊ TỔNG QUAN\n")
            f.write("-"*40 + "\n")
            f.write(f"Tổng số instances đã giải: {len(self.solutions)}\n")
            
            total_routes = sum(stats.num_routes for stats in self.solutions.values())
            total_served = sum(stats.served_customers for stats in self.solutions.values())
            total_unserved = sum(stats.unserved_customers for stats in self.solutions.values())
            
            f.write(f"Tổng số routes: {total_routes}\n")
            f.write(f"Tổng khách hàng được phục vụ: {total_served}\n")
            f.write(f"Tổng khách hàng chưa phục vụ: {total_unserved}\n")
            
            if total_served + total_unserved > 0:
                service_rate = (total_served / (total_served + total_unserved)) * 100
                f.write(f"Tỷ lệ phục vụ: {service_rate:.2f}%\n")
            
            f.write("\n")
            
            # Thống kê chi tiết từng instance
            f.write("2. THỐNG KÊ CHI TIẾT TỪNG INSTANCE\n")
            f.write("-"*80 + "\n")
            f.write(f"{'Instance':<15} {'Routes':<8} {'Served':<8} {'Unserved':<10} {'Avg_Len':<8} {'Min_Len':<8} {'Max_Len':<8}\n")
            f.write("-"*80 + "\n")
            
            for instance_name in sorted(self.solutions.keys()):
                stats = self.solutions[instance_name]
                f.write(f"{stats.instance_name:<15} {stats.num_routes:<8} {stats.served_customers:<8} "
                       f"{stats.unserved_customers:<10} {stats.avg_route_length:<8.1f} "
                       f"{stats.min_route_length:<8} {stats.max_route_length:<8}\n")
            
            f.write("\n")
            
            # Phân tích phân phối độ dài route
            f.write("3. PHÂN TÍCH PHÂN PHỐI ĐỘ DÀI ROUTE\n")
            f.write("-"*40 + "\n")
            
            all_route_lengths = []
            for stats in self.solutions.values():
                all_route_lengths.extend(stats.route_lengths)
            
            if all_route_lengths:
                f.write(f"Tổng số routes: {len(all_route_lengths)}\n")
                f.write(f"Độ dài route trung bình: {statistics.mean(all_route_lengths):.2f}\n")
                f.write(f"Độ dài route ngắn nhất: {min(all_route_lengths)}\n")
                f.write(f"Độ dài route dài nhất: {max(all_route_lengths)}\n")
                f.write(f"Độ lệch chuẩn: {statistics.stdev(all_route_lengths):.2f}\n")
                
                # Histogram đơn giản
                f.write("\nPhân phối độ dài route:\n")
                length_counts = {}
                for length in all_route_lengths:
                    length_counts[length] = length_counts.get(length, 0) + 1
                
                for length in sorted(length_counts.keys()):
                    count = length_counts[length]
                    percentage = (count / len(all_route_lengths)) * 100
                    bar = "█" * min(int(percentage), 50)
                    f.write(f"Độ dài {length:2d}: {count:3d} routes ({percentage:5.1f}%) {bar}\n")
            
            f.write("\n")
            
            # Top instances theo các tiêu chí
            f.write("4. XẾP HẠNG INSTANCES\n")
            f.write("-"*40 + "\n")
            
            # Top instances có nhiều routes nhất
            f.write("Top 10 instances có nhiều routes nhất:\n")
            top_routes = sorted(self.solutions.values(), key=lambda x: x.num_routes, reverse=True)[:10]
            for i, stats in enumerate(top_routes, 1):
                f.write(f"{i:2d}. {stats.instance_name}: {stats.num_routes} routes\n")
            
            f.write("\nTop 10 instances có tỷ lệ phục vụ thấp nhất:\n")
            instances_with_unserved = [stats for stats in self.solutions.values() if stats.unserved_customers > 0]
            if instances_with_unserved:
                top_unserved = sorted(instances_with_unserved, 
                                    key=lambda x: x.unserved_customers / max(x.total_customers, 1), 
                                    reverse=True)[:10]
                for i, stats in enumerate(top_unserved, 1):
                    rate = (stats.unserved_customers / max(stats.total_customers, 1)) * 100
                    f.write(f"{i:2d}. {stats.instance_name}: {stats.unserved_customers}/{stats.total_customers} ({rate:.1f}%)\n")
            else:
                f.write("Tất cả instances đều được phục vụ 100%!\n")
            
            f.write("\n")
            
            # Kết luận
            f.write("5. KẾT LUẬN\n")
            f.write("-"*40 + "\n")
            f.write("Thuật toán Greedy cho VRPSPDTW đã được áp dụng thành công.\n")
            
            if total_unserved == 0:
                f.write("✓ Tất cả khách hàng đều được phục vụ!\n")
            else:
                f.write(f"⚠ Còn {total_unserved} khách hàng chưa được phục vụ.\n")
            
            avg_routes_per_instance = total_routes / len(self.solutions)
            f.write(f"Trung bình {avg_routes_per_instance:.1f} routes/instance.\n")
            
            if all_route_lengths:
                avg_customers_per_route = statistics.mean(all_route_lengths)
                f.write(f"Trung bình {avg_customers_per_route:.1f} khách hàng/route.\n")
            
            f.write("\n" + "="*80 + "\n")
    
    def generate_detailed_report(self, output_file: str):
        """Tạo báo cáo chi tiết từng instance"""
        if not self.solutions:
            print("Không có solution nào để phân tích!")
            return
        
        with open(output_file, 'w', encoding='utf-8') as f:
            f.write("="*80 + "\n")
            f.write("BÁO CÁO CHI TIẾT SOLUTION - THUẬT TOÁN GREEDY VRPSPDTW\n")
            f.write("="*80 + "\n\n")
            
            for instance_name in sorted(self.solutions.keys()):
                stats = self.solutions[instance_name]
                
                f.write(f"INSTANCE: {stats.instance_name}\n")
                f.write("-"*50 + "\n")
                f.write(f"Số routes: {stats.num_routes}\n")
                f.write(f"Khách hàng được phục vụ: {stats.served_customers}/{stats.total_customers}\n")
                
                if stats.unserved_customers > 0:
                    f.write(f"Khách hàng chưa phục vụ: {stats.unserved_customers}\n")
                
                f.write(f"Độ dài route: Min={stats.min_route_length}, Max={stats.max_route_length}, Avg={stats.avg_route_length:.1f}\n")
                
                f.write("\nCác routes:\n")
                for i, route in enumerate(stats.routes, 1):
                    route_str = " -> ".join(map(str, route))
                    customers_in_route = len(route) - 2
                    f.write(f"  Route {i}: {route_str} ({customers_in_route} khách hàng)\n")
                
                f.write("\n" + "="*50 + "\n\n")
    
    def export_json_summary(self, output_file: str):
        """Xuất thống kê dưới dạng JSON"""
        summary_data = {
            "total_instances": len(self.solutions),
            "total_routes": sum(stats.num_routes for stats in self.solutions.values()),
            "total_served_customers": sum(stats.served_customers for stats in self.solutions.values()),
            "total_unserved_customers": sum(stats.unserved_customers for stats in self.solutions.values()),
            "instances": {}
        }
        
        for instance_name, stats in self.solutions.items():
            summary_data["instances"][instance_name] = {
                "num_routes": stats.num_routes,
                "served_customers": stats.served_customers,
                "unserved_customers": stats.unserved_customers,
                "total_customers": stats.total_customers,
                "avg_route_length": round(stats.avg_route_length, 2),
                "min_route_length": stats.min_route_length,
                "max_route_length": stats.max_route_length,
                "route_lengths": stats.route_lengths
            }
        
        with open(output_file, 'w', encoding='utf-8') as f:
            json.dump(summary_data, f, indent=2, ensure_ascii=False)

def analyze_wang_chen_solutions():
    """Phân tích solutions của dataset Wang_Chen"""
    solution_folder = r"d:\Logistic\vrpspdtw\VRPenstein\data\Wang_Chen\solution"
    output_folder = r"d:\Logistic\vrpspdtw\VRPenstein\output"
    
    # Tạo thư mục output nếu chưa có
    if not os.path.exists(output_folder):
        os.makedirs(output_folder)
    
    print("PHÂN TÍCH SOLUTION VRPSPDTW - THUẬT TOÁN GREEDY")
    print("="*60)
    
    # Khởi tạo analyzer
    analyzer = SolutionAnalyzer(solution_folder)
    
    # Load và phân tích solutions
    analyzer.load_solutions()
    
    if not analyzer.solutions:
        print("Không tìm thấy solution nào để phân tích!")
        return
    
    print(f"Đã load {len(analyzer.solutions)} solutions thành công!")
    
    # Tạo các báo cáo
    summary_file = os.path.join(output_folder, "solution_summary.txt")
    detailed_file = os.path.join(output_folder, "solution_detailed.txt")
    json_file = os.path.join(output_folder, "solution_stats.json")
    
    print("Đang tạo báo cáo tổng hợp...")
    analyzer.generate_summary_report(summary_file)
    
    print("Đang tạo báo cáo chi tiết...")
    analyzer.generate_detailed_report(detailed_file)
    
    print("Đang xuất dữ liệu JSON...")
    analyzer.export_json_summary(json_file)
    
    print("\nHoàn thành! Các file báo cáo:")
    print(f"- Báo cáo tổng hợp: {summary_file}")
    print(f"- Báo cáo chi tiết: {detailed_file}")
    print(f"- Dữ liệu JSON: {json_file}")
    
    # In một số thống kê nhanh
    total_instances = len(analyzer.solutions)
    total_routes = sum(stats.num_routes for stats in analyzer.solutions.values())
    total_served = sum(stats.served_customers for stats in analyzer.solutions.values())
    total_unserved = sum(stats.unserved_customers for stats in analyzer.solutions.values())
    
    print(f"\nThống kê nhanh:")
    print(f"- Instances: {total_instances}")
    print(f"- Routes: {total_routes}")
    print(f"- Khách hàng phục vụ: {total_served}")
    print(f"- Khách hàng chưa phục vụ: {total_unserved}")
    
    if total_served + total_unserved > 0:
        service_rate = (total_served / (total_served + total_unserved)) * 100
        print(f"- Tỷ lệ phục vụ: {service_rate:.2f}%")

if __name__ == "__main__":
    analyze_wang_chen_solutions()