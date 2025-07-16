# Mã Giả Thuật Toán Ant Colony Optimization (ACO)

## Tổng Quan
Thuật toán Ant Colony Optimization (ACO) mô phỏng hành vi tìm kiếm thức ăn của đàn kiến trong tự nhiên. Thuật toán này dựa trên việc sử dụng pheromone (chất thông tin hóa học) để giao tiếp gián tiếp giữa các kiến và tìm ra đường đi tối ưu. ACO được áp dụng để giải quyết bài toán Vehicle Routing Problem with Time Windows (VRPTW) và Pickup and Delivery Problem with Time Windows (PDPTW) thông qua việc xây dựng các tuyến đường dựa trên ma trận pheromone và thông tin heuristic.

## Tham Số Thuật Toán

```
MAX_ITERATIONS = 100             // Số vòng lặp tối đa
COLONY_SIZE = 20                 // Số lượng kiến trong đàn
ALPHA = 1.0                      // Hệ số ảnh hưởng của pheromone
BETA = 2.0                       // Hệ số ảnh hưởng của heuristic (khoảng cách)
RHO = 0.1                        // Tốc độ bay hơi pheromone
Q = 100.0                        // Hằng số chất lượng pheromone

colony[]                         // Danh sách các kiến
bestAnt                          // Kiến có fitness tốt nhất
pheromone[][]                    // Ma trận pheromone
heuristic[][]                    // Ma trận heuristic (khoảng cách nghịch đảo)
numLocations                     // Số lượng địa điểm
```

## Thuật Toán Chính

```pseudocode
ALGORITHM AntColonyOptimization
INPUT: initialSolutions[], fitnessUtil, checkConditionUtil, locations[], currentTarget
OUTPUT: bestSolution

BEGIN
    // 1. KHỞI TẠO
    CALL setupParameters(fitnessUtil, checkConditionUtil, locations, currentTarget)
    CALL initialize(initialSolutions)
    
    // 2. VÒNG LẶP CHÍNH
    FOR iteration ← 0 TO MAX_ITERATIONS-1 DO
        // Cập nhật giải pháp cho từng kiến
        FOR EACH ant IN colony DO
            CALL updateAntSolution(ant)
        END FOR
        
        // Cập nhật pheromone
        CALL updatePheromone()
        
        // Đa dạng hóa đàn kiến định kỳ
        IF iteration MOD 10 = 0 THEN
            CALL diversifyColony()
        END IF
    END FOR
    
    RETURN bestAnt.solution
END
```

## Khởi Tạo Đàn Kiến

```pseudocode
FUNCTION initialize(initialSolutions[])
BEGIN
    colony ← []
    numLocations ← locations.length
    
    // Khởi tạo ma trận pheromone và heuristic
    CALL initializeMatrices()
    
    // Khởi tạo đàn kiến từ các giải pháp ban đầu
    FOR EACH solution IN initialSolutions DO
        ant ← NEW Ant(solution.copy(), solution.fitness)
        ADD ant TO colony
        
        // Cập nhật kiến tốt nhất
        IF bestAnt = NULL OR ant.fitness < bestAnt.fitness THEN
            bestAnt ← NEW Ant(solution.copy(), solution.fitness)
        END IF
    END FOR
    
    // Thêm kiến mới nếu cần để đạt đủ kích thước đàn
    WHILE colony.size() < COLONY_SIZE DO
        newSolution ← createRandomSolution(initialSolutions[0])
        newFitness ← fitnessUtil.calculatorFitness(newSolution.routes, locations)
        newSolution.fitness ← newFitness
        
        ant ← NEW Ant(newSolution, newFitness)
        ADD ant TO colony
        
        // Cập nhật kiến tốt nhất
        IF ant.fitness < bestAnt.fitness THEN
            bestAnt ← NEW Ant(newSolution.copy(), newFitness)
        END IF
    END WHILE
END
```

## Khởi Tạo Ma Trận Pheromone và Heuristic

