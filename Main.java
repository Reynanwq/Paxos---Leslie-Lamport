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
            int id = Np.generateUniqueId();
            Np<String> node = new Np<>("node" + i, 6, identityConverter, id);
            nodes.add(node);
            System.out.println("Criado nó com ID: " + node.getIdNum());// Acesso ao ID
        }
        //System.out.println("Nós criados: " + nodes);

        // Encontrar e adicionar o menor ID não retornado
        Optional<Integer> smallestId = Np.findSmallestIdNotReturned();
        System.out.println("Menor ID não retornado: " + smallestId.orElse(null));
        
        Np.addIdToReturned();
        
        // Verificar se o ID foi adicionado com sucesso
        System.out.println("IDs retornados: " + Np.getReturnedIds());

        String proposerId = "";
        Np<String> proposerNode = null;
        // Adicionar um loop para imprimir todos os nós com seus IDs
        System.out.println("Nós criados e seus IDs:");
        for (Np<String> node : nodes) {
            System.out.println("Nome do nó: " + node.getProposer().getNetworkUidProposer() + ", ID: " + node.getIdNum());
            if(smallestId.orElse(null).equals(node.getIdNum())){
                proposerNode = node;
                proposerId = node.getProposer().getNetworkUidProposer();

            }
        }

        // Adicionar os Proposers ao gerenciador
        for (Np<String> node : nodes) {
            ProposerManager.addProposer(node.getProposer());
        }
        //System.out.println("Proposers adicionados ao gerenciador.");

        Thread.sleep(2000); // Intervalo de 2 segundos

        // Criar instâncias dos Acceptors
        List<Acceptor<String>> acceptors = new ArrayList<>();
        for (Np<String> node : nodes) {
            acceptors.add(node.getAcceptor());
        }
        //System.out.println("Acceptors criados: " + acceptors);

        // Criar instâncias dos Learners
        List<Learner<String>> learners = new ArrayList<>();
        for (Np<String> node : nodes) {
            learners.add(node.getLearner());
        }
        //System.out.println("Learners criados: " + learners);

        
        
        if (proposerNode != null) {
            System.out.println("Proposer encontrado com ID: " + proposerId);

            // O nó específico propõe um valor
            //System.out.println("O nó " + proposerId + " está propondo o valor 'valorA'.");
            
            
            //System.out.println("O nó " + proposerId + " está propondo o valor '" + smallestId.orElse(null) + "'.");
            proposerNode.proposeValue(smallestId.orElse(null), "valorA");
            //proposerNode.proposeValue(3, "valorXXX");
            proposerNode.printProposedValues(); //TESTAR FUNCIONALIDADE
            Prepare prepare = proposerNode.getProposer().prepareProposer();

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
            if(!(proposerNode.checkAcceptorsSize(acceptorsToPromise, proposerNode.getProposer().getQuorum()))){
                System.out.println("QUANTIDADE DE PROMESSAS INSUFICIENTES");
            }else{
                System.out.println("QUANTIDADE DE PROMESSAS SUFICIENTES");

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

                System.out.println("Proposta aceita: " + acceptMessage.getProposalValue());


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
                    }
                    
                    nodeL.getFinalValueLearner().ifPresent(value4 -> {
                        System.out.println("Learner " + nodeL.getNetworkUidLearner() + " decidiu valor final: " +  acceptMessage.getProposalValue());
                    });
                }


                } else {
                    System.out.println("Nenhuma proposta aceita.");
                } 

                
                    proposerNode.checkFinalValues();
                }
            }
            System.out.println("-------------//-------------//--------------");
            ProposerManager.removeProposer(proposerId);

            // Escolher um Acceptor para enviar o ping
            Acceptor<String> chosenNode = acceptors.get(0);
            boolean proposerAtivo = chosenNode.sendPingAcceptor(proposerId);
            if (proposerAtivo) {
                System.out.println("O Proposer está ativo e respondeu ao ping.");
            }else{
                System.out.println("O Proposer não respondeu ao ping. Ele pode estar inativo.");
                System.out.println("Os outros nós iniciarão o processo de verificação de queda.");
                
                for(Acceptor<String> acceptor: acceptors){
                    System.out.println("Acceptor " + acceptor.getNetworkUidAcceptor() + " Enviou um Ping.");
                    int i=0;
                    acceptor = acceptors.get(i);
                    boolean proposerAtivo2 = acceptor.sendPingAcceptor(proposerId);
                    i++;
                }
            }

            //ProposerManager.removeProposer(proposerId);
            ProposerManager.removeProposer("proposer25");       
    }
}

