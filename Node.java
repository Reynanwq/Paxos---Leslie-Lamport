import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Node implements INode{
    private static Node instanciaUnica;
    private static String valorAceito = null;
    private static String propostaAtual = null; // Armazena a proposta atual
    private static final int PORT = 12345;
    private static final String HOST = "localhost";
    private static final int TOTAL_NODES = 3; // Número total de nós
    private static List<Integer> nodeIds = new ArrayList<>();
    private static int nodeLider = 0, nodeTry = 0, nodeIdN2 = -1, server=0, nodeIdN3 = 0;
    private static final int NODE_TO_BECOME_LEADER = 1; // Define qual nó deve se tornar o líder (ex: nó com ID 1)
    private static int promessasRecebidas = 0;
   
    // Construtor privado para impedir a criação de instâncias externas
    private Node() {
        // Inicialização, se necessário
    }

    // Método estático para obter a instância única
    public static synchronized Node getInstancia() {
        if (instanciaUnica == null) {
            instanciaUnica = new Node();
        }
        return instanciaUnica;
    }

    @Override
    public void iniciar() {
        // Adiciona todos os nodeIds na lista
        for (int i = 0; i < TOTAL_NODES; i++) {
            nodeIds.add(i);
        }

        // Ordena a lista para encontrar o menor nodeId
        Collections.sort(nodeIds);

        // Executa 1 rodada
        System.out.println("Rodada 1");

        // Reseta variáveis de estado
        valorAceito = null;
        propostaAtual = null;
        nodeLider = 0;
        nodeTry = 0;
        promessasRecebidas = 0;

        // Cria e inicia threads para múltiplos nós
        for (int i = 0; i < TOTAL_NODES; i++) {
            int nodeId = i;
            new Thread(() -> runNode(nodeId)).start();
        }

        // Aguarda todas as threads terminarem (pode ser necessário ajustar o tempo de espera)
        try {
            Thread.sleep(20000); // Aumente este tempo conforme necessário
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void runNode(int nodeId) {
        // Cria o servidor para o nó
        new Thread(() -> {
            try (ServerSocket serverSocket = new ServerSocket(PORT + nodeId)) {
                System.out.println("Node " + nodeId + " (Acceptor) aguardando conexão na porta " + (PORT + nodeId) + "...");
               
                while (true) {
                    try (Socket socket = serverSocket.accept();
                         BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                         PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {
                        
                        // Ler mensagem do cliente
                        String mensagem = in.readLine();
                        if (mensagem.startsWith("Propose:")) {
                            System.out.println("Node " + nodeId + " (Acceptor) recebeu proposta: " + mensagem);
                            String proposta = mensagem.substring(8).trim();
                            if (valorAceito == null || (server>0) || proposta.compareTo(propostaAtual) > 0) {
                                valorAceito = proposta;
                                propostaAtual = proposta;
                                out.println("Aceito: " + valorAceito);
                            } else {
                                out.println("Já aceito: " + valorAceito);
                            }
                        } else if (mensagem.equals("Prepare")) {
                            System.out.println("Node " + nodeId + " (Acceptor) recebeu 'prepare'.");
                            out.println("Aceito ouvir sua proposta");
                        } else if (mensagem.startsWith("Prepare New Leader")) {
                            System.out.println("Node " + nodeId + " (Acceptor) recebeu 'prepare' do novo líder.");
                            String novaProposta = mensagem.substring(18).trim();
                            if (propostaAtual == null || (nodeLider < nodeTry) || (novaProposta.compareTo(propostaAtual) > 0)) {
                                propostaAtual = novaProposta;
                                out.println("Aceito ouvir sua proposta");
                            } else {
                                out.println("Proposta menor que a atual");
                            }
                        } else if (mensagem.equals("Get value")) {
                            System.out.println("Node " + nodeId + " (Acceptor) recebeu solicitação de valor.");
                            // Responder à solicitação de valor aceito
                            out.println(valorAceito != null ? "Valor atual: " + valorAceito : "Nenhum valor aceito");
                        } else if (mensagem.equals("Ping")) {
                            // Apenas responde ao ping para indicar que está ativo
                            out.println(" Está ativo!");
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();

        // Aguarda para garantir que o servidor esteja pronto
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Encontra o menor nodeId
        int menorNodeId = Collections.min(nodeIds);

        // Se o nó for o de menor ID, envia "prepare" e faz a proposta
        if (nodeId == menorNodeId) {
            try {
                // Envia "prepare" para todos os Acceptors
                System.out.println("Node " + nodeId + " (Proposer) enviou 'prepare'.");
                promessasRecebidas = 0; // Resetar contagem de promessas recebidas
                for (int otherNodeId : nodeIds) {
                    if (otherNodeId != nodeId) {
                        try (Socket socket = new Socket(HOST, PORT + otherNodeId);
                             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
                            out.println("Prepare");
                            String response = in.readLine();
                            System.out.println("Node " + nodeId + " (Proposer) Recebeu uma Promessa do Acceptor " + otherNodeId + ": " + response);
                            if (response.equals("Aceito ouvir sua proposta")) {
                                promessasRecebidas++;
                            }
                        }
                    }
                }

                // Verifica se o número de promessas recebidas é suficiente
                if (promessasRecebidas >= (TOTAL_NODES / 2)) {
                    // Aguarda para garantir que o "prepare" tenha sido processado
                    Thread.sleep(2000);

                    // Envia a proposta
                    System.out.println("Node " + nodeId + " (Proposer) fez uma proposta.");
                    nodeLider = nodeId;
                    for (int otherNodeId : nodeIds) {
                        if (otherNodeId != nodeId) {
                            try (Socket socket = new Socket(HOST, PORT + otherNodeId);
                                 PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                                 BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
                                out.println("Propose: valor" + nodeId);
                                String response = in.readLine();
                                System.out.println("Node " + nodeId + " (Proposer) Resposta do Acceptor " + otherNodeId + ": " + response);
                            }
                        }
                    }
                } else {
                    System.out.println("Node " + nodeId + " (Proposer) não recebeu promessas suficientes para fazer uma proposta.");
                }
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        } else {
            // Aguarda para garantir que a proposta tenha sido processada
            try {
                Thread.sleep(5000); // Aumentado o tempo de espera para garantir que a proposta seja processada
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        // Como Learner
        try (Socket socket = new Socket(HOST, PORT + nodeId);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {
            Thread.sleep(10000);  // Aguarda para garantir que todos os Acceptors tenham processado a proposta
            out.println("Get value");
            String resposta = in.readLine();
            System.out.println("Node " + nodeId + " (Learner) Valor aprendido: " + resposta);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

        

        // Após todos os Learners aprenderem o valor, tenta se tornar um novo líder
        if (nodeId != Collections.min(nodeIds) && nodeId != NODE_TO_BECOME_LEADER) {
            sendPingToLeader();
            try {

            // Executa 2 rodada
            System.out.println("Rodada 2");
                // Decidir se se tornar o novo líder
                String novaProposta = "valor" + nodeId;
                System.out.println("Node " + nodeId + " (Novo Proposer) enviou 'prepare' como novo líder.");
                nodeTry = nodeId;
                for (int otherNodeId : nodeIds) {
                    if (otherNodeId != nodeId) {
                        try (Socket socket = new Socket(HOST, PORT + otherNodeId);
                             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
                            out.println("Prepare New Leader: " + novaProposta);
                            String response = in.readLine();
                            System.out.println("Node " + nodeId + " (Novo Proposer) Resposta do Acceptor " + otherNodeId + ": " + response);
                        }
                    }
                }

                // Aguarda para garantir que o "prepare" do novo líder tenha sido processado
                Thread.sleep(10000);

                // Envia a nova proposta
                System.out.println("Node " + nodeId + " (Novo Proposer) fez uma nova proposta.");
                for (int otherNodeId : nodeIds) {
                    if (otherNodeId != nodeId) {
                        try (Socket socket = new Socket(HOST, PORT + otherNodeId);
                             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
                            out.println("Propose: " + novaProposta);
                            String response = in.readLine();
                            System.out.println("Node " + nodeId + " (Novo Proposer) Resposta do Acceptor " + otherNodeId + ": " + response);
                        }
                    }
                }
                Thread.sleep(2000);
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }

               // Consultando valor de todos os nodes
    for (int otherNodeId : nodeIds) {
        if (true) {
            try (Socket socket = new Socket(HOST, PORT + otherNodeId);
                 BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                 PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {
                // Aguarda para garantir que todos os Acceptors tenham processado a nova proposta
                Thread.sleep(3000);
                out.println("Get value");
                String resposta = in.readLine();
                
                int nodeIdN = (nodeId+nodeIdN2)-1;
                
                System.out.println("Node " + nodeIdN + " (Learner) Valor aprendido: " + resposta);
                nodeIdN2++;
                nodeLider = nodeId;
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

        sendPingToLeader();

        System.out.println("Rodada 3");
        //System.out.println(nodeLider);
        removeNode(nodeLider);
        removeNode(nodeLider);
        sendPingToLeader();
        checkAndHandleLeaderFailure();

        }

    }

    private void sendPingToLeader() {
        System.out.println("Enviando ping para o líder");
        if (!nodeIds.contains(nodeLider)) {
            System.out.println("Líder caiu!");
            server++;
            return;
        }
        for (int otherNodeId : nodeIds) {
            if (otherNodeId != nodeLider) {
                try (Socket socket = new Socket(HOST, PORT + otherNodeId);
                     PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                     BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
                    out.println("Ping");
                    String response = in.readLine();
                    System.out.println("Node " + otherNodeId + " recebeu resposta: Node" + nodeLider + response);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void removeNode(int nodeId) {
        // Verifica se o nó a ser removido existe na lista
        if (nodeIds.contains(nodeId)) {
            // Remove o nó da lista de IDs
            nodeIds.remove(Integer.valueOf(nodeId));
            //System.exit(nodeId); 
            //System.out.println("Node " + nodeId + " removido da lista de nós.");
    
            // Fechar conexões ou realizar qualquer outra ação necessária para remover o nó
            // Nota: Isso é um exemplo; dependendo da sua arquitetura, você pode precisar adicionar lógica adicional
        } else {
            //System.out.println("Node " + nodeId + " não encontrado na lista de nós.");
        }
    }

    @Override
    public void checkAndHandleLeaderFailure() {
        //System.out.println("Verificando se o líder está ativo");
        
        if (!nodeIds.contains(nodeLider)) {
           // System.out.println("Iniciando nova eleição.");
            initiateElection();
        } else {
            try (Socket socket = new Socket(HOST, PORT + nodeLider);
                 PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                 BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
                out.println("Ping");
                String response = in.readLine();
                if (response == null || !response.contains("Está ativo")) {
                    System.out.println("Líder está inativo. Iniciando nova eleição.");
                    initiateElection();
                }
            } catch (IOException e) {
                System.out.println("Falha ao contatar o líder. Iniciando nova eleição.");
                initiateElection();
            }
        }
    }
    

    @Override
    public void initiateElection() {
        System.out.println("Iniciando nova eleição.");
        updateLeader();
    
        //Envia um "prepare" para todos os nós para iniciar a nova eleição
        for (int nodeId : nodeIds) {
            if (nodeId != -1) {  // Verifique se o nó não foi removido
                try (Socket socket = new Socket(HOST, PORT + nodeId);
                     PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                     BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
                    out.println("Prepare");
                    String response = in.readLine();
                    if (response != null && response.contains("Aceito")) {
                        System.out.println("Node " + nodeId + " aceitou ouvir a proposta de Node: " + nodeLider);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        r3Proposer(nodeLider);
        r3Learner(nodeLider);
    }

    @Override
    public void updateLeader() {
        if (!nodeIds.isEmpty()) {
            nodeLider = Collections.min(nodeIds);
            System.out.println("Novo líder escolhido: Node " + nodeLider);
        } else {
            nodeLider = -1;
        }
    }

    @Override
    public void r3Proposer(int nodeId){
        try {
            // Envia a nova proposta
            //System.out.println("Node " + nodeId + " fez uma nova proposta.");
            for (int otherNodeId : nodeIds) {
                if (otherNodeId != nodeId) {
                    try (Socket socket = new Socket(HOST, PORT + otherNodeId);
                            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
                            out.println("Propose: " + nodeId);
                            String response = in.readLine();
                            System.out.println("Node " + nodeId + " Resposta do Acceptor " + otherNodeId + ": " + response);
                        }
                    }
                }
                Thread.sleep(2000);
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
    }
    
    @Override
    public void r3Learner(int nodeId){
        
        // Consultando valor de todos os nodes
        for (int otherNodeId : nodeIds) {
            if (true) {
                try(Socket socket = new Socket(HOST, PORT + otherNodeId);
                    BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {
                    // Aguarda para garantir que todos os Acceptors tenham processado a nova proposta
                    Thread.sleep(20000);
                    out.println("Get value");
                    String resposta = in.readLine();
                    
                    int nodeIdN = (nodeId+nodeIdN3);
                    
                    System.out.println("Node " + nodeIdN + " (Learner) Valor aprendido: " + resposta);
                    nodeIdN3++;
                    nodeLider = nodeId;
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}