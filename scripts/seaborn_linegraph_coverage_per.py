import numpy as np
import pandas as pd
import matplotlib.pyplot as plt
import seaborn as sns

sns.set(style="darkgrid", font="sans-serif",font_scale=1.25)

# intialise data of lists.
data = pd.read_csv("coverage_per_and_gap_per.csv") 

#plot line using seaborn
df = pd.DataFrame(data)
g = sns.lineplot(x ="Scenario", y="Solution Gap", data=df, markers=True, dashes=True)
#g = sns.lineplot(x = np.repeat("Bronx", 30), y="Bronx Coverage", label="Bronx", data=df)
#g = sns.lineplot(x = np.repeat("Queens", 30), y="Queens Coverage", label="Queens", data=df)
#g = sns.lineplot(x = np.repeat("Brooklyn", 30), y="Brooklyn Coverage", label="Brooklyn", data=df)
#g = sns.lineplot(x = np.repeat("Manhattan", 30), y="Manhattan Coverage", label="Manhattan", data=df)
#g.fig.autofmt_xdate()

# control x and y limits
plt.ylim(0, 2)
plt.xlim(0, None)
#plt.fill_between(df.Scenario.values,df.Coverage.values-0.01, color="gold", alpha=0.3)
plt.ylabel("Solution Gap (%)")
plt.xlabel("Scenario")

#leg = g._legend
#leg.set_bbox_to_anchor([0.65, 0.85])  # coordinates of lower left of bounding box
#leg._loc = 1

plt.subplots_adjust(left=0.14, bottom=0.14, right=0.95, top=0.98, wspace=None, hspace=None)

plt.show()
#plt.savefig('ocp_cost_gap.eps', format='eps')