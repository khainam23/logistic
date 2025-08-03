# Mã Giả Thuật Toán Ant Colony Optimization (ACO) - Tóm Tắt

## Tham Số
```
MAX_ITERATIONS = 100
COLONY_SIZE = 20
ALPHA = 1.0          // Ảnh hưởng pheromone
BETA = 2.0           // Ảnh hưởng heuristic
RHO = 0.1            // Tốc độ bay hơi
Q = 100.0            // Hằng số pheromone
```

## Logic Chính

```pseudocode
ALGORITHM AntColonyOptimization
BEGIN
    // 1. KHỞI TẠO
    initializePheromoneMatrix()
    initializeAntColony(initialSolutions)
    
    // 2. VÒNG LẶP CHÍNH
    FOR iteration ← 0 TO MAX_ITERATIONS-1 DO
        // Cải thiện giải pháp từng kiến
        FOR EACH ant IN colony DO
            improveAntSolution(ant)
        END FOR
        
        // Cập nhật pheromone
        updatePheromone()
    END FOR
    
    RETURN bestAnt.solution
END
```

## Cải Thiện Giải Pháp

```pseudocode
FUNCTION improveAntSolution(ant)
BEGIN
    FOR EACH route IN ant.routes DO
        IF random() < 0.8 THEN
            // Xây dựng lại tuyến dựa trên pheromone
            constructRouteWithPheromone(route)
        ELSE
            // Áp dụng toán tử ngẫu nhiên
            applyRandomOperation(route)
        END IF
    END FOR
    
    // Cập nhật nếu tốt hơn
    IF newFitness < ant.fitness THEN
        ant.solution ← newSolution
        updateBestAnt(ant)
    END IF
END
```

## Cập Nhật Pheromone

```pseudocode
FUNCTION updatePheromone()
BEGIN
    // Bay hơi pheromone
    FOR i ← 0 TO numLocations-1 DO
        FOR j ← 0 TO numLocations-1 DO
            pheromone[i][j] ← pheromone[i][j] × (1.0 - RHO)
        END FOR
    END FOR
    
    // Thêm pheromone từ các kiến
    FOR EACH ant IN colony DO
        deltaPheromone ← Q / ant.fitness
        FOR EACH edge IN ant.routes DO
            pheromone[edge.from][edge.to] += deltaPheromone
        END FOR
    END FOR
END
```

## Chọn Địa Điểm Dựa Trên Pheromone

```pseudocode
FUNCTION selectNextLocationByPheromone(current, unvisited)
BEGIN
    // Tính xác suất cho từng địa điểm
    total ← 0.0
    FOR EACH next IN unvisited DO
        probability ← pheromone[current][next]^ALPHA × heuristic[current][next]^BETA
        total += probability
    END FOR
    
    // Roulette wheel selection
    rand ← random() × total
    sum ← 0.0
    FOR EACH next IN unvisited DO
        probability ← pheromone[current][next]^ALPHA × heuristic[current][next]^BETA
        sum += probability
        IF rand ≤ sum THEN
            RETURN next
        END IF
    END FOR
    
    RETURN unvisited[0]  // Fallback
END
```

## Nguyên Lý Hoạt Động

1. **Khởi tạo** → Ma trận pheromone và heuristic
2. **Vòng lặp chính** → Cải thiện từng kiến dựa trên pheromone
3. **Cập nhật pheromone** → Bay hơi + thêm pheromone từ các kiến tốt
4. **Chọn địa điểm** → Dựa trên công thức: τ^α × η^β

**Công thức ACO:** P = (pheromone^ALPHA × heuristic^BETA) / Σ(tất cả lựa chọn)