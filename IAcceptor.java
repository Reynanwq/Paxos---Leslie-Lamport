import java.util.Optional;
import java.util.Set;

public interface IAcceptor<T> {
    Optional<ProposalID> getPromisedIdAcceptor();
    Optional<ProposalID> getAcceptedIdAcceptor();
    Optional<T> getAcceptedValueAcceptor();
    Message receiveAcceptor(Message message);
    String getNetworkUidAcceptor();
    boolean sendPingAcceptor(String proposerUid);
    Proposer<T> convertToProposerAcceptor(int quorum, Set<String> acceptorsToPromise);
}
