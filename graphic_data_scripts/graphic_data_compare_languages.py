import pandas as pd
import matplotlib.pyplot as plt
import seaborn as sns
import numpy as np

# Set dark theme and improved aesthetics
plt.style.use('dark_background')
plt.rcParams['font.family'] = 'sans-serif'
plt.rcParams['font.sans-serif'] = ['Arial', 'Helvetica', 'DejaVu Sans']
plt.rcParams['axes.labelsize'] = 14
plt.rcParams['axes.titlesize'] = 16
plt.rcParams['xtick.labelsize'] = 12
plt.rcParams['ytick.labelsize'] = 12
plt.rcParams['legend.fontsize'] = 12
plt.rcParams['figure.facecolor'] = '#121212'  # Dark background
plt.rcParams['axes.facecolor'] = '#1E1E1E'    # Slightly lighter background for contrast

def load_and_process_data(filename, function_type):
    """Loads data from CSV, filters for the specified function type,
    and normalizes execution time.
    """
    df = pd.read_csv(filename)
    df = df[df["functionType"] == function_type]
    df = df.dropna(subset=['Real Time', 'MatrixSize'])
    df['Real Time'] = pd.to_numeric(df['Real Time'], errors='coerce')
    df['MatrixSize'] = pd.to_numeric(df['MatrixSize'], errors='coerce')
    df = df.dropna(subset=['Real Time', 'MatrixSize'])
    
    # Group by MatrixSize and calculate average execution time
    # This will eliminate multiple values for the same matrix size
    df = df.groupby('MatrixSize')['Real Time'].mean().reset_index()
    
    # Sort by MatrixSize to ensure smooth lines
    df = df.sort_values('MatrixSize')
    
    # Normalize RealTime
    min_rt = df["Real Time"].min()
    max_rt = df["Real Time"].max()
    if max_rt > min_rt:
        df["RealTime_Norm"] = (df["Real Time"] - min_rt) / (max_rt - min_rt)
    else:
        df["RealTime_Norm"] = 0
    return df

# Function types to plot
function_types = ["Normal Mult", "Inline Mult"]

# Languages with vibrant colors for better visibility on dark background
languages = {
    "C++": "#00BFFF",   # Bright blue
    "Go": "#FFA500",    # Bright orange
    "Java": "#32CD32"   # Bright green
}

# Create plots for each function type
for function_type in function_types:
    plt.figure(figsize=(12, 7))
    
    # Plot data for each language
    for language, color in languages.items():
        filename = f"docs/data_cpp.csv" if language == "C++" else f"docs/data_{language.lower()}.csv"
        try:
            data = load_and_process_data(filename, function_type)
            
            # Plot line with enhanced visibility - no markers
            plt.plot(data["MatrixSize"], data["RealTime_Norm"], 
                     linestyle="-", linewidth=3.0, color=color, label=language, alpha=0.9)
        except Exception as e:
            print(f"Error processing {language} data for {function_type}: {e}")
    
    # Enhance axis labels and title
    plt.xlabel("Matrix Size", fontweight='bold', color='white')
    plt.ylabel("Normalized Execution Time", fontweight='bold', color='white')
    plt.title(f"{function_type}: Performance Comparison Across Languages", 
              fontweight='bold', pad=20, color='white', fontsize=18)
              
    # Add subtle grid for readability
    plt.grid(True, alpha=0.2, linestyle='--')
    
    # Improve legend
    legend = plt.legend(title="Language", title_fontsize=13, 
               loc='upper left', frameon=True, framealpha=0.7,
               edgecolor='gray')
    plt.setp(legend.get_title(), color='white')
    
    # Add subtle plot styling elements
    plt.gca().spines['top'].set_visible(False)
    plt.gca().spines['right'].set_visible(False)
    plt.gca().spines['left'].set_color('gray')
    plt.gca().spines['bottom'].set_color('gray')
    
    # Ensure tight layout and save with higher quality
    plt.tight_layout()
    plt.savefig(f"graphic_data_compare_languages/{function_type.replace(' ', '_').upper()}_COMPARISON.png",dpi=300, bbox_inches='tight') 
    plt.close()