
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

class OptimizationResultsVisualizer:
    """
    Lớp trực quan hóa kết quả tối ưu hóa với nhiều loại biểu đồ khác nhau
    """
    
    def __init__(self, excel_file="20250731_152032_optimization_results.xlsx"):
        """
        Khởi tạo và đọc dữ liệu từ file Excel
        """
        self.excel_file = excel_file
        self.df_raw = None
        self.df_clean = None
        self.load_data()
        
    def load_data(self):
        """
        Đọc và xử lý dữ liệu từ file Excel
        """
        try:
            # Đọc file Excel
            self.df_raw = pd.read_excel(self.excel_file, sheet_name="Optimization Results")
            
            # Tạo header từ dòng đầu tiên và làm sạch dữ liệu
            self.df_clean = self.df_raw[1:].copy()
            self.df_clean.columns = ['Instance', 'Algorithm', 'NV_Min', 'NV_Std', 'NV_Mean',
                                   'TC_Min', 'TC_Std', 'TC_Mean',
                                   'SD_Min', 'SD_Std', 'SD_Mean',
                                   'WT_Min', 'WT_Std', 'WT_Mean',
                                   'Time_ms']
            
            # Ép kiểu số cho các cột số
            numeric_columns = self.df_clean.columns.drop(['Instance', 'Algorithm'])
            self.df_clean[numeric_columns] = self.df_clean[numeric_columns].apply(pd.to_numeric, errors='coerce')
            
            # Loại bỏ các dòng có giá trị NaN
            self.df_clean = self.df_clean.dropna()
            
            print(f"✅ Đã tải thành công {len(self.df_clean)} dòng dữ liệu")
            print(f"📊 Các instance: {self.df_clean['Instance'].unique()}")
            print(f"🔧 Các thuật toán: {self.df_clean['Algorithm'].unique()}")
            
        except Exception as e:
            print(f"❌ Lỗi khi đọc file: {e}")
    
    def plot_metrics_comparison(self, instance=None, save_fig=True):
        """
        So sánh các chỉ số cho một instance cụ thể hoặc tất cả
        """
        if instance:
            subset = self.df_clean[self.df_clean['Instance'] == instance]
            title_suffix = f"cho {instance}"
        else:
            subset = self.df_clean
            title_suffix = "cho tất cả instances"
            
        if subset.empty:
            print(f"❌ Không tìm thấy dữ liệu cho instance: {instance}")
            return
            
        metrics = ['TC_Mean', 'WT_Mean', 'Time_ms']
        melted = subset.melt(id_vars=['Algorithm', 'Instance'], value_vars=metrics,
                           var_name='Metric', value_name='Value')
        
        fig, axes = plt.subplots(2, 2, figsize=(15, 12))
        fig.suptitle(f'📊 So sánh các chỉ số tối ưu hóa {title_suffix}', fontsize=16, fontweight='bold')
        
        # Biểu đồ cột nhóm
        ax1 = axes[0, 0]
        sns.barplot(data=melted, x='Algorithm', y='Value', hue='Metric', ax=ax1)
        ax1.set_title('📈 So sánh theo thuật toán')
        ax1.set_xlabel('Thuật toán')
        ax1.set_ylabel('Giá trị')
        ax1.tick_params(axis='x', rotation=45)
        
        # Biểu đồ boxplot
        ax2 = axes[0, 1]
        sns.boxplot(data=melted, x='Metric', y='Value', hue='Algorithm', ax=ax2)
        ax2.set_title('📦 Phân phối giá trị theo chỉ số')
        ax2.set_xlabel('Chỉ số')
        ax2.set_ylabel('Giá trị')
        
        # Biểu đồ violin
        ax3 = axes[1, 0]
        sns.violinplot(data=melted, x='Algorithm', y='Value', hue='Metric', ax=ax3)
        ax3.set_title('🎻 Phân phối mật độ')
        ax3.set_xlabel('Thuật toán')
        ax3.set_ylabel('Giá trị')
        ax3.tick_params(axis='x', rotation=45)
        
        # Heatmap correlation
        ax4 = axes[1, 1]
        if instance:
            corr_data = subset[['TC_Mean', 'WT_Mean', 'Time_ms', 'NV_Mean']].corr()
        else:
            corr_data = self.df_clean[['TC_Mean', 'WT_Mean', 'Time_ms', 'NV_Mean']].corr()
        sns.heatmap(corr_data, annot=True, cmap='coolwarm', center=0, ax=ax4)
        ax4.set_title('🔥 Ma trận tương quan')
        
        plt.tight_layout()
        
        if save_fig:
            filename = f"metrics_comparison_{instance if instance else 'all'}.png"
            plt.savefig(filename, dpi=300, bbox_inches='tight')
            print(f"💾 Đã lưu biểu đồ: {filename}")
        
        plt.show()
    
    def plot_algorithm_performance(self, save_fig=True):
        """
        Phân tích hiệu suất tổng thể của các thuật toán
        """
        fig, axes = plt.subplots(2, 3, figsize=(18, 12))
        fig.suptitle('🚀 Phân tích hiệu suất thuật toán tối ưu hóa', fontsize=16, fontweight='bold')
        
        # 1. Thời gian thực thi trung bình
        ax1 = axes[0, 0]
        avg_time = self.df_clean.groupby('Algorithm')['Time_ms'].mean().sort_values()
        bars1 = ax1.bar(range(len(avg_time)), avg_time.values, color=sns.color_palette("viridis", len(avg_time)))
        ax1.set_title('⏱️ Thời gian thực thi TB')
        ax1.set_xlabel('Thuật toán')
        ax1.set_ylabel('Thời gian (ms)')
        ax1.set_xticks(range(len(avg_time)))
        ax1.set_xticklabels(avg_time.index, rotation=45)
        
        # Thêm giá trị lên cột
        for i, bar in enumerate(bars1):
            height = bar.get_height()
            ax1.text(bar.get_x() + bar.get_width()/2., height + height*0.01,
                    f'{height:.0f}', ha='center', va='bottom')
        
        # 2. Chi phí tổng trung bình
        ax2 = axes[0, 1]
        avg_cost = self.df_clean.groupby('Algorithm')['TC_Mean'].mean().sort_values()
        bars2 = ax2.bar(range(len(avg_cost)), avg_cost.values, color=sns.color_palette("plasma", len(avg_cost)))
        ax2.set_title('💰 Chi phí tổng TB')
        ax2.set_xlabel('Thuật toán')
        ax2.set_ylabel('Chi phí')
        ax2.set_xticks(range(len(avg_cost)))
        ax2.set_xticklabels(avg_cost.index, rotation=45)
        
        # 3. Số xe trung bình
        ax3 = axes[0, 2]
        avg_vehicles = self.df_clean.groupby('Algorithm')['NV_Mean'].mean().sort_values()
        bars3 = ax3.bar(range(len(avg_vehicles)), avg_vehicles.values, color=sns.color_palette("crest", len(avg_vehicles)))
        ax3.set_title('🚛 Số xe TB')
        ax3.set_xlabel('Thuật toán')
        ax3.set_ylabel('Số xe')
        ax3.set_xticks(range(len(avg_vehicles)))
        ax3.set_xticklabels(avg_vehicles.index, rotation=45)
        
        # 4. Scatter plot: Chi phí vs Thời gian
        ax4 = axes[1, 0]
        for alg in self.df_clean['Algorithm'].unique():
            alg_data = self.df_clean[self.df_clean['Algorithm'] == alg]
            ax4.scatter(alg_data['Time_ms'], alg_data['TC_Mean'], label=alg, alpha=0.7, s=60)
        ax4.set_title('⚡ Chi phí vs Thời gian')
        ax4.set_xlabel('Thời gian (ms)')
        ax4.set_ylabel('Chi phí tổng')
        ax4.legend()
        ax4.grid(True, alpha=0.3)
        
        # 5. Hiệu quả tổng hợp (lower is better)
        ax5 = axes[1, 1]
        # Chuẩn hóa các chỉ số (0-1) và tính điểm tổng hợp
        normalized_data = self.df_clean.copy()
        for col in ['TC_Mean', 'Time_ms', 'NV_Mean']:
            normalized_data[f'{col}_norm'] = (normalized_data[col] - normalized_data[col].min()) / (normalized_data[col].max() - normalized_data[col].min())
        
        normalized_data['Efficiency_Score'] = (normalized_data['TC_Mean_norm'] + 
                                             normalized_data['Time_ms_norm'] + 
                                             normalized_data['NV_Mean_norm']) / 3
        
        efficiency = normalized_data.groupby('Algorithm')['Efficiency_Score'].mean().sort_values()
        bars5 = ax5.bar(range(len(efficiency)), efficiency.values, color=sns.color_palette("rocket", len(efficiency)))
        ax5.set_title('🎯 Điểm hiệu quả tổng hợp')
        ax5.set_xlabel('Thuật toán')
        ax5.set_ylabel('Điểm hiệu quả (thấp hơn = tốt hơn)')
        ax5.set_xticks(range(len(efficiency)))
        ax5.set_xticklabels(efficiency.index, rotation=45)
        
        # 6. Radar chart cho thuật toán tốt nhất
        ax6 = axes[1, 2]
        best_alg = efficiency.index[0]  # Thuật toán có điểm hiệu quả thấp nhất
        best_data = self.df_clean[self.df_clean['Algorithm'] == best_alg][['TC_Mean', 'Time_ms', 'NV_Mean', 'WT_Mean']].mean()
        
        categories = ['TC_Mean', 'Time_ms', 'NV_Mean', 'WT_Mean']
        values = [best_data[cat] for cat in categories]
        
        # Chuẩn hóa giá trị cho radar chart
        max_vals = [self.df_clean[cat].max() for cat in categories]
        normalized_values = [val/max_val for val, max_val in zip(values, max_vals)]
        
        angles = np.linspace(0, 2*np.pi, len(categories), endpoint=False).tolist()
        normalized_values += normalized_values[:1]  # Đóng vòng tròn
        angles += angles[:1]
        
        ax6.plot(angles, normalized_values, 'o-', linewidth=2, label=best_alg)
        ax6.fill(angles, normalized_values, alpha=0.25)
        ax6.set_xticks(angles[:-1])
        ax6.set_xticklabels(categories)
        ax6.set_title(f'🎯 Profile thuật toán tốt nhất: {best_alg}')
        ax6.grid(True)
        
        plt.tight_layout()
        
        if save_fig:
            filename = "algorithm_performance_analysis.png"
            plt.savefig(filename, dpi=300, bbox_inches='tight')
            print(f"💾 Đã lưu biểu đồ: {filename}")
        
        plt.show()
    
    def plot_instance_analysis(self, save_fig=True):
        """
        Phân tích hiệu suất theo từng instance
        """
        instances = self.df_clean['Instance'].unique()
        n_instances = len(instances)
        
        fig, axes = plt.subplots(2, 2, figsize=(16, 12))
        fig.suptitle('📋 Phân tích hiệu suất theo Instance', fontsize=16, fontweight='bold')
        
        # 1. Heatmap chi phí theo instance và thuật toán
        ax1 = axes[0, 0]
        pivot_cost = self.df_clean.pivot_table(values='TC_Mean', index='Instance', columns='Algorithm', aggfunc='mean')
        sns.heatmap(pivot_cost, annot=True, fmt='.0f', cmap='YlOrRd', ax=ax1)
        ax1.set_title('🔥 Chi phí tổng theo Instance & Thuật toán')
        
        # 2. Heatmap thời gian thực thi
        ax2 = axes[0, 1]
        pivot_time = self.df_clean.pivot_table(values='Time_ms', index='Instance', columns='Algorithm', aggfunc='mean')
        sns.heatmap(pivot_time, annot=True, fmt='.0f', cmap='Blues', ax=ax2)
        ax2.set_title('⏱️ Thời gian thực thi theo Instance & Thuật toán')
        
        # 3. Biểu đồ cột xếp chồng - số xe
        ax3 = axes[1, 0]
        pivot_vehicles = self.df_clean.pivot_table(values='NV_Mean', index='Instance', columns='Algorithm', aggfunc='mean')
        pivot_vehicles.plot(kind='bar', stacked=False, ax=ax3, width=0.8)
        ax3.set_title('🚛 Số xe trung bình theo Instance')
        ax3.set_xlabel('Instance')
        ax3.set_ylabel('Số xe')
        ax3.legend(title='Thuật toán', bbox_to_anchor=(1.05, 1), loc='upper left')
        ax3.tick_params(axis='x', rotation=45)
        
        # 4. Scatter plot: Độ khó của instance
        ax4 = axes[1, 1]
        instance_difficulty = self.df_clean.groupby('Instance').agg({
            'TC_Mean': 'mean',
            'Time_ms': 'mean',
            'NV_Mean': 'mean'
        })
        
        scatter = ax4.scatter(instance_difficulty['Time_ms'], instance_difficulty['TC_Mean'], 
                            s=instance_difficulty['NV_Mean']*50, alpha=0.6, c=range(len(instance_difficulty)), 
                            cmap='viridis')
        
        # Thêm label cho từng điểm
        for i, instance in enumerate(instance_difficulty.index):
            ax4.annotate(instance, (instance_difficulty.iloc[i]['Time_ms'], instance_difficulty.iloc[i]['TC_Mean']),
                        xytext=(5, 5), textcoords='offset points', fontsize=8)
        
        ax4.set_title('🎯 Độ khó Instance (Kích thước = Số xe)')
        ax4.set_xlabel('Thời gian TB (ms)')
        ax4.set_ylabel('Chi phí TB')
        ax4.grid(True, alpha=0.3)
        
        plt.tight_layout()
        
        if save_fig:
            filename = "instance_analysis.png"
            plt.savefig(filename, dpi=300, bbox_inches='tight')
            print(f"💾 Đã lưu biểu đồ: {filename}")
        
        plt.show()
    
    def generate_summary_report(self):
        """
        Tạo báo cáo tổng hợp
        """
        print("="*60)
        print("📊 BÁO CÁO TỔNG HỢP KẾT QUẢ TỐI ƯU HÓA")
        print("="*60)
        
        print(f"\n📈 THỐNG KÊ TỔNG QUAN:")
        print(f"   • Tổng số thí nghiệm: {len(self.df_clean)}")
        print(f"   • Số instance: {len(self.df_clean['Instance'].unique())}")
        print(f"   • Số thuật toán: {len(self.df_clean['Algorithm'].unique())}")
        
        print(f"\n🏆 THUẬT TOÁN TỐT NHẤT THEO TỪNG CHỈ SỐ:")
        
        # Chi phí thấp nhất
        best_cost = self.df_clean.loc[self.df_clean['TC_Mean'].idxmin()]
        print(f"   • Chi phí thấp nhất: {best_cost['Algorithm']} ({best_cost['TC_Mean']:.2f}) - Instance: {best_cost['Instance']}")
        
        # Thời gian nhanh nhất
        best_time = self.df_clean.loc[self.df_clean['Time_ms'].idxmin()]
        print(f"   • Thời gian nhanh nhất: {best_time['Algorithm']} ({best_time['Time_ms']:.0f}ms) - Instance: {best_time['Instance']}")
        
        # Số xe ít nhất
        best_vehicles = self.df_clean.loc[self.df_clean['NV_Mean'].idxmin()]
        print(f"   • Số xe ít nhất: {best_vehicles['Algorithm']} ({best_vehicles['NV_Mean']:.1f}) - Instance: {best_vehicles['Instance']}")
        
        print(f"\n📊 THỐNG KÊ THEO THUẬT TOÁN:")
        alg_stats = self.df_clean.groupby('Algorithm').agg({
            'TC_Mean': ['mean', 'std', 'min', 'max'],
            'Time_ms': ['mean', 'std', 'min', 'max'],
            'NV_Mean': ['mean', 'std', 'min', 'max']
        }).round(2)
        
        for alg in self.df_clean['Algorithm'].unique():
            print(f"\n   🔧 {alg}:")
            print(f"      - Chi phí TB: {alg_stats.loc[alg, ('TC_Mean', 'mean')]:.2f} ± {alg_stats.loc[alg, ('TC_Mean', 'std')]:.2f}")
            print(f"      - Thời gian TB: {alg_stats.loc[alg, ('Time_ms', 'mean')]:.0f} ± {alg_stats.loc[alg, ('Time_ms', 'std')]:.0f} ms")
            print(f"      - Số xe TB: {alg_stats.loc[alg, ('NV_Mean', 'mean')]:.1f} ± {alg_stats.loc[alg, ('NV_Mean', 'std')]:.1f}")
        
        print("\n" + "="*60)
    
    def create_comprehensive_dashboard(self):
        """
        Tạo dashboard tổng hợp với tất cả các biểu đồ
        """
        print("🚀 Đang tạo dashboard tổng hợp...")
        
        # Tạo báo cáo tổng hợp
        self.generate_summary_report()
        
        # Tạo các biểu đồ
        print("\n📊 Đang tạo biểu đồ phân tích hiệu suất thuật toán...")
        self.plot_algorithm_performance()
        
        print("\n📋 Đang tạo biểu đồ phân tích theo instance...")
        self.plot_instance_analysis()
        
        print("\n📈 Đang tạo biểu đồ so sánh chỉ số...")
        self.plot_metrics_comparison()
        
        print("\n✅ Hoàn thành tạo dashboard!")

# Sử dụng lớp OptimizationResultsVisualizer
if __name__ == "__main__":
    # Khởi tạo visualizer
    visualizer = OptimizationResultsVisualizer()
    
    # Tạo dashboard tổng hợp
    visualizer.create_comprehensive_dashboard()
    
    # Ví dụ sử dụng các hàm riêng lẻ
    # visualizer.plot_metrics_comparison("c101.txt")  # So sánh cho instance cụ thể
    # visualizer.plot_algorithm_performance()          # Phân tích hiệu suất thuật toán
    # visualizer.plot_instance_analysis()              # Phân tích theo instance
