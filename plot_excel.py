import pandas as pd
import matplotlib.pyplot as plt
import seaborn as sns

# Đọc file Excel với dòng tiêu đề ở hàng thứ 2 (index=1)
file_path = "vrptw.xlsx"  # Đổi đường dẫn nếu cần
df = pd.read_excel(file_path, sheet_name="Sheet1", header=1)

# Lấy đúng các cột cần thiết
df_filtered = df[['Unnamed: 0', 'Unnamed: 1', 'Mean.1']].dropna()
df_filtered.columns = ['Instance', 'Algorithm', 'TotalCostMean']

# Vẽ biểu đồ cột nhóm
plt.figure(figsize=(14, 6))
sns.barplot(data=df_filtered, x="Instance", y="TotalCostMean", hue="Algorithm")

plt.title("So sánh tổng chi phí trung bình theo thuật toán cho từng instance")
plt.ylabel("Total Cost (Mean)")
plt.xlabel("Instance")
plt.xticks(rotation=45)
plt.tight_layout()
plt.show()
