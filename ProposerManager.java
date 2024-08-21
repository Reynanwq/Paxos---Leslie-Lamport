import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Objects;

public class ProposerManager {
    private static final Map<String, Proposer<?>> proposers = new HashMap<>();

    public static <T> void addProposer(Proposer<T> proposer) {
        proposers.put(proposer.getNetworkUid(), proposer);
    }

    public static Proposer<?> getProposer(String proposerUid) {
        return proposers.get(proposerUid);
    }

    public static void removeProposer(String proposerUid) {
        Proposer<?> proposer = proposers.remove(proposerUid);
        if (proposer != null) {
            // Limpar o estado do Proposer
            proposer.removeProposer(); 
            // Certifique-se de que todos os registros do Proposer sejam removidos
            System.out.println("Proposer com UID " + proposerUid + " foi removido e seu estado limpo.");
        } else {
            System.out.println("Proposer com UID " + proposerUid + " não encontrado.");
        }
    }
}
