import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Objects;

public class Proposer<T> implements IProposer<T> {
    private final String networkUid;
    private final int quorumSize;
    private Optional<T> proposedValue = Optional.empty();
    private ProposalID proposalId;
    private ProposalID highestProposalId;
    private Optional<ProposalID> highestAcceptedId = Optional.empty();
    private Set<String> promisesReceived = new HashSet<>();
    private Set<String> nacksReceived = new HashSet<>();
    private Optional<Prepare> currentPrepare = Optional.empty();
    private Optional<Message> currentAccept = Optional.empty(); // Alterado para Message
    private Set<String> acceptorsToPromise = new HashSet<>(); // Adicionado para selecionar Acceptors

    private boolean leader = false;

    public Proposer(String networkUid, int quorumSize) {
        this.networkUid = networkUid;
        this.quorumSize = quorumSize;
        this.proposalId = new ProposalID(0, networkUid);
        this.highestProposalId = new ProposalID(0, networkUid);
    }

    // Adicionado para definir os Acceptors que devem fazer uma promessa
    @Override
    public void setAcceptorsToPromiseProposer(Set<String> acceptors) {
        this.acceptorsToPromise = acceptors;
    }

    @Override
    public boolean isLeaderProposer() {
        return leader;
    }

    @Override
    public Optional<T> getProposedValueProposer() {
        return proposedValue;
    }

    @Override
    public ProposalID getProposalIdProposer() {
        return proposalId;
    }

    @Override
    public Optional<Prepare> getCurrentPrepareProposer() {
        return currentPrepare;
    }

    @Override
    public Optional<Message> getCurrentAcceptProposer() { // Alterado para Message
        return currentAccept;
    }

    @Override
    public Optional<Message> proposeValueProposer(ProposalID id, T value) {
        if (!proposedValue.isPresent()) {
            proposedValue = Optional.of(value); 
            if (leader) {
                proposalId = id;
                currentAccept = Optional.of(new Accept<>(networkUid, proposalId, value));
                return currentAccept;
            }
        }
        return Optional.empty();
    }

    @Override
    public Prepare prepareProposer() {
        leader = false;
        promisesReceived.clear();
        nacksReceived.clear();
        proposalId = new ProposalID(highestProposalId.getProposalNumber() + 1, networkUid);
        highestProposalId = proposalId;
        currentPrepare = Optional.of(new Prepare(networkUid, proposalId));

        return currentPrepare.get();
    }

    @Override
    public void observeProposalProposer(ProposalID proposalId) {
        if (proposalId.compareTo(highestProposalId) > 0) {
            highestProposalId = proposalId;
        }
    }

    @Override
    public Optional<Message> receiveProposer(Message msg) {
        if (msg instanceof Nack) {
            return receiveNackProposer((Nack) msg);
        } else if (msg instanceof Promise) {
            return receivePromiseProposer((Promise<T>) msg);
        }
        return Optional.empty();
    }

    private Optional<Message> receiveNackProposer(Nack msg) {
        msg.getPromisedProposalId().ifPresent(this::observeProposalProposer);

        if (msg.getProposalId().equals(proposalId)) {
            nacksReceived.add(msg.getNetworkUid());
            if (nacksReceived.size() >= quorumSize) {
                return Optional.of(prepareProposer());
            }
        }
        return Optional.empty();
    }

    private Optional<Message> receivePromiseProposer(Promise<T> msg) {
        observeProposalProposer(msg.getProposalId());

        if (!leader && msg.getProposalId().equals(proposalId) && !promisesReceived.contains(msg.getNetworkUid())) {
            promisesReceived.add(msg.getNetworkUid());

            if (msg.getLastAcceptedProposalId().isPresent()) {
                ProposalID pid = msg.getLastAcceptedProposalId().get();
                if (!highestAcceptedId.isPresent() || pid.compareTo(highestAcceptedId.get()) > 0) {
                    highestAcceptedId = Optional.of(pid);
                    proposedValue = msg.getLastAcceptedValue();
                }
            }

            // Verifica se o quorum foi alcançado
            if (isQuorumReachedProposer()) {
                leader = true;
                if (proposedValue.isPresent()) {
                    currentAccept = Optional.of(new Accept<>(networkUid, proposalId, proposedValue.get()));
                    return currentAccept;
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public boolean isQuorumReachedProposer() {
        // Verifica se o número de promessas recebidas é maior que a quantidade de acceptors / 2 + 1
        return promisesReceived.size() >= (quorumSize / 2 + 1); // Corrigido para >=
    }

    // Método para processar apenas Acceptors selecionados
    @Override
    public void processPromisesForSelectedAcceptorsProposer(List<Acceptor<T>> acceptors) {
        for (Acceptor<T> acceptor : acceptors) {
            if (acceptorsToPromise.contains(acceptor.getNetworkUidAcceptor())) {
                receiveProposer(new Promise<>(acceptor.getNetworkUidAcceptor(), proposalId, networkUid, Optional.empty(), Optional.empty()));
            }
        }
    }

    @Override
    public String getNetworkUidProposer() {
        return networkUid;
    }

    public boolean isAcceptorsSizeValid(Set<String> acceptorsToPromise, int quorum) {
        return (acceptorsToPromise.size() >= quorum);
    }
    
    // Adicionado para remover o Proposer e limpar o estado
    @Override
    public void removeProposerProposer() {
        // Informar no terminal que o Proposer está sendo removido
        System.out.println("Removendo o Proposer com UID: " + networkUid);
        
        // Limpar o estado do Proposer
        proposedValue = Optional.empty();
        proposalId = new ProposalID(0, networkUid); // Resetar proposalId
        highestProposalId = new ProposalID(0, networkUid); // Resetar highestProposalId
        highestAcceptedId = Optional.empty();
        promisesReceived.clear();
        nacksReceived.clear();
        currentPrepare = Optional.empty();
        currentAccept = Optional.empty();
    }

    @Override
    public boolean receivePingProposer(String acceptorUid) {
        System.out.println("Proposer " + networkUid + " recebeu ping do Acceptor " + acceptorUid);
        // Se o Proposer estiver ativo, ele responde como ativo
        return true;
    }

    @Override
    public boolean isActiveProposer() {
        // Adicione a lógica para determinar se o Proposer ainda está ativo
        // Por exemplo, você pode verificar se o `proposalId` e outros campos estão definidos corretamente
        return !proposalId.equals(new ProposalID(0, networkUid)); // Exemplo simplificado
    }


    // Novo método getQuorum
    public int getQuorum() {
        return quorumSize;
    }
}
