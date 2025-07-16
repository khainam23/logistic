# Mã Giả Thuật Toán Grey Wolf Optimizer (GWO)

## Tổng Quan
Thuật toán Grey Wolf Optimizer (GWO) mô phỏng hành vi săn mồi và thứ bậc xã hội của bầy sói xám trong tự nhiên. Thuật toán này dựa trên cấu trúc thứ bậc của bầy sói với ba cấp độ lãnh đạo: Alpha (α), Beta (β), và Delta (δ), cùng với hành vi săn mồi bao gồm: truy đuổi, bao vây và tấn công con mồi. GWO được áp dụng để giải quyết bài toán Vehicle Routing Problem with Time Windows (VRPTW) và Pickup and Delivery Problem with Time Windows (PDPTW).

## Tham Số Thuật Toán

```
MAX_ITERATIONS = 100             // Số vòng lặp tối đa
population[]                     // Quần thể sói
alpha                           // Sói alpha (tốt nhất)
beta                            // Sói beta (tốt thứ hai)
delta                           // Sói delta (tốt thứ ba)
```

## Thuật Toán Chính

```pseudocode
ALGORITHM GreyWolfOptimizer
INPUT: initialSolutions[], fitnessUtil, checkConditionUtil, locations[], currentTarget
OUTPUT: bestSolution

BEGIN
    // 1. KHỞI TẠO
    CALL setupParameters(fitnessUtil, checkConditionUtil, locations, currentTarget)
    CALL initialize(initialSolutions)
    
    // 2. VÒNG LẶP CHÍNH
    FOR iteration ← 0 TO MAX_ITERATIONS-1 DO
        // Hệ số a giảm tuyến tính từ 2 về 0
        a ← 2 - iteration × (2.0 / MAX_ITERATIONS)
        
        // Cập nhật vị trí từng sói
        FOR EACH wolf IN population DO
            CALL updateWolfPosition(wolf, a)
        END FOR
        
        // Đa dạng hóa quần thể định kỳ (mỗi 10 vòng lặp)
        IF iteration MOD 10 = 0 THEN
            CALL diversifyPopulation()
        END IF
    END FOR
    
    RETURN alpha.solution
END
```

## Khởi Tạo Quần Thể

```pseudocode
FUNCTION initialize(initialSolutions[])
BEGIN
    population ← []
    
    // Khởi tạo quần thể từ các giải pháp ban đầu
    FOR EACH solution IN initialSolutions DO
        wolf ← NEW Wolf(solution.copy(), solution.fitness)
        ADD wolf TO population
    END FOR
    
    // Sắp xếp quần thể theo fitness tăng dần
    SORT population BY fitness
    
    // Xác định sói alpha, beta và delta
    IF population.size() > 0 THEN
        alpha ← NEW Wolf(population[0].solution.copy(), population[0].fitness)
        
        IF population.size() > 1 THEN
            beta ← NEW Wolf(population[1].solution.copy(), population[1].fitness)
        ELSE
            beta ← alpha
        END IF
        
        IF population.size() > 2 THEN
            delta ← NEW Wolf(population[2].solution.copy(), population[2].fitness)
        ELSE
            delta ← beta
        END IF
    END IF
END
```

## Cập Nhật Vị Trí Sói (Core Algorithm)

