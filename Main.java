import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class Main {
    public static void main(String[] args) throws InterruptedException {
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
        //Thread.sleep(2000); // Intervalo de 2 segundos

        // Preparar uma proposta
        Prepare prepare = proposer.prepare();
        System.out.println("Proposta enviada: " + prepare.getProposalId().getProposalNumber());
        //Thread.sleep(2000); // Intervalo de 2 segundos

        // Acceptors recebem a proposta
        for (Acceptor<String> acceptor : acceptors) {
            acceptor.receive(prepare);
        }
        //Thread.sleep(2000); // Intervalo de 2 segundos

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
        //Thread.sleep(2000); // Intervalo de 2 segundos

        // Proposer processa respostas de Promise apenas para Acceptors selecionados
        proposer.processPromisesForSelectedAcceptors(acceptors);
        //Thread.sleep(2000); // Intervalo de 2 segundos

        // Proposer envia Accept
        Optional<Message> acceptMsg = proposer.getCurrentAccept();
        if (acceptMsg.isPresent() && acceptMsg.get() instanceof Accept) {
            Accept<String> acceptMessage = (Accept<String>) acceptMsg.get();
            System.out.println("Proposta aceita: " + acceptMessage.getProposalValue());
            //Thread.sleep(2000); // Intervalo de 2 segundos

            // Acceptors recebem e respondem com Accepted ou Nack
            for (Acceptor<String> acceptor : acceptors) {
                if (acceptorsToPromise.contains(acceptor.getNetworkUid())) {
                    acceptor.receive(acceptMessage);
                    System.out.println("Acceptor " + acceptor.getNetworkUid() + " aceitou a proposta.");
                } else {
                    System.out.println("Acceptor " + acceptor.getNetworkUid() + " rejeitou a proposta.");
                }
                //Thread.sleep(2000); // Intervalo de 2 segundos
            }

            // Learners processam as respostas Accepted
            for (Learner<String> learner : learners) {
                for (Acceptor<String> acceptor : acceptors) {
                    learner.receive(new Accepted<>(acceptor.getNetworkUid(), acceptMessage.getProposalId(), acceptMessage.getProposalValue()));
                }
            }
            //Thread.sleep(2000); // Intervalo de 2 segundos

            // Verificar a decisão final dos Learners
            for (Learner<String> learner : learners) {
                learner.getFinalValue().ifPresent(value -> {
                    System.out.println("Learner " + learner.getNetworkUid() + " decidiu valor final: " + value);
                });
            }
            //Thread.sleep(2000); // Intervalo de 2 segundos
        } else {
            System.out.println("Nenhuma proposta aceita.");
        }

        
        
        

        // Simular a proposição de um valor pelo proposer2
        proposer2.proposeValue("valorB");
        Thread.sleep(2000); // Intervalo de 2 segundos

        // Preparar uma proposta
        Prepare prepare2 = proposer2.prepare();
        System.out.println("Proposta enviada por proposer2: " + prepare2.getProposalId().getProposalNumber());
        Thread.sleep(2000); // Intervalo de 2 segundos

        // Acceptors recebem a proposta
        for (Acceptor<String> acceptor : acceptors) {
            Message response = acceptor.receive(prepare2);
            if (response instanceof Nack) {
                System.out.println("Acceptor " + acceptor.getNetworkUid() + " rejeitou a proposta de proposer2.");
            }
            Thread.sleep(2000); // Intervalo de 2 segundos
        }

        // Adicionar os proposers ao gerenciador
        ProposerManager.addProposer(proposer);
        ProposerManager.addProposer(proposer2);
        Thread.sleep(2000); // Intervalo de 2 segundos

        // Remover um Proposer
        ProposerManager.removeProposer("proposer1");
        Thread.sleep(2000); // Intervalo de 2 segundos

        // Tentar remover um Proposer que não existe
        ProposerManager.removeProposer("proposer3");

        // Escolher um Acceptor para enviar o ping
        Acceptor<String> chosenAcceptor = acceptors.get(0);
        boolean proposerAtivo = chosenAcceptor.sendPing(proposer.getNetworkUid());
        if (proposerAtivo) {
            System.out.println("O Proposer está ativo e respondeu ao ping.");
        } else {
            System.out.println("O Proposer não respondeu ao ping. Ele pode estar inativo.");
        }
    }
}
