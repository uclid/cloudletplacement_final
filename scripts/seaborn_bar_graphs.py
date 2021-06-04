import numpy as np
import pandas as pd
import matplotlib.pyplot as plt
import seaborn as sns

sns.set(style="darkgrid", font="sans-serif",font_scale=1.25)

# intialise data of lists.
cost = {'Candidate Point %':[10, 15, 20, 10, 15, 20, 10, 15, 20],
               'Cost':[37.14, 38.23, 37.75, 34, 33, 33, 52, 53, 53],
        'Type':["GACP Cost","GACP Cost","GACP Cost","OCP Cost","OCP Cost","OCP Cost","OCP Latency Best Cost","OCP Latency Best Cost","OCP Latency Best Cost"]}

latency = {'Candidate Point %':[10, 15, 20, 10, 15, 20, 10, 15, 20],
        'Latency':[1121.19, 1127.54, 1101.36, 855, 789, 819, 1216, 1067, 1039],
               'Type':["GACP Latency","GACP Latency","GACP Latency","OCP Latency","OCP Latency","OCP Latency","OCP Cost Best Latency","OCP Cost Best Latency","OCP Cost Best Latency"]}

#plot line using seaborn
df = pd.DataFrame(cost)
g = sns.barplot(x="Candidate Point %", y="Cost", hue="Type", hue_order=["GACP Cost","OCP Cost", "OCP Latency Best Cost"],data=df)

plt.legend(loc="lower left")
plt.subplots_adjust(left=0.15, bottom=0.13, right=None, top=None, wspace=None, hspace=None)

plt.show()
#plt.savefig('candidate_per_cost.eps', format='eps')
