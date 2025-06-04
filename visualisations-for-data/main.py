import os
import json
import matplotlib.pyplot as plt

global_metrics = {}
number_files = 0

def find_and_parse(directory):
    global global_metrics
    global number_files

    for root, _, files in os.walk(directory):
        for file in files:
            if file.endswith('.json'):
                file_path = os.path.join(root, file)
                try:
                    with open(file_path, 'r', encoding='utf-8') as f:
                        data = json.load(f)
                        metrics_data = data.get('metrics', {})

                        if not isinstance(metrics_data, dict):
                            print(f"'metrics' is not a dictionary in {file_path}")
                            continue

                        for metric, value in metrics_data.items():
                            try:
                                value = int(value)
                            except ValueError:
                                print(f"Non-integer value '{value}' for metric '{metric}' in {file_path}")
                                continue

                            if metric not in global_metrics:
                                global_metrics[metric] = {}

                            if value not in global_metrics[metric]:
                                global_metrics[metric][value] = 1
                            else:
                                global_metrics[metric][value] += 1

                        number_files += 1
                        print(f"Parsed data from file: {file_path}")
                except Exception as e:
                    print(f"Error while parsing {file_path}: {e}")

def plot_metrics(metrics):
    for metric_name, value_counts in metrics.items():
        values = sorted(value_counts.keys())
        counts = [value_counts[v] for v in values]

        plt.figure()
        plt.bar(values, counts)
        plt.title(f"Frequency of Values for Metric: {metric_name}")
        plt.xlabel("Value")
        plt.ylabel("Frequency")
        plt.grid(True)
        plt.tight_layout()
        plt.savefig(f"{metric_name}_plot.png")  # Save plot to PNG file
        plt.close()
        print(f"Saved plot for metric '{metric_name}' as '{metric_name}_plot.png'")

if __name__ == '__main__':

    find_and_parse('/home/paaull/aiprojectvsextension/scripts-metrics/results/claudiu')
    find_and_parse('/home/paaull/aiprojectvsextension/scripts-metrics/results/eca')
    find_and_parse('/home/paaull/aiprojectvsextension/scripts-metrics/results/georgi')


    print("\n--- Summary ---")
    print(f"Number of parsed files: {number_files}")
    print("Aggregated metrics:")
    print(json.dumps(global_metrics, indent=4))

    print("\nGenerating plots...")
    plot_metrics(global_metrics)
