import matplotlib.pyplot as plt

with open("telemetry_log.csv", "r") as f:
    data = f.read().split("\n")
    data = [row.split(",") for row in data]
    data = data[1:]

data = [row for row in data if len(row) == 4]
x = [float(row[0]) for row in data]
y1 = [float(row[1]) for row in data]
y2 = [float(row[2]) for row in data]
y3 = [float(row[3]) for row in data]

plt.plot(x, y1)
plt.plot(x, y2)
plt.plot(x, y3)
plt.legend(["y1 (original)", "y2 (filtered)", "y3 (garbage)"])
plt.show()
