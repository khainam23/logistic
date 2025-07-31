
import pandas as pd
import numpy as np
import os

# === Đường dẫn tới file Excel ===
excel_file = "20250731_152032_optimization_results.xlsx"
sheet_name = "Optimization Results"

# === Đọc và chuẩn hóa bảng dữ liệu ===
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

# === Chọn 2 mục tiêu để tính HV ===
# Mặc định: TC_Mean và WT_Mean (minimization)
pareto_df = df[['TC_Mean', 'WT_Mean']].dropna().values

# === Hàm tính Hypervolume 2 mục tiêu ===
def compute_hv_2d(pareto_points, reference_point):
    pareto_points = pareto_points[np.argsort(pareto_points[:, 0])]
    hv = 0.0
    prev_f1 = reference_point[0]
    for f1, f2 in reversed(pareto_points):
        width = prev_f1 - f1
        height = reference_point[1] - f2
        hv += width * height
        prev_f1 = f1
    return hv

# === Tự động chọn điểm tham chiếu gấp 10% giá trị max ===
ref_point = [
    np.max(pareto_df[:, 0]) * 1.1,
    np.max(pareto_df[:, 1]) * 1.1
]

# === Tính HV và in kết quả ===
hv = compute_hv_2d(pareto_df, ref_point)
print(f"Hypervolume (TC_Mean vs WT_Mean): {hv:.4f}")
