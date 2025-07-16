# Mã Giả Thuật Toán Simulated Annealing (SA)

## Tổng Quan
Thuật toán Simulated Annealing (SA) mô phỏng quá trình ủ kim loại trong luyện kim. Thuật toán này được áp dụng để giải quyết bài toán Vehicle Routing Problem with Time Windows (VRPTW) và Pickup and Delivery Problem with Time Windows (PDPTW) bằng cách tìm kiếm giải pháp tối ưu thông qua việc chấp nhận các giải pháp tệ hơn với xác suất giảm dần theo thời gian.

## Tham Số Thuật Toán

```
INITIAL_TEMPERATURE = 100.0      // Nhiệt độ ban đầu
COOLING_RATE = 0.95              // Tỷ lệ làm lạnh
FINAL_TEMPERATURE = 0.1          // Nhiệt độ cuối cùng
MAX_ITERATIONS = 1000            // Số vòng lặp tối đa ở mỗi nhiệt độ
initialSolution                  // Giải pháp ban đầu
```

## Thuật Toán Chính

```pseudocode
ALGORITHM SimulatedAnnealing
INPUT: initialSolutions[], fitnessUtil, checkConditionUtil, locations[], currentTarget
OUTPUT: bestSolution

BEGIN
    // 1. KHỞI TẠO
    CALL setupParameters(fitnessUtil, checkConditionUtil, locations, currentTarget)
    
    // Sử dụng giải pháp ban đầu từ constructor hoặc từ tham số
    startSolution ← initialSolution
    IF initialSolutions ≠ NULL AND initialSolutions.length > 0 THEN
        startSolution ← initialSolutions[0]
    END IF
    
    population ← NEW Set()
    ADD startSolution TO population
    temperature ← INITIAL_TEMPERATURE
    
    currentSolution ← startSolution
    bestSolution ← currentSolution.copy()
    bestEnergy ← calculateEnergy(bestSolution.getRoutes())
    
    // 2. VÒNG LẶP CHÍNH - Giảm nhiệt độ dần
    WHILE temperature > FINAL_TEMPERATURE DO
        
        // Vòng lặp ở mỗi nhiệt độ
        FOR i ← 0 TO MAX_ITERATIONS-1 DO
            // Tạo giải pháp mới bằng cách biến đổi giải pháp hiện tại
            newSolution ← perturbSolution(currentSolution.copy())
            
            // Chỉ xử lý nếu giải pháp mới khác giải pháp hiện tại
            IF NOT newSolution.equals(currentSolution) THEN
                currentEnergy ← calculateEnergy(currentSolution.getRoutes())
                newEnergy ← calculateEnergy(newSolution.getRoutes())
                deltaEnergy ← newEnergy - currentEnergy
                
                // Chấp nhận giải pháp mới nếu:
                // 1. Tốt hơn (deltaEnergy < 0), hoặc
                // 2. Xác suất chấp nhận > số ngẫu nhiên
                IF deltaEnergy < 0 OR acceptanceProbability(deltaEnergy, temperature) > random() THEN
                    currentSolution ← newSolution.copy()
                    
                    // Cập nhật giải pháp tốt nhất nếu cần
                    IF newEnergy < bestEnergy THEN
                        bestSolution ← newSolution.copy()
                        bestEnergy ← newEnergy
                    END IF
                END IF
            END IF
        END FOR
        
        // Thêm giải pháp hiện tại vào quần thể
        ADD currentSolution TO population
        
        // Giảm nhiệt độ
        temperature ← temperature × COOLING_RATE
    END WHILE
    
    // Trả về giải pháp tốt nhất
    RETURN bestSolution
END
```

## Hàm Biến Đổi Giải Pháp (Perturbation)

```pseudocode
FUNCTION perturbSolution(solution)
BEGIN
    // Lấy các tuyến đường từ giải pháp
    routes[] ← solution.getRoutes()
    
    // Quyết định áp dụng toán tử đơn tuyến hoặc đa tuyến
    useMultiRouteOperator ← random() < 0.3 AND routes.length ≥ 2
    
    IF useMultiRouteOperator THEN
        // Áp dụng toán tử đa tuyến (PD-Shift hoặc PD-Exchange)
        CALL applyRandomMultiRouteOperation(routes)
        
        // Kiểm tra tính khả thi của tất cả các tuyến đường
        FOR i ← 0 TO routes.length-1 DO
            IF NOT checkConditionUtil.isInsertionFeasible(routes[i], locations, 
                                                         routes[i].getMaxPayload(), currentTarget) THEN
                // Khôi phục tuyến đường không khả thi
                routes[i] ← solution.getRoutes()[i].copy()
            END IF
        END FOR
    ELSE
        // Chọn ngẫu nhiên một toán tử biến đổi đơn tuyến
        operator ← RANDOM(0, 2)
        
        // Chọn ngẫu nhiên một tuyến đường để biến đổi
        routeIndex ← RANDOM(0, routes.length-1)
        cloneRoute ← routes[routeIndex].copy()
        
        // Áp dụng toán tử biến đổi
        SWITCH operator
            CASE 0: CALL applySwapOperator(cloneRoute)
            CASE 1: CALL applyInsertOperator(cloneRoute)
            CASE 2: CALL applyReverseOperator(cloneRoute)
        END SWITCH
        
        // Kiểm tra tính khả thi của tuyến đường mới
        IF checkConditionUtil.isInsertionFeasible(cloneRoute, locations, 
                                                 cloneRoute.getMaxPayload(), currentTarget) THEN
            routes[routeIndex] ← cloneRoute  // Cập nhật tuyến đường
        END IF
    END IF
    
    // Cập nhật khoảng cách cho tất cả các tuyến đường
    FOR EACH route IN routes DO
        route.calculateDistance(locations)
    END FOR
    
    RETURN solution
END
```