```pseudocode
FUNCTION updateWolfPosition(wolf, a)
BEGIN
    currentSolution ← wolf.solution
    newSolution ← currentSolution.copy()
    routes[] ← newSolution.routes
    dimensions ← routes.length  // Số chiều (số tuyến đường)
    
    // Tính toán vector A và C cho từng sói lãnh đạo
    A1[] ← calculateAVector(dimensions, a)
    A2[] ← calculateAVector(dimensions, a)
    A3[] ← calculateAVector(dimensions, a)
    
    C1[] ← calculateCVector(dimensions)
    C2[] ← calculateCVector(dimensions)
    C3[] ← calculateCVector(dimensions)
    
    // Cập nhật từng tuyến đường (từng chiều)
    FOR i ← 0 TO dimensions-1 DO
        currentRoute ← routes[i]
        
        // Tính toán khoảng cách đến sói alpha, beta và delta
        D_alpha ← calculateRouteDistance(currentRoute, alpha.solution.routes[i], C1[i])
        D_beta ← calculateRouteDistance(currentRoute, beta.solution.routes[i], C2[i])
        D_delta ← calculateRouteDistance(currentRoute, delta.solution.routes[i], C3[i])
        
        // Tính toán vị trí mới theo hướng dẫn của sói alpha, beta và delta
        X1 ← moveTowardsLeader(alpha.solution.routes[i], D_alpha, A1[i])
        X2 ← moveTowardsLeader(beta.solution.routes[i], D_beta, A2[i])
        X3 ← moveTowardsLeader(delta.solution.routes[i], D_delta, A3[i])
        
        // Cập nhật vị trí mới là trung bình của ba vị trí
        CALL updateRouteFromLeaders(currentRoute, X1, X2, X3)
        
        // Kiểm tra tính khả thi
        IF NOT checkConditionUtil.isInsertionFeasible(currentRoute, locations,
                                                     currentRoute.maxPayload, currentTarget) THEN
            routes[i] ← currentSolution.routes[i].copy()
        END IF
    END FOR
    
    // Áp dụng các toán tử đa tuyến với xác suất 30%
    IF random() < 0.3 THEN
        CALL applyRandomMultiRouteOperation(routes)
        
        // Kiểm tra tính khả thi sau khi áp dụng toán tử đa tuyến
        FOR i ← 0 TO dimensions-1 DO
            IF NOT checkConditionUtil.isInsertionFeasible(routes[i], locations,
                                                         routes[i].maxPayload, currentTarget) THEN
                routes[i] ← currentSolution.routes[i].copy()
            END IF
        END FOR
    END IF
    
    // Tính toán fitness mới
    newFitness ← fitnessUtil.calculatorFitness(routes, locations)
    newSolution.fitness ← newFitness
    
    // Cập nhật nếu tốt hơn
    IF newFitness < wolf.fitness THEN
        wolf.solution ← newSolution
        wolf.fitness ← newFitness
        
        // Cập nhật thứ bậc sói
        CALL updateHierarchy(wolf)
    END IF
END
```

## Cập Nhật Thứ Bậc Sói

```pseudocode
FUNCTION updateHierarchy(wolf)
BEGIN
    IF wolf.fitness < alpha.fitness THEN
        // Sói mới trở thành alpha
        delta ← beta
        beta ← alpha
        alpha ← NEW Wolf(wolf.solution.copy(), wolf.fitness)
        PRINT "New alpha wolf with fitness: " + alpha.fitness
    ELSE IF wolf.fitness < beta.fitness THEN
        // Sói mới trở thành beta
        delta ← beta
        beta ← NEW Wolf(wolf.solution.copy(), wolf.fitness)
    ELSE IF wolf.fitness < delta.fitness THEN
        // Sói mới trở thành delta
        delta ← NEW Wolf(wolf.solution.copy(), wolf.fitness)
    END IF
END
```

## Tính Toán Vector A và C

```pseudocode
FUNCTION calculateAVector(dimensions, a)
BEGIN
    A[] ← NEW ARRAY[dimensions]
    FOR i ← 0 TO dimensions-1 DO
        // Công thức GWO: A = 2a * r1 - a, với r1 là số ngẫu nhiên trong [0,1]
        A[i] ← 2 × a × random() - a
    END FOR
    RETURN A
END

FUNCTION calculateCVector(dimensions)
BEGIN
    C[] ← NEW ARRAY[dimensions]
    FOR i ← 0 TO dimensions-1 DO
        // Công thức GWO: C = 2 * r2, với r2 là số ngẫu nhiên trong [0,1]
        C[i] ← 2 × random()
    END FOR
    RETURN C
END
```

## Tính Khoảng Cách Giữa Hai Tuyến Đường

```pseudocode
FUNCTION calculateRouteDistance(route, leaderRoute, C)
BEGIN
    way[] ← route.getIndLocations()
    leaderWay[] ← leaderRoute.getIndLocations()
    minLength ← MIN(way.length, leaderWay.length)
    
    distance ← 0
    FOR i ← 0 TO minLength-1 DO
        // Công thức GWO: D = |C * X_leader - X|
        distance ← distance + |C × leaderWay[i] - way[i]|
    END FOR
    
    RETURN distance / minLength
END
```

## Di Chuyển Về Phía Sói Lãnh Đạo

```pseudocode
FUNCTION moveTowardsLeader(leaderRoute, D, A)
BEGIN
    newRoute ← leaderRoute.copy()
    leaderWay[] ← leaderRoute.getIndLocations()
    newWay[] ← newRoute.getIndLocations()
    
    // Xác định giới hạn trên cho chỉ số location
    maxLocationIndex ← locations.length - 1
    
    FOR i ← 0 TO newWay.length-1 DO
        // Công thức GWO: X_new = X_leader - A * D
        newPos ← leaderWay[i] - A × D
        
        // Làm tròn và giới hạn trong phạm vi hợp lệ [0, maxLocationIndex]
        adjustedPos ← ROUND(newPos)
        IF adjustedPos ≥ 0 AND adjustedPos ≤ maxLocationIndex THEN
            newWay[i] ← adjustedPos
        ELSE IF adjustedPos > maxLocationIndex THEN
            // Nếu vượt quá giới hạn, gán bằng giới hạn
            newWay[i] ← maxLocationIndex
        END IF
    END FOR
    
    RETURN newRoute
END
```

