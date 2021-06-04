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
               'Latency':[241.14,0,0,0,0,
                          247,758.17,1351.133,1676,3771.267,
                       236.56,653.37,1157.467,1422.4,3152.333,
                       247,777.5,1369.5,1737.667,4095.9,
                       220.28,552.67,981.73,1183.067,2857.867],
               #'Latency':[1.095,0,0,0,0,
                #          1.121,1.372,1.376,1.417,1.320,
                 #      1.074,1.182,1.179,1.202,1.103,
                  #     1.121,1.407,1.395,1.469,1.433,
                   #    1.0,1.0,1.0,1.0,1.0],              
        'Type':["GACP","GACP","GACP","GACP","GACP","ACP","ACP","ACP","ACP","ACP",
                "GACP*","GACP*","GACP*","GACP*","GACP*",
                "OCP-Cost*","OCP-Cost*","OCP-Cost*","OCP-Cost*","OCP-Cost*",
                "OCP-Latency","OCP-Latency","OCP-Latency","OCP-Latency","OCP-Latency"]}

#plot line using seaborn
df = pd.DataFrame(cost)
g = sns.barplot(x="Scenario", y="Latency", hue="Type", hue_order=["GACP","ACP","GACP*","OCP-Cost*", "OCP-Latency"],data=df)

plt.legend(loc="upper left")
plt.subplots_adjust(left=0.15, bottom=0.13, right=None, top=None, wspace=None, hspace=None)

#plt.show()
plt.savefig('scenario_latencies.eps', format='eps')