# Mã Giả Thuật Toán Whale Optimization Algorithm (WOA)

## Tổng Quan
Thuật toán Whale Optimization Algorithm (WOA) mô phỏng hành vi săn mồi của cá voi lưng gù trong tự nhiên. Thuật toán này dựa trên ba hành vi chính: bao vây con mồi (encircling prey), tấn công bong bóng (bubble-net attacking), và tìm kiếm con mồi (search for prey). WOA được áp dụng để giải quyết bài toán Vehicle Routing Problem with Time Windows (VRPTW) và Pickup and Delivery Problem with Time Windows (PDPTW).

## Tham Số Thuật Toán

```
MAX_ITERATIONS = 100             // Số vòng lặp tối đa
b = 1                           // Hằng số xác định hình dạng xoắn ốc logarit
bestWhale                       // Cá voi tốt nhất (giải pháp tốt nhất)
population[]                    // Quần thể cá voi
```

## Thuật Toán Chính

```pseudocode
ALGORITHM WhaleOptimizationAlgorithm
INPUT: initialSolutions[], fitnessUtil, checkConditionUtil, locations[], currentTarget
OUTPUT: bestSolution

BEGIN
    // 1. KHỞI TẠO
    CALL setupParameters(fitnessUtil, checkConditionUtil, locations, currentTarget)
    CALL initialize(initialSolutions)
    
    // 2. VÒNG LẶP CHÍNH
    FOR iteration ← 0 TO MAX_ITERATIONS-1 DO
        // Hệ số a giảm tuyến tính từ 2 về 0
        a ← 2 × (1 - iteration / MAX_ITERATIONS)
        
        // Cập nhật vị trí từng cá voi
        FOR EACH whale IN population DO
            CALL updatePositionWhale(whale, a)
        END FOR
    END FOR
    
    RETURN bestWhale.solution
END
```

## Khởi Tạo Quần Thể

```pseudocode
FUNCTION initialize(initialSolutions[])
BEGIN
    population ← []
    
    // Khởi tạo quần thể từ các giải pháp ban đầu
    FOR EACH solution IN initialSolutions DO
        whale ← NEW Whale(solution.copy(), solution.fitness)
        ADD whale TO population
        
        // Cập nhật cá voi tốt nhất
        IF bestWhale = NULL OR whale.fitness < bestWhale.fitness THEN
            bestWhale ← whale
        END IF
    END FOR
END
```

## Cập Nhật Vị Trí Cá Voi (Core Algorithm)

```pseudocode
FUNCTION updatePositionWhale(whale, a)
BEGIN
    currentSolution ← whale.solution
    bestSolution ← bestWhale.solution
    newSolution ← currentSolution.copy()
    routes[] ← newSolution.routes
    dimensions ← routes.length  // Số chiều (số tuyến đường)
    
    // Tính toán vector A và C
    A[] ← calculatorAVector(dimensions, a)
    C[] ← calculatorCVector(dimensions)
    bestRoute[] ← bestSolution.routes
    
    // Cập nhật từng tuyến đường (từng chiều)
    FOR i ← 0 TO dimensions-1 DO
        p ← random()  // Số ngẫu nhiên [0,1]
        
        IF p < 0.5 THEN
            // GIAI ĐOẠN 1: BAO VÂY CON MỒI hoặc TÌM KIẾM
            IF |A[i]| < 1 THEN
                // Bao vây con mồi (Exploitation)
                CALL encirclingPrey(routes[i], bestRoute[i], A[i], C[i])
            ELSE
                // Tìm kiếm con mồi (Exploration)
                q ← random()
                IF q < 0.5 THEN
                    // Khám phá giải pháp liền kề
                    CALL applyRandomOperation(routes[i])
                ELSE
                    // Khám phá giải pháp hoàn toàn mới
                    CALL applyRandomMultiRouteOperation(routes)
                END IF
            END IF
        ELSE
            // GIAI ĐOẠN 2: TẤN CÔNG BONG BÓNG (Spiral Movement)
            CALL spiralMovement(routes[i], bestRoute[i], C[i])
        END IF
        
        // Kiểm tra tính khả thi
        IF NOT checkConditionUtil.isInsertionFeasible(routes[i], locations,
                                                     routes[i].maxPayload, currentTarget) THEN
            routes[i] ← currentSolution.routes[i].copy()
        END IF
    END FOR
    
    // Tính toán fitness mới
    newFitness ← fitnessUtil.calculatorFitness(routes, locations)
    newSolution.fitness ← newFitness
    
    // Cập nhật nếu tốt hơn
    IF newFitness < whale.fitness THEN
        whale.solution ← newSolution
        whale.fitness ← newFitness
        
        // Cập nhật cá voi tốt nhất
        IF newFitness < bestWhale.fitness THEN
            bestWhale ← NEW Whale(newSolution.copy(), newFitness)
            PRINT "New best solution found with fitness: " + newFitness
        END IF
    END IF
END
```

