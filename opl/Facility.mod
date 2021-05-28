/*********************************************
 * OPL 12.8.0.0 Model
 * Author: Dixit
 * Creation Date: Oct 1, 2018 at 2:25:16 PM
 *********************************************/
tuple GridPoints{
	int x;
	int y;
}

tuple Cloudlet{
	key int id;
	int process;
	int memory;
	int storage;
	int coverage;
	int power_use;
}

tuple EndDevice{
	key int id;
	int process_dem;
	int memory_dem;
	int storage_dem;
	GridPoints location;
	int battery_life;
}

{GridPoints} AllPoints = ...;


{Cloudlet} C = ...;
{EndDevice} E = ...;

{GridPoints} CandidatePoints = ...;

int Cost[C][CandidatePoints] = ...;
int Latency[E][CandidatePoints] = ...;
dvar boolean select[C][CandidatePoints];
dvar boolean select_end[E][CandidatePoints];
//int numCandPoints = card(CandidatePoints);
int numCloudlets = card(C);
//int numDevices = card(E);
int distance[E][CandidatePoints];

//finding distances
execute {
  for(var i in E) {
	  for(var j in CandidatePoints){
	  	distance[i][j] = Opl.ceil(Opl.sqrt((i.location.x - j.x)*(i.location.x - j.x) + (i.location.y - j.y)*(i.location.y - j.y)));
	  }  
  }
}

minimize
  //1, objective functions: cost and latency
  sum(c in C) (sum (p in CandidatePoints) Cost[c][p] * select[c][p]);
  //sum(e in E) (sum (p in CandidatePoints) Latency[e][p] * select_end[e][p]);
subject to {
	
	//2 The total number of cloudlets placed in the grid space should be 
	//less than or equal to number of available cloudlets.
	sum(c in C) sum (p in CandidatePoints) select[c][p] <= numCloudlets;
	
	//3 Each end device must be within coverage range of some cloudlet.
	forall(p in CandidatePoints, e in E) distance[e][p]*select_end[e][p] <= sum (c in C) select[c][p]*c.coverage;
	
	//4 Sum of memory demand of served end devices should be less than or equal to serving couldlet.
	forall(p in CandidatePoints)(sum(e in E) select_end[e][p]*e.memory_dem) <= sum (c in C)select[c][p]*c.memory;
	
	//5 Sum of storage demand of served end devices should be less than or equal to serving couldlet.
	forall(p in CandidatePoints)(sum(e in E) select_end[e][p]*e.storage_dem) <= sum (c in C)select[c][p]*c.storage;
	
	//6 Sum of processing demand of served end devices should be less than or equal to serving couldlet.
	forall(p in CandidatePoints)(sum(e in E) select_end[e][p]*e.process_dem) <= sum (c in C)select[c][p]*c.process;
	
	//7 An end device can be served from a candidate point only if there is at least one cloudlet placed there.
	forall(e in E, p in CandidatePoints) select_end[e][p] <= sum(c in C) select[c][p];
	
	//8 At most one cloudlet can be placed at a candidate point.
	forall(p in CandidatePoints)(sum(c in C) select[c][p]) <= 1 ;
	
	//9 A cloudlet can only be placed at a single candidate point.
	forall(c in C)(sum(p in CandidatePoints) select[c][p]) <= 1 ;
	
	//10 All end devices must be served, each from exactly one candidate point
	forall(e in E)(sum (p in CandidatePoints) select_end[e][p]) == 1;
	
	/**
	Constraints for enforcing thresholds on cost or latency for sensitivity analysis
	**/
	
	//threshold value for cost, used when minimizing latency.
	//sum(c in C) (sum (p in CandidatePoints) Cost[c][p] * select[c][p]) <= 14;
    
    //threshold value for latency, used when minimizing cost
    //sum(e in E) (sum (p in CandidatePoints) Latency[e][p] * select_end[e][p]) <= 190;
	
}

tuple solutionT{
	GridPoints list;
};

int selected[p in CandidatePoints] = ((sum(c in C) select[c][p] >= 1) ? 1:0);

{solutionT} list = { <p> | p in CandidatePoints : selected[p] == 1 };

int final_cost = sum(c in C) (sum (p in CandidatePoints) Cost[c][p] * select[c][p]);
int final_latency = sum(e in E) (sum (p in CandidatePoints) Latency[e][p] * select_end[e][p]);

execute DISPLAY {
  writeln("Selected=", list);
  writeln("Cost=", final_cost);
  writeln("Latency=", final_latency);
}
