import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class Main {
    public static void main(String[] args) {
        // Criar instância do proposer
        Proposer<String> proposer = new Proposer<>("proposer1", 6); // Quórum: 6
        Proposer<String> proposer2 = new Proposer<>("proposer2", 6); // Quórum: 6

        

        // Criar instâncias dos acceptors
        List<Acceptor<String>> acceptors = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            acceptors.add(new Acceptor<>("acceptor" + i));
        }

        // Criar instâncias dos learners
        List<Learner<String>> learners = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            learners.add(new Learner<>("learner" + i, 6)); // Quórum: 6
        }

        // Simular a proposição de um valor
        proposer.proposeValue("valorA");

        // Preparar uma proposta
        Prepare prepare = proposer.prepare();
        System.out.println("Proposta enviada: " + prepare.getProposalId().getProposalNumber());

        // Acceptors recebem a proposta
        for (Acceptor<String> acceptor : acceptors) {
            acceptor.receive(prepare);
        }

        // Definir quais Acceptors devem fazer uma promessa
        Set<String> acceptorsToPromise = new HashSet<>();
        acceptorsToPromise.add("acceptor1");
        acceptorsToPromise.add("acceptor2");
        acceptorsToPromise.add("acceptor3");
        acceptorsToPromise.add("acceptor4");
        acceptorsToPromise.add("acceptor5");
        acceptorsToPromise.add("acceptor6");
        acceptorsToPromise.add("acceptor7");
        acceptorsToPromise.add("acceptor8");
        acceptorsToPromise.add("acceptor9");
        acceptorsToPromise.add("acceptor10");
        proposer.setAcceptorsToPromise(acceptorsToPromise);

        // Proposer processa respostas de Promise apenas para Acceptors selecionados
        proposer.processPromisesForSelectedAcceptors(acceptors);

        // Proposer envia Accept
        Optional<Message> acceptMsg = proposer.getCurrentAccept();
        //se é a promise é (n/2)+1
        if (acceptMsg.isPresent() && acceptMsg.get() instanceof Accept) {
            Accept<String> acceptMessage = (Accept<String>) acceptMsg.get();
            System.out.println("Proposta aceita: " + acceptMessage.getProposalValue());

            // Acceptors recebem e respondem com Accepted ou Nack
            for (Acceptor<String> acceptor : acceptors) {
                if (acceptorsToPromise.contains(acceptor.getNetworkUid())) {
                    //verifica se fez uma promessa
                    acceptor.receive(acceptMessage);
                    System.out.println("Acceptor " + acceptor.getNetworkUid() + " aceitou a proposta.");
                } else {
                    // verifica se não fez uma promessa
                    System.out.println("Acceptor " + acceptor.getNetworkUid() + " rejeitou a proposta.");
                    // Pode-se enviar um Nack para o Proposer se necessário
                    // proposer.receive(new Nack<>(acceptor.getNetworkUid(), acceptMessage.getProposalId(), "proposer1"));
                }
            }

            // Learners processam as respostas Accepted
            for (Learner<String> learner : learners) {
                for (Acceptor<String> acceptor : acceptors) {
                    learner.receive(new Accepted<>(acceptor.getNetworkUid(), acceptMessage.getProposalId(), acceptMessage.getProposalValue()));
                }
            }

            // Verificar a decisão final dos Learners
            for (Learner<String> learner : learners) {
                learner.getFinalValue().ifPresent(value -> {
                    System.out.println("Learner " + learner.getNetworkUid() + " decidiu valor final: " + value);
                });
            }
        } else {
            System.out.println("Nenhuma proposta aceita.");
        }

        // Simular a proposição de um valor pelo proposer2
        proposer2.proposeValue("valorB");

        // Preparar uma proposta
        Prepare prepare2 = proposer2.prepare();
        System.out.println("Proposta enviada por proposer2: " + prepare2.getProposalId().getProposalNumber());

        // Acceptors recebem a proposta
        for (Acceptor<String> acceptor : acceptors) {
            Message response = acceptor.receive(prepare2);
            if (response instanceof Nack) {
                System.out.println("Acceptor " + acceptor.getNetworkUid() + " rejeitou a proposta de proposer2.");
            }
        }

        // Adicionar os proposers ao gerenciador
        ProposerManager.addProposer(proposer);
        ProposerManager.addProposer(proposer2);

        // Remover um Proposer
        ProposerManager.removeProposer("proposer1");

        // Tentar remover um Proposer que não existe
        ProposerManager.removeProposer("proposer3");

    }
}
