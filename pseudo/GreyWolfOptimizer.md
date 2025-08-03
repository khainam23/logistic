# Mã Giả Thuật Toán Grey Wolf Optimizer (GWO) - Tóm Tắt

## Tham Số
```
MAX_ITERATIONS = 100
alpha    // Sói tốt nhất
beta     // Sói tốt thứ hai  
delta    // Sói tốt thứ ba
```

## Logic Chính

```pseudocode
ALGORITHM GreyWolfOptimizer
BEGIN
    // 1. KHỞI TẠO
    initializePopulation(initialSolutions)
    updateLeaders()  // Xác định alpha, beta, delta
    
    // 2. VÒNG LẶP CHÍNH
    FOR iteration ← 0 TO MAX_ITERATIONS-1 DO
        // Hệ số a giảm tuyến tính từ 2 về 0
        a ← 2 - iteration × (2.0 / MAX_ITERATIONS)
        
        // Cập nhật vị trí từng sói
        FOR EACH wolf IN population DO
            updateWolfPosition(wolf, a, alpha, beta, delta)
        END FOR
        
        updateLeaders()
    END FOR
    
    RETURN alpha.solution
END
```

## Cập Nhật Vị Trí Sói

```pseudocode
FUNCTION updateWolfPosition(wolf, a, alpha, beta, delta)
BEGIN
    // Tính vector A và C
    A1 ← 2 × a × random() - a
    A2 ← 2 × a × random() - a  
    A3 ← 2 × a × random() - a
    
    C1 ← 2 × random()
    C2 ← 2 × random()
    C3 ← 2 × random()
    
    // Tính khoảng cách đến các sói lãnh đạo
    D_alpha ← |C1 × alpha.position - wolf.position|
    D_beta ← |C2 × beta.position - wolf.position|
    D_delta ← |C3 × delta.position - wolf.position|
    
    // Cập nhật vị trí
    X1 ← alpha.position - A1 × D_alpha
    X2 ← beta.position - A2 × D_beta
    X3 ← delta.position - A3 × D_delta
    
    wolf.position ← (X1 + X2 + X3) / 3
END
```

## Nguyên Lý Hoạt Động

1. **Thứ bậc sói** → Alpha (tốt nhất), Beta (tốt thứ 2), Delta (tốt thứ 3)
2. **Cập nhật vị trí** → Dựa trên 3 sói lãnh đạo: X = (X1 + X2 + X3) / 3
3. **Hệ số a** → Giảm tuyến tính từ 2 về 0 theo iteration
4. **Vector A, C** → A = 2ar₁ - a, C = 2r₂

**Công thức GWO:** 
- D = |C × X_leader - X|
- X_new = X_leader - A × D