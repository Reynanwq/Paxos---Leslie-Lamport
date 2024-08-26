import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public abstract class Message {}

class Prepare extends Message {
    private final String networkUid;
    private final ProposalID proposalId;

    public Prepare(String networkUid, ProposalID proposalId) {
        this.networkUid = networkUid;
        this.proposalId = proposalId;
    }

    public String getNetworkUid() {
        return networkUid;
    }

    public ProposalID getProposalId() {
        return proposalId;
    }
}

class Promise<T> extends Message {
    private final String networkUid;
    private final ProposalID proposalId;
    private final String proposerUid;
    private final Optional<ProposalID> lastAcceptedProposalId;
    private final Optional<T> lastAcceptedValue;

    public Promise(String networkUid, ProposalID proposalId, String proposerUid, Optional<ProposalID> lastAcceptedProposalId, Optional<T> lastAcceptedValue) {
        this.networkUid = networkUid;
        this.proposalId = proposalId;
        this.proposerUid = proposerUid;
        this.lastAcceptedProposalId = lastAcceptedProposalId;
        this.lastAcceptedValue = lastAcceptedValue;
    }

    public String getNetworkUid() {
        return networkUid;
    }

    public ProposalID getProposalId() {
        return proposalId;
    }

    public Optional<ProposalID> getLastAcceptedProposalId() {
        return lastAcceptedProposalId;
    }

    public Optional<T> getLastAcceptedValue() {
        return lastAcceptedValue;
    }
}

class Nack extends Message {
    private final String networkUid;
    private final ProposalID proposalId;
    private final String proposerUid;
    private final Optional<ProposalID> promisedProposalId;

    public Nack(String networkUid, ProposalID proposalId, String proposerUid, Optional<ProposalID> promisedProposalId) {
        this.networkUid = networkUid;
        this.proposalId = proposalId;
        this.proposerUid = proposerUid;
        this.promisedProposalId = promisedProposalId;
    }

    public String getNetworkUid() {
        return networkUid;
    }

    public ProposalID getProposalId() {
        return proposalId;
    }

    public Optional<ProposalID> getPromisedProposalId() {
        return promisedProposalId;
    }
}

class Accept<T> extends Message {
    private final String networkUid;
    private final ProposalID proposalId;
    private final T proposalValue;

    public Accept(String networkUid, ProposalID proposalId, T proposalValue) {
        this.networkUid = networkUid;
        this.proposalId = proposalId;
        this.proposalValue = proposalValue;
    }

    public String getNetworkUid() {
        return networkUid;
    }

    public ProposalID getProposalId() {
        return proposalId;
    }

    public T getProposalValue() {
        return proposalValue;
    }
}

class Accepted<T> extends Message {
    private final String networkUid;
    private final ProposalID proposalId;
    private final T proposalValue;

    public Accepted(String networkUid, ProposalID proposalId, T proposalValue) {
        this.networkUid = networkUid;
        this.proposalId = proposalId;
        this.proposalValue = proposalValue;
    }

    public String getNetworkUid() {
        return networkUid;
    }

    public ProposalID getProposalId() {
        return proposalId;
    }

    public T getProposalValue() {
        return proposalValue;
    }
}

class Resolution<T> {
    private final String networkUid;
    private final T resolvedValue;

    public Resolution(String networkUid, T resolvedValue) {
        this.networkUid = networkUid;
        this.resolvedValue = resolvedValue;
    }

    public String getNetworkUid() {
        return networkUid;
    }

    public T getResolvedValue() {
        return resolvedValue;
    }
}