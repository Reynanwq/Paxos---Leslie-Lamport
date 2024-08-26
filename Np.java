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

public class Np<T> {
    private Proposer<T> proposer;
    private Acceptor<T> acceptor;
    private Learner<T> learner;
    private Function<String, T> converter; // Função para conversão de String para T

    public Np(String id, int quorum, Function<String, T> converter) {
        this.proposer = new Proposer<>(id, quorum);
        this.acceptor = new Acceptor<>(id);
        this.learner = new Learner<>(id, quorum);
        this.converter = converter; // Inicializa o converter
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

    public void proposeValue(String value) {
        T convertedValue = converter.apply(value); // Converte o valor para o tipo T
        proposer.proposeValueProposer(convertedValue); // Passa o valor convertido
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
            learner.receiveLearner(new Accepted<>(acceptor.getNetworkUidAcceptor(), accept.getProposalId(), accept.getProposalValue()));
            System.out.println("Proposta aceita: " + acceptMessaage.getProposalValue());

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
    

}
