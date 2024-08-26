import java.util.HashSet;
import java.util.Set;

public class ProposalStatus<T> {
    int acceptCount;
    int retainCount;
    Set<String> acceptors;
    T value;

    public ProposalStatus(int acceptCount, int retainCount, Set<String> acceptors, T value) {
        this.acceptCount = acceptCount;
        this.retainCount = retainCount;
        this.acceptors = acceptors;
        this.value = value;
    }
}
