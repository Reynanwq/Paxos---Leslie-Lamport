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
        Proposer<String> proposer4 = new Proposer<>("proposer4", 6); // Quórum: 6

        // Adicionar os proposers ao gerenciador
        ProposerManager.addProposer(proposer);
        ProposerManager.addProposer(proposer2);
        ProposerManager.addProposer(proposer4);
        Thread.sleep(2000); // Intervalo de 2 segundos

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

            System.out.println("Proposta aceita: " + acceptMessage.getProposalValue());

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

        

        // Escolher um Acceptor para enviar o ping
        Acceptor<String> chosenAcceptor = acceptors.get(0);
        boolean proposerAtivo = chosenAcceptor.sendPing(proposer.getNetworkUid());
        if (proposerAtivo) {
            System.out.println("O Proposer está ativo e respondeu ao ping.");
        } else {
            System.out.println("O Proposer não respondeu ao ping. Ele pode estar inativo.");
        }

        // Remover um Proposer
        ProposerManager.removeProposer("proposer1");
        Thread.sleep(2000); // Intervalo de 2 segundos

        // Tentar remover um Proposer que não existe
        ProposerManager.removeProposer("proposer3");

        
        // Escolher um Acceptor para enviar o ping
        boolean proposerAtivo2 = chosenAcceptor.sendPing(proposer.getNetworkUid());
        if (proposerAtivo2) {
            System.out.println("O Proposer está ativo e respondeu ao ping.");
        } else {
            System.out.println("O Proposer não respondeu ao ping. Ele pode estar inativo.");
        }

        // Remover um Proposer
        ProposerManager.removeProposer("proposer2");
        Thread.sleep(2000); // Intervalo de 2 segundos


        // Simular a proposição de um valor pelo proposer4
        proposer4.proposeValue("valorC");
        Thread.sleep(2000); // Intervalo de 2 segundos

        // Preparar uma proposta
        Prepare prepare4 = proposer4.prepare();
        System.out.println("Proposta enviada por proposer4: " + prepare4.getProposalId().getProposalNumber());
        Thread.sleep(2000); // Intervalo de 2 segundos

        // Acceptors recebem a proposta
        for (Acceptor<String> acceptor : acceptors) {
            Message response4 = acceptor.receive(prepare4);
            if (response4 instanceof Nack) {
                System.out.println("Acceptor " + acceptor.getNetworkUid() + " rejeitou a proposta de proposer4.");
            }
        }
            
                // Definir quais Acceptors devem fazer uma promessa
                Set<String> acceptorsToPromise4 = new HashSet<>();
                acceptorsToPromise4.add("acceptor1");
                acceptorsToPromise4.add("acceptor2");
                acceptorsToPromise4.add("acceptor3");
                acceptorsToPromise4.add("acceptor4");
                acceptorsToPromise4.add("acceptor5");
                acceptorsToPromise4.add("acceptor6");
                acceptorsToPromise4.add("acceptor7");
                acceptorsToPromise4.add("acceptor8");
                acceptorsToPromise4.add("acceptor9");
                acceptorsToPromise4.add("acceptor10");
                proposer4.setAcceptorsToPromise(acceptorsToPromise4);
                //Thread.sleep(2000); // Intervalo de 2 segundos

                // Proposer processa respostas de Promise apenas para Acceptors selecionados
                proposer4.processPromisesForSelectedAcceptors(acceptors);

                // Proposer envia Accept
                Optional<Message> acceptMsg4 = proposer4.getCurrentAccept();
                if (acceptMsg4.isPresent() && acceptMsg4.get() instanceof Accept) {
                    Accept<String> acceptMessage = (Accept<String>) acceptMsg4.get();
                    //Thread.sleep(2000); // Intervalo de 2 segundos

                    // Acceptors recebem e respondem com Accepted ou Nack
                    for (Acceptor<String> acceptorr : acceptors) {
                        if (acceptorsToPromise.contains(acceptorr.getNetworkUid())) {
                            acceptorr.receive(acceptMessage);
                            System.out.println("Acceptor " + acceptorr.getNetworkUid() + " aceitou a proposta do Proposer4.");
                        } else {
                            System.out.println("Acceptor " + acceptorr.getNetworkUid() + " rejeitou a proposta do Proposer4.");
                        }
                    }

                    System.out.println("Proposta aceita: " + acceptMessage.getProposalValue());
                    Thread.sleep(4000); // Aguarde um pouco para que os Learners processem as mensagens

                    // Learners processam as respostas Accepted
                    for (Learner<String> learner4 : learners) {
                        //System.out.println("ENTROU2");
                        for (Acceptor<String> acceptorr : acceptors) {
                            learner4.receive(new Accepted<>(acceptorr.getNetworkUid(), acceptMessage.getProposalId(), acceptMessage.getProposalValue()));
                            //System.out.println("Learner " + learner4.getNetworkUid() + " recebeu uma mensagem Accepted de " + acceptorr.getNetworkUid());
                        }
                        
                        for (Acceptor<String> acceptorr : acceptors) {
                            learner4.receive(new Accepted<>(acceptorr.getNetworkUid(), acceptMessage.getProposalId(), acceptMessage.getProposalValue()));
                        }
                    }
                    Thread.sleep(4000); // Aguarde um pouco para que os Learners processem as mensagens

                    // Verificar a decisão final dos Learners
                    for (Learner<String> learner4 : learners) {
                        for (Acceptor<String> acceptorr : acceptors) {
                            learner4.receive(new Accepted<>(acceptorr.getNetworkUid(), acceptMessage.getProposalId(), acceptMessage.getProposalValue()));
                            //System.out.println("Learner " + learner4.getNetworkUid() + " recebeu uma mensagem Accepted de " + acceptorr.getNetworkUid());
                        }
                        
                        //System.out.println("ENTROU");
                        learner4.getFinalValue().ifPresent(value4 -> {
                            System.out.println("Learner " + learner4.getNetworkUid() + " decidiu valor final: " + value4);
                        });
                    }
                    
                } else {
                    System.out.println("Nenhuma proposta aceita.");
                }      
            }
}
