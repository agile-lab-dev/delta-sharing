package io.whitefox.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectWriter;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.iceberg.Schema;
import org.apache.iceberg.StructLike;
import org.apache.iceberg.types.Conversions;
import org.apache.iceberg.types.Types;

public class IcebergFileStatsBuilder {

  private final ObjectWriter objectWriter;

  public IcebergFileStatsBuilder(ObjectWriter objectWriter) {
    this.objectWriter = objectWriter;
  }

  public String buildStats(
      Schema schema,
      Long numRecords,
      Map<Integer, ByteBuffer> minValues,
      Map<Integer, ByteBuffer> maxValues,
      Map<Integer, Long> nullCount)
      throws IcebergFileStatsBuilderException {
    try {
      return objectWriter.writeValueAsString(new FileStats(
          numRecords,
          buildValuesMap(minValues, schema),
          buildValuesMap(maxValues, schema),
          nullCount.entrySet().stream()
              .collect(
                  Collectors.toMap(e -> schema.findColumnName(e.getKey()), Map.Entry::getValue))));
    } catch (JsonProcessingException e) {
      throw new IcebergFileStatsBuilderException(e);
    }
  }

  public Map<String, String> buildPartitionValues(
      List<Types.NestedField> partitionFields, StructLike partitionValues) {
    var map = new HashMap<String, String>();
    for (int i = 0; i < partitionFields.size(); i++) {
      Types.NestedField field = partitionFields.get(i);
      map.put(
          field.name(),
          partitionValues.get(i, field.type().typeId().javaClass()).toString());
    }
    return map;
  }

  private Map<String, Object> buildValuesMap(Map<Integer, ByteBuffer> map, Schema schema) {
    return map.entrySet().stream()
        .collect(Collectors.toMap(
            e -> schema.findColumnName(e.getKey()),
            e -> Conversions.fromByteBuffer(schema.findType(e.getKey()), e.getValue())));
  }
}
