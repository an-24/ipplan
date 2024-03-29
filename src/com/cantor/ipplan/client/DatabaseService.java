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
package com.cantor.ipplan.client;

import java.util.Date;
import java.util.List;

import com.cantor.ipplan.shared.BargainTotals;
import com.cantor.ipplan.shared.BargainWrapper;
import com.cantor.ipplan.shared.CostsWrapper;
import com.cantor.ipplan.shared.CustomerWrapper;
import com.cantor.ipplan.shared.ImportExportProcessInfo;
import com.cantor.ipplan.shared.PUserWrapper;
import com.cantor.ipplan.shared.SearchInfo;
import com.cantor.ipplan.shared.StatusWrapper;
import com.cantor.ipplan.shared.TaskWrapper;
import com.cantor.ipplan.shared.TasktypeWrapper;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("database")
public interface DatabaseService extends RemoteService {
	
	public String getConfig(String name) throws Exception;
	
	public PUserWrapper open(String sessId) throws Exception;
	public void exit();
	public PUserWrapper isLogged();
	public BargainTotals[] getTotals() throws Exception;
	
	public List<BargainWrapper> findBargain(String text, Date date, boolean allUser, boolean[] stats)  throws Exception;
	public List<BargainWrapper> attention() throws Exception;
	public BargainWrapper newBargain(String name, int startStatus) throws Exception;
	public BargainWrapper newBargain(String name, int startStatus, Date start, Date finish) throws Exception;
	public BargainWrapper editBargain(int id) throws Exception;
	public boolean deleteBargain(int id) throws Exception;
	public void deleteBargain(List<BargainWrapper> list) throws Exception;
	public List<BargainWrapper> getTemporalyBargains() throws Exception;
	public void dropTemporalyBargain(int id);
	public void saveTemporalyBargain(BargainWrapper bargain) throws Exception;
	public BargainWrapper saveBargain(BargainWrapper bargain, boolean drop) throws Exception;
	public BargainWrapper prevBargainVersion(int id) throws Exception;
	public BargainWrapper nextBargainVersion(int id) throws Exception;
	public boolean isNewVersionBargain(BargainWrapper bargain, boolean savestate) throws Exception;
	
	public List<CustomerWrapper> findCustomer(String query);
	public CustomerWrapper addCustomer(CustomerWrapper value) throws Exception; 
	public void updateCustomer(CustomerWrapper value)  throws Exception; 
	public boolean deleteCustomer(int id) throws Exception; 
	public void deleteCustomer(List<CustomerWrapper> list) throws Exception; 
	
	public List<TasktypeWrapper> getTasktypes() throws Exception;
	public List<TaskWrapper> getTask(int bargainId) throws Exception;
	public TaskWrapper addTask(TaskWrapper task) throws Exception;
	public TaskWrapper updateTask(TaskWrapper task) throws Exception;
	public void executedTask(int id) throws Exception;
	public boolean deleteTask(int id) throws Exception;
	
	public List<StatusWrapper> getAllStatuses();
	public List<CostsWrapper> findCost(String newtext);
	public void refreshGoogleToken() throws Exception;
	public ImportExportProcessInfo syncContacts() throws Exception;
	public void setContactsAutoSync(int duration) throws Exception;
	public void setCalendarAutoSync(int duration) throws Exception;
	public ImportExportProcessInfo syncCalendar() throws Exception;
	
	public SearchInfo searchFile(int typeDrive, String searchStr) throws Exception;
	
}
