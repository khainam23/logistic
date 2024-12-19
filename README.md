### Ký hiệu
- A: Class abstract
- I: Interface
- C: Config
- U: Util
- S: Security

### Thư viện
- Lombok: Chỉ định nhanh các giá trị thuộc tính bằng annotaiton

### Mô tả
VRPSPDTW là viết tắt của ***Vehicle Routing Problem with Simultaneous Pickup and Delivery and Time Windows***, 
tạm dịch là Bài toán định tuyến phương tiện với thu gom và giao hàng đồng thời trong các khung thời gian. 
Đây là một biến thể phức tạp của bài toán định tuyến phương tiện (Vehicle Routing Problem - VRP).
m-VRPSPDTW là biến thể nâng cao trong đó sẽ m chuyến xe thực hiện di chuyển 
#### Các thành phần chính
1. Simultaneous Pickup and Delivery (Thu gom và giao hàng đồng thời):
- Mỗi phương tiện không chỉ giao hàng từ kho đến các điểm khách hàng, mà còn thu gom hàng hóa từ các khách hàng và đưa về kho hoặc các điểm đích khác.
- Giao và thu gom diễn ra đồng thời, nghĩa là một phương tiện có thể vừa giao hàng vừa thu hàng tại cùng một điểm.
2. Time Windows (Khung thời gian):
- Mỗi khách hàng có một khoảng thời gian cố định (time window) mà trong đó phương tiện phải thực hiện việc giao hoặc thu hàng.
- Nếu phương tiện đến sớm hơn thời gian quy định, nó phải chờ đến khi khung thời gian bắt đầu. Nếu đến muộn, yêu cầu sẽ không được phục vụ.
#### Mục tiêu 
- Tối ưu hóa chi phí vận hành: Thông thường là giảm tổng quãng đường di chuyển hoặc chi phí liên quan đến nhiên liệu.
- Đảm bảo ràng buộc:
  + Năng lực tải trọng của phương tiện (không vượt quá khả năng giao và thu gom).
  + Các khung thời gian của khách hàng.
  + Lộ trình hợp lý để thực hiện cả hai nhiệm vụ (giao và thu) trong cùng một chuyến đi.
#### Thuật toán triển khai
##### Vị trí
Trong bài toán một điểm có thể giao và nhận, thì một vị trí phải được biểu diễn bởi 2 trạng thái:
- Giao hàng
- Nhận hàng

##### SHO (Spotted Hyena Optimizer)
Spotted Hyena Optimizer (SHO) là một thuật toán tối ưu hóa meta-heuristic lấy cảm hứng từ hành vi săn mồi và xã hội của linh cẩu đốm (Crocuta crocuta), một loài động vật có tổ chức xã hội phức tạp và chiến lược săn mồi hiệu quả. Thuật toán này được thiết kế để giải quyết các bài toán tối ưu hóa.
###### Kết quả cuối cùng (Output)
Một lộ trình cho biết thời gian sẽ di chuyển của các tuyến xe. Và mỗi tuyến xe sẽ di chuyển như thế nào để giảm thiểu chi phí nhất.