package com.example.networkmusicplayer.filemanagement

import android.content.Context
import android.content.ContextWrapper
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import java.lang.reflect.Field

class MusicLibraryScannerTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    //Runs before and after each test
    @Before
    @After
    fun resetSingleton() {
        resetScannerInstance()
    }

    //Uses reflection to reset the singleton when all tests are run at once to prevent collisions between tests
    private fun resetScannerInstance() {
        try {
            val field: Field = MusicLibraryScanner::class.java.getDeclaredField("instance")
            field.isAccessible = true
            field.set(null, null)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun getContextFromInstance(scanner: MusicLibraryScanner): Context? {
        val field: Field = MusicLibraryScanner::class.java.getDeclaredField("m_MainContext")
        field.isAccessible = true
        return field.get(scanner) as? Context
    }

    // getInstance() Tests

    @Test
    fun testGetInstance_returnsNonNullInstance() {
        val scanner = MusicLibraryScanner.getInstance()
        assertNotNull("getInstance() should return a non-null instance", scanner)
    }

    @Test
    fun testGetInstance_returnsSameSingletonInstance() {
        val first = MusicLibraryScanner.getInstance()
        val second = MusicLibraryScanner.getInstance()
        assertSame("Multiple calls to getInstance() should return the exact same instance", first, second)
    }

    @Test
    fun testGetInstance_createsInstanceWithNullContext() {
        val scanner = MusicLibraryScanner.getInstance()
        val context = getContextFromInstance(scanner)
        assertNull("getInstance() created before Init() should have null context", context)
    }

    // Init() Tests

    @Test
    fun testInit_initializesInstanceWithContext() {
        val testContext = DummyTestContext()
        MusicLibraryScanner.Init(testContext)

        val scanner = MusicLibraryScanner.getInstance()
        val context = getContextFromInstance(scanner)

        assertNotNull("getInstance() after Init() should return non-null instance", scanner)
        assertEquals("Init() should set m_MainContext to the provided context", testContext, context)
    }

    @Test
    fun testInit_whenInstanceAlreadyExists_doesNotOverwriteExistingInstance() {
        val firstContext = DummyTestContext()
        val secondContext = DummyTestContext()

        MusicLibraryScanner.Init(firstContext)
        val firstInstance = MusicLibraryScanner.getInstance()

        // Call Init again with a different context
        MusicLibraryScanner.Init(secondContext)
        val secondInstance = MusicLibraryScanner.getInstance()

        assertSame("Init() should not overwrite an already existing instance", firstInstance, secondInstance)
        assertEquals("Context should remain the first context", firstContext, getContextFromInstance(secondInstance))
    }

    @Test
    fun testInit_whenGetInstanceCalledFirst_doesNotOverwriteWithInit() {
        val scanner = MusicLibraryScanner.getInstance()
        val testContext = DummyTestContext()

        MusicLibraryScanner.Init(testContext)

        assertNull("Init() after getInstance() should return early and not overwrite instance context", getContextFromInstance(scanner))
    }

    // scanDirectory() Tests

    @Test
    fun testScanDirectory_nullDirectory_returnsEmptyList() {
        val scanner = MusicLibraryScanner.getInstance()
        val result = scanner.scanDirectory(null)

        assertNotNull("scanDirectory(null) should return a non-null list", result)
        assertTrue("scanDirectory(null) should return an empty list", result.isEmpty())
    }

    @Test
    fun testScanDirectory_nonExistentDirectory_returnsEmptyList() {
        val scanner = MusicLibraryScanner.getInstance()
        val nonExistentDir = File(tempFolder.root, "does_not_exist_folder")

        val result = scanner.scanDirectory(nonExistentDir)

        assertNotNull("scanDirectory() with non-existent directory should return a non-null list", result)
        assertTrue("scanDirectory() with non-existent directory should return an empty list", result.isEmpty())
    }

    @Test
    fun testScanDirectory_fileIsNotDirectory_returnsEmptyList() {
        val scanner = MusicLibraryScanner.getInstance()
        val regularFile = tempFolder.newFile("sample.txt")

        val result = scanner.scanDirectory(regularFile)

        assertNotNull("scanDirectory() with a regular file should return a non-null list", result)
        assertTrue("scanDirectory() with a regular file should return an empty list", result.isEmpty())
    }

    @Test
    fun testScanDirectory_emptyDirectory_returnsEmptyList() {
        val scanner = MusicLibraryScanner.getInstance()
        val emptyDir = tempFolder.newFolder("empty_music_dir")

        val result = scanner.scanDirectory(emptyDir)

        assertNotNull("scanDirectory() with empty directory should return a non-null list", result)
        assertTrue("scanDirectory() with empty directory should return an empty list", result.isEmpty())
    }

    @Test
    fun testScanDirectory_whenNoPermission_returnsEmptyList() {
        val scanner = MusicLibraryScanner.getInstance()
        val musicDir = tempFolder.newFolder("music_dir")
        tempFolder.newFile("music_dir/song.mp3")

        val result = scanner.scanDirectory(musicDir)

        assertNotNull("scanDirectory() without permission should return a non-null list", result)
        assertTrue("scanDirectory() without permission should return an empty list", result.isEmpty())
    }

    @Test
    fun testScanDirectory_withVariousFileExtensions_handlesFiles() {
        val scanner = MusicLibraryScanner.getInstance()
        val musicDir = tempFolder.newFolder("mixed_files_dir")
        tempFolder.newFile("mixed_files_dir/song1.mp3")
        tempFolder.newFile("mixed_files_dir/song2.flac")
        tempFolder.newFile("mixed_files_dir/document.pdf")
        tempFolder.newFile("mixed_files_dir/image.png")

        val subDir = File(musicDir, "subfolder")
        subDir.mkdir()
        File(subDir, "song3.wav").createNewFile()
        File(subDir, "readme.txt").createNewFile()

        val result = scanner.scanDirectory(musicDir)

        assertNotNull("scanDirectory() should return a non-null list when processing directory entries", result)
    }

    private class DummyTestContext : ContextWrapper(null)
}