## Cập Nhật Tuyến Đường Từ Ba Sói Lãnh Đạo

```pseudocode
FUNCTION updateRouteFromLeaders(route, X1, X2, X3)
BEGIN
    way[] ← route.getIndLocations()
    way1[] ← X1.getIndLocations()
    way2[] ← X2.getIndLocations()
    way3[] ← X3.getIndLocations()
    
    minLength ← MIN(way.length, MIN(way1.length, MIN(way2.length, way3.length)))
    
    // Xác định giới hạn trên cho chỉ số location
    maxLocationIndex ← locations.length - 1
    
    FOR i ← 0 TO minLength-1 DO
        // Công thức GWO: X_new = (X1 + X2 + X3) / 3
        newPos ← (way1[i] + way2[i] + way3[i]) / 3.0
        
        // Làm tròn và giới hạn trong phạm vi hợp lệ [0, maxLocationIndex]
        adjustedPos ← ROUND(newPos)
        IF adjustedPos ≥ 0 AND adjustedPos ≤ maxLocationIndex THEN
            way[i] ← adjustedPos
        ELSE IF adjustedPos > maxLocationIndex THEN
            // Nếu vượt quá giới hạn, gán bằng giới hạn
            way[i] ← maxLocationIndex
        END IF
    END FOR
    
    // Áp dụng toán tử ngẫu nhiên để đa dạng hóa với xác suất 20%
    IF random() < 0.2 THEN
        CALL applyRandomOperation(route)
    END IF
END
```

## Đa Dạng Hóa Quần Thể

```pseudocode
FUNCTION diversifyPopulation()
BEGIN
    // Sắp xếp quần thể theo fitness
    SORT population BY fitness
    
    // Giữ lại 30% sói tốt nhất, thay thế phần còn lại
    keepCount ← ROUND(population.size() × 0.3)
    
    FOR i ← keepCount TO population.size()-1 DO
        newSolution ← createDiversifiedSolution()
        newFitness ← fitnessUtil.calculatorFitness(newSolution.routes, locations)
        population[i].solution ← newSolution
        population[i].fitness ← newFitness
        
        // Cập nhật thứ bậc sói nếu cần
        IF newFitness < alpha.fitness THEN
            CALL updateHierarchy(population[i])
        END IF
    END FOR
END
```

## Tạo Giải Pháp Đa Dạng Hóa

