package serializer;

import org.apache.avro.io.BinaryEncoder;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.specific.SpecificDatumWriter;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Serializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HexFormat;

public class AvroSerializer implements Serializer<SpecificRecordBase> {
    private final EncoderFactory encoderFactory;
    private final ThreadLocal<BinaryEncoder> encoderThreadLocal = new ThreadLocal<>();

    private static final Logger log = LoggerFactory.getLogger(AvroSerializer.class);
    private static final HexFormat hexFormat = HexFormat.ofDelimiter(":");

    public AvroSerializer() {
        this.encoderFactory = EncoderFactory.get();
    }

    @Override
    public byte[] serialize(String topic, SpecificRecordBase data) {
        if (data == null) {
            return null;
        }
        try(ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            BinaryEncoder encoder = encoderFactory.binaryEncoder(out, encoderThreadLocal.get());
            encoderThreadLocal.set(encoder);

            DatumWriter<SpecificRecordBase> writer = new SpecificDatumWriter<>(data.getSchema());
            writer.write(data,encoder);
            encoder.flush();
            byte[] bytes = out.toByteArray();
            log.info("Данные успешно сериализованы в формат Avro:\n{}", hexFormat.formatHex(bytes));

            return bytes;
        } catch (IOException ex) {
            throw new SerializationException("Ошибка сериализации данных для топика: " + topic, ex);
        }
    }

    @Override
    public void close() {
        encoderThreadLocal.remove();
    }
}
