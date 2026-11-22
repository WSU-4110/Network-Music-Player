package filemanagement
// Test 1: This test verifies that the Track constructor  initializes the object with the correct default values.

import com.example.networkmusicplayer.filemanagement.Track
import org.junit.Assert.*
import org.junit.Test

class TrackTest {

    // To verify that a newly created Track object
      // contains the expected default values.
    @Test
    fun testDefaultValues() {

        // To create a new Track object.
        val track = Track()

        // Check the default title.
        assertEquals("", track.title)

        // Check the default artist.
        assertEquals("Unknown artist", track.artist)

        // Check the default album.
        assertEquals("Unknown album", track.album)

        // Check that the track is not a favorite by default.
        assertFalse(track.isFavorite)
    }


// Test 2: Verify that copy() creates a different object.

    @Test
    fun testCopyCreatesNewObject() {

        // Create the original Track object.
        val original = Track()

        // Create a copy of the original Track object.
        val copy = original.copy()

        // Check that the original and copy are NOT the same object. This verifies that copy() creates a new object.
        assertNotSame(original, copy)
    }

    //Test 3: Verify that copy() copies the title correctly.
    @Test
    fun testCopyTitle() {

        // Create the original Track object.
        val original = Track()

        // Set the original Track's title to "Song A".
        original.title = "Song A"

        // Create a copy of the original Track object.
        val copy = original.copy()

        // Check that the copied Track's title is equal to "Song A".
        assertEquals("Song A", copy.title)
    }
//  Test 4: Verify that copy() copies the artist correctly.
    @Test
    fun testCopyArtist() {

        // Create the original Track object.
        val original = Track()

        // Set the original Track's artist to "Drake".
        original.artist = "Drake"

        // Create a copy of the original Track object.
        val copy = original.copy()

        // Check that the copied Track's artist is equal to "Drake".
        assertEquals("Drake", copy.artist)
    }
    //Test 5: Verify that copy() copies the album correctly.

    @Test
    fun testCopyAlbum() {

        // Create the original Track object.
        val original = Track()

        // Set the original Track's album to "Views".
        original.album = "Views"

        // Create a copy of the original Track object.
        val copy = original.copy()

        // Check that the copied Track's album is equal to "Views".
        assertEquals("Views", copy.album)
    }
    // Test 6:  Verify that copy() copies favorite status correctly.
    @Test
    fun testCopyFavorite() {

        // Create the original Track object.
        val original = Track()

        // Set the original Track's favorite status to true.
        original.isFavorite = true

        // Create a copy of the original Track object.
        val copy = original.copy()

        // Check that the copied Track's favorite status is true.
        assertTrue(copy.isFavorite)
    }
}