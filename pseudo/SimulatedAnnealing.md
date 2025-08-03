# Mã Giả Thuật Toán Simulated Annealing (SA) - Tóm Tắt

## Tham Số
```
INITIAL_TEMPERATURE = 100.0
COOLING_RATE = 0.95
FINAL_TEMPERATURE = 0.1
MAX_ITERATIONS = 1000
```

## Logic Chính

```pseudocode
ALGORITHM SimulatedAnnealing
BEGIN
    // 1. KHỞI TẠO
    currentSolution ← initialSolution
    bestSolution ← currentSolution.copy()
    temperature ← INITIAL_TEMPERATURE
    
    // 2. VÒNG LẶP CHÍNH
    WHILE temperature > FINAL_TEMPERATURE DO
        FOR i ← 0 TO MAX_ITERATIONS-1 DO
            // Tạo giải pháp mới
            newSolution ← perturbSolution(currentSolution.copy())
            
            // Tính năng lượng
            currentEnergy ← calculateEnergy(currentSolution)
            newEnergy ← calculateEnergy(newSolution)
            deltaEnergy ← newEnergy - currentEnergy
            
            // Chấp nhận giải pháp mới
            IF deltaEnergy < 0 OR exp(-deltaEnergy/temperature) > random() THEN
                currentSolution ← newSolution
                IF newEnergy < bestEnergy THEN
                    bestSolution ← newSolution
                END IF
            END IF
        END FOR
        
        // Giảm nhiệt độ
        temperature ← temperature × COOLING_RATE
    END WHILE
    
    RETURN bestSolution
END
```

## Biến Đổi Giải Pháp

```pseudocode
FUNCTION perturbSolution(solution)
BEGIN
    IF random() < 0.5 AND routes.length ≥ 2 THEN
        // Toán tử đa tuyến: PD-Shift hoặc PD-Exchange
        applyMultiRouteOperation(routes)
    ELSE
        // Toán tử đơn tuyến: Swap, Insert, hoặc Reverse
        applyRandomOperation(selectedRoute)
    END IF
    
    // Kiểm tra tính khả thi và khôi phục nếu cần
    validateAndRestore(routes)
    
    RETURN solution
END
```

## Nguyên Lý Hoạt Động

1. **Nhiệt độ cao** → Chấp nhận nhiều giải pháp tệ → **Exploration**
2. **Nhiệt độ giảm dần** → Giảm khả năng chấp nhận giải pháp tệ
3. **Nhiệt độ thấp** → Chỉ chấp nhận giải pháp tốt → **Exploitation**

**Xác suất chấp nhận:** P = e^(-ΔE/T)
- ΔE < 0: Luôn chấp nhận (giải pháp tốt hơn)
- ΔE > 0: Chấp nhận với xác suất giảm dần theo nhiệt độ

