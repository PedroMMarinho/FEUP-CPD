import pandas as pd
import matplotlib.pyplot as plt
import numpy as np
import seaborn as sns

# Load CSV
df = pd.read_csv("docs/data_cpp.csv")

# Calculate Mflops
df["Mflops"] = (2 * df["MatrixSize"]**3) / (df["Real Time"] * 10**6)

# Functions that are not parallelized
non_parallel = ["Normal Mult", "Inline Mult"]

# Filter for non-parallelized functions
df_non_parallel = df[df["functionType"].isin(non_parallel)].copy()

# Function-type normalization
df_non_parallel["Mflops_Normalized"] = df_non_parallel.groupby("functionType")["Mflops"].transform(
    lambda x: (x - x.min()) / (x.max() - x.min())
)

# Create a figure with multiple subplots
fig, axes = plt.subplots(1, 2, figsize=(16, 6))
fig.suptitle("Normal Mult: Normalized Performance Analysis", fontsize=16)

# Focus on "Normal Mult" function
normal_mult_data = df_non_parallel[df_non_parallel["functionType"] == "Normal Mult"]

# 1. Box plot
ax1 = axes[0]
sns.boxplot(x="MatrixSize", y="Mflops_Normalized", data=normal_mult_data, ax=ax1)
ax1.set_title("Box Plot: Distribution of Normalized Mflops by Matrix Size")
ax1.set_xlabel("Matrix Size")
ax1.set_ylabel("Normalized Mflops")
ax1.grid(True)


# 3. Scatter with trend line
ax3 = axes[1]
ax3.scatter(normal_mult_data["MatrixSize"], normal_mult_data["Mflops_Normalized"], alpha=0.5)

# Add trend line
unique_sizes = sorted(normal_mult_data["MatrixSize"].unique())
means = [normal_mult_data[normal_mult_data["MatrixSize"] == size]["Mflops_Normalized"].mean() for size in unique_sizes]
ax3.plot(unique_sizes, means, 'r-', linewidth=2)
ax3.set_title("Scatter Plot with Trend Line")
ax3.set_xlabel("Matrix Size")
ax3.set_ylabel("Normalized Mflops")
ax3.grid(True)


plt.tight_layout(rect=[0, 0, 1, 0.95])

plt.savefig("graphic_data/MFLOP_NORMAL_MULT.png", dpi=300, bbox_inches="tight")
plt.close()


inline_mult_data = df_non_parallel[df_non_parallel["functionType"] == "Inline Mult"]

# Create a figure with multiple subplots
fig, axes = plt.subplots(1, 2, figsize=(16, 6))
fig.suptitle("Inline Mult: Normalized Performance Analysis", fontsize=16)

# 1. Box plot
ax1 = axes[0]
sns.boxplot(x="MatrixSize", y="Mflops_Normalized", data=inline_mult_data, ax=ax1)
ax1.set_title("Box Plot: Distribution of Normalized Mflops by Matrix Size")
ax1.set_xlabel("Matrix Size")
ax1.set_ylabel("Normalized Mflops")
ax1.grid(True)


# 3. Scatter with trend line
ax3 = axes[1]
ax3.scatter(inline_mult_data["MatrixSize"], inline_mult_data["Mflops_Normalized"], alpha=0.5)

# Add trend line
unique_sizes = sorted(inline_mult_data["MatrixSize"].unique())
means = [inline_mult_data[inline_mult_data["MatrixSize"] == size]["Mflops_Normalized"].mean() for size in unique_sizes]
ax3.plot(unique_sizes, means, 'r-', linewidth=2)
ax3.set_title("Scatter Plot with Trend Line")
ax3.set_xlabel("Matrix Size")
ax3.set_ylabel("Normalized Mflops")
ax3.grid(True)


plt.tight_layout(rect=[0, 0, 1, 0.95])
# Save the figure
plt.savefig("graphic_data/MFLOP_INLINE_MULT.png", dpi=300, bbox_inches="tight")

plt.close()


block_data = ["Block Mult", "Inline Block Mult"]

df_block_data = df[df["functionType"].isin(block_data)].copy()

df_block_data["Mflops_Normalized"] = df_block_data.groupby(["functionType", "BlockSize"])["Mflops"].transform(
    lambda x: (x - x.min()) / (x.max() - x.min())
)

print(df_block_data.groupby(['functionType', 'BlockSize']).size())

