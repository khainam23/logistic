# Dự án Tối ưu hóa Bài toán Định tuyến Phương tiện (VRP)

## Giải thích các file Python (.py)

### 🔧 Thuật toán tối ưu hóa

#### `solve_solution_nhh_vrpspdtw.py`
- **Chức năng**: Giải bài toán VRPSPDTW bằng thuật toán Greedy với ràng buộc nghiêm ngặt
- **Đặc điểm**: 
  - Hỗ trợ định dạng file mới với header và sections
  - Xử lý thu gom và giao hàng đồng thời
  - Kiểm tra ràng buộc thời gian và tải trọng
- **Input**: File .vrpsdptw với định dạng chuẩn
- **Output**: File solution với các tuyến đường tối ưu

#### `solve_sulution_nhh.py`
- **Chức năng**: Triển khai thuật toán SHO (Spotted Hyena Optimizer)
- **Đặc điểm**:
  - Meta-heuristic algorithm lấy cảm hứng từ linh cẩu đốm
  - Tối ưu hóa đa mục tiêu
  - Xử lý các bài toán VRP phức tạp
- **Sử dụng**: Cho các bài toán quy mô lớn cần tối ưu hóa cao

#### `gready_vrpspdtw.py`
- **Chức năng**: Thuật toán tham lam cơ bản cho VRPSPDTW
- **Đặc điểm**:
  - Giải pháp nhanh cho bài toán nhỏ
  - Ưu tiên khách hàng gần nhất
  - Kiểm tra ràng buộc cơ bản

### 📊 Trực quan hóa dữ liệu

#### `visualize_routes.py`
- **Chức năng**: Tạo biểu đồ tuyến đường tĩnh 2D
- **Đặc điểm**:
  - Hiển thị depot và các điểm khách hàng
  - Vẽ các tuyến đường với màu sắc khác nhau
  - Xuất file PNG/PDF
- **Output**: Biểu đồ tuyến đường với thông tin chi tiết

#### `animated_routes.py`
- **Chức năng**: Tạo hoạt ảnh di chuyển phương tiện
- **Đặc điểm**:
  - Mô phỏng chuyển động theo thời gian thực
  - Hiển thị quá trình giao/thu hàng
  - Xuất file GIF hoặc MP4
- **Sử dụng**: Trình bày kết quả một cách sinh động

#### `visualize_pareto_front.py`
- **Chức năng**: Trực quan hóa mặt Pareto cho tối ưu đa mục tiêu
- **Đặc điểm**:
  - Biểu đồ 2D/3D cho các mục tiêu
  - Phân tích trade-off giữa các tiêu chí
  - Hỗ trợ nhiều thuật toán so sánh

#### `visualize_optimization_results.py`
- **Chức năng**: Biểu đồ kết quả tối ưu hóa
- **Đặc điểm**:
  - Convergence curves
  - So sánh hiệu suất các thuật toán
  - Thống kê chi tiết

#### `visualize_nv_tc_results.py`
- **Chức năng**: Trực quan hóa kết quả số phương tiện (NV) và tổng chi phí (TC)
- **Sử dụng**: Phân tích trade-off giữa số xe và chi phí

#### `visualize_sa_results.py`
- **Chức năng**: Trực quan hóa kết quả thuật toán Simulated Annealing
- **Đặc điểm**: Theo dõi quá trình làm lạnh và tìm kiếm

#### `visualize_single_dataset.py`
- **Chức năng**: Trực quan hóa một dataset cụ thể
- **Sử dụng**: Phân tích chi tiết một bài toán

#### `visualize_solution_data.py`
- **Chức năng**: Trực quan hóa dữ liệu solution tổng quát
- **Đặc điểm**: Hỗ trợ nhiều định dạng file solution

### 📈 Phân tích và đánh giá

#### `analyze_all_problems.py`
- **Chức năng**: Phân tích toàn diện 4 loại bài toán VRP
- **Đặc điểm**:
  - PDPTW, VRPTW, VRPSPDTW_Wang_Chen, VRPSPDTW_Liu_Tang_Yao
  - Thống kê chi tiết từng loại bài toán
  - Tạo báo cáo tổng hợp
- **Output**: File Excel với phân tích đầy đủ

#### `evaluate_rl_epochs.py`
- **Chức năng**: Đánh giá các epoch của thuật toán học tăng cường
- **Đặc điểm**:
  - Theo dõi quá trình học
  - So sánh hiệu suất qua các epoch
  - Phân tích convergence

#### `process_epochs.py`
- **Chức năng**: Xử lý và làm sạch dữ liệu epoch
- **Sử dụng**: Tiền xử lý cho việc phân tích

#### `check_available_data.py`
- **Chức năng**: Kiểm tra tính toàn vẹn dữ liệu
- **Đặc điểm**:
  - Quét tất cả thư mục dữ liệu
  - Báo cáo file thiếu hoặc lỗi
  - Thống kê số lượng bài toán

### 🌐 Thu thập và xử lý dữ liệu

#### `crawl-solution.py`
- **Chức năng**: Thu thập dữ liệu solution từ web
- **Đặc điểm**:
  - Sử dụng BeautifulSoup
  - Tự động download solution files
  - Xử lý nhiều nguồn dữ liệu

#### `hv_from_excel.py`
- **Chức năng**: Xử lý dữ liệu hypervolume từ file Excel
- **Sử dụng**: Phân tích hiệu suất thuật toán đa mục tiêu

#### `remove_zero.py`
- **Chức năng**: Loại bỏ các route rỗng hoặc không hợp lệ
- **Sử dụng**: Làm sạch dữ liệu solution

## Giải thích các file HTML (.html)

### 🌐 Trực quan hóa web

#### `interactive_routes.html`
- **Chức năng**: Giao diện web tương tác để xem tuyến đường
- **Đặc điểm**:
  - Zoom, pan, click để xem chi tiết
  - Hiển thị thông tin khách hàng khi hover
  - Toggle hiển thị/ẩn các tuyến
  - Responsive design
- **Công nghệ**: HTML5, CSS3, JavaScript, D3.js hoặc Leaflet
- **Sử dụng**: Mở trực tiếp trong trình duyệt

#### `animated_routes.html`
- **Chức năng**: Hoạt ảnh web cho chuyển động phương tiện
- **Đặc điểm**:
  - Animation timeline với controls
  - Play/pause/speed control
  - Hiển thị thời gian thực
  - Thông tin real-time về tải trọng
- **Công nghệ**: HTML5 Canvas hoặc SVG animation
- **Sử dụng**: Demo và presentation

## Cách sử dụng

### Chạy thuật toán
```bash
python solve_solution_nhh_vrpspdtw.py
python solve_sulution_nhh.py
python gready_vrpspdtw.py
```

### Trực quan hóa
```bash
python visualize_routes.py
python animated_routes.py
python visualize_optimization_results.py
```

### Phân tích
```bash
python analyze_all_problems.py
python evaluate_rl_epochs.py
python check_available_data.py
```

### Xem kết quả web
- Mở `interactive_routes.html` trong trình duyệt
- Mở `animated_routes.html` để xem hoạt ảnh