```pseudocode
FUNCTION initializeMatrices()
BEGIN
    pheromone ← NEW MATRIX[numLocations][numLocations]
    heuristic ← NEW MATRIX[numLocations][numLocations]
    
    // Khởi tạo pheromone với giá trị nhỏ đồng đều
    FOR i ← 0 TO numLocations-1 DO
        FOR j ← 0 TO numLocations-1 DO
            IF i ≠ j THEN
                pheromone[i][j] ← 0.1
                
                // Heuristic là nghịch đảo của khoảng cách
                distance ← locations[i].distance(locations[j])
                heuristic[i][j] ← distance > 0 ? 1.0 / distance : 0.0
            END IF
        END FOR
    END FOR
END
```

## Tạo Giải Pháp Ngẫu Nhiên

```pseudocode
FUNCTION createRandomSolution(template)
BEGIN
    newSolution ← template.copy()
    routes[] ← newSolution.routes
    
    // Áp dụng các toán tử đơn tuyến
    FOR EACH route IN routes DO
        operations ← 1 + RANDOM(0, 1)
        FOR i ← 0 TO operations-1 DO
            CALL applyRandomOperation(route)
        END FOR
    END FOR
    
    // Áp dụng các toán tử đa tuyến nếu có đủ tuyến đường
    IF routes.length ≥ 2 THEN
        multiRouteOperations ← 1 + RANDOM(0, 1)
        FOR i ← 0 TO multiRouteOperations-1 DO
            CALL applyRandomMultiRouteOperation(routes)
        END FOR
    END IF
    
    // Cập nhật khoảng cách cho tất cả các tuyến đường
    FOR EACH route IN routes DO
        route.calculateDistance(locations)
    END FOR
    
    RETURN newSolution
END
```

## Cập Nhật Giải Pháp Của Kiến (Core Algorithm)

```pseudocode
FUNCTION updateAntSolution(ant)
BEGIN
    currentSolution ← ant.solution
    newSolution ← currentSolution.copy()
    routes[] ← newSolution.routes
    
    // Cập nhật từng tuyến đường
    FOR routeIndex ← 0 TO routes.length-1 DO
        route ← routes[routeIndex]
        
        // Tạo một tuyến đường mới dựa trên pheromone và heuristic
        IF random() < 0.7 THEN  // 70% cơ hội áp dụng ACO
            CALL constructNewRoute(route)
        ELSE
            // 30% cơ hội áp dụng toán tử ngẫu nhiên
            CALL applyRandomOperation(route)
        END IF
        
        // Kiểm tra tính khả thi
        IF NOT checkConditionUtil.isInsertionFeasible(route, locations,
                                                     route.maxPayload, currentTarget) THEN
            // Nếu không khả thi, quay lại tuyến đường cũ
            routes[routeIndex] ← currentSolution.routes[routeIndex].copy()
        END IF
    END FOR
    
    // Áp dụng các toán tử đa tuyến với xác suất 20%
    IF random() < 0.2 AND routes.length ≥ 2 THEN
        CALL applyRandomMultiRouteOperation(routes)
        
        // Kiểm tra tính khả thi sau khi áp dụng toán tử đa tuyến
        FOR i ← 0 TO routes.length-1 DO
            IF NOT checkConditionUtil.isInsertionFeasible(routes[i], locations,
                                                         routes[i].maxPayload, currentTarget) THEN
                routes[i] ← currentSolution.routes[i].copy()
            END IF
        END FOR
    END IF
    
    // Cập nhật khoảng cách cho tất cả các tuyến đường
    FOR EACH route IN routes DO
        route.calculateDistance(locations)
    END FOR
    
    // Tính toán fitness mới
    newFitness ← fitnessUtil.calculatorFitness(routes, locations)
    newSolution.fitness ← newFitness
    
    // Cập nhật nếu tốt hơn
    IF newFitness < ant.fitness THEN
        ant.solution ← newSolution
        ant.fitness ← newFitness
        
        // Cập nhật kiến tốt nhất
        IF newFitness < bestAnt.fitness THEN
            bestAnt ← NEW Ant(newSolution.copy(), newFitness)
            PRINT "New best solution found with fitness: " + newFitness
        END IF
    END IF
END
```

