import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Optional;
 
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListBranchCommand.ListMode;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.TextProgressMonitor;
import org.eclipse.jgit.revwalk.RevCommit;
 



public class Test {
 
	public static void main(String[] args)
			throws IOException, InvalidRemoteException, TransportException, GitAPIException {
 
		// Local directory on this machine where we will clone remote repo.
		File localRepoDir = new File("C:\\Users\\Public\\ift3913_tp4");
 
		// Monitor to get git command progress printed on java System.out console
		TextProgressMonitor consoleProgressMonitor = new TextProgressMonitor(new PrintWriter(System.out));
 
		/*
		 * Git clone remote repo into local directory.
		 * 
		 * Equivalent of --> $ git clone https://github.com/Ravikharatmal/test.git
		 */
		System.out.println("\n>>> Cloning repository\n");
		Repository repo = Git.cloneRepository().setProgressMonitor(consoleProgressMonitor).setDirectory(localRepoDir).setURI("https://github.com/Ravikharatmal/test.git").call().getRepository();
 
		try (Git git = new Git(repo)) {
			/*
			 * Get list of all branches (including remote) & print
			 * 
			 * Equivalent of --> $ git branch -a
			 * 
			 */
			System.out.println("\n>>> Listing all branches\n");
			git.branchList().setListMode(ListMode.ALL).call().stream().forEach(r -> System.out.println(r.getName()));
 
			
			/*
			 * Verify commit log
			 * 
			 * Equivalent of --> $ git log
			 * 
			 */
			System.out.println("\n>>> Printing commit log\n");
			Iterable<RevCommit> commitLog = git.log().call();
			commitLog.forEach(r -> System.out.println(r.getFullMessage()));
 
		}
 
	}
 
}
