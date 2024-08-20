import java.util.Optional;

public class Acceptor<T> {
    private Optional<ProposalID> promisedId = Optional.empty();
    private Optional<ProposalID> acceptedId = Optional.empty();
    private Optional<T> acceptedValue = Optional.empty();
    private final String networkUid;
    private Optional<String> promisedToProposer = Optional.empty(); // Adicionando campo para rastrear o Proposer

    public Acceptor(String networkUid) {
        this.networkUid = networkUid;
    }

    public String getNetworkUid() {
        return networkUid;
    }

    public Message receive(Message msg) {
        if (msg instanceof Prepare) {
            return receivePrepare((Prepare) msg);
        } else if (msg instanceof Accept) {
            return receiveAccept((Accept<T>) msg);
        }
        return null;
    }

    private Message receivePrepare(Prepare msg) {
        if (promisedId.isEmpty() || msg.getProposalId().compareTo(promisedId.get()) >= 0) {
            // Verificar se o Acceptor já prometeu para outro Proposer
            if (promisedToProposer.isPresent() && !promisedToProposer.get().equals(msg.getNetworkUid())) {
                // Se já prometeu para outro Proposer, retornar Nack
                return new Nack(networkUid, msg.getProposalId(), msg.getNetworkUid(), promisedId);
            }
            promisedId = Optional.of(msg.getProposalId());
            promisedToProposer = Optional.of(msg.getNetworkUid());
            return new Promise<>(networkUid, msg.getProposalId(), msg.getNetworkUid(), acceptedId, acceptedValue);
        } else {
            return new Nack(networkUid, msg.getProposalId(), msg.getNetworkUid(), promisedId);
        }
    }

    private Message receiveAccept(Accept<T> msg) {
        if (promisedId.isEmpty() || msg.getProposalId().compareTo(promisedId.get()) >= 0) {
            // Verificar se o Acceptor já prometeu para outro Proposer
            if (promisedToProposer.isPresent() && !promisedToProposer.get().equals(msg.getNetworkUid())) {
                // Se já prometeu para outro Proposer, retornar Nack
                return new Nack(networkUid, msg.getProposalId(), msg.getNetworkUid(), promisedId);
            }
            promisedId = Optional.of(msg.getProposalId());
            acceptedId = Optional.of(msg.getProposalId());
            acceptedValue = Optional.of(msg.getProposalValue());
            return new Accepted<>(networkUid, msg.getProposalId(), msg.getProposalValue());
        } else {
            return new Nack(networkUid, msg.getProposalId(), msg.getNetworkUid(), promisedId);
        }
    }
}
