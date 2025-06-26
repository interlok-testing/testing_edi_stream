package com.adaptris.edistream.test;

import com.adaptris.testing.LicensedSingleAdapterFunctionalTest;
import com.adaptris.testing.SingleAdapterFunctionalTest;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchKey;
import java.util.Collection;
import java.util.Objects;
import java.util.UUID;

public class DefaultFunctionalTest extends LicensedSingleAdapterFunctionalTest {
    private static final String XML_PAYLOAD_RESOURCE = "xml_edi_invoice.xml";
    private static final String EDI_PAYLOAD_RESOURCE = "edi_invoice_88591.edi";


    @Test
    public void test() throws Exception {
        String ediPayload;
        try (InputStream is = Objects.requireNonNull(getClass().getClassLoader().getResource(EDI_PAYLOAD_RESOURCE)).openStream()) {
            ediPayload = IOUtils.toString(is, StandardCharsets.UTF_8);
        }
        String xmlPayload;
        try (InputStream is = Objects.requireNonNull(getClass().getClassLoader().getResource(XML_PAYLOAD_RESOURCE)).openStream()) {
            xmlPayload = IOUtils.toString(is, StandardCharsets.UTF_8);
        }
        Path xmlIn = Paths.get("xml-in");
        Path ediIn = Paths.get("edi-in");
        Path xmlOut = Paths.get("xml-out");
        Path ediOut = Paths.get("edi-out");
        if (!ediOut.toFile().exists()) ediOut.toFile().mkdir();
        if (!xmlOut.toFile().exists()) xmlOut.toFile().mkdir();
        FileUtils.writeStringToFile(xmlIn.resolve(UUID.randomUUID().toString()).toFile(), xmlPayload, StandardCharsets.UTF_8);
        WatchKey watchKey = waitForFileEvent(ediOut, 15000, StandardWatchEventKinds.ENTRY_CREATE);
        watchKey.pollEvents().forEach(event -> {
            Collection<File> files = FileUtils.listFiles(ediOut.toFile(), null, false);
            assert !files.isEmpty();
            files.forEach(file -> {
                try {
                    Assertions.assertEquals(ediPayload.substring(0, 150), FileUtils.readFileToString(file, StandardCharsets.UTF_8).substring(0, 150));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        });
    }
}
