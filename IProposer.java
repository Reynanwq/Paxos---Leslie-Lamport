import java.util.Optional;
import java.util.Set;
import java.util.List;

public interface IProposer<T> {
    Prepare prepareProposer();
    void setAcceptorsToPromiseProposer(Set<String> acceptorsToPromise);
    void processPromisesForSelectedAcceptorsProposer(List<Acceptor<T>> acceptors);
    void processPromisesForSelectedAcceptorsProposerN(List<Node<T>> nodes);
    Optional<Message> getCurrentAcceptProposer();
    String getNetworkUidProposer();
    boolean isLeaderProposer();
    Optional<T> getProposedValueProposer();
    Optional<Prepare> getCurrentPrepareProposer();
    boolean isActiveProposer();
    boolean receivePingProposer(String acceptorUid);
    void removeProposerProposer();
    ProposalID getProposalIdProposer();
    Optional<Message> proposeValueProposer(T value);
    void observeProposalProposer(ProposalID proposalId);
    Optional<Message> receiveProposer(Message msg);
    boolean isQuorumReachedProposer();
}
