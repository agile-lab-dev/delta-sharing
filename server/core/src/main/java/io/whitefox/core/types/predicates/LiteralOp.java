package io.whitefox.core.types.predicates;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.whitefox.core.types.DataType;
import org.apache.commons.lang3.tuple.Pair;

import static io.whitefox.core.types.predicates.EvaluatorVersion.V1;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "literal")
public
class LiteralOp extends LeafOp {
    @JsonProperty("value")
    String value;

    @Override
    public void validate() throws PredicateException {
        if (value == null) {
            throw new IllegalArgumentException("Value must be specified: " + this);
        }
        if (!isSupportedType(valueType, V1)) {
            throw new IllegalArgumentException("Unsupported type: " + valueType);
        }
        EvalHelper.validateValue(value, valueType);
    }

    @Override
    public Object eval(EvalContext ctx) {
        return Pair.of(value, valueType);
    }

    public LiteralOp() {
        super();
    }

    public LiteralOp(String value, DataType valueType) {
        this.value = value;
        this.valueType = valueType;
    }

    @Override
    public Boolean isNull(EvalContext ctx) {
        return false;
    }

    @Override
    public DataType getOpValueType() {
        return valueType;
    }
}
