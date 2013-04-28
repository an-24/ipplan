/*******************************************************************************
 * Copyright 2011 Google Inc. All Rights Reserved.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package com.cantor.ipplan.server;

import java.io.File;
import java.security.SecureRandom;
import java.sql.SQLException;
import java.text.MessageFormat;

import javax.servlet.http.HttpSession;

import org.firebirdsql.gds.impl.GDSType;
import org.firebirdsql.management.BackupManager;
import org.firebirdsql.management.FBBackupManager;
import org.hibernate.SessionFactory;

import com.cantor.ipplan.client.DatabaseService;
import com.cantor.ipplan.client.LoginService;
import com.gdevelop.gwt.syncrpc.SyncProxy;
import com.google.gwt.user.server.Base64Utils;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public class DatabaseServiceImpl extends RemoteServiceServlet implements DatabaseService {

	@Override
	public String create(String name, String userEmail) throws Exception {
		String dbKey = checkAccess(name,userEmail);
		String url = openStore(name,userEmail);
		IPPlanPoolConnection.setPool(url);
		return dbKey;
	}

	@Override
	public String open(String name, String userEmail) throws Exception {
		String dbKey = checkAccess(name,userEmail);
		String url = openStore(name,userEmail);
		IPPlanPoolConnection.setPool(url);
		return dbKey;
	}

	@Override
	public void close(String key) {
		// TODO Auto-generated method stub
		
	}
	
	private String checkAccess(String name, String userEmail) throws Exception {
		HttpSession sess = this.getThreadLocalRequest().getSession();
		String key = (String) sess.getAttribute("dbKey");
		if (sess.isNew() || key==null ) {
			// проводим проверку через сервер UP
			String host = getServletConfig().getInitParameter("loginCallBack");
			LoginService login = (LoginService) SyncProxy.newProxyInstance(LoginService.class, host,"login");
			if(!login.isAccessDatabase(name,userEmail))
				throw new Exception("Доступ к базе данных запрещен");
			byte[] code = new byte[16];
			new SecureRandom().nextBytes(code);
			key = Base64Utils.toBase64(code);
			sess.setAttribute("dbKey",key);
		}	
		return key;
		
	}
	
	private synchronized String openStore(String name, String userEmail) throws Exception {
		String dir = getServletConfig().getInitParameter("storeLocation");
		if(dir==null)
			throw new Exception("Неверная кофигурация сервера. storeLocation not found. ");
		File root = new File(dir);
		if(!root.exists() || !root.isDirectory())
			throw new Exception(MessageFormat.format("Неверная кофигурация сервера. Directory {0} not found.",new Object[]{dir}));
		
		File userroot = new File(root,name);
		boolean newCatalog = !userroot.exists();
		
		String dbName;
		if(newCatalog) {
			userroot.mkdir();
			dbName = createDatabase(root,name,0);
		} else {
			dbName = "";
		}
		return "jdbc:firebirdsql:localhost:"+dbName;
	}

	private String createDatabase(File root, String name, int ver) throws Exception {
		try {
			String dbname = root.getAbsolutePath()+File.separatorChar+name+File.separatorChar+"db"+ver+".fdb";
			BackupManager restoreManager = new FBBackupManager(GDSType.getType("PURE_JAVA"));
			restoreManager.setHost("localhost");
			restoreManager.setPort(3050);
			restoreManager.setUser("SYSDBA");
			restoreManager.setPassword("masterkey");
			restoreManager.setLogger(System.out);
			restoreManager.setVerbose(true);
			restoreManager.setDatabase(dbname);
			restoreManager.setBackupPath(root.getAbsolutePath()+File.separatorChar+"pattern.fbk");
			restoreManager.restoreDatabase();
			return dbname;
		} catch (Throwable e) {
			throw new Exception(e.getMessage());
		}
	}

}
