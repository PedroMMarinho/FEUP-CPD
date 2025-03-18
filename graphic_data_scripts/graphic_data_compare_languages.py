import pandas as pd
import matplotlib.pyplot as plt
import seaborn as sns
import numpy as np
import os

# Set dark theme and improved aesthetics
plt.style.use('dark_background')
plt.rcParams['font.family'] = 'sans-serif'
plt.rcParams['font.sans-serif'] = ['Arial', 'Helvetica', 'DejaVu Sans']
plt.rcParams['axes.labelsize'] = 14
plt.rcParams['axes.titlesize'] = 16
plt.rcParams['xtick.labelsize'] = 12
plt.rcParams['ytick.labelsize'] = 12
plt.rcParams['legend.fontsize'] = 12
plt.rcParams['figure.facecolor'] = '#121212'
plt.rcParams['axes.facecolor'] = '#1E1E1E'

def load_data(filename, function_type, max_size=3000):
    """Loads data from CSV, filters for the specified function type"""
    df = pd.read_csv(filename)
    df = df[df["functionType"] == function_type]
    df = df.dropna(subset=['Real Time', 'MatrixSize'])
    df['Real Time'] = pd.to_numeric(df['Real Time'], errors='coerce')
    df['MatrixSize'] = pd.to_numeric(df['MatrixSize'], errors='coerce')
    df = df.dropna(subset=['Real Time', 'MatrixSize'])

    # Filter to include only matrix sizes up to max_size
    df = df[df['MatrixSize'] <= max_size]

    # Group by MatrixSize and calculate average execution time
    df = df.groupby('MatrixSize')['Real Time'].mean().reset_index()

    return df

function_types = ["Normal Mult", "Inline Mult"]

languages = {
    "C++": "#00BFFF",
    "Go": "#FFA500",
    "Java": "#32CD32"
}

MAX_MATRIX_SIZE = 3000

os.makedirs("graphic_data_compare_languages", exist_ok=True)

for function_type in function_types:
    plt.figure(figsize=(12, 7))

    language_data = {}
    for language in languages:
        filename = f"docs/data_cpp.csv" if language == "C++" else f"docs/data_{language.lower()}.csv"
        try:
            data = load_data(filename, function_type, MAX_MATRIX_SIZE)

            if len(data) == 0:
                print(f"No data points for {language} with matrix size <= {MAX_MATRIX_SIZE}")
                language_data[language] = pd.DataFrame()
                continue

            language_data[language] = data

        except Exception as e:
            print(f"Error processing {language} data for {function_type}: {e}")
            language_data[language] = pd.DataFrame()

    for language, color in languages.items():
        if len(language_data[language]) > 0: # Use language_data instead of normalized_data
            plt.plot(language_data[language]["MatrixSize"],
                     language_data[language]["Real Time"], # Use "Real Time" directly
                     linestyle="-", linewidth=3.0, color=color,
                     label=language, alpha=0.9)

    # Removed plt.yscale('log')
    plt.xlabel("Matrix Size", fontweight='bold', color='white')
    plt.ylabel("Execution Time (seconds)", fontweight='bold', color='white') # Updated label
    plt.title(f"{function_type}: Performance Comparison",
              fontweight='bold', pad=20, color='white', fontsize=18)

    plt.grid(True, alpha=0.2, linestyle='--')

    legend = plt.legend(title="Language", title_fontsize=13,
                        loc='upper left', frameon=True, framealpha=0.7,
                        edgecolor='gray')
    plt.setp(legend.get_title(), color='white')

    plt.gca().spines['top'].set_visible(False)
    plt.gca().spines['right'].set_visible(False)
    plt.gca().spines['left'].set_color('gray')
    plt.gca().spines['bottom'].set_color('gray')

    plt.tight_layout()
    #plt.savefig(f"graphic_data_compare_languages/{function_type.replace(' ', '_').upper()}_PERFORMANCE.png", dpi=300, bbox_inches='tight', facecolor='#121212')
    plt.close()