## Xây Dựng Tuyến Đường Mới Dựa Trên Pheromone và Heuristic

```pseudocode
FUNCTION constructNewRoute(route)
BEGIN
    way[] ← route.getIndLocations()
    IF way.length ≤ 2 THEN RETURN  // Không đủ điểm để xây dựng lại
    
    // Giữ điểm đầu và điểm cuối (thường là depot)
    start ← way[0]
    end ← way[way.length - 1]
    
    // Tạo danh sách các điểm cần thăm (không bao gồm điểm đầu và cuối)
    unvisited ← NEW LIST()
    FOR i ← 1 TO way.length-2 DO
        ADD way[i] TO unvisited
    END FOR
    
    // Xây dựng tuyến đường mới
    newWay ← NEW LIST()
    ADD start TO newWay
    
    current ← start
    WHILE unvisited.size() > 0 DO
        next ← selectNextLocation(current, unvisited)
        ADD next TO newWay
        current ← next
        REMOVE next FROM unvisited
    END WHILE
    
    ADD end TO newWay
    
    // Cập nhật tuyến đường
    FOR i ← 0 TO way.length-1 DO
        way[i] ← newWay[i]
    END FOR
END
```

## Chọn Địa Điểm Tiếp Theo Dựa Trên Pheromone và Heuristic

```pseudocode
FUNCTION selectNextLocation(current, unvisited)
BEGIN
    IF unvisited.size() = 0 THEN RETURN -1
    IF unvisited.size() = 1 THEN RETURN unvisited[0]
    
    // Tính tổng xác suất
    total ← 0.0
    probabilities[] ← NEW ARRAY[unvisited.size()]
    
    FOR i ← 0 TO unvisited.size()-1 DO
        next ← unvisited[i]
        pheromoneValue ← pheromone[current][next]
        heuristicValue ← heuristic[current][next]
        
        // Công thức ACO: τ^α × η^β
        probabilities[i] ← POWER(pheromoneValue, ALPHA) × POWER(heuristicValue, BETA)
        total ← total + probabilities[i]
    END FOR
    
    // Chọn địa điểm tiếp theo theo xác suất (Roulette Wheel Selection)
    rand ← random() × total
    sum ← 0.0
    
    FOR i ← 0 TO unvisited.size()-1 DO
        sum ← sum + probabilities[i]
        IF rand ≤ sum THEN
            RETURN unvisited[i]
        END IF
    END FOR
    
    // Mặc định trả về địa điểm cuối cùng
    RETURN unvisited[unvisited.size() - 1]
END
```

## Cập Nhật Pheromone

```pseudocode
FUNCTION updatePheromone()
BEGIN
    // GIAI ĐOẠN 1: Giảm pheromone trên tất cả các cạnh (bay hơi)
    FOR i ← 0 TO numLocations-1 DO
        FOR j ← 0 TO numLocations-1 DO
            IF i ≠ j THEN
                pheromone[i][j] ← pheromone[i][j] × (1.0 - RHO)
                IF pheromone[i][j] < 0.1 THEN
                    pheromone[i][j] ← 0.1  // Giới hạn dưới
                END IF
            END IF
        END FOR
    END FOR
    
    // GIAI ĐOẠN 2: Thêm pheromone cho các cạnh trong giải pháp tốt nhất
    bestSolution ← bestAnt.solution
    deltaPheromone ← Q / bestAnt.fitness
    
    FOR EACH route IN bestSolution.routes DO
        way[] ← route.getIndLocations()
        FOR i ← 0 TO way.length-2 DO
            from ← way[i]
            to ← way[i + 1]
            
            // Thêm pheromone cho cả hai hướng (đồ thị vô hướng)
            pheromone[from][to] ← pheromone[from][to] + deltaPheromone
            pheromone[to][from] ← pheromone[to][from] + deltaPheromone
            
            // Giới hạn trên để tránh stagnation
            IF pheromone[from][to] > 10.0 THEN
                pheromone[from][to] ← 10.0
            END IF
            IF pheromone[to][from] > 10.0 THEN
                pheromone[to][from] ← 10.0
            END IF
        END FOR
    END FOR
END
```

