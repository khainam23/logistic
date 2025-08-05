import matplotlib.pyplot as plt
import numpy as np

# Tạo dữ liệu minh họa giống biểu đồ SA trong ảnh
x = np.linspace(0, 10, 1000)
y = (
    np.sin(1.5 * x) * np.exp(-0.1 * x) * 10
    + 0.2 * np.cos(5 * x)
    - 0.1 * x
)

# Vẽ biểu đồ mô phỏng Simulated Annealing
plt.figure(figsize=(10, 6))
plt.plot(x, y, color='black', linewidth=2)

# Các điểm minh họa SA nhảy từ local minima sang điểm khác
points_x = [1, 2.3, 3.7, 4.3, 5.7]
points_y = np.interp(points_x, x, y)
plt.plot(points_x, points_y, 'o', color='blue')

# Các mũi tên thể hiện nhảy trạng thái
arrow_params = dict(facecolor='blue', shrink=0.05, width=2, headwidth=8)

# Vẽ mũi tên thể hiện SA nhảy khỏi local minima
for i in range(len(points_x) - 1):
    plt.annotate('', xy=(points_x[i + 1], points_y[i + 1]),
                 xytext=(points_x[i], points_y[i]),
                 arrowprops=arrow_params)

# Tô điểm global minimum
global_min_x = x[np.argmin(y)]
global_min_y = np.min(y)
plt.plot(global_min_x, global_min_y, 'o', color='red')

# Ẩn trục và chú thích như yêu cầu
plt.xticks([])
plt.yticks([])
plt.box(False)

plt.tight_layout()
plt.show()
