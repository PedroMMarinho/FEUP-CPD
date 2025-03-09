import pandas as pd
import matplotlib.pyplot as plt
import seaborn as sns

# Load data
df = pd.read_csv("docs/data_cpp.csv")

# Filter for non-parallelized functions
non_parallel = ["Normal Mult", "Inline Mult"]
df_non_parallel = df[df["functionType"].isin(non_parallel)].copy()

# Normalize execution time and cache misses
df_non_parallel["RealTime_Normalized"] = df_non_parallel.groupby(["functionType", "MatrixSize"])["Real Time"].transform(lambda x: (x - x.min()) / (x.max() - x.min()))
df_non_parallel["L1_DCM_Normalized"] = df_non_parallel.groupby(["functionType", "MatrixSize"])["L1 DCM"].transform(lambda x: (x - x.min()) / (x.max() - x.min()))
df_non_parallel["L2_DCM_Normalized"] = df_non_parallel.groupby(["functionType", "MatrixSize"])["L2 DCM"].transform(lambda x: (x - x.min()) / (x.max() - x.min()))

# Create plots for each function type
for function in non_parallel:
    df_function = df_non_parallel[df_non_parallel["functionType"] == function]
    matrix_sizes = sorted(df_function["MatrixSize"].unique())
    
    # Group matrix sizes into pairs
    size_pairs = [matrix_sizes[i:i+2] for i in range(0, len(matrix_sizes), 2)]
    
    for pair in size_pairs:
        fig, axes = plt.subplots(1, len(pair), figsize=(12, 6))
        fig.suptitle(f"{function}: Cache Misses vs Execution Time", fontsize=16)
        
        if len(pair) == 1:
            axes = [axes]  # Ensure iterable if only one subplot
        
        for i, size in enumerate(pair):
            subset = df_function[df_function["MatrixSize"] == size]
            ax = axes[i]
            
            sns.lineplot(x=subset["RealTime_Normalized"], y=subset["L1_DCM_Normalized"], label="L1 Cache Misses", ax=ax, color='blue', marker='o')
            sns.lineplot(x=subset["RealTime_Normalized"], y=subset["L2_DCM_Normalized"], label="L2 Cache Misses", ax=ax, color='red', marker='s')
            
            ax.set_title(f"Matrix Size: {size}")
            ax.set_xlabel("Normalized Execution Time")
            ax.set_ylabel("Normalized Cache Misses")
            ax.legend()
            ax.grid(True)
        
        plt.tight_layout(rect=[0, 0, 1, 0.96])
        plt.show()