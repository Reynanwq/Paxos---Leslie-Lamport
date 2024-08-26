import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Objects;


public class ProposalID implements Comparable<ProposalID> {
    private final int proposalNumber;
    private final String networkUid;

    public ProposalID(int proposalNumber, String networkUid) {
        this.proposalNumber = proposalNumber;
        this.networkUid = networkUid;
    }

    public int getProposalNumber() {
        return proposalNumber;
    }

    public String getNetworkUid() {
        return networkUid;
    }

    @Override
    public int compareTo(ProposalID other) {
        int cmp = Integer.compare(this.proposalNumber, other.proposalNumber);
        if (cmp == 0) {
            cmp = this.networkUid.compareTo(other.networkUid);
        }
        return cmp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProposalID that = (ProposalID) o;
        return proposalNumber == that.proposalNumber && Objects.equals(networkUid, that.networkUid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(proposalNumber, networkUid);
    }
}