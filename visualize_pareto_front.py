import pandas as pd
import numpy as np
import matplotlib.pyplot as plt
import seaborn as sns
from matplotlib.patches import Rectangle
import os

# Thiết lập font tiếng Việt
plt.rcParams['font.family'] = ['DejaVu Sans', 'Arial Unicode MS', 'Tahoma']
plt.rcParams['axes.unicode_minus'] = False

# === Đường dẫn tới file Excel ===
excel_file = "20250731_152032_optimization_results.xlsx"
sheet_name = "Optimization Results"

# === Đọc và chuẩn hóa bảng dữ liệu ===
print("Đang đọc dữ liệu từ file Excel...")
xls = pd.ExcelFile(excel_file)
df = xls.parse(sheet_name)

df.columns = [
    'Instance', 'Algorithm',
    'NV_Min', 'NV_Std', 'NV_Mean',
    'TC_Min', 'TC_Std', 'TC_Mean',
    'SD_Min', 'SD_Std', 'SD_Mean',
    'WT_Min', 'WT_Std', 'WT_Mean',
    'Time_ms'
]

df = df[df['Algorithm'].notna()]
numeric_cols = ['NV_Min', 'NV_Std', 'NV_Mean', 'TC_Min', 'TC_Std', 'TC_Mean',
                'SD_Min', 'SD_Std', 'SD_Mean', 'WT_Min', 'WT_Std', 'WT_Mean', 'Time_ms']
df[numeric_cols] = df[numeric_cols].apply(pd.to_numeric, errors='coerce')

print(f"Đã đọc {len(df)} dòng dữ liệu")
print(f"Các thuật toán có trong dữ liệu: {df['Algorithm'].unique()}")

# === Hàm tính Hypervolume 2 mục tiêu ===
def compute_hv_2d(pareto_points, reference_point):
    """Tính Hypervolume cho 2 mục tiêu (minimization)"""
    if len(pareto_points) == 0:
        return 0.0
    
    # Sắp xếp theo mục tiêu đầu tiên
    pareto_points = pareto_points[np.argsort(pareto_points[:, 0])]
    hv = 0.0
    prev_f1 = reference_point[0]
    
    for f1, f2 in reversed(pareto_points):
        width = prev_f1 - f1
        height = reference_point[1] - f2
        if width > 0 and height > 0:  # Chỉ tính nếu điểm nằm trong vùng dominated
            hv += width * height
        prev_f1 = f1
    return hv

# === Hàm tìm Pareto front ===
def find_pareto_front(points):
    """Tìm Pareto front cho bài toán minimization"""
    points = np.array(points)
    pareto_front = []
    
    for i, point in enumerate(points):
        is_dominated = False
        for j, other_point in enumerate(points):
            if i != j:
                # Kiểm tra xem point có bị dominated bởi other_point không
                if (other_point[0] <= point[0] and other_point[1] <= point[1] and 
                    (other_point[0] < point[0] or other_point[1] < point[1])):
                    is_dominated = True
                    break
        
        if not is_dominated:
            pareto_front.append(point)
    
    return np.array(pareto_front)

