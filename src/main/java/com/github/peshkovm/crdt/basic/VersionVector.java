package com.github.peshkovm.crdt.basic;

import io.vavr.collection.Vector;
import java.io.Serializable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@ToString
@EqualsAndHashCode
public class VersionVector implements Serializable, Cloneable {
  @Getter private final Vector<Long> versions;
  private final int id;

  public VersionVector(int amountOfNodes, int id) {
    versions = Vector.fill(amountOfNodes, 0L);
    this.id = id;
  }

  public VersionVector(Vector<Long> versions, int id) {
    this.versions = versions;
    this.id = id;
  }

  public VersionVector increment() {
    return new VersionVector(versions.update(id, versions.get(id) + 1), id);
  }

  public VersionVector merge(VersionVector that) {
    return new VersionVector(
        versions.zip(that.versions).map(pair -> Math.max(pair._1, pair._2)), id);
  }

  public boolean isNewer(VersionVector that) {
    return versions
        .zip(that.versions)
        .filter(pair -> !pair._1.equals(pair._2))
        .forAll(pair -> pair._1 > pair._2);
  }

  public boolean isOlder(VersionVector that) {
    return versions
        .zip(that.versions)
        .filter(pair -> !pair._1.equals(pair._2))
        .forAll(pair -> pair._1 < pair._2);
  }

  public boolean isEqual(VersionVector that) {
    return versions.zip(that.versions).forAll(pair -> pair._1.equals(pair._2));
  }

  public boolean isConcurrent(VersionVector that) {
    return !(isNewer(that) || isOlder(that) || isEqual(that));
  }
}
