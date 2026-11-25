public class Process{

   private String processId;
   private int arrivalTime;
   private int burstTime;
   private int remainingTime;
   private int initialPriority;
   private int firstStartTime = -1;
   private int completionTime = -1;
   private int tier2EntryTime = -1;

   public Process(String Id, int arrivaltime, int bursttime, int priority){
      if(Id == null || Id.isBlank() || arrivaltime < 0 || bursttime <= 0){
         throw new 
         IllegalArgumentException("Please enter valid process data");
      }
      this.processId=Id;
      this.arrivalTime=arrivaltime;
      this.burstTime=bursttime;
      this.remainingTime=bursttime;
      this.initialPriority=priority;
   }   

   public String getProcessId() {
      return processId;
   }

   public int getArrivalTime() {
      return arrivalTime;
   }

   public int getBurstTime() {
      return burstTime;
   }

   public int getRemainingTime() {
      return remainingTime;
   }

   public int getInitialPriority() {
      return initialPriority;
   }

   public int getFirstStartTime() {
      return firstStartTime;
   }

   public int getCompletionTime() {
      return completionTime;
   }

   public int getTier2EntryTime() {
      return tier2EntryTime;
   }

   public void setRemainingTime(int remainingTime) {
      if(remainingTime >= 0 && remainingTime <= burstTime){
         this.remainingTime = remainingTime;
      } 
   }


   public void setFirstStartTime(int firstStartTime) {
      if(this.firstStartTime == -1 && firstStartTime >= 0){
         this.firstStartTime = firstStartTime;
      }
   }

   public void setCompletionTime(int completionTime) {
      if(this.completionTime == -1 && completionTime >= 0){ 
         this.completionTime = completionTime;
      }
   }

   public void setTier2EntryTime(int time) {
      if (tier2EntryTime == -1 && time >= arrivalTime) {
         tier2EntryTime = time;
      }
   }

   public boolean isCompleted() {
      return remainingTime == 0;
   }

   public int getEffectivePriority(int currentTime) {
      if (tier2EntryTime == -1) {
         return initialPriority;
      }
      int waitingTime = currentTime - tier2EntryTime;
      int agingIntervals = waitingTime / 4;
      return initialPriority - agingIntervals;
   }
   
   public Process copy() {
      return new Process(this);
   }
   
   public Process(Process other){
      if(other == null){
         throw new IllegalArgumentException("Invalid process to copy");
      }   
      this.processId = other.processId;
      this.arrivalTime=other.arrivalTime;
      this.burstTime=other.burstTime;
      this.remainingTime=other.burstTime;
      this.initialPriority=other.initialPriority;
      this.firstStartTime=-1;
      this.completionTime=-1;
   }

}