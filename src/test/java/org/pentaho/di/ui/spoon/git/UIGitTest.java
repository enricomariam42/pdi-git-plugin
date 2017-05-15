package org.pentaho.di.ui.spoon.git;

import static org.junit.Assert.*;

import java.io.IOException;
import java.net.URISyntaxException;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoFilepatternException;
import org.eclipse.jgit.junit.RepositoryTestCase;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.StoredConfig;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.transport.RemoteConfig;
import org.eclipse.jgit.transport.URIish;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.ui.repository.pur.repositoryexplorer.model.UIRepositoryObjectRevisions;
import org.pentaho.di.ui.repository.repositoryexplorer.model.UIJob;
import org.pentaho.di.ui.repository.repositoryexplorer.model.UIRepositoryObjects;
import org.pentaho.di.ui.repository.repositoryexplorer.model.UITransformation;
import org.pentaho.di.ui.spoon.git.model.UIGit;

public class UIGitTest extends RepositoryTestCase {
  private Git git;
  private UIGit uiGit;

  @Before
  public void setUp() throws Exception {
    super.setUp();
    git = new Git( db );
    uiGit = new UIGit();
    uiGit.setGit( git );
    uiGit.setAuthorName( "test <test@example.com>" );
    uiGit.setCommitMessage( "test" );
  }

  @Test
  public void testGetAuthorName() {
    assertEquals( "test <test@example.com>", uiGit.getAuthorName() );
  }

  @Test
  public void testGetCommitMessage() {
    assertEquals( "test", uiGit.getCommitMessage() );
  }

  @Test
  public void testGetBranch() {
    assertEquals( "master", uiGit.getBranch() );
  }

  @Test
  public void testGetRemote() throws Exception {
    setupRemote();

    assertNotNull( uiGit.getRemote() );
  }

  private RemoteConfig setupRemote() throws IOException, URISyntaxException {
    // create another repository
    Repository remoteRepository = createWorkRepository();

    // set it up as a remote to this repository
    final StoredConfig config = db.getConfig();
    RemoteConfig remoteConfig = new RemoteConfig( config, Constants.DEFAULT_REMOTE_NAME );

    RefSpec refSpec = new RefSpec();
    refSpec = refSpec.setForceUpdate( true );
    refSpec = refSpec.setSourceDestination( Constants.R_HEADS + "*",
                   Constants.R_REMOTES + Constants.DEFAULT_REMOTE_NAME + "/*" );
    remoteConfig.addFetchRefSpec( refSpec );

    URIish uri = new URIish( remoteRepository.getDirectory().toURI().toURL() );
    remoteConfig.addURI( uri );

    remoteConfig.update( config );
    config.save();

    return remoteConfig;
  }

  @Test
  public void testGetRevisionObjects() throws IOException, NoFilepatternException, GitAPIException {
    writeTrashFile( "Test.txt", "Hello world" );
    git.add().addFilepattern( "Test.txt" ).call();
    git.commit().setMessage( "initial commit" ).call();
    UIRepositoryObjectRevisions revisions = uiGit.getRevisionObjects();
    assertEquals( 1, revisions.size() );
  }

  @Test
  public void testGetUnstagedObjects() throws Exception {
    writeTrashFile( "a.ktr", "content" );
    writeTrashFile( "b.kjb", "content" );
    UIRepositoryObjects stagedObjects = uiGit.getUnstagedObjects();
    assertEquals( 2, stagedObjects.size() );
    assertEquals( UITransformation.class, stagedObjects.get( 0 ).getClass() );
    assertEquals( UIJob.class, stagedObjects.get( 1 ).getClass() );
  }

  @Test
  public void testGetStagedObjects() throws Exception {
    writeTrashFile( "a.ktr", "content" );
    writeTrashFile( "b.kjb", "content" );
    git.add().addFilepattern( "." ).call();
    UIRepositoryObjects stagedObjects = uiGit.getStagedObjects();
    assertEquals( 2, stagedObjects.size() );
    assertEquals( UITransformation.class, stagedObjects.get( 0 ).getClass() );
    assertEquals( UIJob.class, stagedObjects.get( 1 ).getClass() );
  }
}
