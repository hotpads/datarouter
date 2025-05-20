/*
 * Copyright © 2009 HotPads (admin@hotpads.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.datarouter.client.git;

import java.nio.file.Path;
import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.eclipse.jgit.api.CheckoutCommand;
import org.eclipse.jgit.api.CleanCommand;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.CommitCommand;
import org.eclipse.jgit.api.DiffCommand;
import org.eclipse.jgit.api.FetchCommand;
import org.eclipse.jgit.api.GarbageCollectCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.InitCommand;
import org.eclipse.jgit.api.ListBranchCommand;
import org.eclipse.jgit.api.LogCommand;
import org.eclipse.jgit.api.LsRemoteCommand;
import org.eclipse.jgit.api.MergeCommand;
import org.eclipse.jgit.api.PullCommand;
import org.eclipse.jgit.api.RebaseCommand;
import org.eclipse.jgit.api.ResetCommand;
import org.eclipse.jgit.api.TransportCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class DatarouterGitService{

	@Inject
	private DatarouterGitRunService runService;
	@Inject
	private DatarouterGitProgressMonitorFactory progressMonitorFactory;

	public Optional<Ref> lsRemoteBranch(String url, String ref, Duration timeout){
		boolean includeHeads = ref.startsWith(Constants.R_HEADS);
		boolean includeTags = ref.startsWith(Constants.R_TAGS);

		Ref foundRef = runWithGit(git -> git.lsRemote(timeout)
				.setRemote(url)
				.setHeads(includeHeads)
				.setTags(includeTags)
				.callAsMap()
				.get(ref));

		return Optional.ofNullable(foundRef);
	}

	public <T> T runWithGit(DatarouterGitTransportCommands.Runner<T> runner){
		return DatarouterGitRunService.run(() -> runner.run(new DatarouterGitTransportCommands()));
	}

	public <T> T runLocalGit(Path dir, DatarouterGitLocalCommands.Runner<T> runner){
		return DatarouterGitRunService.run(dir, git -> runner.run(new DatarouterGitLocalCommands(git)));
	}

	public <T> T runRemoteGit(Path dir, DatarouterGitRemoteCommands.Runner<T> runner){
		return DatarouterGitRunService.run(dir, git -> runner.run(new DatarouterGitRemoteCommands(git)));
	}

	public class DatarouterGitTransportCommands{

		private DatarouterGitTransportCommands(){
		}

		public CloneCommand clone(Duration timeout){
			return setup(Git.cloneRepository(), timeout)
					.setProgressMonitor(progressMonitorFactory.newMonitor(DatarouterGitOp.CLONE));
		}

		public LsRemoteCommand lsRemote(Duration timeout){
			return setup(new LsRemoteCommand(null){

				@Override
				public Collection<Ref> call() throws GitAPIException, InvalidRemoteException, TransportException{
					DatarouterGitMetrics.incOp(DatarouterGitOp.LS_REMOTE);
					return super.call();
				}

				@Override
				public Map<String,Ref> callAsMap() throws GitAPIException, InvalidRemoteException, TransportException{
					DatarouterGitMetrics.incOp(DatarouterGitOp.LS_REMOTE);
					return super.callAsMap();
				}

			}, timeout);
		}

		public InitCommand init(){
			return new InitCommand(){
				@Override
				public Git call() throws GitAPIException{
					DatarouterGitMetrics.incOp(DatarouterGitOp.INIT);
					return super.call();
				}
			};
		}

		protected <R,T extends TransportCommand<T,R>> T setup(T op, Duration timeout){
			return op.setTransportConfigCallback(runService.transport)
					.setTimeout((int)timeout.getSeconds());
		}

		public interface Runner<T>{
			T run(DatarouterGitTransportCommands commands) throws Exception;
		}

	}

	public class DatarouterGitRemoteCommands extends DatarouterGitTransportCommands{

		public final Git git;
		public final DatarouterGitLocalCommands local;

		private DatarouterGitRemoteCommands(Git git){
			this.git = git;
			this.local = new DatarouterGitLocalCommands(git);
		}

		public PullCommand pull(Duration timeout){
			return setup(git.pull(), timeout)
					.setProgressMonitor(progressMonitorFactory.newMonitor(DatarouterGitOp.PULL));
		}

		public FetchCommand fetch(Duration timeout){
			return setup(git.fetch(), timeout)
					.setProgressMonitor(progressMonitorFactory.newMonitor(DatarouterGitOp.FETCH));
		}

		public interface Runner<T>{
			T run(DatarouterGitRemoteCommands commands) throws Exception;
		}

	}

	public class DatarouterGitLocalCommands{

		public final Git git;

		private DatarouterGitLocalCommands(Git git){
			this.git = git;
		}

		public ListBranchCommand branchList(){
			return new ListBranchCommand(git.getRepository()){
				@Override
				public List<Ref> call() throws GitAPIException{
					DatarouterGitMetrics.incOp(DatarouterGitOp.LIST_BRANCH);
					return super.call();
				}
			};
		}

		public CommitCommand commit(){
			return new CommitCommand(git.getRepository()){
				@Override
				public RevCommit call() throws GitAPIException, NoHeadException{
					DatarouterGitMetrics.incOp(DatarouterGitOp.COMMIT);
					return super.call();
				}
			};
		}

		public CheckoutCommand checkout(){
			return git.checkout().setProgressMonitor(progressMonitorFactory.newMonitor(DatarouterGitOp.CHECKOUT));
		}

		public DiffCommand diff(){
			return git.diff().setProgressMonitor(progressMonitorFactory.newMonitor(DatarouterGitOp.DIFF));
		}

		public GarbageCollectCommand gc(){
			return git.gc().setProgressMonitor(progressMonitorFactory.newMonitor(DatarouterGitOp.GC));
		}

		public LogCommand log(){
			return new LogCommand(git.getRepository()){
				@Override
				public Iterable<RevCommit> call() throws GitAPIException, NoHeadException{
					DatarouterGitMetrics.incOp(DatarouterGitOp.LOG);
					return super.call();
				}
			};
		}

		public MergeCommand merge(){
			return git.merge().setProgressMonitor(progressMonitorFactory.newMonitor(DatarouterGitOp.MERGE));
		}

		public RebaseCommand rebase(){
			return git.rebase().setProgressMonitor(progressMonitorFactory.newMonitor(DatarouterGitOp.REBASE));
		}

		public Repository getRepository(){
			return git.getRepository();
		}

		public ResetCommand reset(){
			return git.reset().setProgressMonitor(progressMonitorFactory.newMonitor(DatarouterGitOp.RESET));
		}

		public CleanCommand clean(){
			return git.clean();
		}

		public interface Runner<T>{
			T run(DatarouterGitLocalCommands commands) throws Exception;
		}

	}

}
