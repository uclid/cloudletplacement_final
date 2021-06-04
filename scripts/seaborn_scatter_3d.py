import matplotlib.pyplot as plt
import pandas as pd
import seaborn as sns
import numpy as np
from mpl_toolkits.mplot3d import Axes3D

df = pd.read_csv('time_3d_plot.csv')
sns.set(style = "whitegrid",font="sans-serif")

fig = plt.figure()
ax = fig.add_subplot(111, projection = '3d')

x = df['Points']
y = df['Devices']
z = df['ACP Time']
z2 = df['GACP Time']
z3 = df['OCP Latency Time']
z4 = df['OCP Cost Time']

ax.set_xlabel("Candidate Points #")
ax.set_ylabel("Devices #")
ax.set_zlabel("Time (in secs)")

#log scale in z axis
#ax.set_zscale("log", basez=2)

#ax.scatter(x, y, z, marker="*", label="ACP")
#ax.scatter(x, y, z2, marker="1", label="GACP*")
#ax.scatter(x, y, z3, marker="2", label="OCP Latency")
#ax.scatter(x, y, z4, marker="3", label="OCP Cost*")



#workaround for log scale
ax.scatter(x, y, np.log2(z), marker="*", label="ACP")
ax.scatter(x, y, np.log2(z2), marker="1", label="GACP*")
ax.scatter(x, y, np.log2(z3), marker="2", label="OCP-Latency")
ax.scatter(x, y, np.log2(z4), marker="3", label="OCP-Cost*")

zticks = [50,500,5000,50000,500000,5000000]
zticks2 = [0.05,0.5,5,50,500,5000]
#zticks = [32,181,1024,5792,32768,185364,1048576,5931642]
#zticks2 = ['$2^5$','$2^{7.5}$','$2^{10}$','$2^{12.5}$','$2^{15}$','$2^{17.5}$','$2^{20}$','$2^{22.5}$']
ax.set_zticks(np.log2(zticks))
ax.set_zticklabels(zticks2)
#ax.tick_params(axis='z', pad=15)
#plt.setp(ax.get_zticklabels(), rotation=15, horizontalalignment='right')

plt.subplots_adjust(left=0.14, bottom=0.14, right=0.95, top=0.98, wspace=None, hspace=None)

plt.legend()
plt.show()