```pseudocode
FUNCTION createDiversifiedSolution()
BEGIN
    // Chọn ngẫu nhiên từ sói alpha, beta hoặc delta
    leader ← NULL
    rand ← random()
    IF rand < 0.6 THEN
        leader ← alpha  // Ưu tiên học từ alpha
    ELSE IF rand < 0.8 THEN
        leader ← beta
    ELSE
        leader ← delta
    END IF
    
    newSolution ← leader.solution.copy()
    routes[] ← newSolution.routes
    
    // Áp dụng các toán tử đơn tuyến
    FOR EACH route IN routes DO
        operations ← 1 + RANDOM(0, 1)
        FOR i ← 0 TO operations-1 DO
            CALL applyRandomOperation(route)
        END FOR
    END FOR
    
    // Áp dụng các toán tử đa tuyến (PD-Shift và PD-Exchange)
    multiRouteOperations ← 1 + RANDOM(0, 1)
    FOR i ← 0 TO multiRouteOperations-1 DO
        CALL applyRandomMultiRouteOperation(routes)
    END FOR
    
    // Cập nhật khoảng cách cho tất cả các tuyến đường
    FOR EACH route IN routes DO
        route.calculateDistance(locations)
    END FOR
    
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

## Thứ Bậc Xã Hội Trong GWO

### 1. Sói Alpha (α)
- **Vai trò:** Lãnh đạo bầy sói, giải pháp tốt nhất
- **Đặc điểm:** Có fitness thấp nhất (tốt nhất)
- **Ảnh hưởng:** Hướng dẫn chính cho việc cập nhật vị trí

### 2. Sói Beta (β)
- **Vai trò:** Phó lãnh đạo, giải pháp tốt thứ hai
- **Đặc điểm:** Hỗ trợ alpha trong việc ra quyết định
- **Ảnh hưởng:** Ảnh hưởng thứ hai trong việc cập nhật vị trí

### 3. Sói Delta (δ)
- **Vai trò:** Cấp độ thứ ba, giải pháp tốt thứ ba
- **Đặc điểm:** Bao gồm trinh sát, canh gác, thợ săn già
- **Ảnh hưởng:** Ảnh hưởng thứ ba trong việc cập nhật vị trí

### 4. Sói Omega (ω)
- **Vai trò:** Các sói còn lại trong bầy
- **Đặc điểm:** Tuân theo hướng dẫn của alpha, beta, delta
- **Ảnh hưởng:** Được cập nhật vị trí dựa trên ba sói lãnh đạo

## Giai Đoạn Săn Mồi Trong GWO

### 1. Truy Đuổi và Bao Vây Con Mồi
- **Điều kiện:** |A| < 1
- **Công thức:** 
  - D = |C × X_p - X|
  - X_new = X_p - A × D
- **Mục đích:** Khai thác vùng xung quanh giải pháp tốt

### 2. Tìm Kiếm Con Mồi
- **Điều kiện:** |A| ≥ 1
- **Hành động:** Thực hiện các toán tử ngẫu nhiên
- **Mục đích:** Khám phá không gian tìm kiếm rộng hơn

### 3. Tấn Công Con Mồi
- **Cơ chế:** Kết hợp hướng dẫn từ alpha, beta, delta
- **Công thức:** X_new = (X1 + X2 + X3) / 3
- **Mục đích:** Tìm vị trí tối ưu dựa trên ba sói lãnh đạo

## Đặc Điểm Nổi Bật

### 1. Cấu Trúc Thứ Bậc
- **Phân cấp rõ ràng:** Alpha > Beta > Delta > Omega
- **Cập nhật động:** Thứ bậc thay đổi theo fitness
- **Hướng dẫn thông minh:** Học từ ba giải pháp tốt nhất

### 2. Cân Bằng Exploration-Exploitation
- **Tham số a:** Giảm tuyến tính từ 2 về 0
- **Vector A:** Điều khiển chuyển đổi giữa exploration và exploitation
- **Đa dạng hóa:** Định kỳ thay thế các sói kém

### 3. Cơ Chế Cập Nhật Vị Trí
- **Đa hướng dẫn:** Kết hợp từ alpha, beta, delta
- **Trung bình hóa:** Giảm thiểu rủi ro từ một hướng dẫn duy nhất
- **Kiểm tra tính khả thi:** Đảm bảo giải pháp hợp lệ

## Ưu Điểm

1. **Cấu trúc đơn giản:** Ít tham số cần điều chỉnh
2. **Hội tụ nhanh:** Sử dụng ba giải pháp tốt nhất để hướng dẫn
3. **Cân bằng tốt:** Giữa exploration và exploitation
4. **Linh hoạt:** Áp dụng được cho nhiều loại bài toán

## Nhược Điểm

1. **Phụ thuộc giải pháp ban đầu:** Chất lượng quần thể ban đầu ảnh hưởng lớn
2. **Có thể hội tụ sớm:** Khi ba sói lãnh đạo quá gần nhau
3. **Thiếu đa dạng:** Trong giai đoạn cuối có thể mất đa dạng

## Công Thức Toán Học Chính

### Vector A và C
```
A = 2 × a × r1 - a
C = 2 × r2
```
Trong đó: r1, r2 là số ngẫu nhiên [0,1], a giảm tuyến tính từ 2 về 0

### Khoảng Cách Đến Con Mồi
```
D_α = |C1 × X_α - X|
D_β = |C2 × X_β - X|  
D_δ = |C3 × X_δ - X|
```

### Cập Nhật Vị Trí
```
X1 = X_α - A1 × D_α
X2 = X_β - A2 × D_β
X3 = X_δ - A3 × D_δ
X_new = (X1 + X2 + X3) / 3
```

## Ứng Dụng
Thuật toán GWO đặc biệt hiệu quả cho:
- Vehicle Routing Problem with Time Windows (VRPTW)
- Pickup and Delivery Problem with Time Windows (PDPTW)
- Traveling Salesman Problem (TSP)
- Feature Selection
- Neural Network Training
- Engineering Design Optimization
- Economic Load Dispatch

## Tham Số Điều Chỉnh

### Số Vòng Lặp (MAX_ITERATIONS)
- **Ít:** Có thể không đủ thời gian hội tụ
- **Nhiều:** Tăng thời gian tính toán

### Kích Thước Quần Thể
- **Nhỏ:** Hội tụ nhanh nhưng có thể bỏ lỡ giải pháp tốt
- **Lớn:** Khám phá tốt hơn nhưng tốn thời gian tính toán

### Tần Suất Đa Dạng Hóa
- **Thường xuyên:** Duy trì đa dạng nhưng có thể làm chậm hội tụ
- **Ít:** Hội tụ nhanh nhưng có thể mất đa dạng

### Tỷ Lệ Giữ Lại Trong Đa Dạng Hóa
- **30%:** Giá trị chuẩn, cân bằng giữa khai thác và khám phá
- **Cao hơn:** Tập trung khai thác
- **Thấp hơn:** Tăng cường khám phá