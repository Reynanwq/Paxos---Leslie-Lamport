import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Objects;


public class Proposer<T> {
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
    public void setAcceptorsToPromise(Set<String> acceptors) {
        this.acceptorsToPromise = acceptors;
    }

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

    public Optional<Message> getCurrentAccept() { // Alterado para Message
        return currentAccept;
    }

    public Optional<Message> proposeValue(T value) {
        if (!proposedValue.isPresent()) {
            proposedValue = Optional.of(value);
            if (leader) {
                currentAccept = Optional.of(new Accept<>(networkUid, proposalId, value));
                return currentAccept;
            }
        }
        return Optional.empty();
    }

    public Prepare prepare() {
        leader = false;
        promisesReceived.clear();
        nacksReceived.clear();
        proposalId = new ProposalID(highestProposalId.getProposalNumber() + 1, networkUid);
        highestProposalId = proposalId;
        currentPrepare = Optional.of(new Prepare(networkUid, proposalId));

        return currentPrepare.get();
    }

    public void observeProposal(ProposalID proposalId) {
        if (proposalId.compareTo(highestProposalId) > 0) {
            highestProposalId = proposalId;
        }
    }

    public Optional<Message> receive(Message msg) {
        if (msg instanceof Nack) {
            return receiveNack((Nack) msg);
        } else if (msg instanceof Promise) {
            return receivePromise((Promise<T>) msg);
        }
        return Optional.empty();
    }

    private Optional<Message> receiveNack(Nack msg) {
        msg.getPromisedProposalId().ifPresent(this::observeProposal);

        if (msg.getProposalId().equals(proposalId)) {
            nacksReceived.add(msg.getNetworkUid());
            if (nacksReceived.size() == quorumSize) {
                return Optional.of(prepare());
            }
        }
        return Optional.empty();
    }

    private Optional<Message> receivePromise(Promise<T> msg) {
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

            // Verifica se o quorum foi alcançado
            if (isQuorumReached()) {
                leader = true;
                if (proposedValue.isPresent()) {
                    currentAccept = Optional.of(new Accept<>(networkUid, proposalId, proposedValue.get()));
                    return currentAccept;
                }
            }
        }
        return Optional.empty();
    }

    public boolean isQuorumReached() {
        // Verifica se o número de promessas recebidas é maior que a quantidade de acceptors / 2 + 1
        return promisesReceived.size() >= (quorumSize / 2 + 1); // Corrigido para >=
    }

    // Método para processar apenas Acceptors selecionados
    public void processPromisesForSelectedAcceptors(List<Acceptor<T>> acceptors) {
        for (Acceptor<T> acceptor : acceptors) {
            if (acceptorsToPromise.contains(acceptor.getNetworkUid())) {
                receive(new Promise<>(acceptor.getNetworkUid(), proposalId, networkUid, Optional.empty(), Optional.empty()));
            }
        }
    }

    public String getNetworkUid() {
        return networkUid;
    }


    // Adicionado para remover o Proposer e limpar o estado
    public void removeProposer() {
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

        // Notificar outros componentes se necessário
        System.out.println("Estado do Proposer com UID " + networkUid + " foi limpo.");
    }

    public boolean receivePing(String acceptorUid) {
        System.out.println("Proposer " + networkUid + " recebeu ping do Acceptor " + acceptorUid);
        // Se o Proposer estiver ativo, ele responde como ativo
        return true;
    }

    public boolean isActive() {
        // Adicione a lógica para determinar se o Proposer ainda está ativo
        // Por exemplo, você pode verificar se o `proposalId` e outros campos estão definidos corretamente
        return !proposalId.equals(new ProposalID(0, networkUid)); // Exemplo simplificado
    }
    
    
}