for function in ["Block Mult", "Inline Block Mult"]:
    block_data = df_block_data[df_block_data["functionType"] == function]

    # Extract unique block sizes
    block_sizes = sorted(block_data["BlockSize"].unique())

    # Create a separate plot for each BlockSize
    for block_size in block_sizes:
        block_size_data = block_data[block_data["BlockSize"] == block_size]

        # Create a figure with multiple subplots
        fig, axes = plt.subplots(1, 2, figsize=(16, 6))
        fig.suptitle(f"{function} - Block Size: {block_size} - Normalized Performance Analysis", fontsize=16)

        # 1. Box plot
        ax1 = axes[0]
        sns.boxplot(x="MatrixSize", y="Mflops_Normalized", data=block_size_data, ax=ax1)
        ax1.set_title(f"Box Plot: Distribution of Normalized Mflops by Matrix Size")
        ax1.set_xlabel("Matrix Size")
        ax1.set_ylabel("Normalized Mflops")
        ax1.grid(True)

        # 3. Scatter with trend line
        ax3 = axes[1]
        ax3.scatter(block_size_data["MatrixSize"], block_size_data["Mflops_Normalized"], alpha=0.5)

        # Add trend line
        unique_sizes = sorted(block_size_data["MatrixSize"].unique())
        means = [block_size_data[block_size_data["MatrixSize"] == size]["Mflops_Normalized"].mean() for size in unique_sizes]
        ax3.plot(unique_sizes, means, 'r-', linewidth=2)
        ax3.set_title(f"Scatter Plot with Trend Line")
        ax3.set_xlabel("Matrix Size")
        ax3.set_ylabel("Normalized Mflops")
        ax3.grid(True)

        plt.tight_layout(rect=[0, 0, 1, 0.95])
        # Save the figure if needed
        plt.savefig(f"graphic_data/MFLOP_{function.replace(' ', '_').upper()}_BLOCKSIZE_{block_size}.png", dpi=300, bbox_inches="tight")
        plt.close()


# TODO CREATE GRAPHICS FOR PARALLELIZED FUNCTIONS
# Parallelized functions


parallel_functions = ["Parallelized Normal Mult", "Parallelized Inline Mult", "Inner Most Loop Parallelization", "Inner Most Loop Parallelization Inline"] # Need more data for "Inner Most Loop Parallelization" and "Inner Most Inline Loop Parallelization"

# Filter the dataset for the parallelized functions
df_parallel = df[df["functionType"].isin(parallel_functions)].copy()

# Normalize MFLOPS within each functionType and NumThreads group
df_parallel["Mflops_Normalized"] = df_parallel.groupby(["functionType", "NumThreads"])["Mflops"].transform(
    lambda x: (x - x.min()) / (x.max() - x.min())
)

# Print counts to check available data
print(df_parallel.groupby(['functionType', 'NumThreads']).size())

# Loop over each function type
for function in parallel_functions:
    function_data = df_parallel[df_parallel["functionType"] == function]

    # Extract unique thread counts
    thread_counts = sorted(function_data["NumThreads"].unique())

    # Generate a separate plot for each NumThreads value
    for num_threads in thread_counts:
        thread_data = function_data[function_data["NumThreads"] == num_threads]

        # Create a figure with multiple subplots
        fig, axes = plt.subplots(1, 2, figsize=(16, 6))
        fig.suptitle(f"{function} - NumThreads: {num_threads} - Normalized Performance Analysis", fontsize=16)

        # 1. Box plot
        ax1 = axes[0]
        sns.boxplot(x="MatrixSize", y="Mflops_Normalized", data=thread_data, ax=ax1)
        ax1.set_title(f"Box Plot: Distribution of Normalized Mflops by Matrix Size")
        ax1.set_xlabel("Matrix Size")
        ax1.set_ylabel("Normalized Mflops")
        ax1.grid(True)

        # 3. Scatter with trend line
        ax3 = axes[1]
        ax3.scatter(thread_data["MatrixSize"], thread_data["Mflops_Normalized"], alpha=0.5)

        # Add trend line
        unique_sizes = sorted(thread_data["MatrixSize"].unique())
        means = [thread_data[thread_data["MatrixSize"] == size]["Mflops_Normalized"].mean() for size in unique_sizes]
        ax3.plot(unique_sizes, means, 'r-', linewidth=2)
        ax3.set_title(f"Scatter Plot with Trend Line")
        ax3.set_xlabel("Matrix Size")
        ax3.set_ylabel("Normalized Mflops")
        ax3.grid(True)


        plt.tight_layout(rect=[0, 0, 1, 0.95])
        # Save the figure if needed
        plt.savefig(f"graphic_data/MFLOP_{function.replace(' ', '_').upper()}_THREADS_{num_threads}.png", dpi=300, bbox_inches="tight")
        plt.close()

