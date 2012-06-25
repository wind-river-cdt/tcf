/*******************************************************************************
 * Copyright (c) 2011, 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.filesystem.core.internal.utils;

import java.util.concurrent.TimeoutException;

import org.eclipse.core.runtime.Assert;
import org.eclipse.osgi.util.NLS;
import org.eclipse.tcf.protocol.IToken;
import org.eclipse.tcf.protocol.Protocol;
import org.eclipse.tcf.services.IFileSystem;
import org.eclipse.tcf.te.tcf.core.concurrent.Rendezvous;
import org.eclipse.tcf.te.tcf.filesystem.core.internal.operations.Operation;
import org.eclipse.tcf.te.tcf.filesystem.core.nls.Messages;

/**
 * A blocking call proxy for a file system service.
 * All calls to the service method are blocked until its
 * "Done" handler is invoked.
 * <p>
 * <em>Note that all method call over the proxy must be made <b>OUTSIDE</b> of
 * the dispatching thread.</em> If it is called inside of the dispatching thread, the call will be
 * blocked forever.
 * <p>
 * This class is used to replace BlockingProxyCall for better debugability.
 * 
 * @see BlockingCallProxy
 */
public class BlockingFileSystemProxy implements IFileSystem {
	// The default timeout waiting for blocked invocations.
	private static final long DEFAULT_TIMEOUT = Operation.DEFAULT_TIMEOUT;

