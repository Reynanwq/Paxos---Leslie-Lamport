/*import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class Node<T> implements IAcceptor<T>, IProposer<T>, ILearner<T> {
    // Estado compartilhado entre as funcionalidades
    private final String networkUid;
    private int quorumSize;

    // Estado específico do Proposer
    private Optional<T> proposedValue = Optional.empty();
    private ProposalID proposalId;
    private ProposalID highestProposalId;
    private Optional<ProposalID> highestAcceptedId = Optional.empty();
    private Set<String> promisesReceived;
    private Set<String> nacksReceived;
    private Optional<Prepare> currentPrepare = Optional.empty();
    private Optional<Message> currentAccept = Optional.empty();
    private boolean leader = false;
    private Set<String> acceptorsToPromise = new HashSet<>(); // Adicionado para selecionar Acceptors

    // Estado específico do Acceptor
    private Optional<ProposalID> promisedId = Optional.empty();
    private Optional<ProposalID> acceptedId = Optional.empty();
    private Optional<T> acceptedValue = Optional.empty();
    private Optional<String> promisedToProposer = Optional.empty();

    // Estado específico do Learner
    private Optional<T> finalValue = Optional.empty();

    // Adicione aqui o estado necessário para o Learner, se houver

    public Node(String networkUid, int quorumSize) {
        this.networkUid = networkUid;
        this.quorumSize = quorumSize;
        this.proposalId = new ProposalID(0, networkUid);
        this.highestProposalId = new ProposalID(0, networkUid);
    }

    // Métodos do Proposer
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
    public Optional<Message> getCurrentAcceptProposer() {
        return currentAccept;
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
    public Optional<Message> proposeValueProposer(ProposalID id, T value) {
        if (!proposedValue.isPresent()) {
            proposedValue = Optional.of(value); 
            if (leader) {
                //proposalId = id;
                currentAccept = Optional.of(new Accept<>(networkUid, id, value));
                return currentAccept;
            }
        }
        return Optional.empty();
    }

    @Override
    public boolean isQuorumReachedProposer() {
        return promisesReceived.size() >= (quorumSize / 2 + 1);
    }

    // Métodos do Acceptor
    @Override
    public Optional<ProposalID> getPromisedIdAcceptor() {
        return promisedId;
    }

    @Override
    public Optional<ProposalID> getAcceptedIdAcceptor() {
        return acceptedId;
    }

    @Override
    public Optional<T> getAcceptedValueAcceptor() {
        return acceptedValue;
    }

    @Override
    public String getNetworkUidAcceptor() {
        return networkUid;
    }

    @Override
    public Message receiveAcceptor(Message msg) {
        if (msg instanceof Prepare) {
            return receivePrepareAcceptor((Prepare) msg);
        } else if (msg instanceof Accept) {
            return receiveAcceptAcceptor((Accept<T>) msg);
        }
        return null;
    }

    private Message receivePrepareAcceptor(Prepare msg) {
        if (promisedId.isEmpty() || msg.getProposalId().compareTo(promisedId.get()) >= 0) {
            if (promisedToProposer.isPresent() && !promisedToProposer.get().equals(msg.getNetworkUid())) {
                boolean proposerAtivo = sendPingAcceptor(promisedToProposer.get());
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

    private Message receiveAcceptAcceptor(Accept<T> msg) {
        if (promisedId.isEmpty() || msg.getProposalId().compareTo(promisedId.get()) >= 0) {
            if (promisedToProposer.isPresent() && !promisedToProposer.get().equals(msg.getNetworkUid())) {
                boolean proposerAtivo = sendPingAcceptor(promisedToProposer.get());
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

    @Override
    public boolean sendPingAcceptor(String proposerUid) {
        Proposer<?> proposer = ProposerManager.getProposer(proposerUid);
        if (proposer == null) {
            return false;
        }
        return proposer.isActiveProposer();
    }

    // Métodos do Learner
    // Adicione os métodos do Learner conforme necessário

    @Override
    public String getNetworkUidProposer() {
        return networkUid;
    }
    
    // Adicione outros métodos conforme necessário para integrar as funcionalidades
    // Exemplo de implementação para alguns dos métodos

    @Override
    public String getNetworkUidLearner() {
        // Retorna o identificador de rede para o Learner
        return this.networkUid;
    }

    @Override
    public void observeProposalProposer(ProposalID proposalId) {
        // Implementa a lógica para observar uma proposta
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
            if (nacksReceived.size() == quorumSize) {
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
    public Optional<Resolution<T>> receiveLearner(Accepted<T> msg) {
        // Cria uma instância do Learner para delegar a lógica de aprendizado
        Learner<T> learner = new Learner<>(networkUid, quorumSize);
    
        // Chama o método receiveLearner do Learner e retorna o resultado
        return learner.receiveLearner(msg);
    }
    

    @Override
    public boolean isActiveProposer() {
        // Adicione a lógica para determinar se o Proposer ainda está ativo
        // Por exemplo, você pode verificar se o `proposalId` e outros campos estão definidos corretamente
        return !proposalId.equals(new ProposalID(0, networkUid)); // Exemplo simplificado
    }

    // Adicionado para definir os Acceptors que devem fazer uma promessa
    @Override
    public void setAcceptorsToPromiseProposer(Set<String> acceptors) {
        this.acceptorsToPromise = acceptors;
    }

    @Override
    public Proposer<T> convertToProposerAcceptor(int quorumSize, Set<String> acceptorsToPromise) {
    // Ajusta o estado interno do Node para atuar como um Proposer
    this.quorumSize = quorumSize;
    this.acceptorsToPromise = acceptorsToPromise;
    
    // Cria um novo Proposer com o mesmo networkUid do Node
    Proposer<T> proposer = new Proposer<>(this.networkUid, quorumSize);
    return proposer;
}

        @Override
    public boolean receivePingProposer(String acceptorUid) {
        System.out.println("Proposer " + networkUid + " recebeu ping do Acceptor " + acceptorUid);
        // Se o Proposer estiver ativo, ele responde como ativo
        return true;
    }

 // Método para processar apenas Acceptors selecionados
    @Override
    public void processPromisesForSelectedAcceptorsProposerN(List<Node<T>> nodes) {
        for (Node<T> node : nodes) {
            if (acceptorsToPromise.contains(node.getNetworkUidAcceptor())) {
                receiveProposer(new Promise<>(node.getNetworkUidAcceptor(), proposalId, networkUid, Optional.empty(), Optional.empty()));
            }
        }
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

        // Notificar outros componentes se necessário
        System.out.println("Estado do Proposer com UID " + networkUid + " foi limpo.");
    }

    @Override
    public Optional<T> getFinalValueLearner() {
        return finalValue;
    }

}*/