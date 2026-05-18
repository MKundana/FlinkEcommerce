package FlinkCommerce;
import org.apache.flink.streaming.api.functions.sink.SinkFunction;

public class EmailSink implements SinkFunction<Transaction> {

    @Override
    public void invoke(Transaction value, SinkFunction.Context context) {
        System.out.println("🔥 EMAIL SINK CALLED");
        EmailAlert.sendAlert("Rejected txn: " + value.toString());
    }
}