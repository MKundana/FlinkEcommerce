package FlinkCommerce;

import org.apache.flink.api.common.serialization.SerializationSchema;
import com.fasterxml.jackson.databind.ObjectMapper;
import FlinkCommerce.Transaction;

public class TransactionSerializer implements SerializationSchema<Transaction> {

    private static final ObjectMapper mapper = new ObjectMapper();

    @Override
    public byte[] serialize(Transaction txn) {
        try {
            return mapper.writeValueAsBytes(txn);
        } catch (Exception e) {
            throw new RuntimeException("Serialization failed", e);
        }
    }
}