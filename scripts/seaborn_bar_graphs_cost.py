import numpy as np
import pandas as pd
import matplotlib.pyplot as plt
import seaborn as sns

sns.set(style="darkgrid", font="sans-serif",font_scale=1.25)

# intialise data of lists.
cost = {'Scenario':["Staten Island","Bronx","Queens","Brooklyn","Manhattan",
                    "Staten Island","Bronx","Queens","Brooklyn","Manhattan",
                    "Staten Island","Bronx","Queens","Brooklyn","Manhattan",
                    "Staten Island","Bronx","Queens","Brooklyn","Manhattan",
                    "Staten Island","Bronx","Queens","Brooklyn","Manhattan"],
               'Cost':[2881.286,0,0,0,0,
                       2920.1, 8870.167,14407.77,18528.2,43753.3,
                       2700.65,8506.267,13773.87,17802.07,44391.27,
                       2692.96,8267.547,13676.02,17754.57,41552.43,
                       3084.6,10388.47,17213.57,22356.53,52150.83],
               #'Cost':[1.07,0,0,0,0,
                #       1.084,1.073,1.054,1.043,1.053,
                 #      1.003,1.029,1.007,1.003,1.068,
                  #     1.0,1.0,1.0,1.0,1.0,
                   #    1.145,1.256,1.259,1.259,1.255],               
        'Type':["GACP","GACP","GACP","GACP","GACP","ACP","ACP","ACP","ACP","ACP",
                "GACP*","GACP*","GACP*","GACP*","GACP*",
                "OCP-Cost*","OCP-Cost*","OCP-Cost*","OCP-Cost*","OCP-Cost*",
                "OCP-Latency","OCP-Latency","OCP-Latency","OCP-Latency","OCP-Latency"]}

#plot line using seaborn
df = pd.DataFrame(cost)
g = sns.barplot(x="Scenario", y="Cost", hue="Type", hue_order=["GACP","ACP","GACP*","OCP-Cost*", "OCP-Latency"],data=df)

plt.legend(loc="upper left")
plt.subplots_adjust(left=0.15, bottom=0.13, right=None, top=None, wspace=None, hspace=None)

#plt.show()
plt.savefig('scenario_costs.eps', format='eps')