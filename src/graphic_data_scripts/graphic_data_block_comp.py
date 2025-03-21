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
df = df[df["FunctionType"].isin(["Block Mult", "Inline Block Mult"])]

# Group by function type and matrix size for better visualization
grouped = df.groupby(["FunctionType", "MatrixSize", "BlockSize"]).agg({
    "L1_DCM": "mean",
    "L2_DCM": "mean", 
    "RealTime": "mean"
}).reset_index()

# Ensure RealTime column exists
if "RealTime" not in grouped.columns:
    raise ValueError("The 'RealTime' column is missing in the grouped DataFrame.")

# Normalize L1 and L2 Cache Misses
grouped["L1_DCM_Norm"] = grouped.groupby("FunctionType")["L1_DCM"].transform(
    lambda x: (x - x.min()) / (x.max() - x.min()) if x.max() > x.min() else 0
)
grouped["L2_DCM_Norm"] = grouped.groupby("FunctionType")["L2_DCM"].transform(
    lambda x: (x - x.min()) / (x.max() - x.min()) if x.max() > x.min() else 0
)

# Normalize RealTime across all algorithms together
real_time_max = grouped["RealTime"].max()
real_time_min = grouped["RealTime"].min()

# Ensure that the normalization of RealTime is applied
grouped["RealTime_Norm"] = (grouped["RealTime"] - real_time_min) / (real_time_max - real_time_min)

# Verify the new column
print(grouped.head())

# Filter data for Block Mult and Inline Block Mult algorithms only
block_mult = grouped[grouped["FunctionType"] == "Block Mult"].sort_values("MatrixSize")
inline_block_mult = grouped[grouped["FunctionType"] == "Inline Block Mult"].sort_values("MatrixSize")

# Separate data for different block sizes for both algorithms
block_sizes = grouped["BlockSize"].unique()

# Dark mode styling
background_color = '#121212'
axes_color = '#1E1E1E'
text_color = '#EEEEEE'
grid_color = '#333333'

# Line colors for different block sizes
colors = {
    64: '#4FC3F7',  # Light Blue
    128: '#FF8A65',  # Orange
    256: '#81C784',  # Green
    512: '#BA68C8'  # Purple
}

# Marker styles
markers = {
    64: "o",
    128: "s",
    256: "^",
    512: "d"
}

# ---- Log-Log Plot (Execution Time vs Matrix Size) ----
# Create the figure for the log-log plot
fig1, ax1 = plt.subplots(figsize=(12, 7), facecolor=background_color)

# Set axis background
ax1.set_facecolor(axes_color)

# Axis label and tick colors
ax1.tick_params(axis='x', colors=text_color)
ax1.tick_params(axis='y', colors=text_color)

# Axis labels
ax1.set_xlabel("Matrix Size", fontweight='bold', color=text_color)
ax1.set_ylabel("Execution Time (Seconds)", fontweight='bold', color=text_color)

# Grid styling
ax1.grid(True, color=grid_color, linestyle='--', alpha=0.5, which='both')

# Line styles
line_styles = ['-', '--', '-.', ':']

# Add small epsilon to avoid log(0)
epsilon = 1e-6

# Plot for Log-Log Scale (Execution Time vs Matrix Size)
for i, block_size in enumerate(block_sizes):
    # Block Mult data
    block_data = block_mult[block_mult["BlockSize"] == block_size]
    real_time_block = block_data["RealTime"].clip(lower=epsilon)
    ax1.plot(block_data["MatrixSize"], real_time_block,
            marker=markers[block_size],
            linestyle=line_styles[i % len(line_styles)],  # Reuse line styles if there are more block sizes than styles
            linewidth=2.5,
            markersize=8,
            color=colors[block_size],
            label=f"Block Mult - Block Size {block_size}")

    # Inline Block Mult data
    inline_block_data = inline_block_mult[inline_block_mult["BlockSize"] == block_size]
    real_time_inline_block = inline_block_data["RealTime"].clip(lower=epsilon)
    ax1.plot(inline_block_data["MatrixSize"], real_time_inline_block,
            marker=markers[block_size],
            linestyle=line_styles[(i + len(block_sizes)) % len(line_styles)],  # Reuse line styles
            linewidth=2.5,
            markersize=8,
            color=colors[block_size],
            label=f"Inline Block Mult - Block Size {block_size}")

