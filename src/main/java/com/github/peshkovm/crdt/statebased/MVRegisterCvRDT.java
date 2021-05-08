package com.github.peshkovm.crdt.statebased;

import com.github.peshkovm.crdt.basic.RegisterCRDT;
import com.github.peshkovm.crdt.basic.VersionVector;
import com.github.peshkovm.crdt.replication.Replicator;
import com.github.peshkovm.crdt.statebased.MVRegisterCvRDT.Pair;
import io.vavr.collection.HashSet;
import io.vavr.collection.Set;
import io.vavr.control.Option;
import java.io.Serializable;
import lombok.Data;

public class MVRegisterCvRDT extends AbstractCvRDT<String, Set<Pair>, Set<Pair>>
    implements RegisterCRDT<String, Set<Pair>> {

  private final int amountOfNodes;
  protected Set<Pair> pairs; // Immutable payload
  protected final int id;

  /**
   * Instantiates new CvRDT Multi-Value register instance.
   *
   * @param identity crdt object identity, for example "countOfLikes"
   * @param replicator {@link Replicator} instance
   */
  public MVRegisterCvRDT(String identity, int amountOfNodes, int id, Replicator replicator) {
    super(identity, replicator);
    pairs = HashSet.of(new Pair("", new VersionVector(amountOfNodes, id)));
    this.id = id;
    this.amountOfNodes = amountOfNodes;
  }

  @Override
  public void assign(String value) {
    update(value);
  }

  private VersionVector incVV() {
    final Set<VersionVector> versionVectors = pairs.map(pair -> pair.versionVector);

    final VersionVector mergedVersionVector =
        versionVectors.reduceOption(VersionVector::merge).get();
    return mergedVersionVector.increment();
  }

  @Override
  public Set<Pair> value() {
    return query();
  }

  @Override
  protected Set<Pair> queryImpl() {
    return pairs;
  }

  @Override
  protected Option<Set<Pair>> updateImpl(String value) {
    VersionVector versionVector = incVV();
    pairs = HashSet.of(new Pair(value, versionVector));

    return Option.none();
  }

  @Override
  protected boolean compareImpl(Set<Pair> localPayload, Set<Pair> replicaPayload) {
    return localPayload.forAll(
        localPair ->
            replicaPayload.forAll(
                replicaPair -> localPair.versionVector.isOlder(replicaPair.versionVector)));
  }

  @Override
  protected Set<Pair> mergeImpl(Set<Pair> localPayload, Set<Pair> replicaPayload) {
    Set<Pair> A;
    Set<Pair> B;

    A =
        localPayload.filter(
            localPair ->
                replicaPayload.forAll(
                    replicaPair ->
                        localPair.versionVector.isNewer(replicaPair.versionVector)
                            || localPair.versionVector.isConcurrent(replicaPair.versionVector)));

    B =
        replicaPayload.filter(
            replicaPair ->
                localPayload.forAll(
                    localPair ->
                        replicaPair.versionVector.isNewer(localPair.versionVector)
                            || replicaPair.versionVector.isConcurrent(localPair.versionVector)));

    return HashSet.ofAll(A).addAll(B);
  }

  @Override
  protected Set<Pair> getPayload() {
    return pairs;
  }

  @Override
  protected void setPayload(Set<Pair> mergedPayload) {
    pairs = mergedPayload;
  }

  @Data
  public static class Pair implements Serializable {
    private final String value;
    private final VersionVector versionVector;

    public Pair(String value, VersionVector versionVector) {
      this.value = value;
      this.versionVector = versionVector;
    }

    @Override
    public String toString() {
      return "(" + value + "," + versionVector + ")";
    }
  }
}
