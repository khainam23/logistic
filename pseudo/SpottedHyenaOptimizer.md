# Mã Giả Thuật Toán Spotted Hyena Optimizer (SHO)

## Tổng Quan
Thuật toán Spotted Hyena Optimizer (SHO) mô phỏng hành vi săn mồi của linh cẩu đốm trong tự nhiên. Thuật toán này được áp dụng để giải quyết bài toán Vehicle Routing Problem with Time Windows (VRPTW) và Pickup and Delivery Problem with Time Windows (PDPTW).

## Tham Số Thuật Toán

```
MAX_ITERATIONS = 100          // Số vòng lặp tối đa
CLUSTER_SIZE = 5              // Kích thước cụm linh cẩu
population[]                  // Quần thể linh cẩu
clusters[][]                  // Các cụm linh cẩu
bestHyena                     // Linh cẩu tốt nhất toàn cục
```

## Thuật Toán Chính

```pseudocode
ALGORITHM SpottedHyenaOptimizer
INPUT: initialSolutions[], fitnessUtil, checkConditionUtil, locations[], currentTarget
OUTPUT: bestSolution

BEGIN
    // 1. KHỞI TẠO
    CALL setupParameters(fitnessUtil, checkConditionUtil, locations, currentTarget)
    CALL initialize(initialSolutions)
    
    // 2. VÒNG LẶP CHÍNH
    FOR iteration ← 0 TO MAX_ITERATIONS-1 DO
        // Tính hệ số a giảm tuyến tính từ 5 về 0
        a ← 5 × (1 - iteration / MAX_ITERATIONS)
        
        // Cập nhật vị trí từng linh cẩu
        FOR EACH hyena IN population DO
            CALL updateHyenaPosition(hyena, a)
        END FOR
        
        // Cập nhật cụm định kỳ (mỗi 10 vòng lặp)
        IF iteration MOD 10 = 0 THEN
            CALL formClusters()
            CALL diversifyClusters()
        END IF
    END FOR
    
    RETURN bestHyena.solution
END
```

## Khởi Tạo Quần Thể

```pseudocode
FUNCTION initialize(initialSolutions[])
BEGIN
    population ← []
    clusters ← []
    bestHyena ← NULL
    
    // Tạo quần thể từ các giải pháp ban đầu
    FOR EACH solution IN initialSolutions DO
        hyena ← NEW Hyena(solution.copy(), solution.fitness)
        ADD hyena TO population
        
        // Cập nhật linh cẩu tốt nhất
        IF bestHyena = NULL OR hyena.fitness < bestHyena.fitness THEN
            bestHyena ← hyena
        END IF
    END FOR
    
    // Phân cụm quần thể
    CALL formClusters()
END
```

## Phân Cụm Quần Thể

```pseudocode
FUNCTION formClusters()
BEGIN
    CLEAR clusters
    
    // Sắp xếp quần thể theo fitness tăng dần
    SORT population BY fitness
    
    // Tạo các cụm với kích thước CLUSTER_SIZE
    FOR i ← 0 TO population.size() STEP CLUSTER_SIZE DO
        end ← MIN(i + CLUSTER_SIZE, population.size())
        cluster ← population[i:end]
        ADD cluster TO clusters
    END FOR
END
```

## Cập Nhật Vị Trí Linh Cẩu (Core Algorithm)

```pseudocode
FUNCTION updateHyenaPosition(hyena, a)
BEGIN
    currentSolution ← hyena.solution
    bestSolution ← bestHyena.solution
    newSolution ← currentSolution.copy()
    routes[] ← newSolution.routes
    dimensions ← routes.length
    
    // Tính toán vector B và E
    B[] ← calculateBVector(dimensions)
    E[] ← calculateEVector(dimensions, a)
    
    // Cập nhật từng tuyến đường (từng chiều)
    FOR i ← 0 TO dimensions-1 DO
        // PHA 1: TÌM KIẾM (Exploration)
        IF |B[i]| > 1 THEN
            // Khám phá: thực hiện biến đổi ngẫu nhiên
            CALL applyRandomOperation(routes[i])
        
        // PHA 2 & 3: BAO VÂY VÀ TẤN CÔNG (Exploitation)
        ELSE
            // Tính khoảng cách đến giải pháp tốt nhất
            D ← calculateRouteDistance(routes[i], bestSolution.routes[i])
            
            // Cập nhật vị trí theo công thức SHO
            IF |E[i]| < 1 THEN
                // Khai thác: di chuyển về phía giải pháp tốt nhất
                CALL learnFromBestRoute(routes[i], bestSolution.routes[i], D, E[i])
            END IF
        END IF
        
        // Kiểm tra tính khả thi
        IF NOT isInsertionFeasible(routes[i]) THEN
            routes[i] ← currentSolution.routes[i].copy()
        END IF
    END FOR
    
    // Áp dụng toán tử đa tuyến với xác suất 30%
    IF random() < 0.3 THEN
        CALL applyRandomMultiRouteOperation(routes)
        
        // Kiểm tra tính khả thi sau toán tử đa tuyến
        FOR i ← 0 TO dimensions-1 DO
            IF NOT isInsertionFeasible(routes[i]) THEN
                routes[i] ← currentSolution.routes[i].copy()
            END IF
        END FOR
    END IF
    
    // Tính fitness mới và cập nhật
    newFitness ← calculatorFitness(routes)
    newSolution.fitness ← newFitness
    
    // Cập nhật nếu tốt hơn
    IF newFitness < hyena.fitness THEN
        hyena.solution ← newSolution
        hyena.fitness ← newFitness
        
        // Cập nhật linh cẩu tốt nhất toàn cục
        IF newFitness < bestHyena.fitness THEN
            bestHyena ← NEW Hyena(newSolution.copy(), newFitness)
            PRINT "New best solution found with fitness: " + newFitness
        END IF
    END IF
END
```

