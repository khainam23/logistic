# Mã Giả Thuật Toán Whale Optimization Algorithm (WOA) - Tóm Tắt

## Tham Số
```
MAX_ITERATIONS = 100
b = 1           // Hằng số xoắn ốc logarit
bestWhale       // Cá voi tốt nhất
```

## Logic Chính

```pseudocode
ALGORITHM WhaleOptimizationAlgorithm
BEGIN
    // 1. KHỞI TẠO
    initializePopulation(initialSolutions)
    updateBestWhale()
    
    // 2. VÒNG LẶP CHÍNH
    FOR iteration ← 0 TO MAX_ITERATIONS-1 DO
        // Hệ số a giảm tuyến tính từ 2 về 0
        a ← 2 × (1 - iteration / MAX_ITERATIONS)
        
        // Cập nhật vị trí từng cá voi
        FOR EACH whale IN population DO
            updateWhalePosition(whale, a, bestWhale)
        END FOR
        
        updateBestWhale()
    END FOR
    
    RETURN bestWhale.solution
END
```

## Cập Nhật Vị Trí Cá Voi

```pseudocode
FUNCTION updateWhalePosition(whale, a, bestWhale)
BEGIN
    p ← random()  // Xác suất chọn hành vi
    
    IF p < 0.5 THEN
        // Bao vây con mồi hoặc tìm kiếm
        A ← 2 × a × random() - a
        C ← 2 × random()
        
        IF |A| < 1 THEN
            // Exploitation: bao vây con mồi
            D ← |C × bestWhale.position - whale.position|
            whale.position ← bestWhale.position - A × D
        ELSE
            // Exploration: tìm kiếm con mồi
            whale.position ← randomPosition()
        END IF
    ELSE
        // Tấn công bong bóng (spiral movement)
        D ← |bestWhale.position - whale.position|
        l ← random(-1, 1)
        whale.position ← D × e^(b×l) × cos(2πl) + bestWhale.position
    END IF
END
```

## Nguyên Lý Hoạt Động

1. **Ba hành vi chính** → Bao vây (p<0.5, |A|<1), Tấn công xoắn ốc (p≥0.5), Tìm kiếm (p<0.5, |A|≥1)
2. **Hệ số a** → Giảm tuyến tính từ 2 về 0 theo iteration
3. **Vector A, C** → A = 2ar - a, C = 2r
4. **Chuyển động xoắn ốc** → Mô phỏng hành vi săn mồi của cá voi

**Công thức WOA:**
- Bao vây: X_new = X_best - A × |C × X_best - X|
- Xoắn ốc: X_new = D × e^(bl) × cos(2πl) + X_best