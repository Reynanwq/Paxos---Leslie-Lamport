import java.util.Optional;

public interface ILearner<T> {
    String getNetworkUidLearner();
    Optional<Resolution<T>> receiveLearner(Accepted<T> message);
    Optional<T> getFinalValueLearner();
}
