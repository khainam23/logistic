#!/usr/bin/env python3
# -*- coding: utf-8 -*-

import os
from solve_sulution_nhh import read_data, assign_customers_to_vehicles, write_solution

def test_explicit_rcdp():
    """Test các file explicit rcdp có vấn đề"""
    
    test_files = [
        'data/vrpspdtw_Wang_Chen/src/explicit_rcdp0501.txt',
        'data/vrpspdtw_Wang_Chen/src/explicit_rcdp0504.txt', 
        'data/vrpspdtw_Wang_Chen/src/explicit_rcdp0507.txt'
    ]
    
    for filepath in test_files:
        print(f"\n{'='*60}")
        print(f"🔍 KIỂM TRA FILE: {os.path.basename(filepath)}")
        print(f"{'='*60}")
        
        try:
            # Đọc dữ liệu
            customers, vehicles = read_data(filepath)
            
            if not customers or not vehicles:
                print(f"❌ Không thể đọc dữ liệu từ {filepath}")
                continue
            
            print(f"📁 File: {os.path.basename(filepath)}")
            print(f"👥 Khách hàng: {len(customers)}")
            print(f"🚛 Xe có sẵn: {len(vehicles)}")
            print(f"📍 Depot: ({vehicles[0].current_location.x}, {vehicles[0].current_location.y})")
            print(f"💰 Dung lượng xe: {vehicles[0].capacity}")
            
            # In thông tin khách hàng
            print(f"\n📋 THÔNG TIN KHÁCH HÀNG:")
            for i, customer in enumerate(customers):
                cid = getattr(customer, 'id', getattr(customer, 'cid', i+1))
                print(f"   Customer {cid}: d_demand={customer.d_demand}, p_demand={customer.p_demand}, tw=[{customer.ready_time}, {customer.due_time}]")
            
            # Chạy thuật toán
            print(f"\n🔄 Chạy thuật toán...")
            vehicles_result = assign_customers_to_vehicles(customers, vehicles)
            
            # Kiểm tra kết quả
            assigned_customers = sum(1 for v in vehicles_result for _ in v.route[1:-1])
            used_vehicles = sum(1 for v in vehicles_result if len(v.route) > 2)
            
            print(f"\n📊 KẾT QUẢ:")
            print(f"   ✅ Khách hàng được gán: {assigned_customers}/{len(customers)} ({assigned_customers/len(customers)*100:.1f}%)")
            print(f"   🚛 Xe được sử dụng: {used_vehicles}/{len(vehicles)} ({used_vehicles/len(vehicles)*100:.1f}%)")
            
            # In chi tiết route
            print(f"\n🛣️ CHI TIẾT ROUTES:")
            for i, vehicle in enumerate(vehicles_result):
                if len(vehicle.route) > 2:  # Có khách hàng
                    route_str = " -> ".join(str(getattr(c, 'id', getattr(c, 'cid', 'depot'))) for c in vehicle.route)
                    print(f"   Vehicle {vehicle.id}: {route_str}")
            
            # Lưu kết quả
            output_file = f"exports/vrpspdtw_Wang_Chen/solution/{os.path.basename(filepath)}"
            write_solution(output_file, vehicles_result)
            print(f"💾 Đã lưu kết quả: {output_file}")
            
        except Exception as e:
            print(f"❌ Lỗi xử lý {filepath}: {e}")
            import traceback
            traceback.print_exc()

if __name__ == "__main__":
    test_explicit_rcdp()