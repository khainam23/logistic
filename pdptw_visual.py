import pandas as pd
import matplotlib.pyplot as plt
import seaborn as sns

# Load and clean the data
file_path = "20250731_182847_optimization_results.xlsx"
df = pd.read_excel(file_path, sheet_name="Optimization Results", skiprows=1)

# Rename columns for clarity
df.columns = ["Instance", "Algorithm", "NV_Min", "NV_Std", "NV_Mean",
              "TC_Min", "TC_Std", "TC_Mean", "Time_ms"]

# Drop rows with missing algorithm names (like header rows or empty lines)
df = df.dropna(subset=["Algorithm"])

# Convert numeric columns
num_cols = ["NV_Min", "NV_Std", "NV_Mean", "TC_Min", "TC_Std", "TC_Mean", "Time_ms"]
df[num_cols] = df[num_cols].apply(pd.to_numeric, errors='coerce')

# Plot 1: Total Cost (TC_Min) by Algorithm per Instance
plt.figure(figsize=(12, 6))
sns.barplot(data=df, x="Instance", y="TC_Min", hue="Algorithm")
plt.title("Total Cost (Min) by Algorithm per Instance")
plt.xticks(rotation=45)
plt.tight_layout()
plt.show()

# Plot 2: Number of Vehicles (NV_Min) by Algorithm per Instance
plt.figure(figsize=(12, 6))
sns.barplot(data=df, x="Instance", y="NV_Min", hue="Algorithm")
plt.title("Number of Vehicles (Min) by Algorithm per Instance")
plt.xticks(rotation=45)
plt.tight_layout()
plt.show()

# Plot 3: Time (ms) by Algorithm per Instance
plt.figure(figsize=(12, 6))
sns.barplot(data=df, x="Instance", y="Time_ms", hue="Algorithm")
plt.title("Execution Time (ms) by Algorithm per Instance")
plt.xticks(rotation=45)
plt.tight_layout()
plt.show()

# Plot 4: Heatmap - Total Cost Mean by Algorithm and Instance
plt.figure(figsize=(14, 8))
pivot_tc = df.pivot(index="Algorithm", columns="Instance", values="TC_Mean")
sns.heatmap(pivot_tc, annot=True, fmt='.1f', cmap='YlOrRd', cbar_kws={'label': 'Total Cost Mean'})
plt.title("Heatmap: Total Cost Mean by Algorithm and Instance")
plt.tight_layout()
plt.show()

# Plot 5: Heatmap - Number of Vehicles Mean by Algorithm and Instance
plt.figure(figsize=(14, 8))
pivot_nv = df.pivot(index="Algorithm", columns="Instance", values="NV_Mean")
sns.heatmap(pivot_nv, annot=True, fmt='.1f', cmap='Blues', cbar_kws={'label': 'Number of Vehicles Mean'})
plt.title("Heatmap: Number of Vehicles Mean by Algorithm and Instance")
plt.tight_layout()
plt.show()

# Plot 6: Box Plot - Total Cost Distribution by Algorithm
plt.figure(figsize=(12, 6))
df_melted_tc = df.melt(id_vars=['Algorithm', 'Instance'], 
                       value_vars=['TC_Min', 'TC_Mean'], 
                       var_name='Metric', value_name='Total_Cost')
sns.boxplot(data=df_melted_tc, x="Algorithm", y="Total_Cost", hue="Metric")
plt.title("Box Plot: Total Cost Distribution by Algorithm")
plt.xticks(rotation=45)
plt.tight_layout()
plt.show()

# Plot 7: Scatter Plot - Total Cost vs Number of Vehicles
plt.figure(figsize=(12, 8))
for algorithm in df['Algorithm'].unique():
    subset = df[df['Algorithm'] == algorithm]
    plt.scatter(subset['NV_Mean'], subset['TC_Mean'], 
               label=algorithm, alpha=0.7, s=100)
plt.xlabel('Number of Vehicles (Mean)')
plt.ylabel('Total Cost (Mean)')
plt.title('Scatter Plot: Total Cost vs Number of Vehicles by Algorithm')
plt.legend()
plt.grid(True, alpha=0.3)
plt.tight_layout()
plt.show()

# Plot 8: Performance Comparison - Normalized Metrics
plt.figure(figsize=(15, 10))

# Normalize metrics to 0-1 scale for comparison
df_norm = df.copy()
metrics_to_norm = ['TC_Mean', 'NV_Mean', 'Time_ms']
for metric in metrics_to_norm:
    df_norm[f'{metric}_norm'] = (df_norm[metric] - df_norm[metric].min()) / (df_norm[metric].max() - df_norm[metric].min())

# Create subplots
fig, axes = plt.subplots(2, 2, figsize=(15, 10))

# Normalized Total Cost
sns.barplot(data=df_norm, x="Algorithm", y="TC_Mean_norm", ax=axes[0,0])
axes[0,0].set_title('Normalized Total Cost Mean')
axes[0,0].tick_params(axis='x', rotation=45)

# Normalized Number of Vehicles
sns.barplot(data=df_norm, x="Algorithm", y="NV_Mean_norm", ax=axes[0,1])
axes[0,1].set_title('Normalized Number of Vehicles Mean')
axes[0,1].tick_params(axis='x', rotation=45)

# Normalized Execution Time
sns.barplot(data=df_norm, x="Algorithm", y="Time_ms_norm", ax=axes[1,0])
axes[1,0].set_title('Normalized Execution Time')
axes[1,0].tick_params(axis='x', rotation=45)

