import pandas as pd
import matplotlib.pyplot as plt
import numpy as np
import seaborn as sns

# ===============================
# DARK THEME SETUP
# ===============================
plt.style.use('dark_background')
sns.set_palette("colorblind")

plt.rcParams.update({
    'axes.facecolor': '#111111',      # dark background for plots
    'figure.facecolor': '#111111',    # dark background for figure
    'axes.edgecolor': 'white',
    'axes.labelcolor': 'white',
    'xtick.color': 'white',
    'ytick.color': 'white',
    'text.color': 'white',
    'axes.grid': True,
    'grid.color': '#333333',
    'axes.labelsize': 12,
    'axes.titlesize': 14,
    'xtick.labelsize': 10,
    'ytick.labelsize': 10,
    'legend.fontsize': 10,
    'figure.titlesize': 16,
    'legend.edgecolor': 'white'
})

# Load the data
data = pd.read_csv("docs/data_cpp.csv")

# Filter to only keep the function types we want to compare
function_types = ["Inner Most Loop Parallelization Inline", "Parallelized Inline Mult","Inline Mult"]
filtered_data = data[data['functionType'].isin(function_types)]

# Calculate speedup, efficiency, and MFLOPS
results = []

serial_times_inline = {}
serial_times_normal = {}

for _, row in data[data['functionType'] == 'Inline Mult'].iterrows():
    serial_times_inline[row['MatrixSize']] = row['Real Time']

for _, row in data[data['functionType'] == 'Normal Mult'].iterrows():
    serial_times_normal[row['MatrixSize']] = row['Real Time']

for _, row in filtered_data.iterrows():
    matrix_size = row['MatrixSize']
    parallel_time = row['Real Time']  # seconds
    
    if "Inline" in row['functionType'] and matrix_size not in serial_times_inline:
        continue
    elif "Normal Mult" in row['functionType'] and matrix_size not in serial_times_normal:
        continue
    
    if "Inline" in row['functionType']:
        serial_time = serial_times_inline[matrix_size]
        serial_type = "Inline Mult"
    else:
        serial_time = serial_times_normal[matrix_size]
        serial_type = "Normal Mult"
    
    if row['functionType'] == serial_type and row['NumThreads'] == 1:
        speedup = 1.0
        efficiency = 1.0
    else:
        speedup = serial_time / parallel_time
        efficiency = speedup / row['NumThreads'] if row['NumThreads'] > 0 else 0.0
    
    # Calculate MFLOPS
    flops = 2 * (matrix_size ** 3)
    mflops = (flops / parallel_time) / 1e6

    results.append({
        'functionType': row['functionType'],
        'MatrixSize': float(matrix_size),
        'NumThreads': row['NumThreads'],
        'SerialTime': serial_time,
        'SerialType': serial_type,
        'ParallelTime': parallel_time,
        'SpeedUp': speedup,
        'Efficiency': efficiency,
        'MFLOPS': mflops
    })

results_df = pd.DataFrame(results)

def create_label(func_type):
    labels = {
        "Parallelized Inline Mult": "Outer Loop Parallelization (V1)",
        "Inner Most Loop Parallelization Inline": "Inner Loop Parallelization (V2)"
    }
    return labels.get(func_type, func_type)

# Colors by thread count (customized for dark theme)
num_threads = len(results_df['NumThreads'].unique())
thread_colors = sns.color_palette("bright", num_threads)

# ==============================================
# GROUPING: Calculate the mean across repetitions
# ==============================================
aggregated_results_df = results_df.groupby(
    ['MatrixSize', 'NumThreads', 'functionType']
).agg({
    'SpeedUp': 'mean',
    'Efficiency': 'mean',
    'MFLOPS': 'mean'
}).reset_index()

# ==============================================
# LINE GRAPH: SPEEDUP, EFFICIENCY & MFLOPS
# ==============================================
function_types_to_plot = ["Inner Most Loop Parallelization Inline", "Parallelized Inline Mult"]
metrics = ['SpeedUp', 'Efficiency', 'MFLOPS']

for metric in metrics:
    plt.figure(figsize=(16, 6 * len(function_types_to_plot)))

    for idx, func_type in enumerate(function_types_to_plot, 1):
        plt.subplot(len(function_types_to_plot), 1, idx)

        func_df = aggregated_results_df[aggregated_results_df['functionType'] == func_type]

        for i, thread_count in enumerate(sorted(func_df['NumThreads'].unique())):
            thread_df = func_df[func_df['NumThreads'] == thread_count].sort_values(by='MatrixSize')

            plt.plot(thread_df['MatrixSize'], thread_df[metric],
                     marker='o', linestyle='-', linewidth=2, markersize=6,
                     label=f"{thread_count} Threads", color=thread_colors[i])

        plt.xlabel('Matrix Size', fontweight='bold')
        plt.ylabel(metric, fontweight='bold')
        plt.title(f'{metric} vs Matrix Size for {create_label(func_type)}',
                  fontweight='bold', pad=20)
        plt.legend(title='Num Threads', loc='best', frameon=True, fancybox=True, shadow=True)
        plt.grid(axis='both', linestyle='--', alpha=0.5)

    # Make the suptitle more distant from the plots below
    plt.suptitle(f"{metric} Comparison Across Matrix Sizes and Thread Counts",
                 fontsize=18, fontweight='bold', y=1.02)

    plt.tight_layout()
    plt.subplots_adjust(hspace=0.4, top=0.9)

    plt.savefig(f'{metric.lower()}_comparison_line_graph_dark.png', dpi=300, bbox_inches='tight')
    plt.show()

# ========================
# SUMMARY TABLE (Optional)
# ========================
summary_df = results_df[results_df['functionType'].isin(function_types_to_plot)].groupby(
    ['NumThreads', 'functionType']
).agg({
    'SpeedUp': 'mean',
    'Efficiency': 'mean',
    'MFLOPS': 'mean'
}).round(3)

summary_df.index = summary_df.index.set_levels(
    summary_df.index.levels[1].map(create_label), level=1
)

print("\nAverage Performance Metrics by Thread Count:")
print(summary_df)

# Save results to CSV
results_df['functionType'] = results_df['functionType'].map(create_label)
results_df.to_csv('inner_inline_vs_normal_parallel_metrics_with_mflops_dark.csv', index=False, float_format='%.3f')