## Đa Dạng Hóa Đàn Kiến

```pseudocode
FUNCTION diversifyColony()
BEGIN
    // Sắp xếp đàn kiến theo fitness
    SORT colony BY fitness
    
    // Giữ lại 30% kiến tốt nhất
    eliteCount ← ROUND(COLONY_SIZE × 0.3)
    
    // Tạo kiến mới cho 70% còn lại
    FOR i ← eliteCount TO COLONY_SIZE-1 DO
        IF i < colony.size() THEN
            // Tạo giải pháp mới từ giải pháp tốt nhất
            newSolution ← createDiversifiedSolution(bestAnt.solution)
            newFitness ← fitnessUtil.calculatorFitness(newSolution.routes, locations)
            
            colony[i].solution ← newSolution
            colony[i].fitness ← newFitness
        END IF
    END FOR
    
    PRINT "Colony diversified. Elite count: " + eliteCount
END
```

## Tạo Giải Pháp Đa Dạng Hóa

```pseudocode
FUNCTION createDiversifiedSolution(original)
BEGIN
    newSolution ← original.copy()
    routes[] ← newSolution.routes
    
    // Áp dụng các toán tử đơn tuyến
    FOR EACH route IN routes DO
        operations ← 1 + RANDOM(0, 1)
        FOR i ← 0 TO operations-1 DO
            CALL applyRandomOperation(route)
        END FOR
    END FOR
    
    // Áp dụng các toán tử đa tuyến (PD-Shift và PD-Exchange)
    IF routes.length ≥ 2 THEN
        multiRouteOperations ← 1 + RANDOM(0, 1)
        FOR i ← 0 TO multiRouteOperations-1 DO
            CALL applyRandomMultiRouteOperation(routes)
        END FOR
    END IF
    
    // Cập nhật khoảng cách cho tất cả các tuyến đường
    FOR EACH route IN routes DO
        route.calculateDistance(locations)
    END FOR
    
    newSolution.fitness ← fitnessUtil.calculatorFitness(routes, locations)
    RETURN newSolution
END
```

## Các Toán Tử Hỗ Trợ

### Toán Tử Ngẫu Nhiên Đơn Tuyến

```pseudocode
FUNCTION applyRandomOperation(route)
BEGIN
    operator ← RANDOM(0, 2)
    SWITCH operator
        CASE 0: CALL applySwapOperator(route)      // Hoán đổi 2 điểm
        CASE 1: CALL applyInsertOperator(route)    // Chèn điểm
        CASE 2: CALL applyReverseOperator(route)   // Đảo ngược đoạn
    END SWITCH
END
```

### Toán Tử Ngẫu Nhiên Đa Tuyến

```pseudocode
FUNCTION applyRandomMultiRouteOperation(routes[])
BEGIN
    IF routes.length < 2 THEN RETURN
    
    operator ← RANDOM(0, 1)
    SWITCH operator
        CASE 0: CALL applyPdShift(routes)      // Di chuyển điểm giữa các tuyến
        CASE 1: CALL applyPdExchange(routes)   // Trao đổi điểm giữa các tuyến
    END SWITCH
END
```

### Toán Tử Hoán Đổi (Swap)

```pseudocode
FUNCTION applySwapOperator(route)
BEGIN
    way[] ← route.getIndLocations()
    IF way.length < 2 THEN RETURN
    
    // Chọn hai vị trí ngẫu nhiên khác nhau
    pos1 ← RANDOM(0, way.length-1)
    DO
        pos2 ← RANDOM(0, way.length-1)
    WHILE pos1 = pos2
    
    // Hoán đổi hai điểm
    temp ← way[pos1]
    way[pos1] ← way[pos2]
    way[pos2] ← temp
    
    // Đảm bảo giá trị không vượt quá giới hạn
    CALL validateLocationIndices(way)
END
```