# Combined Performance Score (lower is better)
df_norm['Performance_Score'] = (df_norm['TC_Mean_norm'] + df_norm['NV_Mean_norm'] + df_norm['Time_ms_norm']) / 3
sns.barplot(data=df_norm, x="Algorithm", y="Performance_Score", ax=axes[1,1])
axes[1,1].set_title('Combined Performance Score (Lower is Better)')
axes[1,1].tick_params(axis='x', rotation=45)

plt.tight_layout()
plt.show()

# Plot 9: Violin Plot - Cost Variability by Algorithm
plt.figure(figsize=(12, 6))
df_violin = df.melt(id_vars=['Algorithm'], 
                    value_vars=['TC_Min', 'TC_Mean'], 
                    var_name='Metric', value_name='Cost')
sns.violinplot(data=df_violin, x="Algorithm", y="Cost", hue="Metric")
plt.title("Violin Plot: Cost Variability by Algorithm")
plt.xticks(rotation=45)
plt.tight_layout()
plt.show()

# Plot 10: Radar Chart - Algorithm Performance Comparison
import numpy as np

def create_radar_chart(df, algorithms):
    # Prepare data for radar chart
    categories = ['Total Cost', 'Num Vehicles', 'Execution Time']
    
    # Normalize metrics (invert so higher is better)
    tc_norm = 1 - (df.groupby('Algorithm')['TC_Mean'].mean() - df['TC_Mean'].min()) / (df['TC_Mean'].max() - df['TC_Mean'].min())
    nv_norm = 1 - (df.groupby('Algorithm')['NV_Mean'].mean() - df['NV_Mean'].min()) / (df['NV_Mean'].max() - df['NV_Mean'].min())
    time_norm = 1 - (df.groupby('Algorithm')['Time_ms'].mean() - df['Time_ms'].min()) / (df['Time_ms'].max() - df['Time_ms'].min())
    
    # Number of variables
    N = len(categories)
    
    # Angle for each axis
    angles = [n / float(N) * 2 * np.pi for n in range(N)]
    angles += angles[:1]  # Complete the circle
    
    # Create the plot
    fig, ax = plt.subplots(figsize=(10, 10), subplot_kw=dict(projection='polar'))
    
    colors = plt.cm.Set3(np.linspace(0, 1, len(algorithms)))
    
    for i, algorithm in enumerate(algorithms):
        values = [tc_norm[algorithm], nv_norm[algorithm], time_norm[algorithm]]
        values += values[:1]  # Complete the circle
        
        ax.plot(angles, values, 'o-', linewidth=2, label=algorithm, color=colors[i])
        ax.fill(angles, values, alpha=0.25, color=colors[i])
    
    # Add category labels
    ax.set_xticks(angles[:-1])
    ax.set_xticklabels(categories)
    ax.set_ylim(0, 1)
    ax.set_title("Radar Chart: Algorithm Performance Comparison\n(Higher values are better)", size=14, y=1.1)
    ax.legend(loc='upper right', bbox_to_anchor=(1.3, 1.0))
    ax.grid(True)
    
    plt.tight_layout()
    plt.show()

# Create radar chart
algorithms = df['Algorithm'].unique()
create_radar_chart(df, algorithms)

# Summary Statistics Table
print("\n" + "="*80)
print("BẢNG THỐNG KÊ TỔNG HỢP (SUMMARY STATISTICS)")
print("="*80)

summary_stats = df.groupby('Algorithm').agg({
    'TC_Mean': ['mean', 'std', 'min', 'max'],
    'NV_Mean': ['mean', 'std', 'min', 'max'],
    'Time_ms': ['mean', 'std', 'min', 'max']
}).round(2)

print(summary_stats)

# Best Performance Analysis
print("\n" + "="*80)
print("PHÂN TÍCH HIỆU SUẤT TỐT NHẤT (BEST PERFORMANCE ANALYSIS)")
print("="*80)

best_tc = df.loc[df['TC_Mean'].idxmin()]
best_nv = df.loc[df['NV_Mean'].idxmin()]
best_time = df.loc[df['Time_ms'].idxmin()]

print(f"Thuật toán tốt nhất về Total Cost: {best_tc['Algorithm']} (Instance: {best_tc['Instance']}, Cost: {best_tc['TC_Mean']:.2f})")
print(f"Thuật toán tốt nhất về Number of Vehicles: {best_nv['Algorithm']} (Instance: {best_nv['Instance']}, Vehicles: {best_nv['NV_Mean']:.2f})")
print(f"Thuật toán nhanh nhất: {best_time['Algorithm']} (Instance: {best_time['Instance']}, Time: {best_time['Time_ms']:.2f} ms)")

# Algorithm Ranking
print("\n" + "="*80)
print("XẾP HẠNG THUẬT TOÁN (ALGORITHM RANKING)")
print("="*80)

algo_performance = df.groupby('Algorithm').agg({
    'TC_Mean': 'mean',
    'NV_Mean': 'mean', 
    'Time_ms': 'mean'
}).round(2)

# Rank algorithms (1 = best)
algo_performance['TC_Rank'] = algo_performance['TC_Mean'].rank()
algo_performance['NV_Rank'] = algo_performance['NV_Mean'].rank()
algo_performance['Time_Rank'] = algo_performance['Time_ms'].rank()
algo_performance['Overall_Rank'] = (algo_performance['TC_Rank'] + algo_performance['NV_Rank'] + algo_performance['Time_Rank']) / 3

algo_performance = algo_performance.sort_values('Overall_Rank')
print(algo_performance)