## Hàm Tính Năng Lượng (Energy/Fitness)

```pseudocode
FUNCTION calculateEnergy(routes[])
BEGIN
    RETURN fitnessUtil.calculatorFitness(routes, locations)
END
```

## Hàm Tính Xác Suất Chấp Nhận

```pseudocode
FUNCTION acceptanceProbability(deltaEnergy, temperature)
BEGIN
    // Công thức Boltzmann: e^(-ΔE/T)
    RETURN exp(-deltaEnergy / temperature)
END
```

## Hàm Chạy Và Trả Về Quần Thể (Backward Compatibility)

```pseudocode
FUNCTION runAndGetPopulation(fitnessUtil, checkConditionUtil, locations[], currentTarget)
BEGIN
    // Thiết lập các tham số từ lớp cha
    CALL setupParameters(fitnessUtil, checkConditionUtil, locations, currentTarget)
    
    // Chạy thuật toán để tìm giải pháp tốt nhất
    CALL run([initialSolution], fitnessUtil, checkConditionUtil, locations, currentTarget)
    
    // Tạo tập quần thể
    population ← NEW Set()
    ADD initialSolution TO population
    temperature ← INITIAL_TEMPERATURE
    currentSolution ← initialSolution
    
    WHILE temperature > FINAL_TEMPERATURE DO
        FOR i ← 0 TO MAX_ITERATIONS-1 DO
            newSolution ← perturbSolution(currentSolution.copy())
            currentEnergy ← calculateEnergy(currentSolution.getRoutes())
            newEnergy ← calculateEnergy(newSolution.getRoutes())
            deltaEnergy ← newEnergy - currentEnergy
            
            IF deltaEnergy < 0 OR acceptanceProbability(deltaEnergy, temperature) > random() THEN
                currentSolution ← newSolution.copy()
            END IF
        END FOR
        ADD currentSolution TO population
        temperature ← temperature × COOLING_RATE
    END WHILE
    
    RETURN population.toArray()
END
```

## Các Toán Tử Biến Đổi

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

## Nguyên Lý Hoạt Động

### 1. Quá Trình Làm Lạnh (Cooling Schedule)
- **Nhiệt độ ban đầu cao:** Chấp nhận nhiều giải pháp tệ hơn
- **Nhiệt độ giảm dần:** Giảm khả năng chấp nhận giải pháp tệ
- **Nhiệt độ thấp:** Chỉ chấp nhận giải pháp tốt hơn

### 2. Xác Suất Chấp Nhận
- **Công thức Boltzmann:** P = e^(-ΔE/T)
- **ΔE < 0:** Luôn chấp nhận (giải pháp tốt hơn)
- **ΔE > 0:** Chấp nhận với xác suất giảm dần theo nhiệt độ

### 3. Cân Bằng Exploration-Exploitation
- **Nhiệt độ cao:** Exploration (khám phá)
- **Nhiệt độ thấp:** Exploitation (khai thác)

## Ưu Điểm

1. **Tránh tối ưu cục bộ:** Chấp nhận giải pháp tệ hơn với xác suất nhất định
2. **Đơn giản:** Dễ hiểu và implement
3. **Linh hoạt:** Có thể áp dụng cho nhiều loại bài toán
4. **Hội tụ:** Đảm bảo hội tụ về giải pháp tối ưu với thời gian đủ dài

## Nhược Điểm

1. **Tốc độ chậm:** Cần nhiều vòng lặp để hội tụ
2. **Tham số nhạy cảm:** Cần điều chỉnh cẩn thận các tham số
3. **Không đảm bảo tối ưu toàn cục:** Trong thời gian hữu hạn

## Ứng Dụng
Thuật toán SA đặc biệt hiệu quả cho:
- Vehicle Routing Problem with Time Windows (VRPTW)
- Pickup and Delivery Problem with Time Windows (PDPTW)
- Traveling Salesman Problem (TSP)
- Scheduling Problems
- Các bài toán tối ưu tổ hợp có không gian tìm kiếm lớn

## Tham Số Điều Chỉnh

### Nhiệt Độ Ban Đầu
- **Quá cao:** Tìm kiếm ngẫu nhiên, hội tụ chậm
- **Quá thấp:** Dễ rơi vào tối ưu cục bộ

### Tỷ Lệ Làm Lạnh
- **Quá nhanh (< 0.9):** Có thể bỏ lỡ giải pháp tốt
- **Quá chậm (> 0.99):** Tốn thời gian tính toán

### Số Vòng Lặp Ở Mỗi Nhiệt Độ
- **Quá ít:** Không đủ thời gian khám phá
- **Quá nhiều:** Lãng phí thời gian tính toán