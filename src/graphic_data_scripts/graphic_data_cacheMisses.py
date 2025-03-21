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

# Custom color palette
colors = {"Normal Mult": "#1f77b4", "Inline Mult": "#d62728", "Block Mult": "#2ca02c", "Inline Block Mult": "#ff7f0e"}
markers = {"Normal Mult": "o", "Inline Mult": "s", "Block Mult": "^", "Inline Block Mult": "d"}

# 1. Plot L1 DCM Normalized vs Matrix Size with connected lines
plt.figure(figsize=(10, 6))

# Plot with both points and lines
for func_type, data in [("Normal Mult", normal_mult), ("Inline Mult", inline_mult), ("Block Mult", block_mult), ("Inline Block Mult", inline_block_mult)]:
    plt.plot(data["MatrixSize"], data["L1_DCM_Norm"], 
             marker=markers[func_type], 
             linestyle='-', 
             linewidth=2.5, 
             markersize=8,
             color=colors[func_type], 
             label=func_type)
    

plt.xlabel("Matrix Size", fontweight='bold')
plt.ylabel("Normalized L1 Cache Misses", fontweight='bold')
plt.title("Normalized L1 Cache Misses vs. Matrix Size", fontweight='bold', fontsize=15)
plt.legend(title="Function Type", title_fontsize=12, loc='best', frameon=True, framealpha=0.9)
plt.grid(True, alpha=0.5)

# Add a subtle background color
plt.gca().set_facecolor("#f9f9f9")

# Add a box around the plot
plt.box(True)

# Save the figure with high resolution
plt.tight_layout()
plt.savefig("l1_cache_misses_vs_matrix_size.png", dpi=300, bbox_inches='tight')
plt.close()

# 2. Plot L2 DCM Normalized vs Matrix Size with connected lines
plt.figure(figsize=(10, 6))

# Plot with both points and lines
for func_type, data in [("Normal Mult", normal_mult), ("Inline Mult", inline_mult), ("Block Mult", block_mult), ("Inline Block Mult", inline_block_mult)]:
    plt.plot(data["MatrixSize"], data["L2_DCM_Norm"], 
             marker=markers[func_type], 
             linestyle='-', 
             linewidth=2.5, 
             markersize=8,
             color=colors[func_type], 
             label=func_type)

plt.xlabel("Matrix Size", fontweight='bold')
plt.ylabel("Normalized L2 Cache Misses", fontweight='bold')
plt.title("Normalized L2 Cache Misses vs. Matrix Size", fontweight='bold', fontsize=15)
plt.legend(title="Function Type", title_fontsize=12, loc='best', frameon=True, framealpha=0.9)
plt.grid(True, alpha=0.5)

# Add a subtle background color
plt.gca().set_facecolor("#f9f9f9")

# Add a box around the plot
plt.box(True)

# Save the figure with high resolution
plt.tight_layout()
plt.savefig("l2_cache_misses_vs_matrix_size.png", dpi=300, bbox_inches='tight')
plt.close()

min_l1 = grouped["L1_DCM"].min()
max_l1 = grouped["L1_DCM"].max()
grouped["L1_DCM_Norm_Global"] = (grouped["L1_DCM"] - min_l1) / (max_l1 - min_l1) if max_l1 > min_l1 else 0

min_l2 = grouped["L2_DCM"].min()
max_l2 = grouped["L2_DCM"].max()
grouped["L2_DCM_Norm_Global"] = (grouped["L2_DCM"] - min_l2) / (max_l2 - min_l2) if max_l2 > min_l2 else 0

# 3. Plot Real Time vs L1 & L2 Cache Misses with distinction by function type
plt.figure(figsize=(12, 7))

# Define color schemes for function types
colors_funcs = {
    "Normal Mult": "#1f77b4",  # Blue
    "Inline Mult": "#ff7f0e",  # Orange
    "Block Mult": "#2ca02c",   # Green
    "Inline Block Mult": "#9467bd"  # Purple
}
markers_funcs = {
    "Normal Mult": "o",
    "Inline Mult": "s",
    "Block Mult": "^",
    "Inline Block Mult": "d"
}

# Transparency levels for cache misses
alpha_levels = {
    "L1 Cache Misses": 0.55,  # Slightly more transparent
    "L2 Cache Misses": 0.8    # Less transparent
}

# Plot for each function type with distinct colors
for metric, y_col in [("L1 Cache Misses", "L1_DCM_Norm_Global"), ("L2 Cache Misses", "L2_DCM_Norm_Global")]:
    for func, marker in markers_funcs.items():
        data = grouped[grouped["FunctionType"] == func]
        
        plt.plot(data["RealTime"], data[y_col], 
                 marker=marker,
                 linestyle='-' if func == "Normal Mult" else '--',
                 linewidth=2.5,
                 markersize=9,
                 alpha=alpha_levels[metric],
                 color=colors_funcs[func],
                 label=f"{func} - {metric}")

