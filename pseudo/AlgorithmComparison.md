# So Sánh Các Thuật Toán Tối Ưu Hóa - Tóm Tắt

## Tổng Quan Các Thuật Toán

| Thuật Toán | Viết Tắt | Nguồn Cảm Hứng | Đặc Điểm Chính |
|------------|----------|-----------------|----------------|
| Ant Colony Optimization | ACO | Hành vi kiến | Ma trận pheromone, học từ tất cả kiến |
| Grey Wolf Optimizer | GWO | Thứ bậc sói | 3 sói lãnh đạo (α, β, δ) |
| Spotted Hyena Optimizer | SHO | Săn mồi linh cẩu | Phân cụm, 3 pha săn mồi |
| Whale Optimization Algorithm | WOA | Săn mồi cá voi | Chuyển động xoắn ốc, 3 hành vi |

## So Sánh Tham Số

| Thuật Toán | Iterations | Tham Số Đặc Biệt | Hệ Số Điều Khiển |
|------------|------------|-------------------|------------------|
| ACO | 100 | ALPHA=1, BETA=2, RHO=0.1, Q=100 | Pheromone matrix |
| GWO | 100 | - | a: 2→0 tuyến tính |
| SHO | 1000 | CLUSTER_SIZE=5 | a: 5→0 tuyến tính |
| WOA | 100 | b=1 (xoắn ốc) | a: 2→0 tuyến tính |

## So Sánh Công Thức Cốt Lõi

### ACO (Ant Colony Optimization)
```
P = (pheromone^ALPHA × heuristic^BETA) / Σ(tất cả lựa chọn)
Cập nhật pheromone: τ = (1-ρ)×τ + Δτ
```

### GWO (Grey Wolf Optimizer)
```
D = |C × X_leader - X|
X_new = X_leader - A × D
X_final = (X_α + X_β + X_δ) / 3
```

### SHO (Spotted Hyena Optimizer)
```
D = |B × X_best - X|
X_new = X_best - E × D
Phân pha: B > 1 (exploration), B ≤ 1 (exploitation)
```

### WOA (Whale Optimization Algorithm)
```
Bao vây: X_new = X_best - A × |C × X_best - X|
Xoắn ốc: X_new = D × e^(bl) × cos(2πl) + X_best
```

## So Sánh Cơ Chế Hoạt Động

### Exploration vs Exploitation

| Thuật Toán | Exploration | Exploitation | Cân Bằng |
|------------|-------------|--------------|----------|
| ACO | 20% random operators | 80% pheromone-based | Pheromone matrix |
| GWO | \|A\| ≥ 1 | \|A\| < 1 | Tham số a giảm |
| SHO | B > 1 | B ≤ 1, \|E\| < 1 | Tham số a giảm |
| WOA | \|A\| ≥ 1 | \|A\| < 1, spiral | Tham số a + xác suất p |

### Cơ Chế Học Hỏi

| Thuật Toán | Học Từ | Cơ Chế | Đặc Điểm |
|------------|--------|--------|----------|
| ACO | Tất cả kiến | Pheromone trail | Gián tiếp, tích lũy |
| GWO | 3 sói tốt nhất | Trung bình hóa | Trực tiếp, đa hướng dẫn |
| SHO | Sói tốt nhất | Vị trí tương đối | Trực tiếp, intensity-based |
| WOA | Cá voi tốt nhất | Bao vây + xoắn ốc | Trực tiếp, đa hành vi |

## So Sánh Ưu Nhược Điểm

### Ưu Điểm

| Thuật Toán | Ưu Điểm Chính |
|------------|---------------|
| ACO | Tự thích ứng, parallel, positive feedback, robust |
| GWO | Đơn giản, hội tụ nhanh, cân bằng tốt, linh hoạt |
| SHO | Phân cụm đa dạng, học thông minh, toán tử đa tuyến |
| WOA | Đơn giản, hiệu quả, linh hoạt, hội tụ nhanh |

### Nhược Điểm

| Thuật Toán | Nhược Điểm Chính |
|------------|------------------|
| ACO | Hội tụ chậm, stagnation, parameter sensitive, memory |
| GWO | Phụ thuộc init, hội tụ sớm, thiếu đa dạng cuối |
| SHO | Phức tạp hơn, nhiều tham số, clustering overhead |
| WOA | Phụ thuộc init, hội tụ sớm, tham số b cố định |

## So Sánh Hiệu Suất

### Thời Gian Hội Tụ
1. **WOA** - Nhanh nhất (100 iterations)
2. **GWO** - Nhanh (100 iterations) 
3. **ACO** - Trung bình (100 iterations)
4. **SHO** - Chậm nhất (1000 iterations)

### Chất Lượng Giải Pháp
- **ACO**: Ổn định, tích lũy kinh nghiệm tốt
- **GWO**: Cân bằng, học từ nhiều nguồn
- **SHO**: Đa dạng cao, khám phá tốt
- **WOA**: Linh hoạt, chuyển động phong phú

### Độ Phức Tạp
1. **WOA** - Đơn giản nhất
2. **GWO** - Đơn giản
3. **ACO** - Trung bình (ma trận pheromone)
4. **SHO** - Phức tạp nhất (clustering + 3 pha)

## Khuyến Nghị Sử Dụng

### Khi Nào Dùng ACO
- Bài toán có cấu trúc đồ thị rõ ràng
- Cần tích lũy kinh nghiệm dài hạn
- Có thể chạy song song
- Không gấp về thời gian

### Khi Nào Dùng GWO
- Cần cân bằng tốt exploration/exploitation
- Muốn thuật toán đơn giản, ít tham số
- Bài toán có nhiều tối ưu cục bộ
- Cần hội tụ nhanh

### Khi Nào Dùng SHO
- Cần đa dạng hóa cao
- Bài toán phức tạp, nhiều ràng buộc
- Có thời gian chạy dài
- Muốn khám phá kỹ không gian tìm kiếm

### Khi Nào Dùng WOA
- Cần thuật toán đơn giản nhất
- Bài toán có cấu trúc liên tục
- Muốn hội tụ nhanh
- Ít kinh nghiệm điều chỉnh tham số

## Kết Luận

Mỗi thuật toán có điểm mạnh riêng:
- **ACO**: Tốt cho bài toán đồ thị, tích lũy kinh nghiệm
- **GWO**: Cân bằng tốt, đa hướng dẫn
- **SHO**: Đa dạng cao, khám phá mạnh
- **WOA**: Đơn giản, linh hoạt, hội tụ nhanh

Lựa chọn phụ thuộc vào:
- Đặc điểm bài toán
- Thời gian có sẵn
- Yêu cầu chất lượng
- Kinh nghiệm điều chỉnh tham số