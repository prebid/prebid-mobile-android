package org.prebid.mobile.rendering.sdk;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.prebid.mobile.rendering.sdk.scripts.JsScriptData;

import java.util.ArrayList;
import java.util.List;

public class JSLibraryManagerTest {

    private AutoCloseable mockManager;

    @Mock
    private JsScriptsDownloader mockDownloader;

    private final List<Runnable> diskTasks = new ArrayList<>();
    private int createdDownloaders = 0;
    private RuntimeException createDownloaderError;

    private JSLibraryManager manager;

    @Before
    public void setUp() {
        mockManager = MockitoAnnotations.openMocks(this);

        manager = new JSLibraryManager(this::createDownloader, diskTasks::add);
    }

    @After
    public void cleanUp() throws Exception {
        mockManager.close();
    }

    @Test
    public void checkScripts_doesNotTouchDiskOnCallingThread() {
        boolean loaded = manager.checkIfScriptsDownloadedAndStartDownloadingIfNot();

        assertFalse(loaded);
        assertEquals(0, createdDownloaders);
        verifyNoInteractions(mockDownloader);
        assertEquals(1, diskTasks.size());
    }

    @Test
    public void checkScripts_scriptsOnDisk_readsThemOnDiskThread() {
        when(mockDownloader.areScriptsDownloadedAlready()).thenReturn(true);
        when(mockDownloader.readFile(JsScriptData.openMeasurementData)).thenReturn("omsdk");
        when(mockDownloader.readFile(JsScriptData.mraidData)).thenReturn("mraid");

        manager.checkIfScriptsDownloadedAndStartDownloadingIfNot();
        runDiskTasks();

        assertEquals("omsdk", manager.getOMSDKScript());
        assertEquals("mraid", manager.getMRAIDScript());
        verify(mockDownloader, never()).downloadScripts(any());
    }

    @Test
    public void checkScripts_scriptsNotOnDisk_downloadsThemOnDiskThread() {
        when(mockDownloader.areScriptsDownloadedAlready()).thenReturn(false);

        manager.checkIfScriptsDownloadedAndStartDownloadingIfNot();
        runDiskTasks();

        verify(mockDownloader).downloadScripts(any());
        verify(mockDownloader, never()).readFile(any());
    }

    @Test
    public void checkScripts_scriptsLoaded_returnsTrueWithoutDiskTask() {
        when(mockDownloader.areScriptsDownloadedAlready()).thenReturn(true);
        when(mockDownloader.readFile(JsScriptData.openMeasurementData)).thenReturn("omsdk");
        when(mockDownloader.readFile(JsScriptData.mraidData)).thenReturn("mraid");
        manager.checkIfScriptsDownloadedAndStartDownloadingIfNot();
        runDiskTasks();

        boolean loaded = manager.checkIfScriptsDownloadedAndStartDownloadingIfNot();

        assertTrue(loaded);
        assertTrue(diskTasks.isEmpty());
    }

    @Test
    public void checkScripts_createDownloaderFails_nextCheckTriesAgain() {
        createDownloaderError = new RuntimeException("No files dir");
        manager.checkIfScriptsDownloadedAndStartDownloadingIfNot();
        runDiskTasks();
        createDownloaderError = null;
        when(mockDownloader.areScriptsDownloadedAlready()).thenReturn(true);
        when(mockDownloader.readFile(JsScriptData.openMeasurementData)).thenReturn("omsdk");
        when(mockDownloader.readFile(JsScriptData.mraidData)).thenReturn("mraid");

        boolean loaded = manager.checkIfScriptsDownloadedAndStartDownloadingIfNot();
        runDiskTasks();

        assertFalse(loaded);
        assertEquals(2, createdDownloaders);
        assertEquals("omsdk", manager.getOMSDKScript());
        assertEquals("mraid", manager.getMRAIDScript());
    }

    @Test
    public void checkScripts_readFails_keepsScriptsEmpty() {
        when(mockDownloader.areScriptsDownloadedAlready()).thenReturn(true);
        when(mockDownloader.readFile(JsScriptData.openMeasurementData)).thenReturn(null);
        when(mockDownloader.readFile(JsScriptData.mraidData)).thenReturn("mraid");
        manager.checkIfScriptsDownloadedAndStartDownloadingIfNot();
        runDiskTasks();

        boolean loaded = manager.checkIfScriptsDownloadedAndStartDownloadingIfNot();

        assertFalse(loaded);
        assertEquals("", manager.getOMSDKScript());
        assertEquals("", manager.getMRAIDScript());
        assertEquals(1, diskTasks.size());
    }

    @Test
    public void startScriptReadingTask_doesNotTouchDiskOnCallingThread() {
        manager.startScriptReadingTask();

        assertEquals(0, createdDownloaders);
        verifyNoInteractions(mockDownloader);
        assertEquals(1, diskTasks.size());
    }

    @Test
    public void startScriptReadingTask_scriptsNotOnDisk_doesNotDownloadThem() {
        when(mockDownloader.areScriptsDownloadedAlready()).thenReturn(false);

        manager.startScriptReadingTask();
        runDiskTasks();

        verify(mockDownloader, never()).downloadScripts(any());
        verify(mockDownloader, never()).readFile(any());
    }

    private JsScriptsDownloader createDownloader() {
        createdDownloaders++;
        if (createDownloaderError != null) {
            throw createDownloaderError;
        }
        return mockDownloader;
    }

    private void runDiskTasks() {
        List<Runnable> tasks = new ArrayList<>(diskTasks);
        diskTasks.clear();
        for (Runnable task : tasks) {
            task.run();
        }
    }

}
