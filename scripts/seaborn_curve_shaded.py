import numpy as np
import pandas as pd
import seaborn as sns
import scipy.stats as stats
import matplotlib.pyplot as plt

data = pd.read_csv("coverage_per_and_gap_per.csv") 
df = pd.DataFrame(data)

x = df["Staten Island Coverage"]
y = df["Bronx Coverage"]
ax = sns.distplot(x, fit_kws={"color":"red"}, kde=False,
        fit=stats.gamma, hist=None, label="Staten Island");
ax = sns.distplot(y, fit_kws={"color":"blue"}, kde=False,
        fit=stats.gamma, hist=None, label="Bronx");

# Get the two lines from the axes to generate shading
l1 = ax.lines[0]
l2 = ax.lines[1]

# Get the xy data from the lines so that we can shade
x1 = l1.get_xydata()[:,0]
y1 = l1.get_xydata()[:,1]
x2 = l2.get_xydata()[:,0]
y2 = l2.get_xydata()[:,1]
ax.fill_between(x1,y1, color="red", alpha=0.3)
ax.fill_between(x2,y2, color="blue", alpha=0.3)

plt.legend()
plt.show()