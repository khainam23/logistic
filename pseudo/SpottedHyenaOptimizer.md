# Mã Giả Thuật Toán Spotted Hyena Optimizer (SHO) - Tóm Tắt

## Tham Số
```
MAX_ITERATIONS = 1000
CLUSTER_SIZE = 5
bestHyena    // Linh cẩu tốt nhất
```

## Logic Chính

```pseudocode
ALGORITHM SpottedHyenaOptimizer
BEGIN
    // 1. KHỞI TẠO
    initializePopulation(initialSolutions)
    formClusters()  // Tạo cụm linh cẩu
    
    // 2. VÒNG LẶP CHÍNH
    FOR iteration ← 0 TO MAX_ITERATIONS-1 DO
        // Hệ số a giảm tuyến tính từ 5 về 0
        a ← 5 × (1 - iteration / MAX_ITERATIONS)
        
        // Cập nhật vị trí từng linh cẩu
        FOR EACH hyena IN population DO
            updateHyenaPosition(hyena, a, bestHyena)
        END FOR
        
        updateBestHyena()
    END FOR
    
    RETURN bestHyena.solution
END
```

## Cập Nhật Vị Trí Linh Cẩu

```pseudocode
FUNCTION updateHyenaPosition(hyena, a, bestHyena)
BEGIN
    // Tính vector B và E
    B ← 2 × random()
    E ← 2 × a × random() - a
    
    // Tính khoảng cách đến con mồi tốt nhất
    D ← |B × bestHyena.position - hyena.position|
    
    // Cập nhật vị trí
    IF B > 1 THEN
        // Exploration: tìm kiếm rộng
        hyena.position ← randomPosition()
    ELSE
        // Exploitation: khai thác vùng tốt
        hyena.position ← bestHyena.position - E × D
    END IF
END
```

## Nguyên Lý Hoạt Động

1. **Phân cụm** → Chia quần thể thành các cụm nhỏ (CLUSTER_SIZE = 5)
2. **Ba pha săn mồi** → Tìm kiếm (B > 1), Bao vây & Tấn công (B ≤ 1)
3. **Hệ số a** → Giảm tuyến tính từ 5 về 0 theo iteration
4. **Vector B, E** → B = 2×random(), E = 2ar - a

**Công thức SHO:**
- D = |B × X_best - X|
- X_new = X_best - E × D