# === Tạo visualization ===
def create_pareto_visualization():
    """Tạo các biểu đồ visualization cho Pareto front"""
    
    # Tạo thư mục lưu kết quả
    output_dir = "pareto_visualization"
    os.makedirs(output_dir, exist_ok=True)
    
    # === 1. Biểu đồ scatter plot tất cả các điểm ===
    plt.figure(figsize=(15, 10))
    
    # Subplot 1: TC_Mean vs WT_Mean
    plt.subplot(2, 2, 1)
    
    # Vẽ các điểm theo thuật toán
    algorithms = df['Algorithm'].unique()
    colors = plt.cm.Set3(np.linspace(0, 1, len(algorithms)))
    
    for i, algo in enumerate(algorithms):
        algo_data = df[df['Algorithm'] == algo]
        plt.scatter(algo_data['TC_Mean'], algo_data['WT_Mean'], 
                   c=[colors[i]], label=algo, alpha=0.7, s=50)
    
    # Tìm và vẽ Pareto front
    all_points = df[['TC_Mean', 'WT_Mean']].dropna().values
    pareto_front = find_pareto_front(all_points)
    
    if len(pareto_front) > 0:
        # Sắp xếp Pareto front để vẽ đường nối
        pareto_front = pareto_front[np.argsort(pareto_front[:, 0])]
        plt.plot(pareto_front[:, 0], pareto_front[:, 1], 'r-', linewidth=2, 
                label='Pareto Front', alpha=0.8)
        plt.scatter(pareto_front[:, 0], pareto_front[:, 1], 
                   c='red', s=100, marker='*', label='Pareto Points', zorder=5)
    
    plt.xlabel('TC_Mean (Chi phí trung bình)')
    plt.ylabel('WT_Mean (Thời gian chờ trung bình)')
    plt.title('Pareto Front: TC_Mean vs WT_Mean')
    plt.legend(bbox_to_anchor=(1.05, 1), loc='upper left')
    plt.grid(True, alpha=0.3)
    
    # Subplot 2: NV_Mean vs TC_Mean
    plt.subplot(2, 2, 2)
    
    for i, algo in enumerate(algorithms):
        algo_data = df[df['Algorithm'] == algo]
        plt.scatter(algo_data['NV_Mean'], algo_data['TC_Mean'], 
                   c=[colors[i]], label=algo, alpha=0.7, s=50)
    
    # Tìm Pareto front cho NV_Mean vs TC_Mean
    nv_tc_points = df[['NV_Mean', 'TC_Mean']].dropna().values
    nv_tc_pareto = find_pareto_front(nv_tc_points)
    
    if len(nv_tc_pareto) > 0:
        nv_tc_pareto = nv_tc_pareto[np.argsort(nv_tc_pareto[:, 0])]
        plt.plot(nv_tc_pareto[:, 0], nv_tc_pareto[:, 1], 'r-', linewidth=2, alpha=0.8)
        plt.scatter(nv_tc_pareto[:, 0], nv_tc_pareto[:, 1], 
                   c='red', s=100, marker='*', zorder=5)
    
    plt.xlabel('NV_Mean (Số xe trung bình)')
    plt.ylabel('TC_Mean (Chi phí trung bình)')
    plt.title('Pareto Front: NV_Mean vs TC_Mean')
    plt.grid(True, alpha=0.3)
    
    # Subplot 3: Hypervolume visualization
    plt.subplot(2, 2, 3)
    
    # Vẽ lại TC_Mean vs WT_Mean với vùng Hypervolume
    for i, algo in enumerate(algorithms):
        algo_data = df[df['Algorithm'] == algo]
        plt.scatter(algo_data['TC_Mean'], algo_data['WT_Mean'], 
                   c=[colors[i]], label=algo, alpha=0.7, s=50)
    
    # Vẽ Pareto front
    if len(pareto_front) > 0:
        plt.plot(pareto_front[:, 0], pareto_front[:, 1], 'r-', linewidth=2, alpha=0.8)
        plt.scatter(pareto_front[:, 0], pareto_front[:, 1], 
                   c='red', s=100, marker='*', zorder=5)
        
        # Tính và hiển thị reference point
        ref_point = [
            np.max(all_points[:, 0]) * 1.1,
            np.max(all_points[:, 1]) * 1.1
        ]
        
        plt.scatter(ref_point[0], ref_point[1], c='black', s=200, 
                   marker='x', linewidth=3, label='Reference Point')
        
        # Vẽ vùng Hypervolume (đơn giản hóa)
        for point in pareto_front:
            rect = Rectangle((point[0], point[1]), 
                           ref_point[0] - point[0], 
                           ref_point[1] - point[1],
                           alpha=0.1, facecolor='blue')
            plt.gca().add_patch(rect)
        
        # Tính Hypervolume
        hv = compute_hv_2d(pareto_front, ref_point)
        plt.text(0.05, 0.95, f'Hypervolume: {hv:.2f}', 
                transform=plt.gca().transAxes, fontsize=12,
                bbox=dict(boxstyle="round,pad=0.3", facecolor="yellow", alpha=0.7))
    
    plt.xlabel('TC_Mean (Chi phí trung bình)')
    plt.ylabel('WT_Mean (Thời gian chờ trung bình)')
    plt.title('Hypervolume Visualization')
    plt.grid(True, alpha=0.3)
    
    # Subplot 4: Thống kê Pareto points
    plt.subplot(2, 2, 4)
    
    # Đếm số Pareto points cho mỗi thuật toán
    pareto_counts = {}
    for algo in algorithms:
        algo_data = df[df['Algorithm'] == algo][['TC_Mean', 'WT_Mean']].dropna().values
        if len(algo_data) > 0:
            algo_pareto = find_pareto_front(algo_data)
            pareto_counts[algo] = len(algo_pareto)
        else:
            pareto_counts[algo] = 0
    
    algos = list(pareto_counts.keys())
    counts = list(pareto_counts.values())
    
    bars = plt.bar(range(len(algos)), counts, color=colors[:len(algos)])
    plt.xlabel('Thuật toán')
    plt.ylabel('Số điểm Pareto')
    plt.title('Số lượng điểm Pareto theo thuật toán')
    plt.xticks(range(len(algos)), algos, rotation=45, ha='right')
    
    # Thêm giá trị lên đầu cột
    for bar, count in zip(bars, counts):
        plt.text(bar.get_x() + bar.get_width()/2, bar.get_height() + 0.1,
                str(count), ha='center', va='bottom')
    
    plt.tight_layout()
    plt.savefig(f'{output_dir}/pareto_front_analysis.png', dpi=300, bbox_inches='tight')
    plt.show()
    
    # === 2. Biểu đồ 3D (nếu có đủ dữ liệu) ===
    fig = plt.figure(figsize=(12, 8))
    ax = fig.add_subplot(111, projection='3d')
    
    for i, algo in enumerate(algorithms):
        algo_data = df[df['Algorithm'] == algo]
        ax.scatter(algo_data['TC_Mean'], algo_data['WT_Mean'], algo_data['NV_Mean'],
                  c=[colors[i]], label=algo, alpha=0.7, s=50)
    
    ax.set_xlabel('TC_Mean (Chi phí)')
    ax.set_ylabel('WT_Mean (Thời gian chờ)')
    ax.set_zlabel('NV_Mean (Số xe)')
    ax.set_title('Biểu đồ 3D: TC_Mean vs WT_Mean vs NV_Mean')
    ax.legend()
    
    plt.savefig(f'{output_dir}/pareto_3d_plot.png', dpi=300, bbox_inches='tight')
    plt.show()
    
    # === 3. Heatmap correlation ===
    plt.figure(figsize=(10, 8))
    
    correlation_cols = ['NV_Mean', 'TC_Mean', 'WT_Mean', 'SD_Mean', 'Time_ms']
    corr_matrix = df[correlation_cols].corr()
    
    sns.heatmap(corr_matrix, annot=True, cmap='coolwarm', center=0,
                square=True, fmt='.3f')
    plt.title('Ma trận tương quan giữa các chỉ số')
    plt.tight_layout()
    plt.savefig(f'{output_dir}/correlation_heatmap.png', dpi=300, bbox_inches='tight')
    plt.show()
    
    return pareto_front, ref_point

