import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class Node<T> {
    private final String networkUid;
    private final int quorumSize;
    private Role role;

    // Proposer specific fields
    private Optional<T> proposedValue = Optional.empty();
    private ProposalID proposalId;
    private ProposalID highestProposalId;
    private Optional<ProposalID> highestAcceptedId = Optional.empty();
    private Set<String> promisesReceived = new HashSet<>();
    private Set<String> nacksReceived = new HashSet<>();
    private Optional<Prepare> currentPrepare = Optional.empty();
    private Optional<Message> currentAccept = Optional.empty();
    private Set<String> acceptorsToPromise = new HashSet<>();
    private boolean leader = false;

    // Acceptor specific fields
    private Optional<ProposalID> promisedId = Optional.empty();
    private Optional<ProposalID> acceptedId = Optional.empty();
    private Optional<T> acceptedValue = Optional.empty();
    private Optional<String> promisedToProposer = Optional.empty();

    public Node(String networkUid, int quorumSize, Role role) {
        this.networkUid = networkUid;
        this.quorumSize = quorumSize;
        this.role = role;
        this.proposalId = new ProposalID(0, networkUid);
        this.highestProposalId = new ProposalID(0, networkUid);
    }

    // Role-specific methods
    public void setAcceptorsToPromise(Set<String> acceptors) {
        this.acceptorsToPromise = acceptors;
    }

    // Proposer methods
    public boolean isLeader() {
        return leader;
    }

    public Optional<T> getProposedValue() {
        return proposedValue;
    }

    public ProposalID getProposalId() {
        return proposalId;
    }

    public Optional<Prepare> getCurrentPrepare() {
        return currentPrepare;
    }

    public Optional<Message> getCurrentAccept() {
        return currentAccept;
    }

    public Optional<Message> proposeValue(T value) {
        if (role == Role.PROPOSER && !proposedValue.isPresent()) {
            proposedValue = Optional.of(value);
            if (leader) {
                currentAccept = Optional.of(new Accept<>(networkUid, proposalId, value));
                return currentAccept;
            }
        }
        return Optional.empty();
    }

    public Prepare prepare() {
        if (role == Role.PROPOSER) {
            leader = false;
            promisesReceived.clear();
            nacksReceived.clear();
            proposalId = new ProposalID(highestProposalId.getProposalNumber() + 1, networkUid);
            highestProposalId = proposalId;
            currentPrepare = Optional.of(new Prepare(networkUid, proposalId));
            return currentPrepare.get();
        }
        return null;
    }

    public void observeProposal(ProposalID proposalId) {
        if (proposalId.compareTo(highestProposalId) > 0) {
            highestProposalId = proposalId;
        }
    }

    public Optional<Message> receive(Message msg) {
        if (role == Role.PROPOSER) {
            if (msg instanceof Nack) {
                return receiveNack((Nack) msg);
            } else if (msg instanceof Promise) {
                return receivePromise((Promise<T>) msg);
            }
        } else if (role == Role.ACCEPTOR) {
            if (msg instanceof Prepare) {
                return receivePrepare((Prepare) msg);
            } else if (msg instanceof Accept) {
                return receiveAccept((Accept<T>) msg);
            }
        }
        return Optional.empty();
    }

    private Optional<Message> receiveNack(Nack msg) {
        if (role == Role.PROPOSER) {
            msg.getPromisedProposalId().ifPresent(this::observeProposal);
            if (msg.getProposalId().equals(proposalId)) {
                nacksReceived.add(msg.getNetworkUid());
                if (nacksReceived.size() == quorumSize) {
                    return Optional.of(prepare());
                }
            }
        }
        return Optional.empty();
    }

    private Optional<Message> receivePromise(Promise<T> msg) {
        if (role == Role.PROPOSER) {
            observeProposal(msg.getProposalId());
            if (!leader && msg.getProposalId().equals(proposalId) && !promisesReceived.contains(msg.getNetworkUid())) {
                promisesReceived.add(msg.getNetworkUid());
                if (msg.getLastAcceptedProposalId().isPresent()) {
                    ProposalID pid = msg.getLastAcceptedProposalId().get();
                    if (!highestAcceptedId.isPresent() || pid.compareTo(highestAcceptedId.get()) > 0) {
                        highestAcceptedId = Optional.of(pid);
                        proposedValue = msg.getLastAcceptedValue();
                    }
                }
                if (isQuorumReached()) {
                    leader = true;
                    if (proposedValue.isPresent()) {
                        currentAccept = Optional.of(new Accept<>(networkUid, proposalId, proposedValue.get()));
                        return currentAccept;
                    }
                }
            }
        }
        return Optional.empty();
    }

    public boolean isQuorumReached() {
        return promisesReceived.size() >= (quorumSize / 2 + 1);
    }

    public void processPromisesForSelectedAcceptors(List<Node<T>> nodes) {
        if (role == Role.PROPOSER) {
            for (Node<T> node : nodes) {
                if (node.getRole() == Role.ACCEPTOR && acceptorsToPromise.contains(node.getNetworkUid())) {
                    node.receive(new Promise<>(node.getNetworkUid(), proposalId, networkUid, Optional.empty(), Optional.empty()));
                }
            }
        }
    }

    public String getNetworkUid() {
        return networkUid;
    }

    // Acceptor methods
    private Message receivePrepare(Prepare msg) {
        if (role == Role.ACCEPTOR) {
            if (promisedId.isEmpty() || msg.getProposalId().compareTo(promisedId.get()) >= 0) {
                if (promisedToProposer.isPresent() && !promisedToProposer.get().equals(msg.getNetworkUid())) {
                    boolean proposerAtivo = sendPing(promisedToProposer.get());
                    if (!proposerAtivo) {
                        promisedId = Optional.empty();
                        acceptedId = Optional.empty();
                        acceptedValue = Optional.empty();
                        promisedToProposer = Optional.empty();
                    } else {
                        return new Nack(networkUid, msg.getProposalId(), msg.getNetworkUid(), promisedId);
                    }
                }
                promisedId = Optional.of(msg.getProposalId());
                promisedToProposer = Optional.of(msg.getNetworkUid());
                return new Promise<>(networkUid, msg.getProposalId(), msg.getNetworkUid(), acceptedId, acceptedValue);
            } else {
                return new Nack(networkUid, msg.getProposalId(), msg.getNetworkUid(), promisedId);
            }
        }
        return null;
    }

    private Message receiveAccept(Accept<T> msg) {
        if (role == Role.ACCEPTOR) {
            if (promisedId.isEmpty() || msg.getProposalId().compareTo(promisedId.get()) >= 0) {
                if (promisedToProposer.isPresent() && !promisedToProposer.get().equals(msg.getNetworkUid())) {
                    boolean proposerAtivo = sendPing(promisedToProposer.get());
                    if (!proposerAtivo) {
                        promisedId = Optional.empty();
                        acceptedId = Optional.empty();
                        acceptedValue = Optional.empty();
                        promisedToProposer = Optional.empty();
                    } else {
                        return new Nack(networkUid, msg.getProposalId(), msg.getNetworkUid(), promisedId);
                    }
                }
                promisedId = Optional.of(msg.getProposalId());
                acceptedId = Optional.of(msg.getProposalId());
                acceptedValue = Optional.of(msg.getProposalValue());
                return new Accepted<>(networkUid, msg.getProposalId(), msg.getProposalValue());
            } else {
                return new Nack(networkUid, msg.getProposalId(), msg.getNetworkUid(), promisedId);
            }
        }
        return null;
    }

    public boolean sendPing(String proposerUid) {
        // Simula a verificação da atividade do Proposer
        Node<?> proposer = NodeManager.getNode(proposerUid); // Obtém o Node com o UID
        if (proposer == null) {
            return false;
        }
        return proposer.isActive();
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public Node<T> convertToProposer(int quorumSize, Set<String> acceptorsToPromise) {
        if (role == Role.ACCEPTOR) {
            Node<T> proposer = new Node<>(this.networkUid, quorumSize, Role.PROPOSER);
            proposer.setAcceptorsToPromise(acceptorsToPromise);
            return proposer;
        }
        return null;
    }

    // Learner-specific methods
    public boolean receiveResolution(Resolution<T> resolution) {
        if (role == Role.LEARNER) {
            // Implement functionality to handle resolution if needed
            return true;
        }
        return false;
    }

    // Utility methods
    private boolean isActive() {
        // Simula a verificação da atividade do Node
        return true;
    }

    // Inner classes and enums
    public enum Role {
        PROPOSER,
        ACCEPTOR,
        LEARNER
    }

    public static class ProposalID {
        private final int proposalNumber;
        private final String networkUid;

        public ProposalID(int proposalNumber, String networkUid) {
            this.proposalNumber = proposalNumber;
            this.networkUid = networkUid;
        }

        public int getProposalNumber() {
            return proposalNumber;
        }

        public String getNetworkUid() {
            return networkUid;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            ProposalID that = (ProposalID) o;

            if (proposalNumber != that.proposalNumber) return false;
            return networkUid.equals(that.networkUid);
        }

        @Override
        public int hashCode() {
            int result = proposalNumber;
            result = 31 * result + networkUid.hashCode();
            return result;
        }

        public int compareTo(ProposalID other) {
            if (this.proposalNumber != other.proposalNumber) {
                return Integer.compare(this.proposalNumber, other.proposalNumber);
            }
            return this.networkUid.compareTo(other.networkUid);
        }
    }
}
