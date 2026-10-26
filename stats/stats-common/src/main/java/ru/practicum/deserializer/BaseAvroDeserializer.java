package ru.practicum.deserializer;


import org.apache.avro.Schema;
import org.apache.avro.io.BinaryDecoder;
import org.apache.avro.io.DatumReader;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.common.serialization.Deserializer;

import java.io.ByteArrayInputStream;
import java.io.IOException;


public class BaseAvroDeserializer<T extends SpecificRecordBase> implements Deserializer<T> {
    private final DecoderFactory decoderFactory;
    private final Schema schema;

    public BaseAvroDeserializer(Schema schema) {
        this(DecoderFactory.get(), schema);
    }

    public BaseAvroDeserializer(DecoderFactory decoderFactory, Schema schema) {
        this.decoderFactory = decoderFactory;
        this.schema = schema;
    }

    @Override
    public T deserialize(String topic, byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes)){
            DatumReader<T> datumReader = new SpecificDatumReader<>(schema);
            BinaryDecoder binaryDecoder = decoderFactory.binaryDecoder(inputStream, null);
            return datumReader.read(null, binaryDecoder);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
