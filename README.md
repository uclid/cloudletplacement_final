# A Bifactor Approximation Algorithm for Cloudlet Placement in Edge Computing

A bifactor approximation algorithm (ACP) to solve the heterogeneous cloudlet placement problem to guarantee a bounded latency and placement cost, while fully mapping  user applications  to  appropriate cloudlets.

We aim to efficiently place  cloudlets  to specific locations in a region to serve the demands of all the end devices (IoT) that require  edge services. We model the region as a two-dimensional space (grid), where cloudlets and devices can exist. The devices could be at any point in the space. On the other hand, we assume only a set of candidate points within the grid are available where the cloudlets can be placed and the devices can be best served from. The candidate points are selected based on the load of user requests and the  location of user demands over a long period.

##Approaches Implemented
*IP and LP (solved using CPLEX library)
**OCP Cost - Optimal Cost Placement 
**OCP Latency - Optimal Latency Placement
**LP Cost - LP Cost Placment
*GACP - Genetic Algorithm Based Cloudlet Placement
*ACP - Approximate Cloudlet Placement (our approach) 


##Classes Summary
*Core Classes: Cloudlet, CandidatePoint, EndDevice
**Extended Classes (for implemanting ACP algorithm): NewCloudlet, NewCandidatePoint, NewEndDevice
*CPLEX Model: [CplexCloudletPlacement]("cplex_model/algorithm/CplexCloudletPlacement.java"), [CplexLPCloudletPlacement]("cplex_model/algorithm/CplexLPCloudletPlacement.java")
*Genetic Algorithm: [GeneticCloudletPlacement]("genetic_algorithm/GeneticCloudletPlacement.java")
*Approximation Algorithm: [ApproxLPRounding]("approx_algorithm/ApproxLPRounding.java")
