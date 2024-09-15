import matplotlib.pyplot as plt

with open("telemetry_log.csv", "r") as f:
    data = f.read().split("\n")
    data = [row.split(",") for row in data]
    data = data[1:]

data = [row for row in data if len(row) == 4]
x = [float(row[0]) / 1e9 for row in data]
y1 = [float(row[1]) for row in data]
y2 = [float(row[2]) for row in data]
y3 = [float(row[3]) for row in data]

plt.title("Kalman filter fused output")
plt.xlabel("Time (sec)")
plt.ylabel("Value")
plt.plot(x, y1, color="blue")
plt.plot(x, y3, alpha=0.5, color="green")
plt.plot(x, y2, color="orange")
plt.legend(["y1 (data model)", "y2 (noisy sensor)", "y3 (Kalman filter)"])
plt.show()
