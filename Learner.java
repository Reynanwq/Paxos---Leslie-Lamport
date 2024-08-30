import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class Learner<T> implements ILearner<T> {
    private final String networkUid;
    private final int quorumSize;

    private static class ProposalStatus<T> {
        int acceptCount;
        int retainCount;
        Set<String> acceptors;
        T value;

        ProposalStatus(int acceptCount, int retainCount, Set<String> acceptors, T value) {
            this.acceptCount = acceptCount;
            this.retainCount = retainCount;
            this.acceptors = acceptors;
            this.value = value;
        }
    }

    private final Map<ProposalID, ProposalStatus<T>> proposals = new HashMap<>();
    private final Map<String, ProposalID> acceptors = new HashMap<>();
    private Optional<ProposalID> finalProposalId = Optional.empty();
    private Optional<T> finalValue = Optional.empty();
    private Set<String> finalAcceptors = new HashSet<>();

    public Learner(String networkUid, int quorumSize) {
        this.networkUid = networkUid;
        this.quorumSize = quorumSize;
    }

    @Override
    public Optional<Resolution<T>> receiveLearner(Accepted<T> msg) {
        finalProposalId = Optional.empty();
        finalValue = Optional.empty();
         
        if (finalValue.isPresent()) {
            if (msg.getProposalId().compareTo(finalProposalId.get()) >= 0 && msg.getProposalValue().equals(finalValue.get())) {
                finalAcceptors.add(msg.getNetworkUid());
            }
            return Optional.of(new Resolution<>(networkUid, finalValue.get()));
        }

        ProposalID lastPid = acceptors.get(msg.getNetworkUid());

        if (lastPid != null && msg.getProposalId().compareTo(lastPid) <= 0) {
            return Optional.empty(); 
        }

        acceptors.put(msg.getNetworkUid(), msg.getProposalId());

        if (lastPid != null) {
            ProposalStatus<T> pstatus = proposals.get(lastPid);
            if (pstatus != null) {
                pstatus.retainCount -= 1;
                pstatus.acceptors.remove(msg.getNetworkUid());
                if (pstatus.retainCount == 0) {
                    proposals.remove(lastPid);
                }
            }
        }

        proposals.putIfAbsent(msg.getProposalId(), new ProposalStatus<>(0, 0, new HashSet<>(), msg.getProposalValue()));

        ProposalStatus<T> pstatus = proposals.get(msg.getProposalId());
        if (pstatus != null) {
            pstatus.acceptCount += 1;
            pstatus.retainCount += 1;
            pstatus.acceptors.add(msg.getNetworkUid());

            if (pstatus.acceptCount >= quorumSize) {
                finalProposalId = Optional.of(msg.getProposalId());
                finalValue = Optional.of(msg.getProposalValue());
                finalAcceptors = pstatus.acceptors;

                proposals.clear();
                acceptors.clear();

                // Verifica se o número de Acceptors que aceitaram é suficiente
                if (finalAcceptors.size() >= (quorumSize / 2) + 1) {
                    return Optional.of(new Resolution<>(networkUid, msg.getProposalValue()));
                } else {
                    System.out.println("Número de Acceptors que aceitaram não é suficiente para consenso.");
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public Optional<T> getFinalValueLearner() {
        return finalValue;
    }

    @Override
    public String getNetworkUidLearner() {
        return networkUid;
    }

    public void removeFinalDecision() {
        proposals.clear();
        acceptors.clear();
        finalProposalId = Optional.empty();
        finalValue = Optional.empty();
        finalAcceptors.clear();
        System.out.println("Decisão final removida e todos os dados foram limpos.");
    }
}
