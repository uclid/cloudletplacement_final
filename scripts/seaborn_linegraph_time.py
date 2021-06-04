import numpy as np
import pandas as pd
import matplotlib.pyplot as plt
import seaborn as sns

sns.set(style="darkgrid", font="sans-serif",font_scale=1.25)

# intialise data of lists.
data = pd.read_csv("Simulations_100_CHarlem_10.csv") 

#plot line using seaborn
df = pd.DataFrame(data)
g = sns.relplot(x="Simulations", y="Time", kind="line", hue="Time Type", style="Time Type", markers=True, data=df)
g.fig.autofmt_xdate()

# control x and y limits
plt.ylim(0, 2500)
plt.xlim(0, None)
plt.ylabel("Running Time (ms)")

leg = g._legend
leg.set_bbox_to_anchor([0.65, 0.85])  # coordinates of lower left of bounding box
leg._loc = 1

plt.subplots_adjust(left=0.14, bottom=0.14, right=0.98, top=0.98, wspace=None, hspace=None)

plt.show()
#plt.savefig('time_harlem_10.eps', format='eps')