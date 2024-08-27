import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Objects;
import java.util.Random;

public class Np<T> {
    private static int counter = 0;
    private final int idNum;
    private static Set<Integer> generatedIds = new HashSet<>(); // Conjunto de IDs gerados
    private static Set<Integer> returnedIds = new HashSet<>(); // Conjunto de IDs retornados
    private Proposer<T> proposer;
    private Acceptor<T> acceptor;
    private Learner<T> learner;
    private Function<String, T> converter; // Função para conversão de String para T
     // Lista para armazenar os ProposalIDs
     private List<ProposalID> proposalIdList = new ArrayList<>();


    // Armazena os valores propostos
    private ProposalID lastProposalId;
    private T lastConvertedValue;
    private int lastId;
    private String lastValue;

    public Np(String id, int quorum, Function<String, T> converter, int idNum) {
        this.idNum = idNum;
        this.proposer = new Proposer<>(id, quorum);
        this.acceptor = new Acceptor<>(id);
        this.learner = new Learner<>(id, quorum);
        this.converter = converter; // Inicializa o converter
        generatedIds.add(this.idNum);
    }

    public Proposer<T> getProposer() {
        return proposer;
    }

    public Acceptor<T> getAcceptor() {
        return acceptor;
    }

    public Learner<T> getLearner() {
        return learner;
    }

    public void setAcceptorsToPromise(Set<String> acceptorsToPromise) {
        proposer.setAcceptorsToPromiseProposer(acceptorsToPromise);
    }

    /*public void proposeValue(String value) {
        proposer.proposeValueProposer(value);
    }*/

    /* public void proposeValue(int id, String value) {
        T convertedValue = converter.apply(value); // Converte o valor para o tipo T
        ProposalID proposalId = new ProposalID(id, proposer.getNetworkUidProposer());
        proposer.proposeValueProposer(proposalId, convertedValue); // Passa o valor convertido
    }*/
    public void proposeValue(int id, String value) {
        lastId = id; // Armazena o id
        lastValue = value; // Armazena o valor
        lastConvertedValue = converter.apply(value); // Converte o valor para o tipo T
        lastProposalId = new ProposalID(id, proposer.getNetworkUidProposer()); // Cria ProposalID
        
        // Passa o valor convertido e ProposalID para o Proposer
        proposer.proposeValueProposer(lastProposalId, lastConvertedValue);
        // Chama o método para armazenar o ProposalID na lista
        storeProposalIdIfGreater(lastProposalId);
    }

    // Novo método para imprimir os valores passados em proposeValue
    public void printProposedValues() {
        System.out.println("Proposta enviada: (" + lastId + "," + lastValue + ")");
        //System.out.println("Last Value: " + lastValue);
        //System.out.println("Last Converted Value: " + lastConvertedValue);
       // System.out.println("Last ProposalID: " + lastProposalId);
    }

    // Método que armazena o ProposalID se for maior que o existente na lista
private void storeProposalIdIfGreater(ProposalID newProposalId) {
    if (proposalIdList.isEmpty()) {
        proposalIdList.add(newProposalId);
        System.out.println("Valor de Proposta armazenada: " + newProposalId.getId());
    } else {
        ProposalID currentProposalId = proposalIdList.get(0);
        if (newProposalId.compareTo(currentProposalId) > 0) {
            proposalIdList.set(0, newProposalId); // Substitui o valor atual
            System.out.println("ProposalID substituído e armazenado: " + newProposalId.getId());
        } else {
            System.out.println("Não aceito. ProposalID menor ou igual ao já presente.");
        }
    }
}

    public void receivePrepare(Prepare prepare) {
        acceptor.receiveAcceptor(prepare);
    }

    /*public void processPromises(Set<Acceptor<T>> acceptors) {
        proposer.processPromisesForSelectedAcceptorsProposer(acceptors);
    }*/

    public void processPromises(Set<Acceptor<T>> acceptors) {
        // Converte Set para List
        List<Acceptor<T>> acceptorList = new ArrayList<>(acceptors);
        proposer.processPromisesForSelectedAcceptorsProposer(acceptorList);
    }

    public Optional<Message> getCurrentAccept() {
        return proposer.getCurrentAcceptProposer();
    }

    public void receiveAccept(Optional<Message> acceptMessage) {
        if (acceptMessage.isPresent() && acceptMessage.get() instanceof Accept) {
            Accept<T> accept = (Accept<T>) acceptMessage.get();
            Accept<String> acceptMessaage = (Accept<String>) acceptMessage.get();
            acceptor.receiveAcceptor(accept);
            //learner.receiveLearner(new Accepted<>(acceptor.getNetworkUidAcceptor(), accept.getProposalId(), accept.getProposalValue()));
        }
    }

    public void checkFinalValues() {
        learner.getFinalValueLearner().ifPresent(value -> {
            System.out.println("Learner " + learner.getNetworkUidLearner() + " decidiu valor final: " + value);
        });
    }

    public Optional<Resolution<T>> processAcceptedMessage(Accepted<T> acceptedMessage) {
        // Chama o método receiveLearner da classe Learner e retorna o resultado
        return learner.receiveLearner(acceptedMessage);
    }


    // Método que apenas chama isAcceptorsSizeValid
    public boolean checkAcceptorsSize(Set<String> acceptorsToPromise, int quorum) {
        return proposer.isAcceptorsSizeValid(acceptorsToPromise, quorum);
    }
    
    public int getIdNum() {
        return idNum; // Retorna o identificador da instância
    }



    public static synchronized int generateUniqueId() {
        Random random = new Random();
        int uniqueId;
        do {
            int randomId = random.nextInt(10000); // Gera um número aleatório (pode ajustar o limite)
            uniqueId = randomId + counter; // Adiciona o contador ao número aleatório
        } while (generatedIds.contains(uniqueId)); // Garante que o ID seja único
        generatedIds.add(uniqueId); // Adiciona o ID gerado ao conjunto
        counter++; // Incrementa o contador para a próxima geração
        return uniqueId; // Retorna o identificador único
    }

    public static Optional<Integer> findSmallestIdNotReturned() {
        System.out.println("Generated IDs: " + generatedIds);
        System.out.println("Returned IDs: " + returnedIds);
    
        Optional<Integer> smallestId = generatedIds.stream()
                                                    .filter(id -> !returnedIds.contains(id))
                                                    .min(Integer::compareTo);
        
        if (!smallestId.isPresent()) {
            System.out.println("No available ID found.");
        }
    
        return smallestId;
    }
    
    // Método para adicionar o menor ID à lista de IDs retornados
    public static void addIdToReturned() {
        findSmallestIdNotReturned().ifPresent(id -> returnedIds.add(id));
    }

     // Método para obter os IDs retornados
     public static Set<Integer> getReturnedIds() {
        return new HashSet<>(returnedIds);
    }

    

    
}