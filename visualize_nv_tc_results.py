import pandas as pd
import matplotlib.pyplot as plt
import seaborn as sns
import numpy as np
from matplotlib.patches import Rectangle
import warnings
warnings.filterwarnings('ignore')

# Thiết lập style cho matplotlib
plt.style.use('seaborn-v0_8')
sns.set_palette("husl")

class NV_TC_ResultsVisualizer:
    """
    Lớp trực quan hóa kết quả tối ưu hóa tập trung vào 2 tiêu chí: NV (Số xe) và TC (Chi phí tổng)
    """
    
    def __init__(self, excel_file="20250731_155756_optimization_results.xlsx"):
        """
        Khởi tạo và đọc dữ liệu từ file Excel chỉ có NV và TC
        """
        self.excel_file = excel_file
        self.df_raw = None
        self.df_clean = None
        self.load_data()
        
    def load_data(self):
        """
        Đọc và xử lý dữ liệu từ file Excel với cấu trúc multi-level headers
        """
        try:
            # Đọc file Excel với header đa cấp
            self.df_raw = pd.read_excel(self.excel_file, sheet_name=0, header=[0, 1])
            
            # Làm phẳng multi-level columns
            self.df_raw.columns = ['_'.join(col).strip() if col[1] != '' else col[0] 
                                  for col in self.df_raw.columns.values]
            
            # Tạo bản sao và làm sạch dữ liệu
            self.df_clean = self.df_raw.copy()
            
            # Đổi tên cột để dễ sử dụng
            column_mapping = {}
            for col in self.df_clean.columns:
                if 'Instance' in col:
                    column_mapping[col] = 'Instance'
                elif 'Algorithm' in col:
                    column_mapping[col] = 'Algorithm'
                elif 'NV_Min' in col or ('NV' in col and 'Min' in col):
                    column_mapping[col] = 'NV_Min'
                elif 'NV_Std' in col or ('NV' in col and 'Std' in col):
                    column_mapping[col] = 'NV_Std'
                elif 'NV_Mean' in col or ('NV' in col and 'Mean' in col):
                    column_mapping[col] = 'NV_Mean'
                elif 'TC_Min' in col or ('TC' in col and 'Min' in col):
                    column_mapping[col] = 'TC_Min'
                elif 'TC_Std' in col or ('TC' in col and 'Std' in col):
                    column_mapping[col] = 'TC_Std'
                elif 'TC_Mean' in col or ('TC' in col and 'Mean' in col):
                    column_mapping[col] = 'TC_Mean'
                elif 'Time' in col:
                    column_mapping[col] = 'Time_ms'
            
            self.df_clean = self.df_clean.rename(columns=column_mapping)
            
            # Loại bỏ các dòng trống hoặc header phụ
            self.df_clean = self.df_clean.dropna(subset=['Instance', 'Algorithm'])
            
            # Ép kiểu số cho các cột số
            numeric_columns = ['NV_Min', 'NV_Std', 'NV_Mean', 'TC_Min', 'TC_Std', 'TC_Mean', 'Time_ms']
            for col in numeric_columns:
                if col in self.df_clean.columns:
                    # Xử lý dấu phẩy trong số (nếu có)
                    if self.df_clean[col].dtype == 'object':
                        self.df_clean[col] = self.df_clean[col].astype(str).str.replace(',', '.')
                    self.df_clean[col] = pd.to_numeric(self.df_clean[col], errors='coerce')
            
            # Loại bỏ các dòng có giá trị NaN trong các cột quan trọng
            self.df_clean = self.df_clean.dropna(subset=['NV_Mean', 'TC_Mean'])
            
            print(f"✅ Đã tải thành công {len(self.df_clean)} dòng dữ liệu")
            print(f"📊 Các instance: {self.df_clean['Instance'].unique()}")
            print(f"🔧 Các thuật toán: {self.df_clean['Algorithm'].unique()}")
            print(f"📋 Các cột dữ liệu: {list(self.df_clean.columns)}")
            
        except Exception as e:
            print(f"❌ Lỗi khi đọc file: {e}")
            # Thử phương pháp đọc đơn giản hơn
            try:
                print("🔄 Đang thử phương pháp đọc khác...")
                self.df_raw = pd.read_excel(self.excel_file, sheet_name=0)
                self.df_clean = self.df_raw.copy()
                
                # Đặt tên cột thủ công dựa trên cấu trúc mô tả
                expected_columns = ['Instance', 'Algorithm', 'NV_Min', 'NV_Std', 'NV_Mean', 
                                  'TC_Min', 'TC_Std', 'TC_Mean', 'Time_ms']
                
                if len(self.df_clean.columns) == len(expected_columns):
                    self.df_clean.columns = expected_columns
                else:
                    print(f"⚠️ Số cột không khớp. Có {len(self.df_clean.columns)} cột, mong đợi {len(expected_columns)}")
                    print(f"Các cột hiện tại: {list(self.df_clean.columns)}")
                
                # Xử lý dữ liệu
                numeric_columns = ['NV_Min', 'NV_Std', 'NV_Mean', 'TC_Min', 'TC_Std', 'TC_Mean', 'Time_ms']
                for col in numeric_columns:
                    if col in self.df_clean.columns:
                        if self.df_clean[col].dtype == 'object':
                            self.df_clean[col] = self.df_clean[col].astype(str).str.replace(',', '.')
                        self.df_clean[col] = pd.to_numeric(self.df_clean[col], errors='coerce')
                
                self.df_clean = self.df_clean.dropna(subset=['Instance', 'Algorithm'])
                print(f"✅ Đã tải thành công {len(self.df_clean)} dòng dữ liệu (phương pháp 2)")
                
            except Exception as e2:
                print(f"❌ Lỗi phương pháp 2: {e2}")
                self.df_clean = pd.DataFrame()  # Tạo DataFrame rỗng
    
    def check_data_availability(self):
        """
        Kiểm tra tính khả dụng của dữ liệu
        """
        if self.df_clean is None or self.df_clean.empty:
            print("❌ Không có dữ liệu để xử lý!")
            return False
        
        required_columns = ['NV_Mean', 'TC_Mean']
        missing_columns = [col for col in required_columns if col not in self.df_clean.columns]
        
        if missing_columns:
            print(f"❌ Thiếu các cột quan trọng: {missing_columns}")
            return False
        
        return True
    
    def plot_nv_tc_comparison(self, instance=None, save_fig=True):
        """
        So sánh NV và TC cho một instance cụ thể hoặc tất cả
        """
        if not self.check_data_availability():
            return
            
        if instance:
            subset = self.df_clean[self.df_clean['Instance'] == instance]
            title_suffix = f"cho {instance}"
        else:
            subset = self.df_clean
            title_suffix = "cho tất cả instances"
            
        if subset.empty:
            print(f"❌ Không tìm thấy dữ liệu cho instance: {instance}")
            return
            
        fig, axes = plt.subplots(2, 2, figsize=(15, 12))
        fig.suptitle(f'🚛💰 So sánh NV và TC {title_suffix}', fontsize=16, fontweight='bold')
        
        # 1. Biểu đồ cột so sánh NV
        ax1 = axes[0, 0]
        nv_data = subset.groupby('Algorithm')['NV_Mean'].mean().sort_values()
        bars1 = ax1.bar(range(len(nv_data)), nv_data.values, 
                       color=sns.color_palette("Blues_r", len(nv_data)))
        ax1.set_title('🚛 Số xe trung bình theo thuật toán')
        ax1.set_xlabel('Thuật toán')
        ax1.set_ylabel('Số xe trung bình')
        ax1.set_xticks(range(len(nv_data)))
        ax1.set_xticklabels(nv_data.index, rotation=45)
        
        # Thêm giá trị lên cột
        for i, bar in enumerate(bars1):
            height = bar.get_height()
            ax1.text(bar.get_x() + bar.get_width()/2., height + height*0.01,
                    f'{height:.1f}', ha='center', va='bottom')
        
        # 2. Biểu đồ cột so sánh TC
        ax2 = axes[0, 1]
        tc_data = subset.groupby('Algorithm')['TC_Mean'].mean().sort_values()
        bars2 = ax2.bar(range(len(tc_data)), tc_data.values, 
                       color=sns.color_palette("Reds_r", len(tc_data)))
        ax2.set_title('💰 Chi phí tổng trung bình theo thuật toán')
        ax2.set_xlabel('Thuật toán')
        ax2.set_ylabel('Chi phí tổng trung bình')
        ax2.set_xticks(range(len(tc_data)))
        ax2.set_xticklabels(tc_data.index, rotation=45)
        
        # Thêm giá trị lên cột
        for i, bar in enumerate(bars2):
            height = bar.get_height()
            ax2.text(bar.get_x() + bar.get_width()/2., height + height*0.01,
                    f'{height:.0f}', ha='center', va='bottom')
        
        # 3. Scatter plot: NV vs TC
        ax3 = axes[1, 0]
        for alg in subset['Algorithm'].unique():
            alg_data = subset[subset['Algorithm'] == alg]
            ax3.scatter(alg_data['NV_Mean'], alg_data['TC_Mean'], 
                       label=alg, alpha=0.7, s=80)
        ax3.set_title('📊 Mối quan hệ giữa Số xe và Chi phí')
        ax3.set_xlabel('Số xe trung bình')
        ax3.set_ylabel('Chi phí tổng trung bình')
        ax3.legend()
        ax3.grid(True, alpha=0.3)
        
        # 4. Heatmap tương quan NV-TC
        ax4 = axes[1, 1]
        if 'NV_Mean' in subset.columns and 'TC_Mean' in subset.columns:
            corr_data = subset[['NV_Mean', 'TC_Mean']].corr()
            sns.heatmap(corr_data, annot=True, cmap='coolwarm', center=0, ax=ax4,
                       square=True, linewidths=0.5)
            ax4.set_title('🔥 Ma trận tương quan NV-TC')
        
        plt.tight_layout()
        
        if save_fig:
            filename = f"nv_tc_comparison_{instance if instance else 'all'}.png"
            plt.savefig(filename, dpi=300, bbox_inches='tight')
            print(f"💾 Đã lưu biểu đồ: {filename}")
        
        plt.show()
    
    def plot_algorithm_nv_tc_performance(self, save_fig=True):
        """
        Phân tích hiệu suất NV và TC của các thuật toán
        """
        if not self.check_data_availability():
            return
        fig, axes = plt.subplots(2, 3, figsize=(18, 12))
        fig.suptitle('🚀 Phân tích hiệu suất NV-TC theo thuật toán', fontsize=16, fontweight='bold')
        
        # 1. Boxplot NV theo thuật toán
        ax1 = axes[0, 0]
        sns.boxplot(data=self.df_clean, x='Algorithm', y='NV_Mean', ax=ax1)
        ax1.set_title('📦 Phân phối số xe theo thuật toán')
        ax1.set_xlabel('Thuật toán')
        ax1.set_ylabel('Số xe')
        ax1.tick_params(axis='x', rotation=45)
        
        # 2. Boxplot TC theo thuật toán
        ax2 = axes[0, 1]
        sns.boxplot(data=self.df_clean, x='Algorithm', y='TC_Mean', ax=ax2)
        ax2.set_title('📦 Phân phối chi phí theo thuật toán')
        ax2.set_xlabel('Thuật toán')
        ax2.set_ylabel('Chi phí tổng')
        ax2.tick_params(axis='x', rotation=45)
        
        # 3. Violin plot kết hợp NV và TC
        ax3 = axes[0, 2]
        melted_data = self.df_clean.melt(id_vars=['Algorithm', 'Instance'], 
                                        value_vars=['NV_Mean', 'TC_Mean'],
                                        var_name='Metric', value_name='Value')
        sns.violinplot(data=melted_data, x='Algorithm', y='Value', hue='Metric', ax=ax3)
        ax3.set_title('🎻 Phân phối mật độ NV-TC')
        ax3.set_xlabel('Thuật toán')
        ax3.set_ylabel('Giá trị')
        ax3.tick_params(axis='x', rotation=45)
        
        # 4. Scatter plot với size theo NV, color theo TC
        ax4 = axes[1, 0]
        scatter = ax4.scatter(self.df_clean['NV_Mean'], self.df_clean['TC_Mean'], 
                            c=self.df_clean['TC_Mean'], s=self.df_clean['NV_Mean']*20, 
                            alpha=0.6, cmap='viridis')
        ax4.set_title('🎯 NV vs TC (Kích thước = NV, Màu = TC)')
        ax4.set_xlabel('Số xe trung bình')
        ax4.set_ylabel('Chi phí tổng trung bình')
        plt.colorbar(scatter, ax=ax4, label='Chi phí tổng')
        
        # 5. Hiệu quả tổng hợp NV-TC
        ax5 = axes[1, 1]
        # Chuẩn hóa NV và TC (lower is better)
        normalized_data = self.df_clean.copy()
        normalized_data['NV_norm'] = (normalized_data['NV_Mean'] - normalized_data['NV_Mean'].min()) / \
                                    (normalized_data['NV_Mean'].max() - normalized_data['NV_Mean'].min())
        normalized_data['TC_norm'] = (normalized_data['TC_Mean'] - normalized_data['TC_Mean'].min()) / \
                                    (normalized_data['TC_Mean'].max() - normalized_data['TC_Mean'].min())
        
        # Điểm hiệu quả tổng hợp (trọng số bằng nhau)
        normalized_data['Efficiency_Score'] = (normalized_data['NV_norm'] + normalized_data['TC_norm']) / 2
        
        efficiency = normalized_data.groupby('Algorithm')['Efficiency_Score'].mean().sort_values()
        bars5 = ax5.bar(range(len(efficiency)), efficiency.values, 
                       color=sns.color_palette("rocket", len(efficiency)))
        ax5.set_title('🎯 Điểm hiệu quả NV-TC tổng hợp')
        ax5.set_xlabel('Thuật toán')
        ax5.set_ylabel('Điểm hiệu quả (thấp hơn = tốt hơn)')
        ax5.set_xticks(range(len(efficiency)))
        ax5.set_xticklabels(efficiency.index, rotation=45)
        
        # Thêm giá trị lên cột
        for i, bar in enumerate(bars5):
            height = bar.get_height()
            ax5.text(bar.get_x() + bar.get_width()/2., height + height*0.01,
                    f'{height:.3f}', ha='center', va='bottom')
        
        # 6. Radar chart cho thuật toán tốt nhất
        ax6 = axes[1, 2]
        best_alg = efficiency.index[0]  # Thuật toán có điểm hiệu quả thấp nhất
        best_data = self.df_clean[self.df_clean['Algorithm'] == best_alg]
        
        # Tính các thống kê cho radar chart
        categories = ['NV_Mean', 'NV_Min', 'TC_Mean', 'TC_Min']
        values = []
        for cat in categories:
            if cat in best_data.columns:
                values.append(best_data[cat].mean())
            else:
                values.append(0)
        
        # Chuẩn hóa giá trị cho radar chart
        max_vals = []
        for cat in categories:
            if cat in self.df_clean.columns:
                max_vals.append(self.df_clean[cat].max())
            else:
                max_vals.append(1)
        
        normalized_values = [val/max_val if max_val != 0 else 0 for val, max_val in zip(values, max_vals)]
        
        angles = np.linspace(0, 2*np.pi, len(categories), endpoint=False).tolist()
        normalized_values += normalized_values[:1]  # Đóng vòng tròn
        angles += angles[:1]
        
        ax6.plot(angles, normalized_values, 'o-', linewidth=2, label=best_alg)
        ax6.fill(angles, normalized_values, alpha=0.25)
        ax6.set_xticks(angles[:-1])
        ax6.set_xticklabels(categories)
        ax6.set_title(f'🎯 Profile thuật toán tốt nhất: {best_alg}')
        ax6.grid(True)
        ax6.set_ylim(0, 1)
        
        plt.tight_layout()
        
        if save_fig:
            filename = "nv_tc_algorithm_performance.png"
            plt.savefig(filename, dpi=300, bbox_inches='tight')
            print(f"💾 Đã lưu biểu đồ: {filename}")
        
        plt.show()
    
    def plot_instance_nv_tc_analysis(self, save_fig=True):
        """
        Phân tích NV và TC theo từng instance
        """
        if not self.check_data_availability():
            return
        instances = self.df_clean['Instance'].unique()
        
        fig, axes = plt.subplots(2, 2, figsize=(16, 12))
        fig.suptitle('📋 Phân tích NV-TC theo Instance', fontsize=16, fontweight='bold')
        
        # 1. Heatmap NV theo instance và thuật toán
        ax1 = axes[0, 0]
        pivot_nv = self.df_clean.pivot_table(values='NV_Mean', index='Instance', 
                                            columns='Algorithm', aggfunc='mean')
        sns.heatmap(pivot_nv, annot=True, fmt='.1f', cmap='Blues', ax=ax1)
        ax1.set_title('🚛 Số xe trung bình theo Instance & Thuật toán')
        
        # 2. Heatmap TC theo instance và thuật toán
        ax2 = axes[0, 1]
        pivot_tc = self.df_clean.pivot_table(values='TC_Mean', index='Instance', 
                                           columns='Algorithm', aggfunc='mean')
        sns.heatmap(pivot_tc, annot=True, fmt='.0f', cmap='Reds', ax=ax2)
        ax2.set_title('💰 Chi phí tổng theo Instance & Thuật toán')
        
        # 3. Biểu đồ cột nhóm - NV và TC theo instance
        ax3 = axes[1, 0]
        instance_stats = self.df_clean.groupby('Instance').agg({
            'NV_Mean': 'mean',
            'TC_Mean': 'mean'
        })
        
        x = np.arange(len(instances))
        width = 0.35
        
        # Chuẩn hóa TC để hiển thị cùng với NV
        tc_normalized = instance_stats['TC_Mean'] / instance_stats['TC_Mean'].max() * instance_stats['NV_Mean'].max()
        
        bars1 = ax3.bar(x - width/2, instance_stats['NV_Mean'], width, 
                       label='Số xe TB', color='skyblue')
        bars2 = ax3.bar(x + width/2, tc_normalized, width, 
                       label='Chi phí TB (chuẩn hóa)', color='lightcoral')
        
        ax3.set_title('📊 So sánh NV và TC theo Instance')
        ax3.set_xlabel('Instance')
        ax3.set_ylabel('Giá trị')
        ax3.set_xticks(x)
        ax3.set_xticklabels(instances, rotation=45)
        ax3.legend()
        
        # 4. Scatter plot: Độ khó instance dựa trên NV-TC
        ax4 = axes[1, 1]
        instance_difficulty = self.df_clean.groupby('Instance').agg({
            'NV_Mean': 'mean',
            'TC_Mean': 'mean'
        })
        
        scatter = ax4.scatter(instance_difficulty['NV_Mean'], instance_difficulty['TC_Mean'], 
                            s=100, alpha=0.7, c=range(len(instance_difficulty)), 
                            cmap='viridis')
        
        # Thêm label cho từng điểm
        for i, instance in enumerate(instance_difficulty.index):
            ax4.annotate(instance, 
                        (instance_difficulty.iloc[i]['NV_Mean'], 
                         instance_difficulty.iloc[i]['TC_Mean']),
                        xytext=(5, 5), textcoords='offset points', fontsize=9)
        
        ax4.set_title('🎯 Độ khó Instance (NV vs TC)')
        ax4.set_xlabel('Số xe trung bình')
        ax4.set_ylabel('Chi phí tổng trung bình')
        ax4.grid(True, alpha=0.3)
        
        plt.tight_layout()
        
        if save_fig:
            filename = "nv_tc_instance_analysis.png"
            plt.savefig(filename, dpi=300, bbox_inches='tight')
            print(f"💾 Đã lưu biểu đồ: {filename}")
        
        plt.show()
    
    def generate_nv_tc_summary_report(self):
        """
        Tạo báo cáo tổng hợp cho NV và TC
        """
        if not self.check_data_availability():
            return
        print("="*60)
        print("🚛💰 BÁO CÁO TỔNG HỢP KẾT QUẢ NV-TC")
        print("="*60)
        
        print(f"\n📈 THỐNG KÊ TỔNG QUAN:")
        print(f"   • Tổng số thí nghiệm: {len(self.df_clean)}")
        print(f"   • Số instance: {len(self.df_clean['Instance'].unique())}")
        print(f"   • Số thuật toán: {len(self.df_clean['Algorithm'].unique())}")
        
        print(f"\n🏆 THUẬT TOÁN TỐT NHẤT THEO TỪNG CHỈ SỐ:")
        
        # Số xe ít nhất
        best_nv = self.df_clean.loc[self.df_clean['NV_Mean'].idxmin()]
        print(f"   • Số xe ít nhất: {best_nv['Algorithm']} ({best_nv['NV_Mean']:.1f}) - Instance: {best_nv['Instance']}")
        
        # Chi phí thấp nhất
        best_tc = self.df_clean.loc[self.df_clean['TC_Mean'].idxmin()]
        print(f"   • Chi phí thấp nhất: {best_tc['Algorithm']} ({best_tc['TC_Mean']:.2f}) - Instance: {best_tc['Instance']}")
        
        print(f"\n📊 THỐNG KÊ THEO THUẬT TOÁN:")
        alg_stats = self.df_clean.groupby('Algorithm').agg({
            'NV_Mean': ['mean', 'std', 'min', 'max'],
            'TC_Mean': ['mean', 'std', 'min', 'max']
        }).round(2)
        
        for alg in self.df_clean['Algorithm'].unique():
            print(f"\n   🔧 {alg}:")
            print(f"      - Số xe TB: {alg_stats.loc[alg, ('NV_Mean', 'mean')]:.1f} ± {alg_stats.loc[alg, ('NV_Mean', 'std')]:.1f}")
            print(f"      - Chi phí TB: {alg_stats.loc[alg, ('TC_Mean', 'mean')]:.2f} ± {alg_stats.loc[alg, ('TC_Mean', 'std')]:.2f}")
        
        print(f"\n📋 THỐNG KÊ THEO INSTANCE:")
        instance_stats = self.df_clean.groupby('Instance').agg({
            'NV_Mean': ['mean', 'std'],
            'TC_Mean': ['mean', 'std']
        }).round(2)
        
        for instance in self.df_clean['Instance'].unique():
            print(f"\n   📄 {instance}:")
            print(f"      - Số xe TB: {instance_stats.loc[instance, ('NV_Mean', 'mean')]:.1f} ± {instance_stats.loc[instance, ('NV_Mean', 'std')]:.1f}")
            print(f"      - Chi phí TB: {instance_stats.loc[instance, ('TC_Mean', 'mean')]:.2f} ± {instance_stats.loc[instance, ('TC_Mean', 'std')]:.2f}")
        
        # Tương quan NV-TC
        correlation = self.df_clean['NV_Mean'].corr(self.df_clean['TC_Mean'])
        print(f"\n🔗 TƯƠNG QUAN NV-TC: {correlation:.3f}")
        if correlation > 0.7:
            print("   → Tương quan dương mạnh: Số xe tăng thì chi phí tăng")
        elif correlation > 0.3:
            print("   → Tương quan dương trung bình")
        elif correlation > -0.3:
            print("   → Tương quan yếu")
        else:
            print("   → Tương quan âm: Số xe tăng thì chi phí giảm")
        
        print("\n" + "="*60)
    
    def create_nv_tc_dashboard(self):
        """
        Tạo dashboard tổng hợp cho NV và TC
        """
        print("🚀 Đang tạo dashboard NV-TC...")
        
        # Tạo báo cáo tổng hợp
        self.generate_nv_tc_summary_report()
        
        # Tạo các biểu đồ
        print("\n📊 Đang tạo biểu đồ phân tích hiệu suất NV-TC...")
        self.plot_algorithm_nv_tc_performance()
        
        print("\n📋 Đang tạo biểu đồ phân tích NV-TC theo instance...")
        self.plot_instance_nv_tc_analysis()
        
        print("\n📈 Đang tạo biểu đồ so sánh NV-TC...")
        self.plot_nv_tc_comparison()
        
        print("\n✅ Hoàn thành tạo dashboard NV-TC!")

# Sử dụng lớp NV_TC_ResultsVisualizer
if __name__ == "__main__":
    # Khởi tạo visualizer
    visualizer = NV_TC_ResultsVisualizer()
    
    # Tạo dashboard tổng hợp NV-TC
    visualizer.create_nv_tc_dashboard()
    
    # Ví dụ sử dụng các hàm riêng lẻ
    # visualizer.plot_nv_tc_comparison("c101.txt")        # So sánh NV-TC cho instance cụ thể
    # visualizer.plot_algorithm_nv_tc_performance()       # Phân tích hiệu suất NV-TC thuật toán
    # visualizer.plot_instance_nv_tc_analysis()           # Phân tích NV-TC theo instance