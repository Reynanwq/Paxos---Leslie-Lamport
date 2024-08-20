import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Objects;

public class Main {
    public static void main(String[] args) {
        // Criar instância do proposer
        Proposer<String> proposer = new Proposer<>("proposer1", 6); // Quórum: 6

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

        // Proposer processa respostas de Promise
        for (int i = 0; i < acceptors.size(); i++) { // Alterado para processar apenas acceptor1 e acceptor2
            proposer.receive(new Promise<>("acceptor" + (i + 1), prepare.getProposalId(), "proposer1", Optional.empty(), Optional.empty()));
        }


        // Proposer envia Accept
        Optional<Message> acceptMsg = proposer.getCurrentAccept();
        if (acceptMsg.isPresent() && acceptMsg.get() instanceof Accept) {
            Accept<String> acceptMessage = (Accept<String>) acceptMsg.get();
            System.out.println("Proposta aceita: " + acceptMessage.getProposalValue());

            // Acceptors recebem e respondem com Accepted ou Nack
for (Acceptor<String> acceptor : acceptors) {
    if (true) {
        acceptor.receive(acceptMessage); // Aceita a proposta
        System.out.println("Acceptor " + acceptor.getNetworkUid() + " Aceitou a proposta.");
    } else {
        // Simula a rejeição da proposta
        System.out.println("Acceptor " + acceptor.getNetworkUid() + " rejeitou a proposta.");
        // Pode-se enviar um Nack para o Proposer se necessário
        // proposer.receive(new Nack<>(acceptor.getNetworkUid(), acceptMessage.getProposalId(), "proposer1"));
    }
}


            // Learners processam as respostas Accepted
            for (Learner<String> learner : learners) {
                for (int i = 0; i < acceptors.size(); i++) {
                    learner.receive(new Accepted<>("acceptor" + (i + 1), acceptMessage.getProposalId(), acceptMessage.getProposalValue()));
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
    }
}