### Toán Tử Chèn (Insert)

```pseudocode
FUNCTION applyInsertOperator(route)
BEGIN
    way[] ← route.getIndLocations()
    IF way.length < 2 THEN RETURN
    
    // Chọn vị trí nguồn và đích
    pos ← RANDOM(0, way.length-1)
    insertPos ← RANDOM(0, way.length-1)
    
    // Thực hiện chèn
    posVal ← way[MAX(insertPos, pos)]
    FOR i ← MIN(insertPos, pos) TO MAX(insertPos, pos) DO
        tempVal ← way[i]
        way[i] ← posVal
        posVal ← tempVal
    END FOR
    
    // Đảm bảo giá trị không vượt quá giới hạn
    CALL validateLocationIndices(way)
END
```

### Toán Tử Đảo Ngược (Reverse)

```pseudocode
FUNCTION applyReverseOperator(route)
BEGIN
    way[] ← route.getIndLocations()
    IF way.length < 2 THEN RETURN
    
    // Chọn hai vị trí ngẫu nhiên
    pos1 ← RANDOM(0, way.length-1)
    pos2 ← RANDOM(0, way.length-1)
    
    // Đảm bảo pos1 < pos2
    IF pos1 > pos2 THEN
        temp ← pos1
        pos1 ← pos2
        pos2 ← temp
    END IF
    
    // Đảo ngược đoạn từ pos1 đến pos2
    WHILE pos1 < pos2 DO
        temp ← way[pos1]
        way[pos1] ← way[pos2]
        way[pos2] ← temp
        pos1 ← pos1 + 1
        pos2 ← pos2 - 1
    END WHILE
    
    // Đảm bảo giá trị không vượt quá giới hạn
    CALL validateLocationIndices(way)
END
```

### Toán Tử PD-Shift

```pseudocode
FUNCTION applyPdShift(routes[])
BEGIN
    IF routes.length < 2 THEN RETURN
    
    // Chọn ngẫu nhiên 2 tuyến đường khác nhau
    routeIndex1 ← RANDOM(0, routes.length-1)
    DO
        routeIndex2 ← RANDOM(0, routes.length-1)
    WHILE routeIndex1 = routeIndex2
    
    route1 ← routes[routeIndex1]
    route2 ← routes[routeIndex2]
    way1[] ← route1.getIndLocations()
    way2[] ← route2.getIndLocations()
    
    // Kiểm tra nếu một trong hai tuyến đường không có điểm nào
    IF way1.length = 0 OR way2.length = 0 THEN RETURN
    
    // Chọn một điểm ngẫu nhiên từ tuyến đường 1 để di chuyển sang tuyến đường 2
    posToMove ← RANDOM(0, way1.length-1)
    locationToMove ← way1[posToMove]
    
    // Chọn vị trí ngẫu nhiên trên tuyến đường 2 để chèn điểm
    insertPos ← way2.length > 0 ? RANDOM(0, way2.length) : 0
    
    // Tạo mảng mới cho tuyến đường 1 (loại bỏ điểm được di chuyển)
    newWay1[] ← NEW ARRAY[way1.length - 1]
    j ← 0
    FOR i ← 0 TO way1.length-1 DO
        IF i ≠ posToMove THEN
            newWay1[j] ← way1[i]
            j ← j + 1
        END IF
    END FOR
    
    // Tạo mảng mới cho tuyến đường 2 (thêm điểm mới)
    newWay2[] ← NEW ARRAY[way2.length + 1]
    FOR i ← 0 TO insertPos-1 DO
        newWay2[i] ← way2[i]
    END FOR
    newWay2[insertPos] ← locationToMove
    FOR i ← insertPos TO way2.length-1 DO
        newWay2[i + 1] ← way2[i]
    END FOR
    
    // Cập nhật các tuyến đường
    route1.setIndLocations(newWay1)
    route2.setIndLocations(newWay2)
    
    // Đảm bảo giá trị không vượt quá giới hạn
    CALL validateLocationIndices(newWay1)
    CALL validateLocationIndices(newWay2)
    
    // Cập nhật khoảng cách
    IF locations ≠ NULL THEN
        route1.calculateDistance(locations)
        route2.calculateDistance(locations)
    END IF
END
```