# === Phân tích chi tiết ===
def detailed_analysis():
    """Phân tích chi tiết về Pareto front"""
    
    print("\n" + "="*60)
    print("PHÂN TÍCH CHI TIẾT PARETO FRONT")
    print("="*60)
    
    # Tìm Pareto front
    all_points = df[['TC_Mean', 'WT_Mean']].dropna().values
    pareto_front = find_pareto_front(all_points)
    
    print(f"\n1. THÔNG TIN TỔNG QUAN:")
    print(f"   - Tổng số điểm dữ liệu: {len(all_points)}")
    print(f"   - Số điểm Pareto: {len(pareto_front)}")
    print(f"   - Tỷ lệ Pareto: {len(pareto_front)/len(all_points)*100:.2f}%")
    
    # Tính reference point và Hypervolume
    ref_point = [
        np.max(all_points[:, 0]) * 1.1,
        np.max(all_points[:, 1]) * 1.1
    ]
    hv = compute_hv_2d(pareto_front, ref_point)
    
    print(f"\n2. HYPERVOLUME:")
    print(f"   - Reference Point: ({ref_point[0]:.2f}, {ref_point[1]:.2f})")
    print(f"   - Hypervolume: {hv:.4f}")
    
    # Phân tích từng thuật toán
    print(f"\n3. PHÂN TÍCH THEO THUẬT TOÁN:")
    algorithms = df['Algorithm'].unique()
    
    for algo in algorithms:
        algo_data = df[df['Algorithm'] == algo]
        algo_points = algo_data[['TC_Mean', 'WT_Mean']].dropna().values
        
        if len(algo_points) > 0:
            algo_pareto = find_pareto_front(algo_points)
            algo_hv = compute_hv_2d(algo_pareto, ref_point)
            
            print(f"\n   {algo}:")
            print(f"     - Số điểm: {len(algo_points)}")
            print(f"     - Số điểm Pareto: {len(algo_pareto)}")
            print(f"     - Hypervolume: {algo_hv:.4f}")
            print(f"     - TC_Mean trung bình: {algo_data['TC_Mean'].mean():.2f}")
            print(f"     - WT_Mean trung bình: {algo_data['WT_Mean'].mean():.2f}")
    
    # Top 5 điểm tốt nhất
    print(f"\n4. TOP 5 ĐIỂM TỐT NHẤT (TC_Mean + WT_Mean thấp nhất):")
    df_copy = df.copy()
    df_copy['Combined_Score'] = df_copy['TC_Mean'] + df_copy['WT_Mean']
    top_5 = df_copy.nsmallest(5, 'Combined_Score')
    
    for i, (idx, row) in enumerate(top_5.iterrows(), 1):
        print(f"   {i}. {row['Algorithm']} - Instance: {row['Instance']}")
        print(f"      TC_Mean: {row['TC_Mean']:.2f}, WT_Mean: {row['WT_Mean']:.2f}")
        print(f"      Combined Score: {row['Combined_Score']:.2f}")

# === Main execution ===
if __name__ == "__main__":
    print("Bắt đầu tạo visualization cho Pareto front...")
    
    # Tạo visualization
    pareto_front, ref_point = create_pareto_visualization()
    
    # Phân tích chi tiết
    detailed_analysis()
    
    print(f"\n✅ Hoàn thành! Các file visualization đã được lưu trong thư mục 'pareto_visualization/'")
    print(f"📊 Các file được tạo:")
    print(f"   - pareto_front_analysis.png: Phân tích tổng quan Pareto front")
    print(f"   - pareto_3d_plot.png: Biểu đồ 3D")
    print(f"   - correlation_heatmap.png: Ma trận tương quan")