## Tính Toán Vector B và E

```pseudocode
FUNCTION calculateBVector(dimensions)
BEGIN
    B[] ← NEW ARRAY[dimensions]
    FOR i ← 0 TO dimensions-1 DO
        B[i] ← 2 × (1 - random())  // Ngẫu nhiên trong [0,2]
    END FOR
    RETURN B
END

FUNCTION calculateEVector(dimensions, a)
BEGIN
    E[] ← NEW ARRAY[dimensions]
    h ← 2 × a × random() - a      // [-a, a]
    FOR i ← 0 TO dimensions-1 DO
        E[i] ← h × (2 × random() - 1)  // Ngẫu nhiên trong [-h,h]
    END FOR
    RETURN E
END
```

## Tính Khoảng Cách Giữa Hai Tuyến Đường

```pseudocode
FUNCTION calculateRouteDistance(route1, route2)
BEGIN
    way1[] ← route1.locations
    way2[] ← route2.locations
    
    IF way1.length = 0 AND way2.length = 0 THEN
        RETURN 0.0
    END IF
    
    // Tính độ khác biệt về độ dài
    lengthDifference ← |way1.length - way2.length|
    
    // Tính số điểm chung
    commonPoints ← 0
    FOR EACH loc1 IN way1 DO
        FOR EACH loc2 IN way2 DO
            IF loc1 = loc2 THEN
                commonPoints ← commonPoints + 1
                BREAK
            END IF
        END FOR
    END FOR
    
    // Tính độ tương đồng về thứ tự
    orderSimilarity ← 0.0
    minLength ← MIN(way1.length, way2.length)
    IF minLength > 0 THEN
        samePositions ← 0
        FOR i ← 0 TO minLength-1 DO
            IF way1[i] = way2[i] THEN
                samePositions ← samePositions + 1
            END IF
        END FOR
        orderSimilarity ← samePositions / minLength
    END IF
    
    // Kết hợp các yếu tố tính khoảng cách
    maxLength ← MAX(way1.length, way2.length)
    structuralDistance ← (maxLength - commonPoints) / maxLength
    orderDistance ← 1.0 - orderSimilarity
    
    // Trọng số: 60% cấu trúc, 40% thứ tự
    RETURN 0.6 × structuralDistance + 0.4 × orderDistance
END
```

## Học Hỏi Từ Tuyến Đường Tốt Nhất

```pseudocode
FUNCTION learnFromBestRoute(targetRoute, bestRoute, D, E)
BEGIN
    targetWay[] ← targetRoute.locations
    bestWay[] ← bestRoute.locations
    
    IF targetWay.length ≤ 1 OR bestWay.length ≤ 1 THEN
        RETURN
    END IF
    
    // Tính số lượng thay đổi dựa trên công thức SHO
    intensity ← |E × D|
    numSwaps ← MAX(1, ROUND(intensity × targetWay.length / 2))
    numSwaps ← MIN(numSwaps, targetWay.length / 2)
    
    // Thực hiện swap để học từ best route
    FOR swap ← 0 TO numSwaps-1 DO
        // Chọn điểm ngẫu nhiên từ best route
        bestIndex ← RANDOM(0, bestWay.length-1)
        bestLocationId ← bestWay[bestIndex]
        
        // Tìm điểm này trong target route
        targetIndex ← -1
        FOR i ← 0 TO targetWay.length-1 DO
            IF targetWay[i] = bestLocationId THEN
                targetIndex ← i
                BREAK
            END IF
        END FOR
        
        // Di chuyển về vị trí tương tự như trong best route
        IF targetIndex ≠ -1 THEN
            relativePos ← bestIndex / bestWay.length
            newTargetIndex ← ROUND(relativePos × (targetWay.length - 1))
            newTargetIndex ← CLAMP(newTargetIndex, 0, targetWay.length-1)
            
            // Thực hiện swap nếu vị trí khác nhau
            IF targetIndex ≠ newTargetIndex THEN
                SWAP(targetWay[targetIndex], targetWay[newTargetIndex])
            END IF
        END IF
    END FOR
END
```