## Tính Toán Vector A và C

```pseudocode
FUNCTION calculatorAVector(dimensions, a)
BEGIN
    A[] ← NEW ARRAY[dimensions]
    FOR i ← 0 TO dimensions-1 DO
        // A = 2 * a * r - a, với r là số ngẫu nhiên [0,1]
        A[i] ← 2 × a × random() - a
    END FOR
    RETURN A
END

FUNCTION calculatorCVector(dimensions)
BEGIN
    C[] ← NEW ARRAY[dimensions]
    FOR i ← 0 TO dimensions-1 DO
        // C = 2 * r, với r là số ngẫu nhiên [0,1]
        C[i] ← 2 × random()
    END FOR
    RETURN C
END
```

## Bao Vây Con Mồi (Encircling Prey)

```pseudocode
FUNCTION encirclingPrey(route, bestRoute, A, C)
BEGIN
    way[] ← route.getIndLocations()
    bestWay[] ← bestRoute.getIndLocations()
    minLength ← MIN(way.length, bestWay.length)
    
    FOR i ← 0 TO minLength-1 DO
        // Tính khoảng cách: D = |C * X_best - X_i|
        D ← |C × bestWay[i] - way[i]|
        
        // Cập nhật vị trí: X_new = X_best - A * D
        newPos ← bestWay[i] - A × D
        adjustedPos ← ROUND(newPos)
        
        // Đảm bảo vị trí hợp lệ
        IF adjustedPos ≥ 0 AND adjustedPos < way.length THEN
            way[i] ← adjustedPos
        END IF
    END FOR
END
```

## Chuyển Động Xoắn Ốc (Spiral Movement)