plt.xlabel("Execution Time (seconds)", fontweight='bold')
plt.ylabel("Normalized Cache Misses", fontweight='bold')
plt.title("Relationship Between Execution Time and Cache Misses", fontweight='bold', fontsize=15)
plt.legend(title="Function & Cache Type", title_fontsize=12, loc='best', frameon=True, framealpha=0.9)
plt.grid(True, alpha=0.5)

# Adjust the plot appearance
plt.gca().set_facecolor("#f9f9f9")
plt.box(True)

# Add some padding to the x-axis to make room for annotations
x_min, x_max = plt.xlim()
plt.xlim(x_min, x_max * 1.05)

# Save the figure with high resolution
plt.tight_layout()
plt.savefig("real_time_vs_cache_misses.png", dpi=300, bbox_inches='tight')


# Create a figure and dual y-axes in dark mode
fig, ax1 = plt.subplots(figsize=(12, 7), facecolor='#121212')
ax2 = ax1.twinx()

# Dark mode styling
background_color = '#121212'
axes_color = '#1E1E1E'
text_color = '#EEEEEE'
grid_color = '#333333'

# Set figure and axes background
fig.patch.set_facecolor(background_color)
ax1.set_facecolor(axes_color)
ax2.set_facecolor(axes_color)

ax1.tick_params(axis='x', colors=text_color)
ax1.tick_params(axis='y', colors=text_color)
ax2.tick_params(axis='y', colors=text_color)

ax1.xaxis.label.set_color(text_color)
ax1.yaxis.label.set_color('#4FC3F7')  # L1 color
ax2.yaxis.label.set_color('#FF8A65')  # L2 color

ax1.grid(True, color=grid_color, linestyle='--', alpha=0.5, which='both')

# Normalization across all L1 and L2 data (combined normalization)
all_l1 = pd.concat([normal_mult["L1_DCM"], inline_mult["L1_DCM"], block_mult["L1_DCM"], inline_block_mult["L1_DCM"]])
all_l2 = pd.concat([normal_mult["L2_DCM"], inline_mult["L2_DCM"], block_mult["L2_DCM"], inline_block_mult["L2_DCM"]])

epsilon = 1e-6
min_l1 = all_l1.min()
max_l1 = all_l1.max()
min_l2 = all_l2.min()
max_l2 = all_l2.max()

# Plot normalized L1 and L2 cache misses per function type
for i, (func_type, data) in enumerate([
    ("Normal Mult", normal_mult),
    ("Inline Mult", inline_mult),
    ("Block Mult", block_mult),
    ("Inline Block Mult", inline_block_mult)
]):
    l1_norm = (data["L1_DCM"] - min_l1) / (max_l1 - min_l1 + epsilon)
    l2_norm = (data["L2_DCM"] - min_l2) / (max_l2 - min_l2 + epsilon)

    ax1.plot(data["MatrixSize"], l1_norm,
             marker=markers[func_type],
             linestyle='-', linewidth=2.5, markersize=8,
             color=colors[func_type],
             label=f"{func_type} - L1 DCM")

    ax2.plot(data["MatrixSize"], l2_norm,
             marker=markers[func_type],
             linestyle='--', linewidth=2.5, markersize=8,
             color=colors[func_type],
             alpha=0.6,
             label=f"{func_type} - L2 DCM")

# Set axis labels and title
ax1.set_xlabel("Matrix Size", fontweight='bold', color=text_color)
ax1.set_ylabel("Normalized L1 Cache Misses", fontweight='bold', color='#4FC3F7')
ax2.set_ylabel("Normalized L2 Cache Misses", fontweight='bold', color='#FF8A65')

plt.title("Normalized Cache Misses Growth with Matrix Size (Linear Scale)",
          fontweight='bold', fontsize=15, color=text_color)

# Combine legends from both axes
lines1, labels1 = ax1.get_legend_handles_labels()
lines2, labels2 = ax2.get_legend_handles_labels()

legend = ax1.legend(lines1 + lines2, labels1 + labels2,
                    loc='upper left', frameon=True, framealpha=0.9, fontsize=10)

# Dark mode legend background
legend.get_frame().set_facecolor('#1E1E1E')
legend.get_frame().set_edgecolor('#444444')
legend.get_frame().set_alpha(0.9)

for text in legend.get_texts():
    text.set_color(text_color)

plt.tight_layout()
plt.savefig("cache_misses_growth_darkmode_linear_normalized.png", dpi=300, bbox_inches='tight')
plt.show()
