/*import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class Main2 {
    public static void main(String[] args) throws InterruptedException {
        // Criar instâncias dos Nodes
        List<Node<String>> nodes = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            nodes.add(new Node<>("node" + i, 6)); // Quórum: 6
        }

        // Simular a proposição de um valor por um Node
        Node<String> proposerNode = nodes.get(0); // Escolhendo o primeiro nó como proposer
        proposerNode.proposeValueProposer("valorA");
        Thread.sleep(2000); // Intervalo de 2 segundos

        // Preparar uma proposta
        Prepare prepare = proposerNode.prepareProposer();
        System.out.println("Proposta enviada: " + prepare.getProposalId().getProposalNumber());
        Thread.sleep(2000); // Intervalo de 2 segundos

        // Acceptors recebem a proposta
        for (Node<String> node : nodes) {
            if (node != proposerNode) {
                node.receiveAcceptor(prepare);
            }
        }
        Thread.sleep(2000); // Intervalo de 2 segundos

        // Definir quais Acceptors devem fazer uma promessa
        Set<String> acceptorsToPromise = new HashSet<>();
        for (Node<String> node : nodes) {
            if (!node.getNetworkUidAcceptor().equals(proposerNode.getNetworkUidAcceptor())) {
                acceptorsToPromise.add(node.getNetworkUidAcceptor());
            }
        }
        proposerNode.setAcceptorsToPromiseProposer(acceptorsToPromise);
        Thread.sleep(2000); // Intervalo de 2 segundos

        // Proposer processa respostas de Promise apenas para Acceptors selecionados
        proposerNode.processPromisesForSelectedAcceptorsProposerN(nodes);
        Thread.sleep(2000); // Intervalo de 2 segundos

        // Proposer envia Accept
        Optional<Message> acceptMsg = proposerNode.getCurrentAcceptProposer();
        if (acceptMsg.isPresent() && acceptMsg.get() instanceof Accept) {
            Accept<String> acceptMessage = (Accept<String>) acceptMsg.get();
            System.out.println("Proposta aceita: " + acceptMessage.getProposalValue());

            // Acceptors recebem e respondem com Accepted ou Nack
            for (Node<String> node : nodes) {
                if (acceptorsToPromise.contains(node.getNetworkUidAcceptor())) {
                    node.receiveAcceptor(acceptMessage);
                    System.out.println("Acceptor " + node.getNetworkUidAcceptor() + " aceitou a proposta.");
                } else {
                    System.out.println("Acceptor " + node.getNetworkUidAcceptor() + " rejeitou a proposta.");
                }
                Thread.sleep(2000); // Intervalo de 2 segundos
            }

            // Learners processam as respostas Accepted
            for (Node<String> node : nodes) {
                node.receiveLearner(new Accepted<>(proposerNode.getNetworkUidAcceptor(), acceptMessage.getProposalId(), acceptMessage.getProposalValue()));
            }
            Thread.sleep(2000); // Intervalo de 2 segundos

            // Verificar a decisão final dos Learners
            for (Node<String> node : nodes) {
                node.getFinalValueLearner().ifPresent(value -> {
                    System.out.println("Node " + node.getNetworkUidAcceptor() + " decidiu valor final: " + value);
                });
            }
        } else {
            System.out.println("Nenhuma proposta aceita.");
        }

        // Simular a proposição de um valor por outro Node
        Node<String> proposerNode2 = nodes.get(1); // Escolhendo o segundo nó como proposer
        proposerNode2.proposeValueProposer("valorB");
        Thread.sleep(2000); // Intervalo de 2 segundos

        // Preparar uma proposta
        Prepare prepare2 = proposerNode2.prepareProposer();
        System.out.println("Proposta enviada por " + proposerNode2.getNetworkUidAcceptor() + ": " + prepare2.getProposalId().getProposalNumber());
        Thread.sleep(2000); // Intervalo de 2 segundos

        // Acceptors recebem a proposta
        for (Node<String> node : nodes) {
            if (node != proposerNode2) {
                Message response = node.receiveAcceptor(prepare2);
                if (response instanceof Nack) {
                    System.out.println("Acceptor " + node.getNetworkUidAcceptor() + " rejeitou a proposta de " + proposerNode2.getNetworkUidAcceptor() + ".");
                }
                Thread.sleep(2000); // Intervalo de 2 segundos
            }
        }

        // Escolher um Node para enviar o ping
        Node<String> chosenNode = nodes.get(2); // Escolhendo o terceiro nó para enviar o ping
        boolean proposerAtivo = chosenNode.sendPingAcceptor(proposerNode.getNetworkUidAcceptor());
        if (proposerAtivo) {
            System.out.println("O Proposer está ativo e respondeu ao ping.");
        } else {
            System.out.println("O Proposer não respondeu ao ping. Ele pode estar inativo.");
        }

        // Remover um Node
        nodes.remove(0); // Remover o primeiro nó
        Thread.sleep(2000); // Intervalo de 2 segundos

        // Tentar remover um Node que não existe
        // Não é necessário aqui, já que estamos manipulando diretamente a lista

        // Escolher um Node para enviar o ping novamente
        boolean proposerAtivo2 = chosenNode.sendPingAcceptor(proposerNode2.getNetworkUidAcceptor());
        if (proposerAtivo2) {
            System.out.println("O Proposer está ativo e respondeu ao ping.");
        } else {
            System.out.println("O Proposer não respondeu ao ping. Ele pode estar inativo.");
        }

        // Remover outro Node
        nodes.remove(1); // Remover o segundo nó
        Thread.sleep(2000); // Intervalo de 2 segundos

        // Simular a proposição de um valor por um Node novo criado
        Node<String> proposerNode4 = nodes.get(0); // Pegando o primeiro nó restante como proposer
        proposerNode4.proposeValueProposer("valorC");
        Thread.sleep(2000); // Intervalo de 2 segundos

        // Preparar uma proposta
        Prepare prepare4 = proposerNode4.prepareProposer();
        System.out.println("Proposta enviada por " + proposerNode4.getNetworkUidAcceptor() + ": " + prepare4.getProposalId().getProposalNumber());
        Thread.sleep(2000); // Intervalo de 2 segundos

        // Acceptors recebem a proposta
        for (Node<String> node : nodes) {
            if (node != proposerNode4) {
                Message response4 = node.receiveAcceptor(prepare4);
                if (response4 instanceof Nack) {
                    System.out.println("Acceptor " + node.getNetworkUidAcceptor() + " rejeitou a proposta de " + proposerNode4.getNetworkUidAcceptor() + ".");
                }
            }
        }

        // Definir quais Acceptors devem fazer uma promessa
        Set<String> acceptorsToPromise4 = new HashSet<>();
        for (Node<String> node : nodes) {
            if (!node.getNetworkUidAcceptor().equals(proposerNode4.getNetworkUidAcceptor())) {
                acceptorsToPromise4.add(node.getNetworkUidAcceptor());
            }
        }
        proposerNode4.setAcceptorsToPromiseProposer(acceptorsToPromise4);
        Thread.sleep(2000); // Intervalo de 2 segundos

        // Proposer processa respostas de Promise apenas para Acceptors selecionados
        proposerNode4.processPromisesForSelectedAcceptorsProposerN(nodes);

        // Proposer envia Accept
        Optional<Message> acceptMsg4 = proposerNode4.getCurrentAcceptProposer();
        if (acceptMsg4.isPresent() && acceptMsg4.get() instanceof Accept) {
            Accept<String> acceptMessage = (Accept<String>) acceptMsg4.get();
            System.out.println("Proposta aceita: " + acceptMessage.getProposalValue());
            Thread.sleep(4000); // Aguarde um pouco para que os Learners processem as mensagens

            // Acceptors recebem e respondem com Accepted ou Nack
            for (Node<String> node : nodes) {
                if (acceptorsToPromise4.contains(node.getNetworkUidAcceptor())) {
                    node.receiveAcceptor(acceptMessage);
                    System.out.println("Acceptor " + node.getNetworkUidAcceptor() + " aceitou a proposta do Proposer4.");
                } else {
                    System.out.println("Acceptor " + node.getNetworkUidAcceptor() + " rejeitou a proposta do Proposer4.");
                }
            }

            // Learners processam as respostas Accepted
            for (Node<String> node : nodes) {
                node.receiveLearner(new Accepted<>(proposerNode4.getNetworkUidAcceptor(), acceptMessage.getProposalId(), acceptMessage.getProposalValue()));
            }
            Thread.sleep(4000); // Aguarde um pouco para que os Learners processem as mensagens

            // Verificar a decisão final dos Learners
            for (Node<String> node : nodes) {
                node.getFinalValueLearner().ifPresent(value -> {
                    System.out.println("Node " + node.getNetworkUidAcceptor() + " decidiu valor final: " + value);
                });
            }
        } else {
            System.out.println("Nenhuma proposta aceita.");
        }
    }
}
*/