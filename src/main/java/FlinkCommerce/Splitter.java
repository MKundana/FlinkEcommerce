package FlinkCommerce;

import org.apache.flink.api.java.tuple.Tuple12; 
import org.apache.flink.api.common.functions.MapFunction;
import java.sql.Timestamp;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.flink.api.common.functions.MapFunction;

public class Splitter implements MapFunction<String, Transaction>
        {
            public transient ObjectMapper mapper;
            public Transaction map(String value) throws Exception
            {

                if (mapper==null)
                {
                    mapper = new ObjectMapper();
                }
                
                return mapper.readValue(value, Transaction.class);
            }

        }