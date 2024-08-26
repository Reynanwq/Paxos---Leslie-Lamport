import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        Function<String, String> identityConverter = Function.identity();

        // Criar nós
        List<Np<String>> nodes = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            nodes.add(new Np<>("node" + i, 6, identityConverter));
        }
        System.out.println("Nós criados: " + nodes);

        // Adicionar os Proposers ao gerenciador
        for (Np<String> node : nodes) {
            ProposerManager.addProposer(node.getProposer());
        }
        System.out.println("Proposers adicionados ao gerenciador.");

        Thread.sleep(2000); // Intervalo de 2 segundos

        // Criar instâncias dos Acceptors
        List<Acceptor<String>> acceptors = new ArrayList<>();
        for (Np<String> node : nodes) {
            acceptors.add(node.getAcceptor());
        }
        System.out.println("Acceptors criados: " + acceptors);

        // Criar instâncias dos Learners
        List<Learner<String>> learners = new ArrayList<>();
        for (Np<String> node : nodes) {
            learners.add(node.getLearner());
        }
        System.out.println("Learners criados: " + learners);



        // Propor valor para um nó específico (por exemplo, "node3")
        String proposerId = "node3";
        Np<String> proposerNode = nodes.stream()
                .filter(n -> n.getProposer().getNetworkUidProposer().equals(proposerId))
                .findFirst()
                .orElse(null);

        if (proposerNode != null) {
            System.out.println("Proposer encontrado com ID: " + proposerId);

            // O nó específico propõe um valor
            System.out.println("O nó " + proposerId + " está propondo o valor 'valorA'.");
            proposerNode.proposeValue("valorA");
            // Preparar uma proposta
            Prepare prepare = proposerNode.getProposer().prepareProposer();
            System.out.println("Proposta enviada: " + prepare.getProposalId().getProposalNumber());

            // Acceptors recebem a proposta
            proposerNode.receivePrepare(prepare);

            // Definir quais Acceptors devem fazer uma promessa
            Set<String> acceptorsToPromise = new HashSet<>();
            acceptorsToPromise.add("node1");
            acceptorsToPromise.add("node2");
            acceptorsToPromise.add("node3");
            acceptorsToPromise.add("node4");
            acceptorsToPromise.add("node5");
           acceptorsToPromise.add("node6");
           // acceptorsToPromise.add("node7");
          //  acceptorsToPromise.add("node8");
           // acceptorsToPromise.add("node9");
           // acceptorsToPromise.add("node10");
            proposerNode.setAcceptorsToPromise(acceptorsToPromise);

            // Proposer processa respostas de Promise apenas para Acceptors selecionados
            Set<Acceptor<String>> acceptorSet = new HashSet<>(acceptors);
            if(proposerNode.checkAcceptorsSize(acceptorsToPromise, proposerNode.getProposer().getQuorum())){
                System.out.println("QUANTIDADE DE PROMESSAS SUFICIENTES");
            }else{
                System.out.println("QUANTIDADE DE PROMESSAS INSUFICIENTES");
            }
            proposerNode.processPromises(acceptorSet);
            

            // Propose Enviar Accept
            Optional<Message> acceptMsg = proposerNode.getCurrentAccept();
            Accept<String> acceptMessage = (Accept<String>) acceptMsg.get();
            if (acceptMsg.isPresent() && acceptMsg.get() instanceof Accept) {
                proposerNode.receiveAccept(acceptMsg);
                // Acceptors recebem e respondem com Accepted ou Nack
            for (Acceptor<String> acceptor : acceptors) {
                if (acceptorsToPromise.contains(acceptor.getNetworkUidAcceptor())) {
                    System.out.println("Acceptor " + acceptor.getNetworkUidAcceptor() + " aceitou a proposta.");
                } else {
                    System.out.println("Acceptor " + acceptor.getNetworkUidAcceptor() + " rejeitou a proposta.");
                }
            }


            for(Learner<String> nodeL : learners){
                for(Acceptor<String> nodeA : acceptors){
                    nodeL.receiveLearner(new Accepted<>(nodeA.getNetworkUidAcceptor(), acceptMessage.getProposalId(), acceptMessage.getProposalValue()));
                }

                for (Acceptor<String> nodeA : acceptors) {
                    nodeL.receiveLearner(new Accepted<>(nodeA.getNetworkUidAcceptor(), acceptMessage.getProposalId(), acceptMessage.getProposalValue()));
                }
            }

            Thread.sleep(4000);

             // Verificar a decisão final dos Learners
             for(Learner<String> nodeL : learners){
                for (Acceptor<String> nodeA : acceptors) {
                    nodeL.receiveLearner(new Accepted<>(nodeA.getNetworkUidAcceptor(), acceptMessage.getProposalId(), acceptMessage.getProposalValue()));
                    //System.out.println("Learner " + learner4.getNetworkUid() + " recebeu uma mensagem Accepted de " + acceptorr.getNetworkUid());
                }
                
                //System.out.println("ENTROU");
                nodeL.getFinalValueLearner().ifPresent(value4 -> {
                    System.out.println("Learner " + nodeL.getNetworkUidLearner() + " decidiu valor final: " +  acceptMessage.getProposalValue());
                });
            }


          }  else {
                System.out.println("Nenhuma proposta aceita.");
            } 

            









            proposerNode.checkFinalValues();
        } else {
            System.out.println("Proposer com ID " + proposerId + " não encontrado.");
        }

        System.out.println("-------------//-------------//--------------");


        // Simular a proposição de um valor por um nó específico (por exemplo, "node3")
        proposerNode = nodes.stream()
                .filter(n -> n.getProposer().getNetworkUidProposer().equals(proposerId))
                .findFirst()
                .orElse(null);

        if (proposerNode != null) {
            System.out.println("O nó " + proposerId + " está propondo o valor 'valorB'.");
            proposerNode.proposeValue("valorB");
            Prepare prepare = proposerNode.getProposer().prepareProposer();
            System.out.println("Proposta enviada com número: " + prepare.getProposalId().getProposalNumber());

            // Acceptors recebem a proposta
            proposerNode.receivePrepare(prepare);
            System.out.println("Acceptors receberam a proposta com número: " + prepare.getProposalId().getProposalNumber());

            // Processar promessas
            Set<Acceptor<String>> acceptorSet = new HashSet<>(acceptors);
            proposerNode.processPromises(acceptorSet);
            System.out.println("Promessas processadas pelo nó " + proposerId);

            // Enviar Accept
            Optional<Message> acceptMsg = proposerNode.getCurrentAccept();
            if (acceptMsg.isPresent()) {
                System.out.println("Accept mensagem recebida pelo nó " + proposerId);
                proposerNode.receiveAccept(acceptMsg);
            } else {
                System.out.println("Nenhuma mensagem Accept encontrada para o nó " + proposerId);
            }
            proposerNode.checkFinalValues();
            System.out.println("Valores finais verificados pelo nó " + proposerId);
        }

        // Remover um Proposer
        System.out.println("Removendo o Proposer com ID: " + proposerId);
        ProposerManager.removeProposer(proposerId);

        // Atualizar Acceptors e Learners após a remoção do Proposer
        acceptors.removeIf(a -> a.getNetworkUidAcceptor().equals(proposerId));
        learners.removeIf(l -> l.getNetworkUidLearner().equals(proposerId));
        System.out.println("Acceptors e Learners atualizados após remoção do Proposer.");

        // Encontrar o Acceptor específico e criar um Proposer a partir dele
        String specificAcceptorUid = "acceptor5";
        Np<String> specificNode = nodes.stream()
                .filter(n -> n.getAcceptor().getNetworkUidAcceptor().equals(specificAcceptorUid))
                .findFirst()
                .orElse(null);

        if (specificNode != null) {
            System.out.println("Acceptor encontrado com UID: " + specificAcceptorUid);
            Np<String> newNode = new Np<>(specificAcceptorUid, 6, identityConverter);
            ProposerManager.addProposer(newNode.getProposer());
            System.out.println("Novo Proposer adicionado com ID: " + specificAcceptorUid);

            // Adicionar novos Acceptors e Learners após a adição do novo Proposer
            acceptors.add(newNode.getAcceptor());
            learners.add(newNode.getLearner());
            System.out.println("Novos Acceptors e Learners adicionados após a adição do novo Proposer.");

            // Simular a proposição de um valor pelo novo Proposer
            System.out.println("O novo nó " + specificAcceptorUid + " está propondo o valor 'valorC'.");
            newNode.proposeValue("valorC");
            Prepare prepare = newNode.getProposer().prepareProposer();
            System.out.println("Proposta enviada com número: " + prepare.getProposalId().getProposalNumber());

            // Acceptors recebem a proposta
            newNode.receivePrepare(prepare);
            System.out.println("Acceptors receberam a proposta com número: " + prepare.getProposalId().getProposalNumber());

            // Processar promessas
            Set<Acceptor<String>> acceptorSet = new HashSet<>(acceptors);
            newNode.processPromises(acceptorSet);
            System.out.println("Promessas processadas pelo novo nó " + specificAcceptorUid);

            // Enviar Accept
            Optional<Message> acceptMsg = newNode.getCurrentAccept();
            if (acceptMsg.isPresent()) {
                System.out.println("Accept mensagem recebida pelo novo nó " + specificAcceptorUid);
                newNode.receiveAccept(acceptMsg);
            } else {
                System.out.println("Nenhuma mensagem Accept encontrada para o novo nó " + specificAcceptorUid);
            }
            newNode.checkFinalValues();
            System.out.println("Valores finais verificados pelo novo nó " + specificAcceptorUid);
        } else {
            System.out.println("Acceptor com UID " + specificAcceptorUid + " não encontrado.");
        }
    }
}
