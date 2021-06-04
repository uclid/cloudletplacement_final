# library
from mpl_toolkits.mplot3d import Axes3D
import matplotlib.pyplot as plt
import pandas as pd
import seaborn as sns

# Get the data (csv file is hosted on the web)
url = 'time_3d_plot.csv'
df = pd.read_csv(url)

# Make the plot
fig = plt.figure()
ax = fig.gca(projection='3d')
ax.plot_trisurf(df['Points'], df['Devices'], df['ACP Time'], cmap=plt.cm.viridis, linewidth=0.2)
plt.show()

# to Add a color bar which maps values to colors.
surf=ax.plot_trisurf(df['Points'], df['Devices'], df['GACP Time'], cmap=plt.cm.viridis, linewidth=0.2)
fig.colorbar( surf, shrink=0.5, aspect=5)
plt.show()

# Rotate it
ax.view_init(30, 45)
plt.show()

# Other palette
ax.plot_trisurf(df['Points'], df['Devices'], df['OCP Cost Time'], cmap=plt.cm.jet, linewidth=0.2)
plt.show()

# Other palette
ax.plot_trisurf(df['Points'], df['Devices'], df['OCP Latency Time'], cmap=plt.cm.jet, linewidth=0.2)
plt.show()