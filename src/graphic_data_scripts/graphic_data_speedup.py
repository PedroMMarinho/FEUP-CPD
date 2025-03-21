import pandas as pd
import matplotlib.pyplot as plt
import numpy as np
import seaborn as sns

# Set the style for better visuals
plt.style.use('ggplot')
sns.set_palette("colorblind")
plt.rcParams['font.family'] = 'sans-serif'
plt.rcParams['font.sans-serif'] = ['Arial', 'DejaVu Sans', 'Liberation Sans', 'Bitstream Vera Sans', 'sans-serif']
plt.rcParams['axes.labelsize'] = 12
plt.rcParams['axes.titlesize'] = 14
plt.rcParams['xtick.labelsize'] = 10
plt.rcParams['ytick.labelsize'] = 10
plt.rcParams['legend.fontsize'] = 10
plt.rcParams['figure.titlesize'] = 16

# Load the data
data = pd.read_csv("docs/data_cpp.csv")

# Filter to only keep the function types mentioned
function_types = ["Normal Mult", "Parallelized Normal Mult", "Inner Most Loop Parallelization"]
filtered_data = data[data['functionType'].isin(function_types)]

# Calculate speedup and efficiency
results = []

# Create a dictionary to store serial times by matrix size
serial_times_dict = {}
for _, row in filtered_data[filtered_data['functionType'] == 'Normal Mult'].iterrows():
    serial_times_dict[row['MatrixSize']] = row['Real Time']

# Process each row to calculate speedup and efficiency
for _, row in filtered_data.iterrows():
    matrix_size = row['MatrixSize']
    
    # Skip if we don't have a serial time for this matrix size
    if matrix_size not in serial_times_dict:
        continue
        
    serial_time = serial_times_dict[matrix_size]
    
    # For serial execution (Normal Mult), speedup is 1.0 and efficiency is 1.0
    if row['functionType'] == 'Normal Mult':
        speedup = 1.0
        efficiency = 1.0
    else:
        speedup = serial_time / row['Real Time']
        # Ensure NumThreads is not 0 to avoid division by zero
        if row['NumThreads'] > 0:
            efficiency = speedup / row['NumThreads']
        else:
            efficiency = 0.0
    
    results.append({
        'functionType': row['functionType'],
        'MatrixSize': float(matrix_size),
        'NumThreads': row['NumThreads'],
        'SerialTime': serial_time,
        'ParallelTime': row['Real Time'],
        'SpeedUp': speedup,
        'Efficiency': efficiency
    })

results_df = pd.DataFrame(results)

# Function to create better labels for the plots
def create_label(func_type):
    labels = {
        "Normal Mult": "Serial Execution",
        "Parallelized Normal Mult": "Outer Loop Parallelization",  # Corrected label
        "Inner Most Loop Parallelization": "Inner Loop Parallelization"
    }
    return labels.get(func_type, func_type)

# Define colors for consistency
colors = {
    "Normal Mult": "#1f77b4",
    "Parallelized Normal Mult": "#ff7f0e",
    "Inner Most Loop Parallelization": "#2ca02c"
}

# Create a comprehensive comparison bar chart
plt.figure(figsize=(16, 12))

# Prepare data for grouped bar chart
matrix_sizes = sorted(results_df['MatrixSize'].unique())
function_types = [ft for ft in results_df['functionType'].unique() if ft != "Normal Mult"]

# Set width of bar
barWidth = 0.35
r = np.arange(len(matrix_sizes))

# Plot SpeedUp bars
plt.subplot(2, 1, 1)
for i, func_type in enumerate(function_types):
    speedups = []
    for size in matrix_sizes:
        temp_df = results_df[(results_df['functionType'] == func_type) & (results_df['MatrixSize'] == size)]
        if not temp_df.empty:
            speedups.append(temp_df['SpeedUp'].values[0])
        else:
            speedups.append(0)
    
    bars = plt.bar(r + i*barWidth/len(function_types), speedups, width=barWidth/len(function_types), 
                   label=create_label(func_type), color=colors[func_type], edgecolor='black', linewidth=1.5)
    
    # Add value labels on top of bars
    for j, bar in enumerate(bars):
        height = bar.get_height()
        if height > 0:
            plt.text(bar.get_x() + bar.get_width()/2., height + 0.1,
                    f'{height:.2f}x', ha='center', va='bottom', fontweight='bold')

plt.xlabel('Matrix Size', fontweight='bold')
plt.ylabel('SpeedUp (Serial Time / Parallel Time)', fontweight='bold')
plt.title('SpeedUp Comparison by Implementation and Matrix Size', fontweight='bold', pad=20)
plt.xticks(r + barWidth/2 - barWidth/(2*len(function_types)), [str(int(size)) for size in matrix_sizes])
plt.legend(loc='upper right', frameon=True, fancybox=True, shadow=True)
plt.grid(axis='y', linestyle='--', alpha=0.7)

# Plot Efficiency bars
plt.subplot(2, 1, 2)
for i, func_type in enumerate(function_types):
    efficiencies = []
    for size in matrix_sizes:
        temp_df = results_df[(results_df['functionType'] == func_type) & (results_df['MatrixSize'] == size)]
        if not temp_df.empty:
            efficiencies.append(temp_df['Efficiency'].values[0])
        else:
            efficiencies.append(0)
    
    bars = plt.bar(r + i*barWidth/len(function_types), efficiencies, width=barWidth/len(function_types), 
                   label=create_label(func_type), color=colors[func_type], edgecolor='black', linewidth=1.5)
    
    # Add value labels on top of bars
    for j, bar in enumerate(bars):
        height = bar.get_height()
        if height > 0:
            plt.text(bar.get_x() + bar.get_width()/2., height + 0.02,
                    f'{height:.2f}', ha='center', va='bottom', fontweight='bold')

plt.xlabel('Matrix Size', fontweight='bold')
plt.ylabel('Efficiency (SpeedUp / NumThreads)', fontweight='bold')
plt.title('Parallel Efficiency Comparison by Implementation and Matrix Size', fontweight='bold', pad=20)
plt.xticks(r + barWidth/2 - barWidth/(2*len(function_types)), [str(int(size)) for size in matrix_sizes])
plt.legend(loc='upper right', frameon=True, fancybox=True, shadow=True)
plt.grid(axis='y', linestyle='--', alpha=0.7)

# Add an overall title
plt.suptitle("Performance Comparison of Parallelization Strategies", fontsize=18, fontweight='bold', y=0.98)

plt.tight_layout()
plt.subplots_adjust(hspace=0.3, top=0.92)
plt.savefig('parallel_performance_comparison.png', dpi=300, bbox_inches='tight')
plt.show()

# Create a summary table with clear function labels
# Average metrics by function type and matrix size
summary_df = results_df.pivot_table(
    index='functionType',
    values=['SpeedUp', 'Efficiency'],
    aggfunc='mean'
).round(3)

# Rename the index for better labeling
summary_df.index = summary_df.index.map(create_label)

print("\nAverage Performance Metrics Summary:")
print(summary_df)

# Save the results to a nicely formatted CSV with proper labels
results_df['functionType'] = results_df['functionType'].map(create_label)
results_df.to_csv('performance_metrics_results.csv', index=False, float_format='%.3f')