### Toán Tử PD-Exchange

```pseudocode
FUNCTION applyPdExchange(routes[])
BEGIN
    IF routes.length < 2 THEN RETURN
    
    // Chọn ngẫu nhiên 2 tuyến đường khác nhau
    routeIndex1 ← RANDOM(0, routes.length-1)
    DO
        routeIndex2 ← RANDOM(0, routes.length-1)
    WHILE routeIndex1 = routeIndex2
    
    route1 ← routes[routeIndex1]
    route2 ← routes[routeIndex2]
    way1[] ← route1.getIndLocations()
    way2[] ← route2.getIndLocations()
    
    // Kiểm tra nếu một trong hai tuyến đường không có điểm nào
    IF way1.length = 0 OR way2.length = 0 THEN RETURN
    
    // Chọn một điểm ngẫu nhiên từ mỗi tuyến đường để trao đổi
    pos1 ← RANDOM(0, way1.length-1)
    pos2 ← RANDOM(0, way2.length-1)
    
    // Trao đổi hai điểm
    temp ← way1[pos1]
    way1[pos1] ← way2[pos2]
    way2[pos2] ← temp
    
    // Đảm bảo giá trị không vượt quá giới hạn
    CALL validateLocationIndices(way1)
    CALL validateLocationIndices(way2)
    
    // Cập nhật khoảng cách
    IF locations ≠ NULL THEN
        route1.calculateDistance(locations)
        route2.calculateDistance(locations)
    END IF
END
```

## Nguyên Lý Hoạt Động Của ACO

### 1. Giao Tiếp Qua Pheromone
- **Pheromone Trail:** Dấu vết hóa học để giao tiếp gián tiếp
- **Tích lũy:** Các tuyến đường tốt được tăng cường pheromone
- **Bay hơi:** Pheromone giảm dần theo thời gian để tránh stagnation

### 2. Xây Dựng Giải Pháp
- **Constructive:** Xây dựng giải pháp từng bước
- **Probabilistic:** Chọn bước tiếp theo dựa trên xác suất
- **Heuristic Information:** Kết hợp thông tin heuristic (khoảng cách)

### 3. Cập Nhật Pheromone
- **Evaporation:** Giảm pheromone trên tất cả các cạnh
- **Reinforcement:** Tăng pheromone trên các cạnh của giải pháp tốt
- **Bounds:** Giới hạn trên và dưới để tránh stagnation và premature convergence

## Đặc Điểm Nổi Bật

### 1. Ma Trận Pheromone
- **Khởi tạo:** Giá trị nhỏ đồng đều (0.1)
- **Cập nhật:** Dựa trên chất lượng giải pháp
- **Giới hạn:** [0.1, 10.0] để tránh các vấn đề hội tụ

### 2. Ma Trận Heuristic
- **Định nghĩa:** η_ij = 1/d_ij (nghịch đảo khoảng cách)
- **Mục đích:** Ưu tiên các cạnh ngắn hơn
- **Kết hợp:** Với pheromone để ra quyết định

### 3. Công Thức Xác Suất
```
P_ij = (τ_ij^α × η_ij^β) / Σ(τ_ik^α × η_ik^β)
```
Trong đó:
- τ_ij: Pheromone trên cạnh (i,j)
- η_ij: Heuristic information
- α: Tầm quan trọng của pheromone
- β: Tầm quan trọng của heuristic

### 4. Cập Nhật Pheromone
```
τ_ij ← (1-ρ) × τ_ij + Δτ_ij
Δτ_ij = Q / L_best
```
Trong đó:
- ρ: Tỷ lệ bay hơi
- Q: Hằng số chất lượng
- L_best: Độ dài tuyến đường tốt nhất

