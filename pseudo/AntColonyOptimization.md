# Ant Colony Optimization (ACO) - Đặc Trưng Cốt Lõi

## Tham Số Chính
```
ALPHA = 1.0    // Trọng số pheromone (vết kiến)
BETA = 2.0     // Trọng số heuristic (khoảng cách)
RHO = 0.1      // Tốc độ bay hơi pheromone
```

## Thuật Toán Chính

```pseudocode
ALGORITHM ACO
BEGIN
    initializePheromone()
    
    FOR iteration ← 1 TO MAX_ITERATIONS DO
        // 1. MỖI KIẾN XÂY DỰNG GIẢI PHÁP
        FOR EACH ant DO
            constructSolution(ant) // Dựa trên pheromone + heuristic
        END FOR
        
        // 2. CẬP NHẬT PHEROMONE
        evaporatePheromone()     // Bay hơi: τ ← τ × (1-ρ)
        depositPheromone()       // Tăng cường: τ ← τ + Δτ
    END FOR
END
```

## Đặc Trưng 1: Xây Dựng Giải Pháp Theo Xác Suất

```pseudocode
FUNCTION constructSolution(ant)
BEGIN
    WHILE hasUnvisitedNodes() DO
        next ← selectNextNode()  // Chọn theo xác suất ACO
        addToRoute(next)
    END WHILE
END

FUNCTION selectNextNode()
BEGIN
    // CÔNG THỨC ACO CỐT LÕI
    FOR EACH unvisited node j DO
        probability[j] = (pheromone[i][j]^ALPHA × heuristic[i][j]^BETA) / totalSum
    END FOR
    
    RETURN rouletteWheelSelection(probability)
END
```

## Đặc Trưng 2: Cập Nhật Pheromone Hai Pha

```pseudocode
FUNCTION updatePheromone()
BEGIN
    // PHA 1: BAY HƠI (Quên dần giải pháp cũ)
    FOR ALL edges (i,j) DO
        pheromone[i][j] ← pheromone[i][j] × (1 - RHO)
    END FOR
    
    // PHA 2: TĂNG CƯỜNG (Nhớ giải pháp tốt)
    FOR EACH ant DO
        delta ← Q / ant.fitness  // Kiến tốt → nhiều pheromone hơn
        FOR EACH edge IN ant.path DO
            pheromone[edge] ← pheromone[edge] + delta
        END FOR
    END FOR
END
```

## Bản Chất ACO

**🐜 Mô phỏng hành vi kiến thật:**
- Kiến để lại vết pheromone khi đi
- Kiến khác theo vết này với xác suất cao
- Đường ngắn → nhiều kiến đi → nhiều pheromone → thu hút thêm kiến

**📊 Công thức xác suất cốt lõi:**
```
P(i→j) = (τᵢⱼᵅ × ηᵢⱼᵝ) / Σ(τᵢₖᵅ × ηᵢₖᵝ)
```
- τ = pheromone (kinh nghiệm tập thể)
- η = heuristic (tri thức cá nhân)
- α, β = cân bằng giữa hai yếu tố