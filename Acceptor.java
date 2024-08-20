import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Objects;


public class Acceptor<T> {
    private Optional<ProposalID> promisedId = Optional.empty();
    private Optional<ProposalID> acceptedId = Optional.empty();
    private Optional<T> acceptedValue = Optional.empty();
    private final String networkUid;

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
            promisedId = Optional.of(msg.getProposalId());
            return new Promise<>(networkUid, msg.getProposalId(), msg.getNetworkUid(), acceptedId, acceptedValue);
        } else {
            return new Nack(networkUid, msg.getProposalId(), msg.getNetworkUid(), promisedId);
        }
    }

    private Message receiveAccept(Accept<T> msg) {
        if (promisedId.isEmpty() || msg.getProposalId().compareTo(promisedId.get()) >= 0) {
            promisedId = Optional.of(msg.getProposalId());
            acceptedId = Optional.of(msg.getProposalId());
            acceptedValue = Optional.of(msg.getProposalValue());
            return new Accepted<>(networkUid, msg.getProposalId(), msg.getProposalValue());
        } else {
            return new Nack(networkUid, msg.getProposalId(), msg.getNetworkUid(), promisedId);
        }
    }


}