## Ưu Điểm

1. **Tự thích ứng:** Pheromone tự động điều chỉnh theo chất lượng giải pháp
2. **Parallel:** Các kiến có thể hoạt động song song
3. **Positive Feedback:** Tăng cường các tuyến đường tốt
4. **Robust:** Ít nhạy cảm với tham số so với các thuật toán khác

## Nhược Điểm

1. **Convergence Time:** Có thể mất nhiều thời gian để hội tụ
2. **Stagnation:** Có thể bị kẹt ở tối ưu cục bộ
3. **Parameter Sensitivity:** Cần điều chỉnh α, β, ρ phù hợp
4. **Memory Requirements:** Cần lưu trữ ma trận pheromone

## Công Thức Toán Học Chính

### Xác Suất Chọn Địa Điểm Tiếp Theo
```
P_ij^k = (τ_ij^α × η_ij^β) / Σ_{l∈N_i^k}(τ_il^α × η_il^β)
```

### Cập Nhật Pheromone
```
τ_ij ← (1-ρ) × τ_ij + Σ_k Δτ_ij^k
Δτ_ij^k = Q / L_k (nếu kiến k đi qua cạnh (i,j))
Δτ_ij^k = 0 (ngược lại)
```

### Heuristic Information
```
η_ij = 1 / d_ij
```

## Biến Thể ACO

### 1. Ant System (AS)
- **Đặc điểm:** Tất cả kiến đều cập nhật pheromone
- **Ưu điểm:** Đơn giản, dễ hiểu
- **Nhược điểm:** Hội tụ chậm

### 2. Elitist Ant System (EAS)
- **Đặc điểm:** Kiến tốt nhất được tăng cường thêm
- **Ưu điểm:** Hội tụ nhanh hơn AS
- **Nhược điểm:** Có thể bị stagnation

### 3. Max-Min Ant System (MMAS)
- **Đặc điểm:** Chỉ kiến tốt nhất cập nhật pheromone, có giới hạn trên/dưới
- **Ưu điểm:** Tránh stagnation hiệu quả
- **Nhược điểm:** Phức tạp hơn trong implementation

## Ứng Dụng
Thuật toán ACO đặc biệt hiệu quả cho:
- Vehicle Routing Problem with Time Windows (VRPTW)
- Pickup and Delivery Problem with Time Windows (PDPTW)
- Traveling Salesman Problem (TSP)
- Quadratic Assignment Problem (QAP)
- Job Shop Scheduling
- Network Routing
- Graph Coloring

## Tham Số Điều Chỉnh

### Alpha (α) - Tầm Quan Trọng Của Pheromone
- **α = 0:** Chỉ dựa vào heuristic (greedy)
- **α = 1:** Cân bằng pheromone và heuristic
- **α > 1:** Ưu tiên pheromone

### Beta (β) - Tầm Quan Trọng Của Heuristic
- **β = 0:** Chỉ dựa vào pheromone
- **β = 2:** Giá trị chuẩn cho hầu hết bài toán
- **β > 2:** Ưu tiên heuristic mạnh

### Rho (ρ) - Tỷ Lệ Bay Hơi
- **ρ = 0:** Không bay hơi (có thể stagnation)
- **ρ = 0.1:** Giá trị chuẩn
- **ρ = 1:** Bay hơi hoàn toàn (random search)

### Q - Hằng Số Chất Lượng
- **Q nhỏ:** Ít ảnh hưởng của fitness
- **Q = 100:** Giá trị chuẩn
- **Q lớn:** Tăng cường mạnh giải pháp tốt

### Kích Thước Đàn Kiến
- **Nhỏ:** Hội tụ nhanh nhưng có thể bỏ lỡ giải pháp tốt
- **Vừa phải:** Cân bằng giữa chất lượng và thời gian
- **Lớn:** Khám phá tốt nhưng tốn thời gian tính toán