```pseudocode
FUNCTION spiralMovement(route, bestRoute, C)
BEGIN
    way[] ← route.getIndLocations()
    bestWay[] ← bestRoute.getIndLocations()
    minLength ← MIN(way.length, bestWay.length)
    
    FOR i ← 0 TO minLength-1 DO
        // Tính khoảng cách: D' = |X_best - X_i|
        D_prime ← |bestWay[i] - way[i]|
        
        // Tham số xoắn ốc ngẫu nhiên trong [-1, 1]
        l ← random(-1, 1)
        
        // Công thức xoắn ốc: X_new = D' * e^(bl) * cos(2πl) + X_best
        newPos ← D_prime × exp(b × l) × cos(2 × PI × l) + bestWay[i]
        adjustedPos ← ROUND(newPos)
        
        // Đảm bảo vị trí hợp lệ
        IF adjustedPos ≥ 0 AND adjustedPos < way.length THEN
            way[i] ← adjustedPos
        END IF
    END FOR
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

## Ba Giai Đoạn Chính Của WOA

### 1. Bao Vây Con Mồi (Encircling Prey)
- **Điều kiện:** p < 0.5 và |A| < 1
- **Công thức:** 
  - D = |C × X_best - X|
  - X_new = X_best - A × D
- **Mục đích:** Khai thác vùng xung quanh giải pháp tốt nhất

### 2. Tấn Công Bong Bóng (Bubble-net Attacking)
- **Điều kiện:** p ≥ 0.5
- **Công thức xoắn ốc:** X_new = D' × e^(bl) × cos(2πl) + X_best
- **Mục đích:** Mô phỏng chuyển động xoắn ốc của cá voi khi săn mồi

### 3. Tìm Kiếm Con Mồi (Search for Prey)
- **Điều kiện:** p < 0.5 và |A| ≥ 1
- **Hành động:** Thực hiện các toán tử ngẫu nhiên
- **Mục đích:** Khám phá không gian tìm kiếm rộng hơn

## Đặc Điểm Nổi Bật

### 1. Cân Bằng Exploration-Exploitation
- **Tham số a:** Giảm tuyến tính từ 2 về 0
- **Vector A:** Điều khiển chuyển đổi giữa exploration và exploitation
- **Xác suất p:** Quyết định sử dụng encircling hay spiral movement

### 2. Chuyển Động Xoắn Ốc
- **Mô phỏng thực tế:** Dựa trên hành vi săn mồi của cá voi lưng gù
- **Công thức toán học:** Sử dụng hàm mũ và lượng giác
- **Hiệu quả:** Tạo ra các chuyển động đa dạng xung quanh giải pháp tốt

### 3. Cơ Chế Cập Nhật Vị Trí
- **Đa chiều:** Xử lý từng tuyến đường độc lập
- **Kiểm tra tính khả thi:** Đảm bảo giải pháp hợp lệ
- **Cập nhật thông minh:** Chỉ chấp nhận giải pháp tốt hơn

## Ưu Điểm

1. **Đơn giản:** Ít tham số cần điều chỉnh
2. **Hiệu quả:** Cân bằng tốt giữa exploration và exploitation
3. **Linh hoạt:** Áp dụng được cho nhiều loại bài toán
4. **Hội tụ nhanh:** Thường tìm được giải pháp tốt trong thời gian ngắn

## Nhược Điểm

1. **Phụ thuộc giải pháp ban đầu:** Chất lượng quần thể ban đầu ảnh hưởng lớn
2. **Có thể hội tụ sớm:** Trong một số trường hợp có thể rơi vào tối ưu cục bộ
3. **Tham số b cố định:** Không thích ứng với đặc điểm bài toán

## Công Thức Toán Học Chính

### Vector A và C
```
A = 2 × a × r - a
C = 2 × r
```
Trong đó: r là số ngẫu nhiên [0,1], a giảm tuyến tính từ 2 về 0

### Bao Vây Con Mồi
```
D = |C × X_best - X|
X_new = X_best - A × D
```

### Chuyển Động Xoắn Ốc
```
D' = |X_best - X|
X_new = D' × e^(bl) × cos(2πl) + X_best
```
Trong đó: l ∈ [-1,1], b = 1 (hằng số hình dạng xoắn ốc)

## Ứng Dụng
Thuật toán WOA đặc biệt hiệu quả cho:
- Vehicle Routing Problem with Time Windows (VRPTW)
- Pickup and Delivery Problem with Time Windows (PDPTW)
- Traveling Salesman Problem (TSP)
- Job Shop Scheduling
- Feature Selection
- Engineering Design Optimization

## Tham Số Điều Chỉnh

### Số Vòng Lặp (MAX_ITERATIONS)
- **Ít:** Có thể không đủ thời gian hội tụ
- **Nhiều:** Tăng thời gian tính toán

### Hằng Số b
- **b = 1:** Giá trị chuẩn cho hầu hết bài toán
- **b > 1:** Xoắn ốc chặt hơn
- **b < 1:** Xoắn ốc rộng hơn

### Kích Thước Quần Thể
- **Nhỏ:** Hội tụ nhanh nhưng có thể bỏ lỡ giải pháp tốt
- **Lớn:** Khám phá tốt hơn nhưng tốn thời gian tính toán