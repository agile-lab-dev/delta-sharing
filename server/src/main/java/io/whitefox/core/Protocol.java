package io.whitefox.core;

import io.whitefox.annotations.SkipCoverageGenerated;
import java.util.Objects;
import java.util.Optional;

public class Protocol {
  private final Optional<Long> minReaderVersion;

  public Protocol(Optional<Long> minReaderVersion) {
    this.minReaderVersion = minReaderVersion;
  }

  public Optional<Long> minReaderVersion() {
    return minReaderVersion;
  }

  @Override
  @SkipCoverageGenerated
  public String toString() {
    return "Protocol{" + "minReaderVersion=" + minReaderVersion + '}';
  }

  @Override
  @SkipCoverageGenerated
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Protocol protocol = (Protocol) o;
    return Objects.equals(minReaderVersion, protocol.minReaderVersion);
  }

  @Override
  @SkipCoverageGenerated
  public int hashCode() {
    return Objects.hash(minReaderVersion);
  }
}