	// The actual object that provides file system services.
	IFileSystem service;
	// The rendezvous used to synchronize invocation
	Rendezvous rendezvous;
	/**
	 * Constructor with an delegating service.
	 * 
	 * @param service The delegating service.
	 */
	public BlockingFileSystemProxy(IFileSystem service) {
		Assert.isNotNull(service);
		this.service = service;
		this.rendezvous = new Rendezvous();
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.protocol.IService#getName()
	 */
	@Override
	public String getName() {
		return service.getName();
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.services.IFileSystem#open(java.lang.String, int, org.eclipse.tcf.services.IFileSystem.FileAttrs, org.eclipse.tcf.services.IFileSystem.DoneOpen)
	 */
	@Override
	public IToken open(String file_name, int flags, FileAttrs attrs, final DoneOpen done) {
		Assert.isTrue(!Protocol.isDispatchThread());
		rendezvous.reset();
		IToken token = service.open(file_name, flags, attrs, new DoneOpen(){
			/*
			 * (non-Javadoc)
			 * @see org.eclipse.tcf.services.IFileSystem.DoneOpen#doneOpen(org.eclipse.tcf.protocol.IToken, org.eclipse.tcf.services.IFileSystem.FileSystemException, org.eclipse.tcf.services.IFileSystem.IFileHandle)
			 */
			@Override
            public void doneOpen(IToken token, FileSystemException error, IFileHandle handle) {
				done.doneOpen(token, error, handle);
				rendezvous.arrive();
            }});
		try {
			rendezvous.waiting(DEFAULT_TIMEOUT);
		}
		catch(TimeoutException e) {
			throw new RuntimeException(NLS.bind(Messages.BlockingFileSystemProxy_TimeoutOpeningFile, file_name), e);
		}
		return token;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.services.IFileSystem#close(org.eclipse.tcf.services.IFileSystem.IFileHandle, org.eclipse.tcf.services.IFileSystem.DoneClose)
	 */
	@Override
	public IToken close(IFileHandle handle, final DoneClose done) {
		Assert.isTrue(!Protocol.isDispatchThread());
		rendezvous.reset();
		IToken token = service.close(handle, new DoneClose(){
			/*
			 * (non-Javadoc)
			 * @see org.eclipse.tcf.services.IFileSystem.DoneClose#doneClose(org.eclipse.tcf.protocol.IToken, org.eclipse.tcf.services.IFileSystem.FileSystemException)
			 */
			@Override
            public void doneClose(IToken token, FileSystemException error) {
				done.doneClose(token, error);
				rendezvous.arrive();
            }});
		try {
			rendezvous.waiting(DEFAULT_TIMEOUT);
		}
		catch(TimeoutException e) {
			throw new RuntimeException(NLS.bind(Messages.BlockingFileSystemProxy_TimeoutClosingFile, handle), e);
		}
		return token;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.services.IFileSystem#read(org.eclipse.tcf.services.IFileSystem.IFileHandle, long, int, org.eclipse.tcf.services.IFileSystem.DoneRead)
	 */
	@Override
	public IToken read(IFileHandle handle, long offset, int len, final DoneRead done) {
		Assert.isTrue(!Protocol.isDispatchThread());
		rendezvous.reset();
		IToken token = service.read(handle, offset, len, new DoneRead(){
			/*
			 * (non-Javadoc)
			 * @see org.eclipse.tcf.services.IFileSystem.DoneRead#doneRead(org.eclipse.tcf.protocol.IToken, org.eclipse.tcf.services.IFileSystem.FileSystemException, byte[], boolean)
			 */
			@Override
            public void doneRead(IToken token, FileSystemException error, byte[] data, boolean eof) {
	            done.doneRead(token, error, data, eof);
	            rendezvous.arrive();
            }});
		try {
			rendezvous.waiting(DEFAULT_TIMEOUT);
		}
		catch(TimeoutException e) {
			throw new RuntimeException(NLS.bind(Messages.BlockingFileSystemProxy_TimeoutReadingFile, handle), e);
		}
		return token;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.services.IFileSystem#write(org.eclipse.tcf.services.IFileSystem.IFileHandle, long, byte[], int, int, org.eclipse.tcf.services.IFileSystem.DoneWrite)
	 */
	@Override
	public IToken write(IFileHandle handle, long offset, byte[] data, int data_pos, int data_size, final DoneWrite done) {
		Assert.isTrue(!Protocol.isDispatchThread());
		rendezvous.reset();
		IToken token = service.write(handle, offset, data, data_pos, data_size, new DoneWrite(){
			/*
			 * (non-Javadoc)
			 * @see org.eclipse.tcf.services.IFileSystem.DoneWrite#doneWrite(org.eclipse.tcf.protocol.IToken, org.eclipse.tcf.services.IFileSystem.FileSystemException)
			 */
			@Override
            public void doneWrite(IToken token, FileSystemException error) {
				done.doneWrite(token, error);
				rendezvous.arrive();
            }});
		try {
			rendezvous.waiting(DEFAULT_TIMEOUT);
		}
		catch(TimeoutException e) {
			throw new RuntimeException(NLS.bind(Messages.BlockingFileSystemProxy_TimeoutWritingFile, handle), e);
		}
		return token;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.services.IFileSystem#stat(java.lang.String, org.eclipse.tcf.services.IFileSystem.DoneStat)
	 */
	@Override
	public IToken stat(String path, final DoneStat done) {
		Assert.isTrue(!Protocol.isDispatchThread());
		rendezvous.reset();
		IToken token = service.stat(path, new DoneStat(){
			/*
			 * (non-Javadoc)
			 * @see org.eclipse.tcf.services.IFileSystem.DoneStat#doneStat(org.eclipse.tcf.protocol.IToken, org.eclipse.tcf.services.IFileSystem.FileSystemException, org.eclipse.tcf.services.IFileSystem.FileAttrs)
			 */
			@Override
            public void doneStat(IToken token, FileSystemException error, FileAttrs attrs) {
	            done.doneStat(token, error, attrs);
	            rendezvous.arrive();
            }});
		try {
			rendezvous.waiting(DEFAULT_TIMEOUT);
		}
		catch(TimeoutException e) {
			throw new RuntimeException(NLS.bind(Messages.BlockingFileSystemProxy_TimeoutStat, path), e);
		}
		return token;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.services.IFileSystem#lstat(java.lang.String, org.eclipse.tcf.services.IFileSystem.DoneStat)
	 */
	@Override
	public IToken lstat(String path, final DoneStat done) {
		Assert.isTrue(!Protocol.isDispatchThread());
		rendezvous.reset();
		IToken token = service.lstat(path, new DoneStat(){
			/*
			 * (non-Javadoc)
			 * @see org.eclipse.tcf.services.IFileSystem.DoneStat#doneStat(org.eclipse.tcf.protocol.IToken, org.eclipse.tcf.services.IFileSystem.FileSystemException, org.eclipse.tcf.services.IFileSystem.FileAttrs)
			 */
			@Override
            public void doneStat(IToken token, FileSystemException error, FileAttrs attrs) {
	            done.doneStat(token, error, attrs);
	            rendezvous.arrive();
            }});
		try {
			rendezvous.waiting(DEFAULT_TIMEOUT);
		}
		catch(TimeoutException e) {
			throw new RuntimeException(NLS.bind(Messages.BlockingFileSystemProxy_TimeoutLstat, path), e);
		}
		return token;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.services.IFileSystem#fstat(org.eclipse.tcf.services.IFileSystem.IFileHandle, org.eclipse.tcf.services.IFileSystem.DoneStat)
	 */
	@Override
	public IToken fstat(IFileHandle handle, final DoneStat done) {
		Assert.isTrue(!Protocol.isDispatchThread());
		rendezvous.reset();
		IToken token = service.fstat(handle, new DoneStat(){
			/*
			 * (non-Javadoc)
			 * @see org.eclipse.tcf.services.IFileSystem.DoneStat#doneStat(org.eclipse.tcf.protocol.IToken, org.eclipse.tcf.services.IFileSystem.FileSystemException, org.eclipse.tcf.services.IFileSystem.FileAttrs)
			 */
			@Override
            public void doneStat(IToken token, FileSystemException error, FileAttrs attrs) {
	            done.doneStat(token, error, attrs);
	            rendezvous.arrive();
            }});
		try {
			rendezvous.waiting(DEFAULT_TIMEOUT);
		}
		catch(TimeoutException e) {
			throw new RuntimeException(NLS.bind(Messages.BlockingFileSystemProxy_TimeoutFstat, handle), e);
		}
		return token;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.services.IFileSystem#setstat(java.lang.String, org.eclipse.tcf.services.IFileSystem.FileAttrs, org.eclipse.tcf.services.IFileSystem.DoneSetStat)
	 */
	@Override
	public IToken setstat(String path, FileAttrs attrs, final DoneSetStat done) {
		Assert.isTrue(!Protocol.isDispatchThread());
		rendezvous.reset();
		IToken token = service.setstat(path, attrs, new DoneSetStat(){
			/*
			 * (non-Javadoc)
			 * @see org.eclipse.tcf.services.IFileSystem.DoneSetStat#doneSetStat(org.eclipse.tcf.protocol.IToken, org.eclipse.tcf.services.IFileSystem.FileSystemException)
			 */
			@Override
            public void doneSetStat(IToken token, FileSystemException error) {
	            done.doneSetStat(token, error);
	            rendezvous.arrive();
            }});
		try {
			rendezvous.waiting(DEFAULT_TIMEOUT);
		}
		catch(TimeoutException e) {
			throw new RuntimeException(NLS.bind(Messages.BlockingFileSystemProxy_TimeoutSetStat, path), e);
		}
		return token;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.services.IFileSystem#fsetstat(org.eclipse.tcf.services.IFileSystem.IFileHandle, org.eclipse.tcf.services.IFileSystem.FileAttrs, org.eclipse.tcf.services.IFileSystem.DoneSetStat)
	 */
	@Override
	public IToken fsetstat(IFileHandle handle, FileAttrs attrs, final DoneSetStat done) {
		Assert.isTrue(!Protocol.isDispatchThread());
		rendezvous.reset();
		IToken token = service.fsetstat(handle, attrs, new DoneSetStat(){
			/*
			 * (non-Javadoc)
			 * @see org.eclipse.tcf.services.IFileSystem.DoneSetStat#doneSetStat(org.eclipse.tcf.protocol.IToken, org.eclipse.tcf.services.IFileSystem.FileSystemException)
			 */
			@Override
            public void doneSetStat(IToken token, FileSystemException error) {
	            done.doneSetStat(token, error);
	            rendezvous.arrive();
            }});
		try {
			rendezvous.waiting(DEFAULT_TIMEOUT);
		}
		catch(TimeoutException e) {
			throw new RuntimeException(NLS.bind(Messages.BlockingFileSystemProxy_TimeoutFSetStat, handle), e);
		}
		return token;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.services.IFileSystem#opendir(java.lang.String, org.eclipse.tcf.services.IFileSystem.DoneOpen)
	 */
	@Override
	public IToken opendir(String path, final DoneOpen done) {
		Assert.isTrue(!Protocol.isDispatchThread());
		rendezvous.reset();
		IToken token = service.opendir(path, new DoneOpen(){
			/*
			 * (non-Javadoc)
			 * @see org.eclipse.tcf.services.IFileSystem.DoneOpen#doneOpen(org.eclipse.tcf.protocol.IToken, org.eclipse.tcf.services.IFileSystem.FileSystemException, org.eclipse.tcf.services.IFileSystem.IFileHandle)
			 */
			@Override
            public void doneOpen(IToken token, FileSystemException error, IFileHandle handle) {
	            done.doneOpen(token, error, handle);
	            rendezvous.arrive();
            }});
		try {
			rendezvous.waiting(DEFAULT_TIMEOUT);
		}
		catch(TimeoutException e) {
			throw new RuntimeException(NLS.bind(Messages.BlockingFileSystemProxy_TimeoutOpeningDir, path), e);
		}
		return token;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.services.IFileSystem#readdir(org.eclipse.tcf.services.IFileSystem.IFileHandle, org.eclipse.tcf.services.IFileSystem.DoneReadDir)
	 */
	@Override
	public IToken readdir(IFileHandle handle, final DoneReadDir done) {
		Assert.isTrue(!Protocol.isDispatchThread());
		rendezvous.reset();
		IToken token = service.readdir(handle, new DoneReadDir(){
			/*
			 * (non-Javadoc)
			 * @see org.eclipse.tcf.services.IFileSystem.DoneReadDir#doneReadDir(org.eclipse.tcf.protocol.IToken, org.eclipse.tcf.services.IFileSystem.FileSystemException, org.eclipse.tcf.services.IFileSystem.DirEntry[], boolean)
			 */
			@Override
            public void doneReadDir(IToken token, FileSystemException error, DirEntry[] entries, boolean eof) {
	            done.doneReadDir(token, error, entries, eof);
	            rendezvous.arrive();
            }});
		try {
			rendezvous.waiting(DEFAULT_TIMEOUT);
		}
		catch(TimeoutException e) {
			throw new RuntimeException(NLS.bind(Messages.BlockingFileSystemProxy_TimeoutReadingDir, handle), e);
		}
		return token;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.services.IFileSystem#mkdir(java.lang.String, org.eclipse.tcf.services.IFileSystem.FileAttrs, org.eclipse.tcf.services.IFileSystem.DoneMkDir)
	 */
	@Override
	public IToken mkdir(String path, FileAttrs attrs, final DoneMkDir done) {
		Assert.isTrue(!Protocol.isDispatchThread());
		rendezvous.reset();
		IToken token = service.mkdir(path, attrs, new DoneMkDir(){
			/*
			 * (non-Javadoc)
			 * @see org.eclipse.tcf.services.IFileSystem.DoneMkDir#doneMkDir(org.eclipse.tcf.protocol.IToken, org.eclipse.tcf.services.IFileSystem.FileSystemException)
			 */
			@Override
            public void doneMkDir(IToken token, FileSystemException error) {
				done.doneMkDir(token, error);
				rendezvous.arrive();
            }});
		try {
			rendezvous.waiting(DEFAULT_TIMEOUT);
		}
		catch(TimeoutException e) {
			throw new RuntimeException(NLS.bind(Messages.BlockingFileSystemProxy_TimeoutMakingDir, path), e);
		}
		return token;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.services.IFileSystem#rmdir(java.lang.String, org.eclipse.tcf.services.IFileSystem.DoneRemove)
	 */
	@Override
	public IToken rmdir(String path, final DoneRemove done) {
		Assert.isTrue(!Protocol.isDispatchThread());
		rendezvous.reset();
		IToken token = service.rmdir(path, new DoneRemove(){
			/*
			 * (non-Javadoc)
			 * @see org.eclipse.tcf.services.IFileSystem.DoneRemove#doneRemove(org.eclipse.tcf.protocol.IToken, org.eclipse.tcf.services.IFileSystem.FileSystemException)
			 */
			@Override
            public void doneRemove(IToken token, FileSystemException error) {
				done.doneRemove(token, error);
				rendezvous.arrive();
            }});
		try {
			rendezvous.waiting(DEFAULT_TIMEOUT);
		}
		catch(TimeoutException e) {
			throw new RuntimeException(NLS.bind(Messages.BlockingFileSystemProxy_TimeoutRemovingDir, path), e);
		}
		return token;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.services.IFileSystem#roots(org.eclipse.tcf.services.IFileSystem.DoneRoots)
	 */
	@Override
	public IToken roots(final DoneRoots done) {
		Assert.isTrue(!Protocol.isDispatchThread());
		rendezvous.reset();
		IToken token = service.roots(new DoneRoots(){
			/*
			 * (non-Javadoc)
			 * @see org.eclipse.tcf.services.IFileSystem.DoneRoots#doneRoots(org.eclipse.tcf.protocol.IToken, org.eclipse.tcf.services.IFileSystem.FileSystemException, org.eclipse.tcf.services.IFileSystem.DirEntry[])
			 */
			@Override
            public void doneRoots(IToken token, FileSystemException error, DirEntry[] entries) {
				done.doneRoots(token, error, entries);
				rendezvous.arrive();
            }});
		try {
			rendezvous.waiting(DEFAULT_TIMEOUT);
		}
		catch(TimeoutException e) {
			throw new RuntimeException(Messages.BlockingFileSystemProxy_TimeoutListingRoots, e);
		}
		return token;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.services.IFileSystem#remove(java.lang.String, org.eclipse.tcf.services.IFileSystem.DoneRemove)
	 */
	@Override
	public IToken remove(String file_name, final DoneRemove done) {
		Assert.isTrue(!Protocol.isDispatchThread());
		rendezvous.reset();
		IToken token = service.remove(file_name, new DoneRemove(){
			/*
			 * (non-Javadoc)
			 * @see org.eclipse.tcf.services.IFileSystem.DoneRemove#doneRemove(org.eclipse.tcf.protocol.IToken, org.eclipse.tcf.services.IFileSystem.FileSystemException)
			 */
			@Override
            public void doneRemove(IToken token, FileSystemException error) {
				done.doneRemove(token, error);
				rendezvous.arrive();
            }});
		try {
			rendezvous.waiting(DEFAULT_TIMEOUT);
		}
		catch(TimeoutException e) {
			throw new RuntimeException(NLS.bind(Messages.BlockingFileSystemProxy_TimeoutRemovingFile, file_name), e);
		}
		return token;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.services.IFileSystem#realpath(java.lang.String, org.eclipse.tcf.services.IFileSystem.DoneRealPath)
	 */
	@Override
	public IToken realpath(String path, final DoneRealPath done) {
		Assert.isTrue(!Protocol.isDispatchThread());
		rendezvous.reset();
		IToken token = service.realpath(path, new DoneRealPath(){
			/*
			 * (non-Javadoc)
			 * @see org.eclipse.tcf.services.IFileSystem.DoneRealPath#doneRealPath(org.eclipse.tcf.protocol.IToken, org.eclipse.tcf.services.IFileSystem.FileSystemException, java.lang.String)
			 */
			@Override
            public void doneRealPath(IToken token, FileSystemException error, String path) {
	            done.doneRealPath(token, error, path);
	            rendezvous.arrive();
            }});
		try {
			rendezvous.waiting(DEFAULT_TIMEOUT);
		}
		catch(TimeoutException e) {
			throw new RuntimeException(NLS.bind(Messages.BlockingFileSystemProxy_TimeoutGettingRealPath, path), e);
		}
		return token;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.services.IFileSystem#rename(java.lang.String, java.lang.String, org.eclipse.tcf.services.IFileSystem.DoneRename)
	 */
	@Override
	public IToken rename(String old_path, String new_path, final DoneRename done) {
		Assert.isTrue(!Protocol.isDispatchThread());
		rendezvous.reset();
		IToken token = service.rename(old_path, new_path, new DoneRename(){
			/*
			 * (non-Javadoc)
			 * @see org.eclipse.tcf.services.IFileSystem.DoneRename#doneRename(org.eclipse.tcf.protocol.IToken, org.eclipse.tcf.services.IFileSystem.FileSystemException)
			 */
			@Override
            public void doneRename(IToken token, FileSystemException error) {
	            done.doneRename(token, error);
	            rendezvous.arrive();
            }});
		try {
			rendezvous.waiting(DEFAULT_TIMEOUT);
		}
		catch(TimeoutException e) {
			throw new RuntimeException(NLS.bind(Messages.BlockingFileSystemProxy_TimeoutRenamingFile, old_path, new_path), e);
		}
		return token;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.services.IFileSystem#readlink(java.lang.String, org.eclipse.tcf.services.IFileSystem.DoneReadLink)
	 */
	@Override
	public IToken readlink(String path, final DoneReadLink done) {
		Assert.isTrue(!Protocol.isDispatchThread());
		rendezvous.reset();
		IToken token = service.readlink(path, new DoneReadLink(){
			/*
			 * (non-Javadoc)
			 * @see org.eclipse.tcf.services.IFileSystem.DoneReadLink#doneReadLink(org.eclipse.tcf.protocol.IToken, org.eclipse.tcf.services.IFileSystem.FileSystemException, java.lang.String)
			 */
			@Override
            public void doneReadLink(IToken token, FileSystemException error, String path) {
				done.doneReadLink(token, error, path);
				rendezvous.arrive();
            }});
		try {
			rendezvous.waiting(DEFAULT_TIMEOUT);
		}
		catch(TimeoutException e) {
			throw new RuntimeException(NLS.bind(Messages.BlockingFileSystemProxy_TimeoutReadingLink, path), e);
		}
		return token;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.services.IFileSystem#symlink(java.lang.String, java.lang.String, org.eclipse.tcf.services.IFileSystem.DoneSymLink)
	 */
	@Override
	public IToken symlink(String link_path, String target_path, final DoneSymLink done) {
		Assert.isTrue(!Protocol.isDispatchThread());
		rendezvous.reset();
		IToken token = service.symlink(link_path, target_path, new DoneSymLink(){
			/*
			 * (non-Javadoc)
			 * @see org.eclipse.tcf.services.IFileSystem.DoneSymLink#doneSymLink(org.eclipse.tcf.protocol.IToken, org.eclipse.tcf.services.IFileSystem.FileSystemException)
			 */
			@Override
            public void doneSymLink(IToken token, FileSystemException error) {
				done.doneSymLink(token, error);
	            rendezvous.arrive();
            }});
		try {
			rendezvous.waiting(DEFAULT_TIMEOUT);
		}
		catch(TimeoutException e) {
			throw new RuntimeException(NLS.bind(Messages.BlockingFileSystemProxy_TimeoutSymLink, link_path, target_path), e);
		}
		return token;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.services.IFileSystem#copy(java.lang.String, java.lang.String, boolean, boolean, org.eclipse.tcf.services.IFileSystem.DoneCopy)
	 */
	@Override
	public IToken copy(String src_path, String dst_path, boolean copy_permissions, boolean copy_ownership, final DoneCopy done) {
		Assert.isTrue(!Protocol.isDispatchThread());
		rendezvous.reset();
		IToken token = service.copy(src_path, dst_path, copy_permissions, copy_ownership, new DoneCopy(){
			/*
			 * (non-Javadoc)
			 * @see org.eclipse.tcf.services.IFileSystem.DoneCopy#doneCopy(org.eclipse.tcf.protocol.IToken, org.eclipse.tcf.services.IFileSystem.FileSystemException)
			 */
			@Override
            public void doneCopy(IToken token, FileSystemException error) {
	            done.doneCopy(token, error);
	            rendezvous.arrive();
            }});
		try {
			rendezvous.waiting(DEFAULT_TIMEOUT);
		}
		catch(TimeoutException e) {
			throw new RuntimeException(NLS.bind(Messages.BlockingFileSystemProxy_TimeoutCopying, src_path, dst_path), e);
		}
		return token;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.services.IFileSystem#user(org.eclipse.tcf.services.IFileSystem.DoneUser)
	 */
	@Override
	public IToken user(final DoneUser done) {
		Assert.isTrue(!Protocol.isDispatchThread());
		rendezvous.reset();
		IToken token = service.user(new DoneUser(){
			/*
			 * (non-Javadoc)
			 * @see org.eclipse.tcf.services.IFileSystem.DoneUser#doneUser(org.eclipse.tcf.protocol.IToken, org.eclipse.tcf.services.IFileSystem.FileSystemException, int, int, int, int, java.lang.String)
			 */
			@Override
            public void doneUser(IToken token, FileSystemException error, int real_uid, int effective_uid, int real_gid, int effective_gid, String home) {
				done.doneUser(token, error, real_uid, effective_uid, real_gid, effective_gid, home);
				rendezvous.arrive();
            }});
		try {
			rendezvous.waiting(DEFAULT_TIMEOUT);
		}
		catch(TimeoutException e) {
			throw new RuntimeException(Messages.BlockingFileSystemProxy_TimeoutGettingUser, e);
		}
		return token;
	}

}
