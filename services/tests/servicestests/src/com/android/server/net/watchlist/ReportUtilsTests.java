/*
 * Copyright 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.server.net.watchlist;

import static org.junit.Assert.assertArrayEquals;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.filters.SmallTest;
import android.support.test.runner.AndroidJUnit4;

import com.android.internal.util.HexDump;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;

/**
 * runtest frameworks-services -c com.android.server.net.watchlist.ReportUtilsTests
 */
@RunWith(AndroidJUnit4.class)
@SmallTest
public class ReportUtilsTests {

    private static final String TEST_XML_1 = "NetworkWatchlistTest/watchlist_config_test1.xml";
    private static final String TEST_XML_1_HASH =
            "C99F27A08B1FDB15B101098E12BB2A0AA0D474E23C50F24920A52AB2322BFD94";
    private static final String REPORT_HEADER_MAGIC = "8D370AAC";
    private static final String REPORT_HEADER_VERSION = "0001";

    private Context mContext;
    private File mTestXmlFile;

    @Before
    public void setUp() throws Exception {
        mContext = InstrumentationRegistry.getContext();
        mTestXmlFile =  new File(mContext.getFilesDir(), "test_watchlist_config.xml");
        mTestXmlFile.delete();
    }

    @After
    public void tearDown() throws Exception {
        mTestXmlFile.delete();
    }

    @Test
    public void testReportUtils_serializeReport() throws Exception {
        final byte[] expectedResult = HexDump.hexStringToByteArray(
                REPORT_HEADER_MAGIC + REPORT_HEADER_VERSION + TEST_XML_1_HASH
                        + "B86F9D37425340B635F43D6BC2506630761ADA71F5E6BBDBCA4651C479F9FB43" + "01"
                        + "C86F9D37425340B635F43D6BC2506630761ADA71F5E6BBDBCA4651C479F9FB44" + "00"
                        + "D86F9D37425340B635F43D6BC2506630761ADA71F5E6BBDBCA4651C479F9FB45" + "00"
                        + "E86F9D37425340B635F43D6BC2506630761ADA71F5E6BBDBCA4651C479F9FB46" + "01"
                        + "F86F9D37425340B635F43D6BC2506630761ADA71F5E6BBDBCA4651C479F9FB47" + "01"
        );
        HashMap<String, Boolean> input = new HashMap<>();
        input.put("C86F9D37425340B635F43D6BC2506630761ADA71F5E6BBDBCA4651C479F9FB44", false);
        input.put("B86F9D37425340B635F43D6BC2506630761ADA71F5E6BBDBCA4651C479F9FB43", true);
        input.put("D86F9D37425340B635F43D6BC2506630761ADA71F5E6BBDBCA4651C479F9FB45", false);
        input.put("E86F9D37425340B635F43D6BC2506630761ADA71F5E6BBDBCA4651C479F9FB46", true);
        input.put("F86F9D37425340B635F43D6BC2506630761ADA71F5E6BBDBCA4651C479F9FB47", true);

        copyWatchlistConfigXml(mContext, TEST_XML_1, mTestXmlFile);
        WatchlistConfig config = new WatchlistConfig(mTestXmlFile);

        byte[] result = ReportEncoder.serializeReport(config, input);
        assertArrayEquals(expectedResult, result);
    }

    private static void copyWatchlistConfigXml(Context context, String xmlAsset, File outFile)
            throws IOException {
        writeToFile(outFile, readAsset(context, xmlAsset));
    }

    private static String readAsset(Context context, String assetPath) throws IOException {
        final StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(
                        context.getResources().getAssets().open(assetPath)))) {
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
                sb.append(System.lineSeparator());
            }
        }
        return sb.toString();
    }

    private static void writeToFile(File path, String content)
            throws IOException {
        path.getParentFile().mkdirs();

        try (FileWriter writer = new FileWriter(path)) {
            writer.write(content);
        }
    }
}