## Đa Dạng Hóa Các Cụm

```pseudocode
FUNCTION diversifyClusters()
BEGIN
    FOR EACH cluster IN clusters DO
        // Tìm linh cẩu tốt nhất trong cụm
        bestInCluster ← MIN(cluster, BY fitness)
        
        IF bestInCluster ≠ NULL THEN
            // Tạo linh cẩu mới từ linh cẩu tốt nhất cụm
            FOR EACH hyena IN cluster DO
                IF hyena ≠ bestInCluster THEN
                    newSolution ← createDiversifiedSolution(bestInCluster.solution)
                    newFitness ← calculatorFitness(newSolution.routes)
                    hyena.solution ← newSolution
                    hyena.fitness ← newFitness
                END IF
            END FOR
        END IF
    END FOR
END
```

## Tạo Giải Pháp Đa Dạng Hóa

```pseudocode
FUNCTION createDiversifiedSolution(original)
BEGIN
    newSolution ← original.copy()
    routes[] ← newSolution.routes
    
    // Áp dụng toán tử đơn tuyến
    FOR EACH route IN routes DO
        operations ← 1 + RANDOM(0, 1)
        FOR i ← 0 TO operations-1 DO
            CALL applyRandomOperation(route)
        END FOR
    END FOR
    
    // Áp dụng toán tử đa tuyến
    multiRouteOperations ← 1 + RANDOM(0, 1)
    FOR i ← 0 TO multiRouteOperations-1 DO
        CALL applyRandomMultiRouteOperation(routes)
    END FOR
    
    // Cập nhật khoảng cách và fitness
    FOR EACH route IN routes DO
        route.calculateDistance()
    END FOR
    
    newSolution.fitness ← calculatorFitness(routes)
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
    SWAP(way[pos1], way[pos2])
    
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
        SWAP(pos1, pos2)
    END IF
    
    // Đảo ngược đoạn từ pos1 đến pos2
    WHILE pos1 < pos2 DO
        SWAP(way[pos1], way[pos2])
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
    route1.calculateDistance(locations)
    route2.calculateDistance(locations)
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
    SWAP(way1[pos1], way2[pos2])
    
    // Đảm bảo giá trị không vượt quá giới hạn
    CALL validateLocationIndices(way1)
    CALL validateLocationIndices(way2)
    
    // Cập nhật khoảng cách
    route1.calculateDistance(locations)
    route2.calculateDistance(locations)
END
```

## Ba Pha Chính Của SHO

### 1. Pha Tìm Kiếm (Exploration)
- **Điều kiện:** |B| > 1
- **Hành động:** Thực hiện các toán tử ngẫu nhiên để khám phá không gian tìm kiếm
- **Mục đích:** Tránh rơi vào tối ưu cục bộ

### 2. Pha Bao Vây (Encircling)
- **Điều kiện:** |B| ≤ 1 và |E| < 1
- **Hành động:** Di chuyển về phía giải pháp tốt nhất
- **Mục đích:** Thu hẹp vùng tìm kiếm xung quanh giải pháp tốt

### 3. Pha Tấn Công (Attacking)
- **Điều kiện:** Kết hợp với pha bao vây
- **Hành động:** Khai thác cục bộ để tìm giải pháp tối ưu
- **Mục đích:** Tinh chỉnh giải pháp cuối cùng

## Đặc Điểm Nổi Bật

1. **Cơ chế phân cụm:** Chia quần thể thành các cụm nhỏ để tăng đa dạng
2. **Học hỏi thông minh:** Sử dụng vị trí tương đối để học từ giải pháp tốt nhất
3. **Toán tử đa tuyến:** Tối ưu hóa giữa các tuyến đường
4. **Cân bằng exploration-exploitation:** Điều chỉnh tự động qua tham số a

## Ứng Dụng
Thuật toán này đặc biệt hiệu quả cho:
- Vehicle Routing Problem with Time Windows (VRPTW)
- Pickup and Delivery Problem with Time Windows (PDPTW)
- Các bài toán tối ưu tổ hợp có ràng buộc phức tạp