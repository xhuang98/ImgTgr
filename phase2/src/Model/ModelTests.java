package Model;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ModelTests {

    @Test
    void testContainer() {
        Container container = new Container();
        File test = new File("/");
        ImageManager im = new ImageManager(test);
        assertTrue(container.getImageManagers().isEmpty());
        container.addImageManager(im);
        assertFalse(container.getImageManagers().isEmpty());
        assertTrue(container.getTagManager() != null);
    }

    @Test
    void testContainerDirectory() {
        Container cont = new Container();
        File test = new File("/");
        cont.setDirectory(test);
        assertTrue(test == cont.getDirectory());
    }

    @Test
    void testImageInitNoTags() {
        File test = new File("/todo.txt");
        String testDir = test.getAbsoluteFile().toPath().getParent().toString();
        Image image = new Image(test);
        assertEquals(image.toString(), "todo.txt");
        assertEquals(image.getImageFile(), test);
        assertEquals(image.getDirectory(), Paths.get(testDir));
        assertFalse(image.getLifeTimeTags().isEmpty());
        assertEquals(image.getImageFile(), test);
    }

    @Test
    void testImageInitWithTags() {
        try {
            Tag tag1 = new Tag("tag1");
            Tag tag2 = new Tag("tag2");
            ArrayList<Tag> listOfTag = new ArrayList<>();
            listOfTag.add(tag1);
            listOfTag.add(tag2);
            File test = new File("/todo.txt");
            Image image = new Image(test, listOfTag);
            assertEquals(image.getCurrentTags().size(), 2);
        } catch (TagNamingException ex) {
            ex.getMessage();
        }
    }

    @Test
    void testImageAddTag(){
        try {
            Tag tag1 = new Tag("tag1");
            File test = new File("/todo.txt");
            Image image = new Image(test);
            image.addTag(tag1);
            assertEquals(1, image.getCurrentTags().size(), "currentTags");
            assertEquals(2, image.getLifeTimeTags().size(), "LifeTimeTags");
            assertEquals(1,image.getLog().size(),"Log");
            assertEquals(2,image.getNameHistory().size(), "NameHistory");
        } catch (TagNamingException ex) {
            ex.getMessage();
        }
    }

    @Test
    void testImageAddExistingTag() {
        try {
            Tag tag1 = new Tag("tag1");
            File test = new File("/todo.txt");
            Image image = new Image(test);
            image.addTag(tag1);
            assertTrue(image.addTag(tag1) == null);
        } catch (TagNamingException ex) {
            ex.getMessage();
        }
    }

    @Test
    void testImageRemoveTag(){
        try {
            Tag tag1 = new Tag("tag1");
            File test = new File("/todo.txt");
            Image image = new Image(test);
            image.addTag(tag1);
            image.removeTag(tag1);
            assertEquals(0, image.getCurrentTags().size(), "currentTags");
            assertEquals(3, image.getLifeTimeTags().size(), "LifeTimeTags");
            assertEquals(2,image.getLog().size(),"Log");
            assertEquals(3,image.getNameHistory().size(), "NameHistory");
        } catch (TagNamingException ex) {
            ex.getMessage();
        }
    }

    @Test
    void testImageRemoveAllTags() {
        try {
            Tag tag1 = new Tag("tag1");
            Tag tag2 = new Tag("tag2");
            ArrayList<Tag> listOfTag = new ArrayList<>();
            listOfTag.add(tag1);
            listOfTag.add(tag2);
            File test = new File("/todo.txt");
            Image image = new Image(test, listOfTag);
            image.removeAllTags();
            assertTrue(image.getCurrentTags().isEmpty());
        } catch (TagNamingException ex) {
            ex.getMessage();
        }
    }

    @Test
    void testImageResetTags(){
        try {
            Tag tag1 = new Tag("tag1");
            File test = new File("/todo.txt");
            Image image = new Image(test);
            image.addTag(tag1);
            image.resetTags(0);
            assertEquals(0, image.getCurrentTags().size(), "currentTags");
        } catch (TagNamingException ex) {
            ex.getMessage();
        }
    }

    @Test
    void testImageGetNameOnly() {
        try {
            Tag tag1 = new Tag("tag1");
            File test = new File("/todo.txt");
            Image image = new Image(test);
            image.addTag(tag1);
            assertEquals("todo", image.getName());
        } catch (TagNamingException ex) {
            ex.getMessage();
        }
    }

    @Test
    void testImageSetManager() {
        File test = new File("/todo.txt");
        Image image = new Image(test);
        File testdir = new File("/");
        ImageManager im = new ImageManager(testdir);
        image.setManager(im);
        assertEquals(im, image.getManager());
    }

    @Test
    void testImageEquals() {
        File test = new File("/todo.txt");
        Image image1 = new Image(test);
        Image image2 = new Image(test);
        assertTrue(image1.equals(image2), "Images are equal");
        Image image3 = new Image(new File("/other.img"));
        assertFalse(image1.equals(image3), "Different names");
        Image image4 = new Image(new File("/dir/todo.txt"));
        assertFalse(image1.equals(image4), "Different directories");
    }

    @Test
    void testTagInitGoodName() {
        try {
            Tag tag = new Tag("Test");
            assertTrue(tag.getName() != null);
            assertTrue(tag.getTaggedImages().isEmpty());
        } catch (TagNamingException ex) {
            assertFalse(true, " An exception was raised.");
        }
    }

    @Test
    void testTagInitBadName() {
        try {
            Tag tag = new Tag("@Test");
            assertFalse(true, "If exception is not caught.");
        } catch (TagNamingException ex) {
            assertTrue(true);
        }
    }

    @Test
    void testIMAddImage() {
        try {
            Tag tag1 = new Tag("tag1");
            Tag tag2 = new Tag("tag2");
            ArrayList<Tag> listOfTag = new ArrayList<>();
            ArrayList<Tag> listEmpty = new ArrayList<>();
            listOfTag.add(tag1);
            listOfTag.add(tag2);
            File test = new File("/");
            ImageManager im = new ImageManager(test);
            File img = new File("/test.img");
            im.addImage(img, listOfTag);
            im.addImage(img, listEmpty);
            assertFalse(im.getImages().isEmpty());
            assertEquals(1, im.getImages().size());
        } catch (TagNamingException ex) {
            ex.getMessage();
        }
    }

    @Test
    void testIMAddRemoveImg() {
        Image img = new Image(new File("test.img"));
        ImageManager im = new ImageManager(new File("/"));
        im.addImage(img);
        assertFalse(im.getImages().isEmpty());
        im.removeImage(img);
        assertTrue(im.getImages().isEmpty());
    }

    @Test
    void testTMAddRemoveTag() {
        try {
            Tag tag1 = new Tag("tag1");
            Tag tag2 = new Tag("tag2");
            TagManager tm = new TagManager();
            tm.addTag(tag1);
            tm.addTag(tag2);
            tm.addTag(tag1);
            assertEquals(2, tm.getTags().size());
            tm.deleteTag(tag1);
            assertEquals(1, tm.getTags().size());
        } catch (TagNamingException ex) {
            ex.getMessage();
        }
    }
}
