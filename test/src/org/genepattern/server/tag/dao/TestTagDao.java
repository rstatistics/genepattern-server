package org.genepattern.server.tag.dao;

import org.genepattern.junitutil.AnalysisJobUtil;
import org.genepattern.junitutil.DbUtil;
import org.genepattern.server.DbException;
import org.genepattern.server.config.GpContext;
import org.genepattern.server.tag.Tag;
import org.junit.*;
import org.junit.rules.TemporaryFolder;

import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by nazaire on 10/8/14.
 */
public class TestTagDao
{
    private TagDao dao;

    private GpContext userContext;

    static String user;
    static String admin;

    @Rule
    public TemporaryFolder temp = new TemporaryFolder();

    @Before
    public void setUp() throws Exception
    {
        DbUtil.initDb();
        user=DbUtil.addUserToDb("test");
        admin=DbUtil.addUserToDb("admin");

        dao = new TagDao();
    }

    @After
    public void tearDown() throws Exception
    {
        DbUtil.deleteAllRows(Tag.class);
    }

    @AfterClass
    static public void afterClass() throws Exception {
        DbUtil.shutdownDb();
    }

    /**
     * Test saving a record to the tag table
     *
     */
    @Test
    public void insertTag()
    {
        Date date = new Date();

        Tag tag = new Tag();
        tag.setDate(date);
        tag.setUserId(user);
        tag.setTag("insert test tag");
        tag.setPublicTag(true);

        dao.insertTag(tag);

        Tag result = dao.selectTagById(tag.getId());
        assertEquals("posted date", date, result.getDate());
        assertEquals("userId", user, result.getUserId());
        assertEquals("comment", tag.getTag(), result.getTag());
        assertEquals("public", tag.isPublicTag(), result.isPublicTag());
    }

    /**
     * Test saving a record to the tag table
     *
     */
    @Test
    public void deleteTag()
    {
        Date date = new Date();

        Tag tag = new Tag();
        tag.setDate(date);
        tag.setUserId(user);
        tag.setTag("Tag to delete");

        //add a tag
        dao.insertTag(tag);

        //now retrieve the tag
        Tag tagResult = dao.selectTagById(tag.getId());
        assertNotNull(tagResult);
        assertEquals("tag", tag.getTag(), tagResult.getTag());

        //now delete the tag
        boolean success = dao.deleteTag(tag.getId());
        assertTrue("success", success);

        //verify tag is not still in the database
        //by attempting to retrieve the tag
        tagResult = dao.selectTagById(tag.getId());
        assertNull(tagResult);
    }

    /**
     * Test getting a record for a user from the tag table
     *
     */
    @Test
    public void getTagsForUser()
    {
        Date date = new Date();

        //add tag for one user
        Tag userTag = new Tag();
        userTag.setDate(date);
        userTag.setUserId(user);
        userTag.setTag("user test tag");
        dao.insertTag(userTag);

        //add tag for another user
        Tag adminTag = new Tag();
        adminTag.setDate(date);
        adminTag.setUserId(admin);
        adminTag.setTag("user admin tag");
        dao.insertTag(adminTag);

        List<Tag> userTags = dao.selectTagsAvailableToUser(user, false);
        assertEquals("num user tags", 1, userTags.size());
        assertEquals("user tag", userTag.getTag(), userTags.get(0).getTag());

        List<Tag> adminTags = dao.selectTagsAvailableToUser(admin, false);
        assertEquals("num admin tags", 1, adminTags.size());
        assertEquals("admin tag", adminTag.getTag(), adminTags.get(0).getTag());

        //add public tag for admin user
        Tag publicTag = new Tag();
        publicTag.setDate(date);
        publicTag.setUserId(admin);
        publicTag.setTag("user admin public tag");
        publicTag.setPublicTag(true);
        dao.insertTag(publicTag);

        List<Tag> userAndPublicTags = dao.selectTagsAvailableToUser(user, true);
        assertEquals("num user and public tags", 2, userAndPublicTags.size());
    }
}