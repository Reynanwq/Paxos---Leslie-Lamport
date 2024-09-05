public interface INode {
    void iniciar();
    void runNode(int nodeId);
    void removeNode(int nodeId);
    void checkAndHandleLeaderFailure();
    void initiateElection();
    void updateLeader();
    void r3Proposer(int nodeId);
    void r3Learner(int nodeId);
} 
