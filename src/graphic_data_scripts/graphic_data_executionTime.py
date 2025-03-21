import pandas as pd
import matplotlib.pyplot as plt
import seaborn as sns
import numpy as np

# Set visual style for better aesthetics
sns.set_style("whitegrid")
plt.rcParams['font.family'] = 'sans-serif'
plt.rcParams['font.sans-serif'] = ['Arial', 'Helvetica', 'DejaVu Sans']
plt.rcParams['axes.labelsize'] = 12
plt.rcParams['axes.titlesize'] = 14
plt.rcParams['xtick.labelsize'] = 10
plt.rcParams['ytick.labelsize'] = 10
plt.rcParams['legend.fontsize'] = 11

# Define column names
column_names = ["FunctionType", "L2 DCM", "L1 DCM", "MatrixSize", "BlockSize", "NumThreads", "RealTime"]

# Read CSV file
df = pd.read_csv("docs/data_cpp.csv", names=column_names, header=None)

# Convert relevant columns to numeric
df["L1_DCM"] = pd.to_numeric(df["L1 DCM"], errors="coerce")
df["L2_DCM"] = pd.to_numeric(df["L2 DCM"], errors="coerce")
df["MatrixSize"] = pd.to_numeric(df["MatrixSize"], errors="coerce")
df["RealTime"] = pd.to_numeric(df["RealTime"], errors="coerce")
df["BlockSize"] = pd.to_numeric(df["BlockSize"], errors="coerce")

# Filter for only the relevant function types
df = df[df["FunctionType"].isin(["Normal Mult", "Inline Mult", "Block Mult", "Inline Block Mult"])]

# Group by function type and matrix size for better visualization
grouped = df.groupby(["FunctionType", "MatrixSize"]).agg({
    "L1_DCM": "mean",
    "L2_DCM": "mean", 
    "RealTime": "mean"
}).reset_index()

# Normalize L1 and L2 Cache Misses
grouped["L1_DCM_Norm"] = grouped.groupby("FunctionType")["L1_DCM"].transform(
    lambda x: (x - x.min()) / (x.max() - x.min()) if x.max() > x.min() else 0
)
grouped["L2_DCM_Norm"] = grouped.groupby("FunctionType")["L2_DCM"].transform(
    lambda x: (x - x.min()) / (x.max() - x.min()) if x.max() > x.min() else 0
)

# Separate data by function type
normal_mult = grouped[grouped["FunctionType"] == "Normal Mult"].sort_values("MatrixSize")
inline_mult = grouped[grouped["FunctionType"] == "Inline Mult"].sort_values("MatrixSize")
block_mult = grouped[grouped["FunctionType"] == "Block Mult"].sort_values("MatrixSize")
inline_block_mult = grouped[grouped["FunctionType"] == "Inline Block Mult"].sort_values("MatrixSize")

# Dark mode styling
background_color = '#121212'
axes_color = '#1E1E1E'
text_color = '#EEEEEE'
grid_color = '#333333'

# Line colors for functions
colors = {
    "Normal Mult": '#4FC3F7',       # Light Blue
    "Inline Mult": '#FF8A65',       # Orange
    "Block Mult": '#81C784',        # Green
    "Inline Block Mult": '#BA68C8'  # Purple
}

# Marker styles
markers = {
    "Normal Mult": "o",
    "Inline Mult": "s",
    "Block Mult": "^",
    "Inline Block Mult": "d"
}

# Create the figure
fig, ax = plt.subplots(figsize=(12, 7), facecolor=background_color)

# Set axis background
ax.set_facecolor(axes_color)

# Axis label and tick colors
ax.tick_params(axis='x', colors=text_color)
ax.tick_params(axis='y', colors=text_color)

# Axis labels
ax.set_xlabel("Matrix Size", fontweight='bold', color=text_color)
ax.set_ylabel("Execution Time (Seconds)", fontweight='bold', color=text_color)

# Grid styling
ax.grid(True, color=grid_color, linestyle='--', alpha=0.5, which='both')

# Line styles
line_styles = ['-', '--', '-.', ':']

# Add small epsilon to avoid log(0)
epsilon = 1e-6

# 1. Log-Log Scale Plot (for Execution Time vs Matrix Size)
def plot_log_log():
    for i, (func_type, data) in enumerate([
        ("Normal Mult", normal_mult),
        ("Inline Mult", inline_mult),
        ("Block Mult", block_mult),
        ("Inline Block Mult", inline_block_mult)
    ]):
        real_time_raw = data["RealTime"].clip(lower=epsilon)
        
        ax.plot(data["MatrixSize"], real_time_raw,
                marker=markers[func_type],
                linestyle=line_styles[i],
                linewidth=2.5,
                markersize=8,
                color=colors[func_type],
                label=f"{func_type}")
    
    # Set log-log scale
    ax.set_yscale('log')
    
    # Title
    plt.title("Execution Time vs. Matrix Size (Log Scale)", 
              fontweight='bold', fontsize=15, color=text_color)

# 2. Normalized Linear Scale Plot (for Execution Time vs Matrix Size)
def plot_normalized_linear():
    # Normalize Execution Time globally across all function types
    all_real_times = pd.concat([
        normal_mult["RealTime"],
        inline_mult["RealTime"],
        block_mult["RealTime"],
        inline_block_mult["RealTime"]
    ])

    # Find global min and max of all execution times
    global_min = all_real_times.min()
    global_max = all_real_times.max()

    # Normalize Execution Time across all function types
    def normalize(real_time_series):
        return (real_time_series - global_min) / (global_max - global_min) if global_max != global_min else 0
    
    for i, (func_type, data) in enumerate([
        ("Normal Mult", normal_mult),
        ("Inline Mult", inline_mult),
        ("Block Mult", block_mult),
        ("Inline Block Mult", inline_block_mult)
    ]):
        real_time_raw = data["RealTime"]
        
        # Normalize the Execution Time values
        real_time_norm = normalize(real_time_raw)
        
        ax.plot(data["MatrixSize"], real_time_norm,
                marker=markers[func_type],
                linestyle=line_styles[i],
                linewidth=2.5,
                markersize=8,
                color=colors[func_type],
                label=f"{func_type}")
    
    # Title for the normalized linear plot
    plt.title("Normalized Execution Time vs. Matrix Size (Log Scale)", 
              fontweight='bold', fontsize=15, color=text_color)


# Choose the plot type you want
# Call the desired function to plot:

# For Log-Log Scale Plot
plot_log_log()


# Uncomment the following line if you want the Normalized Linear Plot
#plot_normalized_linear()

# Create the legend
legend = ax.legend(loc='upper left', frameon=True, framealpha=0.9, fontsize=10)

# Legend styling
legend.get_frame().set_facecolor(axes_color)
legend.get_frame().set_edgecolor('#444444')
legend.get_frame().set_alpha(0.9)

# Legend text color
for text in legend.get_texts():
    text.set_color(text_color)

# Save or display the plot
plt.tight_layout()
plt.savefig("execution_time_vs_matrix_size_log.png", dpi=300, bbox_inches='tight')
plt.show()