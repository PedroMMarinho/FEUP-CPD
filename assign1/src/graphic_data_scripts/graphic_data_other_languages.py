import pandas as pd
import matplotlib.pyplot as plt
import seaborn as sns

# Load CSV
df = pd.read_csv("docs/data_java.csv")

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

# Plot for each function type
for function in non_parallel:
    function_data = df_non_parallel[df_non_parallel["functionType"] == function]
    
    fig, axes = plt.subplots(1, 2, figsize=(16, 6))
    fig.suptitle(f"{function}: Normalized Performance Analysis", fontsize=16)
    
    # Box Plot
    ax1 = axes[0]
    sns.boxplot(x="MatrixSize", y="Mflops_Normalized", data=function_data, ax=ax1)
    ax1.set_title("Box Plot: Distribution of Normalized Mflops by Matrix Size")
    ax1.set_xlabel("Matrix Size")
    ax1.set_ylabel("Normalized Mflops")
    ax1.grid(True)
    
    # Scatter with trend line
    ax2 = axes[1]
    ax2.scatter(function_data["MatrixSize"], function_data["Mflops_Normalized"], alpha=0.5)
    
    unique_sizes = sorted(function_data["MatrixSize"].unique())
    means = [function_data[function_data["MatrixSize"] == size]["Mflops_Normalized"].mean() for size in unique_sizes]
    ax2.plot(unique_sizes, means, 'r-', linewidth=2)
    
    ax2.set_title("Scatter Plot with Trend Line")
    ax2.set_xlabel("Matrix Size")
    ax2.set_ylabel("Normalized Mflops")
    ax2.grid(True)
    
    plt.tight_layout(rect=[0, 0, 1, 0.95])
    plt.savefig(f"graphic_data_java/MFLOP_{function.replace(' ', '_').upper()}.png", dpi=300, bbox_inches="tight")
    plt.close()