# Set log scale for both x and y axes
ax1.set_yscale('log')

# Title for the first plot
ax1.set_title("Execution Time vs. Matrix Size (Log Scale)", 
              fontweight='bold', fontsize=15, color=text_color)

# Add legend
legend1 = ax1.legend(loc='upper left', frameon=True, framealpha=0.9, fontsize=10)

# Legend styling
legend1.get_frame().set_facecolor(axes_color)
legend1.get_frame().set_edgecolor('#444444')
legend1.get_frame().set_alpha(0.9)

# Legend text color
for text in legend1.get_texts():
    text.set_color(text_color)

# Save or display the plot for Log-Log
plt.tight_layout()
plt.savefig("log_log_plot_block_mult_vs_inline_block_mult.png", dpi=300, bbox_inches='tight')
plt.show()

# ---- Linear Normalized Plot (Execution Time vs Matrix Size) ----
# Create the figure for the normalized linear plot
fig2, ax2 = plt.subplots(figsize=(12, 7), facecolor=background_color)

# Set axis background
ax2.set_facecolor(axes_color)

# Axis label and tick colors
ax2.tick_params(axis='x', colors=text_color)
ax2.tick_params(axis='y', colors=text_color)

# Axis labels
ax2.set_xlabel("Matrix Size", fontweight='bold', color=text_color)
ax2.set_ylabel("Normalized Execution Time", fontweight='bold', color=text_color)

# Grid styling
ax2.grid(True, color=grid_color, linestyle='--', alpha=0.5, which='both')

# Line styles
line_styles = ['-', '--', '-.', ':']

# Plot for Normalized Linear Scale (Execution Time vs Matrix Size)
for i, block_size in enumerate(block_sizes):
    # Block Mult data
    block_data = block_mult[block_mult["BlockSize"] == block_size]
    real_time_block_norm = block_data["RealTime_Norm"]
    ax2.plot(block_data["MatrixSize"], real_time_block_norm,
            marker=markers[block_size],
            linestyle=line_styles[i % len(line_styles)],
            linewidth=2.5,
            markersize=8,
            color=colors[block_size],
            label=f"Block Mult - Block Size {block_size}")

    # Inline Block Mult data
    inline_block_data = inline_block_mult[inline_block_mult["BlockSize"] == block_size]
    real_time_inline_block_norm = inline_block_data["RealTime_Norm"]
    ax2.plot(inline_block_data["MatrixSize"], real_time_inline_block_norm,
            marker=markers[block_size],
            linestyle=line_styles[(i + len(block_sizes)) % len(line_styles)],
            linewidth=2.5,
            markersize=8,
            color=colors[block_size],
            label=f"Inline Block Mult - Block Size {block_size}")

# Set log scale for x-axis (Linear plot doesn't need log on y-axis)

# Title for the second plot
ax2.set_title("Execution Time vs. Matrix Size (Normalized Linear Scale)", 
              fontweight='bold', fontsize=15, color=text_color)

# Add legend
legend2 = ax2.legend(loc='upper left', frameon=True, framealpha=0.9, fontsize=10)

# Legend styling
legend2.get_frame().set_facecolor(axes_color)
legend2.get_frame().set_edgecolor('#444444')
legend2.get_frame().set_alpha(0.9)

# Legend text color
for text in legend2.get_texts():
    text.set_color(text_color)

# Save or display the plot for Normalized Linear
plt.tight_layout()
plt.savefig("normalized_linear_plot_block_mult_vs_inline_block_mult.png", dpi=300, bbox_inches='tight')
plt.show()
